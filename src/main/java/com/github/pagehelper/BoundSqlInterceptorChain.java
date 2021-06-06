package com.github.pagehelper;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;

import java.util.List;

public class BoundSqlInterceptorChain implements BoundSqlInterceptor.Chain {
    private final BoundSqlInterceptor.Chain original;
    private final List<BoundSqlInterceptor> interceptors;

    private int     index = 0;
    private boolean executable;

    public BoundSqlInterceptorChain(BoundSqlInterceptor.Chain original, List<BoundSqlInterceptor> interceptors) {
        this(original, interceptors, false);
    }

    private BoundSqlInterceptorChain(BoundSqlInterceptor.Chain original, List<BoundSqlInterceptor> interceptors, boolean executable) {
        this.original = original;
        this.interceptors = interceptors;
        this.executable = executable;
    }

    @Override
    public BoundSql doBoundSql(BoundSqlInterceptor.Type type, BoundSql boundSql, CacheKey cacheKey) {
        if(executable) {
            return _doBoundSql(type, boundSql, cacheKey);
        } else {
            return new BoundSqlInterceptorChain(original, interceptors, true).doBoundSql(type, boundSql, cacheKey);
        }
    }

    private BoundSql _doBoundSql(BoundSqlInterceptor.Type type, BoundSql boundSql, CacheKey cacheKey) {
        if (this.interceptors == null || this.interceptors.size() == this.index) {
            return this.original != null ? this.original.doBoundSql(type, boundSql, cacheKey) : boundSql;
        } else {
            return this.interceptors.get(this.index++).boundSql(type, boundSql, cacheKey, this);
        }
    }

}
