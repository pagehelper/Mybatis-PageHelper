/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2023 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.pagehelper;

import com.github.pagehelper.cache.Cache;
import com.github.pagehelper.cache.CacheFactory;
import com.github.pagehelper.page.PageMethod;
import com.github.pagehelper.util.ClassUtil;
import com.github.pagehelper.util.ExecutorUtil;
import com.github.pagehelper.util.MSUtils;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.SimpleExecutor;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * Mybatis - 通用分页拦截器
 * <p>
 * GitHub: https://github.com/pagehelper/Mybatis-PageHelper
 * <p>
 * Gitee : https://gitee.com/free/Mybatis_PageHelper
 *
 * @author liuzh/abel533/isea533
 * @version 5.0.0
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        }
)
public class PageInterceptor implements Interceptor {
    private static final Log                            log                   = LogFactory.getLog(PageInterceptor.class);
    private static       boolean                        debug                 = false;
    protected            Cache<String, MappedStatement> msCountMap            = null;
    protected            CountMsIdGen                   countMsIdGen          = CountMsIdGen.DEFAULT;
    private volatile     Dialect                        dialect;
    private              String                         countSuffix           = "_COUNT";
    private              String                         default_dialect_class = "com.github.pagehelper.PageHelper";

    public PageInterceptor() {
        String bannerEnabled = System.getProperty("pagehelper.banner");
        if (StringUtil.isEmpty(bannerEnabled)) {
            bannerEnabled = System.getenv("PAGEHELPER_BANNER");
        }
        //默认 TRUE
        if (StringUtil.isEmpty(bannerEnabled) || Boolean.parseBoolean(bannerEnabled)) {
            log.debug("\n\n" +
                    ",------.                           ,--.  ,--.         ,--.                         \n" +
                    "|  .--. '  ,--,--.  ,---.   ,---.  |  '--'  |  ,---.  |  |  ,---.   ,---.  ,--.--. \n" +
                    "|  '--' | ' ,-.  | | .-. | | .-. : |  .--.  | | .-. : |  | | .-. | | .-. : |  .--' \n" +
                    "|  | --'  \\ '-'  | ' '-' ' \\   --. |  |  |  | \\   --. |  | | '-' ' \\   --. |  |    \n" +
                    "`--'       `--`--' .`-  /   `----' `--'  `--'  `----' `--' |  |-'   `----' `--'    \n" +
                    "                   `---'                                   `--'                        is intercepting.\n");
        }
    }

    public static boolean isDebug() {
        return debug;
    }

