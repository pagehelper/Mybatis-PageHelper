package com.github.pagehelper.sqlsource;

import com.github.orderbyhelper.sqlsource.OrderBySqlSource;
import com.github.orderbyhelper.sqlsource.OrderByStaticSqlSource;
import com.github.pagehelper.parser.Parser;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.RawSqlSource;

/**
 * 描述信息
 *
 * @author liuzh
 * @since 2015-06-26
 */
public class PageRawSqlSource implements SqlSource, OrderBySqlSource {
    private SqlSource sqlSource;
    private SqlSource original;
    private Parser parser;
    private Boolean count;

    public PageRawSqlSource(RawSqlSource sqlSource, Parser parser, Boolean count) {
        MetaObject metaObject = SystemMetaObject.forObject(sqlSource);
        this.sqlSource = new PageStaticSqlSource((StaticSqlSource) metaObject.getValue("sqlSource"), parser, count);
        this.original = sqlSource;
        this.parser = parser;
        this.count = count;
    }

    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

    public SqlSource getOriginal() {
        return original;
    }

}