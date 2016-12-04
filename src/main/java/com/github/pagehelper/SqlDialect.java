package com.github.pagehelper;

import java.util.List;
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

/**
 * 数据库方言，针对不同数据库进行实现
 *
 * @author liuzh
 */
@SuppressWarnings("rawtypes")
public interface SqlDialect {
	/**
	 * 跳过 count 和 分页查询
	 *
	 * @param msId 执行的  MyBatis 方法全名
	 * @param parameterObject 方法参数
	 * @param rowBounds 分页参数
	 * @return true 跳过，返回默认查询结果，false 执行分页查询
	 */
	boolean skip(String msId, Object parameterObject, RowBounds rowBounds);

	/**
	 * 执行分页前，返回 true 会进行 count 查询，false 会继续下面的 beforePage 判断
	 *
	 * @param msId 执行的  MyBatis 方法全名
	 * @param parameterObject 方法参数
	 * @param rowBounds 分页参数
	 * @return
	 */
	boolean beforeCount(String msId, Object parameterObject, RowBounds rowBounds);

	/**
	 * 生成 count 查询 sql
	 *
	 * @param boundSql 绑定 SQL 对象
	 * @param parameterObject 方法参数
	 * @param rowBounds 分页参数
	 * @param countKey count 缓存 key
	 * @return
	 */
	String getCountSql(BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey countKey);

	/**
	 * 执行完 count 查询后
	 *
	 * @param count 查询结果总数
	 * @param parameterObject 接口参数
	 * @param rowBounds 分页参数
	 */
	void afterCount(long count, Object parameterObject, RowBounds rowBounds);

	/**
	 * 处理查询参数对象
	 *
	 * @param ms
	 * @param parameterObject
	 * @param boundSql
     * @param pageKey
	 * @return
	 */
	Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey);

	/**
	 * 执行分页前，返回 true 会进行分页查询，false 会返回默认查询结果
	 *
	 * @param msId 执行的 MyBatis 方法全名
	 * @param parameterObject 方法参数
	 * @param rowBounds 分页参数
	 * @return
	 */
	boolean beforePage(String msId, Object parameterObject, RowBounds rowBounds);

	/**
	 * 生成分页查询 sql
	 *
	 * @param boundSql 绑定 SQL 对象
	 * @param parameterObject 方法参数
	 * @param rowBounds 分页参数
	 * @param pageKey 分页缓存 key
	 * @return
	 */
	String getPageSql(BoundSql boundSql, Object parameterObject, RowBounds rowBounds, CacheKey pageKey);

	/**
	 * 分页查询后，处理分页结果，拦截器中直接 return 该方法的返回值
	 *
	 * @param pageList 分页查询结果
	 * @param parameterObject 方法参数
	 * @param rowBounds 分页参数
	 * @return
	 */
	Object afterPage(List pageList, Object parameterObject, RowBounds rowBounds);

	/**
	 * 设置参数
	 *
	 * @param properties 插件属性
	 */
	void setProperties(Properties properties);
}
