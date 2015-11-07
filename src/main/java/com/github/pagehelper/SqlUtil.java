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
import java.util.*;
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

    //缓存count查询的ms
    private final Map<String, MappedStatement> msCountMap = new ConcurrentHashMap<String, MappedStatement>();
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
    //只返回PageInfo的查询结果
    private ReturnPageInfo returnPageInfo = ReturnPageInfo.NONE;
    //存储返回值为是否为PageInfo
    private Map<String, Boolean> returnPageInfoMap = new ConcurrentHashMap<String, Boolean>();
    //是否支持接口参数来传递分页参数，默认false
    private boolean supportMethodsArguments = false;

    /**
     * 构造方法
     *
     * @param strDialect
     */
    public SqlUtil(String strDialect) {
        if (strDialect == null || "".equals(strDialect)) {
            throw new IllegalArgumentException("Mybatis分页插件无法获取dialect参数!");
        }
        Exception exception = null;
        try {
            Dialect dialect = Dialect.of(strDialect);
            parser = AbstractParser.newParser(dialect);
        } catch (Exception e) {
            exception = e;
            //异常的时候尝试反射，允许自己写实现类传递进来
            try {
                Class<?> parserClass = Class.forName(strDialect);
                if (Parser.class.isAssignableFrom(parserClass)) {
                    parser = (Parser) parserClass.newInstance();
                }
            } catch (ClassNotFoundException ex) {
                exception = ex;
            } catch (InstantiationException ex) {
                exception = ex;
            } catch (IllegalAccessException ex) {
                exception = ex;
            }
        }
        if (parser == null) {
            throw new RuntimeException(exception);
        }
    }

    public static Boolean getCOUNT() {
        Page page = getLocalPage();
        if (page != null) {
            return page.getCountSignal();
        }
        return null;
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
            throw new NullPointerException("无法获取分页查询参数!");
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
            throw new NullPointerException("分页查询参数处理失败!");
        }
        Object orderBy = getParamValue(paramsObject, "orderBy", false);
        boolean hasOrderBy = false;
        if (orderBy != null && orderBy.toString().length() > 0) {
            hasOrderBy = true;
        }
        try {
            Object _pageNum = getParamValue(paramsObject, "pageNum", hasOrderBy ? false : true);
            Object _pageSize = getParamValue(paramsObject, "pageSize", hasOrderBy ? false : true);
            if (_pageNum == null || _pageSize == null) {
                Page page = new Page();
                page.setOrderBy(orderBy.toString());
                page.setOrderByOnly(true);
                return page;
            }
            pageNum = Integer.parseInt(String.valueOf(_pageNum));
            pageSize = Integer.parseInt(String.valueOf(_pageSize));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("分页参数不是合法的数字类型!");
        }
        Page page = new Page(pageNum, pageSize);
        //count查询
        Object _count = getParamValue(paramsObject, "count", false);
        if (_count != null) {
            page.setCount(Boolean.valueOf(String.valueOf(_count)));
        }
        //排序
        if (hasOrderBy) {
            page.setOrderBy(orderBy.toString());
        }
        //分页合理化
        Object reasonable = getParamValue(paramsObject, "reasonable", false);
        if (reasonable != null) {
            page.setReasonable(Boolean.valueOf(String.valueOf(reasonable)));
        }
        //查询全部
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
     * 修改SqlSource
     *
     * @param ms
     * @param parser
     * @throws Throwable
     */
    public void processMappedStatement(MappedStatement ms, Parser parser) throws Throwable {
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
     * 获取分页参数
     *
     * @param args
     * @return 返回Page对象
     */
    public Page getPage(Object[] args) {
        Page page = getLocalPage();
        if (page == null || page.isOrderByOnly()) {
            Page oldPage = page;
            //这种情况下,page.isOrderByOnly()必然为true，所以不用写到条件中
            if ((args[2] == null || args[2] == RowBounds.DEFAULT) && page != null) {
                return oldPage;
            }
            if (args[2] instanceof RowBounds && args[2] != RowBounds.DEFAULT) {
                RowBounds rowBounds = (RowBounds) args[2];
                if (offsetAsPageNum) {
                    page = new Page(rowBounds.getOffset(), rowBounds.getLimit(), rowBoundsWithCount);
                } else {
                    page = new Page(new int[]{rowBounds.getOffset(), rowBounds.getLimit()}, rowBoundsWithCount);
                    //offsetAsPageNum=false的时候，由于PageNum问题，不能使用reasonable，这里会强制为false
                    page.setReasonable(false);
                }
            } else {
                try {
                    page = getPageFromObject(args[1]);
                } catch (Exception e) {
                    return null;
                }
            }
            if (oldPage != null) {
                page.setOrderBy(oldPage.getOrderBy());
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
     * Mybatis拦截器方法，这一步嵌套为了在出现异常时也可以清空Threadlocal
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
        Page page = null;
        //支持方法参数时，会先尝试获取Page
        if (supportMethodsArguments) {
            page = getPage(args);
        }
        //分页信息
        RowBounds rowBounds = (RowBounds) args[2];
        //支持方法参数时，如果page == null就说明没有分页条件，不需要分页查询
        if ((supportMethodsArguments && page == null)
                //当不支持分页参数时，判断LocalPage和RowBounds判断是否需要分页
                || (!supportMethodsArguments && SqlUtil.getLocalPage() == null && rowBounds == RowBounds.DEFAULT)) {
            return invocation.proceed();
        } else {
            //不支持分页参数时，page==null，这里需要获取
            if (page == null) {
                page = getPage(args);
            }
            page = doProcessPage(invocation, page, args);
            return processPageInfo(page, args);
        }
    }

    /**
     * 处理Page结果，看是否需要处理为PageInfo
     *
     * @param page
     * @param args
     * @return
     */
    private Object processPageInfo(Page<?> page, Object[] args) {
        switch (returnPageInfo) {
            case NONE:
                return page;
            case ALWAYS:
                return returnPageInfo(page);
            case CHECK:
                if (isReturnPageInfo((MappedStatement) args[0])) {
                    return returnPageInfo(page);
                } else {
                    return page;
                }
        }
        return page;
    }

    /**
     * 返回PageInfo类型
     *
     * @param page
     * @return
     */
    private Object returnPageInfo(Page<?> page) {
        List<PageInfo> list = new ArrayList<PageInfo>();
        list.add(new PageInfo(page));
        return list;
    }

    /**
     * 是否为返回PageInfo的方法
     *
     * @param ms
     * @return
     */
    private boolean isReturnPageInfo(MappedStatement ms) {
        String msId = ms.getId();
        if (!returnPageInfoMap.containsKey(msId)) {
            String _interface = msId.substring(0, msId.lastIndexOf("."));
            String _methodName = msId.substring(_interface.length() + 1);
            try {
                Class<?> mapperClass = Class.forName(_interface);
                Method[] methods = mapperClass.getDeclaredMethods();
                Method m = null;
                for (Method method : methods) {
                    if (method.getName().equals(_methodName)) {
                        m = method;
                        break;
                    }
                }
                if (m == null) {
                    returnPageInfoMap.put(msId, false);
                } else {
                    Class<?> returnClass = m.getReturnType();
                    if (returnClass.equals(PageInfo.class)) {
                        returnPageInfoMap.put(msId, true);
                    } else {
                        returnPageInfoMap.put(msId, false);
                    }
                }
            } catch (ClassNotFoundException e) {
                returnPageInfoMap.put(msId, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return returnPageInfoMap.get(msId);
    }

    /**
     * 是否只做查询
     *
     * @param page
     * @return
     */
    private boolean isQueryOnly(Page page) {
        return page.isOrderByOnly()
                || ((page.getPageSizeZero() != null && page.getPageSizeZero()) && page.getPageSize() == 0);
    }

    /**
     * 只做查询
     *
     * @param page
     * @param invocation
     * @return
     * @throws Throwable
     */
    private Page doQueryOnly(Page page, Invocation invocation) throws Throwable {
        page.setCountSignal(null);
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

    /**
     * Mybatis拦截器方法
     *
     * @param invocation 拦截器入参
     * @return 返回执行结果
     * @throws Throwable 抛出异常
     */
    private Page doProcessPage(Invocation invocation, Page page, Object[] args) throws Throwable {
        //保存RowBounds状态
        RowBounds rowBounds = (RowBounds) args[2];
        //获取原始的ms
        MappedStatement ms = (MappedStatement) args[0];
        //判断并处理为PageSqlSource
        if (!isPageSqlSource(ms)) {
            processMappedStatement(ms, parser);
        }
        //忽略RowBounds-否则会进行Mybatis自带的内存分页
        args[2] = RowBounds.DEFAULT;
        //如果只进行排序 或 pageSizeZero的判断
        if (isQueryOnly(page)) {
            return doQueryOnly(page, invocation);
        }

        //简单的通过total的值来判断是否进行count查询
        if (page.isCount()) {
            page.setCountSignal(Boolean.TRUE);
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
        } else {
            page.setTotal(-1l);
        }
        //pageSize>0的时候执行分页查询，pageSize<=0的时候不执行相当于可能只返回了一个count
        if (page.getPageSize() > 0 &&
                ((rowBounds == RowBounds.DEFAULT && page.getPageNum() > 0)
                        || rowBounds != RowBounds.DEFAULT)) {
            //将参数中的MappedStatement替换为新的qs
            page.setCountSignal(null);
            BoundSql boundSql = ms.getBoundSql(args[1]);
            args[1] = parser.setPageParameter(ms, args[1], boundSql, page);
            page.setCountSignal(Boolean.FALSE);
            //执行分页查询
            Object result = invocation.proceed();
            //得到处理结果
            page.addAll((List) result);
        }
        //返回结果
        return page;
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
        //是否支持接口参数来传递分页参数，默认false
        String supportMethodsArguments = p.getProperty("supportMethodsArguments");
        this.supportMethodsArguments = Boolean.parseBoolean(supportMethodsArguments);
        //returnPageInfo
        String returnPageInfo = p.getProperty("returnPageInfo");
        if (returnPageInfo != null && returnPageInfo.length() > 0) {
            this.returnPageInfo = ReturnPageInfo.valueOf(returnPageInfo.toUpperCase());
        }
        //当offsetAsPageNum=false的时候，不能
        //参数映射
        PARAMS.put("pageNum", "pageNum");
        PARAMS.put("pageSize", "pageSize");
        PARAMS.put("count", "countSql");
        PARAMS.put("orderBy", "orderBy");
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