package com.fr.bi.cal.analyze.cal.sssecret;

import com.finebi.cube.api.ICubeTableService;
import com.finebi.cube.api.ICubeValueEntryGetter;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.base.key.BIKey;
import com.fr.bi.cal.analyze.cal.index.loader.MetricGroupInfo;
import com.fr.bi.cal.analyze.cal.index.loader.TargetAndKey;
import com.fr.bi.cal.analyze.cal.result.NodeExpander;
import com.fr.bi.cal.analyze.session.BISession;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.engine.index.key.IndexKey;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.result.DimensionCalculator;
import com.fr.bi.stable.utils.program.BINonValueUtils;
import com.fr.cache.list.IntList;
import com.fr.general.ComparatorUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根，用于保存游标等其他信息，感觉可以优化成一个游标就完了
 *
 * @author Daniel
 */
public class RootDimensionGroup implements IRootDimensionGroup {

    protected List<MetricGroupInfo> metricGroupInfoList;
    protected BISession session;
    protected boolean useRealData;

    private int rowSize;
    private TreeIterator iter;
    protected ICubeValueEntryGetter[][] getters;
    protected DimensionCalculator[][] columns;
    protected ICubeTableService[] tis;
    private ISingleDimensionGroup[] singleDimensionGroupCache;
    protected BusinessTable[] metrics;
    protected List<TargetAndKey>[] summaryLists;
    protected NoneDimensionGroup root;
    private NodeExpander expander;

    public RootDimensionGroup(List<MetricGroupInfo> metricGroupInfoList, BISession session, boolean useRealData) {
        this.metricGroupInfoList = metricGroupInfoList;
        this.session = session;
        this.useRealData = useRealData;
        init();
    }

    protected void init() {
        initIterator();
        initGetterAndRows();
        initRoot();
    }

    private void initIterator() {
        if (metricGroupInfoList == null || metricGroupInfoList.isEmpty()){
            BINonValueUtils.beyondControl("invalid parameters");
        }
        rowSize = metricGroupInfoList.get(0).getRows().length;
        for (MetricGroupInfo info : metricGroupInfoList){
            if (info.getRows().length != rowSize){
                throw new RuntimeException("invalid parameters");
            }
        }
        this.singleDimensionGroupCache = new ISingleDimensionGroup[rowSize];
        this.iter = new TreeIterator(rowSize);
    }

    protected void initGetterAndRows() {
        getters = new ICubeValueEntryGetter[rowSize][metricGroupInfoList.size()];
        columns = new DimensionCalculator[rowSize][metricGroupInfoList.size()];
        tis = new ICubeTableService[metricGroupInfoList.size()];
        for (int i = 0; i < metricGroupInfoList.size(); i++){
            DimensionCalculator[] rs = metricGroupInfoList.get(i).getRows();
            for (int j = 0; j < rs.length; j++){
                ICubeTableService ti = session.getLoader().getTableIndex(getSource(rs[j]));
                columns[j][i] = rs[j];
                getters[j][i] = ti.getValueEntryGetter(createKey(rs[j]), rs[j].getRelationList());
                tis[i] = ti;
            }
        }
    }

    protected void initRoot() {
        metrics = new BusinessTable[metricGroupInfoList.size()];
        summaryLists = new ArrayList[metricGroupInfoList.size()];
        GroupValueIndex[] gvis = new GroupValueIndex[metricGroupInfoList.size()];
        for (int i = 0; i < metricGroupInfoList.size(); i++){
            metrics[i] = metricGroupInfoList.get(i).getMetric();
            summaryLists[i] = metricGroupInfoList.get(i).getSummaryList();
            gvis[i] = metricGroupInfoList.get(i).getFilterIndex();
        }
        root = NoneDimensionGroup.createDimensionGroup(metrics, summaryLists, tis, gvis, session.getLoader());
    }

