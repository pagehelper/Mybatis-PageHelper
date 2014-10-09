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

import com.foundationdb.sql.StandardException;
import com.foundationdb.sql.parser.*;
import com.foundationdb.sql.unparser.NodeToString;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mybatis - sql工具，获取分页和count的MappedStatement，设置分页参数
 *
 * @author liuzh/abel533/isea533
 * @since 3.3.0
 *          项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 */
public class SqlUtil {
    private static final String BOUND_SQL = "boundSql.sql";
    private static final String SQL_NODES = "sqlSource.rootSqlNode.contents";
    private static final List<ResultMapping> EMPTY_RESULTMAPPING = new ArrayList<ResultMapping>(0);

    //分页的id后缀
    private static final String SUFFIX_PAGE = "_PageHelper";
    //count查询的id后缀
    private static final String SUFFIX_COUNT = SUFFIX_PAGE + "_Count";
    //第一个分页参数
    private static final String PAGEPARAMETER_FIRST = "First" + SUFFIX_PAGE;
    //第二个分页参数
    private static final String PAGEPARAMETER_SECOND = "Second" + SUFFIX_PAGE;

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

    private static SqlUtil.Parser SQLPARSER;

    //数据库方言 - 使用枚举限制数据库类型
    private enum Dialect {
        mysql, oracle, hsqldb
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
            Dialect dialect = Dialect.valueOf(strDialect);
            SQLPARSER = newOrderByParser(dialect);
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
     * @param boundSql
     * @param page
     * @return
     */
    public Map setPageParameter(Object parameterObject, BoundSql boundSql, Page page) {
        return SQLPARSER.setPageParameter(parameterObject, boundSql, page);
    }

    /**
     * 获取count查询的MappedStatement
     *
     * @param ms
     * @param boundSql
     * @return
     */
    public MappedStatement getCountMappedStatement(MappedStatement ms, BoundSql boundSql){
        return getMappedStatement(ms, boundSql, SUFFIX_COUNT);
    }

    /**
     * 获取分页查询的MappedStatement
     *
     * @param ms
     * @param boundSql
     * @return
     */
    public MappedStatement getPageMappedStatement(MappedStatement ms, BoundSql boundSql){
        return getMappedStatement(ms, boundSql, SUFFIX_PAGE);
    }

    private Parser newOrderByParser(Dialect dialect) {
        try {
            Class.forName("com.foundationdb.sql.unparser.NodeToString");
            return new UnParser(dialect);
        } catch (Exception e) {
            return new SimpleParser(dialect);
        }
    }

    /**
     * 处理SQL
     */
    private interface Parser {
        String removeOrderBy(String sql);

        String getCountSql(String sql);

        String getCountSqlBefore();

        String getCountSqlAfter();

        String getPageSql(String sql);

        String getPageSqlBefore();

        String getPageSqlAfter();

        Map setPageParameter(Object parameterObject, BoundSql boundSql, Page page);
    }

    private static class SimpleParser implements Parser {
        private Dialect dialect;

        private SimpleParser(Dialect dialect) {
            this.dialect = dialect;
        }

        @Override
        public String removeOrderBy(String sql) {
            return sql;
        }

        /**
         * 获取总数sql - 如果要支持其他数据库，修改这里就可以
         *
         * @param sql 原查询sql
         * @return 返回count查询sql
         */
        public String getCountSql(final String sql) {
            if (sql.toUpperCase().contains("ORDER")) {
                return getCountSqlBefore() + removeOrderBy(sql) + getCountSqlAfter();
            }
            return getCountSqlBefore() + sql + getCountSqlAfter();
        }

        /**
         * 获取count前置sql
         *
         * @return
         */
        public String getCountSqlBefore() {
            return "select count(0) from (";
        }

        /**
         * 获取count后置sql
         *
         * @return
         */
        public String getCountSqlAfter() {
            return ") tmp_count";
        }

        /**
         * 获取分页sql - 如果要支持其他数据库，修改这里就可以
         *
         * @param sql 原查询sql
         * @return 返回分页sql
         */
        public String getPageSql(String sql) {
            return getPageSqlBefore() + sql + getPageSqlAfter();
        }

