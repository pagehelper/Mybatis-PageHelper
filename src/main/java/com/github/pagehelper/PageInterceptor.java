package com.github.pagehelper;

import com.github.pagehelper.cache.Cache;
import com.github.pagehelper.cache.CacheFactory;
import com.github.pagehelper.util.MSUtils;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Mybatis - 通用分页拦截器<br/>
 * 项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 *
 * @author liuzh/abel533/isea533
 * @version 5.0.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class PageInterceptor implements Interceptor {
    //缓存count查询的ms
    protected Cache<CacheKey, MappedStatement> msCountMap = null;
    private Dialect dialect;
    private String default_dialect_class = "com.github.pagehelper.PageHelper";
    private Field additionalParametersField;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            //获取拦截方法的参数
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameterObject = args[1];
            RowBounds rowBounds = (RowBounds) args[2];
            args[2] = RowBounds.DEFAULT;
            List resultList;
            //调用方法判断是否需要进行分页，如果不需要，直接返回结果
            if (!dialect.skip(ms, parameterObject, rowBounds)) {
                Executor executor = (Executor) invocation.getTarget();
                ResultHandler resultHandler = (ResultHandler) args[3];

                BoundSql boundSql = ms.getBoundSql(parameterObject);
                //反射获取动态参数
                Map<String, Object> additionalParameters = (Map<String, Object>) additionalParametersField.get(boundSql);

                //判断是否需要进行 count 查询
                if (dialect.beforeCount(ms, parameterObject, rowBounds)) {
                    //创建 count 查询的缓存 key
                    CacheKey countKey = executor.createCacheKey(ms, parameterObject, RowBounds.DEFAULT, boundSql);
                    countKey.update("_Count");
                    MappedStatement countMs = msCountMap.get(countKey);
                    if (countMs == null) {
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
                    //返回 true 时继续分页查询，false 时直接返回
                    if (!dialect.afterCount(count, parameterObject, rowBounds)) {
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
                    resultList = (List) invocation.proceed();
                }
            } else {
                resultList = (List) invocation.proceed();
            }
            return dialect.afterPage(resultList, parameterObject, rowBounds);
        } finally {
            dialect.afterAll();
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        //缓存 count ms
        msCountMap = CacheFactory.createSqlCache(properties.getProperty("msCountCache"), "ms", properties);
        String dialectClass = properties.getProperty("dialect");
        if (StringUtil.isEmpty(dialectClass)) {
            dialectClass = default_dialect_class;
        }
        try {
            Class<?> aClass = Class.forName(dialectClass);
            dialect = (Dialect) aClass.newInstance();
        } catch (Exception e) {
            throw new PageException(e);
        }
        dialect.setProperties(properties);
        try {
            //反射获取 BoundSql 中的 additionalParameters 属性
            additionalParametersField = BoundSql.class.getDeclaredField("additionalParameters");
            additionalParametersField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new PageException(e);
        }
    }

}
