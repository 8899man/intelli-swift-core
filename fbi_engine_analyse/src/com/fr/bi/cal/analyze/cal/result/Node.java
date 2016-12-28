package com.fr.bi.cal.analyze.cal.result;

import com.fr.bi.cal.analyze.cal.utils.CubeReadingUtils;
import com.fr.bi.conf.report.widget.field.dimension.BIDimension;
import com.fr.bi.conf.report.widget.field.dimension.filter.DimensionFilter;
import com.fr.bi.stable.constant.BIBaseConstant;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.key.TargetGettingKey;
import com.fr.bi.stable.report.result.*;
import com.fr.bi.stable.structure.collection.map.ChildsMap;
import com.fr.general.ComparatorUtils;
import com.fr.general.NameObject;
import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhou
 */
public class Node implements BINode {

    /**
     * 小于1表示不分页
     */
    public static final int NONE_PAGE_LEVER = 0;
    /**
     *
     */
    private static final long serialVersionUID = -3230643843227594588L;
    /**
     * 子节点
     */
    volatile ChildsMap<Node> childs;
    /**
     *
     */
    private Object data;
    /**
     * 父亲节点
     */
    private Node parent;
    /**
     * 兄弟节点
     */
    private Node sibling;
    //ConcurrentHashMap需要支持高并发访问，不一定是该map高并发,当node过多时也需要高并发
    private volatile Map summaryValue = new ConcurrentHashMap(1);
    //ConcurrentHashMap需要支持高并发访问，不一定是该map高并发,当node过多时也需要高并发
    private volatile Map<TargetGettingKey, GroupValueIndex> targetIndexValueMap;
    private volatile Map<TargetGettingKey, GroupValueIndex> gviMap;
    private GroupValueIndex gvi;
    private String showValue;
    private Comparator c = BIBaseConstant.COMPARATOR.COMPARABLE.ASC;
    private transient Map<TargetGettingKey, Double> childAVG;
    private transient Map<TargetGettingKey, Double> allChildAVG;
    //TODO 低效的算法， 放在result无所谓
    private transient Map<TopNKey, Double> topNLineMap;

    public Node(Comparator comparator, Object data) {
        this.c = comparator;
        this.setData(data);
        childs = new ChildsMap<Node>();
    }

    @Override
    public Comparator getComparator() {
        return c;
    }

    @Override
    public void setComparator(Comparator c) {
        this.c = c;
    }

