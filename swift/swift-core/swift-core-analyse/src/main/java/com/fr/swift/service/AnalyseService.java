package com.fr.swift.service;

import com.fr.swift.basics.annotation.InvokeMethod;
import com.fr.swift.basics.annotation.Target;
import com.fr.swift.basics.handler.CommonProcessHandler;
import com.fr.swift.basics.handler.QueryableProcessHandler;
import com.fr.swift.result.qrs.QueryResultSet;
import com.fr.swift.segment.SegmentLocationInfo;

import java.io.Serializable;
import java.util.List;

/**
 * @author yee
 * @date 2018/6/13
 */
public interface AnalyseService extends SwiftService, Serializable {

    /**
     * 方法调用不区分本地还是远程，转发逻辑在QueryableProcessHandler的实现中处理
     *
     * @param queryJson 查询字符串
     * @return
     * @throws Exception
     */
    @InvokeMethod(value = QueryableProcessHandler.class, target = Target.ANALYSE)
    QueryResultSet getQueryResult(String queryJson) throws Exception;

    // TODO: 2018/12/5 增删改可以合并成一个接口
    @InvokeMethod(value = CommonProcessHandler.class, target = Target.ANALYSE)
    void updateSegmentInfo(SegmentLocationInfo locationInfo, SegmentLocationInfo.UpdateType updateType);

    @InvokeMethod(value = CommonProcessHandler.class, target = Target.ANALYSE)
    void removeSegments(String clusterId, String sourceKey, List<String> segmentKeys);
}
