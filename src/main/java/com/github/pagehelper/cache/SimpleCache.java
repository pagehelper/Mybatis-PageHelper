/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 abel533@gmail.com
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

package com.github.pagehelper.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 最简单的缓存实现，适用于固定SQL时，SQL使用参数的情况，
 *
 * 如果存在大量拼接或者 ${} 的情况时，建议使用其他缓存实现，避免内存溢出
 *
 * @author liuzh
 */
public class SimpleCache implements Cache<String, String> {

    private Map<String, String> CACHE = new ConcurrentHashMap<String, String>();

    @Override
    public String get(String key) {
        return CACHE.get(key);
    }

    @Override
    public void put(String key, String value) {
        CACHE.put(key, value);
    }
}