    /**
     * todo
     *
     * @param data
     */
    private void initShowValue(Object data) {
        setShowValue(data == null ? null : data.toString());
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public void setParent(LightNode parent) {
        this.parent = castNode(parent);
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public Node getSibling() {
        return sibling;
    }

    @Override
    public void setSibling(LightNode sibling) {
        this.sibling = castNode(sibling);
    }

    @Override
    public String getShowValue() {
        if (showValue == null) {
            initShowValue(data);
        }
        return showValue;
    }

    @Override
    public void setShowValue(String showValue) {
        this.showValue = showValue;
    }

    public GroupValueIndex getGroupValueIndex() {
        return gvi;
    }

    /**
     * 添加子节点
     *
     * @param child 子节点
     */
    @Override
    public void addChild(LightNode child) {
        Node node = castNode(child);
        node.setParent(this);
        if (!childs.isEmpty()) {
            Node lastNode = getLastChild();
            lastNode.setSibling(node);
        }
        childs.put(node.getData(), node);
    }

    private Node castNode(LightNode lightNode) {
        return (Node) lightNode;
    }

    @Override
    public Node getLastChild() {
        return childs.getLastValue();
    }

    @Override
    public Map<TargetGettingKey, GroupValueIndex> getTargetIndexValueMap() {
        if (targetIndexValueMap == null) {
            targetIndexValueMap = new ConcurrentHashMap<TargetGettingKey, GroupValueIndex>(1);
        }
        return targetIndexValueMap;
    }

    @Override
    public void setTargetIndexValueMap(Map<TargetGettingKey, GroupValueIndex> targetIndexValueMap) {
        this.targetIndexValueMap = targetIndexValueMap;
    }

    @Override
    public Map<TargetGettingKey, GroupValueIndex> getGroupValueIndexMap() {
        if (gviMap == null) {
            gviMap = new ConcurrentHashMap<TargetGettingKey, GroupValueIndex>(1);
        }
        return gviMap;
    }

    @Override
    public void setGroupValueIndexMap(Map<TargetGettingKey, GroupValueIndex> gviMap) {
        this.gviMap = gviMap;
    }

    @Override
    public Node getFirstChild() {
        return childs.getFirstValue();
    }

    @Override
    public int getChildLength() {
        if (childs == null) {
            return 0;
        }
        return childs.size();
    }

    @Override
    public Node getChild(int index) {
        return childs.get(index);
    }

    @Override
    public Node getChild(Object data) {
        return childs.get(data);
    }

    @Override
    public List<Node> getChilds() {
        return childs.values();
    }

    @Override
    public int getTotalLength() {
        if (childs.isEmpty()) {
            return 1;
        }
        int res = 0;
        for (int i = 0; i < childs.size(); i++) {
            Node node = childs.get(i);
            res += node.getTotalLength();
        }
        return res;
    }

    public int getTotalLengthWithSummary() {
        int res = 1;
        for (int i = 0; i < childs.size(); i++) {
            Node node = childs.get(i);
            res += node.getTotalLengthWithSummary();
        }
        if (!needSummary()) {
            res--;
        }
        return Math.max(res, 1);
    }

    public int getTotalLengthWithSummary(NodeExpander expander) {
        if (expander == null) {
            return 1;
        }

        int res = 1;
        for (int i = 0; i < childs.size(); i++) {
            Node node = childs.get(i);
            res += node.getTotalLengthWithSummary(expander.getChildExpander(node.getShowValue()));
        }
        if (!needSummary()) {
            res--;
        }
        return Math.max(res, 1);
    }

    public int getTotalLength(NodeExpander expander) {
        if (expander == null) {
            return 1;
        }
        if (childs.isEmpty()) {
            return 1;
        }
        int res = 0;
        for (int i = 0; i < childs.size(); i++) {
            Node node = childs.get(i);
            res += node.getTotalLength(expander.getChildExpander(node.getShowValue()));
        }
        return res;
    }

    @Override
    public GroupValueIndex getIndex4Cal() {
        return gvi;
    }

    @Override
    public GroupValueIndex getIndex4CalByTargetKey(TargetGettingKey key) {
        GroupValueIndex gvi = getGroupValueIndexMap().get(key);
        return gvi == null ? getTargetIndexValueMap().get(key) : gvi;
    }

    public void setGroupValueIndex(GroupValueIndex gvi) {
        this.gvi = gvi;
    }


    public void setTargetIndex(TargetGettingKey key, GroupValueIndex gvi) {
        if (gvi != null) {
            getTargetIndexValueMap().put(key, gvi);
        }
    }

    public GroupValueIndex getTargetIndex(TargetGettingKey key) {
        return getTargetIndexValueMap().get(key);
    }

    public void setTargetGetter(TargetGettingKey key, GroupValueIndex gvi) {
        if (gvi != null) {
            getGroupValueIndexMap().put(key, gvi);
        }
    }

    public boolean needSummary() {
        if (getChildLength() > 1) {
            return true;
        }
        if (getChildLength() == 0) {
            return false;
        }
        Node child = getChild(0);
        Iterator it = getSummaryValue().entrySet().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            if (!equalsSummaryValue(child.getSummaryValue(entry.getKey()), entry.getValue())) {
                return true;
            }
        }
        return false;

    }

    private boolean equalsSummaryValue(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == o1;
        }
        if (ComparatorUtils.equals(o1, o2)) {
            return true;
        }
        return ComparatorUtils.equals(o1, o2);
    }

    public GroupValueIndex getGroupValueIndex(TargetGettingKey key) {
        return getGroupValueIndexMap().get(key);
    }

    @Override
    public void setSummaryValue(Object key, Object value) {
        if (value != null){
            value = ((Number)value).doubleValue();
        }
        getSummaryValue().put(key, value);
    }

    @Override
    public Number getSummaryValue(Object key) {
        if (getSummaryValue() == null || getSummaryValue().isEmpty()) {
            return null;
        }
        return (Number) getSummaryValue().get(key);
    }

    @Override
    public Map getSummaryValueMap() {
        return getSummaryValue();
    }

    @Override
    public void setSummaryValueMap(Map summaryValue) {
        if (summaryValue == null) {
            return;
        }
        this.summaryValue = summaryValue;
    }

    public int getIndexByValue(Object value) {
        return getMinCompareValue(0, getChildLength() - 1, value);
    }


