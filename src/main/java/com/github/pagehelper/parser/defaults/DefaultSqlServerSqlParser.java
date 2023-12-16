/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2023 abel533@gmail.com
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

package com.github.pagehelper.parser.defaults;

import com.github.pagehelper.PageException;
import com.github.pagehelper.parser.SqlParserUtil;
import com.github.pagehelper.parser.SqlServerSqlParser;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;

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
public class DefaultSqlServerSqlParser implements SqlServerSqlParser {
    //开始行号
    public static final    String     START_ROW                = String.valueOf(Long.MIN_VALUE);
    //结束行号
    public static final    String     PAGE_SIZE                = String.valueOf(Long.MAX_VALUE);
    //外层包装表
    protected static final String     WRAP_TABLE               = "WRAP_OUTER_TABLE";
    //表别名名字
    protected static final String     PAGE_TABLE_NAME          = "PAGE_TABLE_ALIAS";
    //protected
    public static final    Alias      PAGE_TABLE_ALIAS         = new Alias(PAGE_TABLE_NAME);
    //行号
    protected static final String     PAGE_ROW_NUMBER          = "PAGE_ROW_NUMBER";
    //行号列
    protected static final Column     PAGE_ROW_NUMBER_COLUMN   = new Column(PAGE_ROW_NUMBER);
    //TOP 100 PERCENT
    protected static final Top        TOP100_PERCENT;
    //别名前缀
    protected static final String     PAGE_COLUMN_ALIAS_PREFIX = "ROW_ALIAS_";

    //静态方法处理
    static {
        TOP100_PERCENT = new Top();
        TOP100_PERCENT.setExpression(new LongValue(100));
        TOP100_PERCENT.setPercentage(true);
    }

    /**
     * 转换为分页语句
     *
     * @param sql
     * @return
     */
    public String convertToPageSql(String sql) {
        return convertToPageSql(sql, null, null);
    }

    /**
     * 转换为分页语句
     *
     * @param sql
     * @param offset
     * @param limit
     * @return
     */
    public String convertToPageSql(String sql, Integer offset, Integer limit) {
        //解析SQL
        Statement stmt;
        try {
            stmt = SqlParserUtil.parse(sql);
        } catch (Throwable e) {
            throw new PageException("The SQL statement cannot be converted to a pagination query!", e);
        }
        if (!(stmt instanceof Select)) {
            throw new PageException("the pagination statement must be a select query!");
        }
        //获取分页查询的select
        Select pageSelect = getPageSelect((Select) stmt);
        String pageSql = pageSelect.toString();
        //缓存移到外面了，所以不替换参数
        if (offset != null) {
            pageSql = pageSql.replace(START_ROW, String.valueOf(offset));
        }
        if (limit != null) {
            pageSql = pageSql.replace(PAGE_SIZE, String.valueOf(limit));
        }
        return pageSql;
    }

