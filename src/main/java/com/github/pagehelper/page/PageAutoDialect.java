/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 abel533@gmail.com
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

package com.github.pagehelper.page;

import com.github.pagehelper.Dialect;
import com.github.pagehelper.PageException;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.dialect.helper.*;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.mapping.MappedStatement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基础方言信息
 *
 * @author liuzh
 */
public class PageAutoDialect {

    private static Map<String, Class<? extends Dialect>> dialectAliasMap = new HashMap<String, Class<? extends Dialect>>();

    public static void registerDialectAlias(String alias, Class<? extends Dialect> dialectClass){
        dialectAliasMap.put(alias, dialectClass);
    }

    static {
        //注册别名
        registerDialectAlias("hsqldb", HsqldbDialect.class);
        registerDialectAlias("h2", HsqldbDialect.class);
        registerDialectAlias("postgresql", HsqldbDialect.class);
        registerDialectAlias("phoenix", HsqldbDialect.class);

        registerDialectAlias("mysql", MySqlDialect.class);
        registerDialectAlias("mariadb", MySqlDialect.class);
        registerDialectAlias("sqlite", MySqlDialect.class);

        registerDialectAlias("oracle", OracleDialect.class);
        registerDialectAlias("db2", Db2Dialect.class);
        registerDialectAlias("informix", InformixDialect.class);
        //解决 informix-sqli #129，仍然保留上面的
        registerDialectAlias("informix-sqli", InformixDialect.class);

        registerDialectAlias("sqlserver", SqlServerDialect.class);
        registerDialectAlias("sqlserver2012", SqlServer2012Dialect.class);

        registerDialectAlias("derby", SqlServer2012Dialect.class);
        //达梦数据库,https://github.com/mybatis-book/book/issues/43
        registerDialectAlias("dm", OracleDialect.class);
        //阿里云PPAS数据库,https://github.com/pagehelper/Mybatis-PageHelper/issues/281
        registerDialectAlias("edb", OracleDialect.class);
    }

    //自动获取dialect,如果没有setProperties或setSqlUtilConfig，也可以正常进行
    private boolean autoDialect = true;
    //多数据源时，获取jdbcurl后是否关闭数据源
    private boolean closeConn = true;
    //属性配置
    private Properties properties;
    //缓存
    private Map<String, AbstractHelperDialect> urlDialectMap = new ConcurrentHashMap<String, AbstractHelperDialect>();
    private ReentrantLock lock = new ReentrantLock();
    private AbstractHelperDialect delegate;
    private ThreadLocal<AbstractHelperDialect> dialectThreadLocal = new ThreadLocal<AbstractHelperDialect>();

    //多数据动态获取时，每次需要初始化
    public void initDelegateDialect(MappedStatement ms) {
        if (delegate == null) {
            if (autoDialect) {
                this.delegate = getDialect(ms);
            } else {
                dialectThreadLocal.set(getDialect(ms));
            }
        }
    }

    //获取当前的代理对象
    public AbstractHelperDialect getDelegate() {
        if (delegate != null) {
            return delegate;
        }
        return dialectThreadLocal.get();
    }

    //移除代理对象
    public void clearDelegate() {
        dialectThreadLocal.remove();
    }

    private String fromJdbcUrl(String jdbcUrl) {
        for (String dialect : dialectAliasMap.keySet()) {
            if (jdbcUrl.indexOf(":" + dialect + ":") != -1) {
                return dialect;
            }
        }
        return null;
    }

    /**
     * 反射类
     *
     * @param className
     * @return
     * @throws Exception
     */
    private Class resloveDialectClass(String className) throws Exception {
        if (dialectAliasMap.containsKey(className.toLowerCase())) {
            return dialectAliasMap.get(className.toLowerCase());
        } else {
            return Class.forName(className);
        }
    }

    /**
     * 初始化 helper
     *
     * @param dialectClass
     * @param properties
     */
    private AbstractHelperDialect initDialect(String dialectClass, Properties properties) {
        AbstractHelperDialect dialect;
        if (StringUtil.isEmpty(dialectClass)) {
            throw new PageException("使用 PageHelper 分页插件时，必须设置 helper 属性");
        }
        try {
            Class sqlDialectClass = resloveDialectClass(dialectClass);
            if (AbstractHelperDialect.class.isAssignableFrom(sqlDialectClass)) {
                dialect = (AbstractHelperDialect) sqlDialectClass.newInstance();
            } else {
                throw new PageException("使用 PageHelper 时，方言必须是实现 " + AbstractHelperDialect.class.getCanonicalName() + " 接口的实现类!");
            }
        } catch (Exception e) {
            throw new PageException("初始化 helper [" + dialectClass + "]时出错:" + e.getMessage(), e);
        }
        dialect.setProperties(properties);
        return dialect;
    }

