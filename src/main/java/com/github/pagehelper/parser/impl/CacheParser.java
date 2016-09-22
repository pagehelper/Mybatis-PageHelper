/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.pagehelper.parser.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.SqlUtil;
import com.github.pagehelper.cache.Cache;
import com.github.pagehelper.cache.CacheFactory;
import com.github.pagehelper.parser.Parser;
import com.github.pagehelper.parser.SqlServer;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 缓存实现
 *
 * @author liuzh
 */
public class CacheParser implements Parser {

    private Parser delegate;

    private Cache<String, String> CACHE_COUNTSQL;
    private Cache<String, String> CACHE_PAGESQL;
    private boolean cachePageSql = false;

    public CacheParser(Parser delegate, String sqlCacheClass) {
        this.delegate = delegate;
        CACHE_COUNTSQL = CacheFactory.createSqlCache(sqlCacheClass);
        //一般情况下的pagesql都是简单的字符串拼接，不需要缓存，只有sqlserver方式由于处理SQL结构耗时，因此针对这种情况会缓存
        if(delegate instanceof SqlServerParser){
            CACHE_PAGESQL = CacheFactory.createSqlCache(sqlCacheClass);
            cachePageSql = true;
        }
    }

    @Override
    public boolean isSupportedMappedStatementCache() {
        return delegate.isSupportedMappedStatementCache();
    }

    @Override
    public String getCountSql(String sql) {
        //一般涉及复杂处理，缓存结果
        String countSql = CACHE_COUNTSQL.get(sql);
        if(countSql != null){
            return countSql;
        } else {
            countSql = delegate.getCountSql(sql);
            CACHE_COUNTSQL.put(sql, countSql);
        }
        return countSql;
    }

    @Override
    public String getPageSql(String sql) {
        if(cachePageSql){
            //SqlServerParser实现会缓存
            Page<?> page = SqlUtil.getLocalPage();
            String pageSql = CACHE_PAGESQL.get(sql);
            if(pageSql == null){
                //调用不替换参数的这个方法
                pageSql = ((SqlServerParser)delegate).getPageSqlWithOutPage(sql);
                CACHE_PAGESQL.put(sql, pageSql);
            }
            pageSql = pageSql.replace(SqlServer.START_ROW, String.valueOf(page.getStartRow()));
            pageSql = pageSql.replace(SqlServer.PAGE_SIZE, String.valueOf(page.getPageSize()));
            return pageSql;
        } else {
            //一般情况下，简单字符串拼接，不需要缓存
            return delegate.getPageSql(sql);
        }
    }

    @Override
    public List<ParameterMapping> getPageParameterMapping(Configuration configuration, BoundSql boundSql) {
        return delegate.getPageParameterMapping(configuration, boundSql);
    }

    @Override
    public Map<String, Object> setPageParameter(MappedStatement ms, Object parameterObject, BoundSql boundSql, Page<?> page) {
        return delegate.setPageParameter(ms, parameterObject, boundSql, page);
    }
}