    /**
     * 获取一个外层包装的TOP查询
     *
     * @param select
     * @return
     */
    protected Select getPageSelect(Select select) {
        if (select instanceof SetOperationList) {
            select = wrapSetOperationList((SetOperationList) select);
        }
        //这里的selectBody一定是PlainSelect
        if (((PlainSelect) select).getTop() != null) {
            throw new PageException("The pagination statement already contains the top, and can no longer be used to query the pagination plugin!");
        }
        //获取查询列
        List<SelectItem<?>> selectItems = getSelectItems((PlainSelect) select);
        //对一层的SQL增加ROW_NUMBER()
        List<SelectItem<?>> autoItems = new ArrayList<>();
        SelectItem<?> orderByColumn = addRowNumber((PlainSelect) select, autoItems);
        //加入自动生成列
        ((PlainSelect) select).addSelectItems(autoItems.toArray(new SelectItem[0]));
        //处理子语句中的order by
        processSelectBody(select, 0);

        //中层子查询
        PlainSelect innerSelectBody = new PlainSelect();
        //PAGE_ROW_NUMBER
        innerSelectBody.addSelectItems(orderByColumn);
        innerSelectBody.addSelectItems(selectItems.toArray(new SelectItem[0]));
        //将原始查询作为内层子查询
        ParenthesedSelect fromInnerItem = new ParenthesedSelect();
        fromInnerItem.setSelect(select);
        fromInnerItem.setAlias(PAGE_TABLE_ALIAS);
        innerSelectBody.setFromItem(fromInnerItem);

        //新建一个select
        PlainSelect newSelect = new PlainSelect();
        //设置top
        Top top = new Top();
        top.setExpression(new LongValue(Long.MAX_VALUE));
        newSelect.setTop(top);
        //设置order by
        List<OrderByElement> orderByElements = new ArrayList<OrderByElement>();
        OrderByElement orderByElement = new OrderByElement();
        orderByElement.setExpression(PAGE_ROW_NUMBER_COLUMN);
        orderByElements.add(orderByElement);
        newSelect.setOrderByElements(orderByElements);
        //设置where
        GreaterThan greaterThan = new GreaterThan();
        greaterThan.setLeftExpression(PAGE_ROW_NUMBER_COLUMN);
        greaterThan.setRightExpression(new LongValue(Long.MIN_VALUE));
        newSelect.setWhere(greaterThan);
        //设置selectItems
        newSelect.setSelectItems(selectItems);
        //设置fromIterm
        ParenthesedSelect fromItem = new ParenthesedSelect();
        fromItem.setSelect(innerSelectBody); //中层子查询
        fromItem.setAlias(PAGE_TABLE_ALIAS);
        newSelect.setFromItem(fromItem);

        if (isNotEmptyList(select.getWithItemsList())) {
            newSelect.setWithItemsList(select.getWithItemsList());
            select.setWithItemsList(null);
        }
        return newSelect;
    }

    /**
     * 包装SetOperationList
     *
     * @param setOperationList
     * @return
     */
    protected Select wrapSetOperationList(SetOperationList setOperationList) {
        //获取最后一个plainSelect
        Select setSelectBody = setOperationList.getSelects().get(setOperationList.getSelects().size() - 1);
        if (!(setSelectBody instanceof PlainSelect)) {
            throw new PageException("Unable to process the SQL, you can submit issues in GitHub for help.!");
        }
        PlainSelect plainSelect = (PlainSelect) setSelectBody;
        PlainSelect selectBody = new PlainSelect();
        List<SelectItem<?>> selectItems = getSelectItems(plainSelect);
        selectBody.setSelectItems(selectItems);

        //设置fromIterm
        ParenthesedSelect fromItem = new ParenthesedSelect();
        fromItem.setSelect(setOperationList);
        fromItem.setAlias(new Alias(WRAP_TABLE));
        selectBody.setFromItem(fromItem);
        //order by
        if (isNotEmptyList(setOperationList.getOrderByElements())) {
            selectBody.setOrderByElements(setOperationList.getOrderByElements());
            setOperationList.setOrderByElements(null);
        }
        return selectBody;
    }

    /**
     * 获取查询列
     *
     * @param plainSelect
     * @return
     */
    protected List<SelectItem<?>> getSelectItems(PlainSelect plainSelect) {
        //设置selectItems
        List<SelectItem<?>> selectItems = new ArrayList<>();
        for (SelectItem<?> selectItem : plainSelect.getSelectItems()) {
            if (selectItem.getExpression() instanceof AllTableColumns) {
                selectItems.add(new SelectItem<>(new AllColumns()));
            } else if (selectItem.getAlias() != null) {
                //直接使用别名
                Column column = new Column(selectItem.getAlias().getName());
                SelectItem<?> expressionItem = new SelectItem<>(column);
                selectItems.add(expressionItem);
            } else if (selectItem.getExpression() instanceof Column) {
                Column column = (Column) selectItem.getExpression();
                SelectItem<?> item = null;
                if (column.getTable() != null) {
                    Column newColumn = new Column(column.getColumnName());
                    item = new SelectItem<>(newColumn);
                    selectItems.add(item);
                } else {
                    selectItems.add(selectItem);
                }
            } else {
                selectItems.add(selectItem);
            }
        }
        // SELECT *, 1 AS alias FROM TEST
        // 应该为
        // SELECT * FROM (SELECT *, 1 AS alias FROM TEST)
        // 不应该为
        // SELECT *, alias FROM (SELECT *, 1 AS alias FROM TEST)
        for (SelectItem<?> selectItem : selectItems) {
            if (selectItem.getExpression() instanceof AllColumns) {
                return Collections.singletonList(selectItem);
            }
        }
        return selectItems;
    }

