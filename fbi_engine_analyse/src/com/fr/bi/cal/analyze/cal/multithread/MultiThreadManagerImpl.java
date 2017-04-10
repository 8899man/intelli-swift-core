package com.fr.bi.cal.analyze.cal.multithread;

import com.fr.bi.cal.analyze.session.BISession;
import com.fr.bi.manager.PerformancePlugManager;

/**
 * Created by Hiram on 2015/5/14.
 */
public class MultiThreadManagerImpl {
    private static MultiThreadManagerImpl ourInstance = new MultiThreadManagerImpl();
    private static ThreadLocal<BIMultiThreadExecutor> executorServiceThreadLocal = new ThreadLocal<BIMultiThreadExecutor>();

    private MultiThreadManagerImpl() {
    }

    public static boolean isMultiCall() {
        return PerformancePlugManager.getInstance().isUseMultiThreadCal();
    }

    public static MultiThreadManagerImpl getInstance() {
        return ourInstance;
    }

    /**
     * 刷新当前线程的ExecutorService,一般web容器都会有线程池，用ThreadLocal之前要先清掉
     */

    public void refreshExecutorService() {
        if (!isMultiCall()) {
            return;
        }
        executorServiceThreadLocal.set(null);
    }


    public BIMultiThreadExecutor getExecutorService() {
        BIMultiThreadExecutor executorService = executorServiceThreadLocal.get();
        if (executorService == null) {
            executorService = createNewExecutorServer();
            executorServiceThreadLocal.set(executorService);
        }
        return executorService;
    }


    public BIMultiThreadExecutor createNewExecutorServer() {
        return new BIMultiThreadExecutor();
    }


    public void awaitExecutor(BISession session) {
        if (!isMultiCall()) {
            return;
        }
        BIMultiThreadExecutor executorService = executorServiceThreadLocal.get();
        if (executorService != null){
            executorService.awaitExecutor(session);
            refreshExecutorService();
        }
    }

}