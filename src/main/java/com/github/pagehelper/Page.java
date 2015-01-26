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

import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Mybatis - 分页对象
 *
 * @author liuzh/abel533/isea533
 * @version 3.6.0
 *          项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 */
public class Page<E> extends ArrayList<E> {
    private static final long serialVersionUID = 1L;

    /**
     * 不进行count查询
     */
    private static final int NO_SQL_COUNT = -1;
    /**
     * 进行count查询
     */
    private static final int SQL_COUNT = 0;
    /**
     * 页码，从1开始
     */
    private int pageNum;
    /**
     * 页面大小
     */
    private int pageSize;
    /**
     * 起始行
     */
    private int startRow;
    /**
     * 末行
     */
    private int endRow;
    /**
     * 总数
     */
    private long total;
    /**
     * 总页数
     */
    private int pages;
    /**
     * 针对sqlserver - 在其他数据库中该字段无效
     */
    private String orderBy;
    /**
     * 分页合理化
     */
    private Boolean reasonable;
    /**
     * 当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
     */
    private Boolean pageSizeZero;

    public Page() {
        super();
    }

    public Page(int pageNum, int pageSize) {
        this(pageNum, pageSize, SQL_COUNT, null);
    }

    public Page(int pageNum, int pageSize, boolean count) {
        this(pageNum, pageSize, count ? Page.SQL_COUNT : Page.NO_SQL_COUNT, null);
    }

    private Page(int pageNum, int pageSize, int total, Boolean reasonable) {
        super(pageSize > -1 ? pageSize : 0);
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        calculateStartAndEndRow();
        setReasonable(reasonable);
    }

    public Page(RowBounds rowBounds, boolean count) {
        this(rowBounds, count ? Page.SQL_COUNT : Page.NO_SQL_COUNT);
    }


    public Page(RowBounds rowBounds, int total) {
        super(rowBounds.getLimit() > -1 ? rowBounds.getLimit() : 0);
        this.pageSize = rowBounds.getLimit();
        this.startRow = rowBounds.getOffset();
        //RowBounds方式默认不求count总数，如果想求count,可以修改这里为SQL_COUNT
        this.total = total;
        this.endRow = this.startRow + this.pageSize;
    }

    public List<E> getResult() {
        return this;
    }

    public int getPages() {
        return pages;
    }

    public int getEndRow() {
        return endRow;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        //分页合理化，针对不合理的页码自动处理
        this.pageNum = ((reasonable != null && reasonable) && pageNum <= 0) ? 1 : pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getStartRow() {
        return startRow;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
        if (pageSize > 0) {
            pages = (int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1));
        } else {
            pages = 0;
        }
        //分页合理化，针对不合理的页码自动处理
        if ((reasonable != null && reasonable) && pageNum > pages) {
            pageNum = pages;
            calculateStartAndEndRow();
        }
    }

    public void setReasonable(Boolean reasonable) {
        if (reasonable == null) {
            return;
        }
        this.reasonable = reasonable;
        //分页合理化，针对不合理的页码自动处理
        if (this.reasonable && this.pageNum <= 0) {
            this.pageNum = 1;
            calculateStartAndEndRow();
        }
    }

    public Boolean getReasonable() {
        return reasonable;
    }

    public Boolean getPageSizeZero() {
        return pageSizeZero;
    }

    public void setPageSizeZero(Boolean pageSizeZero) {
        this.pageSizeZero = pageSizeZero;
    }

    public String getOrderBy() {
        return orderBy;
    }

    /**
     * 计算起止行号
     */
    private void calculateStartAndEndRow() {
        this.startRow = this.pageNum > 0 ? (this.pageNum - 1) * this.pageSize : 0;
        this.endRow = this.startRow + this.pageSize * (this.pageNum > 0 ? 1 : 0);
    }

    public boolean isCount() {
        return this.total > NO_SQL_COUNT;
    }

    //增加链式调用方法

    /**
     * 设置页码
     *
     * @param pageNum
     * @return
     */
    public Page<E> pageNum(int pageNum) {
        //分页合理化，针对不合理的页码自动处理
        this.pageNum = ((reasonable != null && reasonable) && pageNum <= 0) ? 1 : pageNum;
        return this;
    }

    /**
     * 设置页面大小
     *
     * @param pageSize
     * @return
     */
    public Page<E> pageSize(int pageSize) {
        this.pageSize = pageSize;
        calculateStartAndEndRow();
        return this;
    }

    /**
     * 是否执行count查询
     *
     * @param count
     * @return
     */
    public Page<E> count(Boolean count) {
        this.total = count ? Page.SQL_COUNT : Page.NO_SQL_COUNT;
        return this;
    }

    /**
     * 针对sqlserver有效
     *
     * @param orderBy
     * @return
     */
    public Page<E> orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    /**
     * 设置合理化
     *
     * @param reasonable
     * @return
     */
    public Page<E> reasonable(Boolean reasonable) {
        setReasonable(reasonable);
        return this;
    }

    /**
     * 当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
     *
     * @param pageSizeZero
     * @return
     */
    public Page<E> pageSizeZero(Boolean pageSizeZero) {
        setPageSizeZero(pageSizeZero);
        return this;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Page{");
        sb.append("pageNum=").append(pageNum);
        sb.append(", pageSize=").append(pageSize);
        sb.append(", startRow=").append(startRow);
        sb.append(", endRow=").append(endRow);
        sb.append(", total=").append(total);
        sb.append(", pages=").append(pages);
        sb.append(", orderBy='").append(orderBy).append('\'');
        sb.append(", reasonable=").append(reasonable);
        sb.append(", pageSizeZero=").append(pageSizeZero);
        sb.append('}');
        return sb.toString();
    }
}
