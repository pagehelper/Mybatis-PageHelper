package com.github.pagehelper.test.basic.dynamic;

import java.util.Map;

/**
 * Created by liuzh on 2015/1/11.
 */
public class Where {
    private Map<String,Object> map;

    public Where(Map<String, Object> map) {
        this.map = map;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }
}