    /**
     * 找出等于前值或者正好大一点的那个值
     *
     * @param start
     * @param end
     * @param value
     * @return
     */
    private int getMinCompareValue(int start, int end, Object value) {
        if (start > end) {
            return start;
        }
        int index = (start + end) / 2;
        Node c = childs.get(index);
        int result = getComparator().compare(value, c.getData());
        if (result > 0) {
            return getMinCompareValue(index + 1, end, value);
        } else if (result < 0) {
            return getMinCompareValue(start, index - 1, value);
        } else {
            return index;
        }
    }
    /**
     * 创建新node
     *
     * @param key 需要保存的key
     * @return 新node
     */
    public Node createNewTargetValueNode(TargetGettingKey key) {
        Node n = createNewNode();
        if (key != null) {
            Object value = getSummaryValue(key.getTargetKey());
            if (value != null) {
                n.setSummaryValue(key, value);
            }
        }
        int clen = childs.size();
        Node tempNode = null;
        for (int i = 0; i < clen; i++) {
            Node temp_node = childs.get(i);
            Node child = temp_node.createNewTargetValueNode(key);
            if (tempNode != null) {
                CubeReadingUtils.setSibing(tempNode, child);
            }
            n.addChild(child);
            tempNode = child;
        }
        return n;
    }

    protected Node createNewNode() {
        Node newNode = new Node(c, this.getData());
        newNode.showValue = this.getShowValue();
        newNode.setGroupValueIndex(this.gvi);
        return newNode;
    }

    /**
     * 转成字符串显示
     *
     * @return 注释
     */
    @Override
    public String toString() {
        if (data != null) {
            return "BINode:[" + data.toString() + "]";
        }
        return null;
    }

    /**
     * 创建包含前count个的node
     *
     * @param count 个数
     * @return 新的node
     */
    public Node createBeforeCountNode(int count) {

        int end = Math.min(count, this.getChildLength());
        Node newnode = createNewNode();
        newnode.summaryValue = this.getSummaryValue();
        ChildsMap<Node> childs = this.childs;
        Node tempNode = null;
        for (int i = 0; i < end && i < childs.size(); i++) {
            Node temp_node = childs.get(i);
            Node child = temp_node.createCloneNodeWithValue();
            if (tempNode != null) {
                CubeReadingUtils.setSibing(tempNode, child);
            }
            newnode.addChild(child);
            tempNode = child;
        }
        return newnode;
    }

    /**
     * 创建包含后count个的node
     *
     * @param count 个数
     * @return 新的node
     */
    public Node createAfterCountNode(int count) {

        int end = this.getChildLength();
        int start = Math.max(end - count, 0);
        Node newnode = createNewNode();
        newnode.summaryValue = this.getSummaryValue();
        ChildsMap<Node> childs = this.childs;
        Node tempNode = null;
        for (int i = start; i < end && i < childs.size(); i++) {
            Node temp_node = childs.get(i);
            Node child = temp_node.createCloneNodeWithValue();
            if (tempNode != null) {
                CubeReadingUtils.setSibing(tempNode, child);
            }
            newnode.addChild(child);
            tempNode = child;
        }
        return newnode;
    }

    /**
     * 克隆node，按最终child的个数取，不是第一层child
     *
     * @param start 开始
     * @param end   结束
     * @return
     */
    public Node createCloneNodeWithValue(int start, int end) {
        Node newnode = copyNode();
        ChildsMap<Node> childs = this.childs;
        Node tempNode = null;
        int position = 0;
        for (int i = 0; i < childs.size(); i++) {
            Node c = childs.get(i);
            int clen = c.getTotalLength();
            if (position + clen > start && position < end) {
                int s = position > start ? 0 : start - position;
                int e = position + clen < end ? clen : end - position;
                Node child = c.createCloneNodeWithValue(s, e);
                if (tempNode != null) {
                    CubeReadingUtils.setSibing(tempNode, child);
                }
                newnode.addChild(child);
                tempNode = child;
            }
            if (position >= end) {
                break;
            }
            position += clen;
        }
        return newnode;
    }

    /**
     * 克隆node
     *
     * @return 新node
     */
    public Node createCloneNodeWithValue() {
        Node newnode = copyNode();
        ChildsMap<Node> childs = this.childs;
        Node tempNode = null;
        for (int i = 0; i < childs.size(); i++) {
            Node temp_node = childs.get(i);
            Node child = temp_node.createCloneNodeWithValue();
            if (tempNode != null) {
                CubeReadingUtils.setSibing(tempNode, child);
            }
            newnode.addChild(child);
            tempNode = child;
        }
        return newnode;
    }

