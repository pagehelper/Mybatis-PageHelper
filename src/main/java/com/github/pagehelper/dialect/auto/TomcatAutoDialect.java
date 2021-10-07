package com.github.pagehelper.dialect.auto;

import org.apache.tomcat.jdbc.pool.DataSource;

/**
 * tomcat-jdbc
 *
 * @author liuzh
 */
public class TomcatAutoDialect extends DataSourceAutoDialect<DataSource> {

    @Override
    public String getJdbcUrl(DataSource dataSource) {
        return dataSource.getUrl();
    }

}
