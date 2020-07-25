package com.github.pagehelper;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;

import java.util.List;

public class BoundSqlInterceptorChain implements BoundSqlInterceptor.Chain {
    private final BoundSqlInterceptor.Chain original;
    private final List<BoundSqlInterceptor> interceptors;

    private int index = 0;

    public BoundSqlInterceptorChain(BoundSqlInterceptor.Chain original, List<BoundSqlInterceptor> interceptors) {
        this.original = original;
        this.interceptors = interceptors;
    }

    public void reset() {
        this.index = 0;
    }

    @Override
    public BoundSql doBoundSql(BoundSqlInterceptor.Type type, BoundSql boundSql, CacheKey cacheKey) {
        if (this.interceptors == null || this.interceptors.size() == this.index) {
            return this.original != null ? this.original.doBoundSql(type, boundSql, cacheKey) : boundSql;
        } else {
            return this.interceptors.get(this.index++).boundSql(type, boundSql, cacheKey, this);
        }
    }

}
