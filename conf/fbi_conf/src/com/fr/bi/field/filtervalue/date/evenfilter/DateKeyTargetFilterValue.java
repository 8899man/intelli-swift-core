package com.fr.bi.field.filtervalue.date.evenfilter;

import com.finebi.cube.api.ICubeColumnIndexReader;
import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.BICubeConfigureCenter;
import com.finebi.cube.conf.table.BusinessTable;
import com.finebi.cube.relation.BITableRelationPath;
import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.conf.report.widget.field.filtervalue.AbstractFilterValue;
import com.fr.bi.conf.report.widget.field.filtervalue.date.DateFilterValue;
import com.fr.bi.stable.data.key.date.BIDateValue;
import com.fr.bi.stable.data.key.date.BIDateValueFactory;
import com.fr.bi.stable.engine.index.key.IndexTypeKey;
import com.fr.bi.stable.exception.BITableUnreachableException;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.gvi.GroupValueIndexOrHelper;
import com.fr.bi.stable.report.key.TargetGettingKey;
import com.fr.bi.stable.report.result.DimensionCalculator;
import com.fr.bi.stable.report.result.BINode;
import com.fr.bi.util.BIConfUtils;
import com.fr.fs.control.UserControl;
import com.fr.general.ComparatorUtils;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;
import com.fr.stable.xml.XMLPrintWriter;
import com.fr.stable.xml.XMLableReader;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DateKeyTargetFilterValue extends AbstractFilterValue<Long> implements DateFilterValue {

    /**
     *
     */
    private static final long serialVersionUID = -2509778015034905186L;
    @BICoreField
    protected int group;
    @BICoreField
    protected Set<BIDateValue> valueSet;

    private JSONObject valueJo;
    private int type;
    private int month;
    private int year;
    private int day;

    public DateKeyTargetFilterValue() {

    }

    public DateKeyTargetFilterValue(int group, Set<BIDateValue> valueSet) {
        this.group = group;
        this.valueSet = valueSet;
    }

    /**
     * 获取过滤后的索引
     *
     * @param target
     * @param loader loader对象
     * @return 过滤索引
     */
    @Override
    public GroupValueIndex createFilterIndex(DimensionCalculator dimension, BusinessTable target, ICubeDataLoader loader, long userId) {
        if (valueSet == null || valueSet.isEmpty()) {
            return getGroupValueIndexWhenNull(target, loader);
        }
        BITableRelationPath firstPath = null;
        try {
            firstPath = BICubeConfigureCenter.getTableRelationManager().getFirstPath(userId, target, dimension.getField().getTableBelongTo());
        } catch (BITableUnreachableException e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
        if (ComparatorUtils.equals(dimension.getField().getTableBelongTo(), target)) {
            firstPath = new BITableRelationPath();
        }
        if (firstPath == null) {
            return null;
        }

        ICubeColumnIndexReader getter = loader.getTableIndex(dimension.getField().getTableBelongTo().getTableSource()).loadGroup(new IndexTypeKey(dimension.getField().getFieldName(), group), BIConfUtils.convert2TableSourceRelation(firstPath.getAllRelations()));
        Iterator<BIDateValue> it = valueSet.iterator();
        GroupValueIndexOrHelper helper = new GroupValueIndexOrHelper();
        while (it.hasNext()) {
            BIDateValue dk = it.next();
            Object[] keys = getter.createKey(1);
            keys[0] = dk == null ? null : dk.getValue();
            GroupValueIndex cgvi = getter.getGroupIndex(keys)[0];
            helper.add(cgvi);
        }
        return helper.compute();
    }

    private GroupValueIndex getGroupValueIndexWhenNull(BusinessTable targetKey, ICubeDataLoader loader) {
        return null;
    }

    @Override
    public boolean isAllCalculatorFilter() {
        return false;
    }

    public Set<BIDateValue> getValues() {
        return valueSet;
    }

    /**
     * 解析json
     *
     * @param jo     json对象
     * @param userId 用户id
     * @throws Exception 报错
     */
    @Override
    public void parseJSON(JSONObject jo, long userId) throws Exception {
        this.valueSet = new HashSet<BIDateValue>();
        if (jo.has("filter_value")) {
            valueJo = jo.getJSONObject("filter_value");
            if (valueJo.has("group") && valueJo.has("values")) {
                this.valueSet.add(BIDateValueFactory.createDateValue(valueJo.getInt("group"), valueJo.getLong("values")));
                this.group = valueJo.getInt("group");
            }
            if (valueJo.has("value")) {
                JSONObject filterValueJo = valueJo.optJSONObject("value");
                //todo 统一value的数据结构 value值可能为object,也可能为int,
                if(filterValueJo == null){
                    return;
                }
                if(filterValueJo.has("year")){
                    this.year = filterValueJo.getInt("year");
                }
                if(filterValueJo.has("month")){
                    this.month = filterValueJo.getInt("month");
                }
                if(filterValueJo.has("day")){
                    this.day = filterValueJo.getInt("day");
                }
            }
        }
    }

    /**
     * 简化
     *
     * @param subType   类型
     * @param value     值
     * @param fieldType 字段日期类型
     * @throws Exception
     */
    private void initValueSetFromValue(int subType, Object value, int fieldType) throws Exception {

    }

    /**
     * todo 代码不好
     * 创建json
     *
     * @return json对象
     * @throws Exception 报错
     */
    @Override
    public JSONObject createJSON() throws Exception {
        return valueJo;
    }

    /**
     * @param reader XML读取对象
     * @see com.fr.stable.xml.XMLableReader
     */
    @Override
    public void readXML(XMLableReader reader) {
        if (reader.getTagName().equals("filter_value")) {

            try {
                JSONObject jo = new JSONObject(reader.getAttrAsString("filter_value", StringUtils.EMPTY));
                this.parseJSON(jo, UserControl.getInstance().getSuperManagerID());
            } catch (Exception e) {
                BILoggerFactory.getLogger().error(e.getMessage(), e);
            }
        }
    }

    /**
     * @param writer XML写入对象
     */
    @Override
    public void writeXML(XMLPrintWriter writer) {
        writer.startTAG(XML_TAG);

        try {
            writer.attr("filter_value", createJSON().toString());
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
        writer.end();
    }

    @Override
    public boolean canCreateFilterIndex() {
        return false;
    }

    @Override
    public boolean showNode(BINode node, TargetGettingKey targetKey, ICubeDataLoader loader) {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateKeyTargetFilterValue that = (DateKeyTargetFilterValue) o;

        if (group != that.group) return false;
        if (type != that.type) return false;
        if (month != that.month) return false;
        if (year != that.year) return false;
        if (day != that.day) return false;
        return valueSet != null ? valueSet.equals(that.valueSet) : that.valueSet == null;

    }

    @Override
    public int hashCode() {
        int result = group;
        result = 31 * result + (valueSet != null ? valueSet.hashCode() : 0);
        result = 31 * result + type;
        result = 31 * result + month;
        result = 31 * result + year;
        result = 31 * result + day;
        return result;
    }

    @Override
    public boolean isMatchValue(Long v) {
        if (v == null) {

        }
        return false;
    }
}