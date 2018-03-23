package com.fr.swift.config.conf;

import com.fr.config.ConfigContext;
import com.fr.config.Configuration;
import com.fr.config.DefaultConfiguration;
import com.fr.config.holder.factory.Holders;
import com.fr.config.holder.impl.ObjectMapConf;
import com.fr.swift.config.IConfigSegment;
import com.fr.transaction.Configurations;
import com.fr.transaction.Worker;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yee
 * @date 2018/3/9
 */
public class SegmentConfig extends DefaultConfiguration {
    private final static String NAMESPACE = "segment_config";

    private static SegmentConfig config = null;

    private ObjectMapConf<Map<String, IConfigSegment>> segmentHolder = Holders.objMap(new HashMap<String, IConfigSegment>(), String.class, IConfigSegment.class);


    public static SegmentConfig getInstance() {
        if (null == config) {
            config = ConfigContext.getConfigInstance(SegmentConfig.class);
        }
        return config;
    }

    public Map<String, IConfigSegment> getAllSegments() {
        return segmentHolder.get();
    }

    public IConfigSegment getSegmentByKey(String key) {
        return (IConfigSegment) segmentHolder.get(key);
    }

    public void putSegments(IConfigSegment... segments) {
        Configurations.update(new Worker() {
            @Override
            public void run() {
                for (IConfigSegment segment : segments) {
                    segmentHolder.put(segment.getSourceKey(), segment);
                }
            }

            @Override
            public Class<? extends Configuration>[] targets() {
                return new Class[] { SegmentConfig.class };
            }
        });

    }

    public void removeSegment(String key) {

        Configurations.update(new Worker() {
            @Override
            public void run() {
                segmentHolder.remove(key);
            }

            @Override
            public Class<? extends Configuration>[] targets() {
                return new Class[] { SegmentConfig.class };
            }
        });
    }

    public void modifySegment(IConfigSegment segment) {
        putSegments(segment);
    }


    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }
}
