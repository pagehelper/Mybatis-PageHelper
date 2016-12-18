package com.github.pagehelper.dialect.rowbounds;

import com.github.pagehelper.parser.SqlServerParser;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

/**
 * sqlserver 基于 RowBounds 的分页
 *
 * @author liuzh
 */
public class SqlServerRowBoundsDialect extends AbstractRowBoundsDialect {
    protected SqlServerParser pageSql = new SqlServerParser();

    //with(nolock)
    protected String WITHNOLOCK = ", PAGEWITHNOLOCK";

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        String sql = boundSql.getSql();
        sql = sql.replaceAll("((?i)with\\s*\\(nolock\\))", WITHNOLOCK);
        sql = countSqlParser.getSmartCountSql(sql);
        sql = sql.replaceAll(WITHNOLOCK, " with(nolock)");
        return sql;
    }

    @Override
    public String getPageSql(String sql, RowBounds rowBounds, CacheKey pageKey) {
        //处理pageKey
        pageKey.update(rowBounds.getOffset());
        pageKey.update(rowBounds.getLimit());
        sql = sql.replaceAll("((?i)with\\s*\\(nolock\\))", WITHNOLOCK);
        sql = pageSql.convertToPageSql(sql, null, null);
        sql = sql.replaceAll(WITHNOLOCK, " with(nolock)");
        sql = sql.replace(String.valueOf(Long.MIN_VALUE), String.valueOf(rowBounds.getOffset()));
        sql = sql.replace(String.valueOf(Long.MAX_VALUE), String.valueOf(rowBounds.getLimit()));
        return sql;
    }
}
