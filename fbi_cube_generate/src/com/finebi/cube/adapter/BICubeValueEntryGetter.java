package com.finebi.cube.adapter;

import com.finebi.cube.api.ICubeValueEntryGetter;
import com.finebi.cube.exception.BICubeIndexException;
import com.finebi.cube.exception.BIResourceInvalidException;
import com.finebi.cube.relation.BITableSourceRelation;
import com.finebi.cube.structure.CubeRelationEntityGetterService;
import com.finebi.cube.structure.ICubeIndexDataGetterService;
import com.finebi.cube.structure.column.CubeColumnReaderService;
import com.finebi.cube.utils.BICubePathUtils;
import com.fr.bi.stable.gvi.GVIFactory;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.io.newio.NIOConstant;
import com.fr.bi.stable.structure.object.CubeValueEntry;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.stable.utils.program.BINonValueUtils;

import java.util.List;

/**
 * Created by 小灰灰 on 2016/7/5.
 */
public class BICubeValueEntryGetter<T> implements ICubeValueEntryGetter {
    private CubeColumnReaderService<T> columnReaderService;
    private ICubeIndexDataGetterService indexDataGetterService;
    private CubeRelationEntityGetterService reverseRowGetter;

    public BICubeValueEntryGetter(CubeColumnReaderService<T> columnReaderService, List<BITableSourceRelation> relationList, CubeRelationEntityGetterService reverseRowGetter) {
        this.columnReaderService = columnReaderService;
        this.reverseRowGetter = reverseRowGetter;
        if (isRelationIndex(relationList)) {
            try {
                indexDataGetterService = columnReaderService.getRelationIndexGetter(BICubePathUtils.convert(relationList));
            } catch (Exception e) {
                throw BINonValueUtils.beyondControl(e);
            }
        } else {
            indexDataGetterService = columnReaderService;
        }
    }

    private int getReverseRow(int row){
        try {
            return reverseRowGetter == null ? row : reverseRowGetter.getReverseIndex(row);
        } catch (BIResourceInvalidException e) {
            BINonValueUtils.beyondControl(e);
        }
        return NIOConstant.INTEGER.NULL_VALUE;
    }

    private boolean isRelationIndex(List<BITableSourceRelation> relationList) {
        return relationList != null && !relationList.isEmpty();
    }

    @Override
    public GroupValueIndex getIndexByRow(int row) {
        try {
            int groupRow  = getPositionOfGroupByRow(row);
            return getIndexByGroupRow(groupRow);
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
        return GVIFactory.createAllEmptyIndexGVI();
    }

    public GroupValueIndex getIndexByGroupRow(int groupRow) {
        try {
            return groupRow == NIOConstant.INTEGER.NULL_VALUE ? indexDataGetterService.getNULLIndex(0) : indexDataGetterService.getBitmapIndex(groupRow);
        } catch (BICubeIndexException e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            return null;
        }
    }

    public T getGroupValue(int groupRow) {
        return  groupRow == NIOConstant.INTEGER.NULL_VALUE ? null : columnReaderService.getGroupObjectValue(groupRow);
    }

    @Override
    public CubeValueEntry getEntryByRow(int row) {
        int groupRow  = NIOConstant.INTEGER.NULL_VALUE;
        GroupValueIndex gvi = null;
        T value = null;
        try {
            groupRow  = getPositionOfGroupByRow(row);
            gvi = getIndexByGroupRow(groupRow);
            value = getGroupValue(groupRow);
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
        return new CubeValueEntry(value, gvi, groupRow);
    }

    @Override
    public int getPositionOfGroupByRow(int row) {
        int groupRow = NIOConstant.INTEGER.NULL_VALUE;
        try {
            int reverseRow = getReverseRow(row);
            groupRow = reverseRow == NIOConstant.INTEGER.NULL_VALUE ? NIOConstant.INTEGER.NULL_VALUE : columnReaderService.getPositionOfGroupByRow(reverseRow);
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
        return groupRow;
    }

    @Override
    public int getGroupSize() {
        return columnReaderService.sizeOfGroup();
    }
}