    /**
     * 创建交叉表head的节点
     *
     * @return 交叉表head的节点
     */
    public CrossHeader createCrossHeader() {
        CrossHeader newnode = new CrossHeader(c, data, gvi);
        newnode.setShowValue(getShowValue());
        try {
            newnode.getTargetIndexValueMap().putAll(this.getTargetIndexValueMap());
            newnode.getGroupValueIndexMap().putAll(this.getGroupValueIndexMap());
        } catch (Exception e) {

        }
        newnode.setSummaryValueMap(this.getSummaryValueMap());
        ChildsMap<Node> childs = this.childs;
        Node tempNode = null;
        for (int i = 0; i < childs.size(); i++) {
            Node temp_node = childs.get(i);
            Node child = temp_node.createCrossHeader();
            if (tempNode != null) {
                CubeReadingUtils.setSibing(tempNode, child);
            }
            newnode.addChild(child);
            tempNode = child;
        }
        return newnode;
    }


    /**
     * 获取指标key子节点的平均值
     *
     * @param key 指标的key
     * @return 平均值
     */
    @Override
    public double getChildAVGValue(TargetGettingKey key) {
        if (getChildAVG().get(key) == null) {
            int count = this.getChildLength();
            if (count == 0) {
                getChildAVG().put(key, this.getSummaryValue(key) == null ? 0 : this.getSummaryValue(key).doubleValue());
                return getChildAVG().get(key);
            }
            double sum = 0;
            for (int i = 0; i < count; i++) {
                Node child = this.getChild(i);
                Number v = child.getSummaryValue(key);
                sum += v == null ? 0 : v.doubleValue();
            }
            getChildAVG().put(key, sum / count);
        }
        return getChildAVG().get(key);
    }

    /**
     * 获取topN的位置临界值
     *
     * @param key 指标
     * @param N   Top几
     * @return 值
     */
    @Override
    public double getChildTOPNValueLine(TargetGettingKey key, int N) {
        if (N < 1) {
            return Double.POSITIVE_INFINITY;
        }
        TopNKey topNKey = new TopNKey(N, key);
        Double nLine = getTopNLineMap().get(topNKey);
        if (nLine == null) {
            nLine = NodeUtils.getTopN(this, key, N);

            getTopNLineMap().put(topNKey, nLine);
        }
        return nLine;
    }

    /**
     * 获取维度的TopN临界值
     *
     * @param N top N位置
     * @return 临界值
     */
    @Override
    public Comparable getChildTOPNValueLine(int N) {
        int count = this.getChildLength();
        if (N < 1 || count == 0) {
            return null;
        }
        return (Comparable) getChild(Math.min(N, count) - 1);
    }

    /**
     * 获取维度的BottomN临界值
     *
     * @param N BottomN位置
     * @return 临界值
     */
    @Override
    public Comparable getChildBottomNValueLine(int N) {
        return getChildTOPNValueLine(this.getChildLength() + 1 - N);
    }

    /**
     * 创建值过滤的新node
     *
     * @param rowDimension 维 度
     * @param targetsMap   所有指标
     * @return 新节点
     */
    public Node createResultFilterNode(BIDimension[] rowDimension,
                                       Map<String, TargetCalculator> targetsMap) {
        Node node = createResultFilterNode(0, rowDimension, targetsMap);
        if (node == null) {
            return createNewNode();
        }
        return node;
    }

    private Node createResultFilterNode(int index, BIDimension[] rowDimension,
                                        Map<String, TargetCalculator> targetsMap) {
        Node newnode = copyNode();
        ChildsMap<Node> childs = this.childs;
        Node tempNode = null;
        for (int i = 0; i < childs.size(); i++) {
            Node temp_node = childs.get(i);
            if (rowDimension[index].showNode(temp_node, targetsMap)) {
                Node child = temp_node.createResultFilterNode(index + 1, rowDimension, targetsMap);
                if (child == null) {
                    continue;
                }
                if (tempNode != null) {
                    CubeReadingUtils.setSibing(tempNode, child);
                }
                newnode.addChild(child);
                tempNode = child;
            }
        }
        if (tempNode == null && childs.size() > 0) {
            return null;
        }
        return newnode;
    }

    /**
     * 创建指标过滤的值
     *
     * @param targetFilterMap 指标过滤
     * @param targetsMap      所有指标
     * @return 新节点
     */
    public Node createResultFilterNode(Map<String, DimensionFilter> targetFilterMap,
                                       Map<String, TargetCalculator> targetsMap) {
        return createTargetFilterNode(targetFilterMap, targetsMap);
    }

