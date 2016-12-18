package com.github.pagehelper.helper.dialect;

import com.github.pagehelper.Page;
import com.github.pagehelper.helper.HelperDialect;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.session.RowBounds;

/**
 * @author liuzh
 */
public class HsqldbDialect extends HelperDialect {

    @Override
    public String getPageSql(String sql, Page page, RowBounds rowBounds, CacheKey pageKey) {
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
