package com.fr.bi.field.target.detailtarget.field;

import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.field.target.detailtarget.BIAbstractDetailTarget;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.general.ComparatorUtils;

import java.util.Map;

public class BIStringDetailTarget extends BIAbstractDetailTarget {

    /**
     * 获取显示值
     *
     * @param value 明细表一行值
     * @return 显示值
     */
    @Override
    public Object createShowValue(Object value) {
        if (ComparatorUtils.equals(value, BIReportConstant.SPECIAL_FIELD_VALUE)) {
            return "";
        }
        return value;
    }

    @Override
    public BusinessTable createTableKey() {
        return column.getTableBelongTo();
    }

    /**
     * 是否为计算指标
     *
     * @return 是否为计算指标
     */
    @Override
    public boolean isCalculateTarget() {
        return false;
    }

    /**
     * 可否计算，看他调用到的指标有没有算完，处理计算指标嵌套情况
     *
     * @param values 指标名和值的map
     * @return 可否计算
     */

    @Override
    public boolean isReady4Calculate(Map<String, Object> values) {
        return true;
    }

}