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

import com.github.pagehelper.parser.CountSqlParser;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.junit.Test;

/**
 * @author liuzh
 */
public class SqlTest {

    @Test
    public void testSqlParser() throws JSQLParserException {
        CountSqlParser countSqlParser = new CountSqlParser();
        System.out.println(countSqlParser.getSmartCountSql("with " +
                "cr as " +
                "( " +
                "    select CountryRegionCode from person.CountryRegion where Name like 'C%' order by name" +
                ") " +
                " " +
                "select * from person.StateProvince where CountryRegionCode in (select * from cr)"));

        System.out.println(countSqlParser.getSmartCountSql("with cr as " +
                " (select aaz093 from aa10 where aaa100 like 'AAB05%' order by aaz093 desc) " +
                "select count(1) from aa10 where aaz093 in (select * from cr)"));


        System.out.println(countSqlParser.getSmartCountSql("select a.aac001,a.aac030,b.aaa103 " +
                "  from ac02 a " +
                "  left join aa10 b " +
                "    on b.aaa100 = 'AAC031' " +
                "   and b.aaa102 = a.aac031 " +
                "   order by a.aac001"));

        System.out.println(countSqlParser.getSmartCountSql("select * from aa10 WHERE aaa100 LIKE 'AAB05%' " +
                "union " +
                "select * from aa10 where aaa100 = 'AAC031'"));

        System.out.println(countSqlParser.getSmartCountSql("select * from (select * from aa10 WHERE aaa100 LIKE 'AAB05%' " +
                "union " +
                "select * from aa10 where aaa100 = 'AAC031')"));

        System.out.println(countSqlParser.getSmartCountSql("select so.id,so.address,so.area_code,so.area_id,so.del_flag,so.email,so.fax,so.grade,so.icon,so.master, so.name,so.parent_id,so.parent_ids,so.phone,so.remarks,so.type,so.zip_code from sys_organization so LEFT JOIN sys_user_organization suo ON (suo.org_id = so.id or FIND_IN_SET(suo.org_id,so.parent_ids)) where suo.user_id = ? group by so.id LIMIT ? "));
    }


    @Test
    public void testSqlParser11() throws JSQLParserException {
        CountSqlParser countSqlParser = new CountSqlParser();
        System.out.println(countSqlParser.getSmartCountSql(
                "select so.id,so.address,so.area_code,so.area_id,so.del_flag,so.email," +
                        "so.fax,so.grade,so.icon,so.master, so.name,so.parent_id,so.parent_ids," +
                        "so.phone,so.remarks,so.type,so.zip_code " +
                        "from sys_organization so " +
                        "LEFT JOIN sys_user_organization suo ON (suo.org_id = so.id or FIND_IN_SET(suo.org_id,so.parent_ids)) " +
                        "where suo.user_id = ? group by so.id LIMIT ? "));

        System.out.println(countSqlParser.getSmartCountSql(
                "select so.id,so.address,so.area_code,so.area_id,so.del_flag,so.email," +
                        "so.fax,so.grade,so.icon,so.master, so.name,so.parent_id,so.parent_ids," +
                        "so.phone,so.remarks,so.type,so.zip_code " +
                        "from sys_organization so " +
                        "LEFT JOIN sys_user_organization suo ON (suo.org_id = so.id or FIND_IN_SET(suo.org_id,so.parent_ids)) " +
                        "where suo.user_id = ?"));
    }

    @Test
    public void testSqlParser2() throws JSQLParserException {
        CountSqlParser countSqlParser = new CountSqlParser();
        System.out.println(countSqlParser.getSmartCountSql("select countryname,count(id) from country group by countryname"));
    }
    @Test
    public void testSqlParser3() throws JSQLParserException {
        CountSqlParser countSqlParser = new CountSqlParser();
        System.out.println(countSqlParser.getSmartCountSql("SELECT *\n" +
                "    FROM vwdatasearch\n" +
                "    WHERE ComId = ?\n" +
                "    AND (\n" +
                "      Title1 %% ?\n" +
                "    )\n"));
    }

    @Test
    public void testWithNolock(){
        String sql = "SELECT * FROM A WITH(NOLOCK) INNER JOIN B WITH(NOLOCK) ON A.TypeId = B.Id";
        System.out.println(sql);
        sql = sql.replaceAll("((?i)\\s*(\\w?)\\s*with\\s*\\(nolock\\))", " $2_PAGEWITHNOLOCK");
        System.out.println(sql);
        //解析SQL
        Statement stmt = null;
        try {
            stmt = CCJSqlParserUtil.parse(sql);
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
        Select select = (Select) stmt;
        SelectBody selectBody = select.getSelectBody();
        sql = selectBody.toString();

        sql = sql.replaceAll("\\s*(\\w*?)_PAGEWITHNOLOCK", " $1 WITH(NOLOCK)");

        System.out.println(sql);
    }
}
