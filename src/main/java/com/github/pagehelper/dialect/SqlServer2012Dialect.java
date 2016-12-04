package com.github.pagehelper.dialect;

import com.github.pagehelper.Page;
import com.github.pagehelper.SqlUtil;
import com.github.pagehelper.parser.SqlServer;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.Map;

/**
 * @author liuzh
 */
public class SqlServer2012Dialect extends AbstractDialect {
    protected SqlServer pageSql = new SqlServer();

    //with(nolock)
    protected static final String WITHNOLOCK = ", PAGEWITHNOLOCK";

    public SqlServer2012Dialect(SqlUtil sqlUtil) {
        super(sqlUtil);
    }

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        String sql = boundSql.getSql();
        sql = sql.replaceAll("((?i)with\\s*\\(nolock\\))", WITHNOLOCK);
        sql = sqlParser.getSmartCountSql(sql);
        sql = sql.replaceAll(WITHNOLOCK, " with(nolock)");
        return sql;
    }

    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey) {
        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, RowBounds rowBounds, CacheKey pageKey) {
        //处理pageKey
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());
        sql = sql.replaceAll("((?i)with\\s*\\(nolock\\))", WITHNOLOCK);
        sql = pageSql.convertToPageSql(sql, page.getStartRow(), page.getPageSize());
        sql = sql.replaceAll(WITHNOLOCK, " with(nolock)");
        return sql;
    }

}
