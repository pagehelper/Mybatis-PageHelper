package com.github.pagehelper.sqlsource;

import com.github.orderbyhelper.OrderByHelper;
import com.github.orderbyhelper.sqlsource.OrderBySqlSource;
import com.github.orderbyhelper.OrderByParser;
import com.github.pagehelper.parser.Parser;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.util.List;

/**
 * 支持orderby和分页
 *
 * @author liuzh
 * @since 2015-06-27
 */
public class PageStaticSqlSource implements SqlSource, OrderBySqlSource {
    private String sql;
    private List<ParameterMapping> parameterMappings;
    private Configuration configuration;
    private Parser parser;
    private SqlSource original;
    private Boolean count;

    public PageStaticSqlSource(StaticSqlSource sqlSource, Parser parser, Boolean count) {
        MetaObject metaObject = SystemMetaObject.forObject(sqlSource);
        this.sql = (String) metaObject.getValue("sql");
        this.parameterMappings = (List<ParameterMapping>) metaObject.getValue("parameterMappings");
        this.configuration = (Configuration) metaObject.getValue("configuration");
        this.original = sqlSource;
        this.parser = parser;
        this.count = count;
    }

    public BoundSql getBoundSql(Object parameterObject) {
        String tempSql = sql;
        if (count) {
            tempSql = parser.getCountSql(sql);
            return new BoundSql(configuration, tempSql, parameterMappings, parameterObject);
        } else {
            String orderBy = OrderByHelper.getOrderBy();
            if (orderBy != null) {
                tempSql = OrderByParser.converToOrderBySql(sql, orderBy);
            }
            tempSql = parser.getPageSql(tempSql);
            return new BoundSql(configuration, tempSql, parser.getPageParameterMapping(configuration, original.getBoundSql(parameterObject)), parameterObject);
        }
    }

    public SqlSource getOriginal() {
        return original;
    }
}
