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

package com.github.pagehelper.parser.impl;

import com.github.pagehelper.Page;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liuzh
 */
public class InformixParser extends AbstractParser {
    @Override
    public String getPageSql(String sql) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 40);
        sqlBuilder.append("select skip ? first ? * from ( ");
        sqlBuilder.append(sql);
        sqlBuilder.append(" ) temp_t");
        return sqlBuilder.toString();
    }

    @Override
    public List<ParameterMapping> getPageParameterMapping(Configuration configuration, BoundSql boundSql) {
        List<ParameterMapping> newParameterMappings = new ArrayList<ParameterMapping>();
        newParameterMappings.add(new ParameterMapping.Builder(configuration, PAGEPARAMETER_FIRST, Integer.class).build());
        newParameterMappings.add(new ParameterMapping.Builder(configuration, PAGEPARAMETER_SECOND, Integer.class).build());
        if (boundSql.getParameterMappings() != null) {
            newParameterMappings.addAll(boundSql.getParameterMappings());
        }
        return newParameterMappings;
    }

    @Override
    public Map setPageParameter(MappedStatement ms, Object parameterObject, BoundSql boundSql, Page page) {
        Map paramMap = super.setPageParameter(ms, parameterObject, boundSql, page);
        paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow());
        paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());
        return paramMap;
    }
}