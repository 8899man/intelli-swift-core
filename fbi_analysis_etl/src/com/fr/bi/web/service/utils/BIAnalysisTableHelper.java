package com.fr.bi.web.service.utils;

import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.base.BIUser;
import com.fr.bi.etl.analysis.data.AnalysisCubeTableSource;
import com.fr.bi.etl.analysis.manager.BIAnalysisETLManagerCenter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by kary on 17-1-11.
 */
public class BIAnalysisTableHelper {

    public static double getTableGeneratingProcessById(String tableId, long userId) {
        double percent;
        try {
            BusinessTable table = BIAnalysisETLManagerCenter.getBusiPackManager().getTable(tableId, userId);
            percent = getPercent((AnalysisCubeTableSource) table.getTableSource(), userId);
        } catch (Exception e) {
            percent = -1;
        }
        return percent;
    }

    public static double getPercent(AnalysisCubeTableSource source, long userId) {
        double percent;
        Set<AnalysisCubeTableSource> sources = new HashSet<AnalysisCubeTableSource>();
        // 判断Version只需判断自身,如果是AnalysisETLTableSource，则需要同时check自己的parents即AnalysisBaseTableSource
        source.getSourceNeedCheckSource(sources);
        int generated = 0;
        for (AnalysisCubeTableSource s : sources) {
            if(BIAnalysisETLManagerCenter.getUserETLCubeManagerProvider().isError(s,new BIUser(userId))){
                return -1;
            }
            //BILoggerFactory.getLogger(BIAnalysisETLGetGeneratingStatusAction.class).info(" check Version Of " + s.createUserTableSource(userId).fetchObjectCore().getIDValue());
            if (BIAnalysisETLManagerCenter.getUserETLCubeManagerProvider().checkVersion(s, new BIUser(userId))) {
                generated++;
            } else {
                BIAnalysisETLManagerCenter.getUserETLCubeManagerProvider().addTask(s, new BIUser(userId));
            }
        }
        percent = generated == sources.size() ? 1 : (0.1 + 0.9 * generated / sources.size());
        return percent;
    }

    public static boolean isError(AnalysisCubeTableSource source, long userId){
        Set<AnalysisCubeTableSource> sources = new HashSet<AnalysisCubeTableSource>();
        // 判断Version只需判断自身,如果是AnalysisETLTableSource，则需要同时check自己的parents即AnalysisBaseTableSource
        source.getSourceNeedCheckSource(sources);
        for (AnalysisCubeTableSource s : sources) {
            if(BIAnalysisETLManagerCenter.getUserETLCubeManagerProvider().isError(s, new BIUser(userId))){
                return true;
            }
        }
        return false;
    }



    public static boolean getTableHealthById(String tableId, long userId) {
        BusinessTable table = null;
        try {
            table = BIAnalysisETLManagerCenter.getBusiPackManager().getTable(tableId, userId);
            return BIAnalysisETLManagerCenter.getUserETLCubeManagerProvider().isAvailable((AnalysisCubeTableSource) table.getTableSource(), new BIUser(userId));
        } catch (Exception e) {
        }
        return false;
    }

    public static int getTableCubeCount(String tableId, long userId) {
        BusinessTable table = null;
        try {
            table = BIAnalysisETLManagerCenter.getBusiPackManager().getTable(tableId, userId);
            if (table == null) {
                return 0;
            }
            return BIAnalysisETLManagerCenter.getUserETLCubeManagerProvider().getThreadPoolCubeCount((AnalysisCubeTableSource) table.getTableSource(), new BIUser(userId));
        } catch (Exception e) {
        }
        return 0;
    }
}
