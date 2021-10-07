package com.github.pagehelper.dialect.auto;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * c3p0
 *
 * @author liuzh
 */
public class C3P0AutoDialect extends DataSourceAutoDialect<ComboPooledDataSource> {

    @Override
    public String getJdbcUrl(ComboPooledDataSource comboPooledDataSource) {
        return comboPooledDataSource.getJdbcUrl();
    }

}
