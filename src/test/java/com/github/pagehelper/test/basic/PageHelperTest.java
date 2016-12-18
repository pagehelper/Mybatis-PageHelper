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

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.mapper.CountryMapper;
import com.github.pagehelper.model.Country;
import com.github.pagehelper.util.MybatisHelper;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PageHelperTest {

    @Test
    public void shouldGetAllCountries() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        try {
            List<Country> list = sqlSession.selectList("selectAll");
            assertEquals(183, list.size());
        } finally {
            sqlSession.close();
        }
    }

    /**
     * 使用Mapper接口调用时，对接口增加RowBounds参数，不需要修改对应的xml配置（或注解配置）
     * <p/>
     * RowBounds方式不进行count查询，可以通过修改Page代码实现
     * <p/>
     * 这种情况下如果同时使用startPage方法，以startPage为准
     */
    @Test
    public void testMapperWithRowBounds() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        try {
            //获取第1页，10条内容，默认查询总数count
            List<Country> list = countryMapper.selectAll(new RowBounds(0, 10));
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(1, list.get(0).getId());
            assertEquals(10, list.get(list.size() - 1).getId());


            //获取第2页，10条内容，显式查询总数count
            list = countryMapper.selectAll(new RowBounds(10, 10));
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(11, list.get(0).getId());
            assertEquals(20, list.get(list.size() - 1).getId());


            //获取第3页，20条内容，默认查询总数count
            list = countryMapper.selectAll(new RowBounds(60, 20));
            assertEquals(20, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(61, list.get(0).getId());
            assertEquals(80, list.get(list.size() - 1).getId());


            //同时使用startPage和RowBounds时，以startPage为准
            PageHelper.startPage(1, 20);
            list = countryMapper.selectAll(new RowBounds(60, 20));
            assertEquals(20, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(1, list.get(0).getId());
            assertEquals(20, list.get(list.size() - 1).getId());
        } finally {
            sqlSession.close();
        }
    }

    /**
     * 使用命名空间调用时，使用PageHelper.startPage
     * <p/>
     * startPage第三个参数可以控制是(true)否(false)执行count查询，使用两个查询的startPage时默认进行count查询
     * <p/>
     * 使用startPage方法时，如果同时使用RowBounds，以startPage为准
     */
    @Test
    public void testNamespaceWithStartPage() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();

        try {
            //获取第1页，10条内容，默认查询总数count
            PageHelper.startPage(1, 10);
            List<Country> list = sqlSession.selectList("selectAll");
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());

            //获取第2页，10条内容，显式查询总数count
            PageHelper.startPage(2, 10, true);
            list = sqlSession.selectList("selectAll");
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());

            //获取第2页，10条内容，不查询总数count
            PageHelper.startPage(2, 10, false);
            list = sqlSession.selectList("selectAll");
            assertEquals(10, list.size());
            assertEquals(-1, ((Page<?>) list).getTotal());

            //获取第3页，20条内容，默认查询总数count
            PageHelper.startPage(3, 20);
            list = sqlSession.selectList("selectAll");
            assertEquals(20, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
        } finally {
            sqlSession.close();
        }
    }

    /**
     * 使用命名空间方式的RowBounds进行分页，使用RowBounds时不进行count查询
     * 通过修改代码可以进行count查询，没法通过其他方法改变参数
     * 因为如果通过调用一个别的方法来标记count查询，还不如直接startPage
     * <p/>
     * 同时使用startPage时，以startPage为准，会根据startPage参数来查询
     */
    @Test
    public void testNamespaceWithRowBounds() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        try {
            //获取从0开始，10条内容
            List<Country> list = sqlSession.selectList("selectAll", null, new RowBounds(0, 10));
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(1, list.get(0).getId());
            assertEquals(10, list.get(list.size() - 1).getId());


            //获取从10开始，10条内容
            list = sqlSession.selectList("selectAll", null, new RowBounds(10, 10));
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(11, list.get(0).getId());
            assertEquals(20, list.get(list.size() - 1).getId());


            //获取从20开始，20条内容
            list = sqlSession.selectList("selectAll", null, new RowBounds(20, 20));
            assertEquals(20, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(21, list.get(0).getId());
            assertEquals(40, list.get(list.size() - 1).getId());


            //同时使用startPage和RowBounds时，以startPage为准
            PageHelper.startPage(1, 20);
            list = sqlSession.selectList("selectAll", null, new RowBounds(0, 10));
            assertEquals(20, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(1, list.get(0).getId());
            assertEquals(20, list.get(list.size() - 1).getId());
        } finally {
            sqlSession.close();
        }
    }
}
