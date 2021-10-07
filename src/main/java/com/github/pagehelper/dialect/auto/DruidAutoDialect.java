package com.github.pagehelper.dialect.auto;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * Druid
 *
 * @author liuzh
 */
public class DruidAutoDialect extends DataSourceAutoDialect<DruidDataSource> {

    @Override
    public String getJdbcUrl(DruidDataSource druidDataSource) {
        return druidDataSource.getUrl();
    }

}
