package com.fr.bi.common.persistent;

import com.finebi.cube.common.log.BILogger;
import com.finebi.cube.common.log.BILoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by neil on 17-3-20.
 */
public class BIBeanHistoryManager {
    private Map<String, List<String>> beanClassMapping;
    private static final String DEFAULT_FILE_NAME = "bean_history_class.xml";
    private static final BILogger LOGGER = BILoggerFactory.getLogger(BIBeanHistoryManager.class);

    private BIBeanHistoryManager() {
        String filePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + DEFAULT_FILE_NAME;
        if (!new File(filePath).exists()) {
            LOGGER.error("Bean History xml File not found in path " + filePath);
        }
        initBeanHistoryManager(DEFAULT_FILE_NAME);
    }

    private static class BIBeanHistoryManagerSingleton {
        static final BIBeanHistoryManager instance = new BIBeanHistoryManager();
    }

    public static BIBeanHistoryManager getInstance() {
        return BIBeanHistoryManagerSingleton.instance;
    }

    public void initBeanHistoryManager(String beanFilePath) {
        BeanHistoryXMLReader beanHistoryXMLReader = new BeanHistoryXMLReader();
        this.beanClassMapping = beanHistoryXMLReader.loadBeanHistoryMap(beanFilePath);
    }

    public String getCurrentClassName(String oldClass) {
        if (beanClassMapping != null) {
            for (Map.Entry<String, List<String>> entry : beanClassMapping.entrySet()) {
                List<String> oldClasses = entry.getValue();
                if (oldClasses.contains(oldClass)) {
                    return entry.getKey();
                }
            }
        }
        return oldClass;
    }

}
