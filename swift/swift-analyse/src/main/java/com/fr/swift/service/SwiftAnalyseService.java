package com.fr.swift.service;

import com.fr.swift.annotation.RpcMethod;
import com.fr.swift.annotation.SwiftService;
import com.fr.swift.basics.annotation.ProxyService;
import com.fr.swift.basics.base.selector.ProxySelector;
import com.fr.swift.config.service.SwiftClusterSegmentService;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.event.analyse.RequestSegLocationEvent;
import com.fr.swift.exception.SwiftServiceException;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.query.info.bean.query.QueryBeanFactory;
import com.fr.swift.query.query.QueryBean;
import com.fr.swift.query.session.factory.SessionFactory;
import com.fr.swift.result.qrs.QueryResultSet;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.SegmentLocationInfo;
import com.fr.swift.segment.SegmentLocationProvider;
import com.fr.swift.segment.SwiftSegmentManager;
import com.fr.swift.segment.SegmentDestination;
import com.fr.swift.segment.bean.impl.RealTimeSegDestImpl;
import com.fr.swift.segment.bean.impl.SegmentDestinationImpl;
import com.fr.swift.segment.bean.impl.SegmentLocationInfoImpl;
import com.fr.swift.service.listener.RemoteSender;
import com.fr.swift.source.SourceKey;
import com.fr.swift.structure.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author pony
 * @date 2017/10/12
 * 分析服务
 */
@SwiftService(name = "analyse")
@ProxyService(AnalyseService.class)
public class SwiftAnalyseService extends AbstractSwiftService implements AnalyseService {
    private static final long serialVersionUID = 841582089735823794L;

    private transient SessionFactory sessionFactory;
    private transient boolean loadable = true;

    public SwiftAnalyseService() {
    }

    @Override
    public boolean start() throws SwiftServiceException {
        boolean start = super.start();
        this.sessionFactory = SwiftContext.get().getBean("swiftQuerySessionFactory", SessionFactory.class);
        cacheSegments();
        return start;
    }

    private void cacheSegments() {
        SwiftClusterSegmentService clusterSegmentService = SwiftContext.get().getBean(SwiftClusterSegmentService.class);
        clusterSegmentService.setClusterId("LOCAL");
        Map<SourceKey, List<SegmentKey>> segments = clusterSegmentService.getOwnSegments();
        SwiftSegmentManager manager = SwiftContext.get().getBean("localSegmentProvider", SwiftSegmentManager.class);
        if (!segments.isEmpty()) {
            for (Map.Entry<SourceKey, List<SegmentKey>> entry : segments.entrySet()) {
                for (SegmentKey segmentKey : entry.getValue()) {
                    manager.getSegment(segmentKey);
                }
            }
        }
    }

    private void loadSegmentLocationInfo(SwiftClusterSegmentService clusterSegmentService) {
        if (loadable) {
            loadSelfSegmentDestination(clusterSegmentService);
            loadable = false;
        }
        List<Pair<SegmentLocationInfo.UpdateType, SegmentLocationInfo>> result =
                (List<Pair<SegmentLocationInfo.UpdateType, SegmentLocationInfo>>) ProxySelector.getInstance().getFactory().getProxy(RemoteSender.class).trigger(new RequestSegLocationEvent(getID()));
        if (!result.isEmpty()) {
            for (Pair<SegmentLocationInfo.UpdateType, SegmentLocationInfo> pair : result) {
                updateSegmentInfo(pair.getValue(), pair.getKey());
            }
        }
    }

    private void initSegDestinations(Map<SourceKey, List<SegmentDestination>> map, SourceKey key) {
        if (null == map.get(key)) {
            map.put(key, new ArrayList<SegmentDestination>() {
                @Override
                public boolean add(SegmentDestination segmentDestination) {
                    return !contains(segmentDestination) && super.add(segmentDestination);
                }
            });
        }
    }

    private void loadSelfSegmentDestination(SwiftClusterSegmentService clusterSegmentService) {
//        SwiftClusterSegmentService clusterSegmentService = SwiftContext.get().getBean(SwiftClusterSegmentService.class);
//        clusterSegmentService.setClusterId(getID());
        Map<SourceKey, List<SegmentKey>> segments = clusterSegmentService.getOwnSegments();
        SwiftSegmentManager manager = SwiftContext.get().getBean("localSegmentProvider", SwiftSegmentManager.class);
        if (!segments.isEmpty()) {
            Map<SourceKey, List<SegmentDestination>> hist = new HashMap<SourceKey, List<SegmentDestination>>();
            Map<SourceKey, List<SegmentDestination>> realTime = new HashMap<SourceKey, List<SegmentDestination>>();
            for (Map.Entry<SourceKey, List<SegmentKey>> entry : segments.entrySet()) {
                initSegDestinations(hist, entry.getKey());
                initSegDestinations(realTime, entry.getKey());
                for (SegmentKey segmentKey : entry.getValue()) {
                    if (segmentKey.getStoreType().isPersistent()) {
                        hist.get(entry.getKey()).add(new SegmentDestinationImpl(getID(), segmentKey.toString(), segmentKey.getOrder(), HistoryService.class, "historyQuery"));
                    } else {
                        realTime.get(entry.getKey()).add(new RealTimeSegDestImpl(getID(), segmentKey.toString(), segmentKey.getOrder(), RealtimeService.class, "realTimeQuery"));
                    }
                    manager.getSegment(segmentKey);
                }
            }
            updateSegmentInfo(new SegmentLocationInfoImpl(ServiceType.HISTORY, hist), SegmentLocationInfo.UpdateType.PART);
            updateSegmentInfo(new SegmentLocationInfoImpl(ServiceType.REAL_TIME, realTime), SegmentLocationInfo.UpdateType.PART);
        }
    }

    @Override
    public boolean shutdown() throws SwiftServiceException {
        super.shutdown();
        sessionFactory = null;
        return true;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.ANALYSE;
    }

    @Override
    public QueryResultSet getQueryResult(String queryJson) throws Exception {
        SwiftLoggers.getLogger().debug(queryJson);
        QueryBean info = QueryBeanFactory.create(queryJson);
        return sessionFactory.openSession(info.getQueryId()).executeQuery(info);
    }

    @Override
    @RpcMethod(methodName = "updateSegmentInfo")
    public void updateSegmentInfo(SegmentLocationInfo locationInfo, SegmentLocationInfo.UpdateType updateType) {
        String clusterId = getID();
        for (List<SegmentDestination> value : locationInfo.getDestinations().values()) {
            for (SegmentDestination segmentDestination : value) {
                ((SegmentDestinationImpl) segmentDestination).setCurrentNode(clusterId);
            }
        }
        SegmentLocationProvider.getInstance().updateSegmentInfo(locationInfo, updateType);
    }

    @Override
    @RpcMethod(methodName = "removeTable")
    public void removeTable(String cluster, String sourceKey) {
        SegmentLocationProvider.getInstance().removeTable(cluster, new SourceKey(sourceKey));
    }

    @Override
    @RpcMethod(methodName = "removeSegments")
    public void removeSegments(String clusterId, String sourceKey, List<String> segmentKeys) {
        SegmentLocationProvider.getInstance().removeSegments(clusterId, new SourceKey(sourceKey), segmentKeys);
    }
}
