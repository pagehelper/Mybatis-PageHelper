package com.github.pagehelper.sql;

import com.github.pagehelper.mapper.CountryMapper;
import com.github.pagehelper.test.hsqldb.util.DynamicHelper;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author liuzh
 */
public class SqlHelperTest {

@Test
public void test() {
    SqlSession sqlSession = DynamicHelper.getSqlSession();
    try {
        CountryMapper countryMapper = sqlSession.getMapper(CountryMapper.class);

        System.out.println(
                SqlHelper.getMapperSql(
                        countryMapper,
                        "selectIf2ListAndOrder",
                        Arrays.asList(1, 2),
                        Arrays.asList(3, 4)));

        System.out.println(
                SqlHelper.getNamespaceSql(
                        sqlSession,
                        "com.github.pagehelper.mapper.CountryMapper.selectIf2ListAndOrder"));


        System.out.println(
                SqlHelper.getMapperSql(
                        sqlSession,
                        "com.github.pagehelper.mapper.CountryMapper.selectAll"));

        System.out.println(
                SqlHelper.getMapperSql(
                        sqlSession,
                        "com.github.pagehelper.mapper.CountryMapper.selectIf2ListAndOrder",
                        Arrays.asList(1, 2),
                        Arrays.asList(3, 4),
                        "id"));
    } finally {
        sqlSession.close();
    }
}
}
