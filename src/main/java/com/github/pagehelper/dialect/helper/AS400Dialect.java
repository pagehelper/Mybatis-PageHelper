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

package com.github.pagehelper.dialect.helper;

import java.util.Map;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import com.github.pagehelper.Page;
import com.github.pagehelper.dialect.AbstractHelperDialect;

/**
 * @author bluezealot
 */
@SuppressWarnings("rawtypes")
public class AS400Dialect extends AbstractHelperDialect {

	@Override
	public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap,
			Page page, BoundSql boundSql, CacheKey pageKey) {
		paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow());
		paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());
		pageKey.update(page.getStartRow());
		pageKey.update(page.getPageSize());
		handleParameter(boundSql, ms, long.class, int.class);
		return paramMap;
	}

	@Override
	public String getPageSql(String sql, Page page, CacheKey pageKey) {
		return sql + " OFFSET ? ROWS FETCH FIRST ? ROWS ONLY";
	}
}
