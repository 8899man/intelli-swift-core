package com.fr.swift.cluster.service;

import com.fineio.FineIO;
import com.fr.swift.annotation.RpcMethod;
import com.fr.swift.annotation.RpcService;
import com.fr.swift.annotation.RpcServiceType;
import com.fr.swift.annotation.SwiftService;
import com.fr.swift.basics.AsyncRpcCallback;
import com.fr.swift.config.entity.SwiftTablePathEntity;
import com.fr.swift.config.service.SwiftCubePathService;
import com.fr.swift.config.service.SwiftSegmentLocationService;
import com.fr.swift.config.service.SwiftSegmentService;
import com.fr.swift.config.service.SwiftTablePathService;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.cube.CubeUtil;
import com.fr.swift.event.base.SwiftRpcEvent;
import com.fr.swift.event.global.TaskDoneRpcEvent;
import com.fr.swift.event.history.HistoryCommonLoadRpcEvent;
import com.fr.swift.event.history.HistoryLoadSegmentRpcEvent;
import com.fr.swift.exception.SwiftServiceException;
import com.fr.swift.info.ServerCurrentStatus;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.netty.rpc.server.RpcServer;
import com.fr.swift.repository.SwiftRepositoryManager;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.segment.relation.RelationIndexImpl;
import com.fr.swift.service.AbstractSwiftService;
import com.fr.swift.service.IndexingService;
import com.fr.swift.service.ServiceType;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.RelationSource;
import com.fr.swift.source.RelationSourceType;
import com.fr.swift.source.SourceKey;
import com.fr.swift.structure.Pair;
import com.fr.swift.stuff.IndexingStuff;
import com.fr.swift.task.TaskKey;
import com.fr.swift.task.TaskResult;
import com.fr.swift.upload.ReadyUploadContainer;
import com.fr.swift.util.FileUtil;
import com.fr.swift.util.ServiceBeanFactory;
import com.fr.swift.util.Strings;
import com.fr.swift.utils.ClusterCommonUtils;
import com.fr.third.springframework.beans.factory.annotation.Autowired;
import com.fr.third.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.fr.swift.task.TaskResult.Type.SUCCEEDED;

/**
 * @author yee
 * @date 2018/8/6
 */
@SwiftService(name = "clusterIndexing")
@RpcService(type = RpcServiceType.CLIENT_SERVICE, value = IndexingService.class)
public class ClusterIndexingService extends AbstractSwiftService implements IndexingService, Serializable {

    private static final long serialVersionUID = 3153509375653090856L;
    @Autowired
    private transient RpcServer server;

    @Autowired(required = false)
    @Qualifier("indexingService")
    private IndexingService indexingService;
    @Autowired(required = false)
    private transient SwiftRepositoryManager repositoryManager;
    @Autowired
    private transient SwiftCubePathService pathService;
    @Autowired
    private transient SwiftTablePathService tablePathService;
    @Autowired
    private transient SwiftSegmentLocationService locationService;

    @Override
    @RpcMethod(methodName = "index")
    public <Stuff extends IndexingStuff> void index(Stuff stuff) {
        indexingService.index(stuff);
    }

    @Override
    @RpcMethod(methodName = "currentStatus")
    public ServerCurrentStatus currentStatus() {
        return indexingService.currentStatus();
    }

    @Override
    public void setListenerWorker(ListenerWorker listenerWorker) {
        indexingService.setListenerWorker(listenerWorker);
    }

    @Override
    public boolean start() throws SwiftServiceException {
        super.start();
        Set<String> serviceNames = new LinkedHashSet<String>();
        serviceNames.add("indexing");
        List<com.fr.swift.service.SwiftService> services = ServiceBeanFactory.getSwiftServiceByNames(serviceNames);
        indexingService = (IndexingService) services.get(0);
        indexingService.start();
        indexingService.setListenerWorker(new ListenerWorker() {
            @Override
            public void work(Pair<TaskKey, TaskResult> result) {
                SwiftLoggers.getLogger().info("rpc通知server任务完成");
                try {
                    ClusterCommonUtils.runRpc(ClusterCommonUtils.getMasterURL(), server.getMethodByName("rpcTrigger"), new TaskDoneRpcEvent(result));
                    FineIO.doWhenFinished(new ClusterUploadRunnable(result, indexingService.getID()));
                } catch (Exception e) {
                    SwiftLoggers.getLogger().error(e);
                }
            }
        });
        return true;
    }

//    @Override
//    public boolean shutdown() throws SwiftServiceException {
//        EventDispatcher.stopListen(listener);
//        return super.shutdown();
//    }

    private class ClusterUploadRunnable implements Runnable {

        protected Pair<TaskKey, TaskResult> result;
        private String id;

        public ClusterUploadRunnable(Pair<TaskKey, TaskResult> result, String id) {
            this.result = result;
            this.id = id;
        }

