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

package com.github.pagehelper.sql;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.parser.CountSqlParser;
import com.github.pagehelper.parser.SqlParserUtil;
import com.github.pagehelper.parser.defaults.DefaultCountSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author liuzh
 */
public class SqlTest {

    CountSqlParser countSqlParser = new DefaultCountSqlParser();
    @Test
    public void testSqlParser() {

        Assert.assertEquals("WITH AA AS (SELECT 1 FROM A1), BB AS (SELECT 1 FROM B2), AB AS (SELECT * FROM AA UNION ALL SELECT * FROM BB) SELECT count(0) FROM AB",
                countSqlParser.getSmartCountSql("WITH AA AS (SELECT 1 FROM A1), BB AS (SELECT 1 FROM B2), AB AS (SELECT * FROM AA UNION ALL SELECT * FROM BB) SELECT * FROM AB"));

        Assert.assertEquals("WITH cr AS (SELECT UserRegionCode FROM person.UserRegion WHERE Name LIKE 'C%') SELECT count(0) FROM person.StateProvince WHERE UserRegionCode IN (SELECT * FROM cr)",
                countSqlParser.getSmartCountSql("with " +
                        "cr as " +
                        "( " +
                        "    select UserRegionCode from person.UserRegion where Name like 'C%' order by name" +
                        ") " +
                        " " +
                        "select * from person.StateProvince where UserRegionCode in (select * from cr)"));

        Assert.assertEquals("WITH cr AS (SELECT aaz093 FROM aa10 WHERE aaa100 LIKE 'AAB05%') SELECT count(0) FROM (SELECT count(1) FROM aa10 WHERE aaz093 IN (SELECT * FROM cr)) table_count",
                countSqlParser.getSmartCountSql("with cr as " +
                        " (select aaz093 from aa10 where aaa100 like 'AAB05%' order by aaz093 desc) " +
                        "select count(1) from aa10 where aaz093 in (select * from cr)"));


        Assert.assertEquals("SELECT count(0) FROM ac02 a LEFT JOIN aa10 b ON b.aaa100 = 'AAC031' AND b.aaa102 = a.aac031",
                countSqlParser.getSmartCountSql("select a.aac001,a.aac030,b.aaa103 " +
                        "  from ac02 a " +
                        "  left join aa10 b " +
                        "    on b.aaa100 = 'AAC031' " +
                        "   and b.aaa102 = a.aac031 " +
                        "   order by a.aac001"));

        Assert.assertEquals("SELECT count(0) FROM (SELECT * FROM aa10 WHERE aaa100 LIKE 'AAB05%' UNION SELECT * FROM aa10 WHERE aaa100 = 'AAC031') table_count",
                countSqlParser.getSmartCountSql("select * from aa10 WHERE aaa100 LIKE 'AAB05%' " +
                        "union " +
                        "select * from aa10 where aaa100 = 'AAC031'"));

        Assert.assertEquals("SELECT count(0) FROM (SELECT * FROM aa10 WHERE aaa100 LIKE 'AAB05%' UNION SELECT * FROM aa10 WHERE aaa100 = 'AAC031')",
                countSqlParser.getSmartCountSql("select * from (select * from aa10 WHERE aaa100 LIKE 'AAB05%' " +
                        "union " +
                        "select * from aa10 where aaa100 = 'AAC031')"));

        Assert.assertEquals("SELECT count(0) FROM (SELECT so.id, so.address, so.area_code, so.area_id, so.del_flag, so.email, so.fax, so.grade, so.icon, so.master, so.name, so.parent_id, so.parent_ids, so.phone, so.remarks, so.type, so.zip_code FROM sys_organization so LEFT JOIN sys_user_organization suo ON (suo.org_id = so.id OR FIND_IN_SET(suo.org_id, so.parent_ids)) WHERE suo.user_id = ? GROUP BY so.id LIMIT ?) table_count",
                countSqlParser.getSmartCountSql("select so.id,so.address,so.area_code,so.area_id,so.del_flag,so.email,so.fax,so.grade,so.icon,so.master, so.name,so.parent_id,so.parent_ids,so.phone,so.remarks,so.type,so.zip_code from sys_organization so LEFT JOIN sys_user_organization suo ON (suo.org_id = so.id or FIND_IN_SET(suo.org_id,so.parent_ids)) where suo.user_id = ? group by so.id LIMIT ? "));

        Assert.assertEquals("SELECT count(0) FROM xx1 LEFT JOIN xx2 ON xx1.id = xx2.user_id WHERE 1 = 1 AND xx1.`name` LIKE \"%ll%\" AND xx1.status = 1 AND (xx2.`type` = 1)",
                countSqlParser.getSmartCountSql("select xx,xx,xx from xx1 left join xx2 on xx1.id=xx2.user_id where 1=1 and xx1.`name` like \"%ll%\" and  xx1.status = 1 and (xx2.`type` = 1) order by xx1.number asc"));
    }


