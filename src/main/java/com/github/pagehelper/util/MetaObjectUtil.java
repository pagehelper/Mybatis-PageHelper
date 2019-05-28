/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 abel533@gmail.com
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
import org.apache.ibatis.reflection.MetaObject;

import java.lang.reflect.Method;

/**
 * @author liuzh
 */
public class MetaObjectUtil {
    public static Method method;

    static {
        try {
            // 高版本中的 MetaObject.forObject 有 4 个参数，低版本是 1 个
            //先判断当前使用的是否为高版本
            Class.forName("org.apache.ibatis.reflection.ReflectorFactory");
            // 下面这个 MetaObjectWithReflectCache 带反射的缓存信息
            Class<?> metaClass = Class.forName("com.github.pagehelper.util.MetaObjectWithReflectCache");
            method = metaClass.getDeclaredMethod("forObject", Object.class);
        } catch (Throwable e1) {
            try {
                Class<?> metaClass = Class.forName("org.apache.ibatis.reflection.SystemMetaObject");
                method = metaClass.getDeclaredMethod("forObject", Object.class);
            } catch (Exception e2) {
                try {
                    Class<?> metaClass = Class.forName("org.apache.ibatis.reflection.MetaObject");
                    method = metaClass.getDeclaredMethod("forObject", Object.class);
                } catch (Exception e3) {
                    throw new PageException(e3);
                }
            }
        }
    }

    public static MetaObject forObject(Object object) {
        try {
            return (MetaObject) method.invoke(null, object);
        } catch (Exception e) {
            throw new PageException(e);
        }
    }

}
