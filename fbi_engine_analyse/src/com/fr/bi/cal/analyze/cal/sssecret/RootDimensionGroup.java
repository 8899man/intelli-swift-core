package com.fr.bi.cal.analyze.cal.sssecret;

import com.finebi.cube.api.ICubeTableService;
import com.finebi.cube.api.ICubeValueEntryGetter;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.base.key.BIKey;
import com.fr.bi.cal.analyze.cal.index.loader.MetricGroupInfo;
import com.fr.bi.cal.analyze.cal.index.loader.TargetAndKey;
import com.fr.bi.cal.analyze.cal.multithread.BIMultiThreadExecutor;
import com.fr.bi.cal.analyze.cal.multithread.MultiThreadManagerImpl;
import com.fr.bi.cal.analyze.cal.result.Node;
import com.fr.bi.cal.analyze.cal.result.NodeExpander;
import com.fr.bi.cal.analyze.cal.sssecret.diminfo.MergeIteratorCreator;
import com.fr.bi.cal.analyze.session.BISession;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.engine.index.key.IndexKey;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.result.DimensionCalculator;
import com.fr.cache.list.IntList;
import com.fr.general.ComparatorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 根，用于保存游标等其他信息，感觉可以优化成一个游标就完了
 *
 * @author Daniel
 */
public class RootDimensionGroup implements IRootDimensionGroup {

    protected List<MetricGroupInfo> metricGroupInfoList;
    protected MergeIteratorCreator[] mergeIteratorCreators;
    protected int sumLength;
    protected BISession session;
    protected boolean useRealData;

    protected ICubeValueEntryGetter[][] getters;
    protected DimensionCalculator[][] columns;
    protected ICubeTableService[] tis;
    private ISingleDimensionGroup[] singleDimensionGroupCache;
    protected BusinessTable[] metrics;
    protected List<TargetAndKey>[] summaryLists;
    protected NoneDimensionGroup root;
    protected int rowSize;

    protected RootDimensionGroup() {

    }

    public RootDimensionGroup(List<MetricGroupInfo> metricGroupInfoList, MergeIteratorCreator[] mergeIteratorCreators, int sumLength, BISession session, boolean useRealData) {
        this.metricGroupInfoList = metricGroupInfoList;
        this.mergeIteratorCreators = mergeIteratorCreators;
        this.sumLength = sumLength;
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
        if (metricGroupInfoList == null || metricGroupInfoList.isEmpty()) {
            return;
        }
        rowSize = metricGroupInfoList.get(0).getRows().length;
        for (MetricGroupInfo info : metricGroupInfoList) {
            if (info.getRows().length != rowSize) {
                throw new RuntimeException("invalid parameters");
            }
        }
        this.singleDimensionGroupCache = new ISingleDimensionGroup[rowSize];
    }

    protected void initGetterAndRows() {
        getters = new ICubeValueEntryGetter[rowSize][metricGroupInfoList.size()];
        columns = new DimensionCalculator[rowSize][metricGroupInfoList.size()];
        tis = new ICubeTableService[metricGroupInfoList.size()];
        for (int i = 0; i < metricGroupInfoList.size(); i++) {
            CubeTableSource source = metricGroupInfoList.get(i).getMetric().getTableSource();
            if (source != null){
                tis[i] = session.getLoader().getTableIndex(metricGroupInfoList.get(i).getMetric().getTableSource());
            }
            DimensionCalculator[] rs = metricGroupInfoList.get(i).getRows();
            for (int j = 0; j < rs.length; j++) {
                columns[j][i] = rs[j];
                getters[j][i] = session.getLoader().getTableIndex(getSource(rs[j])).getValueEntryGetter(createKey(rs[j]), rs[j].getRelationList());
            }
        }
    }

