package com.github.orderbyhelper.test;

import com.github.orderbyhelper.MybatisOrderHelper;
import com.github.orderbyhelper.OrderByHelper;
import com.github.orderbyhelper.mapper.OrderCountry;
import com.github.orderbyhelper.mapper.OrderMapper;
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
public class TestWith {

    @Test
    public void testUnion() {
        SqlSession sqlSession = MybatisOrderHelper.getSqlSession();
        try {
            OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
            List<OrderCountry> list = orderMapper.selectUnion(173, 10);
            Assert.assertEquals(1, (int) list.get(0).getId());

            OrderByHelper.orderBy("id desc");
            list = orderMapper.selectUnion(173, 10);
            Assert.assertEquals(183, (int) list.get(0).getId());

            OrderByHelper.orderBy("countryname desc");
            list = orderMapper.selectUnion(173, 10);
            Assert.assertEquals(181, (int) list.get(0).getId());

            OrderByHelper.orderBy("countrycode desc");
            list = orderMapper.selectUnion(173, 10);
            Assert.assertEquals(181, (int) list.get(0).getId());
        } finally {
            sqlSession.close();
        }
    }
}
