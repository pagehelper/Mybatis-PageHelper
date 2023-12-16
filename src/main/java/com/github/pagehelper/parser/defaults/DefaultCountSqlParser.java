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

import com.github.pagehelper.page.PageMethod;
import com.github.pagehelper.parser.CountSqlParser;
import com.github.pagehelper.parser.SqlParserUtil;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.parser.Token;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.*;

/**
 * sql解析类，提供更智能的count查询sql
 *
 * @author liuzh
 */
public class DefaultCountSqlParser implements CountSqlParser {
    public static final    String KEEP_ORDERBY = "/*keep orderby*/";
    protected static final Alias  TABLE_ALIAS;

    protected final Set<String> skipFunctions  = Collections.synchronizedSet(new HashSet<>());
    protected final Set<String> falseFunctions = Collections.synchronizedSet(new HashSet<>());

    static {
        TABLE_ALIAS = new Alias("table_count");
        TABLE_ALIAS.setUseAs(false);
    }

    /**
     * 获取智能的countSql
     *
     * @param sql
     * @param countColumn 列名，默认 0
     * @return
     */
    @Override
    public String getSmartCountSql(String sql, String countColumn) {
        //解析SQL
        Statement stmt = null;
        //特殊sql不需要去掉order by时，使用注释前缀
        if (sql.indexOf(KEEP_ORDERBY) >= 0 || keepOrderBy()) {
            return getSimpleCountSql(sql, countColumn);
        }
        try {
            stmt = SqlParserUtil.parse(sql);
        } catch (Throwable e) {
            //无法解析的用一般方法返回count语句
            return getSimpleCountSql(sql, countColumn);
        }
        Select select = (Select) stmt;
        try {
            //处理body-去order by
            processSelect(select);
        } catch (Exception e) {
            //当 sql 包含 group by 时，不去除 order by
            return getSimpleCountSql(sql, countColumn);
        }
        //处理with-去order by
        processWithItemsList(select.getWithItemsList());
        //处理为count查询
        Select countSelect = sqlToCount(select, countColumn);
        String result = countSelect.toString();
        if (select instanceof PlainSelect) {
            Token token = select.getASTNode().jjtGetFirstToken().specialToken;
            if (token != null) {
                String hints = token.toString().trim();
                // 这里判断是否存在hint, 且result是不包含hint的
                if (hints.startsWith("/*") && hints.endsWith("*/") && !result.startsWith("/*")) {
                    result = hints + result;
                }
            }
        }
        return result;
    }

    /**
     * 获取普通的Count-sql
     *
     * @param sql 原查询sql
     * @return 返回count查询sql
     */
    public String getSimpleCountSql(final String sql) {
        return getSimpleCountSql(sql, "0");
    }

    /**
     * 获取普通的Count-sql
     *
     * @param sql 原查询sql
     * @return 返回count查询sql
     */
    public String getSimpleCountSql(final String sql, String name) {
        StringBuilder stringBuilder = new StringBuilder(sql.length() + 40);
        stringBuilder.append("select count(");
        stringBuilder.append(name);
        stringBuilder.append(") from ( \n");
        stringBuilder.append(sql);
        stringBuilder.append("\n ) tmp_count");
        return stringBuilder.toString();
    }

    /**
     * 将sql转换为count查询
     *
     * @param select
     */
    public Select sqlToCount(Select select, String name) {
        // 是否能简化count查询
        List<SelectItem<?>> COUNT_ITEM = new ArrayList<>();
        COUNT_ITEM.add(new SelectItem(new Column("count(" + name + ")")));
        if (select instanceof PlainSelect && isSimpleCount((PlainSelect) select)) {
            ((PlainSelect) select).setSelectItems(COUNT_ITEM);
            return select;
        } else {
            PlainSelect plainSelect = new PlainSelect();
            ParenthesedSelect subSelect = new ParenthesedSelect();
            subSelect.setSelect(select);
            subSelect.setAlias(TABLE_ALIAS);
            plainSelect.setFromItem(subSelect);
            plainSelect.setSelectItems(COUNT_ITEM);
            if (select.getWithItemsList() != null) {
                plainSelect.setWithItemsList(select.getWithItemsList());
                select.setWithItemsList(null);
            }
            return plainSelect;
        }
    }

