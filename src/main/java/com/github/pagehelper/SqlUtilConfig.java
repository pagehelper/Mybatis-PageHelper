/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 abel533@gmail.com
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

/**
 * 适用于Spring Boot
 *
 * @author liuzh_3nofxnp
 * @since 4.1.0
 */
public class SqlUtilConfig {
    //方言
    private String dialect;
    //RowBounds参数offset作为PageNum使用 - 默认不使用
    private boolean offsetAsPageNum = false;
    //RowBounds是否进行count查询 - 默认不查询
    private boolean rowBoundsWithCount = false;
    //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
    private boolean pageSizeZero = false;
    //分页合理化
    private boolean reasonable = false;
    //是否支持接口参数来传递分页参数，默认false
    private boolean supportMethodsArguments = false;
    //参数别名
    private String params;
    //运行时自动获取dialect
    private boolean autoRuntimeDialect;
    //多数据源时，获取jdbcurl后是否关闭数据源
    private boolean closeConn = true;
    //cacheClass sql缓存接口
    private String sqlCacheClass;

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public boolean isOffsetAsPageNum() {
        return offsetAsPageNum;
    }

    public void setOffsetAsPageNum(boolean offsetAsPageNum) {
        this.offsetAsPageNum = offsetAsPageNum;
    }

    public boolean isRowBoundsWithCount() {
        return rowBoundsWithCount;
    }

    public void setRowBoundsWithCount(boolean rowBoundsWithCount) {
        this.rowBoundsWithCount = rowBoundsWithCount;
    }

    public boolean isPageSizeZero() {
        return pageSizeZero;
    }

    public void setPageSizeZero(boolean pageSizeZero) {
        this.pageSizeZero = pageSizeZero;
    }

    public boolean isReasonable() {
        return reasonable;
    }

    public void setReasonable(boolean reasonable) {
        this.reasonable = reasonable;
    }

    public boolean isSupportMethodsArguments() {
        return supportMethodsArguments;
    }

    public void setSupportMethodsArguments(boolean supportMethodsArguments) {
        this.supportMethodsArguments = supportMethodsArguments;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public boolean isAutoRuntimeDialect() {
        return autoRuntimeDialect;
    }

    public void setAutoRuntimeDialect(boolean autoRuntimeDialect) {
        this.autoRuntimeDialect = autoRuntimeDialect;
    }

    public boolean isCloseConn() {
        return closeConn;
    }

    public void setCloseConn(boolean closeConn) {
        this.closeConn = closeConn;
    }

    public String getSqlCacheClass() {
        return sqlCacheClass;
    }

    public void setSqlCacheClass(String sqlCacheClass) {
        this.sqlCacheClass = sqlCacheClass;
    }
}
