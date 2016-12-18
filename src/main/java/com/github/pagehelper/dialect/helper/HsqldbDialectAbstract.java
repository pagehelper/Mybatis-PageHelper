package com.github.pagehelper.dialect.helper;

import com.github.pagehelper.Page;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import org.apache.ibatis.cache.CacheKey;

/**
 * @author liuzh
 */
public class HsqldbDialectAbstract extends AbstractHelperDialect {

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 20);
        sqlBuilder.append(sql);
        if (page.getPageSize() > 0) {
            sqlBuilder.append(" LIMIT ");
            sqlBuilder.append(page.getPageSize());
            pageKey.update(page.getPageSize());
        }
        if (page.getStartRow() > 0) {
            sqlBuilder.append(" OFFSET ");
            sqlBuilder.append(page.getStartRow());
            pageKey.update(page.getStartRow());
        }
        return sqlBuilder.toString();
    }
}
