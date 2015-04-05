package com.github.pagehelper.parser;

import com.github.pagehelper.Page;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 处理SQL
 *
 * @author liuzh
 */
public interface Parser {

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
