package com.github.pagehelper.test.oracle;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;

/**
 * Description: MybatisHelper
 * Author: liuzh
 * Update: liuzh(2014-06-06 13:33)
 */
public class OracleMybatisHelper {

    private static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            //创建SqlSessionFactory
            Reader reader = Resources.getResourceAsReader("oracle/mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Session
     * @return
     */
    public static SqlSession getSqlSession(){
        return sqlSessionFactory.openSession();
    }
}
