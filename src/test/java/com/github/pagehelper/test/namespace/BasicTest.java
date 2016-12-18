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

package com.github.pagehelper.test.namespace;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.model.Country;
import com.github.pagehelper.util.MybatisRowBoundsHelper;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BasicTest {

    @Test
    public void testNamespace1() {
        SqlSession sqlSession = MybatisRowBoundsHelper.getSqlSession();
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            Country country = new Country();
            country.setCountryname("China");
            map.put("country", country);
            //同时测试不可变Map
            map = Collections.unmodifiableMap(map);
            List<Country> list = sqlSession.selectList("select1", map, new RowBounds(1, 10));
            assertEquals(1, list.size());
            //判断查询结果的位置是否正确
            assertEquals(35, list.get(0).getId());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testNamespace3() {
        SqlSession sqlSession = MybatisRowBoundsHelper.getSqlSession();
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            Country country = new Country();
            map.put("country", country);
            //同时测试不可变Map
            map = Collections.unmodifiableMap(map);
            List<Country> list = sqlSession.selectList("select1", map, new RowBounds(1, 10));
            assertEquals(10, list.size());
            //判断查询结果的位置是否正确
            assertEquals(1, list.get(0).getId());

            map = new HashMap<String, Object>();
            country = new Country();
            country.setCountryname("China");
            map.put("country", country);
            //同时测试不可变Map
            map = Collections.unmodifiableMap(map);
            list = sqlSession.selectList("select1", map, new RowBounds(1, 10));
            assertEquals(1, list.size());
            //判断查询结果的位置是否正确
            assertEquals(35, list.get(0).getId());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testNamespace2() {
        SqlSession sqlSession = MybatisRowBoundsHelper.getSqlSession();
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            Country country = new Country();
            country.setCountryname("China");
            map.put("country", country);
            PageHelper.startPage(1, 10);
            List<Country> list = sqlSession.selectList("select1", map);
            assertEquals(1, list.size());
            //判断查询结果的位置是否正确
            assertEquals(35, list.get(0).getId());
        } finally {
            sqlSession.close();
        }
    }
}