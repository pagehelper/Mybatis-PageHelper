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

package com.github.pagehelper.dialect.auto;

import com.github.pagehelper.AutoDialect;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.page.PageAutoDialect;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeReference;

import javax.sql.DataSource;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Properties;

/**
 * 使用 Hikari 连接池时，简单获取 jdbcUrl
 *
 * @author liuzh
 */
public abstract class DataSourceAutoDialect<Ds extends DataSource> implements AutoDialect<String> {
    protected Class dataSourceClass;

    public DataSourceAutoDialect() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        dataSourceClass = (Class) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
    }

    public abstract String getJdbcUrl(Ds ds);

    @Override
    public String extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties) {
        if (dataSourceClass.isInstance(dataSource)) {
            return getJdbcUrl((Ds) dataSource);
        }
        return null;
    }

    @Override
    public AbstractHelperDialect extractDialect(String dialectKey, MappedStatement ms, DataSource dataSource, Properties properties) {
        String dialect = PageAutoDialect.fromJdbcUrl(dialectKey);
        return PageAutoDialect.instanceDialect(dialect, properties);
    }

}
