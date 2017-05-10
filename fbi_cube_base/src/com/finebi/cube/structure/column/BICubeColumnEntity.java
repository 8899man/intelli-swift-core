package com.finebi.cube.structure.column;

import com.finebi.cube.data.ICubeResourceDiscovery;
import com.finebi.cube.exception.BICubeIndexException;
import com.finebi.cube.exception.BICubeRelationAbsentException;
import com.finebi.cube.exception.BIResourceInvalidException;
import com.finebi.cube.exception.IllegalRelationPathException;
import com.finebi.cube.location.ICubeResourceLocation;
import com.finebi.cube.structure.*;
import com.finebi.cube.structure.group.ICubeGroupDataService;
import com.finebi.cube.structure.property.BICubeColumnPositionOfGroupService;
import com.finebi.cube.structure.property.BICubeVersion;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.gvi.RoaringGroupValueIndex;
import com.fr.bi.stable.utils.program.BITypeUtils;

import java.util.Comparator;

/**
 * This class created on 2016/3/3.
 *
 * @author Connery
 * @since 4.0
 */
public abstract class BICubeColumnEntity<T> implements ICubeColumnEntityService<T> {
    protected ICubeResourceLocation currentLocation;
    protected ICubeDetailDataService<T> detailDataService;
    protected ICubeIndexDataService indexDataService;
    protected ICubeGroupDataService<T> groupDataService;
    protected BICubeVersion cubeVersion;
    protected ICubeColumnPositionOfGroupService cubeColumnPositionOfGroupService;
    protected ICubeRelationManagerService relationManagerService;
    protected ICubeResourceDiscovery discovery;

    public BICubeColumnEntity(ICubeResourceDiscovery discovery, ICubeResourceLocation currentLocation) {
        this.discovery = discovery;
        this.currentLocation = currentLocation;
        indexDataService = new BICubeIndexData(discovery, currentLocation);
        cubeVersion = new BICubeVersion(currentLocation, discovery);
        cubeColumnPositionOfGroupService = new BICubeColumnPositionOfGroupService(currentLocation, discovery);
        initial();
    }

    protected abstract void initial();


    public void buildStructure() {
        detailDataService.buildStructure();
        groupDataService.buildStructure();
        indexDataService.buildStructure();
    }

    @Override
    public void setRelationManagerService(ICubeRelationManagerService relationManagerService) {
        this.relationManagerService = relationManagerService;
    }

    @Override
    public Comparator<T> getGroupComparator() {
        return groupDataService.getGroupComparator();
    }

    public void setGroupComparator(Comparator groupComparator) {
        groupDataService.setGroupComparator(groupComparator);
    }


    @Override
    public void addOriginalDataValue(int rowNumber, T originalValue) {
        detailDataService.addDetailDataValue(rowNumber, originalValue);
    }

    public void increaseAddOriginalDataValue(int rowNumber, T originalValue) {
        detailDataService.increaseAddDetailDataValue(rowNumber, originalValue);
    }

    @Override
    public void addGroupValue(int position, T groupValue) {
        groupDataService.addGroupDataValue(position, groupValue);
    }

    @Override
    public void addGroupIndex(int position, GroupValueIndex index) {
        indexDataService.addIndex(position, index);
    }

    @Override
    public void addPositionOfGroup(int position, Integer groupPosition) {
        cubeColumnPositionOfGroupService.addPositionOfGroup(position, groupPosition);
    }

    @Override
    public int getPositionOfGroupByGroupValue(T groupValues) throws BIResourceInvalidException {

        return groupDataService.getPositionOfGroupValue(convert(groupValues));
    }

    @Override
    public int getPositionOfGroupByRow(int row) throws BIResourceInvalidException {
        return cubeColumnPositionOfGroupService.getPositionOfGroup(row);
    }

    private T convert(Object value) {
        if (value == null) {
            return null;
        }
        if (BITypeUtils.isAssignable(Long.class, value.getClass()) &&
                getClassType() == DBConstant.CLASS.DOUBLE) {
            return convertDouble(value);
        } else if (BITypeUtils.isAssignable(Double.class, value.getClass()) &&
                getClassType() == DBConstant.CLASS.LONG) {
            return convertLong(value);
        }

        return (T) value;
    }

    private T convertLong(Object value) {
        return (T) BITypeUtils.convert2Long((Double) value);
    }

