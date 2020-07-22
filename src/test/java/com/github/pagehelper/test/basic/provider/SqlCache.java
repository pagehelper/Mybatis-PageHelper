package com.github.pagehelper.test.basic.provider;

public class SqlCache {

    private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<String>();

    public static void set(String str) {
        THREAD_LOCAL.set(str);
    }

    public static String get() {
        return THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
