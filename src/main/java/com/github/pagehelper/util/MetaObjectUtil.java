package com.github.pagehelper.util;

import org.apache.ibatis.reflection.MetaObject;

import java.lang.reflect.Method;

/**
 * @author liuzh
 */
public class MetaObjectUtil {
    public static Method method;

    static {
        try {
            Class<?> metaClass = Class.forName("org.apache.ibatis.reflection.SystemMetaObject");
            method = metaClass.getDeclaredMethod("forObject", Object.class);
        } catch (Exception e1) {
            try {
                Class<?> metaClass = Class.forName("org.apache.ibatis.reflection.MetaObject");
                method = metaClass.getDeclaredMethod("forObject", Object.class);
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }

    }

    public static MetaObject forObject(Object object) {
        try {
            return (MetaObject) method.invoke(null, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
