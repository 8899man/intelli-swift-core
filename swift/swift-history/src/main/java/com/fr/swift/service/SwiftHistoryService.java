package com.fr.swift.service;

import com.fr.swift.SwiftContext;
import com.fr.swift.annotation.SwiftService;
import com.fr.swift.basics.ProxyFactory;
import com.fr.swift.basics.annotation.ProxyService;
import com.fr.swift.basics.base.selector.ProxySelector;
import com.fr.swift.beans.annotation.SwiftBean;
import com.fr.swift.bitmap.ImmutableBitMap;
import com.fr.swift.cluster.listener.NodeStartedListener;
import com.fr.swift.config.bean.SwiftTablePathBean;
import com.fr.swift.config.service.SwiftClusterSegmentService;
import com.fr.swift.config.service.SwiftCubePathService;
import com.fr.swift.config.service.SwiftSegmentService;
import com.fr.swift.config.service.SwiftTablePathService;
import com.fr.swift.cube.io.Types;
import com.fr.swift.db.Where;
import com.fr.swift.event.ClusterEvent;
import com.fr.swift.event.ClusterEventListener;
import com.fr.swift.event.ClusterEventType;
import com.fr.swift.event.ClusterListenerHandler;
import com.fr.swift.event.SwiftEventDispatcher;
import com.fr.swift.event.global.PushSegLocationRpcEvent;
import com.fr.swift.event.history.CheckLoadHistoryEvent;
import com.fr.swift.exception.SwiftServiceException;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.query.info.bean.query.QueryBeanFactory;
import com.fr.swift.query.info.bean.query.QueryInfoBean;
import com.fr.swift.query.session.factory.SessionFactory;
import com.fr.swift.segment.SegmentDestination;
import com.fr.swift.segment.SegmentHelper;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.SegmentLocationInfo;
import com.fr.swift.segment.SwiftSegmentManager;
import com.fr.swift.segment.bean.impl.SegmentDestinationImpl;
import com.fr.swift.segment.bean.impl.SegmentLocationInfoImpl;
import com.fr.swift.segment.event.SegmentEvent;
import com.fr.swift.segment.operator.delete.WhereDeleter;
import com.fr.swift.selector.ClusterSelector;
import com.fr.swift.service.listener.RemoteSender;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftResultSet;
import com.fr.swift.task.service.ServiceTaskExecutor;
import com.fr.swift.task.service.ServiceTaskType;
import com.fr.swift.task.service.SwiftServiceCallable;
import com.fr.swift.util.FileUtil;
import com.fr.swift.util.concurrent.PoolThreadFactory;
import com.fr.swift.util.concurrent.SwiftExecutors;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author pony
 * @date 2017/10/10
 */
@SwiftService(name = "history")
@ProxyService(HistoryService.class)
@SwiftBean(name = "history")
public class SwiftHistoryService extends AbstractSwiftService implements HistoryService, Serializable {
    private static final long serialVersionUID = -6013675740141588108L;

    private transient SwiftSegmentManager segmentManager;

    private transient ServiceTaskExecutor taskExecutor;

    private transient SwiftTablePathService tablePathService;

    private transient SwiftSegmentService segmentService;

    private transient ExecutorService loadDataService;

    private transient SwiftCubePathService cubePathService;

    private transient ClusterEventListener historyClusterListener;

    public SwiftHistoryService() {
        historyClusterListener = new HistoryClusterListener();
    }

