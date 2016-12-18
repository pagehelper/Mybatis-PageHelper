package com.github.pagehelper.dialect;

import com.github.pagehelper.Dialect;
import com.github.pagehelper.PageRowBounds;
import com.github.pagehelper.parser.CountSqlParser;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Properties;

/**
 * 基于 CountSqlParser 的智能 Count 查询
 *
 * @author liuzh
 */
public abstract class AbstractDialect implements Dialect {
    //处理SQL
    protected CountSqlParser countSqlParser = new CountSqlParser();

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        return countSqlParser.getSmartCountSql(boundSql.getSql());
    }
}
