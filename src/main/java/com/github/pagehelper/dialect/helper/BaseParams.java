package com.github.pagehelper.dialect.helper;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * 基础参数信息
 *
 * @author liuzh
 */
public abstract class BaseParams extends BasePageMethod {
    //RowBounds参数offset作为PageNum使用 - 默认不使用
    protected boolean offsetAsPageNum = false;
    //RowBounds是否进行count查询 - 默认不查询
    protected boolean rowBoundsWithCount = false;
    //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
    protected boolean pageSizeZero = false;
    //分页合理化
    protected boolean reasonable = false;
    //是否支持接口参数来传递分页参数，默认false
    protected boolean supportMethodsArguments = false;

    public void setParams(String params) {
        if (StringUtil.isNotEmpty(params)) {
            String[] ps = params.split("[;|,|&]");
            for (String s : ps) {
                String[] ss = s.split("[=|:]");
                if (ss.length == 2) {
                    PARAMS.put(ss[0], ss[1]);
                }
            }
        }
    }

    /**
     * 获取分页参数
     *
     * @param parameterObject
     * @param rowBounds
     * @return
     */
    public Page getPage(Object parameterObject, RowBounds rowBounds) {
        Page page = getLocalPage();
        if (page == null) {
            if (rowBounds != RowBounds.DEFAULT) {
                if (offsetAsPageNum) {
                    page = new Page(rowBounds.getOffset(), rowBounds.getLimit(), rowBoundsWithCount);
                } else {
                    page = new Page(new int[]{rowBounds.getOffset(), rowBounds.getLimit()}, rowBoundsWithCount);
                    //offsetAsPageNum=false的时候，由于PageNum问题，不能使用reasonable，这里会强制为false
                    page.setReasonable(false);
                }
            } else {
                try {
                    page = getPageFromObject(parameterObject, false);
                } catch (Exception e) {
                    return null;
                }
            }
            setLocalPage(page);
        }
        //分页合理化
        if (page.getReasonable() == null) {
            page.setReasonable(reasonable);
        }
        //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
        if (page.getPageSizeZero() == null) {
            page.setPageSizeZero(pageSizeZero);
        }
        return page;
    }

    public void setProperties(Properties properties) {
        //offset作为PageNum使用
        String offsetAsPageNum = properties.getProperty("offsetAsPageNum");
        this.offsetAsPageNum = Boolean.parseBoolean(offsetAsPageNum);
        //RowBounds方式是否做count查询
        String rowBoundsWithCount = properties.getProperty("rowBoundsWithCount");
        this.rowBoundsWithCount = Boolean.parseBoolean(rowBoundsWithCount);
        //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页
        String pageSizeZero = properties.getProperty("pageSizeZero");
        this.pageSizeZero = Boolean.parseBoolean(pageSizeZero);
        //分页合理化，true开启，如果分页参数不合理会自动修正。默认false不启用
        String reasonable = properties.getProperty("reasonable");
        this.reasonable = Boolean.parseBoolean(reasonable);
        //是否支持接口参数来传递分页参数，默认false
        String supportMethodsArguments = properties.getProperty("supportMethodsArguments");
        this.supportMethodsArguments = Boolean.parseBoolean(supportMethodsArguments);
        //当offsetAsPageNum=false的时候，不能
        //参数映射
        setParams(properties.getProperty("params"));
    }
}