    /**
     * 获取url
     *
     * @param dataSource
     * @return
     */
    private String getUrl(DataSource dataSource) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return conn.getMetaData().getURL();
        } catch (SQLException e) {
            throw new PageException(e);
        } finally {
            if (conn != null) {
                try {
                    if (closeConn) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    //ignore
                }
            }
        }
    }

    /**
     * 根据 jdbcUrl 获取数据库方言
     *
     * @param ms
     * @return
     */
    private AbstractHelperDialect getDialect(MappedStatement ms) {
        //改为对dataSource做缓存
        DataSource dataSource = ms.getConfiguration().getEnvironment().getDataSource();
        String url = getUrl(dataSource);
        if (urlDialectMap.containsKey(url)) {
            return urlDialectMap.get(url);
        }
        try {
            lock.lock();
            if (urlDialectMap.containsKey(url)) {
                return urlDialectMap.get(url);
            }
            if (StringUtil.isEmpty(url)) {
                throw new PageException("无法自动获取jdbcUrl，请在分页插件中配置dialect参数!");
            }
            String dialectStr = fromJdbcUrl(url);
            if (dialectStr == null) {
                throw new PageException("无法自动获取数据库类型，请通过 helperDialect 参数指定!");
            }
            AbstractHelperDialect dialect = initDialect(dialectStr, properties);
            urlDialectMap.put(url, dialect);
            return dialect;
        } finally {
            lock.unlock();
        }
    }

    public void setProperties(Properties properties) {
        //多数据源时，获取 jdbcurl 后是否关闭数据源
        String closeConn = properties.getProperty("closeConn");
        if (StringUtil.isNotEmpty(closeConn)) {
            this.closeConn = Boolean.parseBoolean(closeConn);
        }
        //使用 sqlserver2012 作为默认分页方式，这种情况在动态数据源时方便使用
        String useSqlserver2012 = properties.getProperty("useSqlserver2012");
        if (StringUtil.isNotEmpty(useSqlserver2012) && Boolean.parseBoolean(useSqlserver2012)) {
            registerDialectAlias("sqlserver", SqlServer2012Dialect.class);
            registerDialectAlias("sqlserver2008", SqlServerDialect.class);
        }
        String dialectAlias = properties.getProperty("dialectAlias");
        if (StringUtil.isNotEmpty(dialectAlias)) {
            String[] alias = dialectAlias.split(";");
            for (int i = 0; i < alias.length; i++) {
                String[] kv = alias[i].split("=");
                if(kv.length != 2){
                    throw new IllegalArgumentException("dialectAlias 参数配置错误，" +
                            "请按照 alias1=xx.dialectClass;alias2=dialectClass2 的形式进行配置!");
                }
                for (int j = 0; j < kv.length; j++) {
                    try {
                        Class<? extends Dialect> diallectClass = (Class<? extends Dialect>) Class.forName(kv[1]);
                        //允许覆盖已有的实现
                        registerDialectAlias(kv[0], diallectClass);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalArgumentException("请确保 dialectAlias 配置的 Dialect 实现类存在!", e);
                    }
                }
            }
        }
        //指定的 Helper 数据库方言，和  不同
        String dialect = properties.getProperty("helperDialect");
        //运行时获取数据源
        String runtimeDialect = properties.getProperty("autoRuntimeDialect");
        //1.动态多数据源
        if (StringUtil.isNotEmpty(runtimeDialect) && "TRUE".equalsIgnoreCase(runtimeDialect)) {
            this.autoDialect = false;
            this.properties = properties;
        }
        //2.动态获取方言
        else if (StringUtil.isEmpty(dialect)) {
            autoDialect = true;
            this.properties = properties;
        }
        //3.指定方言
        else {
            autoDialect = false;
            this.delegate = initDialect(dialect, properties);
        }
    }
}
