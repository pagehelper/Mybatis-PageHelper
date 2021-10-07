package com.github.pagehelper.dialect.auto;

import com.github.pagehelper.AutoDialect;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 遍历所有实现，找到匹配的实现
 *
 * @author liuzh
 */
public class DataSourceNegotiationAutoDialect implements AutoDialect<String> {
    public static final Log log = LogFactory.getLog(DataSourceNegotiationAutoDialect.class);
    private static final List<DataSourceAutoDialect> autoDialects = new ArrayList<DataSourceAutoDialect>();
    private Map<String, DataSourceAutoDialect> urlMap = new ConcurrentHashMap<String, DataSourceAutoDialect>();

    /**
     * 创建时，初始化所有实现，当依赖的连接池不存在时，这里不会添加成功，所以理论上这里包含的内容不会多，执行时不会迭代多次
     */
    public DataSourceNegotiationAutoDialect() {
        try {
            autoDialects.add(new HikariAutoDialect());
            log.debug("HikariAutoDialect 初始化成功");
        } catch (Exception ignore) {
        }
        try {
            autoDialects.add(new DruidAutoDialect());
            log.debug("DruidAutoDialect 初始化成功");
        } catch (Exception ignore) {
        }
        try {
            autoDialects.add(new TomcatAutoDialect());
            log.debug("TomcatAutoDialect 初始化成功");
        } catch (Exception ignore) {
        }
        try {
            autoDialects.add(new C3P0AutoDialect());
            log.debug("C3P0AutoDialect 初始化成功");
        } catch (Exception ignore) {
        }
        try {
            autoDialects.add(new DbcpAutoDialect());
            log.debug("DbcpAutoDialect 初始化成功");
        } catch (Exception ignore) {
        }
    }

    /**
     * 允许手工添加额外的实现，实际上没有必要
     *
     * @param autoDialect
     */
    public static void registerAutoDialect(DataSourceAutoDialect autoDialect) {
        autoDialects.add(autoDialect);
    }

    @Override
    public String extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties) {
        for (DataSourceAutoDialect autoDialect : autoDialects) {
            String dialectKey = autoDialect.extractDialectKey(ms, dataSource, properties);
            if (dialectKey != null) {
                if (!urlMap.containsKey(dialectKey)) {
                    urlMap.put(dialectKey, autoDialect);
                }
                return dialectKey;
            }
        }
        //都不匹配的时候使用默认方式
        return DefaultAutoDialect.DEFAULT.extractDialectKey(ms, dataSource, properties);
    }

    @Override
    public AbstractHelperDialect extractDialect(String dialectKey, MappedStatement ms, DataSource dataSource, Properties properties) {
        if (urlMap.containsKey(dialectKey)) {
            return urlMap.get(dialectKey).extractDialect(dialectKey, ms, dataSource, properties);
        }
        //都不匹配的时候使用默认方式
        return DefaultAutoDialect.DEFAULT.extractDialect(dialectKey, ms, dataSource, properties);
    }

}
