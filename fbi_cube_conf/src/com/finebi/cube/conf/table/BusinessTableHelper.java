package com.finebi.cube.conf.table;

import com.finebi.cube.conf.BICubeConfigureCenter;
import com.finebi.cube.conf.field.BusinessField;
import com.finebi.cube.conf.pack.data.IBusinessPackageGetterService;
import com.fr.bi.exception.BIFieldAbsentException;
import com.fr.bi.exception.BIKeyAbsentException;
import com.fr.bi.stable.data.BITableID;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.utils.program.BINonValueUtils;
import com.fr.fs.control.UserControl;
import com.fr.general.ComparatorUtils;

import java.util.List;
import java.util.Set;

/**
 * This class created on 2016/5/26.
 * 参数必须要的ID必须可用
 *
 * @author Connery
 * @since 4.0
 */
public class BusinessTableHelper {
    public static List<BusinessField> getTableFields(BusinessTable table) {
        List<BusinessField> fields = table.getFields();
//        if (fields == null) {
//            fields = new ArrayList<BusinessField>();
//            Iterator<Map.Entry<String, ICubeFieldSource>> it = ((AbstractTableSource) getTableDataSource(table)).getFields().entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry<String, ICubeFieldSource> entry = it.next();
//                ICubeFieldSource fieldSource = entry.getValue();
//                BIFieldID fieldID = new BIFieldID(java.util.UUID.randomUUID().toString());
//                BusinessField field = new BIBusinessField(table, fieldID,
//                        fieldSource.getFieldName(), fieldSource.getClassType(), fieldSource.getFieldSize());
//                fields.add(field);
//            }
//            if (!(table instanceof BIBusinessTableGetter)) {
//                table.setFields(fields);
//            }
//        }
        return fields;
    }

    public static BusinessTable getBusinessTable(BITableID tableID) {
        try {
            return BICubeConfigureCenter.getDataSourceManager().getBusinessTable(tableID);
        } catch (BIKeyAbsentException e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }

    public static CubeTableSource getTableDataSource(BITableID tableID) {
        return getBusinessTable(tableID).getTableSource();
    }

    public static CubeTableSource getTableDataSource(BusinessTable table) {
        BINonValueUtils.checkNull(table);
        BINonValueUtils.checkNull(table.getID());
        if (table.getTableSource() == null) {
            CubeTableSource source = null;
            try {
                source = BICubeConfigureCenter.getDataSourceManager().getTableSource(table);
            } catch (BIKeyAbsentException e) {
                throw BINonValueUtils.beyondControl(e);
            }
            if (!(table instanceof BIBusinessTableGetter)) {
                table.setSource(source);
            }
        }
        return table.getTableSource();
    }

    public static BusinessField getSpecificField(BusinessTable table, String fieldName) throws BIFieldAbsentException {
        BINonValueUtils.checkNull(fieldName);
        List<BusinessField> fields = getTableFields(table);
        for (BusinessField field : fields) {
            if (ComparatorUtils.equals(fieldName, field.getFieldName())) {
                return field;
            }
        }
        String tableId = table.getID().getIdentityValue();
        String tableName = BICubeConfigureCenter.getAliasManager().getAliasName(tableId, UserControl.getInstance().getSuperManagerID());
        throw new BIFieldAbsentException("-- warning from BI -- Please check business package : ' " + getPackageNameByTableId(tableId) + " ' , table : ' " + tableName + " ' (id : " + tableId + ") , the field ' " + fieldName + " ' is missing ! " + " try to click the refresh button !");
    }

    private static String getPackageNameByTableId(String tableId) {
        Set<IBusinessPackageGetterService> allPackages = BICubeConfigureCenter.getPackageManager().getAllPackages(UserControl.getInstance().getSuperManagerID());
        for (IBusinessPackageGetterService pack : allPackages) {
            if (pack.getBusinessTables().contains(new BIBusinessTable(new BITableID(tableId)))) {
                return pack.getName().getName();
            }
        }
        return null;
    }
}
