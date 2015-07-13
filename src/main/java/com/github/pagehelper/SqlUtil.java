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

import com.github.orderbyhelper.OrderByHelper;
import com.github.orderbyhelper.sqlsource.OrderBySqlSource;
import com.github.pagehelper.parser.Parser;
import com.github.pagehelper.parser.impl.AbstractParser;
import com.github.pagehelper.sqlsource.*;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mybatis - sql工具，获取分页和count的MappedStatement，设置分页参数
 *
 * @author liuzh/abel533/isea533
 * @since 3.6.0
 * 项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SqlUtil implements Constant {
    private static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<Page>();
    private static final ThreadLocal<Boolean> COUNT = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return null;
        }
    };
    private static final Map<String, MappedStatement> msCountMap = new ConcurrentHashMap<String, MappedStatement>();
    //params参数映射
    private static Map<String, String> PARAMS = new HashMap<String, String>(5);
    //request获取方法
    private static Boolean hasRequest;
    private static Class<?> requestClass;
    private static Method getParameterMap;

    static {
        try {
            requestClass = Class.forName("javax.servlet.ServletRequest");
            getParameterMap = requestClass.getMethod("getParameterMap", new Class[]{});
            hasRequest = true;
        } catch (Exception e) {
            hasRequest = false;
        }
    }

    //RowBounds参数offset作为PageNum使用 - 默认不使用
    private boolean offsetAsPageNum = false;
    //RowBounds是否进行count查询 - 默认不查询
    private boolean rowBoundsWithCount = false;
    //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
    private boolean pageSizeZero = false;
    //分页合理化
    private boolean reasonable = false;
    //具体针对数据库的parser
    private Parser parser;
    //数据库方言
    private Dialect dialect;

    /**
     * 构造方法
     *
     * @param strDialect
     */
    public SqlUtil(String strDialect) {
        if (strDialect == null || "".equals(strDialect)) {
            throw new IllegalArgumentException("Mybatis分页插件无法获取dialect参数!");
        }
        dialect = Dialect.of(strDialect);
        parser = AbstractParser.newParser(dialect);

    }

    public static Boolean getCOUNT() {
        return COUNT.get();
    }

    /**
     * 获取Page参数
     *
     * @return
     */
    public static Page getLocalPage() {
        return LOCAL_PAGE.get();
    }

    public static void setLocalPage(Page page) {
        LOCAL_PAGE.set(page);
    }

    /**
     * 移除本地变量
     */
    public static void clearLocalPage() {
        LOCAL_PAGE.remove();
        COUNT.remove();
    }

    /**
     * 对象中获取分页参数
     *
     * @param params
     * @return
     */
    public static Page getPageFromObject(Object params) {
        int pageNum;
        int pageSize;
        MetaObject paramsObject = null;
        if (params == null) {
            throw new NullPointerException("分页查询参数params不能为空!");
        }
        if (hasRequest && requestClass.isAssignableFrom(params.getClass())) {
            try {
                paramsObject = SystemMetaObject.forObject(getParameterMap.invoke(params, new Object[]{}));
            } catch (Exception e) {
                //忽略
            }
        } else {
            paramsObject = SystemMetaObject.forObject(params);
        }
        if (paramsObject == null) {
            throw new NullPointerException("分页查询参数params处理失败!");
        }
        try {
            pageNum = Integer.parseInt(String.valueOf(getParamValue(paramsObject, "pageNum", true)));
            pageSize = Integer.parseInt(String.valueOf(getParamValue(paramsObject, "pageSize", true)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("分页参数不是合法的数字类型!");
        }
        Object _count = getParamValue(paramsObject, "count", false);
        boolean count = true;
        if (_count != null) {
            count = Boolean.valueOf(String.valueOf(_count));
        }
        Page page = new Page(pageNum, pageSize, count);
        Object reasonable = getParamValue(paramsObject, "reasonable", false);
        if (reasonable != null) {
            page.setReasonable(Boolean.valueOf(String.valueOf(reasonable)));
        }
        Object pageSizeZero = getParamValue(paramsObject, "pageSizeZero", false);
        if (pageSizeZero != null) {
            page.setPageSizeZero(Boolean.valueOf(String.valueOf(pageSizeZero)));
        }
        return page;
    }

    /**
     * 从对象中取参数
     *
     * @param paramsObject
     * @param paramName
     * @param required
     * @return
     */
    public static Object getParamValue(MetaObject paramsObject, String paramName, boolean required) {
        Object value = null;
        if (paramsObject.hasGetter(PARAMS.get(paramName))) {
            value = paramsObject.getValue(PARAMS.get(paramName));
        }
        if (value != null && value.getClass().isArray()) {
            Object[] values = (Object[]) value;
            if (values.length == 0) {
                value = null;
            } else {
                value = values[0];
            }
        }
        if (required && value == null) {
            throw new RuntimeException("分页查询缺少必要的参数:" + PARAMS.get(paramName));
        }
        return value;
    }

    /**
     * 是否已经处理过
     *
     * @param ms
     * @return
     */
    public static boolean isPageSqlSource(MappedStatement ms) {
        if (ms.getSqlSource() instanceof PageSqlSource) {
            return true;
        }
        return false;
    }

    /**
     * 修改SqlSource
     *
     * @param ms
     * @param parser
     * @throws Throwable
     */
    public static void processMappedStatement(MappedStatement ms, Parser parser) throws Throwable {
        SqlSource sqlSource = ms.getSqlSource();
        MetaObject msObject = SystemMetaObject.forObject(ms);
        SqlSource tempSqlSource = sqlSource;
        if (sqlSource instanceof OrderBySqlSource) {
            tempSqlSource = ((OrderBySqlSource) tempSqlSource).getOriginal();
        }
        SqlSource pageSqlSource;
        if (tempSqlSource instanceof StaticSqlSource) {
            pageSqlSource = new PageStaticSqlSource((StaticSqlSource) tempSqlSource, parser);
        } else if (tempSqlSource instanceof RawSqlSource) {
            pageSqlSource = new PageRawSqlSource((RawSqlSource) tempSqlSource, parser);
        } else if (tempSqlSource instanceof ProviderSqlSource) {
            pageSqlSource = new PageProviderSqlSource((ProviderSqlSource) tempSqlSource, parser);
        } else if (tempSqlSource instanceof DynamicSqlSource) {
            pageSqlSource = new PageDynamicSqlSource((DynamicSqlSource) tempSqlSource, parser);
        } else {
            throw new RuntimeException("无法处理该类型[" + sqlSource.getClass() + "]的SqlSource");
        }
        msObject.setValue("sqlSource", pageSqlSource);
        //由于count查询需要修改返回值，因此这里要创建一个Count查询的MS
        msCountMap.put(ms.getId(), MSUtils.newCountMappedStatement(ms));
    }

    /**
     * 测试[控制台输出]count和分页sql
     *
     * @param dialect     数据库类型
     * @param originalSql 原sql
     */
    public static void testSql(String dialect, String originalSql) {
        testSql(Dialect.of(dialect), originalSql);
    }

    /**
     * 测试[控制台输出]count和分页sql
     *
     * @param dialect     数据库类型
     * @param originalSql 原sql
     */
    public static void testSql(Dialect dialect, String originalSql) {
        Parser parser = AbstractParser.newParser(dialect);
        if (dialect == Dialect.sqlserver) {
            setLocalPage(new Page(1, 10));
        }
        String countSql = parser.getCountSql(originalSql);
        System.out.println(countSql);
        String pageSql = parser.getPageSql(originalSql);
        System.out.println(pageSql);
        if (dialect == Dialect.sqlserver) {
            clearLocalPage();
        }
    }

    /**
     * 获取分页参数
     *
     * @param params RowBounds参数
     * @return 返回Page对象
     */
    public Page getPage(Object params) {
        Page page = getLocalPage();
        if (page == null) {
            if (params instanceof RowBounds) {
                RowBounds rowBounds = (RowBounds) params;
                if (offsetAsPageNum) {
                    page = new Page(rowBounds.getOffset(), rowBounds.getLimit(), rowBoundsWithCount);
                } else {
                    page = new Page(rowBounds, rowBoundsWithCount);
                    //offsetAsPageNum=false的时候，由于PageNum问题，不能使用reasonable，这里会强制为false
                    page.setReasonable(false);
                }
            } else {
                page = getPageFromObject(params);
            }
            setLocalPage(page);
        }
        //分页合理化
        if (page.getReasonable() == null) {
            page.setReasonable(reasonable);
        }
        //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
        if (page.getPageSizeZero() == null) {
            page.setPageSizeZero(pageSizeZero);
        }
        return page;
    }

    /**
     * Mybatis拦截器方法
     *
     * @param invocation 拦截器入参
     * @return 返回执行结果
     * @throws Throwable 抛出异常
     */
    public Object processPage(Invocation invocation) throws Throwable {
        try {
            Object result = _processPage(invocation);
            return result;
        } finally {
            clearLocalPage();
            OrderByHelper.clear();
        }
    }

    /**
     * Mybatis拦截器方法
     *
     * @param invocation 拦截器入参
     * @return 返回执行结果
     * @throws Throwable 抛出异常
     */
    private Object _processPage(Invocation invocation) throws Throwable {
        final Object[] args = invocation.getArgs();
        RowBounds rowBounds = (RowBounds) args[2];
        if (SqlUtil.getLocalPage() == null && rowBounds == RowBounds.DEFAULT) {
            if (OrderByHelper.getOrderBy() != null) {
                OrderByHelper.processIntercept(invocation);
            }
            return invocation.proceed();
        } else {
            //获取原始的ms
            MappedStatement ms = (MappedStatement) args[0];
            //判断并处理为PageSqlSource
            if (!isPageSqlSource(ms)) {
                processMappedStatement(ms, parser);
            }
            //忽略RowBounds-否则会进行Mybatis自带的内存分页
            args[2] = RowBounds.DEFAULT;
            //分页信息
            Page page = getPage(rowBounds);
            //pageSizeZero的判断
            if ((page.getPageSizeZero() != null && page.getPageSizeZero()) && page.getPageSize() == 0) {
                COUNT.set(null);
                //执行正常（不分页）查询
                Object result = invocation.proceed();
                //得到处理结果
                page.addAll((List) result);
                //相当于查询第一页
                page.setPageNum(1);
                //这种情况相当于pageSize=total
                page.setPageSize(page.size());
                //仍然要设置total
                page.setTotal(page.size());
                //返回结果仍然为Page类型 - 便于后面对接收类型的统一处理
                return page;
            }

            //简单的通过total的值来判断是否进行count查询
            if (page.isCount()) {
                COUNT.set(Boolean.TRUE);
                //替换MS
                args[0] = msCountMap.get(ms.getId());
                //查询总数
                Object result = invocation.proceed();
                //还原ms
                args[0] = ms;
                //设置总数
                page.setTotal((Integer) ((List) result).get(0));
                if (page.getTotal() == 0) {
                    return page;
                }
            }
            //pageSize>0的时候执行分页查询，pageSize<=0的时候不执行相当于可能只返回了一个count
            if (page.getPageSize() > 0 &&
                    ((rowBounds == RowBounds.DEFAULT && page.getPageNum() > 0)
                            || rowBounds != RowBounds.DEFAULT)) {
                //将参数中的MappedStatement替换为新的qs
                COUNT.set(null);
                BoundSql boundSql = ms.getBoundSql(args[1]);
                args[1] = parser.setPageParameter(ms, args[1], boundSql, page);
                COUNT.set(Boolean.FALSE);
                //执行分页查询
                Object result = invocation.proceed();
                //得到处理结果
                page.addAll((List) result);
            }
            //返回结果
            return page;
        }
    }

    public void setProperties(Properties p) {
        //offset作为PageNum使用
        String offsetAsPageNum = p.getProperty("offsetAsPageNum");
        this.offsetAsPageNum = Boolean.parseBoolean(offsetAsPageNum);
        //RowBounds方式是否做count查询
        String rowBoundsWithCount = p.getProperty("rowBoundsWithCount");
        this.rowBoundsWithCount = Boolean.parseBoolean(rowBoundsWithCount);
        //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页
        String pageSizeZero = p.getProperty("pageSizeZero");
        this.pageSizeZero = Boolean.parseBoolean(pageSizeZero);
        //分页合理化，true开启，如果分页参数不合理会自动修正。默认false不启用
        String reasonable = p.getProperty("reasonable");
        this.reasonable = Boolean.parseBoolean(reasonable);
        //当offsetAsPageNum=false的时候，不能
        //参数映射
        PARAMS.put("pageNum", "pageNum");
        PARAMS.put("pageSize", "pageSize");
        PARAMS.put("count", "countSql");
        PARAMS.put("reasonable", "reasonable");
        PARAMS.put("pageSizeZero", "pageSizeZero");
        String params = p.getProperty("params");
        if (params != null && params.length() > 0) {
            String[] ps = params.split("[;|,|&]");
            for (String s : ps) {
                String[] ss = s.split("[=|:]");
                if (ss.length == 2) {
                    PARAMS.put(ss[0], ss[1]);
                }
            }
        }
    }
}