        public void uploadTable(final DataSource dataSource) throws Exception {
            final SourceKey sourceKey = dataSource.getSourceKey();
            SwiftTablePathEntity entity = SwiftContext.get().getBean(SwiftTablePathService.class).get(sourceKey.getId());
            Integer path = entity.getTablePath();
            path = null == path ? -1 : path;
            Integer tmpPath = entity.getTmpDir();
            entity.setTablePath(tmpPath);
            entity.setLastPath(path);
            List<SegmentKey> segmentKeys = SwiftContext.get().getBean("segmentServiceProvider", SwiftSegmentService.class).getSegmentByKey(sourceKey.getId());
            String cubePath = pathService.getSwiftPath();
            if (null != segmentKeys) {
                for (SegmentKey segmentKey : segmentKeys) {
                    try {
                        String uploadPath = String.format("%s/%s",
                                segmentKey.getSwiftSchema().getDir(),
                                segmentKey.getUri().getPath());
                        URI local = URI.create(String.format("%s/%s/%d/%s",
                                cubePath,
                                segmentKey.getSwiftSchema().getDir(),
                                tmpPath,
                                segmentKey.getUri().getPath()));
                        upload(local, URI.create(uploadPath));
                    } catch (Exception e) {
                        SwiftLoggers.getLogger().error("upload error! ", e);
                    }
                }
                if (path.compareTo(tmpPath) != 0 && tablePathService.saveOrUpdate(entity)
                        && locationService.delete(sourceKey.getId(), id)) {
                    String deletePath = String.format("%s/%s/%d/%s",
                            pathService.getSwiftPath(),
                            dataSource.getMetadata().getSwiftSchema().getDir(),
                            path,
                            sourceKey.getId());
                    FileUtil.delete(deletePath);
                    new File(deletePath).getParentFile().delete();
                }
                doAfterUpload(new HistoryLoadSegmentRpcEvent(sourceKey.getId()));
            }
        }

        public void uploadRelation(RelationSource relation) throws Exception {
            SourceKey sourceKey = relation.getForeignSource();
            SourceKey primary = relation.getPrimarySource();
            List<URI> needUpload = new ArrayList<URI>();
            List<SegmentKey> segmentKeys = SwiftContext.get().getBean("segmentServiceProvider", SwiftSegmentService.class).getSegmentByKey(sourceKey.getId());
            if (null != segmentKeys) {
                if (relation.getRelationType() != RelationSourceType.FIELD_RELATION) {
                    for (SegmentKey segmentKey : segmentKeys) {
                        try {
                            URI src = URI.create(Strings.unifySlash(
                                    String.format("%s/%s/%s/%s",
                                            pathService.getSwiftPath(),
                                            CubeUtil.getSegPath(segmentKey),
                                            RelationIndexImpl.RELATIONS_KEY,
                                            primary.getId()
                                    )));
                            URI dest = URI.create(String.format("%s/%s/%s/%s",
                                    segmentKey.getSwiftSchema().getDir(),
                                    Strings.unifySlash(segmentKey.getUri().getPath() + "/"),
                                    RelationIndexImpl.RELATIONS_KEY,
                                    primary.getId()));
                            upload(src, dest);
                            needUpload.add(dest);
                        } catch (IOException e) {
                            SwiftLoggers.getLogger().error("upload error! ", e);
                        }
                    }
                } else {
                    for (SegmentKey segmentKey : segmentKeys) {
                        try {
                            URI src = URI.create(Strings.unifySlash(
                                    String.format("%s/field/%s/%s",
                                            CubeUtil.getSegPath(segmentKey),
                                            RelationIndexImpl.RELATIONS_KEY,
                                            primary.getId()
                                    )));
                            URI dest = URI.create(String.format("%s/%s/field/%s/%s",
                                    segmentKey.getSwiftSchema().getDir(),
                                    Strings.unifySlash(segmentKey.getUri().getPath() + "/"),
                                    RelationIndexImpl.RELATIONS_KEY,
                                    primary.getId()));
                            upload(src, dest);
                            needUpload.add(dest);
                        } catch (IOException e) {
                            SwiftLoggers.getLogger().error("upload error! ", e);
                        }
                    }
                }
                doAfterUpload(new HistoryCommonLoadRpcEvent(Pair.of(sourceKey.getId(), needUpload)));
            }
        }

        @Override
        public void run() {
            if (result.getValue().getType() == SUCCEEDED) {
                TaskKey key = result.getKey();
                Object obj = ReadyUploadContainer.instance().get(key);
                try {
                    if (null != obj) {
                        if (obj instanceof DataSource) {
                            uploadTable((DataSource) obj);
                        } else if (obj instanceof RelationSource) {
                            uploadRelation((RelationSource) obj);
                        }
                        ReadyUploadContainer.instance().remove(key);
                    }

                } catch (Exception e) {
                    SwiftLoggers.getLogger().error(e);
                }
            } else {
                TaskKey key = result.getKey();
                Object obj = ReadyUploadContainer.instance().get(key);
                runFailed(key, obj);
            }
        }

        private void runFailed(TaskKey key, Object obj) {
            try {
                if (null != obj) {
                    if (obj instanceof DataSource) {
                        SourceKey sourceKey = ((DataSource) obj).getSourceKey();
                        SwiftTablePathEntity entity = SwiftContext.get().getBean(SwiftTablePathService.class).get(sourceKey.getId());
                        Integer tmpPath = entity.getTmpDir();
                        String deletePath = String.format("%s/%s/%d/%s",
                                pathService.getSwiftPath(),
                                ((DataSource) obj).getMetadata().getSwiftSchema().getDir(),
                                tmpPath,
                                sourceKey.getId());
                        FileUtil.delete(deletePath);
                        new File(deletePath).getParentFile().delete();
                        ReadyUploadContainer.instance().remove(key);
                    }
                }
            } catch (Exception e) {
                SwiftLoggers.getLogger().error(e);
            }

        }

        protected void upload(URI src, URI dest) throws IOException {
            repositoryManager.currentRepo().copyToRemote(src, dest);
        }

        public void doAfterUpload(SwiftRpcEvent event) throws Exception {
            ClusterCommonUtils.runRpc(ClusterCommonUtils.getMasterURL(),
                    server.getMethodByName("rpcTrigger"),
                    event).addCallback(new AsyncRpcCallback() {
                @Override
                public void success(Object result) {

                }

                @Override
                public void fail(Exception e) {

                }
            });
        }
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.INDEXING;
    }
}