    private Node createTargetFilterNode(Map<String, DimensionFilter> targetFilterMap,
                                        Map<String, TargetCalculator> targetsMap) {
        Node newnode = copyNode();
        ChildsMap<Node> childs = this.childs;
        Node tempNode = null;
        for (int i = 0; i < childs.size(); i++) {
            Node temp_node = childs.get(i);
            boolean showNode = true;
            if (targetFilterMap != null) {
                Iterator<DimensionFilter> it = targetFilterMap.values().iterator();
                while (it.hasNext()) {
                    if (!it.next().showNode(temp_node, targetsMap, null)) {
                        showNode = false;
                        break;
                    }
                }
            }
            if (showNode) {
                Node child = temp_node.createTargetFilterNode(targetFilterMap, targetsMap);
                if (child == null) {
                    continue;
                }
                if (tempNode != null) {
                    CubeReadingUtils.setSibing(tempNode, child);
                }
                newnode.addChild(child);
                tempNode = child;
            }
        }
        if (tempNode == null && childs.size() > 0) {
            return null;
        }
        return newnode;
    }

    /**
     * 创建排序后的新值
     *
     * @param rowDimension 维度
     * @param targetsMap   所有指标
     * @return 新节点
     */
    public Node createSortedNode(BIDimension[] rowDimension,
                                 Map<String, TargetGettingKey> targetsMap) {
        return createSortedNode(0, rowDimension, targetsMap);
    }