    /**
     * 获取 ROW_NUMBER() 列
     *
     * @param plainSelect 原查询
     * @param autoItems   自动生成的查询列
     * @return ROW_NUMBER() 列
     */
    protected SelectItem<?> addRowNumber(PlainSelect plainSelect, List<SelectItem<?>> autoItems) {
        //增加ROW_NUMBER()
        StringBuilder orderByBuilder = new StringBuilder();
        orderByBuilder.append("ROW_NUMBER() OVER (");
        if (isNotEmptyList(plainSelect.getOrderByElements())) {
            orderByBuilder.append(PlainSelect.orderByToString(
                    getOrderByElements(plainSelect, autoItems)).substring(1));
            //清空排序列表
            plainSelect.setOrderByElements(null);
        } else {
            orderByBuilder.append("ORDER BY RAND()");
        }
        orderByBuilder.append(") ");
        orderByBuilder.append(PAGE_ROW_NUMBER);
        return new SelectItem<>(new Column(orderByBuilder.toString()));
    }

    /**
     * 处理selectBody去除Order by
     *
     * @param select
     */
    protected void processSelectBody(Select select, int level) {
        if (select != null) {
            if (select instanceof PlainSelect) {
                processPlainSelect((PlainSelect) select, level + 1);
            } else if (select instanceof WithItem) {
                WithItem withItem = (WithItem) select;
                if (withItem.getSelect() != null) {
                    processSelectBody(withItem.getSelect(), level + 1);
                }
            } else {
                SetOperationList operationList = (SetOperationList) select;
                if (operationList.getSelects() != null && !operationList.getSelects().isEmpty()) {
                    List<Select> plainSelects = operationList.getSelects();
                    for (Select plainSelect : plainSelects) {
                        processSelectBody(plainSelect, level + 1);
                    }
                }
            }
        }
    }

