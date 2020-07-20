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

package com.github.pagehelper.test.basic.provider;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.mapper.UserMapper;
import com.github.pagehelper.model.User;
import com.github.pagehelper.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestProvider {

    @Test
    public void testSelectSimple() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            PageHelper.startPage(1, 10);
            Page<User> list = userMapper.selectSimple("飞");
            assertEquals(24, list.get(0).getId());
            assertEquals(6, list.size());
            assertEquals(6, list.getTotal());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testProvider() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 100);
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            PageHelper.startPage(1, 10);
            List<User> list = userMapper.selectByProvider(map);
            assertEquals(100, list.get(0).getId());
            assertEquals(1, list.size());
            assertEquals(1, ((Page<?>) list).getTotal());

            map.put("name", "不存在");
            PageHelper.startPage(1, 10);
            list = userMapper.selectByProvider(map);
            assertEquals(0, list.size());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testUserProvider() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        User user = new User();
        user.setId(100);
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            PageHelper.startPage(1, 10);
            List<User> list = userMapper.selectByUserProvider(user);
            assertEquals(100, list.get(0).getId());
            assertEquals(1, list.size());
            assertEquals(1, ((Page<?>) list).getTotal());

            user.setName("不存在");
            PageHelper.startPage(1, 10);
            list = userMapper.selectByUserProvider(user);
            assertEquals(0, list.size());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testUserSelect() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        User user = new User();
        user.setId(100);
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            PageHelper.startPage(1, 10);
            List<Map<String, Object>> userList = userMapper.selectBySelect();
            System.out.println(userList.size());
        } finally {
            sqlSession.close();
        }
    }
}
