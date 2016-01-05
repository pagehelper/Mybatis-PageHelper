package com.github.pagehelper.sqlsource;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.RawSqlSource;

/**
 * 描述信息
 *
 * @author liuzh
 * @since 2015-06-26
 */
public class PageRawSqlSource extends PageSqlSource {
    private PageSqlSource sqlSource;

    public PageRawSqlSource(RawSqlSource sqlSource) {
        MetaObject metaObject = SystemMetaObject.forObject(sqlSource);
        this.sqlSource = new PageStaticSqlSource((StaticSqlSource) metaObject.getValue("sqlSource"));
    }

    @Override
    protected BoundSql getDefaultBoundSql(Object parameterObject) {
        return sqlSource.getDefaultBoundSql(parameterObject);
    }

    @Override
    protected BoundSql getCountBoundSql(Object parameterObject) {
        return sqlSource.getCountBoundSql(parameterObject);
    }

    @Override
    protected BoundSql getPageBoundSql(Object parameterObject) {
        return sqlSource.getPageBoundSql(parameterObject);
    }

}