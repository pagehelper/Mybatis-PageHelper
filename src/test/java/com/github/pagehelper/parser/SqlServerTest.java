package com.github.pagehelper.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by panmingzhi on 2016/11/29 0029.
 */
public class SqlServerTest {

//    @Test
//    public void testGetColumnAliasMap() throws JSQLParserException {
//        String sql = "select c.id as c_id, u.id as u_id, u.name from Card c left join User u on c.uid = u.id order by u_id";
//        SqlServer sqlServer = new SqlServer();
//        Statement parse = CCJSqlParserUtil.parse(sql);
//        Select select = (Select) parse;
//        Map<String, String> columnAliaMap = sqlServer.getColumnAliasMap((PlainSelect) select.getSelectBody());
//        Assert.assertEquals("c.id",columnAliaMap.get("c_id"));
//        Assert.assertEquals("u.id",columnAliaMap.get("u_id"));
//        Assert.assertNull(columnAliaMap.get("u_name"));
//    }
//
//    @Test
//    public void orderByToString() throws JSQLParserException {
//        String sql = "select c.id as c_id, u.id as u_id, u.name from Card c left join User u on c.uid = u.id order by u_id desc,c_id";
//        SqlServer sqlServer = new SqlServer();
//        Statement parse = CCJSqlParserUtil.parse(sql);
//        Select select = (Select) parse;
//        String orderByToString = sqlServer.orderByToString((PlainSelect) select.getSelectBody());
//        Assert.assertEquals(" ORDER BY u.id DESC , c.id ASC ",orderByToString);
//    }
}