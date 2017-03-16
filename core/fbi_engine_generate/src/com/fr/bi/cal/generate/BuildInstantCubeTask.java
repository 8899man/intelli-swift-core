package com.fr.bi.cal.generate;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.CubeBuildStuff;
import com.fr.bi.base.BIUser;
import com.fr.bi.cal.TempCubeManager;
import com.fr.bi.conf.provider.BIConfigureManagerCenter;
import com.fr.bi.stable.data.db.PersistentTable;
import com.fr.bi.stable.engine.CubeTaskType;
import com.fr.general.DateUtils;

import java.util.concurrent.Future;

/**
 * Created by roy on 16/10/10.
 */
public class BuildInstantCubeTask extends BuildCubeTask {
    private TempCubeManager manager;

    public BuildInstantCubeTask(BIUser biUser, CubeBuildStuff cubeBuild, TempCubeManager tempCubeManager) {
        super(biUser, cubeBuild);
        manager = tempCubeManager;
    }


    @Override
    public void end() {
        Future<String> result = finishObserver.getOperationResult();
        try {
            String message = result.get();
            BILoggerFactory.getLogger().info(message);
            boolean cubeBuildSucceed = finishObserver.success();
            if (!cubeBuildSucceed) {
                checkTaskFinish();
            }

            if (cubeBuildSucceed) {
                cube.addVersion(System.currentTimeMillis());
                long start = System.currentTimeMillis();
                manager.finishGenerateCube();
                BILoggerFactory.getLogger().info("Instance FineIndex successful! Cost :" + DateUtils.timeCostFrom(start));

            } else {
                message = "FineIndex build failed ,the FineIndex files will not be replaced ";
                BIConfigureManagerCenter.getLogManager().errorTable(new PersistentTable("", "", ""), message, biUser.getUserId());
                BILoggerFactory.getLogger().error(message);
            }
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        } finally {

        }
    }

    @Override
    public CubeTaskType getTaskType() {
        return CubeTaskType.INSTANT;
    }
}
