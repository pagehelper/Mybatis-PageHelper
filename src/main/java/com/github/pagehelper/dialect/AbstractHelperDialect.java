/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2023 abel533@gmail.com
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

package com.github.pagehelper.dialect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pagehelper.Constant;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageRowBounds;
import com.github.pagehelper.cache.Cache;
import com.github.pagehelper.cache.CacheFactory;
import com.github.pagehelper.util.ExecutorUtil;
import com.github.pagehelper.util.MetaObjectUtil;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;

import java.util.*;

/**
 * 针对 PageHelper 的实现
 *
 * @author liuzh
 * @since 2016-12-04 14:32
 */
public abstract class AbstractHelperDialect extends AbstractDialect implements Constant {
    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(AbstractHelperDialect.class);

    protected Cache<String, String> CACHE_COUNTSQL;
    protected Cache<String, String> CACHE_PAGESQL;

    public static boolean cacheOnFlag = true;// 临时性开关，为了方便切换，以验证缓存前后对比.
    public static boolean tracingOn = false;// 临时性开关

    /**
     * 获取分页参数
     *
     * @param <T>
     * @return
     */
    public <T> Page<T> getLocalPage() {
        return PageHelper.getLocalPage();
    }

    @Override
    public final boolean skip(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        // 该方法不会被调用
        return true;
    }

