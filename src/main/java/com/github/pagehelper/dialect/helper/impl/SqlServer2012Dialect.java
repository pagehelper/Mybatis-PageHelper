package com.github.pagehelper.dialect.helper.impl;

import com.github.pagehelper.Page;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.session.RowBounds;

/**
 * @author liuzh
 */
public class SqlServer2012Dialect extends SqlServerDialect {

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 14);
        sqlBuilder.append(sql);
        if (page.getStartRow() > 0) {
            sqlBuilder.append(" OFFSET ");
            sqlBuilder.append(page.getStartRow());
            sqlBuilder.append(" ROWS ");
            pageKey.update(page.getStartRow());
        }
        if (page.getPageSize() > 0) {
            sqlBuilder.append(" FETCH NEXT ");
            sqlBuilder.append(page.getPageSize());
            sqlBuilder.append(" ROWS ONLY");
            pageKey.update(page.getPageSize());
        }
        return sqlBuilder.toString();
    }

}
