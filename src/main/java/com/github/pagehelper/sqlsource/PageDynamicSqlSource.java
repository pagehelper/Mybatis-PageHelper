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
import com.github.pagehelper.MSUtils;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

import java.util.Map;

/**
 * @author liuzh
 */
public class PageDynamicSqlSource implements SqlSource, Constant {
    private Configuration configuration;
    private SqlNode rootSqlNode;
    /**
     * 用于区分动态的count查询或分页查询
     */
    private Boolean count;

    private MSUtils msUtils;

    public PageDynamicSqlSource(MSUtils msUtils, Configuration configuration, SqlNode rootSqlNode, Boolean count) {
        this.msUtils = msUtils;
        this.configuration = configuration;
        this.rootSqlNode = rootSqlNode;
        this.count = count;
    }

    public BoundSql getBoundSql(Object parameterObject) {
        DynamicContext context;
        //由于增加分页参数后会修改parameterObject的值，因此在前面处理时备份该值
        //如果发现参数是Map并且包含该KEY，就使用备份的该值
        //解决bug#25:http://git.oschina.net/free/Mybatis_PageHelper/issues/25
        if (parameterObject != null
                && parameterObject instanceof Map
                && ((Map) parameterObject).containsKey(ORIGINAL_PARAMETER_OBJECT)) {
            context = new DynamicContext(configuration, ((Map) parameterObject).get(ORIGINAL_PARAMETER_OBJECT));
        } else {
            context = new DynamicContext(configuration, parameterObject);
        }
        rootSqlNode.apply(context);
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
        SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
        if (count) {
            sqlSource = msUtils.getStaticCountSqlSource(configuration, sqlSource, parameterObject);
        } else {
            sqlSource = msUtils.getStaticPageSqlSource(configuration, sqlSource, parameterObject);
        }
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        //设置条件参数
        for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        return boundSql;
    }
}