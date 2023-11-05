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

package com.github.pagehelper.test.basic;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.dialect.auto.DataSourceAutoDialect;
import com.github.pagehelper.dialect.auto.DataSourceNegotiationAutoDialect;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

@Ignore
public class AsyncCountTest {
    private static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            //创建SqlSessionFactory
            Reader reader = Resources.getResourceAsReader("mybatis-config-async-count.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } catch (IOException e) {
        }
        //添加一个针对 unpool 的 AutoDialect
        DataSourceNegotiationAutoDialect.registerAutoDialect(new DataSourceAutoDialect<UnpooledDataSource>() {
            @Override
            public String getJdbcUrl(UnpooledDataSource unpooledDataSource) {
                return unpooledDataSource.getUrl();
            }
        });
    }

    /**
     * 获取Session
     *
     * @return
     */
    public static SqlSession getSqlSession() {
        return sqlSessionFactory.openSession();
    }

    /**
     * 使用命名空间调用时，使用PageHelper.startPage
     * <p/>
     * startPage第三个参数可以控制是(true)否(false)执行count查询，使用两个查询的startPage时默认进行count查询
     * <p/>
     * 使用startPage方法时，如果同时使用RowBounds，以startPage为准
     */
    @Test
    public void testAsyncCount() {
        SqlSession sqlSession = getSqlSession();
        long start = System.currentTimeMillis();
        try {
            //获取第1页，10条内容，默认查询总数count
            PageHelper.startPage(1, 10).enableAsyncCount().keepOrderBy(true);
            sqlSession.selectList("selectEmployees");
            System.out.println("异步耗时: " + (System.currentTimeMillis() - start));
        } finally {
            sqlSession.close();
        }

        sqlSession = getSqlSession();
        start = System.currentTimeMillis();
        try {
            //获取第1页，10条内容，默认查询总数count
            PageHelper.startPage(1, 10).keepOrderBy(true);
            sqlSession.selectList("selectEmployees");
            System.out.println("同步耗时: " + (System.currentTimeMillis() - start));
        } finally {
            sqlSession.close();
        }
    }

}
