package com.github.pagehelper;

import java.util.Properties;

/**
 * 分页配置，实现该接口的类在初始化后会调用 {@link #setProperties(Properties)} 方法
 *
 * @author liuzh
 */
public interface PageProperties {

    /**
     * 设置参数
     *
     * @param properties 插件属性
     */
    void setProperties(Properties properties);

}
