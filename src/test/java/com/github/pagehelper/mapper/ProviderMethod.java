package com.github.pagehelper.mapper;

import com.github.pagehelper.model.Country;

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
        sbSql.append("order by id");
        return sbSql.toString();
    }

    public String selectCountry(Country country) {
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("select * from country where 1=1 ");
        if (country.getId() > 0) {
            sbSql.append(" and id = #{id} ");
        }
        if (country.getCountrycode() != null) {
            sbSql.append(" and countrycode = #{countrycode} ");
        }
        if (country.getCountryname() != null) {
            sbSql.append(" and countryname = #{countryname} ");
        }
        sbSql.append("order by id");
        return sbSql.toString();
    }
}
