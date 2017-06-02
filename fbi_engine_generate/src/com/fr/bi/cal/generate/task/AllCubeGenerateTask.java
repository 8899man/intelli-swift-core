package com.fr.bi.cal.generate.task;

import com.finebi.cube.conf.ICubeGenerateTask;
import com.fr.bi.cal.generate.task.calculator.AllTaskCalculator;
import com.finebi.cube.conf.ITaskCalculator;

/**
 * Created by Lucifer on 2017-5-19.
 *
 * @author Lucifer
 * @since Advanced FineBI Analysis 1.0
 */
public class AllCubeGenerateTask implements ICubeGenerateTask {

    private String taskType = "AllCubeGenerateTask";

    private long userId;

    public AllCubeGenerateTask(long userId) {
        this.userId = userId;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public String getTableSourceId() {
        return null;
    }

    @Override
    public Integer getUpdateType() {
        return null;
    }

    @Override
    public ITaskCalculator getTaskCalculator() {
        return new AllTaskCalculator(this);
    }

    @Override
    public boolean isOk2Merge() {
        return true;
    }

    @Override
    public ICubeGenerateTask merge(ICubeGenerateTask mergeCubeGenerateTask) {
        return this;
    }

    @Override
    public String getTaskInfo() {
        return taskType;
    }
}
