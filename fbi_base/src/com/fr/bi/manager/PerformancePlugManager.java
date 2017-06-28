package com.fr.bi.manager;

import com.fr.base.FRContext;
import com.fr.stable.project.ProjectConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Hiram on 2015/3/18.
 */
public class PerformancePlugManager implements PerformancePlugManagerInterface {
    public static final Logger LOGGER = LoggerFactory.getLogger(PerformancePlugManager.class);
    public final static String PERFORMANCE = "performance";
    public final static String LIMIT = "limit";
    public static int DEFAULT_DEPLOY_MODE_ON = 4096;
    public static int DEFAULT_DEPLOY_MODE_OFF = -1;
    protected static PerformancePlugManager ourInstance = new PerformancePlugManager();
    public boolean isInit;
    public Properties properties = null;
    public boolean isControl = false;
    public long timeout = Long.MAX_VALUE;
    private String message = "The server is busy,Please try again later. ";
    public ThreadLocal<LocalAdrrBean> localAdrrThreadLocal = new ThreadLocal<LocalAdrrBean>();
    private Map<String, List<Long>> calculateMap = new ConcurrentHashMap<String, List<Long>>();
    public boolean uniqueThread = false;
    public boolean returnEmptyIndex = false;
    public boolean isSearchPinYin = true;
    public boolean useMultiThreadCal = false;
    public double minCubeFreeHDSpaceRate = 2;
    protected final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    //	private String message = "当前模板计算量大或服务器繁忙，请点击上面清除按钮清除条件或稍后再试";
    public boolean useDiskSort = false;

    public long diskSortDumpThreshold = 1l << 15;

    public int biThreadPoolSize = 10;

    public int biTransportThreadPoolSize = 2;

    public boolean useStandardOutError = false;

    public boolean verboseLog = true;
    private boolean useLog4JPropertiesFile = false;
    private String serverJarLocation = "";
    private int deployModeSelectSize = DEFAULT_DEPLOY_MODE_OFF;
    private int retryMaxTimes = 3;
    private long retryMaxSleepTime = 100;

    private boolean forceWrite = false;

    private boolean useDereplication = true;

    private String PhantomServerIP = "127.0.0.1";
    private int PhantomServerPort = 60810;


    private boolean extremeConcurrency = true;
    private int reIndexRowCount = 1 << 12;

    private long cubeReaderReleaseSleepTime = 1L;
    private boolean isDirectGenerating = false;

    private boolean unmapReader = false;
    private boolean isForceWriter = false;

    //cube单个文件的最大的size
    private long maxCubeFileSize = 8;

    private int maxStructureSize = 0;

    private int maxSPADetailSize = 0;
    protected InputStream in;

    protected PerformancePlugManager() {
        initInputStream();
        initDefaultProperties();
    }

    protected void initInputStream() {
        try {
            in = FRContext.getCurrentEnv().readBean("plugs.properties", ProjectConstants.RESOURCES_NAME);
        } catch (Exception e) {
            LOGGER.warn("use default values of configuration", e);
            in = emptyInputStream();
        }
    }

    public static PerformancePlugManager getInstance() {
        return ourInstance;
    }

