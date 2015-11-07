package com.github.pagehelper.model;

/**
 * @author liuzh
 * @since 2015-11-07 14:39
 */
public class CountryQueryModel {

    private Integer pageNum;

    private Integer pageSize;

    private String orderBy;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}
