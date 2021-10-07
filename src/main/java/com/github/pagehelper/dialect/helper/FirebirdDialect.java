package com.github.pagehelper.dialect.helper;

import com.github.pagehelper.Page;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Map;

/**
 * firebirdsql数据库
 * <ul>
 * <li>https://firebirdsql.github.io/jaybird-manual/jaybird_manual.html</li>
 * <li>https://firebirdsql.org/file/documentation/chunk/en/refdocs/fblangref40/fblangref40-dml.html#fblangref40-dml-select-offsetfetch</li>
 * </ul>
 *
 * @author liuzh
 */
public class FirebirdDialect extends AbstractHelperDialect {

    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey) {
        paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow());
        paramMap.put(PAGEPARAMETER_SECOND, page.getPageSize());
        //处理pageKey
        pageKey.update(page.getStartRow());
        pageKey.update(page.getPageSize());
        //处理参数配置
        handleParameter(boundSql, ms, long.class, int.class);
        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 64);
        sqlBuilder.append(sql);
        sqlBuilder.append("\n OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        pageKey.update(page.getPageSize());
        return sqlBuilder.toString();
    }

}
