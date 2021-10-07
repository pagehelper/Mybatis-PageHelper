package com.github.pagehelper.test.features.autodialect;

import com.github.pagehelper.AutoDialect;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.page.PageAutoDialect;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.MappedStatement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 简单示例，直接从属性读取url，无需获取数据库连接，也不需要考虑是否需要关闭
 */
public class SimpleAutoDialect implements AutoDialect<UnpooledDataSource> {

    @Override
    public UnpooledDataSource extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties) {
        if (dataSource instanceof UnpooledDataSource) {
            return (UnpooledDataSource) dataSource;

        }
        throw new UnsupportedOperationException("不支持的数据源类型: " + dataSource.getClass().getName());
    }

    @Override
    public AbstractHelperDialect extractDialect(UnpooledDataSource dialectKey, MappedStatement ms, DataSource dataSource, Properties properties) {
        String url = dialectKey.getUrl();
        String dialect = PageAutoDialect.fromJdbcUrl(url);
        return PageAutoDialect.instanceDialect(dialect, properties);
    }

}
