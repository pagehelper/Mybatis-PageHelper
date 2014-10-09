package com.github.pagehelper.test.hsqldb;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.mapper.CountryMapper;
import com.github.pagehelper.model.Country;
import com.github.pagehelper.test.hsqldb.util.MybatisHelper;
import com.github.pagehelper.test.hsqldb.util.MybatisRowBoundsHelper;
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
            PageInfo page = new PageInfo(list);
            assertEquals(0, list.size());
            assertEquals(183, page.getTotal());

            //limit<0的时候同上
            PageHelper.startPage(1, -100);
            list = countryMapper.selectAll();
            page = new PageInfo(list);
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
            List<Country> list = countryMapper.selectAll(new RowBounds(1, 0));
            PageInfo page = new PageInfo(list);
            assertEquals(0, list.size());
            assertEquals(-1, page.getTotal());

            //limit<0的时候同上
            list = countryMapper.selectAll(new RowBounds(1, -100));
            page = new PageInfo(list);
            assertEquals(0, list.size());
            assertEquals(-1, page.getTotal());
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
            List<Country> list = countryMapper.selectAll(new RowBounds(1, 0));
            PageInfo page = new PageInfo(list);
            assertEquals(0, list.size());
            assertEquals(183, page.getTotal());

            //pageSize<0的时候同上
            list = countryMapper.selectAll(new RowBounds(1, -100));
            page = new PageInfo(list);
            assertEquals(0, list.size());
            assertEquals(183, page.getTotal());
        } finally {
            sqlSession.close();
        }
    }
}
