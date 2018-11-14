package com.fr.swift.segment;

import com.fineio.FineIO;
import com.fr.swift.config.service.SwiftSegmentService;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.segment.container.SegmentContainer;
import com.fr.swift.segment.operator.Inserter;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

/**
 * @author anchore
 * @date 2018/8/20
 */
public class SegmentTransfer {
    private static final SwiftSegmentService SEG_SVC = SwiftContext.get().getBean("segmentServiceProvider", SwiftSegmentService.class);

    protected SegmentKey oldSegKey, newSegKey;

    private boolean index;

    public SegmentTransfer(SegmentKey oldSegKey, SegmentKey newSegKey) {
        this(oldSegKey, newSegKey, true);
    }

    public SegmentTransfer(SegmentKey oldSegKey, SegmentKey newSegKey, boolean index) {
        this.oldSegKey = oldSegKey;
        this.newSegKey = newSegKey;
        this.index = index;
    }

    public void transfer() {
        final Segment oldSeg = newSegment(oldSegKey), newSeg = newSegment(newSegKey);
        Inserter inserter = (Inserter) SwiftContext.get().getBean("inserter", newSeg);
        SegmentResultSet swiftResultSet = null;
        try {
            SEG_SVC.addSegments(Collections.singletonList(newSegKey));

            swiftResultSet = new SegmentResultSet(oldSeg);
            inserter.insertData(swiftResultSet);
            final CountDownLatch latch = new CountDownLatch(1);
            final Exception[] exception = new Exception[1];
            FineIO.doWhenFinished(new Runnable() {
                @Override
                public void run() {
                    try {
                        indexSegmentIfNeed(newSeg);
                        onSucceed();
                    } catch (Exception e) {
                        exception[0] = e;
                    } finally {
                        latch.countDown();
                    }
                }
            });
            latch.await();
            if (null != exception[0]) {
                throw exception[0];
            }
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