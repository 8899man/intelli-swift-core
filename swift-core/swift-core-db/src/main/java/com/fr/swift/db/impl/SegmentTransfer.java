package com.fr.swift.db.impl;

import com.fineio.FineIO;
import com.fr.swift.config.service.SwiftSegmentService;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.SegmentResultSet;
import com.fr.swift.segment.SegmentUtils;
import com.fr.swift.segment.SwiftSegmentManager;
import com.fr.swift.segment.container.SegmentContainer;
import com.fr.swift.segment.operator.Inserter;

import java.util.Collections;
import java.util.concurrent.Callable;

/**
 * @author anchore
 * @date 2018/8/20
 */
public class SegmentTransfer {
    private static final SwiftSegmentService SEG_SVC = SwiftContext.get().getBean("segmentServiceProvider", SwiftSegmentService.class);

    private SegmentKey oldSegKey;
    protected SegmentKey newSegKey;

    public SegmentTransfer(SegmentKey oldSegKey, SegmentKey newSegKey) {
        this.oldSegKey = oldSegKey;
        this.newSegKey = newSegKey;
    }

    public void transfer() {
        final Segment oldSeg = newSegment(oldSegKey), newSeg = newSegment(newSegKey);
        Inserter inserter = (Inserter) SwiftContext.get().getBean("inserter", newSeg);
        SegmentResultSet swiftResultSet = null;
        try {
            SEG_SVC.addSegments(Collections.singletonList(newSegKey));

            swiftResultSet = new SegmentResultSet(oldSeg);
            inserter.insertData(swiftResultSet);

            FineIO.doWhenFinished(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    SwiftLoggers.getLogger().error("enter fineio doWhenFinished");
                    indexSegmentIfNeed(newSeg);
                    onSucceed();
                    return null;
                }
            }).get();
            SegmentContainer.NORMAL.updateSegment(newSegKey, newSeg);

            SwiftLoggers.getLogger().info("seg transferred from {} to {}", oldSegKey, newSegKey);
        } catch (Exception e) {
            SwiftLoggers.getLogger().error("seg transfer from {} to {} failed", oldSegKey, newSegKey, e);
            remove(newSegKey);
        } finally {
            if (swiftResultSet != null) {
                swiftResultSet.close();
            }
        }
    }

    protected void onSucceed() {
        remove(oldSegKey);
        SwiftContext.get().getBean("localSegmentProvider", SwiftSegmentManager.class).getSegment(newSegKey);
    }

    private void indexSegmentIfNeed(Segment newSeg) throws Exception {
        SegmentUtils.indexSegmentIfNeed(Collections.singletonList(newSeg));
    }

    private void remove(final SegmentKey segKey) {
        SEG_SVC.removeSegments(Collections.singletonList(segKey));
        SegmentUtils.clearSegment(segKey);
        SwiftLoggers.getLogger().info("seg {} removed", segKey);
    }

    private Segment newSegment(SegmentKey segKey) {
        return SegmentUtils.newSegment(segKey);
    }
}