package com.github.pagehelper;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * 构建当前查询对应的 count 方法 id
 * <p>
 * 返回的 msId 会先判断是否存在自定义的方法，存在就直接使用
 * <p>
 * 如果不存在，会根据当前的 msId 创建 MappedStatement
 *
 * @author liuzh
 */
public interface CountMsIdGen {

    /**
     * 默认实现
     */
    CountMsIdGen DEFAULT = new CountMsIdGen() {
        @Override
        public String genCountMsId(MappedStatement ms, Object parameter, BoundSql boundSql, String countSuffix) {
            return ms.getId() + countSuffix;
        }
    };

    /**
     * 构建当前查询对应的 count 方法 id
     *
     * @param ms          查询对应的 MappedStatement
     * @param parameter   方法参数
     * @param boundSql    查询SQL
     * @param countSuffix 配置的 count 后缀
     * @return count 查询丢的 msId
     */
    String genCountMsId(MappedStatement ms, Object parameter,
                        BoundSql boundSql, String countSuffix);

}
