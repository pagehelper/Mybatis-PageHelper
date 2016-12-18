package com.github.pagehelper.dialect.rowbounds;

import com.github.pagehelper.Dialect;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageRowBounds;
import com.github.pagehelper.parser.CountSqlParser;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

import java.util.List;
import java.util.Properties;

/**
 * mysql 基于 RowBounds 的分页
 *
 * @author liuzh
 */
public class MySqlRowBoundsDialect extends AbstractRowBoundsDialect {

    @Override
    public String getPageSql(String sql, RowBounds rowBounds, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 14);
        sqlBuilder.append(sql);
        if (rowBounds.getOffset() == 0) {
            sqlBuilder.append(" LIMIT ");
            sqlBuilder.append(rowBounds.getLimit());
        } else {
            sqlBuilder.append(" LIMIT ");
            sqlBuilder.append(rowBounds.getOffset());
            sqlBuilder.append(",");
            sqlBuilder.append(rowBounds.getLimit());
            pageKey.update(rowBounds.getOffset());
        }
        pageKey.update(rowBounds.getLimit());
        return sqlBuilder.toString();
    }

}
