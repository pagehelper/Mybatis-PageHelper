package com.github.pagehelper.test.hsqldb;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.mapper.CountryMapper;
import com.github.pagehelper.model.Country;
import com.github.pagehelper.test.hsqldb.util.MybatisHelper;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by Administrator on 14-8-24.
 */
public class RemoveOrderTest {

    private static final Logger LOGGER = Logger.getLogger(RemoveOrderTest.class);

    @Test
    public void simpleOrderTest() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        //主要观看查询执行count查询的sql
        try {
            PageHelper.startPage(1, 50);
            List<Country> list = countryMapper.selectAllOrderby();
            //总数183
            Assert.assertEquals(183, ((Page)list).getTotal());
        } finally {
            sqlSession.close();
        }
    }


    @Test
    public void paramsOrderTest() {
        SqlSession sqlSession = MybatisHelper.getSqlSession();
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);
        //主要观看查询执行count查询的sql
        try {
            PageHelper.startPage(1, 50);
            List<Country> list = countryMapper.selectAllOrderByParams("countryname", "countrycode");
            //总数183
            Assert.assertEquals(183, ((Page)list).getTotal());
        } finally {
            sqlSession.close();
        }
    }


}
