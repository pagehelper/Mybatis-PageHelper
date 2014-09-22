package com.github.pagehelper.mapper;

import com.github.pagehelper.model.Country;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Map;

public interface CountryMapper {

    List<Country> selectAll();

    List<Country> selectGreterThanId(int id);

    List<Country> selectGreterThanIdAndNotEquelContryname(@Param("id") int id, @Param("countryname") String countryname);

    List<Country> selectAll(RowBounds rowBounds);

    List<Country> selectAllOrderby();

    List<Country> selectAllOrderByParams(@Param("order1") String order1, @Param("order2") String order2);


    //下面是三种参数类型的测试方法
    List<Country> selectAllOrderByMap(Map orders);
    List<Country> selectAllOrderByList(List<Integer> params);
    List<Country> selectAllOrderByArray(Integer[] params);

    //测试动态sql,where/if
    List<Country> selectIf(@Param("id") Integer id);
    List<Country> selectIf2(@Param("id1") Integer id1,@Param("id2") Integer id2);
    List<Country> selectIf2List(@Param("id1") List<Integer> id1,@Param("id2") List<Integer> id2);
}
