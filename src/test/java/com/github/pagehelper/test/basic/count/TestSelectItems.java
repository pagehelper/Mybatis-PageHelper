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

package com.github.pagehelper.test.basic.count;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.mapper.CountryMapper;
import com.github.pagehelper.model.Country;
import com.github.pagehelper.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestSelectItems {

    /**
     * 查询自定义列时 - 实际上这个测试由于使用${}，并没有起到真正的目的
     */
    @Test
    public void testSelectColumns() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        try {
            //获取第1页，10条内容，默认查询总数count
            PageHelper.startPage(1, 10);
            List<Country> list = countryMapper.selectColumns();
            //1,'Angola','AO'
            assertEquals(1, list.get(0).getId());
            assertEquals("Angola", list.get(0).getCountryname());
            assertEquals("AO", list.get(0).getCountrycode());
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());

            //获取第1页，10条内容，默认查询总数count
            PageHelper.startPage(1, 10);
            list = countryMapper.selectColumns("id", "countryname");
            //1,'Angola','AO'
            assertEquals(1, list.get(0).getId());
            assertEquals("Angola", list.get(0).getCountryname());
            assertNull(list.get(0).getCountrycode());
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
        } finally {
            sqlSession.close();
        }
    }

    /**
     * 使用#{}在查询列中进行计算，count查询时需要特殊对待
     */
    @Test
    public void testSelectColumn2() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        try {
            //获取第1页，10条内容，默认查询总数count
            PageHelper.startPage(1, 10);
            List<Country> list = countryMapper.selectMULId(1);
            //1,'Angola','AO'
            assertEquals(1, list.get(0).getId());
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());

            //获取第1页，10条内容，默认查询总数count
            PageHelper.startPage(1, 10);
            list = countryMapper.selectMULId(5);
            //1,'Angola','AO'
            assertEquals(5, list.get(0).getId());
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
        } finally {
            sqlSession.close();
        }
    }
}
