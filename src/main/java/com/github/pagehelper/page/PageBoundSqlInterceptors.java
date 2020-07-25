package com.github.pagehelper.page;

import com.github.pagehelper.BoundSqlInterceptor;
import com.github.pagehelper.BoundSqlInterceptorChain;
import com.github.pagehelper.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PageBoundSqlInterceptors {

    private BoundSqlInterceptor.Chain chain;

    public void setProperties(Properties properties) {
        //初始化 boundSqlInterceptorChain
        String boundSqlInterceptorStr = properties.getProperty("boundSqlInterceptors");
        if (StringUtil.isNotEmpty(boundSqlInterceptorStr)) {
            String[] boundSqlInterceptors = boundSqlInterceptorStr.split("[;|,]");
            List<BoundSqlInterceptor> list = new ArrayList<BoundSqlInterceptor>();
            for (int i = 0; i < boundSqlInterceptors.length; i++) {
                try {
                    list.add((BoundSqlInterceptor) Class.forName(boundSqlInterceptors[i]).newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (list.size() > 0) {
                chain = new BoundSqlInterceptorChain(null, list);
            }
        }
    }

    public BoundSqlInterceptor.Chain getChain() {
        return chain;
    }
}
