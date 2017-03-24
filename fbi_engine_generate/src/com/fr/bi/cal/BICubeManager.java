package com.fr.bi.cal;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.BICubeManagerProvider;
import com.finebi.cube.conf.CubeGenerationManager;
import com.finebi.cube.impl.conf.CubeBuildStuffComplete;
import com.fr.bi.cal.generate.CubeBuildHelper;
import com.fr.bi.cal.utils.Single2CollectionUtils;
import com.fr.bi.conf.provider.BIConfigureManagerCenter;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.constant.Status;
import com.fr.bi.stable.engine.CubeTask;
import com.fr.bi.stable.utils.program.BIConstructorUtils;
import com.fr.general.GeneralContext;
import com.fr.stable.EnvChangedListener;
import com.fr.stable.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel
 *         FIXME 功能代码质量严峻
 *         亟待重构
 */
public class BICubeManager implements BICubeManagerProvider {

    private Map<Long, SingleUserCubeManager> userMap = new ConcurrentHashMap<Long, SingleUserCubeManager>();

    public BICubeManager(Map<Long, SingleUserCubeManager> userMap) {
        this.userMap = userMap;
    }

    public BICubeManager() {

    }

    public SingleUserCubeManager getCubeManager(long userId) {
        return BIConstructorUtils.constructObject(userId, SingleUserCubeManager.class, userMap);
    }

    static {
        GeneralContext.addEnvChangedListener(new EnvChangedListener() {
            @Override
            public void envChanged() {
                CubeGenerationManager.getCubeManager().envChanged();
            }
        });
    }


    @Override
    public CubeBuildStuffComplete getGeneratingObject(long userId) {
        return getCubeManager(userId).getGeneratingObject();
    }

    @Override
    public CubeTask getGeneratedTask(long userId) {
        return getCubeManager(userId).getGeneratedTask();
    }

    @Override
    public CubeTask getGeneratingTask(long userId) {
        return getCubeManager(userId).getGeneratingTask();
    }

    @Override
    public boolean checkCubeStatus(long userId) {
        return getCubeManager(userId).checkCubeStatus();
    }

    @Override
    public void setStatus(long userId, Status status) {
        getCubeManager(userId).setStatus(status);
    }

    /**
     * 若存在相同任务则返回false,不添加
     * 添加成功返回true
     *
     * @param userId 用户id
     * @param t      任务
     * @return true或false
     */
    @Override
    public boolean addTask(CubeTask t, long userId) {
        return getCubeManager(userId).addTask(t);
    }

    /**
     * 是否有任务
     *
     * @param t      任务
     * @param userId 用户id
     * @return true或false
     */
    @Override
    public boolean hasTask(CubeTask t, long userId) {
        return getCubeManager(userId).hasTask(t);
    }


    @Override
    public boolean hasTask(long userId) {
        return getCubeManager(userId).hasTask();
    }

    @Override
    public boolean hasTask() {
        boolean result = false;
        for (long userId : userMap.keySet()) {
            result = (result || getCubeManager(userId).hasTask());
        }
        return result;
    }

    @Override
    public boolean hasWaitingCheckTask(long userId) {
        return getCubeManager(userId).hasWaitingCheckTask();
    }

    /**
     * 移除任务
     *
     * @param userId 用户id
     * @param uuid   uuid号
     */
    @Override
    public void removeTask(String uuid, long userId) {
        getCubeManager(userId).removeTask(uuid);
    }

    @Override
    public boolean hasAllTask(long userId) {
        return getCubeManager(userId).hasAllTask();
    }

    @Override
    public boolean hasCheckTask(long userId) {
        return getCubeManager(userId).hasCheckTask();
    }

    @Override
    public Iterator<CubeTask> getWaitingTaskIterator(long userId) {
        return getCubeManager(userId).getWaitingTaskIterator();
    }

    /**
     * 重置cube生成时间
     *
     * @param userId 用户id
     */
    @Override
    public void resetCubeGenerationHour(final long userId) {
        getCubeManager(userId).resetCubeGenerationHour();
    }


    @Override
    public void envChanged() {
        synchronized (this) {
            for (Entry<Long, SingleUserCubeManager> entry : userMap.entrySet()) {
                SingleUserCubeManager manager = entry.getValue();
                if (manager != null) {
                    manager.envChanged();
                }
            }
            userMap.clear();
        }
    }

    @Override
    public boolean cubeTaskBuild(long userId, String baseTableSourceId, int updateType) {
        try {
            if (StringUtils.isEmpty(baseTableSourceId)) {
                CubeBuildHelper.getInstance().CubeBuildStaff(userId);
            } else {
                CubeBuildHelper.getInstance().addCustomTableTask2Queue(userId, Single2CollectionUtils.toList(baseTableSourceId),
                        Single2CollectionUtils.toList(updateType));
            }
            BIConfigureManagerCenter.getCubeConfManager().updatePackageLastModify();
            BIConfigureManagerCenter.getCubeConfManager().updateMultiPathLastCubeStatus(BIReportConstant.MULTI_PATH_STATUS.NOT_NEED_GENERATE_CUBE);
            BIConfigureManagerCenter.getCubeConfManager().persistData(userId);
            return true;
        } catch (Exception e) {
            CubeGenerationManager.getCubeManager().setStatus(userId, Status.WRONG);
            BILoggerFactory.getLogger(this.getClass()).error("FineIndex task build failed" + "\n" + e.getMessage(), e);
            return false;
        } finally {
            CubeGenerationManager.getCubeManager().setStatus(userId, Status.END);
        }

    }

}
