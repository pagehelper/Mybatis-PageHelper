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

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.FromBaseTable;
import com.foundationdb.sql.parser.OrderByList;
import com.foundationdb.sql.parser.SQLParser;
import com.foundationdb.sql.parser.StatementNode;
import com.foundationdb.sql.unparser.NodeToString;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.*;

/**
 * Mybatis - 通用分页拦截器
 *
 * @author liuzh/abel533/isea533
 * @version 3.3.0
 *          项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 */
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class PageHelper implements Interceptor {
    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

    /**
     * 反射对象，增加对低版本Mybatis的支持
     *
     * @param object 反射对象
     * @return
     */
    public static MetaObject forObject(Object object) {
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
    }

    /**
     * 解析 - 去掉order by 语句
     */
    private static class UnParser extends NodeToString {
        private static final SQLParser PARSER = new SQLParser();

        public String removeOrderBy(String sql) throws StandardException {
            StatementNode stmt = PARSER.parseStatement(sql);
            String result = toString(stmt);
            if (result.indexOf('$') > -1) {
                result = result.replaceAll("\\$\\d+", "?");
            }
            return result;
        }

        @Override
        protected String orderByList(OrderByList node) throws StandardException {
            //order by中如果包含参数就原样返回
            // 这里建议order by使用${param}这样的参数
            // 这种形式的order by可以正确的被过滤掉，并且支持大部分的数据库
            String sql = nodeList(node);
            if (sql.indexOf('$') > -1) {
                return sql;
            }
            return "";
        }

        @Override
        protected String fromBaseTable(FromBaseTable node) throws StandardException {
            String tn = toString(node.getOrigTableName());
            String n = node.getCorrelationName();
            if (n == null) {
                return tn;
            } else if (dialect.equals("oracle")) {
                //Oracle表不支持AS
                return tn + " " + n;
            } else {
                return tn + " AS " + n;
            }
        }
    }

    //SQL反解析
    private static final UnParser UNPARSER = new UnParser();

    //分页的id后缀
    private static final String SUFFIX_PAGE = "_PageHelper";
    //count查询的id后缀
    private static final String SUFFIX_COUNT = SUFFIX_PAGE + "_Count";
    //第一个分页参数
    private static final String PAGEPARAMETER_FIRST = "First" + SUFFIX_PAGE;
    //第二个分页参数
    private static final String PAGEPARAMETER_SECOND = "Second" + SUFFIX_PAGE;

    private static final String BOUND_SQL = "boundSql.sql";
    private static final String SQL_NODES = "sqlSource.rootSqlNode.contents";

    private static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<Page>();

    private static final List<ResultMapping> EMPTY_RESULTMAPPING = new ArrayList<ResultMapping>(0);

    //数据库方言
    private static String dialect = "";
    //RowBounds参数offset作为PageNum使用 - 默认不使用
    private static boolean offsetAsPageNum = false;
    //RowBounds是否进行count查询 - 默认不查询
    private static boolean rowBoundsWithCount = false;
    //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页
    private static boolean pageSizeZero = false;

    /**
     * 开始分页
     *
     * @param pageNum  页码
     * @param pageSize 每页显示数量
     */
    public static void startPage(int pageNum, int pageSize) {
        startPage(pageNum, pageSize, true);
    }

    /**
     * 开始分页
     *
     * @param pageNum  页码
     * @param pageSize 每页显示数量
     * @param count    是否进行count查询
     */
    public static void startPage(int pageNum, int pageSize, boolean count) {
        LOCAL_PAGE.set(new Page(pageNum, pageSize, count));
    }

    /**
     * 获取分页参数
     *
     * @param rowBounds RowBounds参数
     * @return 返回Page对象
     */
    private Page getPage(RowBounds rowBounds) {
        Page page = LOCAL_PAGE.get();
        //移除本地变量
        LOCAL_PAGE.remove();

        if (page == null) {
            if (offsetAsPageNum) {
                page = new Page(rowBounds.getOffset(), rowBounds.getLimit(), rowBoundsWithCount);
            } else {
                page = new Page(rowBounds, rowBoundsWithCount);
            }
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
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        final Object[] args = invocation.getArgs();
        RowBounds rowBounds = (RowBounds) args[2];
        if (LOCAL_PAGE.get() == null && rowBounds == RowBounds.DEFAULT) {
            return invocation.proceed();
        } else {
            //忽略RowBounds-否则会进行Mybatis自带的内存分页
            args[2] = RowBounds.DEFAULT;
            MappedStatement ms = (MappedStatement) args[0];
            Object parameterObject = args[1];
            //分页信息
            Page page = getPage(rowBounds);
            //pageSizeZero的判断
            if (pageSizeZero && page.getPageSize() == 0) {
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
                BoundSql boundSql = ms.getBoundSql(parameterObject);
                //将参数中的MappedStatement替换为新的qs
                args[0] = getMappedStatement(ms, boundSql, SUFFIX_COUNT);
                //查询总数
                Object result = invocation.proceed();
                //设置总数
                page.setTotal((Integer) ((List) result).get(0));
                if (page.getTotal() == 0) {
                    return page;
                }
            }
            //pageSize>0的时候执行分页查询，pageSize<=0的时候不执行相当于可能只返回了一个count
            if (page.getPageSize() > 0) {
                BoundSql boundSql = ms.getBoundSql(parameterObject);
                //将参数中的MappedStatement替换为新的qs
                args[0] = getMappedStatement(ms, boundSql, SUFFIX_PAGE);
                //判断parameterObject，然后赋值
                args[1] = setPageParameter(parameterObject, boundSql, page);
                //执行分页查询
                Object result = invocation.proceed();
                //得到处理结果
                page.addAll((List) result);
            }
            //返回结果
            return page;
        }
    }

    /**
     * 获取总数sql - 如果要支持其他数据库，修改这里就可以
     *
     * @param sql 原查询sql
     * @return 返回count查询sql
     */
    private String getCountSql(final String sql) {
        try {
            if (sql.toUpperCase().contains("ORDER")) {
                return getCountSqlBefore() + UNPARSER.removeOrderBy(sql) + getCountSqlAfter();
            }
        } catch (Exception e) {
            //ignore
        }
        return getCountSqlBefore() + sql + getCountSqlAfter();
    }

    /**
     * 获取count前置sql
     * @return
     */
    private String getCountSqlBefore(){
        return "select count(0) from (";
    }

    /**
     *
     * @return
     */
    private String getCountSqlAfter(){
        return ") tmp_count";
    }

    /**
     * 获取分页sql - 如果要支持其他数据库，修改这里就可以
     *
     * @param sql 原查询sql
     * @return 返回分页sql
     */
    private String getPageSql(String sql) {
        return getPageSqlBefore() + sql + getPageSqlAfter();
    }

    /**
     * 获取分页前置sql
     *
     * @return
     */
    private String getPageSqlBefore(){
        if ("mysql".equals(dialect)) {
            return "select * from (";
        } else if ("oracle".equals(dialect)) {
            return "select * from ( select temp.*, rownum row_id from ( ";
        } else/* if ("hsqldb".equals(dialect)) */{
            return "";
        }
    }

    /**
     * 获取分页后置sql
     *
     * @return
     */
    private String getPageSqlAfter(){
        if ("mysql".equals(dialect)) {
            return ") as tmp_page limit ?,?";
        } else if ("oracle".equals(dialect)) {
            return " ) temp where rownum <= ? ) where row_id > ?";
        } else/* if ("hsqldb".equals(dialect)) */{
            return " LIMIT ? OFFSET ?";
        }
    }

    /**
     * 处理参数对象，添加分页参数值
     *
     * @param parameterObject 参数对象
     * @param page            分页信息
     * @return 返回带有分页信息的参数对象
     */
    private Map setPageParameter(Object parameterObject, BoundSql boundSql, Page page) {
        Map paramMap = null;
        if (parameterObject == null) {
            paramMap = new HashMap();
        } else if (parameterObject instanceof Map) {
            paramMap = (Map) parameterObject;
        } else {
            paramMap = new MapperMethod.ParamMap<Object>();
            if (boundSql.getParameterMappings() != null && boundSql.getParameterMappings().size() > 0) {
                for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                    if (!parameterMapping.getProperty().equals(PAGEPARAMETER_FIRST)
                            && !parameterMapping.getProperty().equals(PAGEPARAMETER_SECOND)) {
                        paramMap.put(parameterMapping.getProperty(), parameterObject);
                    }
                }
            }
        }
        if ("mysql".equals(dialect)) {
            paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow());
            paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());
        } else if ("hsqldb".equals(dialect)) {
            paramMap.put(PAGEPARAMETER_FIRST, page.getPageSize());
            paramMap.put(PAGEPARAMETER_SECOND, page.getStartRow());
        } else if ("oracle".equals(dialect)) {
            paramMap.put(PAGEPARAMETER_FIRST, page.getEndRow());
            paramMap.put(PAGEPARAMETER_SECOND, page.getStartRow());
        }
        return paramMap;
    }

    /**
     * 获取ms - 在这里对新建的ms做了缓存，第一次新增，后面都会使用缓存值
     *
     * @param ms
     * @param boundSql
     * @param suffix
     * @return
     */
    private MappedStatement getMappedStatement(MappedStatement ms, BoundSql boundSql, String suffix) {
        MappedStatement qs = null;
        try {
            qs = ms.getConfiguration().getMappedStatement(ms.getId() + suffix);
        } catch (Exception e) {
            //ignore
        }
        if (qs == null) {
            //创建一个新的MappedStatement
            qs = newMappedStatement(ms, getNewSqlSource(ms,new BoundSqlSqlSource(boundSql),suffix), suffix);
            try {
                ms.getConfiguration().addMappedStatement(qs);
            } catch (Exception e) {
                //ignore
            }
        }
        return qs;
    }

    /**
     * 自定义简单SqlSource
     */
    private class BoundSqlSqlSource implements SqlSource {
        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }

        public BoundSql getBoundSql() {
            return boundSql;
        }
    }

    /**
     * 自定义动态SqlSource
     */
    public class MyDynamicSqlSource implements SqlSource {
        private Configuration configuration;
        private SqlNode rootSqlNode;

        public MyDynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
            this.configuration = configuration;
            this.rootSqlNode = rootSqlNode;
        }

        public BoundSql getBoundSql(Object parameterObject) {
            DynamicContext context = new DynamicContext(configuration, parameterObject);
            rootSqlNode.apply(context);
            SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
            Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
            SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
            BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
            for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
                boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
            }
            //添加参数映射
            MetaObject boundSqlObject = forObject(boundSql);
            List<ParameterMapping> newParameterMappings = new ArrayList<ParameterMapping>();
            newParameterMappings.addAll(boundSql.getParameterMappings());
            newParameterMappings.add(new ParameterMapping.Builder(configuration, PAGEPARAMETER_FIRST, Integer.class).build());
            newParameterMappings.add(new ParameterMapping.Builder(configuration, PAGEPARAMETER_SECOND, Integer.class).build());
            boundSqlObject.setValue("parameterMappings", newParameterMappings);

            return boundSql;
        }
    }

    /**
     * 新建count查询和分页查询的MappedStatement
     *
     * @param ms
     * @param newSqlSource
     * @param suffix
     * @return
     */
    private MappedStatement newMappedStatement(MappedStatement ms, SqlSource newSqlSource, String suffix) {
        String id = ms.getId() + suffix;
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), id, newSqlSource, ms.getSqlCommandType());
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
     * 获取新的sqlSource
     *
     * @param ms
     * @param newSqlSource
     * @param suffix
     * @return
     */
    private SqlSource getNewSqlSource(MappedStatement ms, BoundSqlSqlSource newSqlSource, String suffix){
        SqlSource sqlSource = ms.getSqlSource();
        //从XMLLanguageDriver.java和XMLScriptBuilder.java可以看出只有两种SqlSource
        if (sqlSource instanceof DynamicSqlSource) {
            MetaObject msObject = forObject(ms);
            List<SqlNode> contents = (List<SqlNode>) msObject.getValue(SQL_NODES);
            List<SqlNode> newSqlNodes = new ArrayList<SqlNode>(contents.size() + 2);
            //这里用的等号
            if (suffix == SUFFIX_PAGE) {
                newSqlNodes.add(new TextSqlNode(getPageSqlBefore()));
                newSqlNodes.addAll(contents);
                newSqlNodes.add(new TextSqlNode(getPageSqlAfter()));
                return new MyDynamicSqlSource(ms.getConfiguration(),new MixedSqlNode(newSqlNodes));
            } else {
                newSqlNodes.add(new TextSqlNode(getCountSqlBefore()));
                newSqlNodes.addAll(contents);
                newSqlNodes.add(new TextSqlNode(getCountSqlAfter()));
                return new DynamicSqlSource(ms.getConfiguration(),new MixedSqlNode(newSqlNodes));
            }
        } else {
            //RawSqlSource
            //这里用的等号
            if (suffix == SUFFIX_PAGE) {
                //改为分页sql
                MetaObject sqlObject = forObject(newSqlSource);
                sqlObject.setValue(BOUND_SQL, getPageSql((String) sqlObject.getValue(BOUND_SQL)));
                //添加参数映射
                List<ParameterMapping> newParameterMappings = new ArrayList<ParameterMapping>();
                newParameterMappings.addAll(newSqlSource.getBoundSql().getParameterMappings());
                newParameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, Integer.class).build());
                newParameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, Integer.class).build());
                sqlObject.setValue("boundSql.parameterMappings", newParameterMappings);
            } else {
                //改为count sql
                MetaObject sqlObject = forObject(newSqlSource);
                sqlObject.setValue(BOUND_SQL, getCountSql((String) sqlObject.getValue(BOUND_SQL)));
            }
            return newSqlSource;
        }
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

    /**
     * 设置属性值
     *
     * @param p 属性值
     */
    public void setProperties(Properties p) {
        dialect = p.getProperty("dialect");
        if (dialect == null || "".equals(dialect)) {
            throw new RuntimeException("Mybatis分页插件PageHelper无法获取dialect参数!");
        }
        //offset作为PageNum使用
        String offset = p.getProperty("offsetAsPageNum");
        if (offset != null && "TRUE".equalsIgnoreCase(offset)) {
            offsetAsPageNum = true;
        }
        //RowBounds方式是否做count查询
        String withcount = p.getProperty("rowBoundsWithCount");
        if (withcount != null && "TRUE".equalsIgnoreCase(withcount)) {
            rowBoundsWithCount = true;
        }
        //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页
        String sizeZero = p.getProperty("pageSizeZero");
        if (sizeZero != null && "TRUE".equalsIgnoreCase(sizeZero)) {
            pageSizeZero = true;
        }
    }
}
