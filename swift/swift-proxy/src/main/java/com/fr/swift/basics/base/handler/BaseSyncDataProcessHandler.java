package com.fr.swift.basics.base.handler;

import com.fr.swift.basics.InvokerCreater;
import com.fr.swift.basics.annotation.Target;
import com.fr.swift.basics.handler.SyncDataProcessHandler;

import java.lang.reflect.Method;
import java.util.List;

/**
 * TODO 实现
 *
 * @author yee
 * @date 2018/10/30
 */
public abstract class BaseSyncDataProcessHandler<T> extends BaseProcessHandler<T> implements SyncDataProcessHandler {

    public BaseSyncDataProcessHandler(InvokerCreater invokerCreater) {
        super(invokerCreater);
    }

    @Override
    public Object processResult(Method method, Target target, Object... args) throws Throwable {
        return super.processResult(method, target, args);
    }

    @Override
    protected Object mergeResult(List resultList, Object... args) {
        return null;
    }

    @Override
    public T processUrl(Target target, Object... args) {
        // TODO 获取history地址
        return null;
    }
}
