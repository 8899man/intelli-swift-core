package com.fr.swift.source;

import com.fr.swift.util.Assert;

import java.sql.SQLException;

/**
 * @author anchore
 * @date 2018/6/5
 */
public class LimitedResultSet implements SwiftResultSet {
    private SwiftResultSet origin;

    private boolean closeOrigin;

    private int cursor = 0;

    private int limit;

    public LimitedResultSet(SwiftResultSet origin, int limit) {
        this(origin, limit, true);
    }

    public LimitedResultSet(SwiftResultSet origin, int limit, boolean closeOrigin) {
        Assert.isTrue(limit >= 0, "limit must be greater than or equal 0");
        this.origin = origin;
        this.limit = limit;
        this.closeOrigin = closeOrigin;
    }

    @Override
    public int getFetchSize() {
        return 0;
    }

    @Override
    public SwiftMetaData getMetaData() throws SQLException {
        return origin.getMetaData();
    }

    @Override
    public boolean hasNext() throws SQLException {
        return cursor < limit && origin.hasNext();
    }

    @Override
    public Row getNextRow() throws SQLException {
        cursor++;
        return origin.getNextRow();
    }

    @Override
    public void close() throws SQLException {
        if (closeOrigin) {
            origin.close();
        }
    }
}