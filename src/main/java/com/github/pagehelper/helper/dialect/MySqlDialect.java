package com.github.pagehelper.helper.dialect;

import com.github.pagehelper.Page;
import com.github.pagehelper.helper.HelperDialect;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.session.RowBounds;

/**
 * @author liuzh
 */
public class MySqlDialect extends HelperDialect {

    @Override
    public String getPageSql(String sql, Page page, RowBounds rowBounds, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 14);
        sqlBuilder.append(sql);
        if (page.getStartRow() == 0) {
            sqlBuilder.append(" LIMIT ");
            sqlBuilder.append(page.getPageSize());
        } else {
            sqlBuilder.append(" LIMIT ");
            sqlBuilder.append(page.getStartRow());
            sqlBuilder.append(",");
            sqlBuilder.append(page.getPageSize());
            pageKey.update(page.getStartRow());
        }
        pageKey.update(page.getPageSize());
        return sqlBuilder.toString();
    }

}
