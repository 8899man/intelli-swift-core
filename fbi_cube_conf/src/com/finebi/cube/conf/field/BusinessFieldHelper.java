package com.finebi.cube.conf.field;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.BICubeConfigureCenter;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.exception.BIKeyAbsentException;
import com.fr.bi.stable.data.BIFieldID;
import com.fr.bi.stable.utils.program.BINonValueUtils;

/**
 * This class created on 2016/5/26.
 * 参数必须要有ID
 *
 * @author Connery
 * @since 4.0
 */
public class BusinessFieldHelper {
    public static BusinessField getBusinessFieldSource(BIFieldID fieldID) {
        BINonValueUtils.checkNull(fieldID);
        BINonValueUtils.checkNull(fieldID.getIdentity());
        try {
            return BICubeConfigureCenter.getDataSourceManager().getBusinessField(fieldID);
        } catch (BIKeyAbsentException e) {
            BILoggerFactory.getLogger(BusinessFieldHelper.class).error("the field is absent + \n + fieldID is : " + fieldID.getIdentityValue());
            throw BINonValueUtils.beyondControl(e);
        }
    }

    public static BusinessTable getBusinessTable(BusinessField businessField) {
        BINonValueUtils.checkNull(businessField);
        BusinessTable table = getBusinessFieldSource(businessField.getFieldID()).getTableBelongTo();
        if (!(businessField instanceof BIBusinessFieldGetter)) {
            businessField.setTableBelongTo(table);
        }
        return table;
    }
}
