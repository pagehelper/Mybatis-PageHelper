package com.github.orderbyhelper.mapper;

/**
 * 描述信息
 *
 * @author liuzh
 * @since 2015-06-27
 */
public class OrderCountry {
    private Integer id;
    private String countryname;
    private String countrycode;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCountryname() {
        return countryname;
    }

    public void setCountryname(String countryname) {
        this.countryname = countryname;
    }

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }
}
