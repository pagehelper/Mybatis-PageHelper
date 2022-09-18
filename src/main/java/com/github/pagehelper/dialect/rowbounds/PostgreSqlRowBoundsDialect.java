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

package com.github.pagehelper.dialect.rowbounds;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.session.RowBounds;

import com.github.pagehelper.dialect.AbstractRowBoundsDialect;

/**
 * PostgreSQL 基于 RowBounds 的分页.
 *
 * @author liym
 * @since 2021-02-06 19:31 新建
 */
public class PostgreSqlRowBoundsDialect extends AbstractRowBoundsDialect {

    /**
     * 构建 <a href="https://www.postgresql.org/docs/current/queries-limit.html">PostgreSQL</a>分页查询语句
     */
    @Override
    public String getPageSql(String sql, RowBounds rowBounds, CacheKey pageKey) {
        StringBuilder sqlStr = new StringBuilder(sql.length() + 17);
        sqlStr.append(sql);
        if (rowBounds.getOffset() == 0) {
            sqlStr.append(" LIMIT ");
            sqlStr.append(rowBounds.getLimit());
        } else {
            sqlStr.append(" LIMIT ");
            sqlStr.append(rowBounds.getLimit());
            sqlStr.append(" OFFSET ");
            sqlStr.append(rowBounds.getOffset());
            pageKey.update(rowBounds.getOffset());
        }
        pageKey.update(rowBounds.getLimit());
        return sqlStr.toString();
    }

}
