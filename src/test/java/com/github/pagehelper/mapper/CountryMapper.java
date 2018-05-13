/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.pagehelper.mapper;

import com.github.pagehelper.model.*;
import com.github.pagehelper.test.basic.dynamic.Where;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface CountryMapper {

    @Select("select * from country order by ${order}")
    List<Country> selectByOrder(@Param("order") String order);

    //增加Provider测试
    @SelectProvider(type = ProviderMethod.class, method = "select")
    List<Country> selectByProvider(@Param("param") Map map);

    @SelectProvider(type = ProviderMethod.class, method = "selectCountry")
    List<Country> selectByCountryProvider(Country country);

    @Select("Select * from country")
    List<Map<String, Object>> selectBySelect();

    List<Country> selectByOrder2(@Param("order") String order);

    List<Country> selectAll();

    //嵌套查询
    List<Country> selectCollectionMap();

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

    List<Country> selectIf3(Country country);

    List<Country> selectIf2(@Param("id1") Integer id1, @Param("id2") Integer id2);

    List<Country> selectIf2List(@Param("id1") List<Integer> id1, @Param("id2") List<Integer> id2);

    List<Country> selectIf2ListAndOrder(@Param("id1") List<Integer> id1, @Param("id2") List<Integer> id2, @Param("order") String order);

    List<Country> selectChoose(@Param("id1") Integer id1, @Param("id2") Integer id2);

    List<Country> selectLike(Country country);

    //特殊sql语句的测试 - 主要测试count查询
    List<Country> selectUnion();

    List<Country> selectLeftjoin();

    List<Country> selectWith();

    //select column中包含参数时
    List<Country> selectColumns(@Param("columns") String... columns);

    List<Country> selectMULId(int mul);

    //group by时
    List<Country> selectGroupBy();

    //select Map
    List<Country> selectByWhereMap(Where where);

    //Example
    List<Country> selectByExample(CountryExample example);

    List<Country> selectDistinct();

    List<Country> selectExists();

    List<Country> selectByPageNumSize(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    List<Country> selectByPageNumSizeOrderBy(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("orderBy") String orderBy);

    List<Country> selectByOrderBy(@Param("orderBy") String orderBy);

    List<Country> selectByQueryModel(CountryQueryModel queryModel);

    List<Country> selectByIdList(@Param("idList") List<Long> idList);

    List<Country> selectByIdList2(@Param("idList") List<Long> idList);

    List<Map<String, Object>> execute(@Param("sql") String sql);

    List<CountryCode> selectByCode(Code code);
}