    private CubeTableSource getSource(DimensionCalculator column) {
        //多对多
        if (column.getDirectToDimensionRelationList().size() > 0) {
            ICubeFieldSource primaryField = column.getDirectToDimensionRelationList().get(0).getPrimaryField();
            return primaryField.getTableBelongTo();
        }
        return column.getField().getTableBelongTo().getTableSource();
    }

    private BIKey createKey(DimensionCalculator column) {
        //多对多
        if (column.getDirectToDimensionRelationList().size() > 0) {
            ICubeFieldSource primaryField = column.getDirectToDimensionRelationList().get(0).getPrimaryField();
            return new IndexKey(primaryField.getFieldName());
        }
        return column.createKey();
    }

    public static int findPageIndexDichotomy(int[] shrinkPos, List<int[]> pageIndex, int start, int end) throws ArrayIndexOutOfBoundsException {
        //判断数组是否为空
        if (pageIndex == null) {
            throw new NullPointerException();
        }
        if (start < 0 || end < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (end > start) {
            int middle = (start + end) / 2;
            int[] middleIndex = pageIndex.get(middle);
            //中间值小于当前值
            if (TreePageComparator.TC.compare(shrinkPos, middleIndex) >= 0) {
                //中间值小于当前值，同时下一个值大于等于当前值（end>=middle+1）,则middle为最小的大于值
                if (TreePageComparator.TC.compare(shrinkPos, pageIndex.get(middle + 1)) < 0) {
                    return middle + 1;
                } else {
                    //中间值小于当前值，但是下一个值仍然小于，则结果应该在（middle+1,end）中间
                    return findPageIndexDichotomy(shrinkPos, pageIndex, middle + 1, end);
                }
            } else {
                //中间值大于当前值
                return findPageIndexDichotomy(shrinkPos, pageIndex, start, middle);
            }
        } else if (start == end) {
            return start;
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * TODO 这里可以改成可以前后移动的游标提高性能先这样
     */
    @Override
    public TreeIterator moveToShrinkStartValue(Object[] value) {
        if (value != null) {
            int[] shrinkPos = getValueStartRow(value);
            iter.travelToPositionPage(shrinkPos);
        } else {
            iter.moveCurrentStart();
        }
        return iter;
    }

    @Override
    public TreeIterator moveLast() {
        iter.moveLast();
        return iter;
    }

    @Override
    public TreeIterator moveNext() {
        return iter;
    }

    @Override
    public TreeIterator moveToStart() {
        iter.reset();
        return iter;
    }

    @Override
    public void setExpander(NodeExpander expander) {
        this.expander = expander;
    }

    private int[] getValueStartRow(Object[] value) {
        IntList result = new IntList();
        getValueStartRow(root, value, 0, result);
        for (int i = value.length; i < rowSize; i++) {
            result.add(-1);
        }
        return result.toArray();
    }

    private void getValueStartRow(NoneDimensionGroup ng, Object[] value, int deep, IntList list) {
        if (deep >= value.length) {
            return;
        }
        ISingleDimensionGroup sg = getSingleDimensionGroupCache(value, ng, deep);
        int i = sg.getChildIndexByValue(value[deep]);
        if (i == -1) {
            return;
        }
        list.add(i);
        try {
            NoneDimensionGroup currentNg = sg.getChildDimensionGroup(i);
            if (currentNg == NoneDimensionGroup.NULL) {
                return;
            }
            getValueStartRow(currentNg, value, deep + 1, list);
        } catch (GroupOutOfBoundsException e) {
            return;
        }
    }

    private ISingleDimensionGroup getSingleDimensionGroupCache(Object[] value, NoneDimensionGroup ng, int deep) {
        return createSingleDimensionGroup(value, ng, deep);
    }

    /**
     * 获得下一个值，可能是下一个child，也可能下一个brother。
     * 如果是结点展开，同时深度没有到底。那么就是下一个child。（深度是指维度间的上下级关系）
     * 否则是当前结点下一个brother。
     *
     * @param gv
     * @param ng
     * @param index
     * @param deep
     * @param expander
     * @param list
     */
    private ReturnStatus getNext(GroupConnectionValue gv, NoneDimensionGroup ng, int[] index, int deep, NodeExpander expander, IntList list) {
        return getNext(gv, ng, index, deep, expander, list, null);
    }

    /**
     * 加了一位nextBrother。
     * 当从nextParent进入到GetNext里面时，直接进入nextBrother方法。
     *
     * @param gv
     * @param ng
     * @param index
     * @param deep
     * @param expander
     * @param list
     * @param nextBrother
     */
    private ReturnStatus getNext(GroupConnectionValue gv,
                                 NoneDimensionGroup ng,
                                 int[] index,
                                 int deep,
                                 NodeExpander expander,
                                 IntList list,
                                 Object nextBrother) {
        if (expander == null) {
            return groupEnd();
        }
        if (rowSize == 0) {
            return ReturnStatus.NULL;
        }
        ISingleDimensionGroup sg;
        sg = getSingleDimensionGroup(gv, ng, deep);
        String currentValueShowName = null;
        //如果不是-1说明是从child搞上来的有可能是没有的
        if (index[deep] != -1) {
            try {
                currentValueShowName = sg.getChildShowName(index[deep]);
            } catch (GroupOutOfBoundsException e) {
                if (ReturnStatus.GroupEnd == gotoNextParent(gv, index, deep, expander, list)) {
                    return groupEnd();
                }
            }
        }
        if (notNextChild(index, deep, expander, nextBrother, currentValueShowName)) {
            try {
                ReturnStatus returnStatus = gotoNextBrother(sg, gv, index, deep, expander, list);
                if (returnStatus == ReturnStatus.GroupOutOfBounds) {
                    if (ReturnStatus.GroupEnd == gotoNextParent(gv, index, deep, expander, list)) {
                        return groupEnd();
                    }
                } else if (returnStatus == ReturnStatus.GroupOutOfBounds) {
                    return catchGroupOutOfBounds(gv, index, deep, expander, list);
                } else if (returnStatus == ReturnStatus.GroupEnd) {
                    return groupEnd();
                }
            } catch (GroupOutOfBoundsException e) {
                return catchGroupOutOfBounds(gv, index, deep, expander, list);
            }
        } else {
            if (ReturnStatus.GroupEnd == gotoNextChildValue(sg, gv, index, deep, expander, list)) {
                return groupEnd();
            }
        }
        return ReturnStatus.Success;
    }

    private ReturnStatus catchGroupOutOfBounds(GroupConnectionValue gv, int[] index, int deep, NodeExpander expander, IntList list) {
        ReturnStatus gotoNextParentStatus = gotoNextParent(gv, index, deep, expander, list);
        if (ReturnStatus.GroupEnd == gotoNextParentStatus) {
            return groupEnd();
        }
        return ReturnStatus.Success;
    }

    private boolean notNextChild(int[] index, int deep, NodeExpander expander, Object nextBrother, String currentValueShowName) {
        return index.length == deep + 1 || (currentValueShowName != null && expander.getChildExpander(currentValueShowName) == null || nextBrother != null);
    }

    private ReturnStatus groupEnd() {
        return ReturnStatus.GroupEnd;
    }

    protected ISingleDimensionGroup getSingleDimensionGroup(GroupConnectionValue gv, NoneDimensionGroup ng, int deep) {
        return getCacheDimensionGroup(gv, ng, deep);
    }

    private ISingleDimensionGroup getCacheDimensionGroup(GroupConnectionValue gv, NoneDimensionGroup ng, int deep) {
        if (singleDimensionGroupCache[deep] == null || !ComparatorUtils.equals(singleDimensionGroupCache[deep].getData(), getParentsValuesByGv(gv, deep))) {
            singleDimensionGroupCache[deep] = createSingleDimensionGroup(gv, ng, deep);
        }
        return singleDimensionGroupCache[deep];
    }

    protected ISingleDimensionGroup createSingleDimensionGroup(GroupConnectionValue gv, NoneDimensionGroup ng, int deep) {
        return createSingleDimensionGroup(getParentsValuesByGv(gv, deep), ng, deep);
    }

    protected ISingleDimensionGroup createSingleDimensionGroup(Object[] data, NoneDimensionGroup ng, int deep) {
        return ng.createSingleDimensionGroup(columns[deep], getters[deep], data, useRealData);
    }

    protected Object[] getParentsValuesByGv(GroupConnectionValue groupConnectionValue, int deep) {
        ArrayList al = new ArrayList();
        GroupConnectionValue gv = groupConnectionValue;
        while (deep-- > 0) {
            al.add(gv.getData());
            gv = gv.getParent();
        }
        int len = al.size();
        Object[] obs = new Object[len];
        for (int i = 0; i < len; i++) {
            obs[i] = al.get(len - 1 - i);
        }
        return obs;
    }

    private ReturnStatus gotoNextChildValue(ISingleDimensionGroup sg, GroupConnectionValue gv, int[] index, int deep, NodeExpander expander, IntList list) {
        if (index[deep] == -1) {
            index[deep] += 1;
        }
        return findCurrentValue(sg, gv, index, deep, expander, list, index[deep]);
    }

    private ReturnStatus findCurrentValue(ISingleDimensionGroup sg, GroupConnectionValue gv, int[] index, int deep, NodeExpander expander, IntList list, int row) {
        NoneDimensionGroup nds;
        if (row == 0) {
            try {
                nds = sg.getChildDimensionGroup(row);
                if (NoneDimensionGroup.NULL == nds) {
                    if (deep == 0) {
                        return ReturnStatus.GroupEnd;
                    }
                    index[deep - 1] = index[deep - 1] + 1;
                    list.set(deep - 1, index[deep - 1] + 1);
                    return ReturnStatus.GroupOutOfBounds;
                }
            } catch (GroupOutOfBoundsException e) {
                if (deep == 0) {
                    return ReturnStatus.GroupEnd;
                }
                index[deep - 1] = index[deep - 1] + 1;
                throw e;
            }
        } else {
            nds = sg.getChildDimensionGroup(row);
            if (nds == NoneDimensionGroup.NULL) {
                return ReturnStatus.GroupOutOfBounds;
            }
        }
        GroupConnectionValue ngv = createGroupConnectionValue(sg, row, nds);
        NodeExpander ex = expander.getChildExpander(sg.getChildShowName(row));
        list.add(row);
        ngv.setParent(gv);
        if (ex != null && deep + 1 < index.length) {
            ReturnStatus returnStatus = getNext(ngv, nds, index, deep + 1, ex, list);
            if (ReturnStatus.GroupEnd == returnStatus) {
                return groupEnd();
            }
        }
        return ReturnStatus.Success;
    }

    protected GroupConnectionValue createGroupConnectionValue(ISingleDimensionGroup sg, int row, NoneDimensionGroup nds) {
        GroupConnectionValue ngv = new GroupConnectionValue(sg.getChildData(row), nds);
        return ngv;
    }

    private ReturnStatus gotoNextBrother(ISingleDimensionGroup sg, GroupConnectionValue gv, int[] index, int deep, NodeExpander expander, IntList list) {
        return findCurrentValue(sg, gv, index, deep, expander, list, index[deep] + 1);
    }

    private ReturnStatus gotoNextParent(GroupConnectionValue gv, int[] index, int deep, NodeExpander expander, IntList list) {
        if (deep == 0) {
            return groupEnd();
        }
        int[] newIndex = index.clone();
        //Connery:这里移动下一位置，但是在getNext方法里面，调用了nextBrother，又在parent的向下移动了一个位置。
        newIndex[deep - 1] = list.get(deep - 1);
        Arrays.fill(newIndex, deep, newIndex.length, -1);
        list.remove(list.size() - 1);
        if (ReturnStatus.GroupEnd == getNext(gv.getParent(), gv.getParent().getCurrentValue(), newIndex, deep - 1, expander.getParent(), list, new Object())) {
            return groupEnd();
        }
        return ReturnStatus.Success;
    }

    private static class TreePageComparator implements Comparator<int[]> {

        private static TreePageComparator TC = new TreePageComparator();

        @Override
        public int compare(int[] p1, int[] p2) {
            if (p1 == p2) {
                return 0;
            }
            if (p1 == null) {
                return -1;
            }
            if (p2 == null) {
                return 1;
            }
            int len1 = p1.length;
            int len2 = p2.length;
            int lim = Math.min(len1, len2);
            int k = 0;
            while (k < lim) {
                int c1 = p1[k];
                int c2 = p2[k];
                if (c1 != c2) {
                    return c1 - c2;
                }
                k++;
            }
            return len1 - len2;
        }

    }

    private class TreeIterator implements NodeDimensionIterator {
        private int[] index;
        private int[] tempIndex;
        private Map<BusinessTable, GroupValueIndex> controlFilters = new ConcurrentHashMap<BusinessTable, GroupValueIndex>();

        /**
         * TODO 先放内存看看再说
         */
        private List<int[]> pageIndex = new ArrayList<int[]>();

        private TreeIterator(int len) {
            this.index = new int[len];
            Arrays.fill(this.index, -1);
            pageEnd();
        }

        private void moveLast() {
            int pos = pageIndex.size() - 3;
            if (pos < 0) {
                throw new GroupEndException();
            }
            this.index = pageIndex.get(pos);
            this.pageIndex = this.pageIndex.subList(0, pos + 1);
        }

        private void moveCurrentStart() {
            int pos = pageIndex.size() - 2;
            if (pos < 0) {
                throw new GroupEndException();
            }
            this.index = pageIndex.get(pos);
            this.pageIndex = this.pageIndex.subList(0, pos + 1);
        }


        private void travelToPositionPage(int[] shrinkPos) {
            int position = findPageIndexDichotomy(shrinkPos, pageIndex, 0, pageIndex.size() - 1);
            if (position - 1 >= 0) {
                this.index = pageIndex.get(position - 1);
            }
            if (position < pageIndex.size()) {
                pageIndex = pageIndex.subList(0, position);
            }
        }

        @Override
        public GroupConnectionValue next() {
            return seek(index);
        }

        private GroupConnectionValue seek(int[] index) {
            try {
                GroupConnectionValue gv = new GroupConnectionValue(null, root);
                IntList list = new IntList();
                int indexCopy[] = Arrays.copyOf(index, index.length);
                if (ReturnStatus.GroupEnd == getNext(gv, root, indexCopy, 0, expander, list)) {
                    this.tempIndex = null;
                    return null;
                }
                for (int i = list.size(); i < rowSize; i++) {
                    list.add(-1);
                }
                this.tempIndex = list.toArray();
                return gv;
            } catch (GroupEndException e) {
                this.tempIndex = null;
                return null;
            }
        }

        @Override
        public boolean hasPrevious() {
            return pageIndex.size() > 2;
        }

        @Override
        public boolean hasNext() {
            return next() != null && index != null && index.length > 0;
        }

        @Override
        public void moveNext() {
            if (this.tempIndex != null) {
                this.index = tempIndex.clone();
            }
        }

        @Override
        public int getPageIndex() {
            return pageIndex.size() - 1;
        }

        @Override
        public void pageEnd() {
            pageIndex.add(this.index.clone());
        }

        private void fillEndEmpty(int deep) {
            Arrays.fill(this.index, deep, this.index.length, -1);
        }

        private void reset() {
            fillEndEmpty(0);
            this.pageIndex = this.pageIndex.subList(0, 0);
        }

    }
}