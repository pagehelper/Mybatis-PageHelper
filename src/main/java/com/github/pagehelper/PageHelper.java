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

package com.github.pagehelper;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Mybatis - 通用分页拦截器
 *
 * @author liuzh/abel533/isea533
 * @version 3.3.0
 *          项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 */
@SuppressWarnings("rawtypes")
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class PageHelper implements Interceptor {
    //sql工具类
    private SqlUtil sqlUtil;
    //属性参数信息
    private Properties properties;
    //配置对象方式
    private SqlUtilConfig sqlUtilConfig;
    //自动获取dialect,如果没有setProperties或setSqlUtilConfig，也可以正常进行
    private boolean autoDialect = true;
    //运行时自动获取dialect
    private boolean autoRuntimeDialect;
    //多数据源时，获取jdbcurl后是否关闭数据源
    private boolean closeConn = true;
    //缓存
    private Map<String, SqlUtil> urlSqlUtilMap = new ConcurrentHashMap<String, SqlUtil>();
    private ReentrantLock lock = new ReentrantLock();

    /**
     * 获取任意查询方法的count总数
     *
     * @param select
     * @return
     */
    public static long count(ISelect select) {
        Page<?> page = startPage(1, -1, true);
        select.doSelect();
        return page.getTotal();
    }

    /**
     * 开始分页
     *
     * @param pageNum  页码
     * @param pageSize 每页显示数量
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize) {
        return startPage(pageNum, pageSize, true);
    }

    /**
     * 开始分页
     *
     * @param pageNum  页码
     * @param pageSize 每页显示数量
     * @param count    是否进行count查询
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, boolean count) {
        return startPage(pageNum, pageSize, count, null);
    }

    /**
     * 开始分页
     *
     * @param pageNum  页码
     * @param pageSize 每页显示数量
     * @param orderBy  排序
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, String orderBy) {
        Page<E> page = startPage(pageNum, pageSize);
        page.setOrderBy(orderBy);
        return page;
    }

    /**
     * 开始分页
     *
     * @param offset 页码
     * @param limit  每页显示数量
     */
    public static <E> Page<E> offsetPage(int offset, int limit) {
        return offsetPage(offset, limit, true);
    }

    /**
     * 开始分页
     *
     * @param offset 页码
     * @param limit  每页显示数量
     * @param count  是否进行count查询
     */
    public static <E> Page<E> offsetPage(int offset, int limit, boolean count) {
        Page<E> page = new Page<E>(new int[]{offset, limit}, count);
        //当已经执行过orderBy的时候
        Page<E> oldPage = SqlUtil.getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        SqlUtil.setLocalPage(page);
        return page;
    }

    /**
     * 开始分页
     *
     * @param offset  页码
     * @param limit   每页显示数量
     * @param orderBy 排序
     */
    public static <E> Page<E> offsetPage(int offset, int limit, String orderBy) {
        Page<E> page = offsetPage(offset, limit);
        page.setOrderBy(orderBy);
        return page;
    }

    /**
     * 开始分页
     *
     * @param pageNum    页码
     * @param pageSize   每页显示数量
     * @param count      是否进行count查询
     * @param reasonable 分页合理化,null时用默认配置
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, boolean count, Boolean reasonable) {
        return startPage(pageNum, pageSize, count, reasonable, null);
    }

    /**
     * 开始分页
     *
     * @param pageNum      页码
     * @param pageSize     每页显示数量
     * @param count        是否进行count查询
     * @param reasonable   分页合理化,null时用默认配置
     * @param pageSizeZero true且pageSize=0时返回全部结果，false时分页,null时用默认配置
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, boolean count, Boolean reasonable, Boolean pageSizeZero) {
        Page<E> page = new Page<E>(pageNum, pageSize, count);
        page.setReasonable(reasonable);
        page.setPageSizeZero(pageSizeZero);
        //当已经执行过orderBy的时候
        Page<E> oldPage = SqlUtil.getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        SqlUtil.setLocalPage(page);
        return page;
    }

    /**
     * 开始分页
     *
     * @param params
     */
    public static <E> Page<E> startPage(Object params) {
        Page<E> page = SqlUtil.getPageFromObject(params);
        //当已经执行过orderBy的时候
        Page<E> oldPage = SqlUtil.getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        SqlUtil.setLocalPage(page);
        return page;
    }

    /*
    * 分页并返回PageInfo
    * @param pageNum      页码
    * @param pageSize     每页显示数量
    * @action 查询并返回List<E>的 lambda表达式
    * @since 1.8
    * */
    public static <E> PageInfo<E> startPage(final int pageNum, final int pageSize, Supplier<List<E>> action){
        // 开始分页
        startPage(pageNum, pageSize);
        return new PageInfo<E>(action.get());
    }

    /**
     * 排序
     *
     * @param orderBy
     */
    public static void orderBy(String orderBy) {
        Page<?> page = SqlUtil.getLocalPage();
        if (page != null) {
            page.setOrderBy(orderBy);
        } else {
            page = new Page();
            page.setOrderBy(orderBy);
            page.setOrderByOnly(true);
            SqlUtil.setLocalPage(page);
        }
    }

    /**
     * 获取orderBy
     *
     * @return
     */
    public static String getOrderBy() {
        Page<?> page = SqlUtil.getLocalPage();
        if (page != null) {
            String orderBy = page.getOrderBy();
            if (StringUtil.isEmpty(orderBy)) {
                return null;
            } else {
                return orderBy;
            }
        }
        return null;
    }

    /**
     * Mybatis拦截器方法
     *
     * @param invocation 拦截器入参
     * @return 返回执行结果
     * @throws Throwable 抛出异常
     */
    public Object intercept(Invocation invocation) throws Throwable {
        if (autoRuntimeDialect) {
            SqlUtil sqlUtil = getSqlUtil(invocation);
            return sqlUtil.processPage(invocation);
        } else {
            if (autoDialect) {
                initSqlUtil(invocation);
            }
            return sqlUtil.processPage(invocation);
        }
    }

    /**
     * 初始化sqlUtil
     *
     * @param invocation
     */
    public synchronized void initSqlUtil(Invocation invocation) {
        if (this.sqlUtil == null) {
            this.sqlUtil = getSqlUtil(invocation);
            if (!autoRuntimeDialect) {
                properties = null;
                sqlUtilConfig = null;
            }
            autoDialect = false;
        }
    }

    /**
     * 获取url
     *
     * @param dataSource
     * @return
     */
    public String getUrl(DataSource dataSource){
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return conn.getMetaData().getURL();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if(conn != null){
                try {
                    if(closeConn){
                        conn.close();
                    }
                } catch (SQLException e) {
                    //ignore
                }
            }
        }
    }

    /**
     * 根据datasource创建对应的sqlUtil
     *
     * @param invocation
     */
    public SqlUtil getSqlUtil(Invocation invocation) {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        //改为对dataSource做缓存
        DataSource dataSource = ms.getConfiguration().getEnvironment().getDataSource();
        String url = getUrl(dataSource);
        if (urlSqlUtilMap.containsKey(url)) {
            return urlSqlUtilMap.get(url);
        }
        try {
            lock.lock();
            if (urlSqlUtilMap.containsKey(url)) {
                return urlSqlUtilMap.get(url);
            }
            if (StringUtil.isEmpty(url)) {
                throw new RuntimeException("无法自动获取jdbcUrl，请在分页插件中配置dialect参数!");
            }
            String dialect = Dialect.fromJdbcUrl(url);
            if (dialect == null) {
                throw new RuntimeException("无法自动获取数据库类型，请通过dialect参数指定!");
            }
            SqlUtil sqlUtil = null;
            if (properties != null) {
                sqlUtil = new SqlUtil(dialect, properties.getProperty("sqlCacheClass"));
                sqlUtil.setProperties(properties);
            } else if (sqlUtilConfig != null) {
                sqlUtil = new SqlUtil(dialect, sqlUtilConfig.getSqlCacheClass());
                sqlUtil.setSqlUtilConfig(sqlUtilConfig);
            }
            urlSqlUtilMap.put(url, sqlUtil);
            return sqlUtil;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 只拦截Executor
     *
     * @param target
     * @return
     */
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    private void checkVersion() {
        //MyBatis3.2.0版本校验
        try {
            Class.forName("org.apache.ibatis.scripting.xmltags.SqlNode");//SqlNode是3.2.0之后新增的类
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("您使用的MyBatis版本太低，MyBatis分页插件PageHelper支持MyBatis3.2.0及以上版本!");
        }
    }

    /**
     * 设置属性值
     *
     * @param p 属性值
     */
    public void setProperties(Properties p) {
        checkVersion();
        //多数据源时，获取jdbcurl后是否关闭数据源
        String closeConn = p.getProperty("closeConn");
        //解决#97
        if(StringUtil.isNotEmpty(closeConn)){
            this.closeConn = Boolean.parseBoolean(closeConn);
        }
        //初始化SqlUtil的PARAMS
        SqlUtil.setParams(p.getProperty("params"));
        //数据库方言
        String dialect = p.getProperty("dialect");
        String runtimeDialect = p.getProperty("autoRuntimeDialect");
        if (StringUtil.isNotEmpty(runtimeDialect) && runtimeDialect.equalsIgnoreCase("TRUE")) {
            this.autoRuntimeDialect = true;
            this.autoDialect = false;
            this.properties = p;
        } else if (StringUtil.isEmpty(dialect)) {
            autoDialect = true;
            this.properties = p;
        } else {
            autoDialect = false;
            sqlUtil = new SqlUtil(dialect, p.getProperty("sqlCacheClass"));
            sqlUtil.setProperties(p);
        }
    }

    /**
     * 设置属性值
     *
     * @param config
     */
    public void setSqlUtilConfig(SqlUtilConfig config) {
        checkVersion();
        //初始化SqlUtil的PARAMS
        SqlUtil.setParams(config.getParams());
        //多数据源时，获取jdbcurl后是否关闭数据源
        this.closeConn = config.isCloseConn();
        if (config.isAutoRuntimeDialect()) {
            this.autoRuntimeDialect = true;
            this.autoDialect = false;
            this.sqlUtilConfig = config;
        } else if (StringUtil.isEmpty(config.getDialect())) {
            autoDialect = true;
            this.sqlUtilConfig = config;
        } else {
            autoDialect = false;
            sqlUtil = new SqlUtil(config.getDialect(), config.getSqlCacheClass());
            sqlUtil.setSqlUtilConfig(config);
        }
    }
}
