package com.github.pagehelper.mapper;

import com.github.pagehelper.model.Oracle;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OracleMapper {

    List<Oracle> selectAll();

    List<Oracle> selectAllByOrder(@Param("order") String order);
}
