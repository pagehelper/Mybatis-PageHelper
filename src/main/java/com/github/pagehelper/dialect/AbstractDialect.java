package com.github.pagehelper.dialect;

import com.github.pagehelper.Constant;
import com.github.pagehelper.Dialect;
import com.github.pagehelper.Page;
import com.github.pagehelper.parser.CountSqlParser;
import com.github.pagehelper.parser.OrderByParser;
import com.github.pagehelper.util.MetaObjectUtil;
import com.github.pagehelper.util.SqlUtil;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 针对 PageHelper 的实现
 *
 * @author liuzh
 * @since 2016-12-04 14:32
 */
public abstract class AbstractDialect implements Dialect, Constant {
    //处理SQL
    protected CountSqlParser countSqlParser = new CountSqlParser();
    protected SqlUtil sqlUtil;

    public AbstractDialect(SqlUtil sqlUtil) {
        this.sqlUtil = sqlUtil;
    }

    @Override
    public boolean skip(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        Page page = sqlUtil.getPage(parameterObject, rowBounds);
        if (page == null) {
            return true;
        }
        return (page.getPageSizeZero() != null && page.getPageSizeZero()) && page.getPageSize() == 0;
    }

    @Override
    public boolean beforeCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        Page page = SqlUtil.getLocalPage();
        return !page.isOrderByOnly() && page.isCount();
    }

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        return countSqlParser.getSmartCountSql(boundSql.getSql());
    }

    @Override
    public void afterCount(long count, Object parameterObject, RowBounds rowBounds) {
        Page page = SqlUtil.getLocalPage();
        page.setTotal(count);
    }

    @Override
    public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey) {
        //处理参数
        Page page = SqlUtil.getLocalPage();
        //如果只是 order by 就不必处理参数
        if (page.isOrderByOnly()) {
            return parameterObject;
        }
        Map<String, Object> paramMap = null;
        if (parameterObject == null) {
            paramMap = new HashMap<String, Object>();
        } else if (parameterObject instanceof Map) {
            //解决不可变Map的情况
            paramMap = new HashMap<String, Object>();
            paramMap.putAll((Map) parameterObject);
        } else {
            paramMap = new HashMap<String, Object>();
            //动态sql时的判断条件不会出现在ParameterMapping中，但是必须有，所以这里需要收集所有的getter属性
            //TypeHandlerRegistry可以直接处理的会作为一个直接使用的对象进行处理
            boolean hasTypeHandler = ms.getConfiguration().getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass());
            MetaObject metaObject = MetaObjectUtil.forObject(parameterObject);
            //需要针对注解形式的MyProviderSqlSource保存原值
            if (!hasTypeHandler) {
                for (String name : metaObject.getGetterNames()) {
                    paramMap.put(name, metaObject.getValue(name));
                }
            }
            //下面这段方法，主要解决一个常见类型的参数时的问题
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
    public abstract Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey);

    @Override
    public boolean beforePage(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        Page page = SqlUtil.getLocalPage();
        if (page.isOrderByOnly()) {
            return true;
        }
        if (page.getPageSize() > 0 && ((rowBounds == RowBounds.DEFAULT && page.getPageNum() > 0) || rowBounds != RowBounds.DEFAULT)) {
            return true;
        }
        return false;
    }

    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey pageKey) {
        String sql = boundSql.getSql();
        Page page = SqlUtil.getLocalPage();
        //支持 order by
        String orderBy = page.getOrderBy();
        if (StringUtil.isNotEmpty(orderBy)) {
            pageKey.update(orderBy);
            sql = OrderByParser.converToOrderBySql(sql, orderBy);
        }
        if (page.isOrderByOnly()) {
            return sql;
        }
        return getPageSql(sql, page, rowBounds, pageKey);
    }

    /**
     * 单独处理分页部分
     *
     * @param sql
     * @param page
     * @param rowBounds
     * @param pageKey
     * @return
     */
    public abstract String getPageSql(String sql, Page page, RowBounds rowBounds, CacheKey pageKey);

    @Override
    public Object afterPage(List pageList, Object parameterObject, RowBounds rowBounds) {
        Page page = SqlUtil.getLocalPage();
        if (page == null) {
            return pageList;
        }
        page.addAll(pageList);
        if (!page.isCount()) {
            page.setTotal(-1);
        } else if (page.isOrderByOnly()) {
            page.setTotal(pageList.size());
        } else if ((page.getPageSizeZero() != null && page.getPageSizeZero()) && page.getPageSize() == 0) {
            page.setTotal(pageList.size());
        }
        return page;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
