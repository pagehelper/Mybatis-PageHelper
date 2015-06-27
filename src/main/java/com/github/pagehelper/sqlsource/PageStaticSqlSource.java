package com.github.pagehelper.sqlsource;

import com.github.orderbyhelper.OrderByHelper;
import com.github.orderbyhelper.sqlsource.OrderBy;
import com.github.pagehelper.parser.OrderByParser;
import com.github.pagehelper.parser.Parser;
import com.github.pagehelper.parser.SqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
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
public class PageStaticSqlSource implements SqlSource, OrderBy {
    private String sql;
    private List<ParameterMapping> parameterMappings;
    private Configuration configuration;
    private Parser parser;

    public PageStaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings, Parser parser) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
        this.parser = parser;
    }

    public BoundSql getBoundSql(Object parameterObject) {
        String orderBy = OrderByHelper.getOrderBy();
        String tempSql = sql;
        if (orderBy != null) {
            tempSql = OrderByParser.converToOrderBySql(sql, orderBy);
        }
        tempSql = parser.getPageSql(tempSql);
        return new BoundSql(configuration, tempSql, parameterMappings, parameterObject);
    }

}
