/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2023 abel533@gmail.com
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

package com.github.pagehelper.util;

import com.github.pagehelper.PageException;
import com.github.pagehelper.PageProperties;

import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public class ClassUtil {

    /**
     * 支持配置和SPI，优先级：配置类 > SPI > 默认值
     *
     * @param classStr        配置串，可空
     * @param spi             SPI 接口
     * @param properties      配置属性
     * @param defaultSupplier 默认值
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String classStr, Class<T> spi, Properties properties, Supplier<T> defaultSupplier) {
        if (StringUtil.isNotEmpty(classStr)) {
            try {
                Class<?> cls = Class.forName(classStr);
                return (T) newInstance(cls, properties);
            } catch (Exception ignored) {
            }
        }
        T result = null;
        if (spi != null) {
            ServiceLoader<T> loader = ServiceLoader.load(spi);
            for (T t : loader) {
                result = t;
                break;
            }
        }
        if (result == null) {
            result = defaultSupplier.get();
        }
        if (result instanceof PageProperties) {
            ((PageProperties) result).setProperties(properties);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(String classStr, Properties properties) {
        try {
            Class<?> cls = Class.forName(classStr);
            return (T) newInstance(cls, properties);
        } catch (Exception e) {
            throw new PageException(e);
        }
    }

    public static <T> T newInstance(Class<T> cls, Properties properties) {
        try {
            T instance = cls.newInstance();
            if (instance instanceof PageProperties) {
                ((PageProperties) instance).setProperties(properties);
            }
            return instance;
        } catch (Exception e) {
            throw new PageException(e);
        }
    }

}
