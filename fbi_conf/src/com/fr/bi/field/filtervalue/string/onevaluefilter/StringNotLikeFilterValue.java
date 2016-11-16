package com.fr.bi.field.filtervalue.string.onevaluefilter;

import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.result.DimensionCalculator;
import com.fr.stable.StringUtils;

public class StringNotLikeFilterValue extends StringOneValueFilterValue {
    /**
     *
     */
    private static final long serialVersionUID = -462082894567723171L;
    private static String XML_TAG = "StringNotLikeFilterValue";

    @BICoreField
    private String CLASS_TYPE = "StringNotLikeFilterValue";
    /**
     * 获取过滤后的索引
     *
     * @return 过滤索引
     */
    @Override
    public GroupValueIndex createFilterIndex(DimensionCalculator dimension, BusinessTable target, ICubeDataLoader loader, long userId) {
        if (StringUtils.isEmpty(value)) {
            return loader.getTableIndex(dimension.getField().getTableBelongTo().getTableSource()).getAllShowIndex();
        }
        return super.createFilterIndex(dimension, target, loader, userId)
                .AND(loader.getTableIndex(dimension.getField().getTableBelongTo().getTableSource()).getAllShowIndex());
    }

    /* (non-Javadoc)
     * @see com.fr.bi.report.data.filter.value.string.StringOneValueFilterValue#contains()
     */
    @Override
    public boolean isMatchValue(String key) {
        return key.indexOf(value) == -1;
    }

}