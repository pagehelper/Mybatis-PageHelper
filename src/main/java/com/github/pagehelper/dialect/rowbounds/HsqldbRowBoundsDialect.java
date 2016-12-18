package com.github.pagehelper.dialect.rowbounds;

import com.github.pagehelper.dialect.AbstractRowBoundsDialect;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.session.RowBounds;

/**
 * hsqldb 基于 RowBounds 的分页
 *
 * @author liuzh
 */
public class HsqldbRowBoundsDialect extends AbstractRowBoundsDialect {

    @Override
    public String getPageSql(String sql, RowBounds rowBounds, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 20);
        sqlBuilder.append(sql);
        if (rowBounds.getLimit() > 0) {
            sqlBuilder.append(" LIMIT ");
            sqlBuilder.append(rowBounds.getLimit());
            pageKey.update(rowBounds.getLimit());
        }
        if (rowBounds.getOffset() > 0) {
            sqlBuilder.append(" OFFSET ");
            sqlBuilder.append(rowBounds.getOffset());
            pageKey.update(rowBounds.getOffset());
        }
        return sqlBuilder.toString();
    }
}
