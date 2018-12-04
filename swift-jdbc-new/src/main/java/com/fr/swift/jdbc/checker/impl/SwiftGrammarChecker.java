package com.fr.swift.jdbc.checker.impl;

import com.fr.swift.jdbc.checker.GrammarChecker;
import com.fr.swift.jdbc.druid.sql.SQLUtils;
import com.fr.swift.jdbc.exception.Exceptions;
import com.fr.swift.jdbc.info.SqlRequestInfo;
import com.fr.swift.jdbc.sql.SwiftPreparedStatement;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * @author yee
 * @date 2018-12-03
 */
public class SwiftGrammarChecker implements GrammarChecker {
    @Override
    public SqlRequestInfo check(String sql) {
        SQLUtils.parseStatements(sql, null);
        return new SqlRequestInfo(sql);
    }

    @Override
    public SqlRequestInfo check(String sql, List paramValues) throws SQLException {
        return check(getRealSql(sql, paramValues));
    }

    private String getRealSql(String sql, List values) throws SQLException {
        if (values.contains(SwiftPreparedStatement.NullValue.INSTANCE)) {
            throw Exceptions.sql(String.format("Parameter index %d must be set.", values.indexOf(SwiftPreparedStatement.NullValue.INSTANCE) + 1));
        }
        String tmp = sql.trim();
        for (final Object value : values) {
            String valueStr = null;
            if (value instanceof String) {
                valueStr = "'" + value + "'";
            } else if (value instanceof Date) {
                valueStr = String.valueOf(((Date) value).getTime());
            } else {
                valueStr = value.toString();
            }
            tmp = tmp.replaceFirst(SwiftPreparedStatement.VALUE_POS_PATTERN.pattern(), valueStr);
        }
        return tmp;
    }
}