    private T convertDouble(Object value) {
        return (T) BITypeUtils.convert2Double((Long) value);
    }

    @Override
    public int sizeOfGroup() {
        return groupDataService.sizeOfGroup();
    }

    @Override
    public void recordSizeOfGroup(int size) {
        groupDataService.writeSizeOfGroup(size);
    }

    @Override
    public void copyDetailValue(ICubeColumnEntityService columnEntityService, long rowCount) {

    }

    @Override
    public GroupValueIndex getBitmapIndex(int position) throws BICubeIndexException {
        return indexDataService.getBitmapIndex(position);
    }

    @Override
    public GroupValueIndex getNULLIndex(int position) throws BICubeIndexException {
        return indexDataService.getNULLIndex(position);
    }

    @Override
    public GroupValueIndex getIndexByGroupValue(T groupValues) throws BIResourceInvalidException, BICubeIndexException {
        int position = getPositionOfGroupByGroupValue(groupValues);
        if (position >= 0) {
            return getBitmapIndex(position);
        }
        return new RoaringGroupValueIndex();
    }


    @Override
    public void addNULLIndex(int position, GroupValueIndex groupValueIndex) {
        indexDataService.addNULLIndex(position, groupValueIndex);
    }

    public long getCubeVersion() {
        return cubeVersion.getCubeVersion();
    }

    @Override
    public void addVersion(long version) {
        cubeVersion.addVersion(version);
        cubeVersion.forceReleaseWriter();
    }

    @Override
    public T getGroupObjectValue(int position) {
        return groupDataService.getGroupObjectValueByPosition(position);
    }

    @Override
    public CubeRelationEntityGetterService getRelationIndexGetter(BICubeTablePath path) throws BICubeRelationAbsentException, IllegalRelationPathException {
        if (path.isEmptyPath()) {
            /**
             * 如果路径是空的，说明没有关联
             * 那么返回自身的Index。
             */
            return new BICubeRelationEntity(indexDataService);
        }
        return relationManagerService.getRelationService(path);
    }

    @Override
    public boolean existRelationPath(BICubeTablePath path) {
        try {
            return getRelationIndexGetter(path).isDataAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void clear() {
        if (detailDataService != null) {
            detailDataService.clear();
        }
        if (indexDataService != null) {
            indexDataService.clear();
        }
        if (groupDataService != null) {
            groupDataService.clear();
        }
        if (cubeVersion != null) {
            cubeVersion.clear();
        }
        if (cubeColumnPositionOfGroupService != null) {
            cubeColumnPositionOfGroupService.clear();
        }
        if (relationManagerService != null) {
            relationManagerService.clear();
        }
    }

    @Override
    public void forceReleaseWriter() {
        if (detailDataService != null) {
            detailDataService.forceReleaseWriter();
        }
        if (indexDataService != null) {
            indexDataService.forceReleaseWriter();
        }
        if (groupDataService != null) {
            groupDataService.forceReleaseWriter();
        }
        if (cubeVersion != null) {
            cubeVersion.forceReleaseWriter();
        }
        if (cubeColumnPositionOfGroupService != null) {
            cubeColumnPositionOfGroupService.forceReleaseWriter();
        }
        if (relationManagerService != null) {
            relationManagerService.forceReleaseWriter();
        }
    }

    @Override
    public void forceReleaseReader() {

        if (detailDataService != null) {
            detailDataService.forceReleaseReader();
        }
        if (indexDataService != null) {
            indexDataService.forceReleaseReader();
        }
        if (groupDataService != null) {
            groupDataService.forceReleaseReader();
        }
        if (cubeVersion != null) {
            cubeVersion.forceReleaseReader();
        }
        if (cubeColumnPositionOfGroupService != null) {
            cubeColumnPositionOfGroupService.forceReleaseReader();
        }
        if (relationManagerService != null) {
            relationManagerService.forceReleaseReader();
        }
    }

    @Override
    public T getOriginalObjectValueByRow(int rowNumber) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return indexDataService.isEmpty();
    }

    @Override
    public int getClassType() {
        return detailDataService.getClassType();
    }

    @Override
    public ICubeResourceLocation getResourceLocation() {
        return currentLocation.copy();
    }

    @Override
    public void setOwner(ITableKey owner) {
        relationManagerService.setOwner(owner);
    }

    @Override
    public Boolean isVersionAvailable() {
        return cubeVersion.isVersionAvailable();
    }
}
