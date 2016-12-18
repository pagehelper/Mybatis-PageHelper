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

package com.github.pagehelper.test.pagesize;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.mapper.CountryMapper;
import com.github.pagehelper.model.Country;
import com.github.pagehelper.util.MybatisHelper;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PageSizeLessThenOrEqualZeroTest {

    /**
     * 使用Mapper接口调用时，使用PageHelper.startPage效果更好，不需要添加Mapper接口参数
     */
    @Test
    public void testWithStartPage() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        try {
            //pageSize=0,这时候相当于用分页插件求count
            PageHelper.startPage(1, 0);
            List<Country> list = countryMapper.selectAll();
            PageInfo<Country> page = new PageInfo<Country>(list);
            assertEquals(183, list.size());
            assertEquals(183, page.getTotal());

            //limit<0的时候同上
            PageHelper.startPage(1, -100);
            list = countryMapper.selectAll();
            page = new PageInfo<Country>(list);
            assertEquals(0, list.size());
            assertEquals(183, page.getTotal());
        } finally {
            sqlSession.close();
        }
    }

    /**
     * 使用Mapper接口调用时，使用PageHelper.startPage效果更好，不需要添加Mapper接口参数
     */
    @Test
    public void testWithRowbounds() {
        //注意这里是MybatisRowBoundsHelper，会求count
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        try {
            //limit=0,这时候相当于用分页插件求count,但是前提必须是配置rounbounds方式求count，否则都是-1
            List<Country> list = countryMapper.selectAll(new RowBounds(1, -1));
            PageInfo<Country> page = new PageInfo<Country>(list);
            assertEquals(0, list.size());
            assertEquals(183, page.getTotal());

            //limit<0的时候同上
            list = countryMapper.selectAll(new RowBounds(1, -100));
            page = new PageInfo<Country>(list);
            assertEquals(0, list.size());
            assertEquals(183, page.getTotal());
        } finally {
            sqlSession.close();
        }
    }
}
