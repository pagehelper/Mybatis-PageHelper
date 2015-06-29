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

import com.github.orderbyhelper.sqlsource.OrderBySqlSource;
import com.github.orderbyhelper.sqlsource.OrderByStaticSqlSource;
import com.github.pagehelper.Constant;
import com.github.pagehelper.parser.Parser;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liuzh
 */
public class PageProviderSqlSource extends PageSqlSource implements OrderBySqlSource, Constant {

    private SqlSourceBuilder sqlSourceParser;
    private Class<?> providerType;
    private Method providerMethod;
    private Boolean providerTakesParameterObject;
    private SqlSource original;
    private Configuration configuration;
    private Parser parser;

    public PageProviderSqlSource(ProviderSqlSource provider, Parser parser) {
        MetaObject metaObject = SystemMetaObject.forObject(provider);
        this.sqlSourceParser = (SqlSourceBuilder) metaObject.getValue("sqlSourceParser");
        this.providerType = (Class<?>) metaObject.getValue("providerType");
        this.providerMethod = (Method) metaObject.getValue("providerMethod");
        this.providerTakesParameterObject = (Boolean) metaObject.getValue("providerTakesParameterObject");
        this.configuration = (Configuration) metaObject.getValue("sqlSourceParser.configuration");
        this.original = provider;
        this.parser = parser;
    }

    private SqlSource createSqlSource(Object parameterObject) {
        try {
            String sql;
            if (providerTakesParameterObject) {
                sql = (String) providerMethod.invoke(providerType.newInstance(), parameterObject);
            } else {
                sql = (String) providerMethod.invoke(providerType.newInstance());
            }
            Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
            StaticSqlSource sqlSource = (StaticSqlSource) sqlSourceParser.parse(sql, parameterType, new HashMap<String, Object>());
            return new OrderByStaticSqlSource(sqlSource);
        } catch (Exception e) {
            throw new BuilderException("Error invoking SqlProvider method ("
                    + providerType.getName() + "." + providerMethod.getName()
                    + ").  Cause: " + e, e);
        }
    }

    @Override
    protected BoundSql getDefaultBoundSql(Object parameterObject) {
        SqlSource sqlSource = createSqlSource(parameterObject);
        return sqlSource.getBoundSql(parameterObject);
    }

    @Override
    protected BoundSql getCountBoundSql(Object parameterObject) {
        BoundSql boundSql;
        SqlSource sqlSource = createSqlSource(parameterObject);
        boundSql = sqlSource.getBoundSql(parameterObject);
        return new BoundSql(
                configuration,
                parser.getCountSql(boundSql.getSql()),
                boundSql.getParameterMappings(),
                parameterObject);
    }

    @Override
    protected BoundSql getPageBoundSql(Object parameterObject) {
        BoundSql boundSql;
        if (parameterObject instanceof Map && ((Map) parameterObject).containsKey(PROVIDER_OBJECT)) {
            SqlSource sqlSource = createSqlSource(((Map) parameterObject).get(PROVIDER_OBJECT));
            boundSql = sqlSource.getBoundSql(((Map) parameterObject).get(PROVIDER_OBJECT));
        } else {
            SqlSource sqlSource = createSqlSource(parameterObject);
            boundSql = sqlSource.getBoundSql(parameterObject);
        }
        return new BoundSql(
                configuration,
                parser.getPageSql(boundSql.getSql()),
                parser.getPageParameterMapping(configuration, boundSql),
                parameterObject);
    }

    public SqlSource getOriginal() {
        return original;
    }
}