    @Test
    public void testSqlParser11() {
        Assert.assertEquals("SELECT count(0) FROM (SELECT so.id, so.address, so.area_code, so.area_id, so.del_flag, so.email, so.fax, so.grade, so.icon, so.master, so.name, so.parent_id, so.parent_ids, so.phone, so.remarks, so.type, so.zip_code FROM sys_organization so LEFT JOIN sys_user_organization suo ON (suo.org_id = so.id OR FIND_IN_SET(suo.org_id, so.parent_ids)) WHERE suo.user_id = ? GROUP BY so.id LIMIT ?) table_count",
                countSqlParser.getSmartCountSql(
                        "select so.id,so.address,so.area_code,so.area_id,so.del_flag,so.email," +
                                "so.fax,so.grade,so.icon,so.master, so.name,so.parent_id,so.parent_ids," +
                                "so.phone,so.remarks,so.type,so.zip_code " +
                                "from sys_organization so " +
                                "LEFT JOIN sys_user_organization suo ON (suo.org_id = so.id or FIND_IN_SET(suo.org_id,so.parent_ids)) " +
                                "where suo.user_id = ? group by so.id LIMIT ? "));

        Assert.assertEquals("SELECT count(0) FROM sys_organization so LEFT JOIN sys_user_organization suo ON (suo.org_id = so.id OR FIND_IN_SET(suo.org_id, so.parent_ids)) WHERE suo.user_id = ?",
                countSqlParser.getSmartCountSql(
                        "select so.id,so.address,so.area_code,so.area_id,so.del_flag,so.email," +
                                "so.fax,so.grade,so.icon,so.master, so.name,so.parent_id,so.parent_ids," +
                                "so.phone,so.remarks,so.type,so.zip_code " +
                                "from sys_organization so " +
                                "LEFT JOIN sys_user_organization suo ON (suo.org_id = so.id or FIND_IN_SET(suo.org_id,so.parent_ids)) " +
                                "where suo.user_id = ?"));
    }

    @Test
    public void testSqlParser2() {
        Assert.assertEquals("SELECT count(0) FROM (SELECT name, count(id) FROM user GROUP BY name) table_count",
                countSqlParser.getSmartCountSql("select name,count(id) from user group by name"));
    }


    @Test
    public void testSqlParser3() {
        Assert.assertEquals("select count(0) from ( \n" +
                        "SELECT *\n" +
                        "    FROM vwdatasearch\n" +
                        "    WHERE ComId = ?\n" +
                        "    AND (\n" +
                        "      Title1 %% ?\n" +
                        "    )\n" +
                        "\n" +
                        " ) tmp_count",
                countSqlParser.getSmartCountSql("SELECT *\n" +
                        "    FROM vwdatasearch\n" +
                        "    WHERE ComId = ?\n" +
                        "    AND (\n" +
                        "      Title1 %% ?\n" +
                        "    )\n"));
    }

    @Test
    public void testSqlParser4() {
        String sql = countSqlParser.getSmartCountSql("/* test */select name,count(id) from user group by name");
        Assert.assertEquals("/* test */SELECT count(0) FROM (SELECT name, count(id) FROM user GROUP BY name) table_count",
                sql);
    }

