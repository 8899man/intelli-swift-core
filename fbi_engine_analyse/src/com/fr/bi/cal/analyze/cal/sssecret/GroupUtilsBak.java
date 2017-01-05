//package com.fr.bi.cal.analyze.cal.sssecret;
//
//import com.finebi.cube.common.log.BILoggerFactory;
//import com.finebi.cube.conf.table.BIBusinessTable;
//import com.fr.bi.cal.analyze.cal.multithread.MultiThreadManagerImpl;
//import com.fr.bi.cal.analyze.cal.multithread.SummaryCall;
//import com.fr.bi.cal.analyze.cal.result.Node;
//import com.fr.bi.cal.analyze.cal.result.NodeAndPageInfo;
//import com.fr.bi.cal.analyze.cal.result.NodeUtils;
//import com.fr.bi.cal.analyze.cal.result.operator.Operator;
//import com.fr.bi.stable.constant.BIBaseConstant;
//import com.fr.bi.stable.report.result.BINode;
//import com.fr.bi.stable.report.result.TargetCalculator;
//import com.fr.general.ComparatorUtils;
//
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by 小灰灰 on 2016/12/21.
// */
//public class GroupUtilsBak {
//
//    public static NodeAndPageInfo createNextPageMergeNode(IRootDimensionGroup root, Operator op) {
//        NodeDimensionIterator iter = op.getPageIterator(root);
//        return createMergePageNode(new NodeDimensionIterator[]{iter}, op, new IRootDimensionGroup[]{root});
//    }
//
//    /**
//     * Connery:依据iters获得child节点，并添加到node对象的child属性中
//     *
//     * @param gc
//     * @param node
//     * @param iters
//     * @param op
//     */
//    protected static void merge(GroupConnectionValue[] gc, Node node, NodeDimensionIterator[] iters, List<TargetCalculator[]> calculators, Operator op, IRootDimensionGroup[] roots) {
//        GroupConnectionValue[] gcvsChild = getGroupConnectionValueChildren(gc);
//        gcvsChild = getMinChildGroups(gcvsChild);
//        if (isAllEmpty(gcvsChild)) {
//            moveNext(gc, iters);
//            op.addRow();
//            return;
//        } else {
//            for (int i = 0; i < gcvsChild.length; i++) {
//                if (gcvsChild[i] != null) {
//                    Object data = gcvsChild[i].getData();
//                    Node n = node.getChild(data);
//                    if (n == null) {
//                        //FIXME 交叉表需要传index这里需要考虑改变数据结构
//                        n = new Node(gcvsChild[i].getCk(), data);
//                        int deep = 0;
//                        Node t = node;
//                        while (t.getParent() != null) {
//                            t = t.getParent();
//                            deep++;
//                        }
//                        if (n.getShowValue() != BIBaseConstant.EMPTY_NODE_DATA || deep != 0) {
//                            node.addChild(n);
//                            addSummaryValue(n, gcvsChild, calculators);
//                        }
//                    }
//                    merge(gcvsChild, n, iters, calculators, op, roots);
//                    break;
//                }
//            }
//        }
//    }
//
//    public static void moveNext(GroupConnectionValue[] gc, NodeDimensionIterator[] iters) {
//        for (int i = 0; i < gc.length; i++) {
//            if (gc[i] != null) {
//                iters[i].moveNext();
//            }
//        }
//    }
//
//    public static GroupConnectionValue[] getGroupConnectionValueChildren(GroupConnectionValue[] gc) {
//        GroupConnectionValue[] gcvsChild = new GroupConnectionValue[gc.length];
//        for (int i = 0; i < gc.length; i++) {
//            if (gc[i] != null) {
//                gcvsChild[i] = gc[i].getChild();
//            }
//        }
//        return gcvsChild;
//    }
//
//    private static NodeAndPageInfo createMergePageNode(NodeDimensionIterator[] iters, Operator op, IRootDimensionGroup[] roots) {
//        Node node = new Node(null, null);
//        GroupConnectionValue[] gc = null;
//        try {
//            gc = getNextGC(iters);
//        } catch (Exception e) {
//            BILoggerFactory.getLogger().error(e.getMessage(), e);
//        }
//        if (gc == null) {
//            return new NodeAndPageInfo(node, false, false, 0);
//        }
//        List<TargetCalculator[]> calculators = null;
//        addSummaryValue(node, gc, calculators);
//        GroupConnectionValue[] gcvsChild = new GroupConnectionValue[iters.length];
//        for (int i = 0; i < gc.length; i++) {
//            if (gc[i] != null) {
//                gcvsChild[i] = gc[i].getChild();
//            }
//        }
//        if (!isAllEmpty(gcvsChild)) {
//            merge(gc, node, iters, calculators, op, roots);
//            while (!op.isPageEnd()) {
//                while (node.getParent() != null) {
//                    node = node.getParent();
//                }
//                GroupConnectionValue[] gcv = getNextGC(iters);
//                if (isAllEmpty(gcv)) {
//                    break;
//                }
//                merge(gcv, node, iters, calculators, op, roots);
//            }
//        }
//        try {
//            PageEnd(iters);
//        } catch (Exception e) {
//            BILoggerFactory.getLogger().error(e.getMessage(), e);
//        }
//
//        NodeUtils.setSiblingBetweenFirstAndLastChild(node);
//        return new NodeAndPageInfo(node, hasPrevious(iters), hasNext(iters), getPage(iters));
//    }
//
//    private static boolean hasPrevious(NodeDimensionIterator[] iters) {
//        for (int i = 0; i < iters.length; i++) {
//            if (iters[i].hasPrevious()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private static boolean hasNext(NodeDimensionIterator[] iters) {
//        for (int i = 0; i < iters.length; i++) {
//            if (iters[i].hasNext()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private static int getPage(NodeDimensionIterator[] iters) {
//        if (iters == null || iters.length == 0) {
//            return 0;
//        } else {
//            return iters[0].getPageIndex();
//        }
//    }
//
//    private static GroupConnectionValue[] getNextGC(NodeDimensionIterator[] iters) {
//        GroupConnectionValue[] gcvs = new GroupConnectionValue[iters.length];
//        for (int i = 0; i < iters.length; i++) {
//            if (iters[i] != null) {
//                gcvs[i] = iters[i].next();
//            }
//        }
//        return gcvs;
//    }
//
//    public static boolean isAllEmpty(GroupConnectionValue[] gcvs) {
//        for (GroupConnectionValue v : gcvs) {
//            if (v != null) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private static void PageEnd(NodeDimensionIterator[] iters) {
//        for (NodeDimensionIterator v : iters) {
//            if (v != null) {
//                v.pageEnd();
//            }
//        }
//    }
//
//    public static GroupConnectionValue[] getMinChildGroups(GroupConnectionValue[] gcvs) {
//        Object minValue = null;
//        GroupConnectionValue[] result = new GroupConnectionValue[gcvs.length];
//        for (int i = 0; i < gcvs.length; i++) {
//            GroupConnectionValue gcv = gcvs[i];
//            if (gcv != null) {
//                Object currentValue = gcv.getKey();
//                if (minValue == null) {
//                    minValue = currentValue;
//                } else {
//                    int c;
//                    if (ComparatorUtils.equals(minValue.getClass(), currentValue.getClass())) {
//                        c = gcv.getComparator().compare(minValue, currentValue);
//                    } else {
//                        c = 1;
//                    }
//                    if (c > 0) {
//                        minValue = currentValue;
//                    }
//                }
//            }
//        }
//        for (int i = 0; i < gcvs.length; i++) {
//            GroupConnectionValue gcv = gcvs[i];
//            if (gcv != null) {
//                Object currentValue = gcv.getKey();
//                int c;
//                if (ComparatorUtils.equals(minValue.getClass(), currentValue.getClass())) {
//                    c = gcv.getComparator().compare(minValue, currentValue);
//                } else {
//                    c = 1;
//                }
//
//                if (c == 0) {
//                    result[i] = gcvs[i];
//                }
//            }
//        }
//        return result;
//    }
//
//
//    /**
//     * 获取维度对应指标的汇总值
//     *
//     * @param node
//     * @param gcvs
//     */
//    private static void addSummaryValue(Node node, GroupConnectionValue[] gcvs, List<TargetCalculator[]> calculators) {
//        NoneDimensionGroup[] groups = new NoneDimensionGroup[gcvs.length];
//        for (int i = 0; i < gcvs.length; i++) {
//            if (gcvs[i] != null) {
//                groups[i] = gcvs[i].getCurrentValue();
//            }
//        }
//        for (int i = 0; i < groups.length; i++) {
//            if (groups[i] != null && !ComparatorUtils.equals(groups[i].getTableKey(), BIBusinessTable.createEmptyTable())) {
//                if (groups[i] instanceof TreeNoneDimensionGroup) {
//                    setSummaryValueMap(node, (TreeNoneDimensionGroup) groups[i]);
//                    BINode root = groups[i].getLightNode();
//                    NodeUtils.copyIndexMap(node, root);
//                    break;
//                }
//                if (MultiThreadManagerImpl.isMultiCall()) {
//                    TargetCalculator[] cs = calculators.get(i);
//                    if (cs != null){
//                        for (TargetCalculator c : cs){
//                            MultiThreadManagerImpl.getInstance().getExecutorService().add(new SummaryCall(node, groups[i],c));
//                        }
//                    }
//                } else {
//                    for (TargetCalculator calculator : calculators.get(i)){
//                        Number v = groups[i].getSummaryValue(calculator);
//                        if (v != null) {
//                            node.setTargetGetter(calculator.createTargetGettingKey(), groups[i].getRoot().getGroupValueIndex());
//                            node.setTargetIndex(calculator.createTargetGettingKey(), groups[i].getRoot().getGroupValueIndex());
//                            node.setSummaryValue(calculator.createTargetGettingKey(), v);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private static void setSummaryValueMap(Node node, TreeNoneDimensionGroup group) {
//        Map summaryValueMap = group.getTargetGettingKeyRoot().getSummaryValueMap();
//        node.setSummaryValueMap(summaryValueMap);
//    }
//}
