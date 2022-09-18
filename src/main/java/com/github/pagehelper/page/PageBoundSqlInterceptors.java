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

package com.github.pagehelper.page;

import com.github.pagehelper.BoundSqlInterceptor;
import com.github.pagehelper.BoundSqlInterceptorChain;
import com.github.pagehelper.PageProperties;
import com.github.pagehelper.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PageBoundSqlInterceptors {

    private BoundSqlInterceptor.Chain chain;

    public void setProperties(Properties properties) {
        //初始化 boundSqlInterceptorChain
        String boundSqlInterceptorStr = properties.getProperty("boundSqlInterceptors");
        if (StringUtil.isNotEmpty(boundSqlInterceptorStr)) {
            String[] boundSqlInterceptors = boundSqlInterceptorStr.split("[;|,]");
            List<BoundSqlInterceptor> list = new ArrayList<BoundSqlInterceptor>();
            for (int i = 0; i < boundSqlInterceptors.length; i++) {
                try {
                    BoundSqlInterceptor boundSqlInterceptor = (BoundSqlInterceptor) Class.forName(boundSqlInterceptors[i]).newInstance();
                    if (boundSqlInterceptor instanceof PageProperties) {
                        ((PageProperties) boundSqlInterceptor).setProperties(properties);
                    }
                    list.add(boundSqlInterceptor);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (list.size() > 0) {
                chain = new BoundSqlInterceptorChain(null, list);
            }
        }
    }

    public BoundSqlInterceptor.Chain getChain() {
        return chain;
    }
}
