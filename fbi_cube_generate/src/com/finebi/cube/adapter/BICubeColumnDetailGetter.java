package com.finebi.cube.adapter;

import com.finebi.cube.api.*;
import com.finebi.cube.structure.column.BICubeDoubleColumn;
import com.finebi.cube.structure.column.BICubeLongColumn;
import com.finebi.cube.structure.column.CubeColumnReaderService;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.io.newio.NIOConstant;

/**
 * Created by 小灰灰 on 2016/6/24.
 */
public class BICubeColumnDetailGetter implements ICubeColumnDetailGetter {
    private static final long serialVersionUID = -3389205614212915623L;
    CubeColumnReaderService service;

    public BICubeColumnDetailGetter(CubeColumnReaderService service) {
        this.service = service;
    }

    @Override
    public Object getValue(int row) {
        return service.getOriginalObjectValueByRow(row);
    }

    @Override
    public PrimitiveType getPrimitiveType() {
        return service.getClassType() == DBConstant.CLASS.LONG ? PrimitiveType.LONG : PrimitiveType.DOUBLE;
    }

    @Override
    public PrimitiveDetailGetter createPrimitiveDetailGetter() {
        PrimitiveType type = getPrimitiveType();
        if (type == PrimitiveType.DOUBLE) {
            return new PrimitiveDoubleGetter() {
                private static final long serialVersionUID = 7141828639662850151L;

                @Override
                public double getValue(int row) {
                    double value = ((BICubeDoubleColumn) service).getOriginalValueByRow(row);
                    return Double.isNaN(value) ? 0 : value;
                }
            };
        } else if (type == PrimitiveType.LONG) {
            return new PrimitiveLongGetter() {
                private static final long serialVersionUID = 3658058739271903945L;

                @Override
                public long getValue(int row) {
                    long value = ((BICubeLongColumn) service).getOriginalValueByRow(row);
                    return value == NIOConstant.LONG.NULL_VALUE ? 0 : value;
                }
            };
        }
        return null;
    }
    @Override
    public String getICubeResourceLocationPath(){
        return service.getResourceLocation().getAbsolutePath();
    }

    @Override
    public void clear() {
        service.clear();
    }
}
