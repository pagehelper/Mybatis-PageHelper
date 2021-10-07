package com.github.pagehelper.dialect.auto;

import com.github.pagehelper.AutoDialect;
import com.github.pagehelper.PageException;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.page.PageAutoDialect;
import com.github.pagehelper.util.StringUtil;
import org.apache.ibatis.mapping.MappedStatement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 最初的默认实现，获取连接再获取 url，这种方式通用性强，但是性能低，处理不好关闭连接时容易出问题
 *
 * @author liuzh
 */
public class DefaultAutoDialect implements AutoDialect<String> {

    public static final AutoDialect<String> DEFAULT = new DefaultAutoDialect();

    @Override
    public String extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return conn.getMetaData().getURL();
        } catch (SQLException e) {
            throw new PageException(e);
        } finally {
            if (conn != null) {
                try {
                    String closeConn = properties.getProperty("closeConn");
                    if (StringUtil.isEmpty(closeConn) || Boolean.parseBoolean(closeConn)) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    //ignore
                }
            }
        }
    }

    @Override
    public AbstractHelperDialect extractDialect(String dialectKey, MappedStatement ms, DataSource dataSource, Properties properties) {
        String dialectStr = PageAutoDialect.fromJdbcUrl(dialectKey);
        if (dialectStr == null) {
            throw new PageException("无法自动获取数据库类型，请通过 helperDialect 参数指定!");
        }
        return PageAutoDialect.instanceDialect(dialectStr, properties);
    }
}
