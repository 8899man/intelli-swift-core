package com.fr.bi.etl.analysis.manager;

import com.finebi.cube.conf.BIAliasManagerProvider;
import com.finebi.cube.conf.BIDataSourceManagerProvider;
import com.fr.stable.bridge.StableFactory;

/**
 * Created by 小灰灰 on 2016/4/7.
 */
public class BIAnalysisETLManagerCenter {
    public static BIAnalysisBusiPackManagerProvider getBusiPackManager(){
        return StableFactory.getMarkedObject(BIAnalysisBusiPackManagerProvider.XML_TAG, BIAnalysisBusiPackManagerProvider.class);
    }

    public static BIDataSourceManagerProvider getDataSourceManager(){
        return StableFactory.getMarkedObject(BIAnalysisDataSourceManagerProvider.XML_TAG, BIAnalysisDataSourceManagerProvider.class);
    }

    public static UserETLCubeManagerProvider getUserETLCubeManagerProvider(){
        return StableFactory.getMarkedObject(UserETLCubeManager.class.getName(), UserETLCubeManager.class);
    }
    public static UserETLCubeManagerProvider getUserETLCubeCheckManagerProvider(){
        return StableFactory.getMarkedObject(UserETLCubeManagerProvider.class.getName(), UserETLCubeManagerProvider.class);
    }
    public static BIAliasManagerProvider getAliasManagerProvider(){
        return StableFactory.getMarkedObject(BIAliasManagerProvider.class.getName(), BIAliasManagerProvider.class);
    }
}
