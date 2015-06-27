package com.github.orderbyhelper.mapper;

import java.util.Map;

/**
 * 描述信息
 *
 * @author liuzh
 * @since 2015-06-27
 */
public class OrderProvider {

    public String selectProvider(Map<String,Object> params){
        return "select * from country_order where id > #{start}";
    }
}
