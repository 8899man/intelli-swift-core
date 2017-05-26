package com.finebi.cube.structure.column.date;

import com.finebi.cube.data.ICubeResourceDiscovery;
import com.finebi.cube.exception.BICubeIndexException;
import com.finebi.cube.exception.BICubeRelationAbsentException;
import com.finebi.cube.exception.BIResourceInvalidException;
import com.finebi.cube.exception.IllegalRelationPathException;
import com.finebi.cube.location.ICubeResourceLocation;
import com.finebi.cube.structure.BICubeTablePath;
import com.finebi.cube.structure.CubeRelationEntityGetterService;
import com.finebi.cube.structure.ICubeRelationManagerService;
import com.finebi.cube.structure.ITableKey;
import com.finebi.cube.structure.column.BICubeColumnEntity;
import com.finebi.cube.structure.column.ICubeColumnEntityService;
import com.fr.bi.stable.gvi.GroupValueIndex;

import java.util.Calendar;
import java.util.Comparator;

/**
 * 日期子类。
 * 详细数据是保存在HostColumn中的，
 * 这里selfColumn主要存储自身的索引。
 * This class created on 2016/4/7.
 *
 * @author Connery
 * @since 4.0
 */
public abstract class BICubeDateSubColumn<T> implements ICubeColumnEntityService<T> {
    protected BICubeDateColumn hostDataColumn;
    protected BICubeColumnEntity<T> selfColumnEntity;
    protected ICubeResourceDiscovery discovery;

    public BICubeDateSubColumn(ICubeResourceDiscovery discovery, ICubeResourceLocation currentLocation, BICubeDateColumn hostDataColumn) {
        this.discovery = discovery;
        initialColumnEntity(currentLocation);
        this.hostDataColumn = hostDataColumn;
    }

    protected abstract void initialColumnEntity(ICubeResourceLocation currentLocation);

    @Override
    public void addOriginalDataValue(int rowNumber, T originalValue) {
        throw new UnsupportedOperationException();
    }

    public void increaseAddOriginalDataValue(int rowNumber, T originalValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRelationManagerService(ICubeRelationManagerService relationManagerService) {
        selfColumnEntity.setRelationManagerService(relationManagerService);
    }

    @Override
    public Comparator<T> getGroupComparator() {
        return selfColumnEntity.getGroupComparator();
    }


    @Override
    public void addGroupValue(int position, T groupValue) {
        selfColumnEntity.addGroupValue(position, groupValue);
    }

    @Override
    public void addGroupIndex(int position, GroupValueIndex index) {
        selfColumnEntity.addGroupIndex(position, index);
    }

    @Override
    public void addPositionOfGroup(int position, Integer groupPosition) {
        selfColumnEntity.addPositionOfGroup(position, groupPosition);
    }

    @Override
    public void recordSizeOfGroup(int size) {
        selfColumnEntity.recordSizeOfGroup(size);
    }

    public long getCubeVersion() {
        return selfColumnEntity.getCubeVersion();
    }

    @Override
    public void addVersion(long version) {
        selfColumnEntity.addVersion(version);
    }

    @Override
    public void addNULLIndex(int position, GroupValueIndex groupValueIndex) {
        selfColumnEntity.addNULLIndex(position, groupValueIndex);
    }

    @Override
    public void copyDetailValue(ICubeColumnEntityService columnEntityService, long rowCount) {
        selfColumnEntity.copyDetailValue(columnEntityService, rowCount);
    }

    @Override
    public int getPositionOfGroupByGroupValue(T groupValues) throws BIResourceInvalidException {
        return selfColumnEntity.getPositionOfGroupByGroupValue(groupValues);
    }

    @Override
    public int getPositionOfGroupByRow(int row) throws BIResourceInvalidException {
        return selfColumnEntity.getPositionOfGroupByRow(row);
    }

    @Override
    public int sizeOfGroup() {
        return selfColumnEntity.sizeOfGroup();
    }

    @Override
    public T getOriginalObjectValueByRow(int rowNumber) {
        return (T) getOriginalValueByRow(rowNumber);
    }


    @Override
    public CubeRelationEntityGetterService getRelationIndexGetter(BICubeTablePath path) throws BICubeRelationAbsentException, IllegalRelationPathException {
        return selfColumnEntity.getRelationIndexGetter(path);
    }

    @Override
    public GroupValueIndex getBitmapIndex(int position) throws BICubeIndexException {
        return selfColumnEntity.getBitmapIndex(position);
    }

    @Override
    public GroupValueIndex getNULLIndex(int position) throws BICubeIndexException {
        return selfColumnEntity.getNULLIndex(position);
    }

    @Override
    public void clear() {
        selfColumnEntity.clear();
        hostDataColumn.clear();
    }

    @Override
    public void forceReleaseWriter() {
        selfColumnEntity.forceReleaseWriter();
    }

    @Override
    public void forceReleaseReader() {
        selfColumnEntity.forceReleaseReader();
    }

    /**
     * 根据行号获得对应的原始值。
     *
     * @param rowNumber 数据库中的行号
     * @return 原始值
     */
    public Number getOriginalValueByRow(int rowNumber) {
        long value = hostDataColumn.getOriginalValueByRow(rowNumber);
        // 这里之所以不返回原始值是为了其子类好处理。真正的子类如果是空值会真正进行返回相应的空值对应的最小值。
        return convertDate(value);
    }

    /**
     * 根据行号获得对应的原始值。
     *
     * @param rowNumber 数据库中的行号
     * @param calendar 传一个calendar过来取，避免重复构造calendar
     * @return 原始值
     */
    public Number getOriginalValueByRow(int rowNumber, Calendar calendar) {
        long value = hostDataColumn.getOriginalValueByRow(rowNumber);
        // 这里之所以不返回原始值是为了其子类好处理。真正的子类如果是空值会真正进行返回相应的空值对应的最小值。
        return convertDate(value == NIOConstant.LONG.NULL_VALUE ? null : value, calendar);
    }

    protected abstract Number convertDate(Long date);

    protected abstract Number convertDate(Long date, Calendar calendar);

    @Override
    public GroupValueIndex getIndexByGroupValue(T groupValues) throws BIResourceInvalidException, BICubeIndexException {
        return selfColumnEntity.getIndexByGroupValue(groupValues);
    }

    @Override
    public T getGroupObjectValue(int position) {
        return selfColumnEntity.getGroupObjectValue(position);
    }

    @Override
    public void setOwner(ITableKey owner) {
        selfColumnEntity.setOwner(owner);
    }

    @Override
    public boolean existRelationPath(BICubeTablePath path) {
        return selfColumnEntity.existRelationPath(path);
    }

    @Override
    public boolean isEmpty() {
        return selfColumnEntity.isEmpty();
    }

    @Override
    public int getClassType() {
        return selfColumnEntity.getClassType();
    }

    @Override
    public ICubeResourceLocation getResourceLocation() {
        return selfColumnEntity.getResourceLocation();
    }

    @Override
    public Boolean isVersionAvailable() {
        return selfColumnEntity.isVersionAvailable();
    }

    @Override
    public void buildStructure() {
        selfColumnEntity.buildStructure();
    }
}
