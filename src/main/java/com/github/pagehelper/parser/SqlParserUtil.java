package com.github.pagehelper.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statement;

import java.util.ServiceLoader;

public class SqlParserUtil {

    private static final SqlParser SQL_PARSER;

    static {
        SqlParser temp = null;
        ServiceLoader<SqlParser> loader = ServiceLoader.load(SqlParser.class);
        for (SqlParser sqlParser : loader) {
            temp = sqlParser;
            break;
        }
        if (temp == null) {
            temp = SqlParser.DEFAULT;
        }
        SQL_PARSER = temp;
    }

    public static Statement parse(String statementReader) {
        try {
            return SQL_PARSER.parse(statementReader);
        } catch (JSQLParserException | ParseException e) {
            throw new RuntimeException(e);
        }
    }


}
