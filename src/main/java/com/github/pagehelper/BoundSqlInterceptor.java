package com.github.pagehelper;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;

/**
 * BoundSql 处理器
 */
public interface BoundSqlInterceptor {

    /**
     * boundsql 处理
     *
     * @param type     类型
     * @param boundSql 当前类型的 boundSql
     * @param cacheKey 缓存 key
     * @param chain    处理器链，通过 chain.doBoundSql 方法继续执行后续方法，也可以直接返回 boundSql 终止后续方法的执行
     * @return 允许修改 boundSql 并返回修改后的
     */
    BoundSql boundSql(Type type, BoundSql boundSql, CacheKey cacheKey, Chain chain);

    enum Type {
        /**
         * 原始SQL，分页插件执行前，先执行这个类型
         */
        ORIGINAL,
        /**
         * count SQL，第二个执行这里
         */
        COUNT_SQL,
        /**
         * 分页 SQL，最后执行这里
         */
        PAGE_SQL
    }

    /**
     * 处理器链，可以控制是否继续执行
     */
    interface Chain {

        Chain DO_NOTHING = new Chain() {
            @Override
            public BoundSql doBoundSql(Type type, BoundSql boundSql, CacheKey cacheKey) {
                return boundSql;
            }
        };

        BoundSql doBoundSql(Type type, BoundSql boundSql, CacheKey cacheKey);

    }
}
