package com.github.pagehelper;

/**
 * 是否返回PageInfo
 *
 * @author liuzh
 * @since 2015-11-07 11:26
 */
public enum ReturnPageInfo {
    ALWAYS, //总是返回PageInfo
    CHECK,  //检查返回类型判断是否为PageInfo
    NONE    //不返回PageInfo,返回的Page
}
