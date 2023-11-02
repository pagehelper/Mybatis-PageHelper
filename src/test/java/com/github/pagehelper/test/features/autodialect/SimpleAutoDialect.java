/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2023 abel533@gmail.com
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

package com.github.pagehelper.test.features.autodialect;

import com.github.pagehelper.AutoDialect;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.page.PageAutoDialect;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.MappedStatement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 简单示例，直接从属性读取url，无需获取数据库连接，也不需要考虑是否需要关闭
 */
public class SimpleAutoDialect implements AutoDialect<UnpooledDataSource> {

    @Override
    public UnpooledDataSource extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties) {
        if (dataSource instanceof UnpooledDataSource) {
            return (UnpooledDataSource) dataSource;

        }
        throw new UnsupportedOperationException("不支持的数据源类型: " + dataSource.getClass().getName());
    }

    @Override
    public AbstractHelperDialect extractDialect(UnpooledDataSource dialectKey, MappedStatement ms, DataSource dataSource, Properties properties) {
        String url = dialectKey.getUrl();
        String dialect = PageAutoDialect.fromJdbcUrl(url);
        return PageAutoDialect.instanceDialect(dialect, properties);
    }

}
