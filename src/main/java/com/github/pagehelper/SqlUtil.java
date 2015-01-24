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

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Mybatis - sql工具，获取分页和count的MappedStatement，设置分页参数
 *
 * @author liuzh/abel533/isea533
 * @since 3.3.0
 * 项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SqlUtil {
    private static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<Page>();
    //RowBounds参数offset作为PageNum使用 - 默认不使用
    private boolean offsetAsPageNum = false;
    //RowBounds是否进行count查询 - 默认不查询
    private boolean rowBoundsWithCount = false;
    //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
    private boolean pageSizeZero = false;
    //分页合理化
    private boolean reasonable = false;
    //params参数映射
    private static Map<String, String> PARAMS = new HashMap<String, String>(5);
    //request获取方法
    private static Boolean hasRequest;
    private static Class<?> requestClass;
    private static Method getParameter;

    static {
        try {
            requestClass = Class.forName("javax.servlet.ServletRequest");
            getParameter = requestClass.getMethod("getParameter", String.class);
            hasRequest = true;
        } catch (Exception e) {
            hasRequest = false;
        }
    }

    private static final List<ResultMapping> EMPTY_RESULTMAPPING = new ArrayList<ResultMapping>(0);
    //分页的id后缀
    private static final String SUFFIX_PAGE = "_PageHelper";
    //count查询的id后缀
    private static final String SUFFIX_COUNT = SUFFIX_PAGE + "_Count";
    //第一个分页参数
    private static final String PAGEPARAMETER_FIRST = "First" + SUFFIX_PAGE;
    //第二个分页参数
    private static final String PAGEPARAMETER_SECOND = "Second" + SUFFIX_PAGE;

    private static final String PROVIDER_OBJECT = "_provider_object";
    //存储原始的参数
    private static final String ORIGINAL_PARAMETER_OBJECT = "_ORIGINAL_PARAMETER_OBJECT";

    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

    /**
     * 反射对象，增加对低版本Mybatis的支持
     *
     * @param object 反射对象
     * @return
     */
    private static MetaObject forObject(Object object) {
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
    }

    //处理SQL
    private static final SqlParser sqlParser = new SqlParser();
    //具体针对数据库的parser
    private Parser parser;
    //数据库方言
    private Dialect dialect;

    //数据库方言 - 使用枚举限制数据库类型
    public enum Dialect {
        mysql, mariadb, sqlite, oracle, hsqldb, postgresql
    }

    public static void setLocalPage(Page page) {
        LOCAL_PAGE.set(page);
    }

    /**
     * 获取Page参数
     *
     * @return
     */
    public static Page getLocalPage() {
        return LOCAL_PAGE.get();
    }

    /**
     * 移除本地变量
     */
    public static void clearLocalPage() {
        LOCAL_PAGE.remove();
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
     * 从request或Map中获取分页参数
     *
     * @param params
     * @return
     */
    public static Page getPageFromObject(Object params) {
        int pageNum;
        int pageSize;
        try {
            pageNum = Integer.parseInt(String.valueOf(getParamValue(params, "pageNum", true)));
            pageSize = Integer.parseInt(String.valueOf(getParamValue(params, "pageSize", true)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("分页参数不是合法的数字类型!");
        }
        Object _count = getParamValue(params, "count", false);
        boolean count = true;
        if (_count != null) {
            count = Boolean.valueOf(String.valueOf(_count));
        }
        Page page = new Page(pageNum, pageSize, count);
        Object reasonable = getParamValue(params, "reasonable", false);
        if (reasonable != null) {
            page.setReasonable(Boolean.valueOf(String.valueOf(reasonable)));
        }
        Object pageSizeZero = getParamValue(params, "pageSizeZero", false);
        if (pageSizeZero != null) {
            page.setPageSizeZero(Boolean.valueOf(String.valueOf(pageSizeZero)));
        }
        return page;
    }

    /**
     * 从对象中取参数
     *
     * @param params
     * @param paramName
     * @param required
     * @return
     */
    public static Object getParamValue(Object params, String paramName, boolean required) {
        if (params == null) {
            throw new NullPointerException("分页查询参数params不能为空!");
        }
        Object value = null;
        if (params instanceof Map) {
            if (((Map) params).containsKey(PARAMS.get(paramName))) {
                value = ((Map) params).get(PARAMS.get(paramName));
            }
        } else if (hasRequest && requestClass.isAssignableFrom(params.getClass())) {
            try {
                value = getParameter.invoke(params, PARAMS.get(paramName));
            } catch (Exception e) {
                //忽略
            }
        } else {
            //TODO 其他类型
        }
        if (required && value == null) {
            throw new RuntimeException("分页查询缺少必要的参数:" + PARAMS.get(paramName));
        }
        return value;
    }

    /**
     * Mybatis拦截器方法
     *
     * @param invocation 拦截器入参
     * @return 返回执行结果
     * @throws Throwable 抛出异常
     */
    public Object processPage(Invocation invocation) throws Throwable {
        Object result = _processPage(invocation);
        clearLocalPage();
        return result;
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
            return invocation.proceed();
        } else {
            //获取原始的ms
            MappedStatement ms = (MappedStatement) args[0];
            //忽略RowBounds-否则会进行Mybatis自带的内存分页
            args[2] = RowBounds.DEFAULT;
            //分页信息
            Page page = getPage(rowBounds);
            //pageSizeZero的判断
            if ((page.getPageSizeZero() != null && page.getPageSizeZero()) && page.getPageSize() == 0) {
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
            SqlSource sqlSource = ((MappedStatement) args[0]).getSqlSource();
            //简单的通过total的值来判断是否进行count查询
            if (page.isCount()) {
                //将参数中的MappedStatement替换为新的qs
                processCountMappedStatement(ms, sqlSource, args);
                //查询总数
                Object result = invocation.proceed();
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
                processPageMappedStatement(ms, sqlSource, page, args);
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

    /**
     * 构造方法
     *
     * @param strDialect
     */
    public SqlUtil(String strDialect) {
        if (strDialect == null || "".equals(strDialect)) {
            throw new IllegalArgumentException("Mybatis分页插件无法获取dialect参数!");
        }
        try {
            dialect = Dialect.valueOf(strDialect);
            parser = SimpleParser.newParser(dialect);
        } catch (IllegalArgumentException e) {
            String dialects = null;
            for (Dialect d : Dialect.values()) {
                if (dialects == null) {
                    dialects = d.toString();
                } else {
                    dialects += "," + d;
                }
            }
            throw new IllegalArgumentException("Mybatis分页插件dialect参数值错误，可选值为[" + dialects + "]");
        }
    }

    /**
     * 设置分页参数
     *
     * @param parameterObject
     * @param page
     * @return
     */
    public Map setPageParameter(MappedStatement ms, Object parameterObject, Page page) {
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        return parser.setPageParameter(ms, parameterObject, boundSql, page);
    }

    /**
     * 处理count查询的MappedStatement
     *
     * @param ms
     * @param sqlSource
     * @param args
     */
    public void processCountMappedStatement(MappedStatement ms, SqlSource sqlSource, Object[] args) {
        args[0] = getMappedStatement(ms, sqlSource, args[1], SUFFIX_COUNT);
    }

    /**
     * 处理分页查询的MappedStatement
     *
     * @param ms
     * @param sqlSource
     * @param page
     * @param args
     */
    public void processPageMappedStatement(MappedStatement ms, SqlSource sqlSource, Page page, Object[] args) {
        args[0] = getMappedStatement(ms, sqlSource, args[1], SUFFIX_PAGE);
        //处理入参
        args[1] = setPageParameter((MappedStatement) args[0], args[1], page);
    }

    /**
     * 处理SQL
     */
    public static interface Parser {

        /**
         * 是否支持MappedStatement全局缓存
         *
         * @return
         */
        boolean isSupportedMappedStatementCache();

        /**
         * 获取总数sql - 如果要支持其他数据库，修改这里就可以
         *
         * @param sql 原查询sql
         * @return 返回count查询sql
         */
        String getCountSql(String sql);

        /**
         * 获取分页sql - 如果要支持其他数据库，修改这里就可以
         *
         * @param sql 原查询sql
         * @return 返回分页sql
         */
        String getPageSql(String sql);

        /**
         * 获取分页参数映射
         *
         * @param configuration
         * @param boundSql
         * @return
         */
        List<ParameterMapping> getPageParameterMapping(Configuration configuration, BoundSql boundSql);

        /**
         * 设置分页参数
         *
         * @param ms
         * @param parameterObject
         * @param boundSql
         * @param page
         * @return
         */
        Map setPageParameter(MappedStatement ms, Object parameterObject, BoundSql boundSql, Page page);
    }

    /**
     * 基础的Parser抽象类
     */
    public static abstract class SimpleParser implements Parser {
        public static Parser newParser(Dialect dialect) {
            Parser parser = null;
            switch (dialect) {
                case mysql:
                case mariadb:
                case sqlite:
                    parser = new MysqlParser();
                    break;
                case oracle:
                    parser = new OracleParser();
                    break;
                case hsqldb:
                    parser = new HsqldbParser();
                    break;
                case postgresql:
                default:
                    parser = new PostgreSQLParser();
            }
            return parser;
        }

        @Override
        public boolean isSupportedMappedStatementCache() {
            return true;
        }

        public String getCountSql(final String sql) {
            return sqlParser.getSmartCountSql(sql);
        }

        public abstract String getPageSql(String sql);

        public List<ParameterMapping> getPageParameterMapping(Configuration configuration, BoundSql boundSql) {
            List<ParameterMapping> newParameterMappings = new ArrayList<ParameterMapping>();
            newParameterMappings.addAll(boundSql.getParameterMappings());
            newParameterMappings.add(new ParameterMapping.Builder(configuration, PAGEPARAMETER_FIRST, Integer.class).build());
            newParameterMappings.add(new ParameterMapping.Builder(configuration, PAGEPARAMETER_SECOND, Integer.class).build());
            return newParameterMappings;
        }

        public Map setPageParameter(MappedStatement ms, Object parameterObject, BoundSql boundSql, Page page) {
            Map paramMap = null;
            if (parameterObject == null) {
                paramMap = new HashMap();
            } else if (parameterObject instanceof Map) {
                paramMap = (Map) parameterObject;
            } else {
                paramMap = new HashMap();
                //动态sql时的判断条件不会出现在ParameterMapping中，但是必须有，所以这里需要收集所有的getter属性
                //TypeHandlerRegistry可以直接处理的会作为一个直接使用的对象进行处理
                boolean hasTypeHandler = ms.getConfiguration().getTypeHandlerRegistry().hasTypeHandler(parameterObject.getClass());
                MetaObject metaObject = forObject(parameterObject);
                //需要针对注解形式的MyProviderSqlSource保存原值
                if (ms.getSqlSource() instanceof MyProviderSqlSource) {
                    paramMap.put(PROVIDER_OBJECT, parameterObject);
                }
                if (!hasTypeHandler) {
                    for (String name : metaObject.getGetterNames()) {
                        paramMap.put(name, metaObject.getValue(name));
                    }
                }
                //下面这段方法，主要解决一个常见类型的参数时的问题
                if (boundSql.getParameterMappings() != null && boundSql.getParameterMappings().size() > 0) {
                    for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                        String name = parameterMapping.getProperty();
                        if (!name.equals(PAGEPARAMETER_FIRST)
                                && !name.equals(PAGEPARAMETER_SECOND)
                                && paramMap.get(name) == null) {
                            if (hasTypeHandler
                                    || parameterMapping.getJavaType().equals(parameterObject.getClass())) {
                                paramMap.put(name, parameterObject);
                                break;
                            }
                        }
                    }
                }
            }
            //备份原始参数对象
            paramMap.put(ORIGINAL_PARAMETER_OBJECT, parameterObject);
            return paramMap;
        }
    }

    //Mysql
    private static class MysqlParser extends SimpleParser {
        @Override
        public String getPageSql(String sql) {
            StringBuilder sqlBuilder = new StringBuilder(sql.length() + 14);
            sqlBuilder.append(sql);
            sqlBuilder.append(" limit ?,?");
            return sqlBuilder.toString();
        }

        @Override
        public Map setPageParameter(MappedStatement ms, Object parameterObject, BoundSql boundSql, Page page) {
            Map paramMap = super.setPageParameter(ms, parameterObject, boundSql, page);
            paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow());
            paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());
            return paramMap;
        }
    }

    //Oracle
    private static class OracleParser extends SimpleParser {
        @Override
        public String getPageSql(String sql) {
            StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
            sqlBuilder.append("select * from ( select tmp_page.*, rownum row_id from ( ");
            sqlBuilder.append(sql);
            sqlBuilder.append(" ) tmp_page where rownum <= ? ) where row_id > ?");
            return sqlBuilder.toString();
        }

        @Override
        public Map setPageParameter(MappedStatement ms, Object parameterObject, BoundSql boundSql, Page page) {
            Map paramMap = super.setPageParameter(ms, parameterObject, boundSql, page);
            paramMap.put(PAGEPARAMETER_FIRST, page.getEndRow());
            paramMap.put(PAGEPARAMETER_SECOND, page.getStartRow());
            return paramMap;
        }
    }

    //Hsqldb
    private static class HsqldbParser extends SimpleParser {
        @Override
        public String getPageSql(String sql) {
            StringBuilder sqlBuilder = new StringBuilder(sql.length() + 20);
            sqlBuilder.append(sql);
            sqlBuilder.append(" limit ? offset ?");
            return sqlBuilder.toString();
        }

        @Override
        public Map setPageParameter(MappedStatement ms, Object parameterObject, BoundSql boundSql, Page page) {
            Map paramMap = super.setPageParameter(ms, parameterObject, boundSql, page);
            paramMap.put(PAGEPARAMETER_FIRST, page.getPageSize());
            paramMap.put(PAGEPARAMETER_SECOND, page.getStartRow());
            return paramMap;
        }
    }

    //PostgreSQL
    private static class PostgreSQLParser extends SimpleParser {
        @Override
        public String getPageSql(String sql) {
            StringBuilder sqlBuilder = new StringBuilder(sql.length() + 14);
            sqlBuilder.append(sql);
            sqlBuilder.append(" limit ? offset ?");
            return sqlBuilder.toString();
        }

        @Override
        public Map setPageParameter(MappedStatement ms, Object parameterObject, BoundSql boundSql, Page page) {
            Map paramMap = super.setPageParameter(ms, parameterObject, boundSql, page);
            paramMap.put(PAGEPARAMETER_FIRST, page.getPageSize());
            paramMap.put(PAGEPARAMETER_SECOND, page.getStartRow());
            return paramMap;
        }
    }

    /**
     * 自定义动态SqlSource
     */
    private class MyDynamicSqlSource implements SqlSource {
        private Configuration configuration;
        private SqlNode rootSqlNode;
        /**
         * 用于区分动态的count查询或分页查询
         */
        private Boolean count;

        public MyDynamicSqlSource(Configuration configuration, SqlNode rootSqlNode, Boolean count) {
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
                sqlSource = getCountSqlSource(configuration, sqlSource, parameterObject);
            } else {
                sqlSource = getPageSqlSource(configuration, sqlSource, parameterObject);
            }
            BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
            //设置条件参数
            for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
                boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
            }
            return boundSql;
        }
    }

    /**
     * 自定义ProviderSqlSource，代理方法
     */
    private class MyProviderSqlSource implements SqlSource {
        private Configuration configuration;
        private ProviderSqlSource providerSqlSource;

        /**
         * 用于区分动态的count查询或分页查询
         */
        private Boolean count;

        private MyProviderSqlSource(Configuration configuration, ProviderSqlSource providerSqlSource, Boolean count) {
            this.configuration = configuration;
            this.providerSqlSource = providerSqlSource;
            this.count = count;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            BoundSql boundSql = null;
            if (parameterObject instanceof Map && ((Map) parameterObject).containsKey(PROVIDER_OBJECT)) {
                boundSql = providerSqlSource.getBoundSql(((Map) parameterObject).get(PROVIDER_OBJECT));
            } else {
                boundSql = providerSqlSource.getBoundSql(parameterObject);
            }
            if (count) {
                return new BoundSql(
                        configuration,
                        parser.getCountSql(boundSql.getSql()),
                        boundSql.getParameterMappings(),
                        parameterObject);
            } else {
                return new BoundSql(
                        configuration,
                        parser.getPageSql(boundSql.getSql()),
                        parser.getPageParameterMapping(configuration, boundSql),
                        parameterObject);
            }
        }
    }


    /**
     * 获取ms - 在这里对新建的ms做了缓存，第一次新增，后面都会使用缓存值
     *
     * @param ms
     * @param sqlSource
     * @param suffix
     * @return
     */
    private MappedStatement getMappedStatement(MappedStatement ms, SqlSource sqlSource, Object parameterObject, String suffix) {
        MappedStatement qs = null;
        if (parser.isSupportedMappedStatementCache()) {
            try {
                qs = ms.getConfiguration().getMappedStatement(ms.getId() + suffix);
            } catch (Exception e) {
                //ignore
            }
        }
        if (qs == null) {
            //创建一个新的MappedStatement
            qs = newMappedStatement(ms, getsqlSource(ms, sqlSource, parameterObject, suffix), suffix);
            if (parser.isSupportedMappedStatementCache()) {
                try {
                    ms.getConfiguration().addMappedStatement(qs);
                } catch (Exception e) {
                    //ignore
                }
            }
        }
        return qs;
    }

    /**
     * 新建count查询和分页查询的MappedStatement
     *
     * @param ms
     * @param sqlSource
     * @param suffix
     * @return
     */
    private MappedStatement newMappedStatement(MappedStatement ms, SqlSource sqlSource, String suffix) {
        String id = ms.getId() + suffix;
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), id, sqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        if (suffix == SUFFIX_PAGE) {
            builder.resultMaps(ms.getResultMaps());
        } else {
            //count查询返回值int
            List<ResultMap> resultMaps = new ArrayList<ResultMap>();
            ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), id, int.class, EMPTY_RESULTMAPPING).build();
            resultMaps.add(resultMap);
            builder.resultMaps(resultMaps);
        }
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    /**
     * 判断当前执行的是否为动态sql
     *
     * @param ms
     * @return
     */
    public boolean isDynamic(MappedStatement ms) {
        return ms.getSqlSource() instanceof DynamicSqlSource;
    }

    /**
     * 获取新的sqlSource
     *
     * @param ms
     * @param sqlSource
     * @param parameterObject
     * @param suffix
     * @return
     */
    private SqlSource getsqlSource(MappedStatement ms, SqlSource sqlSource, Object parameterObject, String suffix) {
        //1. 从XMLLanguageDriver.java和XMLScriptBuilder.java可以看出只有两种SqlSource
        //2. 增加注解情况的ProviderSqlSource
        //3. 对于RawSqlSource需要进一步测试完善
        //如果是动态sql
        if (isDynamic(ms)) {
            MetaObject msObject = forObject(ms);
            SqlNode sqlNode = (SqlNode) msObject.getValue("sqlSource.rootSqlNode");
            MixedSqlNode mixedSqlNode = null;
            if (sqlNode instanceof MixedSqlNode) {
                mixedSqlNode = (MixedSqlNode) sqlNode;
            } else {
                List<SqlNode> contents = new ArrayList<SqlNode>(1);
                contents.add(sqlNode);
                mixedSqlNode = new MixedSqlNode(contents);
            }
            return new MyDynamicSqlSource(ms.getConfiguration(), mixedSqlNode, suffix == SUFFIX_COUNT);
        } else if (sqlSource instanceof ProviderSqlSource) {
            return new MyProviderSqlSource(ms.getConfiguration(), (ProviderSqlSource) sqlSource, suffix == SUFFIX_COUNT);
        }
        //如果是静态分页sql
        else if (suffix == SUFFIX_PAGE) {
            //改为分页sql
            return getPageSqlSource(ms.getConfiguration(), sqlSource, parameterObject);
        }
        //如果是静态count-sql
        else {
            return getCountSqlSource(ms.getConfiguration(), sqlSource, parameterObject);
        }
    }

    /**
     * 获取分页的sqlSource
     *
     * @param configuration
     * @param sqlSource
     * @return
     */
    private SqlSource getPageSqlSource(Configuration configuration, SqlSource sqlSource, Object parameterObject) {
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        return new StaticSqlSource(configuration, parser.getPageSql(boundSql.getSql()), parser.getPageParameterMapping(configuration, boundSql));
    }

    /**
     * 获取count的sqlSource
     *
     * @param sqlSource
     * @return
     */
    private SqlSource getCountSqlSource(Configuration configuration, SqlSource sqlSource, Object parameterObject) {
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        return new StaticSqlSource(configuration, parser.getCountSql(boundSql.getSql()), boundSql.getParameterMappings());
    }

    /**
     * 测试[控制台输出]count和分页sql
     *
     * @param dialet      数据库类型
     * @param originalSql 原sql
     */
    public static void testSql(String dialet, String originalSql) {
        SqlUtil sqlUtil = new SqlUtil(dialet);
        String countSql = sqlUtil.parser.getCountSql(originalSql);
        System.out.println(countSql);
        String pageSql = sqlUtil.parser.getPageSql(originalSql);
        System.out.println(pageSql);
    }
}