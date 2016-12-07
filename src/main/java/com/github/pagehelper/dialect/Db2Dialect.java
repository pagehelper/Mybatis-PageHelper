package com.github.pagehelper.dialect;

import com.github.pagehelper.Page;
import com.github.pagehelper.util.MetaObjectUtil;
import com.github.pagehelper.util.SqlUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liuzh
 */
public class Db2Dialect extends AbstractDialect {
    public Db2Dialect(SqlUtil sqlUtil) {
        super(sqlUtil);
    }

    @Override
    public Object processPageParameter(MappedStatement ms, Map<String, Object> paramMap, Page page, BoundSql boundSql, CacheKey pageKey) {
        paramMap.put(PAGEPARAMETER_FIRST, page.getStartRow() + 1);
        paramMap.put(PAGEPARAMETER_SECOND, page.getEndRow());
        //处理pageKey
        pageKey.update(page.getStartRow() + 1);
        pageKey.update(page.getEndRow());
        //处理参数配置
        if (boundSql.getParameterMappings() != null) {
            List<ParameterMapping> newParameterMappings = new ArrayList<ParameterMapping>();
            if (boundSql != null && boundSql.getParameterMappings() != null) {
                newParameterMappings.addAll(boundSql.getParameterMappings());
            }
            newParameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_FIRST, Integer.class).build());
            newParameterMappings.add(new ParameterMapping.Builder(ms.getConfiguration(), PAGEPARAMETER_SECOND, Integer.class).build());
            MetaObject metaObject = MetaObjectUtil.forObject(boundSql);
            metaObject.setValue("parameterMappings", newParameterMappings);
        }
        return paramMap;
    }

    @Override
    public String getPageSql(String sql, Page page, RowBounds rowBounds, CacheKey pageKey) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
        sqlBuilder.append("select * from (select tmp_page.*,rownumber() over() as row_id from ( ");
        sqlBuilder.append(sql);
        sqlBuilder.append(" ) as tmp_page) where row_id between ? and ?");
        return sqlBuilder.toString();
    }

}
