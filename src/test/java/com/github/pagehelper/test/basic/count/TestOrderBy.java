package com.github.pagehelper.test.basic.count;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.mapper.UserMapper;
import com.github.pagehelper.model.User;
import com.github.pagehelper.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

/**
 * @author light pwd
 * @description
 * @date 2025/10/22
 */
public class TestOrderBy {

    @Test
    public void testOrderByBool() throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            Page<User> page = PageHelper.startPage(1, 10);
            userMapper.selectOrderByBool("ZSJ");
        } finally {
            sqlSession.close();
        }
    }


    @Test
    public void testOrderByCase() throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            Page<User> page = PageHelper.startPage(1, 10);
            userMapper.selectOrderByCase("CNZ");
        } finally {
            sqlSession.close();
        }
    }
}
