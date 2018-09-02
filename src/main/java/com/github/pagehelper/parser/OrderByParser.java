/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 abel533@gmail.com
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

import com.github.pagehelper.PageException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.List;

/**
 * 处理 Order by
 *
 * @author liuzh
 * @since 2015-06-27
 */
public class OrderByParser {
    /**
     * convert to order by sql
     *
     * @param sql
     * @param orderBy
     * @return
     */
    public static String converToOrderBySql(String sql, String orderBy) {
        //解析SQL
        Statement stmt = null;
        try {
            stmt = CCJSqlParserUtil.parse(sql);
            Select select = (Select) stmt;
            SelectBody selectBody = select.getSelectBody();
            //处理body-去最外层order by
            List<OrderByElement> orderByElements = extraOrderBy(selectBody);
            String defaultOrderBy = PlainSelect.orderByToString(orderByElements);
            if (defaultOrderBy.indexOf('?') != -1) {
                throw new PageException("原SQL[" + sql + "]中的order by包含参数，因此不能使用OrderBy插件进行修改!");
            }
            //新的sql
            sql = select.toString();
        } catch (Throwable e) {
            throw new PageException("处理排序失败: " + e, e);
        }
        return sql + " order by " + orderBy;
    }

    /**
     * extra order by and set default orderby to null
     *
     * @param selectBody
     */
    public static List<OrderByElement> extraOrderBy(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            List<OrderByElement> orderByElements = ((PlainSelect) selectBody).getOrderByElements();
            ((PlainSelect) selectBody).setOrderByElements(null);
            return orderByElements;
        } else if (selectBody instanceof WithItem) {
            WithItem withItem = (WithItem) selectBody;
            if (withItem.getSelectBody() != null) {
                return extraOrderBy(withItem.getSelectBody());
            }
        } else {
            SetOperationList operationList = (SetOperationList) selectBody;
            if (operationList.getSelects() != null && operationList.getSelects().size() > 0) {
                List<SelectBody> plainSelects = operationList.getSelects();
                return extraOrderBy(plainSelects.get(plainSelects.size() - 1));
            }
        }
        return null;
    }
}
