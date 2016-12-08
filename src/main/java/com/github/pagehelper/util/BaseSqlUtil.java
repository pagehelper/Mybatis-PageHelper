package com.github.pagehelper.util;

import com.github.pagehelper.Page;
import com.github.pagehelper.cache.Cache;
import com.github.pagehelper.cache.CacheFactory;
import com.github.pagehelper.dialect.*;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author liuzh
 */
public class BaseSqlUtil {
    protected static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<Page>();
    //params参数映射
    protected static Map<String, String> PARAMS = new HashMap<String, String>(5);
    protected static Map<String, Class<?>> dialectAliasMap = new HashMap<String, Class<?>>();
    //request获取方法
    protected static Boolean hasRequest;
    protected static Class<?> requestClass;
    protected static Method getParameterMap;

    static {
        try {
            requestClass = Class.forName("javax.servlet.ServletRequest");
            getParameterMap = requestClass.getMethod("getParameterMap", new Class[]{});
            hasRequest = true;
        } catch (Throwable e) {
            hasRequest = false;
        }
        //注册别名
        dialectAliasMap.put("hsqldb", HsqldbDialect.class);
        dialectAliasMap.put("h2", HsqldbDialect.class);
        dialectAliasMap.put("postgresql", HsqldbDialect.class);

        dialectAliasMap.put("mysql", MySqlDialect.class);
        dialectAliasMap.put("mariadb", MySqlDialect.class);
        dialectAliasMap.put("sqlite", MySqlDialect.class);

        dialectAliasMap.put("oracle", OracleDialect.class);
        dialectAliasMap.put("db2", Db2Dialect.class);
        dialectAliasMap.put("informix", InformixDialect.class);

        dialectAliasMap.put("sqlserver", SqlServerDialect.class);
        dialectAliasMap.put("sqlserver2012", SqlServer2012Dialect.class);
        dialectAliasMap.put("derby", SqlServer2012Dialect.class);
    }

    //缓存count查询的ms
    protected Cache<CacheKey, MappedStatement> msCountMap = null;
    //RowBounds参数offset作为PageNum使用 - 默认不使用
    protected boolean offsetAsPageNum = false;
    //RowBounds是否进行count查询 - 默认不查询
    protected boolean rowBoundsWithCount = false;
    //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
    protected boolean pageSizeZero = false;
    //分页合理化
    protected boolean reasonable = false;
    //是否支持接口参数来传递分页参数，默认false
    protected boolean supportMethodsArguments = false;

    public static String fromJdbcUrl(String jdbcUrl) {
        for (String dialect : dialectAliasMap.keySet()) {
            if (jdbcUrl.indexOf(":" + dialect.toLowerCase() + ":") != -1) {
                return dialect;
            }
        }
        return null;
    }

    /**
     * 反射类
     *
     * @param className
     * @return
     * @throws Exception
     */
    public static Class resloveDialectClass(String className) throws Exception {
        if (dialectAliasMap.containsKey(className.toLowerCase())) {
            return dialectAliasMap.get(className.toLowerCase());
        } else {
            return Class.forName(className);
        }
    }

    public static Boolean getCOUNT() {
        Page page = getLocalPage();
        if (page != null) {
            return page.getCountSignal();
        }
        return null;
    }

    /**
     * 获取Page参数
     *
     * @return
     */
    public static <T> Page<T> getLocalPage() {
        return LOCAL_PAGE.get();
    }

    public static void setLocalPage(Page page) {
        LOCAL_PAGE.set(page);
    }

    /**
     * 移除本地变量
     */
    public static void clearLocalPage() {
        LOCAL_PAGE.remove();
    }

    /**
     * 获取orderBy
     *
     * @return
     */
    public static String getOrderBy() {
        Page<?> page = getLocalPage();
        if (page != null) {
            String orderBy = page.getOrderBy();
            if (StringUtil.isEmpty(orderBy)) {
                return null;
            } else {
                return orderBy;
            }
        }
        return null;
    }

    /**
     * 对象中获取分页参数
     *
     * @param params
     * @return
     */
    public static <T> Page<T> getPageFromObject(Object params) {
        int pageNum;
        int pageSize;
        MetaObject paramsObject = null;
        if (params == null) {
            throw new NullPointerException("无法获取分页查询参数!");
        }
        if (hasRequest && requestClass.isAssignableFrom(params.getClass())) {
            try {
                paramsObject = MetaObjectUtil.forObject(getParameterMap.invoke(params, new Object[]{}));
            } catch (Exception e) {
                //忽略
            }
        } else {
            paramsObject = MetaObjectUtil.forObject(params);
        }
        if (paramsObject == null) {
            throw new NullPointerException("分页查询参数处理失败!");
        }
        Object orderBy = getParamValue(paramsObject, "orderBy", false);
        boolean hasOrderBy = false;
        if (orderBy != null && orderBy.toString().length() > 0) {
            hasOrderBy = true;
        }
        try {
            Object _pageNum = getParamValue(paramsObject, "pageNum", hasOrderBy ? false : true);
            Object _pageSize = getParamValue(paramsObject, "pageSize", hasOrderBy ? false : true);
            if (_pageNum == null || _pageSize == null) {
                Page page = new Page();
                page.setOrderBy(orderBy.toString());
                page.setOrderByOnly(true);
                return page;
            }
            pageNum = Integer.parseInt(String.valueOf(_pageNum));
            pageSize = Integer.parseInt(String.valueOf(_pageSize));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("分页参数不是合法的数字类型!");
        }
        Page page = new Page(pageNum, pageSize);
        //count查询
        Object _count = getParamValue(paramsObject, "count", false);
        if (_count != null) {
            page.setCount(Boolean.valueOf(String.valueOf(_count)));
        }
        //排序
        if (hasOrderBy) {
            page.setOrderBy(orderBy.toString());
        }
        //分页合理化
        Object reasonable = getParamValue(paramsObject, "reasonable", false);
        if (reasonable != null) {
            page.setReasonable(Boolean.valueOf(String.valueOf(reasonable)));
        }
        //查询全部
        Object pageSizeZero = getParamValue(paramsObject, "pageSizeZero", false);
        if (pageSizeZero != null) {
            page.setPageSizeZero(Boolean.valueOf(String.valueOf(pageSizeZero)));
        }
        return page;
    }

