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

import com.github.pagehelper.Page;
import com.github.pagehelper.model.*;
import com.github.pagehelper.test.basic.dynamic.Where;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface UserMapper {

    @Select("select * from user order by ${order}")
    List<User> selectByOrder(@Param("order") String order);

    //增加Provider测试
    @SelectProvider(type = ProviderMethod.class, method = "selectSimple")
    Page<User> selectSimple(String str);

    //增加Provider测试
    @SelectProvider(type = ProviderMethod.class, method = "select")
    List<User> selectByProvider(@Param("param") Map map);

    @SelectProvider(type = ProviderMethod.class, method = "selectUser")
    List<User> selectByUserProvider(User user);

    @Select("Select * from user")
    List<Map<String, Object>> selectBySelect();

    List<User> selectByOrder2(@Param("order") String order);

    List<User> selectAll();

    //嵌套查询
    List<User> selectCollectionMap();

    List<User> selectGreterThanId(int id);

    List<User> selectGreterThanIdAndNotEquelName(@Param("id") int id, @Param("name") String name);

    List<User> selectAll(RowBounds rowBounds);

    List<User> selectAllOrderby();

    List<User> selectAllOrderByParams(@Param("order1") String order1, @Param("order2") String order2);


    //下面是三种参数类型的测试方法
    List<User> selectAllOrderByMap(Map orders);

    List<User> selectAllOrderByList(List<Integer> params);

    List<User> selectAllOrderByArray(Integer[] params);

    //测试动态sql,where/if
    List<User> selectIf(@Param("id") Integer id);

    List<User> selectIf3(User user);

    List<User> selectIf2(@Param("id1") Integer id1, @Param("id2") Integer id2);

    List<User> selectIf2List(@Param("id1") List<Integer> id1, @Param("id2") List<Integer> id2);

    List<User> selectIf2ListAndOrder(@Param("id1") List<Integer> id1, @Param("id2") List<Integer> id2, @Param("order") String order);

    List<User> selectChoose(@Param("id1") Integer id1, @Param("id2") Integer id2);

    List<User> selectLike(User user);

    //特殊sql语句的测试 - 主要测试count查询
    List<User> selectUnion();

    List<User> selectLeftjoin();

    List<User> selectWith();

    //select column中包含参数时
    List<User> selectColumns(@Param("columns") String... columns);

    List<User> selectMULId(int mul);

    //group by时
    List<User> selectGroupBy();

    //select Map
    List<User> selectByWhereMap(Where where);

    //Example
    List<User> selectByExample(UserExample example);

    List<User> selectDistinct();

    List<User> selectExists();

    List<User> selectByPageNumSize(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    List<User> selectByPageNumSizeOrderBy(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize, @Param("orderBy") String orderBy);

    List<User> selectByOrderBy(@Param("orderBy") String orderBy);

    List<User> selectByQueryModel(UserQueryModel queryModel);

    List<User> selectByIdList(@Param("idList") List<Long> idList);

    List<User> selectByIdList2(@Param("idList") List<Long> idList);

    List<Map<String, Object>> execute(@Param("sql") String sql);

    List<UserCode> selectByCode(Code code);
}
