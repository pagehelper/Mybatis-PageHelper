package com.github.pagehelper.dialect.auto;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 * commons-dbcp
 *
 * @author liuzh
 */
public class DbcpAutoDialect extends DataSourceAutoDialect<BasicDataSource> {

    @Override
    public String getJdbcUrl(BasicDataSource basicDataSource) {
        return basicDataSource.getUrl();
    }

}