    /**
     * 处理PlainSelect类型的selectBody
     *
     * @param plainSelect
     */
    protected void processPlainSelect(PlainSelect plainSelect, int level) {
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
        if (plainSelect.getJoins() != null && !plainSelect.getJoins().isEmpty()) {
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
    protected void processFromItem(FromItem fromItem, int level) {
        if (fromItem instanceof LateralSubSelect) {
            processSelectBody(((LateralSubSelect) fromItem).getSelect(), level + 1);
        } else if (fromItem instanceof ParenthesedSelect) {
            processSelectBody(((ParenthesedSelect) fromItem).getSelect(), level + 1);
        } else if (fromItem instanceof Select) {
            processSelectBody((Select) fromItem, level + 1);
        } else if (fromItem instanceof ParenthesedFromItem) {
            processFromItem(((ParenthesedFromItem) fromItem).getFromItem(), level + 1);
        }
        //Table时不用处理
    }

    /**
     * List不空
     *
     * @param list
     * @return
     */
    public boolean isNotEmptyList(List<?> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 复制 OrderByElement
     *
     * @param orig  原 OrderByElement
     * @param alias 新 OrderByElement 的排序要素
     * @return 复制的新 OrderByElement
     */
    protected OrderByElement cloneOrderByElement(OrderByElement orig, String alias) {
        return cloneOrderByElement(orig, new Column(alias));
    }

    /**
     * 复制 OrderByElement
     *
     * @param orig       原 OrderByElement
     * @param expression 新 OrderByElement 的排序要素
     * @return 复制的新 OrderByElement
     */
    protected OrderByElement cloneOrderByElement(OrderByElement orig, Expression expression) {
        OrderByElement element = new OrderByElement();
        element.setAsc(orig.isAsc());
        element.setAscDescPresent(orig.isAscDescPresent());
        element.setNullOrdering(orig.getNullOrdering());
        element.setExpression(expression);
        return element;
    }

    /**
     * 获取新的排序列表
     *
     * @param plainSelect 原始查询
     * @param autoItems   生成的新查询要素
     * @return 新的排序列表
     */
    protected List<OrderByElement> getOrderByElements(PlainSelect plainSelect,
                                                      List<SelectItem<?>> autoItems) {
        List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
        ListIterator<OrderByElement> iterator = orderByElements.listIterator();
        OrderByElement orderByElement;

        // 非 `*` 且 非 `t.*` 查询列集合
        Map<String, SelectItem<?>> selectMap = new HashMap<>();
        // 别名集合
        Set<String> aliases = new HashSet<String>();
        // 是否包含 `*` 查询列
        boolean allColumns = false;
        // `t.*` 查询列的表名集合
        Set<String> allColumnsTables = new HashSet<String>();

        for (SelectItem<?> item : plainSelect.getSelectItems()) {
            Expression expression = item.getExpression();
            if (expression instanceof AllTableColumns) {
                allColumnsTables.add(((AllTableColumns) expression).getTable().getName());
            } else if (expression instanceof AllColumns) {
                allColumns = true;
            } else {
                selectMap.put(expression.toString(), item);
                Alias alias = item.getAlias();
                if (alias != null) {
                    aliases.add(alias.getName());
                }
            }
        }

        // 开始遍历 OrderByElement 列表
        int aliasNo = 1;
        while (iterator.hasNext()) {
            orderByElement = iterator.next();
            Expression expression = orderByElement.getExpression();
            SelectItem<?> selectExpressionItem = selectMap.get(expression.toString());
            if (selectExpressionItem != null) { // OrderByElement 在查询列表中
                Alias alias = selectExpressionItem.getAlias();
                if (alias != null) { // 查询列含有别名时用查询列别名
                    iterator.set(cloneOrderByElement(orderByElement, alias.getName()));

                } else { // 查询列不包含别名
                    if (expression instanceof Column) {
                        // 查询列为普通列，这时因为列在嵌套查询外时名称中不包含表名，故去除排序列的表名引用
                        // 例（仅为解释此处逻辑，不代表最终分页结果）：
                        // SELECT TEST.A FROM TEST ORDER BY TEST.A
                        // -->
                        // SELECT A FROM (SELECT TEST.A FROM TEST) ORDER BY A
                        ((Column) expression).setTable(null);

                    } else {
                        // 查询列不为普通列时（例如函数列）不支持分页
                        // 此种情况比较难预测，简单的增加新列容易产生不可预料的结果
                        // 而为列增加别名是非常简单的，故此要求排序复杂列必须使用别名
                        throw new PageException("The column \"" + expression + "\" needs to define an alias");
                    }
                }

            } else { // OrderByElement 不在查询列表中，需要自动生成一个查询列
                if (expression instanceof Column) { // OrderByElement 为普通列
                    Table table = ((Column) expression).getTable();
                    if (table == null) { // 表名为空
                        if (allColumns ||
                                (allColumnsTables.size() == 1 && plainSelect.getJoins() == null) ||
                                aliases.contains(((Column) expression).getColumnName())) {
                            // 包含`*`查询列 或者 只有一个 `t.*`列且为单表查询 或者 其实排序列是一个别名
                            // 此时排序列其实已经包含在查询列表中了，不需做任何操作
                            continue;
                        }

                    } else { //表名不为空
                        String tableName = table.getName();
                        if (allColumns || allColumnsTables.contains(tableName)) {
                            // 包含`*`查询列 或者 包含特定的`t.*`列
                            // 此时排序列其实已经包含在查询列表中了，只需去除排序列的表名引
                            ((Column) expression).setTable(null);
                            continue;
                        }
                    }
                }

                // 将排序列加入查询列中
                String aliasName = PAGE_COLUMN_ALIAS_PREFIX + aliasNo++;

                SelectItem<?> item = new SelectItem<>(expression);
                item.setAlias(new Alias(aliasName));
                autoItems.add(item);

                iterator.set(cloneOrderByElement(orderByElement, aliasName));
            }
        }

        return orderByElements;
    }
}
