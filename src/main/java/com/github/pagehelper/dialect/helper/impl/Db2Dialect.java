package com.github.pagehelper.dialect.helper.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.dialect.helper.HelperDialect;
import org.apache.ibatis.cache.CacheKey;

/**
 * @author liuzh
 */
public class Db2Dialect extends HelperDialect {

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
        sqlBuilder.append("SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS ROW_ID FROM ( ");
        sqlBuilder.append(sql);
        sqlBuilder.append(" ) AS TMP_PAGE) WHERE ROW_ID BETWEEN ");
        sqlBuilder.append(page.getStartRow() + 1);
        sqlBuilder.append(" AND ");
        sqlBuilder.append(page.getEndRow());
        pageKey.update(page.getStartRow() + 1);
        pageKey.update(page.getEndRow());
        return sqlBuilder.toString();
    }

}
