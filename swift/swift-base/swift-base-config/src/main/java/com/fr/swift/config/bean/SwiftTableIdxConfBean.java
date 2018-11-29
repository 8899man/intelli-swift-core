package com.fr.swift.config.bean;

import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.source.alloter.AllotRule;
import com.fr.swift.util.Crasher;

import java.lang.reflect.Constructor;

/**
 * @author yee
 * @date 2018-11-26
 */
public class SwiftTableIdxConfBean implements ObjectConverter {
    public static final Class TYPE = entityType();
    private String tableKey;
    private AllotRule allotRule;

    public SwiftTableIdxConfBean() {
    }

    public SwiftTableIdxConfBean(String id, AllotRule allotRule) {
        this.tableKey = id;
        this.allotRule = allotRule;
    }

    private static Class entityType() {
        try {
            return Class.forName("com.fr.swift.config.entity.SwiftTableIndexingConf");
        } catch (ClassNotFoundException e) {
            return Crasher.crash(e);
        }
    }

    public String getTableKey() {
        return tableKey;
    }

    public void setTableKey(String tableKey) {
        this.tableKey = tableKey;
    }

    public AllotRule getAllotRule() {
        return allotRule;
    }

    public void setAllotRule(AllotRule allotRule) {
        this.allotRule = allotRule;
    }

    @Override
    public Object convert() {
        try {
            Constructor constructor = TYPE.getDeclaredConstructor(SwiftTableIdxConfBean.class);
            return constructor.newInstance(this);
        } catch (Exception e) {
            SwiftLoggers.getLogger().error(e);
        }
        return null;
    }
}
