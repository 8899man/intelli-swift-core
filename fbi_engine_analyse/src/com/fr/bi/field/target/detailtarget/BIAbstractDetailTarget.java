package com.fr.bi.field.target.detailtarget;

import com.finebi.cube.api.*;
import com.finebi.cube.conf.table.BusinessTable;
import com.finebi.cube.relation.BITableRelation;
import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.cluster.utils.ClusterLockObject;
import com.fr.bi.common.persistent.xml.BIIgnoreField;
import com.fr.bi.conf.report.widget.field.target.detailtarget.BIDetailTarget;
import com.fr.bi.conf.report.widget.field.target.filter.TargetFilter;
import com.fr.bi.field.BIStyleTarget;
import com.fr.bi.field.target.filter.TargetFilterFactory;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.operation.group.BIGroupFactory;
import com.fr.bi.stable.operation.group.IGroup;
import com.fr.bi.stable.operation.group.group.NoGroup;
import com.fr.bi.stable.operation.sort.BISortFactory;
import com.fr.bi.stable.operation.sort.BISortUtils;
import com.fr.bi.stable.operation.sort.ISort;
import com.fr.bi.stable.operation.sort.sort.NoSort;
import com.fr.bi.stable.structure.collection.CubeIndexGetterWithNullValue;
import com.fr.bi.util.BIConfUtils;
import com.fr.general.ComparatorUtils;
import com.fr.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class BIAbstractDetailTarget extends BIStyleTarget implements BIDetailTarget {
    private static final long serialVersionUID = -8312854279862416396L;
    @BICoreField
    protected TargetFilter filter;
    @BIIgnoreField
    protected transient ICubeColumnDetailGetter columnDetailGetter;
    @BIIgnoreField
    protected transient ICubeColumnDetailGetter copyColumnDetailGetter;
    protected ISort sort = new NoSort();
    @BICoreField
    protected IGroup group = new NoGroup();
    @BIIgnoreField
    private ClusterLockObject columnDetailGetterLock = new ClusterLockObject();
    private List<BITableRelation> relationList = new ArrayList<BITableRelation>();


    public TargetFilter getFilter() {

        return filter;
    }

    /**
     * 取值
     *
     * @param row    行
     * @param values 相关的值map
     * @param loader loader对象
     * @return 实际值
     */
    @Override
    public Object createDetailValue(Long row, Map<String, Object> values, ICubeDataLoader loader, long userId) {
        if (row != null) {
            int r = row.intValue();
            if (r > -1) {
                initialTableSource(loader);
                return columnDetailGetter.getValue(r);
            }
        }
        return null;
    }

    protected void initialTableSource(ICubeDataLoader loader) {
        synchronized (columnDetailGetterLock) {
            if (columnDetailGetter == null) {
                columnDetailGetter = loader.getTableIndex(this.createTableKey().getTableSource()).getColumnDetailReader(this.createKey(getStatisticElement()));
            }
        }
    }

    private boolean shouldClearDetailGetter(ICubeColumnDetailGetter newGetter, ICubeColumnDetailGetter oldGetter){
        if(newGetter !=null && oldGetter !=null
                &&!newGetter.getICubeResourceLocationPath().equals(oldGetter.getICubeResourceLocationPath())){
            return true;
        }else {
            return false;
        }
    }
    @Override
    public List<BITableRelation> getRelationList(BusinessTable target, long userId) {
        return relationList;
    }


    public void setRelationList(List<BITableRelation> relationList) {
        this.relationList = relationList;
    }

    @Override
    public ISort getSort() {
        return sort;
    }

    public void setSort(ISort sort) {
        this.sort = sort;
    }

    /**
     * 创建分组的map
     *
     * @param target 目标表
     * @param loader loader对象
     * @return 分组的map
     */
    @Override
    public CubeIndexGetterWithNullValue createGroupValueMapGetter(BusinessTable target, ICubeDataLoader loader, long userId) {
        ICubeTableService ti = loader.getTableIndex(column.getTableBelongTo().getTableSource());
        ICubeColumnIndexReader baseGroupMap = ti.loadGroup(createKey(getStatisticElement()), BIConfUtils.convert2TableSourceRelation(getRelationList(target, userId)));
        ICubeValueEntryGetter getter = ti.getValueEntryGetter(createKey(getStatisticElement()), BIConfUtils.convert2TableSourceRelation(getRelationList(target, userId)));
        return new CubeIndexGetterWithNullValue(baseGroupMap, getter, ti.getNullGroupValueIndex(createKey(getStatisticElement())));
    }

    /**
     * 将JSON对象转换成java对象
     *
     * @param jo     json对象
     * @param userId 用户的id
     * @throws Exception
     */
    @Override
    public void parseJSON(JSONObject jo, long userId) throws Exception {
        super.parseJSON(jo, userId);
        if (jo.has("conditions")) {
            filter = TargetFilterFactory.parseFilter(jo.getJSONObject("conditions"), userId);
        }
        if (jo.has("sort")) {
            JSONObject sortJo = jo.optJSONObject("sort");
            sortJo.put("type", BISortUtils.getSortTypeByDimensionType(sortJo.optInt("type", BIReportConstant.SORT.NONE), jo.optInt("type")));
            this.sort = BISortFactory.parseSort(sortJo);
        }
        if (jo.has("filter_value")) {
            this.filter = TargetFilterFactory.parseFilter(jo.getJSONObject("filter_value"), userId);
        }

        if (jo.has("group")) {
            JSONObject groupJo = jo.getJSONObject("group");
            group = BIGroupFactory.parseGroup(groupJo);
        }
    }


    @Override
    public void clear() {
//        if (columnDetailGetter != null) {
//            columnDetailGetter.clear();
//        }
    }
    public void reSetDetailGetter(){
        if (columnDetailGetter != null) {
            synchronized (columnDetailGetterLock) {
                if( shouldClearDetailGetter(columnDetailGetter,copyColumnDetailGetter)){
                    copyColumnDetailGetter.clear();
                    copyColumnDetailGetter = columnDetailGetter;
                }
                columnDetailGetter = null;
            }
        }
    }

    public IGroup getGroup() {
        return group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        BIAbstractDetailTarget that = (BIAbstractDetailTarget) o;

        if (!ComparatorUtils.equals(filter, that.filter)) {
            return false;
        }
        if (!ComparatorUtils.equals(sort, that.sort)) {
            return false;
        }
        if (!ComparatorUtils.equals(group, that.group)) {
            return false;
        }
        return ComparatorUtils.equals(relationList, that.relationList);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (filter != null ? filter.hashCode() : 0);
        result = 31 * result + (sort != null ? sort.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (relationList != null ? relationList.hashCode() : 0);
        return result;
    }
}
