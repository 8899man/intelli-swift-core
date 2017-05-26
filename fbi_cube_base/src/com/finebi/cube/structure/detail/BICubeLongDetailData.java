package com.finebi.cube.structure.detail;

import com.finebi.cube.data.ICubeResourceDiscovery;
import com.finebi.cube.data.input.ICubeLongReaderWrapper;
import com.finebi.cube.exception.BIResourceInvalidException;
import com.finebi.cube.location.ICubeResourceLocation;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.io.newio.NIOConstant;
import com.finebi.cube.common.log.BILoggerFactory;

/**
 * This class created on 2016/3/28.
 *
 * @author Connery
 * @since 4.0
 */
public class BICubeLongDetailData extends BICubeDetailData<Long> {
    public BICubeLongDetailData(ICubeResourceDiscovery discovery, ICubeResourceLocation superLocation) {
        super(discovery, superLocation);
    }

    public long getOriginalValueByRow(int rowNumber) {
        try {
            return ((ICubeLongReaderWrapper)getCubeReader()).getSpecificValue(rowNumber);
        } catch (BIResourceInvalidException e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            return NIOConstant.LONG.NULL_VALUE;
        }
    }

    @Override
    protected ICubeResourceLocation setDetailType(boolean isIncrease) {
        return currentLocation.setLongTypeWrapper();
    }

    @Override
    public Long getOriginalObjectValueByRow(int rowNumber) {
        return  getOriginalValueByRow(rowNumber);
    }

    @Override
    public int getClassType() {
        return DBConstant.CLASS.LONG;
    }

    /**
     * 获取空值表示
     *
     * @return
     */
    @Override
    public Long getCubeNullValue() {
        return NIOConstant.LONG.NULL_VALUE;
    }
}
