package com.fr.bi.cal.generate.timerTask;

import com.fr.bi.cal.generate.timerTask.quartz.JobTask;
import com.fr.bi.cal.generate.timerTask.quartz.QuartzManager;
import com.fr.bi.stable.utils.code.BILogger;
import com.fr.bi.stable.utils.program.BINonValueUtils;
import com.fr.third.org.quartz.SchedulerException;

import java.util.List;

/**
 * Created by Kary on 2016/6/28.
 */
public class BICubeTimeTaskCreatorManager implements BICubeTimeTaskCreatorProvider {
    public BICubeTimeTaskCreatorManager() {
    }

    @Override
    public void reGenerateTimeTasks(long userId, List<TimerTaskSchedule> scheduleList) {
        for (TimerTaskSchedule schedule : scheduleList) {
            JobTask jobTask = new JobTask();
            try {
                QuartzManager.addJob(jobTask, schedule);
            } catch (Exception e) {
                BILogger.getLogger().error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void removeAllTimeTasks(long userId, List<TimerTaskSchedule> scheduleList) {
        try {
            QuartzManager.removeAllJobs();
        } catch (SchedulerException e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }


}
