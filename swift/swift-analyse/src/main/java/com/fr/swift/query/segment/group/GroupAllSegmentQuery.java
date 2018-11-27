package com.fr.swift.query.segment.group;

import com.fr.swift.query.group.by2.node.NodeGroupByUtils;
import com.fr.swift.query.group.info.GroupByInfo;
import com.fr.swift.query.group.info.MetricInfo;
import com.fr.swift.result.GroupNode;
import com.fr.swift.result.NodeMergeResultSet;
import com.fr.swift.result.NodeMergeResultSetImpl;
import com.fr.swift.result.QueryResultSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by pony on 2017/12/18.
 */
public class GroupAllSegmentQuery extends AbstractGroupSegmentQuery{

    public GroupAllSegmentQuery(GroupByInfo groupByInfo, MetricInfo metricInfo) {
        super(groupByInfo, metricInfo);
    }

    @Override
    public QueryResultSet getQueryResult() {
        Iterator<NodeMergeResultSet<GroupNode>> iterator = NodeGroupByUtils.groupBy(groupByInfo, metricInfo);
        // TODO: 2018/11/27
        return (QueryResultSet) (iterator.hasNext() ? iterator.next() : new NodeMergeResultSetImpl<GroupNode>(groupByInfo.getFetchSize(),
                new GroupNode(-1, null), new ArrayList<Map<Integer, Object>>()));
    }
}
