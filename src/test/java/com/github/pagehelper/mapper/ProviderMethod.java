package com.github.pagehelper.mapper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuzh
 */
public class ProviderMethod {

    public String select(Map<String, Object> map) {
        Map<String, Object> param = (HashMap<String, Object>) map.get("param");
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("select * from country where 1=1 ");
        int index = 0;
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            sbSql.append(" and "+entry.getKey() + "= #{param." + entry.getKey() + "} ");
        }
        return sbSql.toString();
    }
}