    @Test
    public void testWithNolock() {
        String sql = "SELECT * FROM A WITH(NOLOCK) INNER JOIN B WITH(NOLOCK) ON A.TypeId = B.Id";
        sql = sql.replaceAll("((?i)\\s*(\\w?)\\s*with\\s*\\(nolock\\))", " $2_PAGEWITHNOLOCK");
        //解析SQL
        Statement stmt = SqlParserUtil.parse(sql);
        Select select = (Select) stmt;
        sql = select.toString();

        sql = sql.replaceAll("\\s*(\\w*?)_PAGEWITHNOLOCK", " $1 WITH(NOLOCK)");
        Assert.assertEquals("SELECT * FROM A WITH(NOLOCK) INNER JOIN B WITH(NOLOCK) ON A.TypeId = B.Id", sql);
    }

    @Test
    public void testSql375() {
        Assert.assertEquals("SELECT count(0) FROM tbl", countSqlParser.getSmartCountSql("SELECT IF(score >= 60, 'pass', 'failed') FROM tbl"));
    }

    @Test
    public void testSql350() {
        Assert.assertEquals("select count(0) from ( \n" +
                "select a,b,c from tb_test having a not null\n" +
                " ) tmp_count", countSqlParser.getSmartCountSql("select a,b,c from tb_test having a not null"));
    }

    @Test
    public void testSql555() {
        Assert.assertEquals("SELECT count(0) FROM (SELECT (a.column1 + a.column2) AS popCount FROM peaf_staff AS a ORDER BY FIELD(a.`store_id`, ?, ?), popCount DESC) table_count",
                countSqlParser.getSmartCountSql("SELECT (a.column1+a.column2) as popCount  FROM peaf_staff AS a ORDER BY FIELD(a.`store_id`, ?, ?), popCount DESC\n"));
    }

    @Test
    public void testSql606() {
        Assert.assertEquals("SELECT count(0) FROM (SELECT (SELECT COUNT(1) FROM test1 WHERE test1.id = test.test1_id) AS successCount, (SELECT COUNT(1) FROM test1) AS Total FROM test HAVING successCount = Total) table_count",
                countSqlParser.getSmartCountSql("select\n" +
                        "(SELECT COUNT(1) FROM test1 WHERE test1.id = test.test1_id )as successCount ,\n" +
                        "(SELECT COUNT(1) FROM test1 ) as Total\n" +
                        "from test\n" +
                        "Having successCount = Total"));
    }

    @Test
    public void testSql201() {
        Assert.assertEquals("SELECT count(0) FROM BASE_PARENT bp LEFT JOIN (BASE_STUDENT bs, BASE_PARENT_STUDENT bst) ON (bp.ID = bst.PARENT_ID AND bs.ID = bst.STUDENT_ID) WHERE 1 = 1",
                countSqlParser.getSmartCountSql("SELECT bp.ID, bp.NAME, bp.PHONE, bp.IDCODE, bp.CREDENTIALS_PIC, bp.ROLE, bs.ID child_id, bs.NAME child_name " +
                        "FROM BASE_PARENT bp " +
                        "LEFT JOIN (BASE_STUDENT bs ,BASE_PARENT_STUDENT bst) ON (bp.ID = bst.PARENT_ID AND bs.ID = bst.STUDENT_ID) " +
                        "WHERE 1 = 1"));
    }

    @Test
    public void testSql545() {
        Assert.assertEquals("SELECT count(0) FROM user_info",
                countSqlParser.getSmartCountSql(" select * from user_info order by [ ]"));
    }

    @Test
    public void testKeepOrderBy() {
        try {
            PageHelper.startPage(1, 10).keepOrderBy(true);
            Assert.assertEquals("select count(0) from ( \n" +
                            " select * from user_info order by name desc\n" +
                            " ) tmp_count",
                    countSqlParser.getSmartCountSql(" select * from user_info order by name desc"));
        } finally {
            PageHelper.clearPage();
        }
        try {
            PageHelper.startPage(1, 10).keepSubSelectOrderBy(true);
            Assert.assertEquals("SELECT count(0) FROM (SELECT id, name FROM user_info ORDER BY name DESC) temp",
                    countSqlParser.getSmartCountSql("select * from (select id, name from user_info order by name desc) temp order by id"));
        } finally {
            PageHelper.clearPage();
        }
    }
}
