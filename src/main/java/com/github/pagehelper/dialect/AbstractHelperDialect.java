package com.github.pagehelper.dialect;

import com.github.pagehelper.*;
import com.github.pagehelper.dialect.AbstractDialect;
import com.github.pagehelper.parser.CountSqlParser;
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
public abstract class AbstractHelperDialect extends AbstractDialect {

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
        //该方法不会被调用
        return true;
    }

    @Override
    public boolean beforeCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        Page page = getLocalPage();
        return page.isCount();
    }

    @Override
    public boolean afterCount(long count, Object parameterObject, RowBounds rowBounds) {
        Page page = getLocalPage();
        page.setTotal(count);
        if (rowBounds instanceof PageRowBounds) {
            ((PageRowBounds) rowBounds).setTotal(count);
        }
        //pageSize < 0 的时候，不执行分页查询
        //pageSize = 0 的时候，还需要执行后续查询，但是不会分页
        if (page.getPageSize() < 0) {
            return false;
        }
        return count > 0;
    }

    @Override
    public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey) {
        return parameterObject;
    }

    @Override
    public boolean beforePage(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        Page page = getLocalPage();
        if (page.getPageSize() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey pageKey) {
        String sql = boundSql.getSql();
        Page page = getLocalPage();
        return getPageSql(sql, page, pageKey);
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
        if (!page.isCount()) {
            page.setTotal(-1);
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