    protected void initDefaultProperties() {
        try {
            if (in == null) {
                in = emptyInputStream();
            }
            properties = new Properties();
            properties.load(in);
            setTimeoutConfig(properties);
            returnEmptyIndex = getBoolean(PERFORMANCE + ".emptyWhenNotSelect", false);
            isSearchPinYin = getBoolean(PERFORMANCE + ".isSearchPinYin", true);
            useMultiThreadCal = getBoolean(PERFORMANCE + ".useMultiThreadCal", useMultiThreadCal);
            diskSortDumpThreshold = getLong(PERFORMANCE + ".diskSortDumpThreshold", diskSortDumpThreshold);
            useDiskSort = getBoolean(PERFORMANCE + ".useDiskSort", false);
            biThreadPoolSize = getInt(PERFORMANCE + ".biThreadPoolSize", biThreadPoolSize);
            useStandardOutError = getBoolean(PERFORMANCE + ".useStandardOutError", useStandardOutError);
            verboseLog = getBoolean(PERFORMANCE + ".verboseLog", verboseLog);
            useLog4JPropertiesFile = getBoolean(PERFORMANCE + ".useLog4JPropertiesFile", useLog4JPropertiesFile);
            serverJarLocation = getString(PERFORMANCE + ".serverJarLocation", serverJarLocation);
            deployModeSelectSize = getInt(PERFORMANCE + ".deployModeSelectSize", deployModeSelectSize);
            retryMaxTimes = getInt(PERFORMANCE + ".retryMaxTimes", retryMaxTimes);
            retryMaxSleepTime = getLong(PERFORMANCE + ".retryMaxSleepTime", retryMaxSleepTime);
            extremeConcurrency = getBoolean(PERFORMANCE + ".extremeConcurrency", extremeConcurrency);
            unmapReader = getBoolean(PERFORMANCE + ".unmapReader", unmapReader);
            reIndexRowCount = getInt(PERFORMANCE + ".reIndexRowCount", reIndexRowCount);
            cubeReaderReleaseSleepTime = getLong(PERFORMANCE + ".cubeReaderReleaseSleepTime", cubeReaderReleaseSleepTime);
            isDirectGenerating = getBoolean(PERFORMANCE + ".isDirectGenerating", isDirectGenerating);
            isForceWriter = getBoolean(PERFORMANCE + ".isForceWriter", isForceWriter);
            maxCubeFileSize = getLong(PERFORMANCE + ".maxCubeFileSize", maxCubeFileSize);
            maxStructureSize = getInt(LIMIT + ".maxStructureSize", maxStructureSize);
            maxSPADetailSize = getInt(LIMIT + ".maxSPADetailSize", maxSPADetailSize);
//            logConfiguration();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                }
            }
        }
    }

    protected InputStream emptyInputStream() {
        return new ByteArrayInputStream("".getBytes());
    }


    private void logConfiguration() {
        LOGGER.info("");
        LOGGER.info("");
        LOGGER.info("Properties of FineIndex system can be set through editing the plugs.properties file in the resource folder. " +
                "The current value is displayed  below ");
        LOGGER.info("The value of {}.returnEmptyIndex is {}", PERFORMANCE, returnEmptyIndex);
        LOGGER.info("The value of {}.isSearchPinYin is {}", PERFORMANCE, isSearchPinYin);
        LOGGER.info("The value of {}.useMultiThreadCal is {}", PERFORMANCE, useMultiThreadCal);
        LOGGER.info("The value of {}.diskSortDumpThreshold is {}", PERFORMANCE, diskSortDumpThreshold);
        LOGGER.info("The value of {}.useDiskSort is {}", PERFORMANCE, useDiskSort);
        LOGGER.info("The value of {}.biThreadPoolSize is {}", PERFORMANCE, biThreadPoolSize);
        LOGGER.info("The value of {}.useStandardOutError is {}", PERFORMANCE, useStandardOutError);
        LOGGER.info("The value of {}.verboseLog is {}", PERFORMANCE, verboseLog);
        LOGGER.info("The value of {}.useLog4JPropertiesFile is {}", PERFORMANCE, useLog4JPropertiesFile);
        LOGGER.info("The value of {}.serverJarLocation is {}", PERFORMANCE, serverJarLocation);
        LOGGER.info("The value of {}.deployModeSelectSize is {}", PERFORMANCE, deployModeSelectSize);
        LOGGER.info("The value of {}.retryMaxTimes is {}", PERFORMANCE, retryMaxTimes);
        LOGGER.info("The value of {}.retryMaxSleepTime is {}", PERFORMANCE, retryMaxSleepTime);
        LOGGER.info("The value of {}.cubeReaderReleaseSleepTime is {}", PERFORMANCE, cubeReaderReleaseSleepTime);
        LOGGER.info("The value of {}.unmapReader is {}", PERFORMANCE, unmapReader);
        LOGGER.info("The value of {}.isDirectGenerating is {}", PERFORMANCE, isDirectGenerating);
        LOGGER.info("The value of {}.isForceWriter is {}", PERFORMANCE, isForceWriter);
        LOGGER.info("The value of {}.maxCubeFileSize is {}", PERFORMANCE, maxCubeFileSize);
        LOGGER.info("");
        LOGGER.info("");
    }

    @Override
    public void printSystemParameters() {
        logConfiguration();
    }

    @Override
    public String BIServerJarLocation() {
        return null;
    }

    @Override
    public boolean useStandardOutError() {
        return useStandardOutError;
    }

    @Override
    public int getDeployModeSelectSize() {
        return deployModeSelectSize;
    }

    @Override
    public void setDeployModeSelectSize(int size) {
        deployModeSelectSize = size;
    }

    @Override
    public void setThreadPoolSize(int size) {
        biThreadPoolSize = size;
    }


    @Override
    public int getThreadPoolSize() {
        return biThreadPoolSize;
    }

    @Override
    public void setBiTransportThreadPoolSize(int size) {
        biTransportThreadPoolSize = size;
    }

    @Override
    public boolean verboseLog() {
        return verboseLog;
    }

    @Override
    public boolean useLog4JPropertiesFile() {
        return false;
    }

    private boolean getBoolean(String name, boolean defaultValue) {
        try {
            String property = properties.getProperty(name);
            if (property != null) {
                return Boolean.valueOf(property);
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int getInt(String name, int defaultValue) {
        try {
            String property = properties.getProperty(name);
            if (property != null) {
                return Integer.valueOf(property);
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private long getLong(String name, long defaultValue) {
        try {
            String property = properties.getProperty(name);
            if (property != null) {
                return Long.valueOf(property);
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getString(String name, String defaultValue) {
        try {
            String property = properties.getProperty(name);
            if (property != null) {
                return property;
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void setTimeoutConfig(Properties properties) {
        try {
            String controlTimeout = properties.getProperty(PERFORMANCE + ".controlTimeout");
            isControl = Boolean.parseBoolean(controlTimeout);
            timeout = Long.parseLong(properties.getProperty(PERFORMANCE + ".timeout"));
            String message = properties.getProperty(PERFORMANCE + ".message");
            if (message != null) {
                this.message = message;
            }
        } catch (NumberFormatException e) {
            isControl = false;
            timeout = Long.MAX_VALUE;
        }
    }

    @Override
    public boolean controlTimeout() {
        return isControl;
    }

    @Override
    public boolean isTimeout(long time) {
        return System.currentTimeMillis() - time > timeout;
    }

    @Override
    public String getTimeoutMessage() {
        return message;
    }

    private void put(String localAddr, long startTime) {
        List<Long> startTimeList = calculateMap.get(localAddr);
        if (startTimeList == null) {
            List<Long> list = new ArrayList<Long>();
            list.add(startTime);
            calculateMap.put(localAddr, list);
        } else {
            synchronized (startTimeList) {
                startTimeList.add(startTime);
            }
        }
    }

    @Override
    public void mark(String localAddr, long startTime) {
        localAdrrThreadLocal.set(new LocalAdrrBean(localAddr, startTime));
        put(localAddr, startTime);
    }

    @Override
    public boolean controlUniqueThread() {
        return true;
    }

    public boolean shouldExit() {
        LocalAdrrBean localAdrr = localAdrrThreadLocal.get();
        if (localAdrr == null) {
            return false;
        }
        List<Long> startTimeList = calculateMap.get(localAdrr.getLocalAddr());
        long startTime = localAdrr.getStartTime();
        synchronized (startTimeList) {
            for (long time : startTimeList) {
                if (time > startTime) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void checkExit() {
        if (shouldExit()) {
            removeStartTime();
            exit();
        }
    }

    private void removeStartTime() {
        LocalAdrrBean localAdrr = localAdrrThreadLocal.get();
        if (localAdrr == null) {
            return;
        }
        List<Long> startTimeList = calculateMap.get(localAdrr.getLocalAddr());
        startTimeList.remove(localAdrr.getStartTime());
    }

    private void exit() {
        throw new RuntimeException("Duplicate calculation.");
//		throw new RuntimeException("同时只能进行一次分析计算");
    }

    @Override
    public boolean isReturnEmptyIndex() {
        return returnEmptyIndex;
    }

    @Override
    public boolean isSearchPinYin() {
        return isSearchPinYin;
    }


    @Override
    public boolean isUseDiskSort() {
        return useDiskSort;
    }

    @Override
    public long getDiskSortDumpThreshold() {
        return diskSortDumpThreshold;
    }


    public boolean isForceMapBufferWrite() {
        return forceWrite;
    }

    public boolean useDereplication() {
        return useDereplication;
    }

    public boolean isUseMultiThreadCal() {
        return useMultiThreadCal;
    }

    public int getBiThreadPoolSize() {
        return biThreadPoolSize;
    }

    @Override
    public int getBiTransportThreadPoolSize() {
        return biTransportThreadPoolSize;
    }

    public int getRetryMaxTimes() {
        return retryMaxTimes;
    }

    public long getRetryMaxSleepTime() {
        return retryMaxSleepTime;
    }

    @Override
    public void setPhantomServerIP(String ip) {
        PhantomServerIP = ip;
    }

    @Override
    public String getPhantomServerIP() {
        return PhantomServerIP;
    }

    @Override
    public void setPhantomServerPort(int port) {
        PhantomServerPort = port;
    }

    @Override
    public int getPhantomServerPort() {
        return PhantomServerPort;
    }

    @Override
    public boolean isExtremeConcurrency() {
        return extremeConcurrency;
    }

    @Override
    public int getReIndexRowCount() {
        return reIndexRowCount;
    }

    @Override
    public void setMinCubeFreeHDSpaceRate(double rate) {
        minCubeFreeHDSpaceRate = rate;
    }

    @Override
    public double getMinCubeFreeHDSpaceRate() {
        return minCubeFreeHDSpaceRate;
    }

    @Override
    public long getCubeReaderReleaseSleepTime() {
        return cubeReaderReleaseSleepTime;
    }

    @Override
    public boolean isDirectGenerating() {
        return isDirectGenerating;
    }

    @Override
    public boolean isForceWriter() {
        return isForceWriter;
    }


    public boolean isUnmapReader() {
        return unmapReader;
    }

    public void setUnmapReader(boolean unmapReader) {
        this.unmapReader = unmapReader;
    }

    @Override
    public long getMaxCubeFileSize() {
        return maxCubeFileSize;
    }

    @Override
    public int getMaxStructureSize() {
        return maxStructureSize;
    }

    @Override
    public int getMaxSPADetailSize() {
        return maxSPADetailSize;
    }

    public void setUseDiskSort(boolean useDiskSort) {
        this.useDiskSort = useDiskSort;
    }

    public void setUseMultiThreadCal(boolean useMultiThreadCal) {
        this.useMultiThreadCal = useMultiThreadCal;
    }

    /**
     * @return
     */
    @Override
    public Map<String, Object> getAllConf() {
        Map<String, Object> confMap = new HashMap<String, Object>();
        confMap.put("useMultiThreadCal", useMultiThreadCal);
        confMap.put("useDiskSort", useDiskSort);
        return confMap;
    }

    /*
    * properties文件保存成功后再修该内存中的参数，以免出现内存和文件不一致的情况
    * */
    @Override
    public boolean resetConf(Map<String, Object> configs) {
        LOGGER.info("start reset system configurations……" + configs.toString());
        boolean res = saveProperties(configs);
        if (res) {
            setMemoryConfigs(configs);
        }
        return res;
    }

    protected boolean saveProperties(Map<String, Object> configs) {
        OutputStream out = null;
        try {
            readWriteLock.writeLock().lock();
            out = FRContext.getCurrentEnv().writeBean("plugs.properties", ProjectConstants.RESOURCES_NAME);
            Properties properties = new Properties();
            for (String key : configs.keySet()) {
                if (PropertyValidiationCheck(key)) {
                    properties.setProperty(PERFORMANCE+"."+key, String.valueOf(configs.get(key)));
                }
            }
            properties.store(out, "");
        } catch (Exception e) {
            LOGGER.error("save system configurations failed" + e.getMessage(), e);
            return false;
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            readWriteLock.writeLock().unlock();
        }
        return true;
    }

    /*
    * 过滤脏属性,不在该manager的属性都过滤掉
    * */
    protected boolean PropertyValidiationCheck(String key) {
        try {
            this.getClass().getField(key);
            return true;
        } catch (NoSuchFieldException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    protected void setMemoryConfigs(Map<String, Object> configs) {
        Object useMultiThreadCal = configs.containsKey(PERFORMANCE + ".useMultiThreadCal") ? configs.get(PERFORMANCE + ".useMultiThreadCal") : this.useMultiThreadCal;
        Object diskSort = configs.containsKey(PERFORMANCE + ".useDiskSort") ? configs.get(PERFORMANCE + ".useDiskSort") : this.useDiskSort;
        setUseDiskSort((Boolean) diskSort);
        setUseMultiThreadCal((Boolean) useMultiThreadCal);
    }
}
