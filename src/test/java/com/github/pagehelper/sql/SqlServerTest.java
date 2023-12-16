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

import com.github.pagehelper.dialect.ReplaceSql;
import com.github.pagehelper.dialect.replace.RegexWithNolockReplaceSql;
import com.github.pagehelper.parser.CountSqlParser;
import com.github.pagehelper.parser.SqlServerSqlParser;
import com.github.pagehelper.parser.defaults.DefaultCountSqlParser;
import com.github.pagehelper.parser.defaults.DefaultSqlServerSqlParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author liuzh
 */
public class SqlServerTest {
    public static final SqlServerSqlParser sqlServer = new DefaultSqlServerSqlParser();
    CountSqlParser countSqlParser = new DefaultCountSqlParser();

    @Test
    public void testSqlTestWithlock() {
        String originalSql = "select * from Agency with (NOLOCK) where status=0 order by CreateTime";
        Assert.assertEquals("SELECT TOP 10 * FROM (SELECT ROW_NUMBER() OVER (ORDER BY CreateTime) PAGE_ROW_NUMBER, * FROM (SELECT * FROM Agency WITH (NOLOCK) WHERE status = 0) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlTest() {
        String originalSql = "Select * from user o where id > 10 order by id desc , name asc";
        Assert.assertEquals("SELECT TOP 10 * FROM (SELECT ROW_NUMBER() OVER (ORDER BY id DESC, name ASC) PAGE_ROW_NUMBER, * FROM (SELECT * FROM user o WHERE id > 10) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlAlias() {
        String originalSql = "Select o.* from user o where id > 10 order by id desc , name asc";
        Assert.assertEquals("SELECT TOP 10 * FROM (SELECT ROW_NUMBER() OVER (ORDER BY id DESC, name ASC) PAGE_ROW_NUMBER, * FROM (SELECT o.* FROM user o WHERE id > 10) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlOrderByAlias() {
        String originalSql = "select py code from user order by code";
        Assert.assertEquals("SELECT TOP 10 code FROM (SELECT ROW_NUMBER() OVER (ORDER BY code) PAGE_ROW_NUMBER, code FROM (SELECT py code FROM user) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlDistinct() {
        String originalSql = "select distinct py,name from user order by py";
        Assert.assertEquals("SELECT TOP 10 py, name FROM (SELECT ROW_NUMBER() OVER (ORDER BY py) PAGE_ROW_NUMBER, py, name FROM (SELECT DISTINCT py, name FROM user) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlTableAll() {
        String originalSql = "Select user.* from user where id > 10 order by id desc , name asc";
        Assert.assertEquals("SELECT TOP 10 * FROM (SELECT ROW_NUMBER() OVER (ORDER BY id DESC, name ASC) PAGE_ROW_NUMBER, * FROM (SELECT user.* FROM user WHERE id > 10) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlAs() {
        String originalSql = "Select id as id,name name,py as code from user o where id > 10 order by id desc , name asc";
        Assert.assertEquals("SELECT TOP 10 id, name, code FROM (SELECT ROW_NUMBER() OVER (ORDER BY id DESC, name ASC) PAGE_ROW_NUMBER, id, name, code FROM (SELECT id AS id, name name, py AS code FROM user o WHERE id > 10) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSelectParameter() {
        //TODO 这种情况会增加?的个数，需要实际在Mybatis中测试
        String originalSql = "Select id as id,? name,? from user o where id > 10 order by id desc , name asc";
        Assert.assertEquals("SELECT TOP 10 id, name, ? FROM (SELECT ROW_NUMBER() OVER (ORDER BY id DESC, name ASC) PAGE_ROW_NUMBER, id, name, ? FROM (SELECT id AS id, ? name, ? FROM user o WHERE id > 10) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlWith() {
        String originalSql = "with cr as " +
                " (select id " +
                "    from user " +
                "   where id > 100 " +
                "     and id < 120) " +
                "select id, name, py " +
                "  from user " +
                " where id in (select * from cr) order by id";
        Assert.assertEquals("WITH cr AS (SELECT id FROM user WHERE id > 100 AND id < 120) SELECT TOP 10 id, name, py FROM (SELECT ROW_NUMBER() OVER (ORDER BY id) PAGE_ROW_NUMBER, id, name, py FROM (SELECT id, name, py FROM user WHERE id IN (SELECT * FROM cr)) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlLeftJoin() {
        String originalSql = "select * " +
                "  from (select distinct A.USERID, " +
                "                        A.USERCODE, " +
                "                        A.USERNAME, " +
                "                        A.USERPWD, " +
                "                        A.CREATEDATE, " +
                "                        A.UPDATEDATE, " +
                "                        A.USERSTATE, " +
                "                        A.MEMO, " +
                "                        A.USERPHONE, " +
                "                        A.USEREMAIL, " +
                "                        A.IDCARD, " +
                "                        D.DEPTNAME, " +
                "                        D.DEPTID " +
                "          from BASE_SYS_USER A " +
                "          left JOIN BASE_SYS_ROLE_USER_REL B " +
                "            ON A.USERID = B.USERID " +
                "          left JOIN BASE_SYS_ROLE C " +
                "            on C.ROLEID = B.ROLEID " +
                "          left join BASE_SYS_DEPT_USER_REL REL " +
                "            on REL.USERID = A.USERID " +
                "          left join BASE_SYS_DEPT D " +
                "            on D.DEPTID = REL.DEPTID " +
                "         where 1 = 1 " +
                "           and C.ROLEID = ? " +
                "           and D.DEPTID = ? " +
                "           and A.USERNAME LIKE '%heh%' " +
                "           and A.USERSTATE = ? " +
                "         ) A Order by A.createDate desc";
        Assert.assertEquals("SELECT TOP 10 * FROM (SELECT ROW_NUMBER() OVER (ORDER BY createDate DESC) PAGE_ROW_NUMBER, * FROM (SELECT * FROM (SELECT DISTINCT A.USERID, A.USERCODE, A.USERNAME, A.USERPWD, A.CREATEDATE, A.UPDATEDATE, A.USERSTATE, A.MEMO, A.USERPHONE, A.USEREMAIL, A.IDCARD, D.DEPTNAME, D.DEPTID FROM BASE_SYS_USER A LEFT JOIN BASE_SYS_ROLE_USER_REL B ON A.USERID = B.USERID LEFT JOIN BASE_SYS_ROLE C ON C.ROLEID = B.ROLEID LEFT JOIN BASE_SYS_DEPT_USER_REL REL ON REL.USERID = A.USERID LEFT JOIN BASE_SYS_DEPT D ON D.DEPTID = REL.DEPTID WHERE 1 = 1 AND C.ROLEID = ? AND D.DEPTID = ? AND A.USERNAME LIKE '%heh%' AND A.USERSTATE = ?) A) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlUnion() {
        String originalSql = "select name,py code from user where id >170 " +
                "union all " +
                "select name,py code from user where id < 10 order by code";
        Assert.assertEquals("SELECT TOP 10 name, code FROM (SELECT ROW_NUMBER() OVER (ORDER BY code) PAGE_ROW_NUMBER, name, code FROM (SELECT name, code FROM (SELECT name, py code FROM user WHERE id > 170 UNION ALL SELECT name, py code FROM user WHERE id < 10) AS WRAP_OUTER_TABLE) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlUnion2() {
        String originalSql = "select name,code from ( " +
                "\tselect name,py code from user where id >170 " +
                "\tunion all " +
                "\tselect name,py code from user where id < 10 " +
                ") as temp " +
                "order by code";
        Assert.assertEquals("SELECT TOP 10 name, code FROM (SELECT ROW_NUMBER() OVER (ORDER BY code) PAGE_ROW_NUMBER, name, code FROM (SELECT name, code FROM (SELECT name, py code FROM user WHERE id > 170 UNION ALL SELECT name, py code FROM user WHERE id < 10) AS temp) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlOrderByFunctionAlias() {
        String originalSql = "select py code, func() func_alias from user order by func()";
        Assert.assertEquals("SELECT TOP 10 code, func_alias FROM (SELECT ROW_NUMBER() OVER (ORDER BY func_alias) PAGE_ROW_NUMBER, code, func_alias FROM (SELECT py code, func() func_alias FROM user) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlOrderByUnknown() {
        String originalSql = "select name from user order by py";
        Assert.assertEquals("SELECT TOP 10 name FROM (SELECT ROW_NUMBER() OVER (ORDER BY ROW_ALIAS_1) PAGE_ROW_NUMBER, name FROM (SELECT name, py AS ROW_ALIAS_1 FROM user) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlOrderByTable() {
        String originalSql = "select t.py, t.name from user t order by t.py";
        Assert.assertEquals("SELECT TOP 10 py, name FROM (SELECT ROW_NUMBER() OVER (ORDER BY py) PAGE_ROW_NUMBER, py, name FROM (SELECT t.py, t.name FROM user t) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSqlStar() {
        String originalSql = "select t.*, 1 alias from user t order by t.py";
        Assert.assertEquals("SELECT TOP 10 * FROM (SELECT ROW_NUMBER() OVER (ORDER BY py) PAGE_ROW_NUMBER, * FROM (SELECT t.*, 1 alias FROM user t) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    /**
     * JSqlParser的4.4版本对于 UR RS RR CS 作为别名解析错误
     * 故将UR更改为SUR
     * <p>
     * 详见：https://github.com/JSQLParser/JSqlParser/issues/1520
     */
    @Test
    public void testSql377() {
        String originalSql = "select distinct u.user_id, u.dept_id, u.login_name, u.user_name, u.email, u.phonenumber, u.status, u.create_time from sys_user u left join sys_dept d on u.dept_id = d.dept_id left join sys_user_role sur on u.user_id = sur.user_id left join sys_role r on r.role_id = sur.role_id where u.del_flag = '0' and (r.role_id != 1 or r.role_id IS NULL) and u.user_id not in (select u.user_id from sys_user u inner join sys_user_role sur on u.user_id = sur.user_id and sur.role_id = 1)";
        Assert.assertEquals("SELECT TOP 10 user_id, dept_id, login_name, user_name, email, phonenumber, status, create_time FROM (SELECT ROW_NUMBER() OVER (ORDER BY RAND()) PAGE_ROW_NUMBER, user_id, dept_id, login_name, user_name, email, phonenumber, status, create_time FROM (SELECT DISTINCT u.user_id, u.dept_id, u.login_name, u.user_name, u.email, u.phonenumber, u.status, u.create_time FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id = d.dept_id LEFT JOIN sys_user_role sur ON u.user_id = sur.user_id LEFT JOIN sys_role r ON r.role_id = sur.role_id WHERE u.del_flag = '0' AND (r.role_id != 1 OR r.role_id IS NULL) AND u.user_id NOT IN (SELECT u.user_id FROM sys_user u INNER JOIN sys_user_role sur ON u.user_id = sur.user_id AND sur.role_id = 1)) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSql386() {
        String originalSql = " select a.Guid,\n" +
                "               ProManager,\n" +
                "               WorkOrderType,\n" +
                "               a.Name,\n" +
                "               WorkNote,\n" +
                "               b.Name                             TeamName,\n" +
                "               ConstructionSite,\n" +
                "               iif(a.MaterialGuid is null, 0, 1) as IsMaterial,\n" +
                "               a.MaterialGuid,\n" +
                "               c.Code                          as MaterialCode,\n" +
                "               c.FullName                         MaterialName,\n" +
                "               d.FullName                         MatStdSortName\n" +
                "        from RMC_WorkOrder a\n" +
                "                 left join dbo.SYS_OrgFrame b on a.TeamGuid = b.Code and b.ParentGuid is null\n" +
                "                 left join dbo.BAS_Material c on a.MaterialGuid = c.Guid\n" +
                "                 left join BAS_MatStdSort d on a.MatStdSortGuid = d.Guid\n" +
                "        where a.ConfirmUser is null\n" +
                "          and b.Guid = 1\n" +
                "        order by a.ContractBillNO desc";
        Assert.assertEquals("SELECT TOP 10 Guid, ProManager, WorkOrderType, Name, WorkNote, TeamName, ConstructionSite, IsMaterial, MaterialGuid, MaterialCode, MaterialName, MatStdSortName FROM (SELECT ROW_NUMBER() OVER (ORDER BY ROW_ALIAS_1 DESC) PAGE_ROW_NUMBER, Guid, ProManager, WorkOrderType, Name, WorkNote, TeamName, ConstructionSite, IsMaterial, MaterialGuid, MaterialCode, MaterialName, MatStdSortName FROM (SELECT a.Guid, ProManager, WorkOrderType, a.Name, WorkNote, b.Name TeamName, ConstructionSite, iif(a.MaterialGuid IS NULL, 0, 1) AS IsMaterial, a.MaterialGuid, c.Code AS MaterialCode, c.FullName MaterialName, d.FullName MatStdSortName, a.ContractBillNO AS ROW_ALIAS_1 FROM RMC_WorkOrder a LEFT JOIN dbo.SYS_OrgFrame b ON a.TeamGuid = b.Code AND b.ParentGuid IS NULL LEFT JOIN dbo.BAS_Material c ON a.MaterialGuid = c.Guid LEFT JOIN BAS_MatStdSort d ON a.MatStdSortGuid = d.Guid WHERE a.ConfirmUser IS NULL AND b.Guid = 1) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }


    @Test
    public void testSql354() {
        String originalSql = "SELECT ISNULL(tb.a, '') from table tb";
        Assert.assertEquals("SELECT TOP 10 ISNULL(tb.a, '') FROM (SELECT ROW_NUMBER() OVER (ORDER BY RAND()) PAGE_ROW_NUMBER, ISNULL(tb.a, '') FROM (SELECT ISNULL(tb.a, '') FROM table tb) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSql374() {
        String originalSql = "select * from table_a order by id";
        Assert.assertEquals("SELECT TOP 10 * FROM (SELECT ROW_NUMBER() OVER (ORDER BY id) PAGE_ROW_NUMBER, * FROM (SELECT * FROM table_a) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                sqlServer.convertToPageSql(originalSql, 1, 10));
    }

    @Test
    public void testSql345() {
        String originalSql = "Select CC.ClinicID, CC.CaseHistoryNum, CC.CaseHistoryID, CC.DoctorID, CC.ClientRegisterID\n" +
                "From Client CC With(Nolock)\n" +
                "Left Outer Join Register CR With(Nolock) On CC.ClientRegisterID = CR.ClientRegisterID\n" +
                "Where CC.ClientID = 14374";
        ReplaceSql replaceSql = new RegexWithNolockReplaceSql();
        String replace = replaceSql.replace(originalSql);
        String pageSql = sqlServer.convertToPageSql(replace, 1, 10);
        String result = replaceSql.restore(pageSql);
        Assert.assertEquals("SELECT TOP 10 ClinicID, CaseHistoryNum, CaseHistoryID, DoctorID, ClientRegisterID FROM (SELECT ROW_NUMBER() OVER (ORDER BY RAND()) PAGE_ROW_NUMBER, ClinicID, CaseHistoryNum, CaseHistoryID, DoctorID, ClientRegisterID FROM (SELECT CC.ClinicID, CC.CaseHistoryNum, CC.CaseHistoryID, CC.DoctorID, CC.ClientRegisterID FROM Client CC WITH(NOLOCK) LEFT OUTER JOIN Register CR WITH(NOLOCK) ON CC.ClientRegisterID = CR.ClientRegisterID WHERE CC.ClientID = 14374) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER", result);
    }

    @Test
    public void testSql306() {
        String originalSql = "Select * FROM table1 t1 with(nolock)\n" +
                "left join table2 t2 with(nolock) on t1.id=t2.id\n" +
                "left join table3 t3 with(nolock) on t1.id=t3.id";
        ReplaceSql replaceSql = new RegexWithNolockReplaceSql();
        String replace = replaceSql.replace(originalSql);
        String pageSql = sqlServer.convertToPageSql(replace, 1, 10);
        String result = replaceSql.restore(pageSql);
        Assert.assertEquals("SELECT TOP 10 * FROM (SELECT ROW_NUMBER() OVER (ORDER BY RAND()) PAGE_ROW_NUMBER, * FROM (SELECT * FROM table1 t1 WITH(NOLOCK) LEFT JOIN table2 t2 WITH(NOLOCK) ON t1.id = t2.id LEFT JOIN table3 t3 WITH(NOLOCK) ON t1.id = t3.id) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER", result);
    }

    @Test
    public void testSql66() {
        String originalSql = "SELECT *\n" +
                "FROM\n" +
                "forum_post_info a with(nolock)\n" +
                "LEFT JOIN forum_carcase_tags as b with(nolock) on a.id = b.carcase_id where b.tag_id = 127";
        ReplaceSql replaceSql = new RegexWithNolockReplaceSql();
        String replace = replaceSql.replace(originalSql);
        String pageSql = sqlServer.convertToPageSql(replace, 1, 10);


        String smartCountSql = countSqlParser.getSmartCountSql(replace);
        smartCountSql = replaceSql.restore(smartCountSql);
        Assert.assertEquals("SELECT count(0) FROM forum_post_info a WITH(NOLOCK) LEFT JOIN forum_carcase_tags AS b WITH(NOLOCK) ON a.id = b.carcase_id WHERE b.tag_id = 127",
                smartCountSql);


        String result = replaceSql.restore(pageSql);
        Assert.assertEquals("SELECT TOP 10 * FROM (SELECT ROW_NUMBER() OVER (ORDER BY RAND()) PAGE_ROW_NUMBER, * FROM (SELECT * FROM forum_post_info a WITH(NOLOCK) LEFT JOIN forum_carcase_tags AS b WITH(NOLOCK) ON a.id = b.carcase_id WHERE b.tag_id = 127) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                result);
    }

    @Test
    public void testSql398() {
        String originalSql = "Select AUS.ScheduleID, AUS.SystemID, AUS.ClinicID, AUS.DoctorID, AUS.ScheduleDate, \n" +
                "\tAUS.StartTime, AUS.EndTime, AUS.Status, AUS.BookBy, AUS.Note, AUS.Remark, AUS.SourceType, CM.CompanyName,\n" +
                "\tAU.UserName As DoctorName, AU.UserNumber As DoctorNumber, CC.CodeDesc As ClinicName, CD.Lat, CD.Lng,\n" +
                "\tCD.ContactTel, CD.Address, CR.ConsultationStatusID, CR.RegisterStatus,A1.CodeDesc as AreaLevel1, A2.CodeDesc as AreaLevel2\n" +
                "\tFrom ACM_User_Schedule AUS with(nolock)\n" +
                "\tLeft Join Client_Register CR with(nolock) On AUS.BookBy=CR.ClientID And CR.SourceType='F' And AUS.ClientRegisterNum=CR.ClientRegisterNum \n" +
                "\tInner Join ACM_User AU with(nolock) On AU.UserID = AUS.DoctorID \n" +
                "\tInner Join Code_Clinic CC with(nolock) On AUS.ClinicID=CC.CodeID\n" +
                "\tInner Join Clinic_Detail CD with(nolock) On CC.CodeID = CD.ClinicID\n" +
                "\tInner Join Code_Area A1 with(nolock) On CD.AreaLevel1ID=A1.CodeID\n" +
                "\tInner Join Code_Area A2 with(nolock) On CD.AreaLevel2ID=A2.CodeID\n" +
                "\tInner Join Company_Master CM with(nolock) On CC.SystemID = CM.SystemID\n" +
                "\tWhere BookBy=1";
        ReplaceSql replaceSql = new RegexWithNolockReplaceSql();
        String replace = replaceSql.replace(originalSql);
        String pageSql = sqlServer.convertToPageSql(replace, 1, 10);

        String smartCountSql = countSqlParser.getSmartCountSql(replace);
        smartCountSql = replaceSql.restore(smartCountSql);
        Assert.assertEquals("SELECT count(0) FROM ACM_User_Schedule AUS WITH(NOLOCK) LEFT JOIN Client_Register CR WITH(NOLOCK) ON AUS.BookBy = CR.ClientID AND CR.SourceType = 'F' AND AUS.ClientRegisterNum = CR.ClientRegisterNum INNER JOIN ACM_User AU WITH(NOLOCK) ON AU.UserID = AUS.DoctorID INNER JOIN Code_Clinic CC WITH(NOLOCK) ON AUS.ClinicID = CC.CodeID INNER JOIN Clinic_Detail CD WITH(NOLOCK) ON CC.CodeID = CD.ClinicID INNER JOIN Code_Area A1 WITH(NOLOCK) ON CD.AreaLevel1ID = A1.CodeID INNER JOIN Code_Area A2 WITH(NOLOCK) ON CD.AreaLevel2ID = A2.CodeID INNER JOIN Company_Master CM WITH(NOLOCK) ON CC.SystemID = CM.SystemID WHERE BookBy = 1",
                smartCountSql);


        String result = replaceSql.restore(pageSql);
        Assert.assertEquals("SELECT TOP 10 ScheduleID, SystemID, ClinicID, DoctorID, ScheduleDate, StartTime, EndTime, Status, BookBy, Note, Remark, SourceType, CompanyName, DoctorName, DoctorNumber, ClinicName, Lat, Lng, ContactTel, Address, ConsultationStatusID, RegisterStatus, AreaLevel1, AreaLevel2 FROM (SELECT ROW_NUMBER() OVER (ORDER BY RAND()) PAGE_ROW_NUMBER, ScheduleID, SystemID, ClinicID, DoctorID, ScheduleDate, StartTime, EndTime, Status, BookBy, Note, Remark, SourceType, CompanyName, DoctorName, DoctorNumber, ClinicName, Lat, Lng, ContactTel, Address, ConsultationStatusID, RegisterStatus, AreaLevel1, AreaLevel2 FROM (SELECT AUS.ScheduleID, AUS.SystemID, AUS.ClinicID, AUS.DoctorID, AUS.ScheduleDate, AUS.StartTime, AUS.EndTime, AUS.Status, AUS.BookBy, AUS.Note, AUS.Remark, AUS.SourceType, CM.CompanyName, AU.UserName AS DoctorName, AU.UserNumber AS DoctorNumber, CC.CodeDesc AS ClinicName, CD.Lat, CD.Lng, CD.ContactTel, CD.Address, CR.ConsultationStatusID, CR.RegisterStatus, A1.CodeDesc AS AreaLevel1, A2.CodeDesc AS AreaLevel2 FROM ACM_User_Schedule AUS WITH(NOLOCK) LEFT JOIN Client_Register CR WITH(NOLOCK) ON AUS.BookBy = CR.ClientID AND CR.SourceType = 'F' AND AUS.ClientRegisterNum = CR.ClientRegisterNum INNER JOIN ACM_User AU WITH(NOLOCK) ON AU.UserID = AUS.DoctorID INNER JOIN Code_Clinic CC WITH(NOLOCK) ON AUS.ClinicID = CC.CodeID INNER JOIN Clinic_Detail CD WITH(NOLOCK) ON CC.CodeID = CD.ClinicID INNER JOIN Code_Area A1 WITH(NOLOCK) ON CD.AreaLevel1ID = A1.CodeID INNER JOIN Code_Area A2 WITH(NOLOCK) ON CD.AreaLevel2ID = A2.CodeID INNER JOIN Company_Master CM WITH(NOLOCK) ON CC.SystemID = CM.SystemID WHERE BookBy = 1) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                result);
    }

    @Test
    public void testSqlServerSquareBrackets() {
        String originalSql = "SELECT [ID] AS [ComsnCountID] FROM B_ComsnCount;";
        String pageSql = sqlServer.convertToPageSql(originalSql, 1, 20);

        Assert.assertEquals("SELECT TOP 20 [ComsnCountID] FROM (SELECT ROW_NUMBER() OVER (ORDER BY RAND()) PAGE_ROW_NUMBER, [ComsnCountID] FROM (SELECT [ID] AS [ComsnCountID] FROM B_ComsnCount) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                pageSql);
    }

    @Test
    public void testSqlServer768() {
        String originalSql = "SELECT column1, column2, column3 from table1 \n" +
                "UNION \n" +
                "SELECT column1, column2, column3 from table2 \n" +
                "UNION \n" +
                "SELECT column1, column2, column3 from table3 \n" +
                "ORDER BY column1, column2";
        String pageSql = sqlServer.convertToPageSql(originalSql, 1, 20);

        Assert.assertEquals("SELECT TOP 20 column1, column2, column3 FROM (SELECT ROW_NUMBER() OVER (ORDER BY column1, column2) PAGE_ROW_NUMBER, column1, column2, column3 FROM (SELECT column1, column2, column3 FROM (SELECT column1, column2, column3 FROM table1 UNION SELECT column1, column2, column3 FROM table2 UNION SELECT column1, column2, column3 FROM table3) AS WRAP_OUTER_TABLE) AS PAGE_TABLE_ALIAS) AS PAGE_TABLE_ALIAS WHERE PAGE_ROW_NUMBER > 1 ORDER BY PAGE_ROW_NUMBER",
                pageSql);
    }
}
