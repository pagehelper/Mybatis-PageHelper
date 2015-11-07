/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 abel533@gmail.com
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
import com.github.pagehelper.mapper.CountryMapper;
import com.github.pagehelper.model.Country;
import com.github.pagehelper.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReturnPageInfoTest {

    /**
     * 使用Mapper接口调用时，使用PageHelper.startPage效果更好，不需要添加Mapper接口参数
     */
    @Test
    public void testReturnPageInfo() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        try {
            //获取第1页，10条内容，默认查询总数count
            PageHelper.startPage(1, 10, "id desc");
            PageInfo<Country> pageInfo = countryMapper.selectPageInfo();
            assertEquals(10, pageInfo.getSize());
            assertEquals(183, pageInfo.getTotal());


            //获取第2页，10条内容，显式查询总数count
            PageHelper.orderBy("countryname desc");
            pageInfo = countryMapper.selectPageInfo();
            assertEquals(183, pageInfo.getSize());
            assertEquals(183, pageInfo.getTotal());


            //获取第2页，10条内容，不查询总数count
            PageHelper.startPage(2, 10, false);
            PageHelper.orderBy("id asc");
            pageInfo = countryMapper.selectPageInfo();
            assertEquals(10, pageInfo.getSize());
            assertEquals(-1, pageInfo.getTotal());

            //获取第3页，20条内容，默认查询总数count
            PageHelper.orderBy("countryname desc");
            PageHelper.startPage(3, 20);
            pageInfo = countryMapper.selectPageInfo();
            assertEquals(20, pageInfo.getSize());
            assertEquals(183, pageInfo.getTotal());
        } finally {
            sqlSession.close();
        }
    }
}
