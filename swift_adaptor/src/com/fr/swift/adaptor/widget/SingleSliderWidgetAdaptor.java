package com.fr.swift.adaptor.widget;

import com.finebi.conf.internalimp.analysis.bean.operator.add.group.custom.number.NumberMaxAndMinValue;
import com.finebi.conf.internalimp.dashboard.widget.control.number.SingleSliderWidget;
import com.finebi.conf.structure.bean.field.FineBusinessField;
import com.finebi.conf.structure.bean.table.FineBusinessTable;
import com.finebi.conf.structure.dashboard.widget.dimension.FineDimension;
import com.finebi.conf.structure.result.control.number.BISingleSliderResult;
import com.finebi.conf.utils.FineTableUtils;
import com.fr.swift.adaptor.transformer.FilterInfoFactory;
import com.fr.swift.adaptor.transformer.IndexingDataSourceFactory;
import com.fr.swift.cal.Query;
import com.fr.swift.cal.builder.QueryBuilder;
import com.fr.swift.cal.info.DetailQueryInfo;
import com.fr.swift.cal.result.group.RowCursor;
import com.fr.swift.log.SwiftLogger;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.query.adapter.dimension.DetailDimension;
import com.fr.swift.query.filter.info.FilterInfo;
import com.fr.swift.query.sort.AscSort;
import com.fr.swift.query.sort.DescSort;
import com.fr.swift.result.DetailResultSet;
import com.fr.swift.segment.column.ColumnKey;
import com.fr.swift.source.DataSource;
import com.fr.swift.structure.array.IntList;
import com.fr.swift.structure.array.IntListFactory;

import java.util.List;

/**
 * Created by pony on 2018/3/22.
 */
public class SingleSliderWidgetAdaptor {
    private static final SwiftLogger LOGGER = SwiftLoggers.getLogger(SingleSliderWidgetAdaptor.class);
    public static BISingleSliderResult calculate(SingleSliderWidget widget) {
        NumberMaxAndMinValue value = new NumberMaxAndMinValue();
        try {
            List<FineDimension> dimensions = widget.getDimensionList();
            for (FineDimension dimension : dimensions){
                String fieldId = dimension.getFieldId();
                FineBusinessTable fineBusinessTable = FineTableUtils.getTableByFieldId(fieldId);
                FineBusinessField fineBusinessField = fineBusinessTable.getFieldByFieldId(fieldId);
                DataSource baseDataSource = IndexingDataSourceFactory.transformDataSource(fineBusinessTable);
                FilterInfo filterInfo = FilterInfoFactory.transformFineFilter(widget.getFilters());
                //先通过明细表排序查最小
                DetailDimension ascDimension = new DetailDimension(0, baseDataSource.getSourceKey(), new ColumnKey(fineBusinessField.getName()),
                        null, new AscSort(0), filterInfo);
                IntList sortIndex = IntListFactory.createHeapIntList(1);
                sortIndex.add(0);
                DetailQueryInfo minQueryInfo = new DetailQueryInfo(new RowCursor(), widget.getWidgetId(), new DetailDimension[]{ascDimension}, baseDataSource.getSourceKey(), null, null, null);
                Query<DetailResultSet> minQuery = QueryBuilder.buildQuery(minQueryInfo);
                DetailResultSet minResultSet = minQuery.getQueryResult();
                minResultSet.next();
                Number min = minResultSet.getRowData().getValue(0);
                value.setMin(Math.min(value.getMin(), min.doubleValue()));
                //再通过明细表排序差最大
                DetailDimension descDimension = new DetailDimension(0, baseDataSource.getSourceKey(), new ColumnKey(fineBusinessField.getName()),
                        null, new DescSort(0), filterInfo);
                DetailQueryInfo maxQueryInfo = new DetailQueryInfo(new RowCursor(), widget.getWidgetId(), new DetailDimension[]{descDimension}, baseDataSource.getSourceKey(), null, null, null);
                Query<DetailResultSet> maxQuery = QueryBuilder.buildQuery(maxQueryInfo);
                DetailResultSet maxResultSet = maxQuery.getQueryResult();
                maxResultSet.next();
                Number max = maxResultSet.getRowData().getValue(0);
                value.setMax(Math.max(value.getMax(), max.doubleValue()));
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return new SingleSliderResult(value);
    }

    static class SingleSliderResult implements BISingleSliderResult{
        private NumberMaxAndMinValue value;

        SingleSliderResult(NumberMaxAndMinValue value) {
            this.value = value;
        }

        @Override
        public NumberMaxAndMinValue getMaxAndMinValue() {
            return value;
        }

        @Override
        public ResultType getResultType() {
            return ResultType.INTERVAL_SLIDER;
        }
    }
}