        /**
         * 获取分页前置sql
         *
         * @return
         */
        public String getPageSqlBefore() {
            switch (dialect) {
                case mysql:
                    return "select * from (";
                case oracle:
                    return "select * from ( select temp.*, rownum row_id from ( ";
                case hsqldb:
                default:
                    return "";
            }
        }

        /**
         * 获取分页后置sql
         *
         * @return
         */
        public String getPageSqlAfter() {
            switch (dialect) {
                case mysql:
                    return ") as tmp_page limit ?,?";
                case oracle:
                    return " ) temp where rownum <= ? ) where row_id > ?";
                case hsqldb:
                default:
                    return " LIMIT ? OFFSET ?";
            }
        }

        @Override
        public Map setPageParameter(Object parameterObject, BoundSql boundSql, Page page) {
            Map paramMap = null;
            if (parameterObject == null) {
                paramMap = new HashMap();
            } else if (parameterObject instanceof Map) {
                paramMap = (Map) parameterObject;
            } else {
                paramMap = new HashMap();
                if (boundSql.getParameterMappings() != null && boundSql.getParameterMappings().size() > 0) {
                    for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                        if (!parameterMapping.getProperty().equals(PAGEPARAMETER_FIRST)
                                && !parameterMapping.getProperty().equals(PAGEPARAMETER_SECOND)) {
                            paramMap.put(parameterMapping.getProperty(), parameterObject);
                        }
                    }
                }
            }
            switch (dialect) {
                case mysql:
                    paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow());
                    paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());
                    break;
                case oracle:
                    paramMap.put(PAGEPARAMETER_FIRST, page.getEndRow());
                    paramMap.put(PAGEPARAMETER_SECOND, page.getStartRow());
                    break;
                case hsqldb:
                default:
                    paramMap.put(PAGEPARAMETER_FIRST, page.getPageSize());
                    paramMap.put(PAGEPARAMETER_SECOND, page.getStartRow());
            }
            return paramMap;
        }
    }

    /**
     * 解析 - 去掉order by 语句
     */
    private static class UnParser extends NodeToString implements Parser {
        private SQLParser PARSER = new SQLParser();
        private Parser simple;
        private Dialect dialect;
        private Map<String, String> CACHE = new HashMap<String, String>();

        private UnParser(Dialect dialect) {
            this.dialect = dialect;
            this.simple = new SimpleParser(dialect);
        }

        @Override
        public String getCountSql(String sql) {
            if (sql.toUpperCase().contains("ORDER")) {
                return getCountSqlBefore() + removeOrderBy(sql) + getCountSqlAfter();
            }
            return getCountSqlBefore() + sql + getCountSqlAfter();
        }

        @Override
        public String getCountSqlBefore() {
            return simple.getCountSqlBefore();
        }

        @Override
        public String getCountSqlAfter() {
            return simple.getCountSqlAfter();
        }

        @Override
        public String getPageSql(String sql) {
            return simple.getPageSql(sql);
        }

        @Override
        public String getPageSqlBefore() {
            return simple.getPageSqlBefore();
        }

        @Override
        public String getPageSqlAfter() {
            return simple.getPageSqlAfter();
        }

        @Override
        public Map setPageParameter(Object parameterObject, BoundSql boundSql, Page page) {
            return simple.setPageParameter(parameterObject, boundSql, page);
        }

        @Override
        public String removeOrderBy(String sql) {
            try {
                if (CACHE.get(sql) != null) {
                    return CACHE.get(sql);
                }
                StatementNode stmt = PARSER.parseStatement(sql);
                String result = toString(stmt);
                if (result.indexOf('$') > -1) {
                    result = result.replaceAll("\\$\\d+", "?");
                }
                CACHE.put(sql, result);
                return result;
            } catch (Exception e) {
                return sql;
            }
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
        protected String fromSubquery(FromSubquery node) throws StandardException {
            StringBuilder str = new StringBuilder(toString(node.getSubquery()));
            if (node.getOrderByList() != null) {
                str.append(' ');
                str.append(toString(node.getOrderByList()));
            }
            str.insert(0, '(');
            str.append(") ");
            if (dialect != Dialect.oracle) {
                //Oracle表不支持AS
                str.append("AS ");
            }
            str.append(node.getCorrelationName());
            if (node.getResultColumns() != null) {
                str.append('(');
                str.append(toString(node.getResultColumns()));
                str.append(')');
            }
            return str.toString();
        }

        @Override
        protected String fromBaseTable(FromBaseTable node) throws StandardException {
            String tn = toString(node.getOrigTableName());
            String n = node.getCorrelationName();
            if (n == null) {
                return tn;
            } else if (dialect == Dialect.oracle) {
                //Oracle表不支持AS
                return tn + " " + n;
            } else {
                return tn + " AS " + n;
            }
        }
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
    private class MyDynamicSqlSource implements SqlSource {
        private Configuration configuration;
        private SqlNode rootSqlNode;
        /**用于区分动态的count查询或分页查询*/
        private Boolean count;

        public MyDynamicSqlSource(Configuration configuration, SqlNode rootSqlNode,Boolean count) {
            this.configuration = configuration;
            this.rootSqlNode = rootSqlNode;
            this.count = count;
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
            if (count) {
                MetaObject boundSqlObject = forObject(boundSql);
                boundSqlObject.setValue("sql", SQLPARSER.removeOrderBy(boundSql.getSql()));
            } else {
                //添加参数映射
                MetaObject boundSqlObject = forObject(boundSql);
                List<ParameterMapping> newParameterMappings = new ArrayList<ParameterMapping>();
                newParameterMappings.addAll(boundSql.getParameterMappings());
                newParameterMappings.add(new ParameterMapping.Builder(configuration, PAGEPARAMETER_FIRST, Integer.class).build());
                newParameterMappings.add(new ParameterMapping.Builder(configuration, PAGEPARAMETER_SECOND, Integer.class).build());
                boundSqlObject.setValue("parameterMappings", newParameterMappings);
            }
            return boundSql;
        }
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
            qs = newMappedStatement(ms, getNewSqlSource(ms, new BoundSqlSqlSource(boundSql), suffix), suffix);
            try {
                ms.getConfiguration().addMappedStatement(qs);
            } catch (Exception e) {
                //ignore
            }
        }
        return qs;
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
    private SqlSource getNewSqlSource(MappedStatement ms, BoundSqlSqlSource newSqlSource, String suffix) {
        SqlSource sqlSource = ms.getSqlSource();
        //从XMLLanguageDriver.java和XMLScriptBuilder.java可以看出只有两种SqlSource
        if (sqlSource instanceof DynamicSqlSource) {
            MetaObject msObject = forObject(ms);
            List<SqlNode> contents = (List<SqlNode>) msObject.getValue(SQL_NODES);
            List<SqlNode> newSqlNodes = new ArrayList<SqlNode>(contents.size() + 2);
            //这里用的等号
            if (suffix == SUFFIX_PAGE) {
                newSqlNodes.add(new TextSqlNode(SQLPARSER.getPageSqlBefore()));
                newSqlNodes.addAll(contents);
                newSqlNodes.add(new TextSqlNode(SQLPARSER.getPageSqlAfter()));
                return new MyDynamicSqlSource(ms.getConfiguration(), new MixedSqlNode(newSqlNodes), false);
            } else {
                newSqlNodes.add(new TextSqlNode(SQLPARSER.getCountSqlBefore()));
                newSqlNodes.addAll(contents);
                newSqlNodes.add(new TextSqlNode(SQLPARSER.getCountSqlAfter()));
                return new MyDynamicSqlSource(ms.getConfiguration(), new MixedSqlNode(newSqlNodes), true);
            }
        } else {
            //RawSqlSource
            //这里用的等号
            if (suffix == SUFFIX_PAGE) {
                //改为分页sql
                MetaObject sqlObject = forObject(newSqlSource);
                sqlObject.setValue(BOUND_SQL, SQLPARSER.getPageSql((String) sqlObject.getValue(BOUND_SQL)));
                //添加参数映射
                List<ParameterMapping> newParameterMappings = new ArrayList<ParameterMapping>();
                newParameterMappings.addAll(newSqlSource.getBoundSql().getParameterMappings());
                newParameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, Integer.class).build());
                newParameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, Integer.class).build());
                sqlObject.setValue("boundSql.parameterMappings", newParameterMappings);
            } else {
                //改为count sql
                MetaObject sqlObject = forObject(newSqlSource);
                sqlObject.setValue(BOUND_SQL, SQLPARSER.getCountSql((String) sqlObject.getValue(BOUND_SQL)));
            }
            return newSqlSource;
        }
    }
}
