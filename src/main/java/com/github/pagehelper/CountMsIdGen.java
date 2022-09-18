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

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * 构建当前查询对应的 count 方法 id
 * <p>
 * 返回的 msId 会先判断是否存在自定义的方法，存在就直接使用
 * <p>
 * 如果不存在，会根据当前的 msId 创建 MappedStatement
 *
 * @author liuzh
 */
public interface CountMsIdGen {

    /**
     * 默认实现
     */
    CountMsIdGen DEFAULT = new CountMsIdGen() {
        @Override
        public String genCountMsId(MappedStatement ms, Object parameter, BoundSql boundSql, String countSuffix) {
            return ms.getId() + countSuffix;
        }
    };

    /**
     * 构建当前查询对应的 count 方法 id
     *
     * @param ms          查询对应的 MappedStatement
     * @param parameter   方法参数
     * @param boundSql    查询SQL
     * @param countSuffix 配置的 count 后缀
     * @return count 查询丢的 msId
     */
    String genCountMsId(MappedStatement ms, Object parameter,
                        BoundSql boundSql, String countSuffix);

}
