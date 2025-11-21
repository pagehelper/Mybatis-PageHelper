package com.github.pagehelper.test.basic.count;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.mapper.UserMapper;
import com.github.pagehelper.model.User;
import com.github.pagehelper.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test for ORDER BY with parameters - issue #868
 * 
 * @author pwdLight (original fix)
 * @description Test that ORDER BY containing parameters is not removed in count
 *              query
 * @date 2025/10/22
 */
public class TestOrderBy {

    /**
     * Test ORDER BY with parameters - issue #868
     * When ORDER BY contains parameters (#{param}), it should not be removed in
     * count query
     * to avoid JDBC parameter mismatch
     */
    @Test
    public void testOrderByWithParameters() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            // Test with ORDER BY containing parameter binding (#{py})
            // This will generate ORDER BY with ? placeholder
            PageHelper.startPage(1, 10);
            List<User> list = userMapper.selectOrderByWithParam("ZSJ");

            // Verify pagination works correctly
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
        } finally {
            sqlSession.close();
        }
    }

    /**
     * Test ORDER BY without parameters - should use simple count optimization
     */
    @Test
    public void testOrderByWithoutParameters() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            // Test with simple ORDER BY (no parameters)
            PageHelper.startPage(1, 10);
            List<User> list = userMapper.selectAllOrderby();

            // Verify pagination works correctly
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
        } finally {
            sqlSession.close();
        }
    }
}
