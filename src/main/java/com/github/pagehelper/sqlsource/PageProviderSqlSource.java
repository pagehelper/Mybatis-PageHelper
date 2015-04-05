package com.github.pagehelper.sqlsource;

import com.github.pagehelper.Constant;
import com.github.pagehelper.parser.Parser;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.util.Map;

public class PageProviderSqlSource implements SqlSource, Constant {
    private Configuration configuration;
    private ProviderSqlSource providerSqlSource;

    /**
     * 用于区分动态的count查询或分页查询
     */
    private Boolean count;

    private Parser parser;

    public PageProviderSqlSource(Parser parser, Configuration configuration, ProviderSqlSource providerSqlSource, Boolean count) {
        this.parser = parser;
        this.configuration = configuration;
        this.providerSqlSource = providerSqlSource;
        this.count = count;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        BoundSql boundSql = null;
        if (parameterObject instanceof Map && ((Map) parameterObject).containsKey(PROVIDER_OBJECT)) {
            boundSql = providerSqlSource.getBoundSql(((Map) parameterObject).get(PROVIDER_OBJECT));
        } else {
            boundSql = providerSqlSource.getBoundSql(parameterObject);
        }
        if (count) {
            return new BoundSql(
                    configuration,
                    parser.getCountSql(boundSql.getSql()),
                    boundSql.getParameterMappings(),
                    parameterObject);
        } else {
            return new BoundSql(
                    configuration,
                    parser.getPageSql(boundSql.getSql()),
                    parser.getPageParameterMapping(configuration, boundSql),
                    parameterObject);
        }
    }
}