    /**
     * 输出启用分页方法时的调用堆栈信息
     */
    protected void debugStackTraceLog() {
        if (isDebug()) {
            Page<Object> page = PageMethod.getLocalPage();
            log.debug(page.getStackTrace());
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        try {
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            Object parameter = args[1];
            RowBounds rowBounds = (RowBounds) args[2];
            ResultHandler resultHandler = (ResultHandler) args[3];
            Executor executor = (Executor) invocation.getTarget();
            CacheKey cacheKey;
            BoundSql boundSql;
            //由于逻辑关系，只会进入一次
            if (args.length == 4) {
                //4 个参数时
                boundSql = ms.getBoundSql(parameter);
                cacheKey = executor.createCacheKey(ms, parameter, rowBounds, boundSql);
            } else {
                //6 个参数时
                cacheKey = (CacheKey) args[4];
                boundSql = (BoundSql) args[5];
            }
            checkDialectExists();
            //对 boundSql 的拦截处理
            if (dialect instanceof BoundSqlInterceptor.Chain) {
                boundSql = ((BoundSqlInterceptor.Chain) dialect).doBoundSql(BoundSqlInterceptor.Type.ORIGINAL, boundSql, cacheKey);
            }
            List resultList;
            //调用方法判断是否需要进行分页，如果不需要，直接返回结果
            if (!dialect.skip(ms, parameter, rowBounds)) {
                //开启debug时，输出触发当前分页执行时的PageHelper调用堆栈
                // 如果和当前调用堆栈不一致，说明在启用分页后没有消费，当前线程再次执行时消费，调用堆栈显示的方法使用不安全
                debugStackTraceLog();
                Future<Long> countFuture = null;
                //判断是否需要进行 count 查询
                if (dialect.beforeCount(ms, parameter, rowBounds)) {
                    if (dialect.isAsyncCount()) {
                        countFuture = asyncCount(ms, boundSql, parameter, rowBounds);
                    } else {
                        //查询总数
                        Long count = count(executor, ms, parameter, rowBounds, null, boundSql);
                        //处理查询总数，返回 true 时继续分页查询，false 时直接返回
                        if (!dialect.afterCount(count, parameter, rowBounds)) {
                            //当查询总数为 0 时，直接返回空的结果
                            return dialect.afterPage(new ArrayList(), parameter, rowBounds);
                        }
                    }
                }
                resultList = ExecutorUtil.pageQuery(dialect, executor,
                        ms, parameter, rowBounds, resultHandler, boundSql, cacheKey);
                if (countFuture != null) {
                    Long count = countFuture.get();
                    dialect.afterCount(count, parameter, rowBounds);
                }
            } else {
                //rowBounds用参数值，不使用分页插件处理时，仍然支持默认的内存分页
                resultList = executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
            }
            return dialect.afterPage(resultList, parameter, rowBounds);
        } finally {
            if (dialect != null) {
                dialect.afterAll();
            }
        }
    }

    /**
     * 异步查询总数
     */
    private Future<Long> asyncCount(MappedStatement ms, BoundSql boundSql, Object parameter, RowBounds rowBounds) {
        Configuration configuration = ms.getConfiguration();
        //异步不能复用 BoundSql，因为分页使用时会添加分页参数，这里需要复制一个新的
        BoundSql countBoundSql = new BoundSql(configuration, boundSql.getSql(), new ArrayList<>(boundSql.getParameterMappings()), parameter);
        try{
           for (String key : boundSql.getAdditionalParameters().keySet()) {
                countBoundSql.setAdditionalParameter(key, boundSql.getAdditionalParameters().get(key));
            }
        }catch(Excet
            
        //异步想要起作用需要新的数据库连接，需要独立的事务，创建新的Executor，因此异步查询只适合在独立查询中使用，如果混合增删改操作，不能开启异步
        Environment environment = configuration.getEnvironment();
        TransactionFactory transactionFactory = null;
        if (environment == null || environment.getTransactionFactory() == null) {
            transactionFactory = new ManagedTransactionFactory();
        } else {
            transactionFactory = environment.getTransactionFactory();
        }
        //创建新的事务
        Transaction tx = transactionFactory.newTransaction(environment.getDataSource(), null, false);
        //使用新的 Executor 执行 count 查询，这里没有加载拦截器，避免递归死循环
        Executor countExecutor = new CachingExecutor(new SimpleExecutor(configuration, tx));

        return dialect.asyncCountTask(() -> {
            try {
                return count(countExecutor, ms, parameter, rowBounds, null, countBoundSql);
            } finally {
                tx.close();
            }
        });
    }

    /**
     * Spring bean 方式配置时，如果没有配置属性就不会执行下面的 setProperties 方法，就不会初始化
     * <p>
     * 因此这里会出现 null 的情况 fixed #26
     */
    private void checkDialectExists() {
        if (dialect == null) {
            synchronized (default_dialect_class) {
                if (dialect == null) {
                    setProperties(new Properties());
                }
            }
        }
    }

    private Long count(Executor executor, MappedStatement ms, Object parameter,
                       RowBounds rowBounds, ResultHandler resultHandler,
                       BoundSql boundSql) throws SQLException {
        String countMsId = countMsIdGen.genCountMsId(ms, parameter, boundSql, countSuffix);
        Long count;
        //先判断是否存在手写的 count 查询
        MappedStatement countMs = ExecutorUtil.getExistedMappedStatement(ms.getConfiguration(), countMsId);
        if (countMs != null) {
            count = ExecutorUtil.executeManualCount(executor, countMs, parameter, boundSql, resultHandler);
        } else {
            if (msCountMap != null) {
                countMs = msCountMap.get(countMsId);
            }
            //自动创建
            if (countMs == null) {
                //根据当前的 ms 创建一个返回值为 Long 类型的 ms
                countMs = MSUtils.newCountMappedStatement(ms, countMsId);
                if (msCountMap != null) {
                    msCountMap.put(countMsId, countMs);
                }
            }
            count = ExecutorUtil.executeAutoCount(this.dialect, executor, countMs, parameter, boundSql, rowBounds, resultHandler);
        }
        return count;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        //缓存 count ms
        msCountMap = CacheFactory.createCache(properties.getProperty("msCountCache"), "ms", properties);
        String dialectClass = properties.getProperty("dialect");
        if (StringUtil.isEmpty(dialectClass)) {
            dialectClass = default_dialect_class;
        }
        Dialect tempDialect = ClassUtil.newInstance(dialectClass, properties);
        tempDialect.setProperties(properties);

        String countSuffix = properties.getProperty("countSuffix");
        if (StringUtil.isNotEmpty(countSuffix)) {
            this.countSuffix = countSuffix;
        }

        // debug模式，用于排查不安全分页调用
        debug = Boolean.parseBoolean(properties.getProperty("debug"));

        // 通过 countMsId 配置自定义类
        String countMsIdGenClass = properties.getProperty("countMsIdGen");
        if (StringUtil.isNotEmpty(countMsIdGenClass)) {
            countMsIdGen = ClassUtil.newInstance(countMsIdGenClass, properties);
        }
        // 初始化完成后再设置值，保证 dialect 完成初始化
        dialect = tempDialect;
    }

}
