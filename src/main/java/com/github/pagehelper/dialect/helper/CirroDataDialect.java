package com.github.pagehelper.dialect.helper;

import com.github.pagehelper.Page;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Map;

/**
 * @author sxh
 */
public class CirroDataDialect extends AbstractHelperDialect {
    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey) {
        paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow() + 1);
        paramMap.put(PAGEPARAMETER_SECOND, page.getEndRow());
        //处理pageKey
        pageKey.update(page.getStartRow() + 1);
        pageKey.update(page.getEndRow());
        //处理参数配置
        handleParameter(boundSql, ms, long.class, long.class);
        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 16);
        sqlBuilder.append(sql);
        sqlBuilder.append("\n LIMIT ( ?, ? )");
        return sqlBuilder.toString();
    }

}
