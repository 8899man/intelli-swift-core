package com.finebi.cube.utils;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.structure.Cube;
import com.fr.bi.conf.provider.BIConfigureManagerCenter;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.data.impl.Connection;
import com.fr.general.ComparatorUtils;

import java.io.File;

/**
 * Created by kary on 2016/6/20.
 */
public class CubePreConditionsCheckManager implements CubePreConditionsCheck {

    @Override
    public boolean HDSpaceCheck(File file) {
        if (!file.exists()) {
            return true;
        }
        double dirSize = getDirSize(file);
        double availableSpace = getAvailableSpace(file);
        return (availableSpace > dirSize * 2);
    }

    @Override
    public boolean SQLCheck(Cube cube, CubeTableSource tableSource) {
        return true;
    }

    @Override
    public boolean ConnectionCheck(Connection connection) {
        try {
            connection.testConnection();
            return true;
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            return false;
        }

    }

    @Override
    public boolean envCheck(long userId) {
        String currentJavaVersion = System.getProperty("java.version");
        String lastVersion = BIConfigureManagerCenter.getCubeTaskRecordManager().getLastSysProperties(userId).getJavaVersion();
        if (null == lastVersion) {
            return true;
        }
//        JavaVersion.atOrAboveVersion(lastVersion);
        return ComparatorUtils.equals(currentJavaVersion, lastVersion);
    }

    private double getDirSize(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            double size = 0;
            if (null != children) {
                for (File f : children) {
                    size += getDirSize(f);
                }
            }
            return size;
        } else {
            double size = file.length();
            return size;
        }
    }

    private double getAvailableSpace(File file) {
        return file.getFreeSpace();
    }
}
