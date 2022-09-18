/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2022 abel533@gmail.com
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
