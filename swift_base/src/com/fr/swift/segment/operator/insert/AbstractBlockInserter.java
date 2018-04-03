package com.fr.swift.segment.operator.insert;

import com.fr.swift.config.IConfigSegment;
import com.fr.swift.config.IMetaData;
import com.fr.swift.config.ISegmentKey;
import com.fr.swift.config.conf.MetaDataConfig;
import com.fr.swift.config.conf.MetaDataConvertUtil;
import com.fr.swift.config.conf.SegmentConfig;
import com.fr.swift.config.unique.SegmentKeyUnique;
import com.fr.swift.config.unique.SegmentUnique;
import com.fr.swift.cube.io.Types;
import com.fr.swift.cube.io.location.IResourceLocation;
import com.fr.swift.cube.io.location.ResourceLocation;
import com.fr.swift.exception.meta.SwiftMetaDataException;
import com.fr.swift.log.SwiftLogger;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.SegmentIndexCache;
import com.fr.swift.segment.column.ColumnKey;
import com.fr.swift.segment.operator.Inserter;
import com.fr.swift.segment.operator.utils.InserterUtils;
import com.fr.swift.source.ColumnTypeConstants;
import com.fr.swift.source.ColumnTypeUtils;
import com.fr.swift.source.Row;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftMetaDataColumn;
import com.fr.swift.source.SwiftResultSet;
import com.fr.swift.source.SwiftSourceAlloter;
import com.fr.swift.source.SwiftSourceAlloterFactory;
import com.fr.swift.util.Crasher;

import java.util.ArrayList;
import java.util.List;

/**
 * This class created on 2018/3/27
 *
 * @author Lucifer
 * @description 指定数据，分块逻辑在内部计算
 * @since Advanced FineBI Analysis 1.0
 */
public abstract class AbstractBlockInserter implements Inserter {

    private SwiftLogger logger = SwiftLoggers.getLogger(AbstractBlockInserter.class);

    protected SourceKey sourceKey;
    protected String cubeSourceKey;
    protected SwiftMetaData swiftMetaData;
    protected List<String> fields;
    protected List<Segment> segments;
    protected IConfigSegment configSegment;
    protected SwiftSourceAlloter alloter;
    protected SegmentIndexCache segmentIndexCache;
    private int startSegIndex;

    public AbstractBlockInserter(SourceKey sourceKey, String cubeSourceKey, SwiftMetaData swiftMetaData) {
        this(sourceKey, cubeSourceKey, swiftMetaData, swiftMetaData.getFieldNames());
    }

    public AbstractBlockInserter(SourceKey sourceKey, String cubeSourceKey, SwiftMetaData swiftMetaData, List<String> fields) {
        this(new ArrayList<Segment>(), sourceKey, cubeSourceKey, swiftMetaData, fields);
    }

    public AbstractBlockInserter(List<Segment> segments, SourceKey sourceKey, String cubeSourceKey, SwiftMetaData swiftMetaData) {
        this(segments, sourceKey, cubeSourceKey, swiftMetaData, swiftMetaData.getFieldNames());
    }

    public AbstractBlockInserter(List<Segment> segments, SourceKey sourceKey, String cubeSourceKey, SwiftMetaData swiftMetaData, List<String> fields) {
        this.sourceKey = sourceKey;
        this.cubeSourceKey = cubeSourceKey;
        this.swiftMetaData = swiftMetaData;
        this.fields = fields;
        this.alloter = SwiftSourceAlloterFactory.createSourceAlloter(sourceKey);
        this.segments = new ArrayList<Segment>();
        this.configSegment = new SegmentUnique();
        this.configSegment.setSourceKey(sourceKey.getId());
        this.segments = segments;
        this.segmentIndexCache = new SegmentIndexCache();
        this.startSegIndex = segments.size();
        for (int i = 0; i < segments.size(); i++) {
            if (segments.get(i).isHistory()) {
                createSegment(i, Types.StoreType.FINE_IO);
            } else {
                createSegment(i, Types.StoreType.MEMORY);
            }
        }
    }

    @Override
    public boolean insertData(List<Row> rowList) {
        return false;
    }

    @Override
    public boolean insertData(SwiftResultSet swiftResultSet) throws Exception {
        try {
            long count = 0;
            String allotColumn = fields.get(0);
            while (swiftResultSet.next()) {
                Row rowData = swiftResultSet.getRowData();
                // fixme 为啥这里要传后两个参数？
                // 为了以后特殊的分块逻辑
                int size = segments.size();
                int index = alloter.allot(count, allotColumn, rowData.getValue(swiftMetaData.getColumnIndex(allotColumn))) + startSegIndex;
                if (index >= size) {
                    for (int i = size; i <= index; i++) {
                        segmentIndexCache.putSegRow(i, 0);
                        segments.add(createSegment(i));
                    }
                } else if (index == -1) {
                    index = segments.size() - 1;
                }
                int segmentRow = segmentIndexCache.getSegRowByIndex(index);

                Segment segment = segments.get(index);
                segmentIndexCache.putSegment(index, segment);
                for (int i = 0; i < fields.size(); i++) {
                    if (InserterUtils.isBusinessNullValue(rowData.getValue(i))) {
                        SwiftMetaDataColumn metaDataColumn = swiftMetaData.getColumn(fields.get(i));
                        ColumnTypeConstants.ClassType clazz = ColumnTypeUtils.getClassType(metaDataColumn);
                        segment.getColumn(new ColumnKey(fields.get(i))).getDetailColumn().put(segmentRow, InserterUtils.getNullValue(clazz));
                        segmentIndexCache.putSegFieldNull(index, fields.get(i), segmentRow);
                    } else {
                        segment.getColumn(new ColumnKey(fields.get(i))).getDetailColumn().put(segmentRow, rowData.getValue(i));
                    }
                }
                segmentIndexCache.putSegRow(index, ++segmentRow);
                count++;
            }
        } finally {
            swiftResultSet.close();
        }
        release();
        return true;
    }

    protected abstract Segment createSegment(int order);

    /**
     * 创建Segment
     * TODO 分块存放目录
     *
     * @param order 块号
     * @return
     * @throws Exception
     */
    protected Segment createSegment(int order, Types.StoreType storeType) {
        String cubePath = System.getProperty("user.dir") + "/cubes/" + cubeSourceKey + "/seg" + order;
        IResourceLocation location = new ResourceLocation(cubePath, storeType);
        ISegmentKey segmentKey = new SegmentKeyUnique();
        segmentKey.setSegmentOrder(order);
        segmentKey.setUri(location.getUri().getPath());
        segmentKey.setSourceId(sourceKey.getId());
        segmentKey.setStoreType(storeType.name());
        configSegment.addSegment(segmentKey);
        return createNewSegment(location, swiftMetaData);
    }

    protected abstract Segment createNewSegment(IResourceLocation location, SwiftMetaData swiftMetaData);

    public void release() {
        persistMeta();
        persistSegment();
    }

    protected void persistMeta() {
        try {
            IMetaData metaData = MetaDataConvertUtil.convert2ConfigMetaData(this.swiftMetaData);
            MetaDataConfig.getInstance().addMetaData(sourceKey.getId(), metaData);
        } catch (SwiftMetaDataException e) {
            logger.error("save metadata failed! ", e);
            Crasher.crash(e);
        }
    }

    protected void persistSegment() {
        SegmentConfig.getInstance().putSegment(configSegment);
    }

    @Override
    public List<String> getFields() {
        return fields;
    }
}
