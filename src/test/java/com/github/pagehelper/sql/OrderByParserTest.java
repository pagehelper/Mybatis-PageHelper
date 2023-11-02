package com.github.pagehelper.sql;

import com.github.pagehelper.parser.OrderByParser;
import org.junit.Assert;
import org.junit.Test;

public class OrderByParserTest {

    @Test
    public void testOrderBy() {
        String sql = OrderByParser.converToOrderBySql("select * from user where length(name) > 0 order by id desc", "name desc");
        Assert.assertEquals("SELECT * FROM user WHERE length(name) > 0 order by name desc", sql);
    }
}
