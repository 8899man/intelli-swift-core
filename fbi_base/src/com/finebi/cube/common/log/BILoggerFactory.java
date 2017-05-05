package com.finebi.cube.common.log;

import com.fr.general.FRLogger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BI日志输出
 */
public class BILoggerFactory {
    static {
        /**
         * 添加默认Log配置
         */
        PropertyConfigurator.configure(defaultProperties());
//        if (PerformancePlugManager.getInstance().useLog4JPropertiesFile()) {
        URL resource = Loader.getResource("log4j.properties");
        if (resource != null) {
            PropertyConfigurator.configure(resource);
            FRLogger.getLogger().info("The log properties url:" + resource.toString());
        }
//        }
    }

    public static Map<Class, BILogger> loggerMap = new HashMap<Class, BILogger>();
    public static Map<String, Map<String, Object>> loggerCacheMap = new ConcurrentHashMap<String, Map<String, Object>>();

    public static BILogger getLogger(Class clazz) {
        if (loggerMap.containsKey(clazz)) {
            return loggerMap.get(clazz);
        } else {
            synchronized (BILoggerFactory.class) {
                if (!loggerMap.containsKey(clazz)) {
                    BILogger logger = new BILogger(LoggerFactory.getLogger(clazz));
                    loggerMap.put(clazz, logger);
                }
                return loggerMap.get(clazz);
            }
        }
    }

    public static BILogger getLogger() {
        return getLogger(BILogger.class);
    }

    public static InputStream defaultProperties() {
        String str = "log4j.rootLogger=INFO, ServerDailyRollingFile, stdout\n" +
                "\n" +
                "log4j.appender.ServerDailyRollingFile=org.apache.log4j.DailyRollingFileAppender\n" +
                "\n" +
                "log4j.appender.ServerDailyRollingFile.DatePattern='.'yyyy-MM-dd\n" +
                "\n" +
                "log4j.appender.ServerDailyRollingFile.File=logs/notify-subscription.log\n" +
                "\n" +
                "log4j.appender.ServerDailyRollingFile.layout=org.apache.log4j.PatternLayout\n" +
                "\n" +
                "log4j.appender.ServerDailyRollingFile.layout.ConversionPattern=%d - %m%n\n" +
                "\n" +
                "log4j.appender.ServerDailyRollingFile.Append=true\n" +
                "\n" +
                "log4j.appender.stdout=org.apache.log4j.ConsoleAppender\n" +
                "\n" +
                "log4j.appender.stdout.layout=org.apache.log4j.PatternLayout\n" +
                "\n" +
                "log4j.appender.stdout.layout.ConversionPattern=%d{yyyy-MM-dd HH\\:mm\\:ss} %p [%c] %m%n";

        return new ByteArrayInputStream(str.getBytes());
    }


    public static void cacheLoggerInfo(String cacheTag, String cacheSubTag, Object cacheContent) {
        Map<String, Object> cacheMap = BILoggerFactory.getSpecificCacheMap(cacheTag);
        cacheMap.put(cacheSubTag, cacheContent);
    }

    public static Map<String, Object> getSpecificCacheMap(String cacheTag) {
        if (loggerCacheMap.containsKey(cacheTag)) {
            return loggerCacheMap.get(cacheTag);
        } else {
            synchronized (BILoggerFactory.class) {
                if (!loggerCacheMap.containsKey(cacheTag)) {
                    loggerCacheMap.put(cacheTag, new HashMap<String, Object>());
                }
                return loggerCacheMap.get(cacheTag);
            }
        }
    }

    public static Object getLoggerCacheValue(String cacheTag, String cacheSubTag) {
        if (!loggerCacheMap.containsKey(cacheTag)) {
//            getLogger(BILoggerFactory.class).warn("\n" + "The LoggerInfoCache does not contains the cacheTag: " + cacheTag);
            return null;
        }

        Map specificCacheMap = getSpecificCacheMap(cacheTag);
        if (!specificCacheMap.containsKey(cacheSubTag)) {
//            getLogger(BILoggerFactory.class).warn("\n" + "The LoggerInfoCache contains the cacheTag: " + cacheTag + " but does not contains the subTag: " + cacheSubTag);
            return null;
        }
        return specificCacheMap.get(cacheSubTag);
    }

    public static void clearLoggerCacheValue(String cacheTag) {
        if (loggerCacheMap.containsKey(cacheTag)) {
            loggerCacheMap.get(cacheTag).clear();
            loggerCacheMap.remove(cacheTag);
        }
    }


}
