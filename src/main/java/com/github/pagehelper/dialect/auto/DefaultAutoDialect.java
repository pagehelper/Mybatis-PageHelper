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
import com.github.pagehelper.PageException;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.page.PageAutoDialect;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.mapping.MappedStatement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 最初的默认实现，获取连接再获取 url，这种方式通用性强，但是性能低，处理不好关闭连接时容易出问题
 *
 * @author liuzh
 */
public class DefaultAutoDialect implements AutoDialect<String> {

    public static final AutoDialect<String> DEFAULT = new DefaultAutoDialect();

    @Override
    public String extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return conn.getMetaData().getURL();
        } catch (SQLException e) {
            throw new PageException(e);
        } finally {
            if (conn != null) {
                try {
                    String closeConn = properties.getProperty("closeConn");
                    if (StringUtil.isEmpty(closeConn) || Boolean.parseBoolean(closeConn)) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    //ignore
                }
            }
        }
    }

    @Override
    public AbstractHelperDialect extractDialect(String dialectKey, MappedStatement ms, DataSource dataSource, Properties properties) {
        String dialectStr = PageAutoDialect.fromJdbcUrl(dialectKey);
        if (dialectStr == null) {
            throw new PageException("无法自动获取数据库类型，请通过 helperDialect 参数指定!");
        }
        return PageAutoDialect.instanceDialect(dialectStr, properties);
    }
}
