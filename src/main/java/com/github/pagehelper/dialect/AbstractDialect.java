/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2022 abel533@gmail.com
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

import com.github.pagehelper.Dialect;
import com.github.pagehelper.JSqlParser;
import com.github.pagehelper.PageException;
import com.github.pagehelper.PageProperties;
import com.github.pagehelper.parser.CountSqlParser;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * 基于 CountSqlParser 的智能 Count 查询
 *
 * @author liuzh
 */
public abstract class AbstractDialect implements Dialect {
    //处理SQL
    protected CountSqlParser countSqlParser;
    protected JSqlParser     jSqlParser;

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey) {
        return countSqlParser.getSmartCountSql(boundSql.getSql());
    }

    @Override
    public void setProperties(Properties properties) {
        // 自定义 jsqlparser 的 sql 解析器
        String sqlParser = properties.getProperty("sqlParser");
        if (StringUtil.isNotEmpty(sqlParser)) {
            try {
                Class<?> aClass = Class.forName(sqlParser);
                jSqlParser = (JSqlParser) aClass.newInstance();
                if (jSqlParser instanceof PageProperties) {
                    ((PageProperties) jSqlParser).setProperties(properties);
                }
            } catch (Exception e) {
                throw new PageException(e);
            }
        } else {
            jSqlParser = JSqlParser.DEFAULT;
        }
        this.countSqlParser = new CountSqlParser(jSqlParser);
    }
}
