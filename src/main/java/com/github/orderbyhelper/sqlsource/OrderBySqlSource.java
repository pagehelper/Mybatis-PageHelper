package com.github.orderbyhelper.sqlsource;

import org.apache.ibatis.mapping.SqlSource;

/**
 * OrderBySqlSource 接口
 *
 * @author liuzh
 * @since 2015-06-26
 */
public interface OrderBySqlSource {

    /**
     * 获取原来的sqlSource
     *
     * @return
     */
    SqlSource getOriginal();

}
