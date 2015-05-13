/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.pagehelper.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 将sqlserver查询语句转换为分页语句<br>
 * 注意事项：<br>
 * <ol>
 * <li>请先保证你的SQL可以执行</li>
 * <li>sql中最好直接包含order by，可以自动从sql提取</li>
 * <li>如果没有order by，可以通过入参提供，但是需要自己保证正确</li>
 * <li>如果sql有order by，可以通过orderby参数覆盖sql中的order by</li>
 * <li>order by的列名不能使用别名</li>
 * <li>表和列使用别名的时候不要使用单引号(')</li>
 * </ol>
 * 该类设计为一个独立的工具类，依赖jsqlparser,可以独立使用
 *
 * @author liuzh
 */
public class SqlServer {
    //缓存结果
    private static final Map<String, String> CACHE = new ConcurrentHashMap<String, String>();
    //开始行号
    private static final String START_ROW = String.valueOf(Long.MIN_VALUE);
    //结束行号
    private static final String PAGE_SIZE = String.valueOf(Long.MAX_VALUE);
    //外层包装表
    private static final String WRAP_TABLE = "WRAP_OUTER_TABLE";
    //表别名名字
    private static final String PAGE_TABLE_NAME = "PAGE_TABLE_ALIAS";
    //private
    public static final Alias PAGE_TABLE_ALIAS = new Alias(PAGE_TABLE_NAME);
    //行号
    private static final String PAGE_ROW_NUMBER = "PAGE_ROW_NUMBER";
    //行号列
    private static final Column PAGE_ROW_NUMBER_COLUMN = new Column(PAGE_ROW_NUMBER);
    //TOP 100 PERCENT
    private static final Top TOP100_PERCENT;

    //静态方法处理
    static {
        TOP100_PERCENT = new Top();
        TOP100_PERCENT.setRowCount(100);
        TOP100_PERCENT.setPercentage(true);
    }

    /**
     * 转换为分页语句
     *
     * @param sql
     * @param offset
     * @param limit
     * @return
     */
    public String convertToPageSql(String sql, int offset, int limit) {
        String pageSql = CACHE.get(sql);
        if (pageSql == null) {
            //解析SQL
            Statement stmt;
            try {
                stmt = CCJSqlParserUtil.parse(sql);
            } catch (Throwable e) {
                throw new RuntimeException("不支持该SQL转换为分页查询!");
            }
            if (!(stmt instanceof Select)) {
                throw new RuntimeException("分页语句必须是Select查询!");
            }
            //获取分页查询的select
            Select pageSelect = getPageSelect((Select) stmt);
            pageSql = pageSelect.toString();
            CACHE.put(sql, pageSql);
        }
        pageSql = pageSql.replace(START_ROW, String.valueOf(offset));
        pageSql = pageSql.replace(PAGE_SIZE, String.valueOf(limit));
        return pageSql;
    }

    /**
     * 获取一个外层包装的TOP查询
     *
     * @param select
     * @return
     */
    private Select getPageSelect(Select select) {
        SelectBody selectBody = select.getSelectBody();
        if (selectBody instanceof SetOperationList) {
            selectBody = wrapSetOperationList((SetOperationList) selectBody);
        }
        //这里的selectBody一定是PlainSelect
        if (((PlainSelect) selectBody).getTop() != null) {
            throw new RuntimeException("被分页的语句已经包含了Top，不能再通过分页插件进行分页查询!");
        }
        //获取查询列
        List<SelectItem> selectItems = getSelectItems((PlainSelect) selectBody);
        //对一层的SQL增加ROW_NUMBER()
        addRowNumber((PlainSelect) selectBody);
        //处理子语句中的order by
        processSelectBody(selectBody, 0);

        //新建一个select
        Select newSelect = new Select();
        PlainSelect newSelectBody = new PlainSelect();
        //设置top
        Top top = new Top();
        top.setRowCount(Long.MAX_VALUE);
        newSelectBody.setTop(top);
        //设置order by
        List<OrderByElement> orderByElements = new ArrayList<OrderByElement>();
        OrderByElement orderByElement = new OrderByElement();
        orderByElement.setExpression(PAGE_ROW_NUMBER_COLUMN);
        orderByElements.add(orderByElement);
        newSelectBody.setOrderByElements(orderByElements);
        //设置where
        GreaterThan greaterThan = new GreaterThan();
        greaterThan.setLeftExpression(PAGE_ROW_NUMBER_COLUMN);
        greaterThan.setRightExpression(new LongValue(Long.MIN_VALUE));
        newSelectBody.setWhere(greaterThan);
        //设置selectItems
        newSelectBody.setSelectItems(selectItems);
        //设置fromIterm
        SubSelect fromItem = new SubSelect();
        fromItem.setSelectBody(selectBody);
        fromItem.setAlias(PAGE_TABLE_ALIAS);
        newSelectBody.setFromItem(fromItem);

        newSelect.setSelectBody(newSelectBody);
        if (isNotEmptyList(select.getWithItemsList())) {
            newSelect.setWithItemsList(select.getWithItemsList());
        }
        return newSelect;
    }

    /**
     * 包装SetOperationList
     *
     * @param setOperationList
     * @return
     */
    private SelectBody wrapSetOperationList(SetOperationList setOperationList) {
        //获取最后一个plainSelect
        PlainSelect plainSelect = setOperationList.getPlainSelects().get(setOperationList.getPlainSelects().size() - 1);

        PlainSelect selectBody = new PlainSelect();
        List<SelectItem> selectItems = getSelectItems(plainSelect);
        selectBody.setSelectItems(selectItems);

        //设置fromIterm
        SubSelect fromItem = new SubSelect();
        fromItem.setSelectBody(setOperationList);
        fromItem.setAlias(new Alias(WRAP_TABLE));
        selectBody.setFromItem(fromItem);
        //order by
        if (isNotEmptyList(plainSelect.getOrderByElements())) {
            selectBody.setOrderByElements(plainSelect.getOrderByElements());
            plainSelect.setOrderByElements(null);
        }
        return selectBody;
    }

    /**
     * 获取查询列
     *
     * @param plainSelect
     * @return
     */
    private List<SelectItem> getSelectItems(PlainSelect plainSelect) {
        //设置selectItems
        List<SelectItem> selectItems = new ArrayList<SelectItem>();
        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            //别名需要特殊处理
            if (selectItem instanceof SelectExpressionItem) {
                SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                if (selectExpressionItem.getAlias() != null) {
                    //直接使用别名
                    Column column = new Column(selectExpressionItem.getAlias().getName());
                    SelectExpressionItem expressionItem = new SelectExpressionItem(column);
                    selectItems.add(expressionItem);
                } else if (selectExpressionItem.getExpression() instanceof Column) {
                    Column column = (Column) selectExpressionItem.getExpression();
                    SelectExpressionItem item = null;
                    if (column.getTable() != null) {
                        Column newColumn = new Column(column.getColumnName());
                        item = new SelectExpressionItem(newColumn);
                        selectItems.add(item);
                    } else {
                        selectItems.add(selectItem);
                    }
                } else {
                    selectItems.add(selectItem);
                }
            } else if (selectItem instanceof AllTableColumns) {
                selectItems.add(new AllColumns());
            } else {
                selectItems.add(selectItem);
            }
        }
        return selectItems;
    }

    /**
     * 最外层的SQL查询需要增加ROW_NUMBER()
     *
     * @param plainSelect
     */
    private void addRowNumber(PlainSelect plainSelect) {
        //增加ROW_NUMBER()
        StringBuilder orderByBuilder = new StringBuilder();
        orderByBuilder.append("ROW_NUMBER() OVER (");
        if (isNotEmptyList(plainSelect.getOrderByElements())) {
            //注意：order by别名的时候有错,由于没法判断一个列是否为别名，所以不能解决
            orderByBuilder.append(PlainSelect.orderByToString(false, plainSelect.getOrderByElements()));
        } else {
            throw new RuntimeException("请您在sql中包含order by语句!");
        }
        //需要把改orderby清空
        if (isNotEmptyList(plainSelect.getOrderByElements())) {
            plainSelect.setOrderByElements(null);
        }
        orderByBuilder.append(") ");
        orderByBuilder.append(PAGE_ROW_NUMBER);
        Column orderByColumn = new Column(orderByBuilder.toString());
        plainSelect.getSelectItems().add(0, new SelectExpressionItem(orderByColumn));
    }

    /**
     * 处理selectBody去除Order by
     *
     * @param selectBody
     */
    private void processSelectBody(SelectBody selectBody, int level) {
        if (selectBody instanceof PlainSelect) {
            processPlainSelect((PlainSelect) selectBody, level + 1);
        } else if (selectBody instanceof WithItem) {
            WithItem withItem = (WithItem) selectBody;
            if (withItem.getSelectBody() != null) {
                processSelectBody(withItem.getSelectBody(), level + 1);
            }
        } else {
            SetOperationList operationList = (SetOperationList) selectBody;
            if (operationList.getPlainSelects() != null && operationList.getPlainSelects().size() > 0) {
                List<PlainSelect> plainSelects = operationList.getPlainSelects();
                for (PlainSelect plainSelect : plainSelects) {
                    processPlainSelect(plainSelect, level + 1);
                }
            }
        }
    }

    /**
     * 处理PlainSelect类型的selectBody
     *
     * @param plainSelect
     */
    private void processPlainSelect(PlainSelect plainSelect, int level) {
        if (level > 1) {
            if (isNotEmptyList(plainSelect.getOrderByElements())) {
                if (plainSelect.getTop() == null) {
                    plainSelect.setTop(TOP100_PERCENT);
                }
            }
        }
        if (plainSelect.getFromItem() != null) {
            processFromItem(plainSelect.getFromItem(), level + 1);
        }
        if (plainSelect.getJoins() != null && plainSelect.getJoins().size() > 0) {
            List<Join> joins = plainSelect.getJoins();
            for (Join join : joins) {
                if (join.getRightItem() != null) {
                    processFromItem(join.getRightItem(), level + 1);
                }
            }
        }
    }

    /**
     * 处理子查询
     *
     * @param fromItem
     */
    private void processFromItem(FromItem fromItem, int level) {
        if (fromItem instanceof SubJoin) {
            SubJoin subJoin = (SubJoin) fromItem;
            if (subJoin.getJoin() != null) {
                if (subJoin.getJoin().getRightItem() != null) {
                    processFromItem(subJoin.getJoin().getRightItem(), level + 1);
                }
            }
            if (subJoin.getLeft() != null) {
                processFromItem(subJoin.getLeft(), level + 1);
            }
        } else if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) fromItem;
            if (subSelect.getSelectBody() != null) {
                processSelectBody(subSelect.getSelectBody(), level + 1);
            }
        } else if (fromItem instanceof ValuesList) {

        } else if (fromItem instanceof LateralSubSelect) {
            LateralSubSelect lateralSubSelect = (LateralSubSelect) fromItem;
            if (lateralSubSelect.getSubSelect() != null) {
                SubSelect subSelect = lateralSubSelect.getSubSelect();
                if (subSelect.getSelectBody() != null) {
                    processSelectBody(subSelect.getSelectBody(), level + 1);
                }
            }
        }
        //Table时不用处理
    }

    /**
     * List不空
     *
     * @param list
     * @return
     */
    private boolean isNotEmptyList(List<?> list) {
        if (list == null || list.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 字符串不空
     *
     * @param str
     * @return
     */
    private boolean isNotEmptyString(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        return true;
    }
}
