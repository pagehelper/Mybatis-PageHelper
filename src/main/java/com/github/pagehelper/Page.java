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

package com.github.pagehelper;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Mybatis - 分页对象
 *
 * @author liuzh/abel533/isea533
 * @version 3.6.0
 *          项目地址 : http://git.oschina.net/free/Mybatis_PageHelper
 */
public class Page<E> extends ArrayList<E> implements Closeable {
    private static final long serialVersionUID = 1L;

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
     * 包含count查询
     */
    private boolean count = true;
    /**
     * 分页合理化
     */
    private Boolean reasonable;
    /**
     * 当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
     */
    private Boolean pageSizeZero;
    /**
     * 进行count查询的列名
     */
    private String countColumn;
    /**
     * 排序
     */
    private String orderBy;
    /**
     * 只增加排序
     */
    private boolean orderByOnly;

    public Page() {
        super();
    }

    public Page(int pageNum, int pageSize) {
        this(pageNum, pageSize, true, null);
    }

    public Page(int pageNum, int pageSize, boolean count) {
        this(pageNum, pageSize, count, null);
    }

    private Page(int pageNum, int pageSize, boolean count, Boolean reasonable) {
        super(0);
        if (pageNum == 1 && pageSize == Integer.MAX_VALUE) {
            pageSizeZero = true;
            pageSize = 0;
        }
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.count = count;
        calculateStartAndEndRow();
        setReasonable(reasonable);
    }

    /**
     * int[] rowBounds
     * 0 : offset
     * 1 : limit
     */
    public Page(int[] rowBounds, boolean count) {
        super(0);
        if (rowBounds[0] == 0 && rowBounds[1] == Integer.MAX_VALUE) {
            pageSizeZero = true;
            this.pageSize = 0;
        } else {
            this.pageSize = rowBounds[1];
            this.pageNum = rowBounds[1] != 0 ? (int) (Math.ceil(((double) rowBounds[0] + rowBounds[1]) / rowBounds[1])) : 0;
        }
        this.startRow = rowBounds[0];
        this.count = count;
        this.endRow = this.startRow + rowBounds[1];
    }

    public List<E> getResult() {
        return this;
    }

    public int getPages() {
        return pages;
    }

    public Page<E> setPages(int pages) {
        this.pages = pages;
        return this;
    }

    public int getEndRow() {
        return endRow;
    }

    public Page<E> setEndRow(int endRow) {
        this.endRow = endRow;
        return this;
    }

    public int getPageNum() {
        return pageNum;
    }

    public Page<E> setPageNum(int pageNum) {
        //分页合理化，针对不合理的页码自动处理
        this.pageNum = ((reasonable != null && reasonable) && pageNum <= 0) ? 1 : pageNum;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Page<E> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public int getStartRow() {
        return startRow;
    }

    public Page<E> setStartRow(int startRow) {
        this.startRow = startRow;
        return this;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
        if (total == -1) {
            pages = 1;
            return;
        }
        if (pageSize > 0) {
            pages = (int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1));
        } else {
            pages = 0;
        }
        //分页合理化，针对不合理的页码自动处理
        if ((reasonable != null && reasonable) && pageNum > pages) {
            if(pages!=0){
                pageNum = pages;
            }
            calculateStartAndEndRow();
        }
    }

    public Boolean getReasonable() {
        return reasonable;
    }

    public Page<E> setReasonable(Boolean reasonable) {
        if (reasonable == null) {
            return this;
        }
        this.reasonable = reasonable;
        //分页合理化，针对不合理的页码自动处理
        if (this.reasonable && this.pageNum <= 0) {
            this.pageNum = 1;
            calculateStartAndEndRow();
        }
        return this;
    }

    public Boolean getPageSizeZero() {
        return pageSizeZero;
    }

    public Page<E> setPageSizeZero(Boolean pageSizeZero) {
        if (pageSizeZero != null) {
            this.pageSizeZero = pageSizeZero;
        }
        return this;
    }
    public String getOrderBy() {
        return orderBy;
    }

    public <E> Page<E> setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return (Page<E>) this;
    }

    public boolean isOrderByOnly() {
        return orderByOnly;
    }

    public void setOrderByOnly(boolean orderByOnly) {
        this.orderByOnly = orderByOnly;
    }

    /**
     * 计算起止行号
     */
    private void calculateStartAndEndRow() {
        this.startRow = this.pageNum > 0 ? (this.pageNum - 1) * this.pageSize : 0;
        this.endRow = this.startRow + this.pageSize * (this.pageNum > 0 ? 1 : 0);
    }

    public boolean isCount() {
        return this.count;
    }

    public Page<E> setCount(boolean count) {
        this.count = count;
        return this;
    }

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
        this.count = count;
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

    /**
     * 指定 count 查询列
     *
     * @param columnName
     * @return
     */
    public Page<E> countColumn(String columnName) {
        this.countColumn = columnName;
        return this;
    }

    public PageInfo<E> toPageInfo() {
        PageInfo<E> pageInfo = new PageInfo<E>(this);
        return pageInfo;
    }

    public PageSerializable<E> toPageSerializable() {
        PageSerializable<E> serializable = new PageSerializable<E>(this);
        return serializable;
    }

    public <E> Page<E> doSelectPage(ISelect select) {
        select.doSelect();
        return (Page<E>) this;
    }

    public <E> PageInfo<E> doSelectPageInfo(ISelect select) {
        select.doSelect();
        return (PageInfo<E>) this.toPageInfo();
    }

    public <E> PageSerializable<E> doSelectPageSerializable(ISelect select) {
        select.doSelect();
        return (PageSerializable<E>) this.toPageSerializable();
    }

    public long doCount(ISelect select) {
        this.pageSizeZero = true;
        this.pageSize = 0;
        select.doSelect();
        return this.total;
    }

    public String getCountColumn() {
        return countColumn;
    }

    public void setCountColumn(String countColumn) {
        this.countColumn = countColumn;
    }

    @Override
    public String toString() {
        return "Page{" +
                "count=" + count +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", startRow=" + startRow +
                ", endRow=" + endRow +
                ", total=" + total +
                ", pages=" + pages +
                ", reasonable=" + reasonable +
                ", pageSizeZero=" + pageSizeZero +
                '}' + super.toString();
    }

    @Override
    public void close() {
        PageHelper.clearPage();
    }
}
