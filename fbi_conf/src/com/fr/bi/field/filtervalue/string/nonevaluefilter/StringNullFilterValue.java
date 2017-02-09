package com.fr.bi.field.filtervalue.string.nonevaluefilter;

import com.finebi.cube.api.ICubeColumnIndexReader;
import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.api.ICubeTableService;
import com.finebi.cube.conf.table.BusinessTable;
import com.finebi.cube.relation.BITableSourceRelation;
import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.stable.gvi.GVIFactory;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.result.DimensionCalculator;
import com.fr.stable.StringUtils;

import java.util.List;


public class StringNullFilterValue extends StringNoneValueFilterValue {
    /**
     *
     */
    private static final long serialVersionUID = 2782005822329326343L;
    private static String XML_TAG = "StringNullFilterValue";

    @BICoreField
    private String CLASS_TYPE = "StringNullFilterValue";

    @Override
    public boolean isMatchValue(String value) {
        return StringUtils.isEmpty(value);
    }

    /**
     * 获取过滤后的索引
     *
     * @return 过滤索引
     */
    @Override
    public GroupValueIndex createFilterIndex(DimensionCalculator dimension, BusinessTable target, ICubeDataLoader loader, long userId) {
        ICubeTableService ti = loader.getTableIndex(dimension.getField().getTableBelongTo().getTableSource());
        List<BITableSourceRelation> relations = dimension.getRelationList();
        ICubeColumnIndexReader getter = ti.loadGroup(dimension.createKey(), relations);
        GroupValueIndex res = getter.getGroupIndex(new String[]{StringUtils.EMPTY})[0];
        GroupValueIndex nullres = getter.getNULLIndex();
        if (res == null) {
            res = nullres;
        } else {
            res = res.OR(nullres);
        }
        if (res == null) {
            return GVIFactory.createAllEmptyIndexGVI();
        } else {
            return res;
        }
    }

}