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

import com.github.pagehelper.ISelect;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.mapper.CountryMapper;
import com.github.pagehelper.model.Country;
import com.github.pagehelper.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author liuzh_3nofxnp
 * @since 2015-12-26 09:15
 */
public class TestISelect {
    @Test
    public void testGroupBy2() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        final CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        try {
            Page<Country> page = PageHelper.startPage(1, 10).doSelectPage(new ISelect() {
                @Override
                public void doSelect() {
                    countryMapper.selectGroupBy();
                }
            });
            //下面是该方法的lambda用法
            //Page<Country> page = PageHelper.startPage(1, 10).setOrderBy("id desc").doSelectPage(()-> countryMapper.selectGroupBy());
            //1,'Angola','AO'
            assertEquals(1, page.get(0).getId());
            assertEquals(10, page.size());
            assertEquals(183, page.getTotal());

            PageInfo<Country> pageInfo = page.toPageInfo();
            System.out.println(pageInfo);


            pageInfo = PageHelper.startPage(1, 10).doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    countryMapper.selectGroupBy();
                }
            });
            //lambda
            //pageInfo = PageHelper.startPage(1, 10).setOrderBy("id desc").doSelectPageInfo(() -> countryMapper.selectGroupBy());

            System.out.println(pageInfo);

            final Country country = new Country();
            country.setCountryname("c");

            long total = PageHelper.count(new ISelect() {
                @Override
                public void doSelect() {
                    countryMapper.selectLike(country);
                }
            });
            //lambda
            //long total = PageHelper.count(()->countryMapper.selectLike(country));

            System.out.println(total);
        } finally {
            sqlSession.close();
        }
    }
}
