package com.github.pagehelper.mapper;

import com.github.pagehelper.model.Country;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

public interface CountryMapper {

    List<Country> selectAll();

    List<Country> selectAll(RowBounds rowBounds);

    List<Country> selectAllOrderby();

    List<Country> selectAllOrderByParams(@Param("order1") String order1, @Param("order2") String order2);
}
