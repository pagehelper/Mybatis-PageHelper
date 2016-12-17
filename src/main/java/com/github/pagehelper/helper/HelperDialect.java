package com.github.pagehelper.helper;

import com.github.pagehelper.*;
import com.github.pagehelper.parser.CountSqlParser;
import com.github.pagehelper.parser.OrderByParser;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Properties;

/**
 * 针对 PageHelper 的实现
 *
 * @author liuzh
 * @since 2016-12-04 14:32
 */
public abstract class HelperDialect implements Dialect, Constant {
    //处理SQL
    protected CountSqlParser countSqlParser = new CountSqlParser();

    @Override
    public boolean skip(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        //该方法不会被调用
        return true;
    }

    @Override
    public boolean beforeCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        Page page = PageHelper.getLocalPage();
        return !page.isOrderByOnly() && page.isCount();
    }

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        return countSqlParser.getSmartCountSql(boundSql.getSql());
    }

    @Override
    public void afterCount(long count, Object parameterObject, RowBounds rowBounds) {
        Page page = PageHelper.getLocalPage();
        page.setTotal(count);
        if (rowBounds instanceof PageRowBounds) {
            ((PageRowBounds) rowBounds).setTotal(count);
        }
    }

    @Override
    public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey) {
        return parameterObject;
    }

    @Override
    public boolean beforePage(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        Page page = PageHelper.getLocalPage();
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
        Page page = PageHelper.getLocalPage();
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
        Page page = PageHelper.getLocalPage();
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
    public void afterAll() {

    }

    @Override
    public void setProperties(Properties properties) {

    }
}
