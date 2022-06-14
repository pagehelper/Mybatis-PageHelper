package com.github.pagehelper.util;

import java.util.regex.Pattern;

/**
 * 更严格的SQL注入检测
 */
public class SqlSafeUtil {
    /**
     * SQL语法检查正则：符合两个关键字（有先后顺序）才算匹配
     * <p>
     * 参考: mybatis-plus-core/src/main/java/com/baomidou/mybatisplus/core/toolkit/sql/SqlInjectionUtils.java
     */
    private static final Pattern SQL_SYNTAX_PATTERN = Pattern.compile("(insert|delete|update|select|create|drop|truncate|grant|alter|deny|revoke|call|execute|exec|declare|show|rename|set)" +
            ".+(into|from|set|where|table|database|view|index|on|cursor|procedure|trigger|for|password|union|and|or)", Pattern.CASE_INSENSITIVE);
    /**
     * 使用'、;或注释截断SQL检查正则
     * <p>
     * 参考: mybatis-plus-core/src/main/java/com/baomidou/mybatisplus/core/toolkit/sql/SqlInjectionUtils.java
     */
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile("'.*(or|union|--|#|/*|;)", Pattern.CASE_INSENSITIVE);

    /**
     * 检查参数是否存在 SQL 注入
     *
     * @param value 检查参数
     * @return true 非法 false 合法
     */
    public static boolean check(String value) {
        if (value == null) {
            return false;
        }
        // 不允许使用任何函数（不能出现括号），否则无法检测后面这个注入 order by id,if(1=2,1,(sleep(100)));
        return value.contains("(") || SQL_COMMENT_PATTERN.matcher(value).find() || SQL_SYNTAX_PATTERN.matcher(value).find();
    }
}
