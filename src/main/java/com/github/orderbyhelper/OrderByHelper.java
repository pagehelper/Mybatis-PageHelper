package com.github.orderbyhelper;

import com.github.orderbyhelper.sqlsource.*;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * 排序辅助类
 *
 * @author liuzh
 * @since 2015-06-26
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class OrderByHelper implements Interceptor {
    private static final ThreadLocal<String> ORDER_BY = new ThreadLocal<String>();

    public static String getOrderBy() {
        String orderBy = ORDER_BY.get();
        if (orderBy == null || orderBy.length() == 0) {
            return null;
        }
        return orderBy;
    }

    /**
     * 增加排序
     *
     * @param orderBy
     */
    public static void orderBy(String orderBy) {
        ORDER_BY.set(orderBy);
    }

    /**
     * 清除本地变量
     */
    public static void clear() {
        ORDER_BY.remove();
    }

    /**
     * 是否已经处理过
     *
     * @param ms
     * @return
     */
    public static boolean hasOrderBy(MappedStatement ms) {
        if (ms.getSqlSource() instanceof OrderBySqlSource) {
            return true;
        }
        return false;
    }

    /**
     * 不支持注解形式(ProviderSqlSource)的增加order by
     *
     * @param invocation
     * @throws Throwable
     */
    public static void processIntercept(Invocation invocation) throws Throwable {
        final Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        if (!hasOrderBy(ms)) {
            MetaObject msObject = SystemMetaObject.forObject(ms);
            //判断是否自带order by，自带的情况下作为默认排序
            SqlSource sqlSource = ms.getSqlSource();
            if (sqlSource instanceof StaticSqlSource) {
                msObject.setValue("sqlSource", new OrderByStaticSqlSource((StaticSqlSource) sqlSource));
            } else if (sqlSource instanceof RawSqlSource) {
                msObject.setValue("sqlSource", new OrderByRawSqlSource((RawSqlSource) sqlSource));
            } else if (sqlSource instanceof ProviderSqlSource) {
                msObject.setValue("sqlSource", new OrderByProviderSqlSource((ProviderSqlSource) sqlSource));
            } else if (sqlSource instanceof DynamicSqlSource) {
                msObject.setValue("sqlSource", new OrderByDynamicSqlSource((DynamicSqlSource) sqlSource));
            } else {
                throw new RuntimeException("无法处理该类型[" + sqlSource.getClass() + "]的SqlSource");
            }
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            if (getOrderBy() != null) {
                processIntercept(invocation);
            }
            return invocation.proceed();
        } finally {
            clear();
        }
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
