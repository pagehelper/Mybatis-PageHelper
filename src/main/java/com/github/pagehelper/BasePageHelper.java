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

package com.github.pagehelper;

import com.github.pagehelper.util.SqlUtil;

public abstract class BasePageHelper {
    /**
     * 获取任意查询方法的count总数
     *
     * @param select
     * @return
     */
    public static long count(ISelect select) {
        Page<?> page = startPage(1, -1, true);
        select.doSelect();
        return page.getTotal();
    }

    /**
     * 开始分页
     *
     * @param pageNum  页码
     * @param pageSize 每页显示数量
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize) {
        return startPage(pageNum, pageSize, true);
    }

    /**
     * 开始分页
     *
     * @param pageNum  页码
     * @param pageSize 每页显示数量
     * @param count    是否进行count查询
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, boolean count) {
        return startPage(pageNum, pageSize, count, null);
    }

    /**
     * 开始分页
     *
     * @param pageNum  页码
     * @param pageSize 每页显示数量
     * @param orderBy  排序
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, String orderBy) {
        Page<E> page = startPage(pageNum, pageSize);
        page.setOrderBy(orderBy);
        return page;
    }

    /**
     * 开始分页
     *
     * @param offset 页码
     * @param limit  每页显示数量
     */
    public static <E> Page<E> offsetPage(int offset, int limit) {
        return offsetPage(offset, limit, true);
    }

    /**
     * 开始分页
     *
     * @param offset 页码
     * @param limit  每页显示数量
     * @param count  是否进行count查询
     */
    public static <E> Page<E> offsetPage(int offset, int limit, boolean count) {
        Page<E> page = new Page<E>(new int[]{offset, limit}, count);
        //当已经执行过orderBy的时候
        Page<E> oldPage = SqlUtil.getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        SqlUtil.setLocalPage(page);
        return page;
    }

    /**
     * 开始分页
     *
     * @param offset  页码
     * @param limit   每页显示数量
     * @param orderBy 排序
     */
    public static <E> Page<E> offsetPage(int offset, int limit, String orderBy) {
        Page<E> page = offsetPage(offset, limit);
        page.setOrderBy(orderBy);
        return page;
    }

    /**
     * 开始分页
     *
     * @param pageNum    页码
     * @param pageSize   每页显示数量
     * @param count      是否进行count查询
     * @param reasonable 分页合理化,null时用默认配置
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, boolean count, Boolean reasonable) {
        return startPage(pageNum, pageSize, count, reasonable, null);
    }

    /**
     * 开始分页
     *
     * @param pageNum      页码
     * @param pageSize     每页显示数量
     * @param count        是否进行count查询
     * @param reasonable   分页合理化,null时用默认配置
     * @param pageSizeZero true且pageSize=0时返回全部结果，false时分页,null时用默认配置
     */
    public static <E> Page<E> startPage(int pageNum, int pageSize, boolean count, Boolean reasonable, Boolean pageSizeZero) {
        Page<E> page = new Page<E>(pageNum, pageSize, count);
        page.setReasonable(reasonable);
        page.setPageSizeZero(pageSizeZero);
        //当已经执行过orderBy的时候
        Page<E> oldPage = SqlUtil.getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        SqlUtil.setLocalPage(page);
        return page;
    }

    /**
     * 开始分页
     *
     * @param params
     */
    public static <E> Page<E> startPage(Object params) {
        Page<E> page = SqlUtil.getPageFromObject(params);
        //当已经执行过orderBy的时候
        Page<E> oldPage = SqlUtil.getLocalPage();
        if (oldPage != null && oldPage.isOrderByOnly()) {
            page.setOrderBy(oldPage.getOrderBy());
        }
        SqlUtil.setLocalPage(page);
        return page;
    }

    /**
     * 排序
     *
     * @param orderBy
     */
    public static void orderBy(String orderBy) {
        Page<?> page = SqlUtil.getLocalPage();
        if (page != null) {
            page.setOrderBy(orderBy);
        } else {
            page = new Page();
            page.setOrderBy(orderBy);
            page.setOrderByOnly(true);
            SqlUtil.setLocalPage(page);
        }
    }

    /**
     * 手动清空分页参数
     */
    public static void clearPage(){
        SqlUtil.clearLocalPage();
    }
}
