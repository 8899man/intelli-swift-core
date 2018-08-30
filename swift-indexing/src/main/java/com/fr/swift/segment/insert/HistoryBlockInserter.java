package com.fr.swift.segment.insert;

import com.fr.swift.config.bean.SegmentKeyBean;
import com.fr.swift.config.entity.SwiftTablePathEntity;
import com.fr.swift.config.service.SwiftTablePathService;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.cube.CubeUtil;
import com.fr.swift.cube.io.Types.StoreType;
import com.fr.swift.cube.io.location.ResourceLocation;
import com.fr.swift.segment.HistorySegmentImpl;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.operator.Inserter;
import com.fr.swift.segment.operator.insert.BaseBlockInserter;
import com.fr.swift.segment.operator.insert.SwiftInserter;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.alloter.SegmentInfo;
import com.fr.swift.source.alloter.SwiftSourceAlloter;
import com.fr.swift.source.alloter.impl.line.LineRowInfo;

/**
 * @author anchore
 * @date 2018/8/1
 */
public class HistoryBlockInserter extends BaseBlockInserter {
    private SwiftTablePathService tablePathService = SwiftContext.get().getBean(SwiftTablePathService.class);

    private int currentDir = 0;

    private int segOrder = 0;

    public HistoryBlockInserter(DataSource dataSource) {
        super(dataSource);
        init();
    }

    public HistoryBlockInserter(DataSource dataSource, SwiftSourceAlloter alloter) {
        super(dataSource, alloter);
        init();
    }

    private void init() {
        SourceKey sourceKey = dataSource.getSourceKey();
        SwiftTablePathEntity entity = tablePathService.get(sourceKey.getId());
        if (entity == null) {
            entity = new SwiftTablePathEntity(sourceKey.getId(), 0);
        } else {
            currentDir = entity.getTablePath() == null ? 0 : entity.getTablePath() + 1;
            entity.setTmpDir(currentDir);
        }
        tablePathService.saveOrUpdate(entity);
    }

    @Override
    protected Inserter getInserter() {
        return new SwiftInserter(currentSeg);
    }

    private Segment newHistorySegment(SegmentInfo segInfo, int segCount) {
        currentSegKey = new SegmentKeyBean(dataSource.getSourceKey(), segCount + segInfo.getOrder(), StoreType.FINE_IO, dataSource.getMetadata().getSwiftDatabase());
        ResourceLocation location = new ResourceLocation(CubeUtil.getHistorySegPath(dataSource, currentDir, currentSegKey.getOrder()), currentSegKey.getStoreType());
        return new HistorySegmentImpl(location, dataSource.getMetadata());
    }

    @Override
    protected boolean nextSegment() {
        currentSeg = newHistorySegment(alloter.allot(new LineRowInfo(0)), segOrder++);
        return true;
    }
}