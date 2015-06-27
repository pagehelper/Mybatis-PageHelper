package com.github.orderbyhelper.sqlsource;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.util.Map;

/**
 * 描述信息
 *
 * @author liuzh
 * @since 2015-06-26
 */
public class OrderByDynamicSqlSource implements SqlSource, OrderBySqlSource {

    private Configuration configuration;
    private SqlNode rootSqlNode;
    private SqlSource original;

    public OrderByDynamicSqlSource(DynamicSqlSource sqlSource) {
        MetaObject metaObject = SystemMetaObject.forObject(sqlSource);
        this.configuration = (Configuration) metaObject.getValue("configuration");
        this.rootSqlNode = (SqlNode) metaObject.getValue("rootSqlNode");
        this.original = sqlSource;
    }

    public BoundSql getBoundSql(Object parameterObject) {
        DynamicContext context = new DynamicContext(configuration, parameterObject);
        rootSqlNode.apply(context);
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
        SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
        sqlSource = new OrderByStaticSqlSource((StaticSqlSource) sqlSource);
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
        return boundSql;
    }

    public SqlSource getOriginal() {
        return original;
    }

}
