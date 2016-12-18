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

package com.github.pagehelper.test.rowbounds;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.mapper.CountryMapper;
import com.github.pagehelper.model.Country;
import com.github.pagehelper.util.MybatisRowBoundsHelper;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RowBoundsTest {

    /**
     * 使用Mapper接口调用时，对接口增加RowBounds参数，不需要修改对应的xml配置（或注解配置）
     * <p/>
     * RowBounds方式不进行count查询，可以通过修改Page代码实现
     * <p/>
     * 这种情况下如果同时使用startPage方法，以startPage为准
     */
    @Test
    public void testMapperWithRowBounds() {
        SqlSession sqlSession = MybatisRowBoundsHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        try {
            //获取第1页，10条内容，默认查询总数count
            List<Country> list = countryMapper.selectAll(new RowBounds(1, 10));
            //新增PageInfo对象，对返回结果进行封装
            PageInfo<Country> page = new PageInfo<Country>(list);
            assertEquals(10, list.size());
            assertEquals(183, page.getTotal());
            //判断查询结果的位置是否正确
            assertEquals(1, list.get(0).getId());
            assertEquals(10, list.get(list.size() - 1).getId());


            //获取第10页，10条内容，显式查询总数count
            list = countryMapper.selectAll(new RowBounds(10, 10));
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(91, list.get(0).getId());
            assertEquals(100, list.get(list.size() - 1).getId());


            //获取第3页，20条内容，默认查询总数count
            list = countryMapper.selectAll(new RowBounds(6, 20));
            assertEquals(20, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(101, list.get(0).getId());
            assertEquals(120, list.get(list.size() - 1).getId());
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
        SqlSession sqlSession = MybatisRowBoundsHelper.getSqlSession();
        try {
            //获取从0开始，10条内容
            List<Country> list = sqlSession.selectList("selectAll", null, new RowBounds(1, 10));
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
            assertEquals(91, list.get(0).getId());
            assertEquals(100, list.get(list.size() - 1).getId());


            //获取从20开始，20条内容
            list = sqlSession.selectList("selectAll", null, new RowBounds(6, 20));
            assertEquals(20, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(101, list.get(0).getId());
            assertEquals(120, list.get(list.size() - 1).getId());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testNamespaceWithRowBounds2() {
        SqlSession sqlSession = MybatisRowBoundsHelper.getSqlSession();
        try {
            //获取从0开始，10条内容
            List<Country> list = sqlSession.selectList("selectIf", null, new RowBounds(1, 10));
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(1, list.get(0).getId());
            assertEquals(10, list.get(list.size() - 1).getId());

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", 10);
            //获取从10开始，10条内容
            list = sqlSession.selectList("selectIf", map, new RowBounds(10, 10));
            assertEquals(10, list.size());
            assertEquals(173, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(101, list.get(0).getId());
            assertEquals(110, list.get(list.size() - 1).getId());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testNamespaceWithRowBounds3() {
        SqlSession sqlSession = MybatisRowBoundsHelper.getSqlSession();
        try {
            //获取从0开始，10条内容
            PageHelper.startPage(1, 10);
            List<Country> list = sqlSession.selectList("selectIf", null);
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(1, list.get(0).getId());
            assertEquals(10, list.get(list.size() - 1).getId());

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", 10);
            //获取从10开始，10条内容
            PageHelper.startPage(10, 10);
            list = sqlSession.selectList("selectIf", map);
            assertEquals(10, list.size());
            assertEquals(173, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(101, list.get(0).getId());
            assertEquals(110, list.get(list.size() - 1).getId());

            IdBean country = new IdBean();
            //获取从10开始，10条内容
            PageHelper.startPage(10, 10);
            list = sqlSession.selectList("selectIf", country);
            assertEquals(10, list.size());
            assertEquals(183, ((Page<?>) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(91, list.get(0).getId());
            assertEquals(100, list.get(list.size() - 1).getId());
        } finally {
            sqlSession.close();
        }
    }

    /**
     * 使用Mapper接口调用时，使用PageHelper.startPage效果更好，不需要添加Mapper接口参数
     */
    @Test
    public void testWithRowboundsAndCountTrue() {
        SqlSession sqlSession = MybatisRowBoundsHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        try {
            //limit=0,这时候相当于用分页插件求count,但是前提必须是配置rounbounds方式求count，否则都是-1
            //这里由于没有配置，应该都是-1
            List<Country> list = countryMapper.selectAll(new RowBounds(1, -1));
            PageInfo<Country> page = new PageInfo<Country>(list);
            assertEquals(0, list.size());
            assertEquals(183, page.getTotal());

            //pageSize<0的时候同上
            list = countryMapper.selectAll(new RowBounds(1, -100));
            page = new PageInfo<Country>(list);
            assertEquals(0, list.size());
            assertEquals(183, page.getTotal());
        } finally {
            sqlSession.close();
        }
    }

    class IdBean {
        private Integer id;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }
}