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

package com.github.pagehelper.sql;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.junit.Test;

import java.util.List;

/**
 * @author liuzh
 */
public class FunctionCountTest {
    public static Select select(String sql) {
        Statement stmt = null;
        try {
            stmt = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
        if (stmt instanceof Select) {
            return (Select) stmt;
        }
        throw new RuntimeException("仅支持Select查询");
    }

    @Test
    public void test() {
        Select select = select("select max(name),code,min(aa),nvl(ab,0),heh from user where a > 100");
        List<SelectItem> selectItems = ((PlainSelect) select.getSelectBody()).getSelectItems();
        for (SelectItem item : selectItems) {
            if (item instanceof SelectExpressionItem) {
                Expression exp = ((SelectExpressionItem) item).getExpression();
                if (exp instanceof Function) {
                    System.out.println("Function:" + item.toString());
                } else {
                    System.out.println("Not a function:" + exp.toString());
                }
            } else {
                System.out.println("Not a function:" + item.toString());
            }
        }
    }

    @Test
    public void test2() {
        Select select = select("select distinct(name) from user where a > 100");
        List<SelectItem> selectItems = ((PlainSelect) select.getSelectBody()).getSelectItems();
        for (SelectItem item : selectItems) {
            if (item instanceof Function) {
                System.out.println("Function:" + item.toString());
            } else {
                System.out.println("Not a function:" + item.toString());
            }
        }
    }
}
