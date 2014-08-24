package com.github.pagehelper.test;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.mapper.CountryMapper;
import com.github.pagehelper.model.Country;
import com.github.pagehelper.util.MybatisRowBoundsHelper;
import junit.framework.Assert;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;

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
            PageInfo page = new PageInfo(list);
            assertEquals(10, list.size());
            assertEquals(183, page.getTotal());
            //判断查询结果的位置是否正确
            assertEquals(1, list.get(0).getId());
            assertEquals(10, list.get(list.size() - 1).getId());


            //获取第10页，10条内容，显式查询总数count
            list = countryMapper.selectAll(new RowBounds(10, 10));
            assertEquals(10, list.size());
            Assert.assertEquals(183, ((Page) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(91, list.get(0).getId());
            assertEquals(100, list.get(list.size() - 1).getId());


            //获取第3页，20条内容，默认查询总数count
            list = countryMapper.selectAll(new RowBounds(6, 20));
            assertEquals(20, list.size());
            assertEquals(183, ((Page) list).getTotal());
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
            assertEquals(183, ((Page) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(1, list.get(0).getId());
            assertEquals(10, list.get(list.size() - 1).getId());


            //获取从10开始，10条内容
            list = sqlSession.selectList("selectAll", null, new RowBounds(10, 10));
            assertEquals(10, list.size());
            assertEquals(183, ((Page) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(91, list.get(0).getId());
            assertEquals(100, list.get(list.size() - 1).getId());


            //获取从20开始，20条内容
            list = sqlSession.selectList("selectAll", null, new RowBounds(6, 20));
            assertEquals(20, list.size());
            assertEquals(183, ((Page) list).getTotal());
            //判断查询结果的位置是否正确
            assertEquals(101, list.get(0).getId());
            assertEquals(120, list.get(list.size() - 1).getId());
        } finally {
            sqlSession.close();
        }
    }
}