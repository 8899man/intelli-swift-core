package com.fr.swift.event.base;

/**
 * @author yee
 * @date 2018/6/8
 */
public abstract class AbstractRealTimeRpcEvent<T> implements SwiftRpcEvent<T> {

    @Override
    public EventType type() {
        return EventType.REAL_TIME;
    }

    public abstract Event subEvent();

    public enum Event implements SubEvent {
        QUERY, MERGE, RECOVER
    }
}
