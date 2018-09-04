package com.fr.swift.segment.operator.insert;

import com.fr.swift.config.service.SwiftSegmentService;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.cube.CubeUtil;
import com.fr.swift.db.Database;
import com.fr.swift.db.impl.SwiftDatabase;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.operator.Inserter;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.Row;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftResultSet;
import com.fr.swift.source.alloter.SwiftSourceAlloter;
import com.fr.swift.source.alloter.impl.line.LineAllotRule;
import com.fr.swift.source.alloter.impl.line.LineSourceAlloter;
import com.fr.swift.source.resultset.LimitedResultSet;
import com.fr.swift.source.resultset.ListResultSet;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * @author anchore
 * @date 2018/8/1
 */
public abstract class BaseBlockInserter implements Inserter {
    protected SwiftSourceAlloter alloter;

    protected DataSource dataSource;

    protected SegmentKey currentSegKey;

    protected Segment currentSeg;

    public BaseBlockInserter(DataSource dataSource) {
        this(dataSource, new LineSourceAlloter(dataSource.getSourceKey(), new LineAllotRule()));
    }

    public BaseBlockInserter(DataSource dataSource, SwiftSourceAlloter alloter) {
        this.dataSource = dataSource;
        this.alloter = alloter;
    }

    private void insert(SwiftResultSet resultSet) throws Exception {
        try {
            persistMeta();

            while (resultSet.hasNext()) {
                boolean newSeg = nextSegment();
                Inserter inserter = getInserter();

                int step = ((LineAllotRule) alloter.getAllotRule()).getStep();
                int limit = CubeUtil.isReadable(currentSeg) ? step - currentSeg.getRowCount() : step;
                inserter.insertData(new LimitedResultSet(resultSet, limit, false));

                if (newSeg) {
                    persistSegment();
                }
            }
        } finally {
            resultSet.close();
        }
    }

    protected abstract Inserter getInserter();

    protected abstract boolean nextSegment();

    private void persistMeta() throws SQLException {
        Database db = SwiftDatabase.getInstance();
        SourceKey tableKey = dataSource.getSourceKey();
        if (!db.existsTable(tableKey)) {
            db.createTable(tableKey, dataSource.getMetadata());
        }
    }

    protected void persistSegment() {
        SwiftContext.get().getBean("segmentServiceProvider", SwiftSegmentService.class).addSegments(Collections.singletonList(currentSegKey));
    }

    @Override
    public void insertData(List<Row> rowList) throws Exception {
        insertData(new ListResultSet(dataSource.getMetadata(), rowList));
    }

    @Override
    public void insertData(SwiftResultSet swiftResultSet) throws Exception {
        insert(swiftResultSet);
    }

    @Override
    public List<String> getFields() {
        return dataSource.getMetadata().getFieldNames();
    }
}