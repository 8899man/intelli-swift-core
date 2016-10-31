/**
 *
 */
package com.fr.bi.field.filtervalue.number.containsfilter;

import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.api.ICubeTableService;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.result.DimensionCalculator;

/**
 * @author Daniel
 *
 */
public class NumberContainsFilterValue extends NumberValuesFilterValue {

	/**
	 *
	 */
	private static final long serialVersionUID = -6973324281928100499L;

	@BICoreField
	private String CLASS_TYPE = "NumberContainsFilterValue";

	@Override
	public boolean isMatchValue(Number value) {
		return valueSet.contains(value.doubleValue());
	}


    @Override
    public GroupValueIndex createFilterIndex(DimensionCalculator dimension, BusinessTable target, ICubeDataLoader loader, long userId) {
        GroupValueIndex gvi = super.createFilterIndex(dimension, target, loader, userId);
        ICubeTableService ti = loader.getTableIndex(target.getTableSource());
        return gvi == null ? ti.getAllShowIndex()
                : gvi;
    }

}