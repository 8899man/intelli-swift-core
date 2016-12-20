/**
 *
 */
package com.fr.bi.field.target.filter.tree;

import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.conf.field.BusinessField;
import com.finebi.cube.conf.table.BusinessTable;
import com.finebi.cube.relation.BITableRelation;
import com.fr.bi.base.BICore;
import com.fr.bi.base.BICoreGenerator;
import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.common.BICoreService;
import com.fr.bi.field.dimension.calculator.NoneDimensionCalculator;
import com.fr.bi.field.filtervalue.string.rangefilter.StringINFilterValue;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.gvi.GroupValueIndexOrHelper;
import com.fr.bi.stable.report.result.DimensionCalculator;
import com.fr.bi.util.BIConfUtils;
import com.fr.general.ComparatorUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.json.JSONParser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class TreeFilterValue implements JSONParser, BICoreService {
    private static String XML_TAG = "TreeFilterValue";
    @BICoreField
    private String value;
    @BICoreField
    private TreeFilterValue[] childs;

    /**
     * 重写code
     *
     * @return 数值
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(childs);
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        TreeFilterValue other = (TreeFilterValue) obj;
        if (!ComparatorUtils.equals(childs, other.childs)) {
            return false;
        }

        if (value == null) {
            if (other.value != null) {
                return false;
            }

        } else if (!ComparatorUtils.equals(value, other.value)) {
            return false;
        }

        return true;
    }

    /**
     * 解析json
     *
     * @param jo json对象
     * @throws Exception 报错
     */
    @Override
    public void parseJSON(JSONObject jo) throws Exception {
        if (jo.has("value")) {
            value = jo.getString("value");
        }
        if (jo.has("childs")) {
            JSONArray ja = jo.getJSONArray("childs");
            int len = ja.length();
            childs = new TreeFilterValue[len];
            for (int i = 0; i < len; i++) {
                childs[i] = new TreeFilterValue();
                childs[i].parseJSON(ja.getJSONObject(i));
            }
        }
    }

    /**
     * 获取过滤值
     *
     * @return 字符串过滤值
     */
    private StringINFilterValue createFilterValue() {
        Set<String> valueSet = new HashSet<String>();
        valueSet.add(value);
        return new StringINFilterValue(valueSet);
    }

    /**
     * 获取过滤索引
     *
     * @param controlCKS        数组
     * @param controlLayerIndex 层级
     * @param tableKey          表
     * @param relations         filter中保存的关联
     * @param loader            loader对象
     * @return 索引
     */
    public GroupValueIndex createFilterIndex(DimensionCalculator calculator,
                                             BusinessField[] controlCKS, int controlLayerIndex,
                                             BusinessTable tableKey, BITableRelation[][] relations, ICubeDataLoader loader, long userId) {
        BusinessField ck = controlCKS[controlLayerIndex];
        GroupValueIndex gvi = createFilterValue().createFilterIndex(new NoneDimensionCalculator(ck, BIConfUtils.convert2TableSourceRelation(Arrays.asList(relations[controlLayerIndex]))), tableKey, loader, userId);
        if (childs != null) {
            GroupValueIndexOrHelper helper = new GroupValueIndexOrHelper();
            for (int i = 0; i < childs.length; i++) {
                if (childs[i] != null && controlCKS.length > controlLayerIndex + 1) {
                    GroupValueIndex tgvi = childs[i].createFilterIndex(calculator, controlCKS, controlLayerIndex + 1, tableKey, relations, loader, userId);
                    helper.add(tgvi);
                }
            }
            GroupValueIndex resgvi = helper.compute();
            if (gvi == null) {
                gvi = resgvi;
            } else {
                gvi = gvi.AND(resgvi);
            }
        }
        return gvi;
    }

    @Override
    public BICore fetchObjectCore() {
        return new BICoreGenerator(this).fetchObjectCore();
    }
}