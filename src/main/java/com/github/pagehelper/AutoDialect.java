package com.github.pagehelper;

import com.github.pagehelper.dialect.AbstractHelperDialect;
import org.apache.ibatis.mapping.MappedStatement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 自动获取方言
 *
 * @param <K> 缓存key类型
 */
public interface AutoDialect<K> {

    /**
     * 获取用于缓存 {@link #extractDialect } 方法返回值的 key，当返回 null 时不缓存，返回值时先判断是否已存在，不存在时调用 {@link #extractDialect } 再缓存
     *
     * @param ms
     * @param dataSource
     * @param properties
     * @return
     */
    K extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties);

    /**
     * 提取 dialect
     *
     * @param dialectKey
     * @param ms
     * @param dataSource
     * @param properties
     * @return
     */
    AbstractHelperDialect extractDialect(K dialectKey, MappedStatement ms, DataSource dataSource, Properties properties);

}
