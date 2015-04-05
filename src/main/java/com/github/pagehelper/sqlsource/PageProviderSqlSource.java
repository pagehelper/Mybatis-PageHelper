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

package com.github.pagehelper.sqlsource;

import com.github.pagehelper.Constant;
import com.github.pagehelper.parser.Parser;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;

import java.util.Map;

/**
 * @author liuzh
 */
public class PageProviderSqlSource implements SqlSource, Constant {
    private Configuration configuration;
    private ProviderSqlSource providerSqlSource;

    /**
     * 用于区分动态的count查询或分页查询
     */
    private Boolean count;

    private Parser parser;

    public PageProviderSqlSource(Parser parser, Configuration configuration, ProviderSqlSource providerSqlSource, Boolean count) {
        this.parser = parser;
        this.configuration = configuration;
        this.providerSqlSource = providerSqlSource;
        this.count = count;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        BoundSql boundSql = null;
        if (parameterObject instanceof Map && ((Map) parameterObject).containsKey(PROVIDER_OBJECT)) {
            boundSql = providerSqlSource.getBoundSql(((Map) parameterObject).get(PROVIDER_OBJECT));
        } else {
            boundSql = providerSqlSource.getBoundSql(parameterObject);
        }
        if (count) {
            return new BoundSql(
                    configuration,
                    parser.getCountSql(boundSql.getSql()),
                    boundSql.getParameterMappings(),
                    parameterObject);
        } else {
            return new BoundSql(
                    configuration,
                    parser.getPageSql(boundSql.getSql()),
                    parser.getPageParameterMapping(configuration, boundSql),
                    parameterObject);
        }
    }
}