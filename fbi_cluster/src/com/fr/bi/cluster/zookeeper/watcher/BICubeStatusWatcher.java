package com.fr.bi.cluster.zookeeper.watcher;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.data.disk.BICubeDiskPrimitiveDiscovery;
import com.fr.bi.cluster.ClusterAdapter;
import com.fr.bi.cluster.zookeeper.BIWatcher;
import com.fr.bi.cluster.zookeeper.ZooKeeperManager;
import org.apache.zookeeper.WatchedEvent;

/**
 * wang 2016-12-08
 * 集群中监控cube生成状态的节点，主要用于集群中资源释放的问题
 */
public class BICubeStatusWatcher extends BIWatcher {
    public final static String CUBE_STATUS = "/cube/status";

    public BICubeStatusWatcher() {
    }

    @Override
    public String getFocusedEventPath() {
        return CUBE_STATUS;
    }


    @Override
    public void eventProcessor(WatchedEvent event) {
        //            todo   如果是子节点收到通知，则调用nio资源强制释放
        if (!ClusterAdapter.getManager().getHostManager().isBuildCube()) {
            BILoggerFactory.getLogger(BICubeStatusWatcher.class).info("============slaver release resource=========== ");
            BICubeDiskPrimitiveDiscovery.getInstance().forceRelease();
            BICubeDiskPrimitiveDiscovery.getInstance().finishRelease();
        }else{
            BILoggerFactory.getLogger(BICubeStatusWatcher.class).info("============master release resource=========== ");
        }
        try {
            ZooKeeperManager.getInstance().getZooKeeper().exists(CUBE_STATUS,this);
        } catch (Exception e) {
            BILoggerFactory.getLogger(BICubeStatusWatcher.class).error("cube status watcher register failed "+e.getMessage(),e);
        }
    }
}