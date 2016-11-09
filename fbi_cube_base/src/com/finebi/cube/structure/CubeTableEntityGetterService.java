package com.finebi.cube.structure;

import com.finebi.cube.exception.BICubeColumnAbsentException;
import com.finebi.cube.exception.BICubeRelationAbsentException;
import com.finebi.cube.exception.IllegalRelationPathException;
import com.finebi.cube.structure.column.BIColumnKey;
import com.finebi.cube.structure.column.CubeColumnReaderService;
import com.fr.bi.common.inter.Release;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.stable.collections.array.IntArray;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Cube表的只读接口
 * This class created on 2016/3/2.
 *
 * @author Connery
 * @since 4.0
 */
public interface CubeTableEntityGetterService extends Release, ICubeVersion, ICubeResourceForceRelease {


    /**
     * 获得CubeTable对应的数据源表的字段信息
     *
     * @return 按照顺序的字段信息
     */
    List<ICubeFieldSource> getFieldInfo();

    /**
     * Cube中保存的字段信息。
     * 包括子类型的处理
     *
     * @return cube的字段信息。
     */
    Set<BIColumnKey> getCubeColumnInfo();

    int getRowCount();

    IntArray getRemovedList();

    ICubeFieldSource getSpecificColumn(String fieldName) throws BICubeColumnAbsentException;

    Date getLastExecuteTime();

    Date getCurrentExecuteTime();

    /**
     * 获取列的接口
     *
     * @param columnKey 列
     * @return 获取列的接口
     */
    CubeColumnReaderService getColumnDataGetter(BIColumnKey columnKey) throws BICubeColumnAbsentException;

    /**
     * 获取列的接口
     *
     * @param columnName 列名
     * @return 获取列的接口
     */
    CubeColumnReaderService getColumnDataGetter(String columnName) throws BICubeColumnAbsentException;

    /**
     * 获取关联数据的接口
     *
     * @param path 关联的路径
     * @return 获取关联数据的接口
     */
    CubeRelationEntityGetterService getRelationIndexGetter(BICubeTablePath path)
            throws BICubeRelationAbsentException, BICubeColumnAbsentException, IllegalRelationPathException;

    boolean tableDataAvailable();

    boolean relationExists(BICubeTablePath path);

    boolean isRowCountAvailable();

    boolean isLastExecuteTimeAvailable();

    boolean isCurrentExecuteTimeAvailable();
}
