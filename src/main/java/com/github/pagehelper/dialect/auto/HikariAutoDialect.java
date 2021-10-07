package com.github.pagehelper.dialect.auto;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Hikari
 *
 * @author liuzh
 */
public class HikariAutoDialect extends DataSourceAutoDialect<HikariDataSource> {

    @Override
    public String getJdbcUrl(HikariDataSource hikariDataSource) {
        return hikariDataSource.getJdbcUrl();
    }

}