    protected void initRoot() {
        metrics = new BusinessTable[metricGroupInfoList.size()];
        summaryLists = new ArrayList[metricGroupInfoList.size()];
        GroupValueIndex[] gvis = new GroupValueIndex[metricGroupInfoList.size()];
        for (int i = 0; i < metricGroupInfoList.size(); i++) {
            metrics[i] = metricGroupInfoList.get(i).getMetric();
            summaryLists[i] = metricGroupInfoList.get(i).getSummaryList();
            gvis[i] = metricGroupInfoList.get(i).getFilterIndex();
        }
        root = NoneDimensionGroup.createDimensionGroup(metrics, summaryLists, sumLength, tis, gvis, session.getLoader());
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

    public List<MetricGroupInfo> getMetricGroupInfoList() {
        return metricGroupInfoList;
    }

    @Override
    public Node getConstructedRoot() {
        return new Node(sumLength);
    }

    @Override
    public NoneDimensionGroup getRoot() {
        return root;
    }

    public ICubeValueEntryGetter[][] getGetters() {
        return getters;
    }

    public DimensionCalculator[][] getColumns() {
        return columns;
    }

    public int[] getValueStartRow(Object[] value) {
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
        NoneDimensionGroup currentNg = sg.getChildDimensionGroup(i);
        if (currentNg == NoneDimensionGroup.NULL) {
            return;
        }
        getValueStartRow(currentNg, value, deep + 1, list);
    }

    private ISingleDimensionGroup getSingleDimensionGroupCache(Object[] value, NoneDimensionGroup ng, int deep) {
        return createSingleDimensionGroup(value, ng, deep);
    }

    /**
     * @param gv       表示一行的值
     * @param index    上一次游标的位置
     * @param deep     维度的层级
     * @param expander 展开信息
     * @param list     当前的游标
     */
    public ReturnStatus getNext(GroupConnectionValue gv,
                                int[] index,
                                int deep,
                                NodeExpander expander,
                                IntList list) {
        if (expander == null) {
            return ReturnStatus.GroupEnd;
        }
        if (rowSize == 0) {
            return ReturnStatus.Success;
        }
        ISingleDimensionGroup sg = getCacheDimensionGroup(gv, deep);
        //如果往下移动，行数就加1
        if (notNextChild(index, deep, expander, sg)) {
            index[deep]++;
        }
        ReturnStatus returnStatus = findCurrentValue(sg, gv, list, index[deep]);
        //如果越界，就一直往上移，直到不越界或者结束。
        while (returnStatus == ReturnStatus.GroupOutOfBounds) {
            //如果找到根节点还是越界，说明结束了。
            if (deep == 0) {
                return ReturnStatus.GroupEnd;
            }
            //把index数组deep位置后面的都清了，下移一位开始找，每次seek的时候这个while循环至多执行一次。
            Arrays.fill(index, deep, index.length, -1);
            list.remove(list.size() - 1);
            deep -= 1;
            gv = gv.getParent();
            sg = getCacheDimensionGroup(gv, deep);
            expander = expander.getParent();
            returnStatus = findCurrentValue(sg, gv, list, ++index[deep]);
        }
        //如果往下展开，就继续往下
        NodeExpander ex = expander.getChildExpander(sg.getChildShowName(index[deep]));
        if (ex != null && deep + 1 < index.length) {
            if (ReturnStatus.GroupEnd == getNext(gv.getChild(), index, deep + 1, ex, list)) {
                return ReturnStatus.GroupEnd;
            }
        }
        return ReturnStatus.Success;
    }

    //最后一个维度或者初始化的情况或者没有展开的情况必定是往下的
    private boolean notNextChild(int[] index, int deep, NodeExpander expander, ISingleDimensionGroup sg) {
        if (index.length == deep + 1 || index[deep] == -1) {
            return true;
        }
        String showValue = sg.getChildShowName(index[deep]);
        return showValue != null && expander.getChildExpander(showValue) == null;
    }

    private ISingleDimensionGroup getCacheDimensionGroup(GroupConnectionValue gv, int deep) {
        if (singleDimensionGroupCache[deep] == null || !ComparatorUtils.equals(singleDimensionGroupCache[deep].getData(), getParentsValuesByGv(gv, deep))) {
            singleDimensionGroupCache[deep] = createSingleDimensionGroup(gv, deep);
        }
        return singleDimensionGroupCache[deep];
    }

    protected ISingleDimensionGroup createSingleDimensionGroup(GroupConnectionValue gv, int deep) {
        return createSingleDimensionGroup(getParentsValuesByGv(gv, deep), gv.getCurrentValue(), deep);
    }

    protected ISingleDimensionGroup createSingleDimensionGroup(Object[] data, NoneDimensionGroup ng, int deep) {
        return ng.createSingleDimensionGroup(columns[deep], getters[deep], data, mergeIteratorCreators[deep], useRealData);
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

    private ReturnStatus findCurrentValue(ISingleDimensionGroup sg, GroupConnectionValue gv, IntList list, int row) {
        NoneDimensionGroup nds = sg.getChildDimensionGroup(row);
        if (nds == NoneDimensionGroup.NULL) {
            return ReturnStatus.GroupOutOfBounds;
        }
        GroupConnectionValue ngv = createGroupConnectionValue(sg, row, nds);
        list.add(row);
        ngv.setParent(gv);
        return ReturnStatus.Success;
    }

    protected GroupConnectionValue createGroupConnectionValue(ISingleDimensionGroup sg, int row, NoneDimensionGroup nds) {
        GroupConnectionValue ngv = new GroupConnectionValue(sg.getChildData(row), nds);
        return ngv;
    }

    @Override
    public IRootDimensionGroup createClonedRoot() {
        RootDimensionGroup rootDimensionGroup = (RootDimensionGroup) createNew();
        rootDimensionGroup.metricGroupInfoList = metricGroupInfoList;
        rootDimensionGroup.mergeIteratorCreators = mergeIteratorCreators;
        rootDimensionGroup.sumLength = sumLength;
        rootDimensionGroup.session = session;
        rootDimensionGroup.useRealData = useRealData;
        rootDimensionGroup.getters = getters;
        rootDimensionGroup.columns = columns;
        rootDimensionGroup.tis = tis;
        rootDimensionGroup.singleDimensionGroupCache = singleDimensionGroupCache.clone();
        rootDimensionGroup.metrics = metrics;
        rootDimensionGroup.summaryLists = summaryLists;
        rootDimensionGroup.root = root;
        rootDimensionGroup.rowSize = rowSize;
        return rootDimensionGroup;
    }

    @Override
    public void checkStatus() {
        checkThreadPool();
    }

    private void checkThreadPool() {
        BIMultiThreadExecutor executor = MultiThreadManagerImpl.getInstance().isMultiCall() ? MultiThreadManagerImpl.getInstance().getExecutorService() : null;
        for (MergeIteratorCreator creator : mergeIteratorCreators) {
            creator.setExecutor(executor);
        }
    }

    protected IRootDimensionGroup createNew() {
        return new RootDimensionGroup();
    }
}