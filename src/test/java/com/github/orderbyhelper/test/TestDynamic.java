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
public class TestDynamic {

    @Test
    public void testDynamic() {
        SqlSession sqlSession = MybatisOrderHelper.getSqlSession();
        try {
            OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
            List<OrderCountry> list = orderMapper.selectDynamic(10);
            Assert.assertEquals(1, (int) list.get(0).getId());

            PageHelper.startPage(1, 10, "id desc");
            list = orderMapper.selectDynamic(10);
            Assert.assertEquals(9, (int) list.get(0).getId());

            PageHelper.startPage(1, 10, "countryname desc");
            list = orderMapper.selectDynamic(10);
            Assert.assertEquals(9, (int) list.get(0).getId());

            PageHelper.startPage(1, 10, "countrycode desc");
            list = orderMapper.selectDynamic(10);
            Assert.assertEquals(4, (int) list.get(0).getId());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testDynamic2() {
        SqlSession sqlSession = MybatisOrderHelper.getSqlSession();
        try {
            OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
            List<OrderCountry> list = orderMapper.selectDynamic(10);
            Assert.assertEquals(1, (int) list.get(0).getId());

            PageHelper.startPage(1, 10, "id desc");
            list = orderMapper.selectDynamic(10);
            Assert.assertEquals(9, (int) list.get(0).getId());

            PageHelper.startPage(1, 10);
            list = orderMapper.selectDynamic(10);
            Assert.assertEquals(1, (int) list.get(0).getId());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testDynamic3() {
        SqlSession sqlSession = MybatisOrderHelper.getSqlSession();
        try {
            OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
            List<OrderCountry> list = orderMapper.selectDynamic(10);
            Assert.assertEquals(1, (int) list.get(0).getId());

            PageHelper.startPage(1, 10, "countryname desc");
            list = orderMapper.selectDynamic(10);
            Assert.assertEquals(9, (int) list.get(0).getId());

            PageHelper.startPage(1, 10);
            list = orderMapper.selectDynamic(10);
            Assert.assertEquals(1, (int) list.get(0).getId());

            PageHelper.startPage(1, 10, "countrycode desc");
            list = orderMapper.selectDynamic(10);
            Assert.assertEquals(4, (int) list.get(0).getId());
        } finally {
            sqlSession.close();
        }
    }
}
