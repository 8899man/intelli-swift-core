package com.fr.bi.report.result;

import com.finebi.cube.conf.field.BusinessField;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.base.key.BIKey;
import com.finebi.cube.api.ICubeDataLoader;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.operation.group.IGroup;
import com.finebi.cube.relation.BITableSourceRelation;
import com.finebi.cube.api.ICubeColumnIndexReader;
import com.fr.stable.FCloneable;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by 小灰灰 on 2015/6/30.
 */
public interface DimensionCalculator extends FCloneable,Serializable {

     BusinessField getField();


    ICubeColumnIndexReader createNoneSortNoneGroupValueMapGetter(BusinessTable target, ICubeDataLoader loader);
    /**
     * 获取维度到维度/指标的分组索引
     *
     * @param target
     * @param loader
     * @return
     */
    ICubeColumnIndexReader createNoneSortGroupValueMapGetter(BusinessTable target, ICubeDataLoader loader);

    List<BITableSourceRelation> getRelationList();

    List<BITableSourceRelation> getDirectToDimensionRelationList();

    void setRelationList(List<BITableSourceRelation> relationList);

    BIKey createKey();

    Comparator getComparator();

    Object createEmptyValue();

    int getSortType();

    Iterator createValueMapIterator(BusinessTable table, ICubeDataLoader loader);

    Iterator createValueMapIterator(BusinessTable table, ICubeDataLoader loader, boolean useReallData, int groupLimit, GroupValueIndex filterGvi);

    IGroup getGroup();

    Object convertToOriginValue(String stringValue);

}