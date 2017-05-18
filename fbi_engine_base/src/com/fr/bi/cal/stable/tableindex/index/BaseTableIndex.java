package com.fr.bi.cal.stable.tableindex.index;

import com.finebi.cube.api.ICubeColumnDetailGetter;
import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.field.BusinessField;
import com.fr.bi.base.key.BIKey;
import com.fr.bi.cal.stable.tableindex.AbstractTableIndex;
import com.fr.bi.stable.engine.index.BITableCubeFile;
import com.fr.bi.stable.engine.index.key.IndexKey;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.gvi.traversal.CalculatorTraversalAction;
import com.fr.bi.stable.gvi.traversal.SingleRowTraversalAction;
import com.fr.bi.stable.io.newio.NIOConstant;
import com.fr.bi.stable.io.newio.SingleUserNIOReadManager;
import com.fr.bi.stable.utils.BICollectionUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by GUY on 2015/3/18.
 */
public abstract class BaseTableIndex extends AbstractTableIndex {

    protected Map<BIKey, Double> numberMaxValueMap = new ConcurrentHashMap<BIKey, Double>();
    protected Map<BIKey, Double> numberMinValueMap = new ConcurrentHashMap<BIKey, Double>();
    protected Map<BIKey, Double> numberSumValueMap = new ConcurrentHashMap<BIKey, Double>();

    protected BaseTableIndex(String path, SingleUserNIOReadManager manager) {
        super(path, manager);
    }

    public BaseTableIndex(BITableCubeFile cube) {
        super(cube);
    }

    @Override
    public BIKey getColumnIndex(String fieldName) {
        return new IndexKey(fieldName);
    }

    @Override
    public BIKey getColumnIndex(BusinessField field) {
        return getColumnIndex(field.getFieldName());
    }


    @Override
    public double getMAXValue(GroupValueIndex gvi, BIKey summaryIndex) {
        final ICubeColumnDetailGetter list = getColumnDetailReader(summaryIndex);
        CalculatorTraversalAction ss = new CalculatorTraversalAction() {
            boolean firstValue = true;

            @Override
            public void actionPerformed(int row) {
                Object v = list.getValue(row);
                if (v != null) {
                    double res = ((Number) v).doubleValue();
                    if (firstValue) {
                        firstValue = false;
                        sum = res;
                    } else {
                        sum = Math.max(sum, res);
                    }
                }
            }

            @Override
            public double getCalculatorValue() {
                return sum;
            }
        };
        gvi.Traversal(ss);
        return ss.getCalculatorValue();
    }

    @Override
    public double getMAXValue(BIKey summaryIndex) {
        if (numberMaxValueMap.get(summaryIndex) == null) {
            numberMaxValueMap.put(summaryIndex, getMAXValue(allShowIndex, summaryIndex));
        }
        return numberMaxValueMap.get(summaryIndex);
    }

    @Override
    public double getMINValue(GroupValueIndex gvi, BIKey summaryIndex) {
        final ICubeColumnDetailGetter list = getColumnDetailReader(summaryIndex);
        CalculatorTraversalAction ss = new CalculatorTraversalAction() {
            boolean firstValue = true;

            @Override
            public void actionPerformed(int row) {
                Object v = list.getValue(row);
                if (v != null) {
                    double res = ((Number) v).doubleValue();
                    if (firstValue) {
                        firstValue = false;
                        sum = res;
                    } else {
                        sum = Math.min(sum, res);
                    }
                }
            }

            @Override
            public double getCalculatorValue() {
                return sum;
            }
        };
        gvi.Traversal(ss);
        return ss.getCalculatorValue();
    }

    @Override
    public double getMINValue(BIKey summaryIndex) {
        if (numberMinValueMap.get(summaryIndex) == null) {
            numberMinValueMap.put(summaryIndex, getMINValue(allShowIndex, summaryIndex));
        }
        return numberMinValueMap.get(summaryIndex);
    }

    @Override
    public double getSUMValue(GroupValueIndex gvi, BIKey summaryIndex) {
        final ICubeColumnDetailGetter list = getColumnDetailReader(summaryIndex);
        CalculatorTraversalAction ss = new CalculatorTraversalAction() {
            // 这里不能像SumCalculator那样先获取PrimitiveDetailGetter然后在判断是否都为空值因为MemoryDetailGetter不支持
            // 所以只能一个个进行判断

            protected double sum = NIOConstant.DOUBLE.NULL_VALUE;

            @Override
            public void actionPerformed(int row) {
                Object v = list.getValue(row);
                // 空汇总值不参与计算
                if (!BICollectionUtils.isCubeNullKey(v)) {
                    if (BICollectionUtils.isCubeNullKey(sum)) {
                        sum = 0;
                    }
                    double res = ((Number) v).doubleValue();
                    sum += res;
                }
            }

            @Override
            public double getCalculatorValue() {
                return sum;
            }
        };
        gvi.Traversal(ss);
        return ss.getCalculatorValue();
    }

    @Override
    public double getSUMValue(BIKey summaryIndex) {
        if (numberSumValueMap.get(summaryIndex) == null) {
            numberSumValueMap.put(summaryIndex, getSUMValue(allShowIndex, summaryIndex));
        }
        return numberSumValueMap.get(summaryIndex);
    }

    @Override
    public double getDistinctCountValue(GroupValueIndex gvi, final BIKey distinct_field) {
        final Set<Object> resMap = new HashSet<Object>();
        final ICubeColumnDetailGetter getter = getColumnDetailReader(distinct_field);
        SingleRowTraversalAction ss = new SingleRowTraversalAction() {
            @Override
            public void actionPerformed(int row) {
                Object v = getter.getValue(row);
                //D:null值不做统计
                if (v != null) {
                    resMap.add(v);
                }
            }
        };
        gvi.Traversal(ss);
        return resMap.size();
    }

    @Override
    public ICubeColumnDetailGetter getColumnDetailReader(BIKey key) {
        try {
            return cube.createDetailGetter(key, manager);
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void clear() {
        cube.clear();
    }
}