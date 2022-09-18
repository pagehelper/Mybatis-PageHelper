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
import org.apache.ibatis.mapping.MappedStatement;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 遍历所有实现，找到匹配的实现
 *
 * @author liuzh
 */
public class DataSourceNegotiationAutoDialect implements AutoDialect<String> {
    private static final List<DataSourceAutoDialect>        AUTO_DIALECTS = new ArrayList<DataSourceAutoDialect>();
    private              Map<String, DataSourceAutoDialect> urlMap        = new ConcurrentHashMap<String, DataSourceAutoDialect>();

    static {
        //创建时，初始化所有实现，当依赖的连接池不存在时，这里不会添加成功，所以理论上这里包含的内容不会多，执行时不会迭代多次
        try {
            AUTO_DIALECTS.add(new HikariAutoDialect());
        } catch (Exception ignore) {
        }
        try {
            AUTO_DIALECTS.add(new DruidAutoDialect());
        } catch (Exception ignore) {
        }
        try {
            AUTO_DIALECTS.add(new TomcatAutoDialect());
        } catch (Exception ignore) {
        }
        try {
            AUTO_DIALECTS.add(new C3P0AutoDialect());
        } catch (Exception ignore) {
        }
        try {
            AUTO_DIALECTS.add(new DbcpAutoDialect());
        } catch (Exception ignore) {
        }
    }

    /**
     * 允许手工添加额外的实现，实际上没有必要
     *
     * @param autoDialect
     */
    public static void registerAutoDialect(DataSourceAutoDialect autoDialect) {
        AUTO_DIALECTS.add(autoDialect);
    }

    @Override
    public String extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties) {
        for (DataSourceAutoDialect autoDialect : AUTO_DIALECTS) {
            String dialectKey = autoDialect.extractDialectKey(ms, dataSource, properties);
            if (dialectKey != null) {
                if (!urlMap.containsKey(dialectKey)) {
                    urlMap.put(dialectKey, autoDialect);
                }
                return dialectKey;
            }
        }
        //都不匹配的时候使用默认方式
        return DefaultAutoDialect.DEFAULT.extractDialectKey(ms, dataSource, properties);
    }

    @Override
    public AbstractHelperDialect extractDialect(String dialectKey, MappedStatement ms, DataSource dataSource, Properties properties) {
        if (dialectKey != null && urlMap.containsKey(dialectKey)) {
            return urlMap.get(dialectKey).extractDialect(dialectKey, ms, dataSource, properties);
        }
        //都不匹配的时候使用默认方式
        return DefaultAutoDialect.DEFAULT.extractDialect(dialectKey, ms, dataSource, properties);
    }

}
