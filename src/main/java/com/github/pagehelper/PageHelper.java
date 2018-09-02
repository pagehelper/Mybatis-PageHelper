/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 abel533@gmail.com
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

package com.github.pagehelper;

import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.page.PageAutoDialect;
import com.github.pagehelper.page.PageMethod;
import com.github.pagehelper.page.PageParams;
import com.github.pagehelper.parser.CountSqlParser;
import com.github.pagehelper.util.MSUtils;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Properties;

/**
 * Mybatis - 通用分页拦截器<br/>
 * 项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 *
 * @author liuzh/abel533/isea533
 * @version 5.0.0
 */
public class PageHelper extends PageMethod implements Dialect {
    private PageParams pageParams;
    private PageAutoDialect autoDialect;

    @Override
    public boolean skip(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        if (ms.getId().endsWith(MSUtils.COUNT)) {
            throw new RuntimeException("在系统中发现了多个分页插件，请检查系统配置!");
        }
        Page page = pageParams.getPage(parameterObject, rowBounds);
        if (page == null) {
            return true;
        } else {
            //设置默认的 count 列
            if (StringUtil.isEmpty(page.getCountColumn())) {
                page.setCountColumn(pageParams.getCountColumn());
            }
            autoDialect.initDelegateDialect(ms);
            return false;
        }
    }

    @Override
    public boolean beforeCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().beforeCount(ms, parameterObject, rowBounds);
    }

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        return autoDialect.getDelegate().getCountSql(ms, boundSql, parameterObject, rowBounds, countKey);
    }

    @Override
    public boolean afterCount(long count, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().afterCount(count, parameterObject, rowBounds);
    }

    @Override
    public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey) {
        return autoDialect.getDelegate().processParameterObject(ms, parameterObject, boundSql, pageKey);
    }

    @Override
    public boolean beforePage(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().beforePage(ms, parameterObject, rowBounds);
    }

    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey pageKey) {
        return autoDialect.getDelegate().getPageSql(ms, boundSql, parameterObject, rowBounds, pageKey);
    }

    public String getPageSql(String sql, Page page, RowBounds rowBounds, CacheKey pageKey) {
        return autoDialect.getDelegate().getPageSql(sql, page, pageKey);
    }

    @Override
    public Object afterPage(List pageList, Object parameterObject, RowBounds rowBounds) {
        //这个方法即使不分页也会被执行，所以要判断 null
        AbstractHelperDialect delegate = autoDialect.getDelegate();
        if (delegate != null) {
            return delegate.afterPage(pageList, parameterObject, rowBounds);
        }
        return pageList;
    }

    @Override
    public void afterAll() {
        //这个方法即使不分页也会被执行，所以要判断 null
        AbstractHelperDialect delegate = autoDialect.getDelegate();
        if (delegate != null) {
            delegate.afterAll();
            autoDialect.clearDelegate();
        }
        clearPage();
    }

    @Override
    public void setProperties(Properties properties) {
        setStaticProperties(properties);
        pageParams = new PageParams();
        autoDialect = new PageAutoDialect();
        pageParams.setProperties(properties);
        autoDialect.setProperties(properties);
        //20180902新增 aggregateFunctions, 允许手动添加聚合函数（影响行数）
        CountSqlParser.addAggregateFunctions(properties.getProperty("aggregateFunctions"));
    }
}
