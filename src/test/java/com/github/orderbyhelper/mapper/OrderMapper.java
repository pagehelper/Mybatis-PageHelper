package com.github.orderbyhelper.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

/**
 * 描述信息
 *
 * @author liuzh
 * @since 2015-06-27
 */
public interface OrderMapper {

    List<OrderCountry> selectAll();

    List<OrderCountry> selectUnion(@Param("start") int start,@Param("end") int end);

    List<OrderCountry> selectLeft();

    List<OrderCountry> selectWith(@Param("start") int start);

    @Select("<script>" +
            "select * from country_order where id > #{start}" +
            "</script>")
    List<OrderCountry> selectRaw(@Param("start") int start);

    @SelectProvider(type = OrderProvider.class, method = "selectProvider")
    List<OrderCountry> selectProvider(@Param("start") int start);

    List<OrderCountry> selectDynamic(@Param("id") int start);
}
