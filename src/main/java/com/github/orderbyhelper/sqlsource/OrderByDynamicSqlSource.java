package com.github.orderbyhelper.sqlsource;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 描述信息
 *
 * @author liuzh
 * @since 2015-06-26
 */
public class OrderByDynamicSqlSource implements SqlSource, OrderBy {

    private Configuration configuration;
    private SqlNode rootSqlNode;

    public OrderByDynamicSqlSource(DynamicSqlSource sqlSource) {
        MetaObject metaObject = SystemMetaObject.forObject(sqlSource);
        this.configuration = (Configuration) metaObject.getValue("configuration");
        SqlNode sqlNode = (SqlNode) metaObject.getValue("rootSqlNode");
        List<SqlNode> contents = null;
        if (sqlNode instanceof TextSqlNode) {
            contents = new LinkedList<SqlNode>();
            contents.add(sqlNode);

        } else {
            MixedSqlNode mixedSqlNode = (MixedSqlNode) sqlNode;
            metaObject = SystemMetaObject.forObject(mixedSqlNode);
            contents = (List<SqlNode>) metaObject.getValue("contents");
        }
        contents.add(new IfSqlNode(new TextSqlNode("order by ${orderByHelper}"),"orderByHelper != null and orderByHelper != ''"));
        this.rootSqlNode = new MixedSqlNode(contents);
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
        return boundSql;
    }

}