    /**
     * 是否可以用简单的count查询方式
     *
     * @param select
     * @return
     */
    public boolean isSimpleCount(PlainSelect select) {
        //包含group by的时候不可以
        if (select.getGroupBy() != null) {
            return false;
        }
        //包含distinct的时候不可以
        if (select.getDistinct() != null) {
            return false;
        }
        //#606,包含having时不可以
        if (select.getHaving() != null) {
            return false;
        }
        for (SelectItem<?> item : select.getSelectItems()) {
            //select列中包含参数的时候不可以，否则会引起参数个数错误
            if (item.toString().contains("?")) {
                return false;
            }
            //如果查询列中包含函数，也不可以，函数可能会聚合列
            Expression expression = item.getExpression();
            if (expression instanceof Function) {
                String name = ((Function) expression).getName();
                if (name != null) {
                    String NAME = name.toUpperCase();
                    if (skipFunctions.contains(NAME)) {
                        //go on
                    } else if (falseFunctions.contains(NAME)) {
                        return false;
                    } else {
                        for (String aggregateFunction : AGGREGATE_FUNCTIONS) {
                            if (NAME.startsWith(aggregateFunction)) {
                                falseFunctions.add(NAME);
                                return false;
                            }
                        }
                        skipFunctions.add(NAME);
                    }
                }
            } else if (expression instanceof Parenthesis && item.getAlias() != null) {
                //#555，当存在 (a+b) as c 时，c 如果出现了 order by 或者 having 中时，会找不到对应的列，
                // 这里想要更智能，需要在整个SQL中查找别名出现的位置，暂时不考虑，直接排除
                return false;
            }
        }
        return true;
    }

    /**
     * 处理selectBody去除Order by
     *
     * @param select
     */
    public void processSelect(Select select) {
        if (select != null) {
            if (select instanceof PlainSelect) {
                processPlainSelect((PlainSelect) select);
            } else if (select instanceof ParenthesedSelect) {
                processSelect(((ParenthesedSelect) select).getSelect());
            } else if (select instanceof SetOperationList) {
                List<Select> selects = ((SetOperationList) select).getSelects();
                for (Select sel : selects) {
                    processSelect(sel);
                }
                if (!orderByHashParameters(select.getOrderByElements())) {
                    select.setOrderByElements(null);
                }
            }
            /*
            if (select instanceof WithItem) {
                WithItem withItem = (WithItem) selectBody;
                if (withItem.getSubSelect() != null && !keepSubSelectOrderBy()) {
                    processSelectBody(withItem.getSubSelect().getSelectBody());
                }
            }
             */
        }
    }

    /**
     * 处理PlainSelect类型的selectBody
     *
     * @param plainSelect
     */
    public void processPlainSelect(PlainSelect plainSelect) {
        if (!orderByHashParameters(plainSelect.getOrderByElements())) {
            plainSelect.setOrderByElements(null);
        }
        if (plainSelect.getFromItem() != null) {
            processFromItem(plainSelect.getFromItem());
        }
        if (plainSelect.getJoins() != null && plainSelect.getJoins().size() > 0) {
            List<Join> joins = plainSelect.getJoins();
            for (Join join : joins) {
                if (join.getRightItem() != null) {
                    processFromItem(join.getRightItem());
                }
            }
        }
    }

    /**
     * 处理WithItem
     *
     * @param withItemsList
     */
    public void processWithItemsList(List<WithItem> withItemsList) {
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem item : withItemsList) {
                if (item.getSelect() != null && !keepSubSelectOrderBy()) {
                    processSelect(item.getSelect());
                }
            }
        }
    }

    /**
     * 处理子查询
     *
     * @param fromItem
     */
    public void processFromItem(FromItem fromItem) {
        if (fromItem instanceof ParenthesedSelect) {
            ParenthesedSelect parenthesedSelect = (ParenthesedSelect) fromItem;
            if (parenthesedSelect.getSelect() != null && !keepSubSelectOrderBy()) {
                processSelect(parenthesedSelect.getSelect());
            }
        } else if (fromItem instanceof Select) {
            processSelect((Select) fromItem);
        } else if (fromItem instanceof ParenthesedFromItem) {
            ParenthesedFromItem parenthesedFromItem = (ParenthesedFromItem) fromItem;
            processFromItem(parenthesedFromItem.getFromItem());
        }
        //Table时不用处理
    }

    /**
     * 保留 order by
     */
    protected boolean keepOrderBy() {
        return PageMethod.getLocalPage() != null && PageMethod.getLocalPage().keepOrderBy();
    }

    /**
     * 保留子查询 order by
     */
    protected boolean keepSubSelectOrderBy() {
        return PageMethod.getLocalPage() != null && PageMethod.getLocalPage().keepSubSelectOrderBy();
    }

    /**
     * 判断Orderby是否包含参数，有参数的不能去
     *
     * @param orderByElements
     * @return
     */
    public boolean orderByHashParameters(List<OrderByElement> orderByElements) {
        if (orderByElements == null) {
            return false;
        }
        for (OrderByElement orderByElement : orderByElements) {
            if (orderByElement.toString().contains("?")) {
                return true;
            }
        }
        return false;
    }
}
