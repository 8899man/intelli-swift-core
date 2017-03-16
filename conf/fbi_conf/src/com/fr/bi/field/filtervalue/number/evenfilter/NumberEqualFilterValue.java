package com.fr.bi.field.filtervalue.number.evenfilter;

import com.finebi.cube.api.ICubeDataLoader;
import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.stable.report.key.TargetGettingKey;
import com.fr.bi.stable.report.result.BINode;

import java.math.BigDecimal;


public class NumberEqualFilterValue extends NumberEvenFilterValue {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8506484357364003247L;

    @BICoreField
    private String CLASS_TYPE = "NumberEqualFilterValue";

	/**
     * 显示节点
     *
     * @param node      节点
     * @param targetKey 指标信息
     * @return true或false
     */
    @Override
    public boolean showNode(BINode node, TargetGettingKey targetKey, ICubeDataLoader loader) {
        Number targetValue = node.getSummaryValue(targetKey);
        if (targetValue == null) {
            return false;
        }
        double v = targetValue.doubleValue();
        if (Double.isNaN(v)) {
            return false;
        }
        BigDecimal v1 = new BigDecimal(v);
        BigDecimal v2 = new BigDecimal(V);
        return v1.compareTo(v2) == 0;
    }
}