    private Node createSortedNode(int index, BIDimension[] rowDimension,
                                  Map<String, TargetGettingKey> targetsMap) {
        if (rowDimension.length == index) {
            return this;
        }

        Node newnode = copyNode();
        ChildsMap childs = this.childs;
        Node tempNode = null;
        String sort_target = rowDimension[index].getSortTarget();
        List<Node> childNodes = childs.getNodeList();
        final TargetGettingKey target_key = sort_target != null ? targetsMap.get(sort_target) : null;
        final int sortType = rowDimension[index].getSortType();
        if (target_key != null) {
            Collections.sort(childNodes, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    Number v1 = o1.getSummaryValue(target_key);
                    Number v2 = o2.getSummaryValue(target_key);
                    if (v1 == v2) {
                        return 0;
                    }
                    if (v1 == null) {
                        return 1;
                    }
                    if (v2 == null) {
                        return -1;
                    }
                    if (v1.doubleValue() == v2.doubleValue()) {
                        return 0;
                    }
                    boolean v = v1.doubleValue() < v2.doubleValue();
                    return (sortType == BIReportConstant.SORT.ASC) == v ? -1 : 1;
                }
            });
        }
        for (int i = 0; i < childNodes.size(); i++) {
            Node temp_node = childNodes.get(i);
            Node child = temp_node.createSortedNode(index + 1, rowDimension, targetsMap);
            //清除兄弟关系
            temp_node.setSibling(null);
            if (tempNode != null) {
                CubeReadingUtils.setSibing(tempNode, child);
            }
            newnode.addChild(child);
            tempNode = child;
        }
        return newnode;
    }

    /**
     * 创建排序后的新值
     *
     * @param targetSort 排序的指标
     * @param targetsMap 所有指标
     * @return 新节点
     */
    @Override
    public Node createSortedNode(NameObject targetSort,
                                 Map<String, TargetGettingKey> targetsMap) {
        return createTargetSortedNode(targetSort, targetsMap);
    }

    @Override
    public LightNode createSortedNode(NameObject targetSort, Map<String, TargetCalculator> targetsMap, ISortInfoList sortInfoList, int i) {
        Map<String, TargetGettingKey> keys = new HashMap<String, TargetGettingKey>();
        Iterator<Entry<String, TargetCalculator>> it = targetsMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, TargetCalculator> entry = it.next();
            keys.put(entry.getKey(), entry.getValue().createTargetGettingKey());
        }
        return createTargetSortedNode(targetSort, keys);
    }

    private Node createTargetSortedNode(NameObject targetSort,
                                        Map<String, TargetGettingKey> targetsMap) {
        if (targetSort == null) {
            return this;
        }
        Node newnode = copyNode();
        ChildsMap childs = this.childs;
        Node tempNode = null;
        String sort_target = targetSort.getName();
        List<Node> childNodes = childs.getNodeList();
        final TargetGettingKey target_key = sort_target != null ? targetsMap.get(sort_target) : null;
        final int sortType = (Integer) (targetSort.getObject());
        if (target_key != null) {
            Collections.sort(childNodes, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    Number v1 = o1.getSummaryValue(target_key);
                    Number v2 = o2.getSummaryValue(target_key);
                    if (v1 == v2) {
                        return 0;
                    }
                    if (v1 == null) {
                        return 1;
                    }
                    if (v2 == null) {
                        return -1;
                    }
                    if (v1.doubleValue() == v2.doubleValue()) {
                        return 0;
                    }
                    boolean v = v1.doubleValue() < v2.doubleValue();
                    return (sortType == BIReportConstant.SORT.ASC) == v ? -1 : 1;
                }
            });
        }
        for (int i = 0; i < childNodes.size(); i++) {
            Node temp_node = childNodes.get(i);
            Node child = temp_node.createTargetSortedNode(targetSort, targetsMap);
            //清除兄弟关系
            temp_node.setSibling(null);
            if (tempNode != null) {
                CubeReadingUtils.setSibing(tempNode, child);
            }
            newnode.addChild(child);
            tempNode = child;
        }
        return newnode;
    }

    private Node copyNode() {
        Node newnode = createNewNode();
        newnode.summaryValue = this.getSummaryValue();
        return newnode;
    }

    /**
     * 挖掘用的，生成一个新列
     *
     * @param key        指标
     * @param deltaValue delta值
     */
    public void addSummaryValue(Object key, double deltaValue) {
        if (this.getSummaryValue().get(key) == null) {
            this.getSummaryValue().put(key, deltaValue);
        } else {
            double value = (Double) this.getSummaryValue().get(key);
            double newValue = value + deltaValue;
            this.getSummaryValue().put(key, newValue);
        }
        if (this.getParent() != null) {
            this.getParent().addSummaryValue(key, deltaValue);
        }
    }

    public Map getSummaryValue() {
        return summaryValue;
    }

    public void setSummaryValue(Map summaryValueMap) {
        getSummaryValue().putAll(summaryValueMap);
    }

    /**
     * 注释
     *
     * @param dimensions 注释求
     * @param keys       注释回
     * @param index      当前操注释类型
     * @return 注释
     */
    public JSONObject toJSONObject(BIDimension[] dimensions, TargetGettingKey[] keys, int index) throws JSONException {
        JSONObject jo = new JSONObject();
        if (data != null) {
            jo.put("n", dimensions[index].toString(data));
        }
        JSONArray summary = new JSONArray();
        for (int i = 0; i < keys.length; i++) {
//            summary.put(GeneralUtils.objectToNumber(GeneralUtils.objectToString(this.getSummaryValue(keys[i]))));
            summary.put(this.getSummaryValue(keys[i]));
        }
        jo.put("s", summary);
        jo.put("x", getTotalLengthWithSummary());
        JSONArray children = new JSONArray();
        int childsSize = childs.size();
        if (childsSize > 0) {
            for (int i = 0; i < childsSize; i++) {
                children.put(childs.get(i).toJSONObject(dimensions, keys, index + 1));
            }
            jo.put("c", children);
        }
        return jo;
    }


    private Map<TargetGettingKey, Double> getChildAVG() {
        if (childAVG == null) {
            childAVG = new ConcurrentHashMap<TargetGettingKey, Double>();
        }
        return childAVG;
    }

    private Map<TargetGettingKey, Double> getAllChildAVG() {
        if (allChildAVG == null) {
            allChildAVG = new ConcurrentHashMap<TargetGettingKey, Double>(1);
        }
        return allChildAVG;
    }

    private Map<TopNKey, Double> getTopNLineMap() {
        if (topNLineMap == null) {
            topNLineMap = new ConcurrentHashMap<TopNKey, Double>(1);
        }
        return topNLineMap;
    }

    private class TopNKey{
        private int N;
        private TargetGettingKey key;

        public TopNKey(int n, TargetGettingKey key) {
            N = n;
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()){
                return false;
            }

            TopNKey topNKey = (TopNKey) o;

            if (N != topNKey.N) {
                return false;
            }
            return key != null ? ComparatorUtils.equals(key, topNKey.key) : topNKey.key == null;

        }

        @Override
        public int hashCode() {
            int result = N;
            result = 31 * result + (key != null ? key.hashCode() : 0);
            return result;
        }
    }
}