package com.fr.swift.result;

import com.fr.swift.result.qrs.QueryResultSet;
import com.fr.swift.result.qrs.QueryResultSetMerger;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.structure.Pair;

import java.util.List;
import java.util.Map;

/**
 * @author anchore
 * @date 12/11/2018
 */
public abstract class BaseNodeResultSet<T extends SwiftNode> implements QueryResultSet<Pair<T, List<Map<Integer, Object>>>> {

    private int fetchSize;

    public BaseNodeResultSet(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    @Override
    public int getFetchSize() {
        return fetchSize;
    }

    @Override
    public <Q extends QueryResultSet<Pair<T, List<Map<Integer, Object>>>>> QueryResultSetMerger<Pair<T, List<Map<Integer, Object>>>, Q> getMerger() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SwiftResultSet convert(SwiftMetaData metaData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }
}