    @Override
    public boolean start() throws SwiftServiceException {
        super.start();
        segmentManager = SwiftContext.get().getBean("localSegmentProvider", SwiftSegmentManager.class);
        taskExecutor = SwiftContext.get().getBean(ServiceTaskExecutor.class);
        tablePathService = SwiftContext.get().getBean(SwiftTablePathService.class);
        segmentService = SwiftContext.get().getBean("segmentServiceProvider", SwiftSegmentService.class);
        loadDataService = SwiftExecutors.newSingleThreadExecutor(new PoolThreadFactory(SwiftHistoryService.class));
        cubePathService = SwiftContext.get().getBean(SwiftCubePathService.class);
        final Map<SourceKey, Set<String>> needLoad = SegmentHelper.checkSegmentExists(segmentService, segmentManager);
        loadDataService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    load(needLoad, false);
                } catch (Exception e) {
                    SwiftLoggers.getLogger().warn(e);
                }
            }
        });
        ClusterListenerHandler.addExtraListener(historyClusterListener);
        return true;
    }


    @Override
    public boolean shutdown() throws SwiftServiceException {
        super.shutdown();
        segmentManager = null;
        taskExecutor = null;
        tablePathService = null;
        loadDataService.shutdown();
        loadDataService = null;
        segmentService = null;
        ClusterListenerHandler.removeExtraListener(historyClusterListener);
        return true;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.HISTORY;
    }

    @Override
    public void load(Set<SegmentKey> sourceSegKeys, boolean replace) throws Exception {
        Map<SourceKey, Set<String>> needLoadSegments = new HashMap<SourceKey, Set<String>>();
        for (SegmentKey segmentKey : sourceSegKeys) {
            SourceKey sourceKey = segmentKey.getTable();
            if (!needLoadSegments.containsKey(sourceKey)) {
                needLoadSegments.put(sourceKey, new HashSet<String>());
            }
            needLoadSegments.get(sourceKey).add(segmentKey.getUri().getPath());
        }
        load(needLoadSegments, replace);
    }

    @Override
    public void load(Map<SourceKey, Set<String>> remoteUris, final boolean replace) throws Exception {
        if (null == remoteUris || remoteUris.isEmpty()) {
            return;
        }
        List<Future<?>> futures = new ArrayList<Future<?>>(remoteUris.size());
        for (final SourceKey sourceKey : remoteUris.keySet()) {
            final Set<String> uris = remoteUris.get(sourceKey);
            if (uris.isEmpty()) {
                return;
            }
            try {
                futures.add(taskExecutor.submit(new SwiftServiceCallable<Void>(sourceKey, ServiceTaskType.DOWNLOAD, new Callable<Void>() {
                    @Override
                    public Void call() {
                        SegmentHelper.download(sourceKey.getId(), uris, replace);
                        SwiftLoggers.getLogger().info("{}, {}", sourceKey, uris);
                        return null;
                    }
                })));

            } catch (InterruptedException e) {
                SwiftLoggers.getLogger().warn("download seg {} of {} failed", uris, sourceKey, e);
            }
        }
        for (Future<?> future : futures) {
            future.get();
        }
    }


    @Override
    public SwiftResultSet query(final String queryDescription) throws Exception {
        try {
            final QueryInfoBean bean = QueryBeanFactory.create(queryDescription);
            SessionFactory factory = SwiftContext.get().getBean(SessionFactory.class);
            // TODO: 2018/11/28
            return (SwiftResultSet) factory.openSession(bean.getQueryId()).executeQuery(bean);
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public boolean delete(final SourceKey sourceKey, final Where where, final List<String> needUpload) throws Exception {
        Future<Boolean> future = taskExecutor.submit(new SwiftServiceCallable<Boolean>(sourceKey, ServiceTaskType.DELETE, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                List<SegmentKey> segmentKeys = segmentManager.getSegmentKeys(sourceKey);
                for (SegmentKey segKey : segmentKeys) {
                    if (segKey.getStoreType().isTransient()) {
                        continue;
                    }
                    if (!segmentManager.existsSegment(segKey)) {
                        continue;
                    }
                    WhereDeleter whereDeleter = (WhereDeleter) SwiftContext.get().getBean("decrementer", segKey);
                    ImmutableBitMap allShowBitmap = whereDeleter.delete(where);
                    if (needUpload.contains(segKey.toString())) {
                        if (allShowBitmap.isEmpty()) {
                            SwiftEventDispatcher.fire(SegmentEvent.REMOVE_HISTORY, segKey);
                        } else {
                            SwiftEventDispatcher.fire(SegmentEvent.MASK_HISTORY, segKey);
                        }
                    }
                }
                return true;
            }
        }));
        return future.get();
    }

    @Override
    public void truncate(String sourceKey) {
        SwiftTablePathBean entity = tablePathService.get(sourceKey);
        int path = 0;
        if (null != entity) {
            path = entity.getTablePath() == null ? 0 : entity.getTablePath();
            tablePathService.removePath(sourceKey);
        }
        SwiftSegmentService segmentService = SwiftContext.get().getBean(SwiftClusterSegmentService.class);
        segmentService.removeSegments(sourceKey);

        String localPath = String.format("%s/%d/%s", cubePathService.getSwiftPath(), path, sourceKey);
        FileUtil.delete(localPath);
    }

    @Override
    public void commonLoad(SourceKey sourceKey, Map<SegmentKey, List<String>> needLoad) throws Exception {
        Map<SourceKey, Set<String>> needLoadPath = new HashMap<SourceKey, Set<String>>();
        Set<String> uris = new HashSet<String>();
        for (Map.Entry<SegmentKey, List<String>> entry : needLoad.entrySet()) {
            uris.addAll(entry.getValue());
        }
        needLoadPath.put(sourceKey, uris);
        load(needLoadPath, false);
    }


    /**
     * 加入集群后，historyService做集群相应处理
     */
    private class HistoryClusterListener implements ClusterEventListener {

        private ProxyFactory proxyFactory;
        private RemoteSender senderProxy;

        private HistoryClusterListener() {
            proxyFactory = ProxySelector.getInstance().getFactory();
            senderProxy = proxyFactory.getProxy(RemoteSender.class);
        }

        @Override
        public void handleEvent(ClusterEvent clusterEvent) {
            if (clusterEvent.getEventType() == ClusterEventType.JOIN_CLUSTER) {
                NodeStartedListener.INSTANCE.registerTask(new NodeStartedListener.NodeStartedTask() {
                    @Override
                    public void run() {
                        checkSegmentExists();
                        sendLocalSegmentInfo();
                        checkLoad();
                    }
                });
            }
        }

        private void checkSegmentExists() {
            SwiftClusterSegmentService segmentService = SwiftContext.get().getBean(SwiftClusterSegmentService.class);
            segmentService.setClusterId(getID());
            final Map<SourceKey, Set<String>> needDownload = SegmentHelper.checkSegmentExists(segmentService, segmentManager);
            if (!needDownload.isEmpty()) {
                loadDataService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            load(needDownload, false);
                        } catch (Exception e) {
                            SwiftLoggers.getLogger().warn(e);
                        }
                    }
                });
            }
        }

        private void sendLocalSegmentInfo() {
            SegmentLocationInfo info = loadSelfSegmentDestination();
            if (null != info) {
                try {
                    senderProxy.trigger(new PushSegLocationRpcEvent(info));
                } catch (Exception e) {
                    SwiftLoggers.getLogger().warn("Cannot sync native segment info to server! ", e);
                }
            }
        }

        private void checkLoad() {
            try {
                senderProxy.trigger(new CheckLoadHistoryEvent(getID()));
            } catch (Exception e) {
                SwiftLoggers.getLogger().warn("Cannot sync native segment info to server! ", e);
            }
        }


        protected SegmentLocationInfo loadSelfSegmentDestination() {
            SwiftClusterSegmentService clusterSegmentService = SwiftContext.get().getBean(SwiftClusterSegmentService.class);
            clusterSegmentService.setClusterId(getID());
            Map<SourceKey, List<SegmentKey>> segments = clusterSegmentService.getOwnSegments();
            if (!segments.isEmpty()) {
                Map<SourceKey, List<SegmentDestination>> hist = new HashMap<SourceKey, List<SegmentDestination>>();
                for (Map.Entry<SourceKey, List<SegmentKey>> entry : segments.entrySet()) {
                    initSegDestinations(hist, entry.getKey());
                    for (SegmentKey segmentKey : entry.getValue()) {
                        if (segmentKey.getStoreType() == Types.StoreType.FINE_IO) {
                            hist.get(entry.getKey()).add(createSegmentDestination(segmentKey));
                        }
                    }
                }
                if (hist.isEmpty()) {
                    return null;
                }
                return new SegmentLocationInfoImpl(ServiceType.HISTORY, hist);
            }
            return null;
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

        protected SegmentDestination createSegmentDestination(SegmentKey segmentKey) {
            String clusterId = ClusterSelector.getInstance().getFactory().getCurrentId();
            return new SegmentDestinationImpl(clusterId, segmentKey.toString(), segmentKey.getOrder(), HistoryService.class, "historyQuery");
        }
    }
}
