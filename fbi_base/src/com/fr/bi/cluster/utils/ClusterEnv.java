package com.fr.bi.cluster.utils;

import com.fr.stable.ProductConstants;

import java.io.File;

/**
 * Created by Hiram on 2015/2/26.
 */
public class ClusterEnv {
    final static String BI_DIR = "fineBI";
    final static String CLUSTER_DIR = "cluster";
    final static String SELF_INFO = "self.info";
    final static String REDIRECT_INFO = "redirect.info";
    final static String ZOOKEEPER_INFO = "zookeeper.info";
    final static String DIRECT_INFO = "direct.info";

    public static File getSelfInfoFile() {
        String filePath = getClusterEnvDir() + File.separator + SELF_INFO;
        return new File(filePath);
    }

    public static File getRedirectInfoFile() {
        String filePath = getClusterEnvDir() + File.separator + REDIRECT_INFO;
        return new File(filePath);
    }
    public static File getDirectInfoFile() {
        String filePath = getClusterEnvDir() + File.separator + DIRECT_INFO;
        return new File(filePath);
    }
    private static String getClusterEnvDir() {
        return ProductConstants.getEnvHome() + File.separator + BI_DIR
                + File.separator + CLUSTER_DIR;
    }

    public static File getZookeeperInfoFile() {
        return new File(getClusterEnvDir() + File.separator + ZOOKEEPER_INFO);
    }

    public static boolean isCluster() {
        //暂时通过有没有zookeeper配置文件来判断
        return getZookeeperInfoFile().exists();
    }
    public static boolean isDirectCluster(){
        return getDirectInfoFile().exists();
    }
}