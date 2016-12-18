package com.github.pagehelper.helper.dialect;

import com.github.pagehelper.Page;
import com.github.pagehelper.cache.Cache;
import com.github.pagehelper.cache.CacheFactory;
import com.github.pagehelper.helper.HelperDialect;
import com.github.pagehelper.parser.SqlServerParser;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * @author liuzh
 */
public class SqlServerDialect extends HelperDialect {
    protected SqlServerParser pageSql = new SqlServerParser();
    protected Cache<String, String> CACHE_COUNTSQL;
    protected Cache<String, String> CACHE_PAGESQL;

    //with(nolock)
    protected String WITHNOLOCK = ", PAGEWITHNOLOCK";

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        String sql = boundSql.getSql();
        String cacheSql = CACHE_COUNTSQL.get(sql);
        if (cacheSql != null) {
            return cacheSql;
        } else {
            cacheSql = sql;
        }
        cacheSql = cacheSql.replaceAll("((?i)with\\s*\\(nolock\\))", WITHNOLOCK);
        cacheSql = countSqlParser.getSmartCountSql(cacheSql);
        cacheSql = cacheSql.replaceAll(WITHNOLOCK, " with(nolock)");
        CACHE_COUNTSQL.put(sql, cacheSql);
        return cacheSql;
    }

    @Override
    public String getPageSql(String sql, Page page, RowBounds rowBounds, CacheKey pageKey) {
        //处理pageKey
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());
        String cacheSql = CACHE_PAGESQL.get(sql);
        if (cacheSql == null) {
            cacheSql = sql;
            cacheSql = cacheSql.replaceAll("((?i)with\\s*\\(nolock\\))", WITHNOLOCK);
            cacheSql = pageSql.convertToPageSql(cacheSql, null, null);
            cacheSql = cacheSql.replaceAll(WITHNOLOCK, " with(nolock)");
            CACHE_PAGESQL.put(sql, cacheSql);
        }
        cacheSql = cacheSql.replace(String.valueOf(Long.MIN_VALUE), String.valueOf(page.getStartRow()));
        cacheSql = cacheSql.replace(String.valueOf(Long.MAX_VALUE), String.valueOf(page.getPageSize()));
        return cacheSql;
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        String sqlCacheClass = properties.getProperty("sqlCacheClass");
        if (StringUtil.isNotEmpty(sqlCacheClass) && !sqlCacheClass.equalsIgnoreCase("false")) {
            CACHE_COUNTSQL = CacheFactory.createSqlCache(sqlCacheClass, "count", properties);
            CACHE_PAGESQL = CacheFactory.createSqlCache(sqlCacheClass, "page", properties);
        } else {
            CACHE_COUNTSQL = CacheFactory.createSqlCache(null, "count", properties);
            CACHE_PAGESQL = CacheFactory.createSqlCache(null, "page", properties);
        }
    }
}
