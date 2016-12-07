package com.github.pagehelper;

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
 * 最简单的分页插件拦截器
 */
@SuppressWarnings("rawtypes")
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class PaginationInterceptor implements Interceptor {
    protected Dialect dialect;
    protected Field additionalParametersField;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //获取拦截方法的参数
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        List resultList;
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
                //根据当前的 ms 创建一个返回值为 Long 类型的 ms
                MappedStatement countMs = MSUtils.newCountMappedStatement(ms);
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

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        String dialectClassStr = properties.getProperty("dialect");
        if (StringUtil.isEmpty(dialectClassStr)) {
            throw new RuntimeException("使用 PaginationInterceptor 分页插件时，必须设置 dialect 属性");
        }
        try {
            Class dialectClass = Class.forName(dialectClassStr);
            dialect = (Dialect) dialectClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("初始化 dialect [" + dialectClassStr + "]时出错:" + e.getMessage());
        }
        dialect.setProperties(properties);
        try {
            //反射获取 BoundSql 中的 additionalParameters 属性
            additionalParametersField = BoundSql.class.getDeclaredField("additionalParameters");
            additionalParametersField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
