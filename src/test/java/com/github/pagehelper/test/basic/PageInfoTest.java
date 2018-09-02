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

package com.github.pagehelper.test.basic;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageSerializable;
import com.github.pagehelper.mapper.CountryMapper;
import com.github.pagehelper.model.Country;
import com.github.pagehelper.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PageInfoTest {

    /**
     * 使用Mapper接口调用时，使用PageHelper.startPage效果更好，不需要添加Mapper接口参数
     */
    @Test
    public void testPageSize10() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        try {
            //获取第1页，10条内容，默认查询总数count
            PageHelper.startPage(1, 10);
            List<Country> list = countryMapper.selectAll();
            System.out.println(list);
            PageInfo<Country> page = new PageInfo<Country>(list);
            assertEquals(1, page.getPageNum());
            assertEquals(10, page.getPageSize());
            assertEquals(1, page.getStartRow());
            assertEquals(10, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(19, page.getPages());
            assertEquals(true, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(false, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());

            PageSerializable<Country> serializable = PageSerializable.of(list);
            assertEquals(183, serializable.getTotal());


            //获取第2页，10条内容，默认查询总数count
            PageHelper.startPage(2, 10);
            list = countryMapper.selectAll();
            page = new PageInfo<Country>(list);
            assertEquals(2, page.getPageNum());
            assertEquals(10, page.getPageSize());
            assertEquals(11, page.getStartRow());
            assertEquals(20, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(19, page.getPages());
            assertEquals(false, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(true, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());


            //获取第19页，10条内容，默认查询总数count
            PageHelper.startPage(19, 10);
            list = countryMapper.selectAll();
            page = new PageInfo<Country>(list);
            assertEquals(19, page.getPageNum());
            assertEquals(10, page.getPageSize());
            assertEquals(181, page.getStartRow());
            assertEquals(183, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(19, page.getPages());
            assertEquals(false, page.isIsFirstPage());
            assertEquals(true, page.isIsLastPage());
            assertEquals(true, page.isHasPreviousPage());
            assertEquals(false, page.isHasNextPage());

        } finally {
            sqlSession.close();
        }
    }


    /**
     * 使用Mapper接口调用时，使用PageHelper.startPage效果更好，不需要添加Mapper接口参数
     */
    @Test
    public void testPageSize50() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        try {
            //获取第1页，50条内容，默认查询总数count
            PageHelper.startPage(1, 50);
            List<Country> list = countryMapper.selectAll();
            PageInfo<Country> page = new PageInfo<Country>(list);
            assertEquals(1, page.getPageNum());
            assertEquals(50, page.getPageSize());
            assertEquals(1, page.getStartRow());
            assertEquals(50, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(4, page.getPages());
            assertEquals(true, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(false, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());


            //获取第2页，50条内容，默认查询总数count
            PageHelper.startPage(2, 50);
            list = countryMapper.selectAll();
            page = new PageInfo<Country>(list);
            assertEquals(2, page.getPageNum());
            assertEquals(50, page.getPageSize());
            assertEquals(51, page.getStartRow());
            assertEquals(100, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(4, page.getPages());
            assertEquals(false, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(true, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());

            //获取第3页，50条内容，默认查询总数count
            PageHelper.startPage(3, 50);
            list = countryMapper.selectAll();
            page = new PageInfo<Country>(list);
            assertEquals(3, page.getPageNum());
            assertEquals(50, page.getPageSize());
            assertEquals(101, page.getStartRow());
            assertEquals(150, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(4, page.getPages());
            assertEquals(false, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(true, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());


            //获取第4页，50条内容，默认查询总数count
            PageHelper.startPage(4, 50);
            list = countryMapper.selectAll();
            page = new PageInfo<Country>(list);
            assertEquals(4, page.getPageNum());
            assertEquals(50, page.getPageSize());
            assertEquals(151, page.getStartRow());
            assertEquals(183, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(4, page.getPages());
            assertEquals(false, page.isIsFirstPage());
            assertEquals(true, page.isIsLastPage());
            assertEquals(true, page.isHasPreviousPage());
            assertEquals(false, page.isHasNextPage());

        } finally {
            sqlSession.close();
        }
    }


    /**
     * 使用Mapper接口调用时，使用PageHelper.startPage效果更好，不需要添加Mapper接口参数
     */
    @Test
    public void testNavigatePages() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        try {
            //获取第1页，10条内容，默认查询总数count
            PageHelper.startPage(1, 10);
            List<Country> list = countryMapper.selectAll();
            PageInfo<Country> page = new PageInfo<Country>(list, 20);
            assertEquals(1, page.getPageNum());
            assertEquals(10, page.getPageSize());
            assertEquals(1, page.getStartRow());
            assertEquals(10, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(19, page.getPages());
            assertEquals(true, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(false, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());

            //获取第2页，50条内容，默认查询总数count
            PageHelper.startPage(2, 50);
            list = countryMapper.selectAll();
            page = new PageInfo<Country>(list, 2);
            assertEquals(2, page.getPageNum());
            assertEquals(50, page.getPageSize());
            assertEquals(51, page.getStartRow());
            assertEquals(100, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(4, page.getPages());
            assertEquals(false, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(true, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());
        } finally {
            sqlSession.close();
        }
    }
}
