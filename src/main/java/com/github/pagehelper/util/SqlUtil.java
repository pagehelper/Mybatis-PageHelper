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

package com.github.pagehelper.util;

import com.github.pagehelper.Constant;
import com.github.pagehelper.Dialect;
import com.github.pagehelper.dialect.AbstractDialect;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Mybatis - sql工具，获取分页和count的MappedStatement，设置分页参数
 *
 * @author liuzh/abel533/isea533
 * @since 3.6.0
 * 项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SqlUtil extends BaseSqlUtil implements Constant {
    private Dialect dialect;
    private Field additionalParametersField;
    private Properties properties;
    private Lock lock = new ReentrantLock();

    /**
     * 真正的拦截器方法
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            return doIntercept(invocation);
        } finally {
            clearLocalPage();
        }
    }

    /**
     * 真正的拦截器方法
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    public Object doIntercept(Invocation invocation) throws Throwable {
        //获取拦截方法的参数
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        List resultList;
        if(dialect == null){
            initDialectByDatabaseId(ms.getDatabaseId());
        }
        //调用方法判断是否需要进行分页，如果不需要，直接返回结果
        if (!dialect.skip(ms, parameterObject, rowBounds)) {
            ResultHandler resultHandler = (ResultHandler) args[3];
            //当前的目标对象
            Executor executor = (Executor) invocation.getTarget();
            BoundSql boundSql = ms.getBoundSql(parameterObject);
            //反射获取动态参数
            Map<String, Object> additionalParameters = (Map<String, Object>) additionalParametersField.get(boundSql);
            //判断是否需要进行 count 查询
            if (dialect.beforeCount(ms, parameterObject, rowBounds)) {
                //创建 count 查询的缓存 key
                CacheKey countKey = executor.createCacheKey(ms, parameterObject, RowBounds.DEFAULT, boundSql);
                countKey.update("_Count");
                MappedStatement countMs = msCountMap.get(countKey);
                if(countMs == null){
                    //根据当前的 ms 创建一个返回值为 Long 类型的 ms
                    countMs = MSUtils.newCountMappedStatement(ms);
                    msCountMap.put(countKey, countMs);
                }
                //调用方言获取 count sql
                String countSql = dialect.getCountSql(ms, boundSql, parameterObject, rowBounds, countKey);
                BoundSql countBoundSql = new BoundSql(ms.getConfiguration(), countSql, boundSql.getParameterMappings(), parameterObject);
                //当使用动态 SQL 时，可能会产生临时的参数，这些参数需要手动设置到新的 BoundSql 中
                for (String key : additionalParameters.keySet()) {
                    countBoundSql.setAdditionalParameter(key, additionalParameters.get(key));
                }
                //执行 count 查询
                Object countResultList = executor.query(countMs, parameterObject, RowBounds.DEFAULT, resultHandler, countKey, countBoundSql);
                Long count = (Long) ((List) countResultList).get(0);
                //处理查询总数
                dialect.afterCount(count, parameterObject, rowBounds);
                if (count == 0L) {
                    //当查询总数为 0 时，直接返回空的结果
                    return dialect.afterPage(new ArrayList(), parameterObject, rowBounds);
                }
            }
            //判断是否需要进行分页查询
            if (dialect.beforePage(ms, parameterObject, rowBounds)) {
                //生成分页的缓存 key
                CacheKey pageKey = executor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
                //处理参数对象
                parameterObject = dialect.processParameterObject(ms, parameterObject, boundSql, pageKey);
                //调用方言获取分页 sql
                String pageSql = dialect.getPageSql(ms, boundSql, parameterObject, rowBounds, pageKey);
                BoundSql pageBoundSql = new BoundSql(ms.getConfiguration(), pageSql, boundSql.getParameterMappings(), parameterObject);
                //设置动态参数
                for (String key : additionalParameters.keySet()) {
                    pageBoundSql.setAdditionalParameter(key, additionalParameters.get(key));
                }
                //执行分页查询
                resultList = executor.query(ms, parameterObject, RowBounds.DEFAULT, resultHandler, pageKey, pageBoundSql);
            } else {
                resultList = new ArrayList();
            }
        } else {
            args[2] = RowBounds.DEFAULT;
            resultList = (List) invocation.proceed();
        }
        //返回默认查询
        return dialect.afterPage(resultList, parameterObject, rowBounds);
    }

    /**
     * 通过 databaseId 初始化
     *
     * @param databaseId
     */
    private void initDialectByDatabaseId(String databaseId){
        if(StringUtil.isEmpty(databaseId)){
            throw new RuntimeException("当分页插件 PageHelper 不提供 dialect 属性时，必须配置 databaseIdProvider");
        }
        try {
            lock.lock();
            if(dialect == null){
                initDialect(databaseId, this.properties);
                this.properties = null;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 初始化 dialect
     *
     * @param dialectClass
     * @param properties
     */
    private void initDialect(String dialectClass, Properties properties){
        if(StringUtil.isEmpty(dialectClass)){
            throw new RuntimeException("使用 PageHelper 分页插件时，必须设置 dialect 属性");
        }
        try {
            Class sqlDialectClass = resloveDialectClass(dialectClass);
            if(AbstractDialect.class.isAssignableFrom(sqlDialectClass)){
                dialect = (Dialect) sqlDialectClass.getConstructor(SqlUtil.class).newInstance(this);
            } else {
                dialect = (Dialect) sqlDialectClass.newInstance();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("初始化 dialect [" + dialectClass + "]时出错:" + e.getMessage());
        }
        dialect.setProperties(properties);
    }

    public void setProperties(Properties properties) {
        //设置 sqlUtil 的属性
        super.setProperties(properties);
        //TODO 考虑自动获取，通过 databaseId 方式
        String dialectClass = properties.getProperty("dialect");
        if(StringUtil.isEmpty(dialectClass)){
            this.properties = properties;
        } else {
            initDialect(dialectClass, properties);
        }
        try {
            //反射获取 BoundSql 中的 additionalParameters 属性
            additionalParametersField = BoundSql.class.getDeclaredField("additionalParameters");
            additionalParametersField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}