package com.finebi.cube.relation;

import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.data.db.BICubeFieldSource;
import com.fr.json.JSONObject;

/**
 * This class created on 2016/3/28.
 * 位图索引通过主外键的关系，而建立了基于行号的关联。
 * 该对象值的就是某张表的行号字段。
 *
 * @author Connery
 * @since 4.0
 */
public final class BIRowField extends BICubeFieldSource {
    private static final long serialVersionUID = 6114314337052821566L;
    public static BIRowField rowNumberField = new BIRowField();

    private BIRowField() {
        super(null, "row_number_field", DBConstant.CLASS.ROW, 2);
    }


    @Override
    public void setFieldName(String fieldName) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new UnsupportedOperationException();

    }


    @Override
    public void parseJSON(JSONObject jo) throws Exception {
        throw new UnsupportedOperationException();

    }

    @Override
    public JSONObject createJSON() throws Exception {
        throw new UnsupportedOperationException();

    }


}
