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
