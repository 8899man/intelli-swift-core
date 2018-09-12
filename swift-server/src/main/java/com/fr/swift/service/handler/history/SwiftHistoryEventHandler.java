package com.fr.swift.service.handler.history;

import com.fr.swift.basics.AsyncRpcCallback;
import com.fr.swift.cluster.entity.ClusterEntity;
import com.fr.swift.cluster.service.ClusterSwiftServerService;
import com.fr.swift.config.service.SwiftClusterSegmentService;
import com.fr.swift.event.base.AbstractHistoryRpcEvent;
import com.fr.swift.event.history.HistoryLoadSegmentRpcEvent;
import com.fr.swift.log.SwiftLogger;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.segment.SegmentKey;
import com.fr.swift.service.ServiceType;
import com.fr.swift.service.handler.base.AbstractHandler;
import com.fr.swift.structure.Pair;
import com.fr.third.springframework.beans.factory.annotation.Autowired;
import com.fr.third.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yee
 * @date 2018/6/8
 */
@Service
public class SwiftHistoryEventHandler extends AbstractHandler<AbstractHistoryRpcEvent> {

    private static final SwiftLogger LOGGER = SwiftLoggers.getLogger(SwiftHistoryEventHandler.class);
    @Autowired
    private HistoryDataSyncManager historyDataSyncManager;
    @Autowired(required = false)
    private SwiftClusterSegmentService clusterSegmentService;

    @Override
    public <S extends Serializable> S handle(AbstractHistoryRpcEvent event) {
        try {
            switch (event.subEvent()) {
                case LOAD_SEGMENT:
                case TRANS_COLLATE_LOAD:
                    return historyDataSyncManager.handle((HistoryLoadSegmentRpcEvent) event);
                case COMMON_LOAD:
                    return handleCommonLoad(event);
                default:
                    return null;
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return null;
    }

    private <S extends Serializable> S handleCommonLoad(AbstractHistoryRpcEvent event) throws Exception {
        Map<String, ClusterEntity> services = ClusterSwiftServerService.getInstance().getClusterEntityByService(ServiceType.HISTORY);
        if (null == services || services.isEmpty()) {
            throw new RuntimeException("Cannot find history service");
        }
        Pair<String, Map<String, List<String>>> pair = (Pair<String, Map<String, List<String>>>) event.getContent();
        Iterator<Map.Entry<String, ClusterEntity>> iterator = services.entrySet().iterator();
        Map<String, List<String>> uris = pair.getValue();
        while (iterator.hasNext()) {
            Map.Entry<String, ClusterEntity> entry = iterator.next();
            Map<String, List<SegmentKey>> map = clusterSegmentService.getOwnSegments(entry.getKey());
            List<SegmentKey> list = map.get(pair.getKey());
            Set<String> needLoad = new HashSet<String>();
            if (!list.isEmpty()) {
                for (SegmentKey segmentKey : list) {
                    String segKey = segmentKey.toString();
                    if (uris.containsKey(segKey)) {
                        needLoad.addAll(uris.get(segKey));
                    }
                }
            }
            if (!needLoad.isEmpty()) {
                Map<String, Set<String>> load = new HashMap<String, Set<String>>();
                load.put(pair.getKey(), needLoad);
                runAsyncRpc(entry.getKey(), entry.getValue().getServiceClass(), "load", load, false)
                        .addCallback(new AsyncRpcCallback() {
                            @Override
                            public void success(Object result) {
                                LOGGER.info("load success");
                            }

                            @Override
                            public void fail(Exception e) {
                                LOGGER.error("load error! ", e);
                            }
                        });
            }
        }
        return null;
    }
}
