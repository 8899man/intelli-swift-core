package com.fr.bi.manager;

import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.base.FRContext;
import com.fr.stable.project.ProjectConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Hiram on 2015/3/18.
 */
public class PerformancePlugManager implements PerformancePlugManagerInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformancePlugManager.class);
    private final static String PERFORMANCE = "performance";
    private final static String LIMIT = "limit";
    public static int DEFAULT_DEPLOY_MODE_ON = 4096;
    public static int DEFAULT_DEPLOY_MODE_OFF = -1;
    private static PerformancePlugManager ourInstance = new PerformancePlugManager();
    private boolean isInit;
    private Properties properties = null;
    private boolean isControl = false;
    private long timeout = Long.MAX_VALUE;
    private String message = "The server is busy,Please try again later. ";
    private ThreadLocal<LocalAdrrBean> localAdrrThreadLocal = new ThreadLocal<LocalAdrrBean>();
    private Map<String, List<Long>> calculateMap = new ConcurrentHashMap<String, List<Long>>();
    private boolean uniqueThread = false;
    private boolean returnEmptyIndex = false;
    private boolean isSearchPinYin = true;
    private boolean useMultiThreadCal = false;
    private double minCubeFreeHDSpaceRate = 2;
    private String filePath = FRContext.getCurrentEnv().getPath();
    private static final String OLD_FILE_NAME = "plugs.properties";
    private static final String NEW_FILE_NAME = "plugsUpdate.properties";
    private File oldFile = new File(filePath + File.separator + ProjectConstants.RESOURCES_NAME + File.separator + OLD_FILE_NAME);
    private boolean isControlMaxMemory = false;
    private BIPerformanceParamConfig config = new BIPerformanceParamConfig();

    //	private String message = "当前模板计算量大或服务器繁忙，请点击上面清除按钮清除条件或稍后再试";
    private boolean diskSort = false;

    private long diskSortDumpThreshold = 1l << 15;

    private int biThreadPoolSize = 10;

    private int biTransportThreadPoolSize = 2;

    private boolean useStandardOutError = false;

    private boolean verboseLog = true;
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

    private int maxNodeCount = Integer.MAX_VALUE;

    private PerformancePlugManager() {
        init();
    }

    public static PerformancePlugManager getInstance() {
        return ourInstance;
    }

    private void init() {
        try {
            InputStream in = null;
            try {
                in = FRContext.getCurrentEnv().readBean("plugs.properties", ProjectConstants.RESOURCES_NAME);
            } catch (Exception e) {
                LOGGER.warn("use default values of configuration", e);
                in = emptyInputStream();
            }
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
            diskSort = getBoolean(PERFORMANCE + ".useDiskSort", false);
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
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
    }

    /**
     * 先整理需要更新的数据，然后更新参数配置信息
     *
     * @param resultMap
     * @return
     */
    @Override
    public boolean updateParam(Map<String, String> resultMap) {
        try {
            Map<String, String> doUpdateMap = new HashMap<String, String>();
            Map<String, String> runMap = getExtraParam("run");
            Map<String, String> newMap = getExtraParam("new");
            Map<String, String> deafultMap = PerformanceParamTools.convertParamKey(getDefaultConfig());
            resultMap = PerformanceParamTools.convertParamKey(resultMap);
            resultMap = config.beforeDoWrite(runMap, newMap, resultMap);
            Iterator<String> it = resultMap.keySet().iterator();
            while (it.hasNext()) {
                String paramKey = it.next();
                String defaultValue = deafultMap.get(paramKey);
                String newValue = resultMap.get(paramKey);
                if (!defaultValue.equals(newValue)) {
                    doUpdateMap.put(paramKey, newValue);
                    continue;
                }
            }
            return config.writeConfig(doUpdateMap, FRContext.getCurrentEnv().writeBean(NEW_FILE_NAME, ProjectConstants.RESOURCES_NAME));
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * 获取系统默认配置
     *
     * @return
     */
    @Override
    public Map<String, String> getDefaultConfig() {
        Map<String, String> defaultMap = new HashMap<String, String>();
//        defaultMap.put("returnEmptyIndex", String.valueOf(false));
//        defaultMap.put("isSearchPinYin", String.valueOf(true));
//        defaultMap.put("isGetTemplateScreenCapture", String.valueOf(true));
//        defaultMap.put("isControlMaxMemory", String.valueOf(false));
//        defaultMap.put("useMultiThreadCal", String.valueOf(false));
//        defaultMap.put("maxNodeCount", String.valueOf(Integer.MAX_VALUE));
//        defaultMap.put("diskSortDumpThreshold", String.valueOf(11 << 15));
//        defaultMap.put("diskSort", String.valueOf(false));
//        defaultMap.put("biThreadPoolSize", String.valueOf(1));
//        defaultMap.put("biTransportThreadPoolSize", String.valueOf(2));
//        defaultMap.put("useStandardOutError", String.valueOf(false));
//        defaultMap.put("verboseLog", String.valueOf(true));
//        defaultMap.put("useLog4JPropertiesFile", String.valueOf(false));
//        defaultMap.put("serverJarLocation", String.valueOf(""));
//        defaultMap.put("deployModeSelectSize", String.valueOf(DEFAULT_DEPLOY_MODE_OFF));
//        defaultMap.put("retryMaxTimes", String.valueOf(3));
//        defaultMap.put("retryMaxSleepTime", String.valueOf(100));
//        defaultMap.put("minCubeFreeHDSpaceRate", String.valueOf(2));
//        defaultMap.put("cubeReaderReleaseSleepTime", String.valueOf(1L));
        return defaultMap;
    }

    /**
     * 通过参数类型，获取额外的参数配置信息
     *
     * @param paramType
     * @return
     */
    @Override
    public Map<String, String> getExtraParam(String paramType) {
        String readFileName = null;
        if ("run".equals(paramType)) {
            readFileName = OLD_FILE_NAME;
        }
        if ("new".equals(paramType)) {
            readFileName = NEW_FILE_NAME;
        }
        Map<String, String> paramConfig = new HashMap<String, String>();
        try {
            paramConfig = config.readConfig(FRContext.getCurrentEnv().readBean(readFileName, ProjectConstants.RESOURCES_NAME));
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
        return paramConfig;
    }

    /**
     * 判断参数是否属于字段信息
     *
     * @param resultMap
     * @return
     */
    @Override
    public boolean isBelongTo(Map<String, String> resultMap) {
        return PerformanceParamTools.isBelongConfig(this, resultMap);
    }

    private InputStream emptyInputStream() {
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
        LOGGER.info("The value of {}.useDiskSort is {}", PERFORMANCE, diskSort);
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

    /**
     * 通过不同的paramType，可以获取配置
     * paramType == "new"  更新成功后，系统重启的参数配置
     * paramType == "run"  本次系统启动时，额外的参数配置
     *
     * @param paramType
     * @return
     */
    @Override
    public Map<String, String> getConfigByType(String paramType) {
        String fileName = null;
        Map<String, String> newMap = new HashMap<String, String>();
        if ("run".equals(paramType)) {
            fileName = OLD_FILE_NAME;
        }
        if ("new".equals(paramType)) {
            fileName = NEW_FILE_NAME;
        }
        try {
            InputStream in = FRContext.getCurrentEnv().readBean(fileName, ProjectConstants.RESOURCES_NAME);
            if (in != null) {
                properties = new Properties();
                properties.load(in);
                setTimeoutConfig(properties);
                newMap.put("returnEmptyIndex", getString(PERFORMANCE + ".emptyWhenNotSelect", String.valueOf(false)));
                newMap.put("isSearchPinYin", getString(PERFORMANCE + ".isSearchPinYin", String.valueOf(true)));
                newMap.put("isGetTemplateScreenCapture", getString(PERFORMANCE + ".isGetTemplateScreenCapture", String.valueOf(true)));
                newMap.put("isControlMaxMemory", getString(PERFORMANCE + ".isControlMaxMemory", String.valueOf(isControlMaxMemory)));
                newMap.put("useMultiThreadCal", getString(PERFORMANCE + ".useMultiThreadCal", String.valueOf(useMultiThreadCal)));
                newMap.put("maxNodeCount", getString(PERFORMANCE + ".maxNodeCount", String.valueOf(maxNodeCount)));
                newMap.put("diskSortDumpThreshold", getString(PERFORMANCE + ".diskSortDumpThreshold", String.valueOf(diskSortDumpThreshold)));
                newMap.put("diskSort", getString(PERFORMANCE + ".useDiskSort", String.valueOf(false)));
                newMap.put("biThreadPoolSize", getString(PERFORMANCE + ".biThreadPoolSize", String.valueOf(biThreadPoolSize)));
                newMap.put("biTransportThreadPoolSize", getString(PERFORMANCE + ".biTransportThreadPoolSize", String.valueOf(biTransportThreadPoolSize)));
                newMap.put("useStandardOutError", getString(PERFORMANCE + ".useStandardOutError", String.valueOf(useStandardOutError)));
                newMap.put("verboseLog", getString(PERFORMANCE + ".verboseLog", String.valueOf(verboseLog)));
                newMap.put("useLog4JPropertiesFile", getString(PERFORMANCE + ".useLog4JPropertiesFile", String.valueOf(useLog4JPropertiesFile)));
                newMap.put("serverJarLocation", getString(PERFORMANCE + ".serverJarLocation", String.valueOf(serverJarLocation)));
                newMap.put("deployModeSelectSize", getString(PERFORMANCE + ".deployModeSelectSize", String.valueOf(deployModeSelectSize)));
                newMap.put("retryMaxTimes", getString(PERFORMANCE + ".retryMaxTimes", String.valueOf(retryMaxTimes)));
                newMap.put("retryMaxSleepTime", getString(PERFORMANCE + ".retryMaxSleepTime", String.valueOf(retryMaxSleepTime)));
                newMap.put("minCubeFreeHDSpaceRate", getString(PERFORMANCE + ".minCubeFreeHDSpaceRate", String.valueOf(minCubeFreeHDSpaceRate)));
                newMap.put("cubeReaderReleaseSleepTime", getString(PERFORMANCE + ".cubeReaderReleaseSleepTime", String.valueOf(cubeReaderReleaseSleepTime)));
            }
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
        return newMap;
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
    public boolean isDiskSort() {
        return diskSort;
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
}
