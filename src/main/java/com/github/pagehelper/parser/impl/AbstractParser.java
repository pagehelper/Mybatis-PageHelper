package com.github.pagehelper.parser.impl;

import com.github.pagehelper.*;
import com.github.pagehelper.parser.Parser;
import com.github.pagehelper.sqlsource.PageProviderSqlSource;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractParser implements Parser, Constant {
    //处理SQL
    public static final SqlParser sqlParser = new SqlParser();

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
            case sqlserver:
                parser = new SqlServerParser();
                break;
            case db2:
                parser = new Db2Parser();
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
        if (boundSql.getParameterMappings() != null) {
            newParameterMappings.addAll(boundSql.getParameterMappings());
        }
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
            MetaObject metaObject = SystemMetaObject.forObject(parameterObject);
            //需要针对注解形式的MyProviderSqlSource保存原值
            if (ms.getSqlSource() instanceof PageProviderSqlSource) {
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
