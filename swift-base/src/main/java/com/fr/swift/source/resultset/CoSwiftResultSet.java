package com.fr.swift.source.resultset;

import com.fr.swift.source.Row;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftResultSet;
import com.fr.swift.util.Assert;

import java.sql.SQLException;
import java.util.List;

/**
 * @author anchore
 * @date 2018/7/27
 * <p>
 * 将多个result set合成一个
 */
public class CoSwiftResultSet implements SwiftResultSet {
    private List<SwiftResultSet> resultSets;

    private int cursor;

    public CoSwiftResultSet(List<SwiftResultSet> resultSets) {
        Assert.notEmpty(resultSets);
        this.resultSets = resultSets;
    }

    @Override
    public int getFetchSize() {
        return 0;
    }

    @Override
    public SwiftMetaData getMetaData() throws SQLException {
        return resultSets.get(0).getMetaData();
    }

    @Override
    public boolean hasNext() throws SQLException {
        if (!resultSets.get(cursor).hasNext()) {
            do {
                resultSets.get(cursor).close();
                cursor++;
            } while (cursor < resultSets.size() && !resultSets.get(cursor).hasNext());

            return cursor < resultSets.size();
        }
        return true;
    }

    @Override
    public Row getNextRow() throws SQLException {
        return resultSets.get(cursor).getNextRow();
    }

    @Override
    public void close() {
    }
}