    @Override
    public boolean beforeCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        Page page = getLocalPage();
        return !page.isOrderByOnly() && page.isCount();
    }

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds,
            CacheKey countKey) {
        final long startTime = tracingOn || logger.isDebugEnabled() ? System.nanoTime() : 0;
        if (startTime > 0) {
            logger.info("getCountSql start ...");
        }
        Page<Object> page = getLocalPage();
        String countColumn = page.getCountColumn();
        final String sql = boundSql.getSql();
        final String countSqlKey;
        String cachedSql;
        final boolean cacheOn = cacheOnFlag && CACHE_COUNTSQL != null;
        if (StringUtil.isNotEmpty(countColumn)) {
            countSqlKey = sql + countColumn;
            cachedSql = cacheOn ? CACHE_COUNTSQL.get(countSqlKey) : null;
            if (cachedSql != null) {
                logCountSqlEnd(startTime);
                return cachedSql;
            }
            cachedSql = countSqlParser.getSmartCountSql(sql, countColumn);
        } else {
            countSqlKey = sql;
            cachedSql = cacheOn ? CACHE_COUNTSQL.get(countSqlKey) : null;
            if (cachedSql != null) {
                logCountSqlEnd(startTime);
                return cachedSql;
            }
            cachedSql = countSqlParser.getSmartCountSql(sql);
        }
        if (cacheOn) {
            CACHE_COUNTSQL.put(countSqlKey, cachedSql);
        }
        logCountSqlEnd(startTime);
        return cachedSql;
    }

    private void logCountSqlEnd(final long startTime) {
        if (startTime > 0) {
            final long time = System.nanoTime() - startTime;
            logger.info("getCountSql(cacheOn={}) end: {}", cacheOnFlag,
                    Double.toString(time == 0 ? 0 : time / 1000000d));
        }
    }

    @Override
    public boolean afterCount(long count, Object parameterObject, RowBounds rowBounds) {
        Page page = getLocalPage();
        page.setTotal(count);
        if (rowBounds instanceof PageRowBounds) {
            ((PageRowBounds) rowBounds).setTotal(count);
        }
        // pageSize < 0 的时候，不执行分页查询
        // pageSize = 0 的时候，还需要执行后续查询，但是不会分页
        if (page.getPageSizeZero() != null) {
            // PageSizeZero=false&&pageSize<=0
            if (!page.getPageSizeZero() && page.getPageSize() <= 0) {
                return false;
            }
            // PageSizeZero=true&&pageSize<0 返回 false，只有>=0才需要执行后续的
            else if (page.getPageSizeZero() && page.getPageSize() < 0) {
                return false;
            }
        }
        // 页码>0 && 开始行数<总行数即可，不需要考虑 pageSize（上面的 if 已经处理不符合要求的值了）
        return page.getPageNum() > 0 && count > page.getStartRow();
    }

    @Override
    public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql,
            CacheKey pageKey) {
        // 处理参数
        Page page = getLocalPage();
        // 如果只是 order by 就不必处理参数
        if (page.isOrderByOnly()) {
            return parameterObject;
        }
        Map<String, Object> paramMap = null;
        if (parameterObject == null) {
            paramMap = new HashMap<String, Object>();
        } else if (parameterObject instanceof Map) {
            // 解决不可变Map的情况
            paramMap = new HashMap<String, Object>();
            paramMap.putAll((Map) parameterObject);
        } else {
            paramMap = new HashMap<String, Object>();
            // sqlSource为ProviderSqlSource时，处理只有1个参数的情况
            if (ms.getSqlSource() instanceof ProviderSqlSource) {
                String[] providerMethodArgumentNames = ExecutorUtil
                        .getProviderMethodArgumentNames((ProviderSqlSource) ms.getSqlSource());
                if (providerMethodArgumentNames != null && providerMethodArgumentNames.length == 1) {
                    paramMap.put(providerMethodArgumentNames[0], parameterObject);
                    paramMap.put("param1", parameterObject);
                }
            }
            // 动态sql时的判断条件不会出现在ParameterMapping中，但是必须有，所以这里需要收集所有的getter属性
            // TypeHandlerRegistry可以直接处理的会作为一个直接使用的对象进行处理
            boolean hasTypeHandler = ms.getConfiguration().getTypeHandlerRegistry()
                    .hasTypeHandler(parameterObject.getClass());
            MetaObject metaObject = MetaObjectUtil.forObject(parameterObject);
            // 需要针对注解形式的MyProviderSqlSource保存原值
            if (!hasTypeHandler) {
                for (String name : metaObject.getGetterNames()) {
                    paramMap.put(name, metaObject.getValue(name));
                }
            }
            // 下面这段方法，主要解决一个常见类型的参数时的问题
            if (boundSql.getParameterMappings() != null && boundSql.getParameterMappings().size() > 0) {
                for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                    String name = parameterMapping.getProperty();
                    if (!name.equals(PAGEPARAMETER_FIRST)
                            && !name.equals(PAGEPARAMETER_SECOND)
                            && paramMap.get(name) == null) {
                        if (hasTypeHandler
                                || parameterMapping.getJavaType().equals(parameterObject.getClass())) {
                            paramMap.put(name, parameterObject);
                            break;
                        }
                    }
                }
            }
        }
        return processPageParameter(ms, paramMap, page, boundSql, pageKey);
    }

    /**
     * 处理分页参数
     *
     * @param ms
     * @param paramMap
     * @param page
     * @param boundSql
     * @param pageKey
     * @return
     */
    public abstract Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page,
            BoundSql boundSql, CacheKey pageKey);

    @Override
    public boolean beforePage(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        Page page = getLocalPage();
        if (page.isOrderByOnly() || page.getPageSize() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds,
            CacheKey pageKey) {
        String sql = boundSql.getSql();
        Page page = getLocalPage();
        // 支持 order by
        String orderBy = page.getOrderBy();
        String cacheSqlKey = getPageCacheSqlKey(page, sql);
        final boolean cacheOn = cacheOnFlag && CACHE_PAGESQL != null;
        final boolean orderByOnly = page.isOrderByOnly();
        if (StringUtil.isNotEmpty(orderBy)) {
            if (cacheOn) {
                cacheSqlKey += orderBy;
                if (orderByOnly) {
                    cacheSqlKey += "-orderByOnly";
                }
            }
            pageKey.update(orderBy);

            String cachedSql = cacheOn ? CACHE_PAGESQL.get(cacheSqlKey) : null;
            if (cachedSql == null) {
                cachedSql = orderBySqlParser.converToOrderBySql(sql, orderBy);
                if (cacheOn && orderByOnly) {
                    CACHE_PAGESQL.put(cacheSqlKey, cachedSql);
                }
            }
            sql = cachedSql;
        }
        if (orderByOnly) {
            return sql;
        }
        String pageSql = cacheOn ? CACHE_PAGESQL.get(cacheSqlKey) : null;
        if (pageSql == null) {
            pageSql = getPageSql(sql, page, pageKey);
            if (cacheOn) {
                CACHE_PAGESQL.put(cacheSqlKey, pageSql);
            }
        }
        return pageSql;
    }

    protected String getPageCacheSqlKey(final Page page, final String sql) {
        if (page.getStartRow() == 0) {
            return sql;
        }
        return sql + "-1";
    }

    /**
     * 单独处理分页部分
     *
     * @param sql
     * @param page
     * @param pageKey
     * @return
     */
    public abstract String getPageSql(String sql, Page page, CacheKey pageKey);

    @Override
    public Object afterPage(List pageList, Object parameterObject, RowBounds rowBounds) {
        Page page = getLocalPage();
        if (page == null) {
            return pageList;
        }
        page.addAll(pageList);
        // 调整判断顺序，如果查全部，total就是size，如果只排序，也是全部，其他情况下如果不查询count就是-1
        if ((page.getPageSizeZero() != null && page.getPageSizeZero()) && page.getPageSize() == 0) {
            page.setTotal(pageList.size());
        } else if (page.isOrderByOnly()) {
            page.setTotal(pageList.size());
        } else if (!page.isCount()) {
            page.setTotal(-1);
        }
        return page;
    }

    @Override
    public void afterAll() {

    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        final String sqlCacheClass = properties.getProperty("sqlCacheClass");
        if (StringUtil.isNotEmpty(sqlCacheClass) && !sqlCacheClass.equalsIgnoreCase("false")) {
            CACHE_COUNTSQL = CacheFactory.createCache(sqlCacheClass, "count", properties);
            CACHE_PAGESQL = CacheFactory.createCache(sqlCacheClass, "page", properties);
        } else if (!"false".equalsIgnoreCase(sqlCacheClass)) {
            CACHE_COUNTSQL = CacheFactory.createCache(null, "count", properties);
            CACHE_PAGESQL = CacheFactory.createCache(null, "page", properties);
        }
    }

    /**
     * @param boundSql
     * @param ms
     * @deprecated use
     *             {@code handleParameter(BoundSql boundSql, MappedStatement ms, Class<?> firstClass, Class<?> secondClass)}
     */
    @Deprecated
    protected void handleParameter(BoundSql boundSql, MappedStatement ms) {
        if (boundSql.getParameterMappings() != null) {
            handleParameter(boundSql, ms, long.class, long.class);
        }
    }

    protected void handleParameter(BoundSql boundSql, MappedStatement ms, Class<?> firstClass, Class<?> secondClass) {
        if (boundSql.getParameterMappings() != null) {
            List<ParameterMapping> newParameterMappings = new ArrayList<ParameterMapping>(
                    boundSql.getParameterMappings());
            newParameterMappings
                    .add(new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, firstClass).build());
            newParameterMappings.add(
                    new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, secondClass).build());
            MetaObject metaObject = MetaObjectUtil.forObject(boundSql);
            metaObject.setValue("parameterMappings", newParameterMappings);
        }
    }
}
