package com.fr.bi.cal.analyze.cal.sssecret;

import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.api.ICubeTableService;
import com.finebi.cube.api.ICubeValueEntryGetter;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.cal.analyze.cal.index.loader.TargetAndKey;
import com.fr.bi.cal.analyze.cal.sssecret.diminfo.MergeIteratorCreator;
import com.fr.bi.common.inter.Release;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.result.DimensionCalculator;

import java.util.List;


/**
 * TODO 需要改成可以半路计算，提升即时计算性能
 * 即可以用之前已经计算好的结果继续计算
 *
 * @author Daniel
 *         分页机制，使用另外一个线程来判断计算当前已经计算了多少结果了 并取数
 */
public class NoneDimensionGroup implements Release {

    public final static NoneDimensionGroup NULL = new NoneDimensionGroup();

    private BusinessTable[] metrics;
    private List<TargetAndKey>[] summaryLists;
    private GroupValueIndex[] gvis;
    private ICubeTableService[] tis;
    private ICubeDataLoader loader;

    protected NoneDimensionGroup() {
    }


    /**
     * Group计算的构造函数
     */
    protected NoneDimensionGroup(BusinessTable[] metrics, List<TargetAndKey>[] summaryLists, ICubeTableService[] tis, GroupValueIndex[] gvis, ICubeDataLoader loader) {
        this.metrics = metrics;
        this.summaryLists = summaryLists;
        this.tis = tis;
        this.gvis = gvis;
        this.loader = loader;
    }


    public static NoneDimensionGroup createDimensionGroup(BusinessTable[] metrics, List<TargetAndKey>[] summaryLists, ICubeTableService[] tis, GroupValueIndex[] gvis, ICubeDataLoader loader) {
        return new NoneDimensionGroup(metrics, summaryLists, tis, gvis, loader);
    }


    public ISingleDimensionGroup createSingleDimensionGroup(DimensionCalculator[] columns, ICubeValueEntryGetter[] getters, Object[] data, MergeIteratorCreator mergeIteratorCreator, boolean useRealData) {
        return SingleDimensionGroup.createDimensionGroup(metrics, summaryLists, tis, columns, getters, data, gvis, mergeIteratorCreator, loader, useRealData);
    }


    public ISingleDimensionGroup createSingleDimensionGroup(DimensionCalculator[] columns, ICubeValueEntryGetter[] getters, Object[] data, MergeIteratorCreator mergeIteratorCreator, GroupValueIndex[] gvis, boolean useRealData) {
        return SingleDimensionGroup.createDimensionGroup(metrics, summaryLists, tis, columns, getters, data, gvis, mergeIteratorCreator, loader, useRealData);
    }


    /**
     * 释放资源，之前需要释放的，现在暂时没有什么需要释放的
     */
    @Override
    public void clear() {
//        root.release();
    }

    public List<TargetAndKey>[] getSummaryLists() {
        return summaryLists;
    }

    public GroupValueIndex[] getGvis() {
        return gvis;
    }

    public ICubeTableService[] getTis() {
        return tis;
    }

    public ICubeDataLoader getLoader() {
        return loader;
    }

}