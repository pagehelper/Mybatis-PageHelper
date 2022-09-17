package com.github.pagehelper.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTraceUtil {

    /**
     * 当前方法堆栈信息
     */
    public static String current() {
        Exception exception = new Exception("设置分页参数时的堆栈信息");
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

}