    /**
     * 从对象中取参数
     *
     * @param paramsObject
     * @param paramName
     * @param required
     * @return
     */
    public static Object getParamValue(MetaObject paramsObject, String paramName, boolean required) {
        Object value = null;
        if (paramsObject.hasGetter(PARAMS.get(paramName))) {
            value = paramsObject.getValue(PARAMS.get(paramName));
        }
        if (value != null && value.getClass().isArray()) {
            Object[] values = (Object[]) value;
            if (values.length == 0) {
                value = null;
            } else {
                value = values[0];
            }
        }
        if (required && value == null) {
            throw new RuntimeException("分页查询缺少必要的参数:" + PARAMS.get(paramName));
        }
        return value;
    }

    public void setParams(String params) {
        PARAMS.put("pageNum", "pageNum");
        PARAMS.put("pageSize", "pageSize");
        PARAMS.put("count", "countSql");
        PARAMS.put("orderBy", "orderBy");
        PARAMS.put("reasonable", "reasonable");
        PARAMS.put("pageSizeZero", "pageSizeZero");
        if (StringUtil.isNotEmpty(params)) {
            String[] ps = params.split("[;|,|&]");
            for (String s : ps) {
                String[] ss = s.split("[=|:]");
                if (ss.length == 2) {
                    PARAMS.put(ss[0], ss[1]);
                }
            }
        }
    }

    /**
     * 获取分页参数
     *
     * @param parameterObject
     * @param rowBounds
     * @return
     */
    public Page getPage(Object parameterObject, RowBounds rowBounds) {
        Page page = getLocalPage();
        if (page == null || page.isOrderByOnly()) {
            Page oldPage = page;
            //这种情况下,page.isOrderByOnly()必然为true，所以不用写到条件中
            if ((rowBounds == null || rowBounds == RowBounds.DEFAULT) && page != null) {
                return oldPage;
            }
            if (rowBounds != RowBounds.DEFAULT) {
                if (offsetAsPageNum) {
                    page = new Page(rowBounds.getOffset(), rowBounds.getLimit(), rowBoundsWithCount);
                } else {
                    page = new Page(new int[]{rowBounds.getOffset(), rowBounds.getLimit()}, rowBoundsWithCount);
                    //offsetAsPageNum=false的时候，由于PageNum问题，不能使用reasonable，这里会强制为false
                    page.setReasonable(false);
                }
            } else {
                try {
                    page = getPageFromObject(parameterObject);
                } catch (Exception e) {
                    return null;
                }
            }
            if (oldPage != null) {
                page.setOrderBy(oldPage.getOrderBy());
            }
            setLocalPage(page);
        }
        //分页合理化
        if (page.getReasonable() == null) {
            page.setReasonable(reasonable);
        }
        //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页，返回全部结果
        if (page.getPageSizeZero() == null) {
            page.setPageSizeZero(pageSizeZero);
        }
        return page;
    }

    public void setOffsetAsPageNum(boolean offsetAsPageNum) {
        this.offsetAsPageNum = offsetAsPageNum;
    }

    public void setRowBoundsWithCount(boolean rowBoundsWithCount) {
        this.rowBoundsWithCount = rowBoundsWithCount;
    }

    public void setPageSizeZero(boolean pageSizeZero) {
        this.pageSizeZero = pageSizeZero;
    }

    public void setReasonable(boolean reasonable) {
        this.reasonable = reasonable;
    }

    public void setSupportMethodsArguments(boolean supportMethodsArguments) {
        this.supportMethodsArguments = supportMethodsArguments;
    }

    public void setProperties(Properties p) {
        //缓存 count ms
        msCountMap = CacheFactory.createSqlCache(p.getProperty("msCountCache"), "ms", p);
        //offset作为PageNum使用
        String offsetAsPageNum = p.getProperty("offsetAsPageNum");
        this.offsetAsPageNum = Boolean.parseBoolean(offsetAsPageNum);
        //RowBounds方式是否做count查询
        String rowBoundsWithCount = p.getProperty("rowBoundsWithCount");
        this.rowBoundsWithCount = Boolean.parseBoolean(rowBoundsWithCount);
        //当设置为true的时候，如果pagesize设置为0（或RowBounds的limit=0），就不执行分页
        String pageSizeZero = p.getProperty("pageSizeZero");
        this.pageSizeZero = Boolean.parseBoolean(pageSizeZero);
        //分页合理化，true开启，如果分页参数不合理会自动修正。默认false不启用
        String reasonable = p.getProperty("reasonable");
        this.reasonable = Boolean.parseBoolean(reasonable);
        //是否支持接口参数来传递分页参数，默认false
        String supportMethodsArguments = p.getProperty("supportMethodsArguments");
        this.supportMethodsArguments = Boolean.parseBoolean(supportMethodsArguments);
        //当offsetAsPageNum=false的时候，不能
        //参数映射
        setParams(p.getProperty("params"));
    }
}
