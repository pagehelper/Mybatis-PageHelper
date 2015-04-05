package com.github.pagehelper;

/**
 * Created by liuzh on 2015/4/5.
 */
public interface Constant {
    //分页的id后缀
    String SUFFIX_PAGE = "_PageHelper";
    //count查询的id后缀
    String SUFFIX_COUNT = SUFFIX_PAGE + "_Count";
    //第一个分页参数
    String PAGEPARAMETER_FIRST = "First" + SUFFIX_PAGE;
    //第二个分页参数
    String PAGEPARAMETER_SECOND = "Second" + SUFFIX_PAGE;

    String PROVIDER_OBJECT = "_provider_object";
    //存储原始的参数
    String ORIGINAL_PARAMETER_OBJECT = "_ORIGINAL_PARAMETER_OBJECT";
}
