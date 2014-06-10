/*
	The MIT License (MIT)

	Copyright (c) 2014 abel533@gmail.com

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
*/

package com.github.pagehelper;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Mybatis - 通用分页拦截器
 *
 * @author liuzh/abel533/isea533
 * @version 3.2.1
 * @url http://git.oschina.net/free/Mybatis_PageHelper
 */
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class PageHelper implements Interceptor {
    private static final ThreadLocal<Page> localPage = new ThreadLocal<Page>();

    private static final List<ResultMapping> EMPTY_RESULTMAPPING = new ArrayList<ResultMapping>(0);

    //数据库方言
    private static String dialect = "";
    //RowBounds参数offset作为PageNum使用 - 默认不使用
    private static boolean offsetAsPageNum = false;
    //RowBounds是否进行count查询 - 默认不查询
    private static boolean rowBoundsWithCount = false;

    /**
     * 开始分页
     *
     * @param pageNum
     * @param pageSize
     */
    public static void startPage(int pageNum, int pageSize) {
        startPage(pageNum, pageSize, true);
    }

    /**
     * 开始分页
     *
     * @param pageNum
     * @param pageSize
     */
    public static void startPage(int pageNum, int pageSize, boolean count) {
        localPage.set(new Page(pageNum, pageSize, count ? Page.SQL_COUNT : Page.NO_SQL_COUNT));
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final Object[] args = invocation.getArgs();
        RowBounds rowBounds = (RowBounds) args[2];
        if (localPage.get() == null && rowBounds == RowBounds.DEFAULT) {
            return invocation.proceed();
        } else {
            //忽略RowBounds-否则会进行Mybatis自带的内存分页
            args[2] = RowBounds.DEFAULT;
            MappedStatement ms = (MappedStatement) args[0];
            Object parameterObject = args[1];
            BoundSql boundSql = ms.getBoundSql(parameterObject);

            //分页信息
            Page page = localPage.get();
            //移除本地变量
            localPage.remove();

            if (page == null) {
                if (offsetAsPageNum) {
                    page = new Page(rowBounds.getOffset(), rowBounds.getLimit(), rowBoundsWithCount ? Page.SQL_COUNT : Page.NO_SQL_COUNT);
                } else {
                    page = new Page(rowBounds, rowBoundsWithCount ? Page.SQL_COUNT : Page.NO_SQL_COUNT);
                }
            }
            MappedStatement qs = newMappedStatement(ms, new BoundSqlSqlSource(boundSql));
            //将参数中的MappedStatement替换为新的qs，防止并发异常
            args[0] = qs;
            MetaObject msObject = SystemMetaObject.forObject(qs);
            String sql = (String) msObject.getValue("sqlSource.boundSql.sql");
            //简单的通过total的值来判断是否进行count查询
            if (page.getTotal() > Page.NO_SQL_COUNT) {
                //求count - 重写sql
                msObject.setValue("sqlSource.boundSql.sql", getCountSql(sql));
                //查询总数
                Object result = invocation.proceed();
                int totalCount = (Integer) ((List) result).get(0);
                page.setTotal(totalCount);
                int totalPage = totalCount / page.getPageSize() + ((totalCount % page.getPageSize() == 0) ? 0 : 1);
                page.setPages(totalPage);
                //分页sql - 重写sql
                msObject.setValue("sqlSource.boundSql.sql", getPageSql(sql, page));
                //恢复类型
                msObject.setValue("resultMaps", ms.getResultMaps());
                //执行分页查询
                result = invocation.proceed();
                //得到处理结果
                page.addAll((List) result);
                //返回结果
                return page;
            } else {
                //分页sql - 重写sql
                msObject.setValue("sqlSource.boundSql.sql", getPageSql(sql, page));
                //恢复类型
                msObject.setValue("resultMaps", ms.getResultMaps());
                //执行分页查询
                Object result = invocation.proceed();
                //得到处理结果
                page.addAll((List) result);
                //返回结果
                return page;
            }
        }
    }

    /**
     * 获取总数sql - 如果要支持其他数据库，修改这里就可以
     *
     * @param sql
     * @return
     */
    private String getCountSql(String sql) {
        return "select count(0) from (" + sql + ") tmp_count";
    }

    /**
     * 获取分页sql - 如果要支持其他数据库，修改这里就可以
     *
     * @param sql
     * @param page
     * @return
     */
    private String getPageSql(String sql, Page page) {
        StringBuilder pageSql = new StringBuilder(200);
        if ("mysql".equals(dialect)) {
            pageSql.append(sql);
            pageSql.append(" limit " + page.getStartRow() + "," + page.getPageSize());
        } else if ("hsqldb".equals(dialect)) {
            pageSql.append(sql);
            pageSql.append(" LIMIT " + page.getPageSize() + " OFFSET " + page.getStartRow());
        } else if ("oracle".equals(dialect)) {
            pageSql.append("select * from ( select temp.*, rownum row_id from ( ");
            pageSql.append(sql);
            pageSql.append(" ) temp where rownum <= ").append(page.getEndRow());
            pageSql.append(") where row_id > ").append(page.getStartRow());
        }
        return pageSql.toString();
    }

    private class BoundSqlSqlSource implements SqlSource {
        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

    /**
     * 由于MappedStatement是一个全局共享的对象，因而需要复制一个对象来进行操作，防止并发访问导致错误
     *
     * @param ms
     * @param newSqlSource
     * @return
     */
    private MappedStatement newMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + "_分页", newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuffer keyProperties = new StringBuffer();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        //由于resultMaps第一次需要返回int类型的结果，所以这里需要生成一个resultMap - 防止并发错误
        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), int.class, EMPTY_RESULTMAPPING).build();
        resultMaps.add(resultMap);
        builder.resultMaps(resultMaps);
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    /**
     * 只拦截Executor
     *
     * @param target
     * @return
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    public void setProperties(Properties p) {
        dialect = p.getProperty("dialect");
        if (dialect == null || dialect.equals("")) {
            throw new RuntimeException("Mybatis分页插件PageHelper无法获取dialect参数!");
        }
        //offset作为PageNum使用
        String offset = p.getProperty("offsetAsPageNum");
        if (offset != null && offset.toUpperCase().equals("TRUE")) {
            offsetAsPageNum = true;
        }
        //RowBounds方式是否做count查询
        String withcount = p.getProperty("rowBoundsWithCount");
        if (withcount != null && withcount.toUpperCase().equals("TRUE")) {
            rowBoundsWithCount = true;
        }
    }
}
