package com.finebi.cube.structure.column.date;

import com.finebi.cube.data.ICubeResourceDiscovery;
import com.finebi.cube.location.ICubeResourceLocation;
import com.finebi.cube.structure.column.BICubeIntegerColumn;
import com.fr.bi.base.ValueConverterFactory;
import com.fr.bi.stable.constant.DateConstant;

/**
 * This class created on 2017/03/28
 * 周数
 * @author wang
 */
public class BICubeWeekNumberColumn extends BICubeDateSubColumn<Integer> {
    public BICubeWeekNumberColumn(ICubeResourceDiscovery discovery, ICubeResourceLocation currentLocation, BICubeDateColumn hostDataColumn) {
        super(discovery, currentLocation, hostDataColumn);
    }

    @Override
    protected Integer convertDate(Long date) {
        return date != null ? (Integer) ValueConverterFactory.createDateValueConverter(DateConstant.DATE.WEEKNUMBER).result2Value(date) : null;

    }

    @Override
    protected void initialColumnEntity(ICubeResourceLocation currentLocation) {
        selfColumnEntity = new BICubeIntegerColumn(discovery, currentLocation);

    }

    public int getGroupValue(int position) {
        return ((BICubeIntegerColumn)selfColumnEntity).getGroupValue(position);
    }
}
