package com.github.orderbyhelper.test;

import com.github.orderbyhelper.MybatisOrderHelper;
import com.github.orderbyhelper.OrderByHelper;
import com.github.orderbyhelper.mapper.OrderCountry;
import com.github.orderbyhelper.mapper.OrderMapper;
import com.github.pagehelper.PageHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * 描述信息
 *
 * @author liuzh
 * @since 2015-06-27
 */
public class TestBasic {

    @Test
    public void testAll() {
        SqlSession sqlSession = MybatisOrderHelper.getSqlSession();
        try {
            OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
            List<OrderCountry> list = orderMapper.selectAll();
            Assert.assertEquals(1, (int) list.get(0).getId());

            OrderByHelper.orderBy("id desc");
            list = orderMapper.selectAll();
            Assert.assertEquals(183, (int) list.get(0).getId());

            OrderByHelper.orderBy("countryname desc");
            list = orderMapper.selectAll();
            Assert.assertEquals(181, (int) list.get(0).getId());

            OrderByHelper.orderBy("countrycode desc");
            list = orderMapper.selectAll();
            Assert.assertEquals(181, (int) list.get(0).getId());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testAll2() {
        SqlSession sqlSession = MybatisOrderHelper.getSqlSession();
        try {
            OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
            List<OrderCountry> list = orderMapper.selectAll();
            Assert.assertEquals(1, (int) list.get(0).getId());

            PageHelper.orderBy("id desc");
            list = orderMapper.selectAll();
            Assert.assertEquals(183, (int) list.get(0).getId());

            PageHelper.orderBy("countryname desc");
            list = orderMapper.selectAll();
            Assert.assertEquals(181, (int) list.get(0).getId());

            PageHelper.orderBy("countrycode desc");
            list = orderMapper.selectAll();
            Assert.assertEquals(181, (int) list.get(0).getId());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testAll3() {
        SqlSession sqlSession = MybatisOrderHelper.getSqlSession();
        try {
            OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
            List<OrderCountry> list = orderMapper.selectAll();
            Assert.assertEquals(1, (int) list.get(0).getId());

            PageHelper.startPage(1, 10, "id desc");
            list = orderMapper.selectAll();
            Assert.assertEquals(183, (int) list.get(0).getId());

            PageHelper.startPage(1, 10, "countryname desc");
            list = orderMapper.selectAll();
            Assert.assertEquals(181, (int) list.get(0).getId());

            PageHelper.startPage(1, 10, "countrycode desc");
            list = orderMapper.selectAll();
            Assert.assertEquals(181, (int) list.get(0).getId());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testAll4() {
        SqlSession sqlSession = MybatisOrderHelper.getSqlSession();
        try {
            OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
            List<OrderCountry> list = orderMapper.selectAll();
            Assert.assertEquals(1, (int) list.get(0).getId());

            OrderByHelper.orderBy("id desc");
            PageHelper.startPage(1, 10);
            list = orderMapper.selectAll();
            Assert.assertEquals(183, (int) list.get(0).getId());

            PageHelper.startPage(1, 10);
            OrderByHelper.orderBy("countryname desc");
            list = orderMapper.selectAll();
            Assert.assertEquals(181, (int) list.get(0).getId());

            OrderByHelper.orderBy("countrycode desc");
            PageHelper.startPage(1, 10);
            list = orderMapper.selectAll();
            Assert.assertEquals(181, (int) list.get(0).getId());
        } finally {
            sqlSession.close();
        }
    }
}
