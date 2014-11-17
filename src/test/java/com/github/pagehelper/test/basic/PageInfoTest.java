package com.github.pagehelper.test.basic;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
            PageInfo page = new PageInfo(list);
            assertEquals(1, page.getPageNum());
            assertEquals(10, page.getPageSize());
            assertEquals(1, page.getStartRow());
            assertEquals(10, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(19, page.getPages());
            assertEquals(1, page.getFirstPage());
            assertEquals(8, page.getLastPage());
            assertEquals(true, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(false, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());


            //获取第2页，10条内容，默认查询总数count
            PageHelper.startPage(2, 10);
            list = countryMapper.selectAll();
            page = new PageInfo(list);
            assertEquals(2, page.getPageNum());
            assertEquals(10, page.getPageSize());
            assertEquals(11, page.getStartRow());
            assertEquals(20, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(19, page.getPages());
            assertEquals(1, page.getFirstPage());
            assertEquals(8, page.getLastPage());
            assertEquals(false, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(true, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());


            //获取第19页，10条内容，默认查询总数count
            PageHelper.startPage(19, 10);
            list = countryMapper.selectAll();
            page = new PageInfo(list);
            assertEquals(19, page.getPageNum());
            assertEquals(10, page.getPageSize());
            assertEquals(181, page.getStartRow());
            assertEquals(183, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(19, page.getPages());
            assertEquals(12, page.getFirstPage());
            assertEquals(19, page.getLastPage());
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
            PageInfo page = new PageInfo(list);
            assertEquals(1, page.getPageNum());
            assertEquals(50, page.getPageSize());
            assertEquals(1, page.getStartRow());
            assertEquals(50, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(4, page.getPages());
            assertEquals(1, page.getFirstPage());
            assertEquals(4, page.getLastPage());
            assertEquals(true, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(false, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());


            //获取第2页，50条内容，默认查询总数count
            PageHelper.startPage(2, 50);
            list = countryMapper.selectAll();
            page = new PageInfo(list);
            assertEquals(2, page.getPageNum());
            assertEquals(50, page.getPageSize());
            assertEquals(51, page.getStartRow());
            assertEquals(100, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(4, page.getPages());
            assertEquals(1, page.getFirstPage());
            assertEquals(4, page.getLastPage());
            assertEquals(false, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(true, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());

            //获取第3页，50条内容，默认查询总数count
            PageHelper.startPage(3, 50);
            list = countryMapper.selectAll();
            page = new PageInfo(list);
            assertEquals(3, page.getPageNum());
            assertEquals(50, page.getPageSize());
            assertEquals(101, page.getStartRow());
            assertEquals(150, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(4, page.getPages());
            assertEquals(1, page.getFirstPage());
            assertEquals(4, page.getLastPage());
            assertEquals(false, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(true, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());


            //获取第4页，50条内容，默认查询总数count
            PageHelper.startPage(4, 50);
            list = countryMapper.selectAll();
            page = new PageInfo(list);
            assertEquals(4, page.getPageNum());
            assertEquals(50, page.getPageSize());
            assertEquals(151, page.getStartRow());
            assertEquals(183, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(4, page.getPages());
            assertEquals(1, page.getFirstPage());
            assertEquals(4, page.getLastPage());
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
            PageInfo page = new PageInfo(list, 20);
            assertEquals(1, page.getPageNum());
            assertEquals(10, page.getPageSize());
            assertEquals(1, page.getStartRow());
            assertEquals(10, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(19, page.getPages());
            assertEquals(1, page.getFirstPage());
            assertEquals(19, page.getLastPage());
            assertEquals(true, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(false, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());

            //获取第2页，50条内容，默认查询总数count
            PageHelper.startPage(2, 50);
            list = countryMapper.selectAll();
            page = new PageInfo(list, 2);
            assertEquals(2, page.getPageNum());
            assertEquals(50, page.getPageSize());
            assertEquals(51, page.getStartRow());
            assertEquals(100, page.getEndRow());
            assertEquals(183, page.getTotal());
            assertEquals(4, page.getPages());
            assertEquals(1, page.getFirstPage());
            assertEquals(2, page.getLastPage());
            assertEquals(false, page.isIsFirstPage());
            assertEquals(false, page.isIsLastPage());
            assertEquals(true, page.isHasPreviousPage());
            assertEquals(true, page.isHasNextPage());
        } finally {
            sqlSession.close();
        }
    }
}
