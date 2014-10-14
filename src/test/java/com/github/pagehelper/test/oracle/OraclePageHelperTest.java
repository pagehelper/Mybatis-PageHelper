package com.github.pagehelper.test.oracle;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.mapper.OracleMapper;
import com.github.pagehelper.model.Oracle;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class OraclePageHelperTest {

    @Test
    public void shouldGetAllCountries() {
        SqlSession sqlSession = OracleMybatisHelper.getSqlSession();
        try {
            List<Oracle> list = sqlSession.selectList("selectAll");
        } finally {
            sqlSession.close();
        }
    }

    /**
     * 使用Mapper接口调用时，使用PageHelper.startPage效果更好，不需要添加Mapper接口参数
     */
    @Test
    public void testMapperWithStartPage() {
        SqlSession sqlSession = OracleMybatisHelper.getSqlSession();
        OracleMapper oracleMapper = sqlSession.getMapper(OracleMapper.class);
        try {
            //获取第1页，10条内容，默认查询总数count
            PageHelper.startPage(1, 10);
            List<Oracle> list = oracleMapper.selectAllByOrder("aaa100");
            assertEquals(10, list.size());


            //获取第2页，10条内容，显式查询总数count
            PageHelper.startPage(2, 10, true);
            list = oracleMapper.selectAll();
            assertEquals(10, list.size());


            //获取第2页，10条内容，不查询总数count
            PageHelper.startPage(2, 10, false);
            list = oracleMapper.selectAll();
            assertEquals(10, list.size());
            assertEquals(-1, ((Page) list).getTotal());


            //获取第3页，20条内容，默认查询总数count
            PageHelper.startPage(3, 20);
            list = oracleMapper.selectAll();
            assertEquals(20, list.size());
        } finally {
            sqlSession.close();
        }
    }
}
