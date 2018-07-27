package com.fr.swift.result.node.resultset;

import com.fr.swift.query.aggregator.Aggregator;
import com.fr.swift.result.GroupNode;
import com.fr.swift.result.NodeMergeResultSet;
import com.fr.swift.result.NodeMergeResultSetImpl;
import com.fr.swift.result.SwiftNode;
import com.fr.swift.result.SwiftNodeUtils;
import com.fr.swift.util.function.Function;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Lyon on 2018/7/26.
 */
class NodeResultSetMerger implements Iterator<NodeMergeResultSet<GroupNode>> {

    private int fetchSize;
    private boolean[] isGlobalIndexed;
    private List<NodeMergeResultSet<GroupNode>> sources;
    private List<Comparator<GroupNode>> comparators;
    private Function<List<NodeMergeResultSet<GroupNode>>, NodeMergeResultSet<GroupNode>> operator;
    private NodeMergeResultSet<GroupNode> remainResultSet;
    // 用于判断是否从源resultSet中更新数据。remainRowCount >= fetchSize不为空，否则为空
    private List<GroupNode> theRowOfRemainNode;
    private List<List<GroupNode>> lastRowOfPrevPages;

    NodeResultSetMerger(int fetchSize, boolean[] isGlobalIndexed, List<NodeMergeResultSet<GroupNode>> sources,
                        List<Aggregator> aggregators, List<Comparator<GroupNode>> comparators) {
        this.fetchSize = fetchSize;
        this.isGlobalIndexed = isGlobalIndexed;
        this.sources = sources;
        this.comparators = comparators;
        this.operator = new MergeOperator(fetchSize, aggregators, comparators);
        init();
    }

    private void init() {
        lastRowOfPrevPages = new ArrayList<List<GroupNode>>(sources.size());
        for (int i = 0; i < sources.size(); i++) {
            lastRowOfPrevPages.add(null);
        }
    }

    private NodeMergeResultSet<GroupNode> updateAll() {
        List<NodeMergeResultSet<GroupNode>> resultSets = new ArrayList<NodeMergeResultSet<GroupNode>>();
        for (int i = 0; i < sources.size(); i++) {
            if (sources.get(i).hasNextPage()) {
                GroupNode node = (GroupNode) sources.get(i).getNode();
                resultSets.add(new NodeMergeResultSetImpl<GroupNode>(fetchSize, node, sources.get(i).getRowGlobalDictionaries()));
                lastRowOfPrevPages.set(i, SwiftNodeUtils.getLastRow(node));
            }
        }
        if (remainResultSet != null) {
            resultSets.add(remainResultSet);
        }
        NodeMergeResultSet<GroupNode> mergeResultSet = operator.apply(resultSets);
        return getPage(mergeResultSet);
    }

    private NodeMergeResultSet<GroupNode> getNext() {
        if (theRowOfRemainNode == null) {
            return updateAll();
        }
        List<NodeMergeResultSet<GroupNode>> newPages = new ArrayList<NodeMergeResultSet<GroupNode>>();
        for (int i = 0; i < sources.size(); i++) {
            if (sources.get(i).hasNextPage()) {
                if (shouldUpdate(lastRowOfPrevPages.get(i))) {
                    GroupNode node = (GroupNode) sources.get(i).getNode();
                    List<Map<Integer, Object>> dict = sources.get(i).getRowGlobalDictionaries();
                    newPages.add(new NodeMergeResultSetImpl<GroupNode>(fetchSize, node, dict));
                    List<GroupNode> lastRow = SwiftNodeUtils.getLastRow(node);
                    lastRowOfPrevPages.set(i, lastRow);
                }
            }
        }
        NodeMergeResultSet<GroupNode> mergeResultSet;
        if (!newPages.isEmpty()) {
            newPages.add(remainResultSet);
            mergeResultSet = operator.apply(newPages);
        } else {
            mergeResultSet = remainResultSet;
        }
        NodeMergeResultSet<GroupNode> page = getPage(mergeResultSet);
        if (SwiftNodeUtils.countRows(page.getNode()) < fetchSize && hasNext()) {
            // 按照前面的规则更新了，但是不满一页，并且源结果集还有剩余，继续取下一页
            remainResultSet = page;
            return getNext();
        }
        return page;
    }

    private boolean shouldUpdate(List<GroupNode> lastRowOfPage) {
        for (int i = 0; i < comparators.size(); i++) {
            if (comparators.get(i).compare(theRowOfRemainNode.get(i), lastRowOfPage.get(i)) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将合并出来的结果拆分成两份，拆分node和字典
     *
     * @param mergeResultSet
     * @return
     */
    private NodeMergeResultSet<GroupNode> getPage(NodeMergeResultSet<GroupNode> mergeResultSet) {
        GroupNode root = (GroupNode) mergeResultSet.getNode();
        GroupNode[] nodes = SwiftNodeUtils.splitNode(root, 2, fetchSize);
        GroupNode page = nodes[0];
        GroupNode remainNode = null;
        theRowOfRemainNode = null;
        if (nodes[1] != null) {
            remainNode = nodes[1];
            if (SwiftNodeUtils.countRows(remainNode) >= fetchSize) {
                theRowOfRemainNode = SwiftNodeUtils.getRow(remainNode, fetchSize - 1);
            }
        }
        List<Map<Integer, Object>> oldDictionary = mergeResultSet.getRowGlobalDictionaries();
        remainResultSet = null;
        if (remainNode != null) {
            remainResultSet = new NodeMergeResultSetImpl<GroupNode>(fetchSize, remainNode, getDictionary(remainNode, oldDictionary));
        }
        return new NodeMergeResultSetImpl<GroupNode>(fetchSize, page, getDictionary(page, oldDictionary));
    }

    private List<Map<Integer, Object>> getDictionary(GroupNode root, List<Map<Integer, Object>> oldDictionary) {
        List<Map<Integer, Object>> dictionary = new ArrayList<Map<Integer, Object>>(oldDictionary.size());
        for (int i = 0; i < oldDictionary.size(); i++) {
            dictionary.add(null);
        }
        Iterator<List<SwiftNode>> rows = SwiftNodeUtils.node2RowListIterator(root);
        while (rows.hasNext()) {
            List<SwiftNode> row = rows.next();
            for (int i = 0; i < row.size(); i++) {
                GroupNode node = (GroupNode) row.get(i);
                if (!isGlobalIndexed[i]) {
                    continue;
                }
                if (dictionary.get(i) == null) {
                    dictionary.set(i, new HashMap<Integer, Object>());
                }
                dictionary.get(i).put(node.getDictionaryIndex(), oldDictionary.get(i).get(node.getDictionaryIndex()));
            }
        }
        return dictionary;
    }

    @Override
    public boolean hasNext() {
        if (remainResultSet != null) {
            return true;
        }
        for (NodeMergeResultSet resultSet : sources) {
            if (resultSet.hasNextPage()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NodeMergeResultSet<GroupNode> next() {
        return getNext();
    }

    @Override
    public void remove() {

    }
}
