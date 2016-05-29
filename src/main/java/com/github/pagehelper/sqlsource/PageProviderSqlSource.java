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
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liuzh
 */
public class PageProviderSqlSource extends PageSqlSource implements Constant {

    private SqlSourceBuilder sqlSourceParser;
    private Class<?> providerType;
    private Method providerMethod;
    private Boolean providerTakesParameterObject;
    private String[] providerMethodArgumentNames;
    private Configuration configuration;

    public PageProviderSqlSource(ProviderSqlSource provider) {
        MetaObject metaObject = SystemMetaObject.forObject(provider);
        this.sqlSourceParser = (SqlSourceBuilder) metaObject.getValue("sqlSourceParser");
        this.providerType = (Class<?>) metaObject.getValue("providerType");
        this.providerMethod = (Method) metaObject.getValue("providerMethod");
        this.configuration = (Configuration) metaObject.getValue("sqlSourceParser.configuration");
        try {
            //先针对3.3.1和之前版本做判断
            this.providerTakesParameterObject = (Boolean) metaObject.getValue("providerTakesParameterObject");
        } catch (ReflectionException e) {
            //3.4.0+版本，解决#102 by Ian Lim
            providerMethodArgumentNames = (String[]) metaObject.getValue("providerMethodArgumentNames");
        }
    }

    private SqlSource createSqlSource(Object parameterObject) {
        if(providerTakesParameterObject != null){
            return createSqlSource331(parameterObject);
        } else {
            return createSqlSource340(parameterObject);
        }
    }

    /**
     * 3.3.1版本之前的方法
     *
     * @param parameterObject
     * @return
     */
    private SqlSource createSqlSource331(Object parameterObject) {
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

    /**
     * 3.4.0之后的方法
     *
     * @param parameterObject
     * @return
     */
    private SqlSource createSqlSource340(Object parameterObject) {
        try {
            Class<?>[] parameterTypes = providerMethod.getParameterTypes();
            String sql;
            if (parameterTypes.length == 0) {
                sql = (String) providerMethod.invoke(providerType.newInstance());
            } else if (parameterTypes.length == 1 &&
                    (parameterObject == null || parameterTypes[0].isAssignableFrom(parameterObject.getClass()))) {
                sql = (String) providerMethod.invoke(providerType.newInstance(), parameterObject);
            } else if (parameterObject instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>) parameterObject;
                sql = (String) providerMethod.invoke(providerType.newInstance(), extractProviderMethodArguments(params, providerMethodArgumentNames));
            } else {
                throw new BuilderException("Error invoking SqlProvider method ("
                        + providerType.getName() + "." + providerMethod.getName()
                        + "). Cannot invoke a method that holds "
                        + (parameterTypes.length == 1 ? "named argument(@Param)": "multiple arguments")
                        + " using a specifying parameterObject. In this case, please specify a 'java.util.Map' object.");
            }
            Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
            StaticSqlSource sqlSource = (StaticSqlSource) sqlSourceParser.parse(sql, parameterType, new HashMap<String, Object>());
            return new OrderByStaticSqlSource(sqlSource);
            //return sqlSourceParser.parse(sql, parameterType, new HashMap<String, Object>());
        } catch (BuilderException e) {
            throw e;
        } catch (Exception e) {
            throw new BuilderException("Error invoking SqlProvider method ("
                    + providerType.getName() + "." + providerMethod.getName()
                    + ").  Cause: " + e, e);
        }
    }

    private Object[] extractProviderMethodArguments(Map<String, Object> params, String[] argumentNames) {
        Object[] args = new Object[argumentNames.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = params.get(argumentNames[i]);
        }
        return args;
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
                localParser.get().getCountSql(boundSql.getSql()),
                boundSql.getParameterMappings(),
                parameterObject);
    }

    @SuppressWarnings("rawtypes")
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
                localParser.get().getPageSql(boundSql.getSql()),
                localParser.get().getPageParameterMapping(configuration, boundSql),
                parameterObject);
    }

}