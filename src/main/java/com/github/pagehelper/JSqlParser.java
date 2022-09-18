/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2022 abel533@gmail.com
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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

/**
 * 为了兼容不同版本 jdk 和 jsqlparser
 * <p>
 * 使用 sqlserver 时，可以使用下面方式支持 `[]`
 * <pre>
 * JSqlParser SQLSERVER_PARSER = new JSqlParser() {
 *      @Override
 *      public Statement parse(String statementReader) throws JSQLParserException {
 *          return CCJSqlParserUtil.parse(statementReader, parser -> parser.withSquareBracketQuotation(true));
 *      }
 *  }
 *  </pre>
 *
 * @author liuzh
 */
public interface JSqlParser {

    /**
     * 默认实现
     */
    JSqlParser DEFAULT = new JSqlParser() {
        @Override
        public Statement parse(String statementReader) throws JSQLParserException {
            return CCJSqlParserUtil.parse(statementReader);
        }
    };

    /**
     * 解析 SQL
     *
     * @param statementReader SQL
     * @return
     * @throws JSQLParserException
     */
    Statement parse(String statementReader) throws JSQLParserException;

}
