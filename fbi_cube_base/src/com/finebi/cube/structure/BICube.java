package com.finebi.cube.structure;

import com.finebi.cube.common.log.BILogger;
import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.data.ICubeResourceDiscovery;
import com.finebi.cube.exception.*;
import com.finebi.cube.location.ICubeResourceLocation;
import com.finebi.cube.location.ICubeResourceRetrievalService;
import com.finebi.cube.structure.column.BIColumnKey;
import com.finebi.cube.structure.column.CubeColumnReaderService;
import com.finebi.cube.structure.property.BICubeVersion;
import com.finebi.cube.structure.table.BICubeTableEntity;
import com.finebi.cube.structure.table.CompoundCubeTableReader;
import com.fr.bi.stable.constant.CubeConstant;
import com.fr.bi.stable.exception.BITablePathConfusionException;
import com.fr.bi.stable.exception.BITablePathEmptyException;
import com.fr.bi.stable.utils.program.BINonValueUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class created on 2016/3/14.
 *
 * @author Connery
 * @since 4.0
 */
public class BICube implements Cube {
    private static BILogger logger = BILoggerFactory.getLogger(BICube.class);
    private static final long serialVersionUID = -5241804642657280524L;
    private ICubeResourceRetrievalService resourceRetrievalService;
    private ICubeResourceDiscovery discovery;
    private BICubeVersion cubeVersion;
    private static String CUBE_PROPERTY = CubeConstant.CUBE_PROPERTY;
    private Map<String, CompoundCubeTableReader> cacheTableReader;

    public BICube(ICubeResourceRetrievalService resourceRetrievalService, ICubeResourceDiscovery discovery) {
        this.resourceRetrievalService = resourceRetrievalService;
        this.discovery = discovery;
        cubeVersion = new BICubeVersion(getCubeLocation(), discovery);
        cacheTableReader = new ConcurrentHashMap<String, CompoundCubeTableReader>();
    }

    private ICubeResourceLocation getCubeLocation() {
        try {
            return this.resourceRetrievalService.retrieveRootResource(CUBE_PROPERTY);
        } catch (BICubeResourceAbsentException e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }

    @Override
    public CubeTableEntityGetterService getCubeTable(ITableKey tableKey) {
//        synchronized (this) {
//            if (cacheTableReader.containsKey(tableKey.getSourceID())) {
//                return cacheTableReader.get(tableKey.getSourceID());
//            } else {
//                BILoggerFactory.getLogger().info("add table reader:" + tableKey.getSourceID());
//                cacheTableReader.put(tableKey.getSourceID(), new CompoundCubeTableReader(tableKey, resourceRetrievalService, discovery));
//                return cacheTableReader.get(tableKey.getSourceID());
//            }
//        }
        return new CompoundCubeTableReader(tableKey, resourceRetrievalService, discovery);
    }

    @Override
    public CubeTableEntityService getCubeTableWriter(ITableKey tableKey) {
        return new BICubeTableEntity(tableKey, resourceRetrievalService, discovery);
    }

    @Override
    public CubeColumnReaderService getCubeColumn(ITableKey tableKey, BIColumnKey field) throws BICubeColumnAbsentException {
        return getCubeTable(tableKey).getColumnDataGetter(field);
    }

    @Override
    public CubeRelationEntityGetterService getCubeRelation(ITableKey tableKey, BICubeTablePath relationPath) throws BICubeRelationAbsentException, BICubeColumnAbsentException, IllegalRelationPathException {
        return getCubeTable(tableKey).getRelationIndexGetter(relationPath);
    }

    @Override
    public CubeRelationEntityGetterService getCubeRelation(ITableKey tableKey, BICubeRelation relation) throws BICubeRelationAbsentException, BICubeColumnAbsentException, IllegalRelationPathException {
        BICubeTablePath relationPath = new BICubeTablePath();
        try {
            relationPath.addRelationAtHead(relation);
        } catch (BITablePathConfusionException e) {
            throw BINonValueUtils.illegalArgument(relation.toString() + " the relation is so terrible");
        }
        return getCubeRelation(tableKey, relationPath);
    }

    @Override
    public ICubeRelationEntityService getCubeRelationWriter(ITableKey tableKey, BICubeRelation relation) throws BICubeRelationAbsentException, BICubeColumnAbsentException, IllegalRelationPathException {
        BICubeTablePath relationPath = new BICubeTablePath();
        try {
            relationPath.addRelationAtHead(relation);
        } catch (BITablePathConfusionException e) {
            throw BINonValueUtils.illegalArgument(relation.toString() + " the relation is so terrible");
        }
        return (ICubeRelationEntityService) getCubeTableWriter(tableKey).getRelationIndexGetter(relationPath);
    }

    @Override
    public ICubeRelationEntityService getCubeRelationWriter(ITableKey tableKey, BICubeTablePath relationPath) throws BICubeRelationAbsentException, BICubeColumnAbsentException, IllegalRelationPathException {
        return (ICubeRelationEntityService) getCubeTableWriter(tableKey).getRelationIndexGetter(relationPath);
    }

    @Override
    public boolean exist(ITableKey tableKey) {
        try {
            ICubeResourceLocation location = resourceRetrievalService.retrieveResource(tableKey);
            if (isResourceExist(location)) {
                CubeTableEntityGetterService tableEntityGetterService = getCubeTable(tableKey);
                boolean result = tableEntityGetterService.tableDataAvailable();
                tableEntityGetterService.clear();
                return result;
            }
            return false;

        } catch (BICubeResourceAbsentException e) {
            e.printStackTrace();
            return false;
        } catch (BICubeTableAbsentException e) {
            return false;
        }
    }

    @Override
    public boolean exist(ITableKey tableKey, BICubeRelation relation) {
        BICubeTablePath relationPath = new BICubeTablePath();
        try {
            relationPath.addRelationAtHead(relation);
        } catch (BITablePathConfusionException e) {
            throw BINonValueUtils.illegalArgument(relation.toString() + " the relation is so terrible");
        }
        return exist(tableKey, relationPath);
    }

    @Override
    public boolean exist(ITableKey tableKey, BICubeTablePath relationPath) {
        if (exist(tableKey)) {
            try {
                ICubeResourceLocation location = resourceRetrievalService.retrieveResource(tableKey, relationPath);
                if (isResourceExist(location)) {
                    CubeTableEntityGetterService tableEntityGetterService = getCubeTable(tableKey);
                    boolean result = tableEntityGetterService.relationExists(relationPath);
                    tableEntityGetterService.clear();
                    return result;
                }
                return false;
            } catch (BICubeResourceAbsentException e) {
                logger.warn(e.getMessage(), e);
                return false;
            } catch (BITablePathEmptyException e) {
                logger.warn(e.getMessage(), e);
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean exist(ITableKey tableKey, BIColumnKey field, BICubeTablePath relationPath) {
        CubeColumnReaderService columnReaderService = null;
        CubeRelationEntityGetterService basicTableRelation = null;
        CubeRelationEntityGetterService fieldRelation = null;
        try {
            columnReaderService = getCubeColumn(tableKey, field);
            basicTableRelation = getCubeRelation(tableKey, relationPath);

            if (exist(tableKey, relationPath)) {
                /**
                 * 如果基础关联存在，那么需要判断版本。字段版本，必须晚于基础关联版本
                 */
                long basicRelationVersion = basicTableRelation.getCubeVersion();
                if (columnReaderService.existRelationPath(relationPath)) {
                    fieldRelation = columnReaderService.getRelationIndexGetter(relationPath);
                    long fieldRelationVersion = fieldRelation.getCubeVersion();
                    if (basicRelationVersion > fieldRelationVersion) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                /**
                 * 如果基础关联不存在，那么就依据字段关联自身。
                 */
                return columnReaderService.existRelationPath(relationPath);
            }
        } catch (Exception e) {
            throw BINonValueUtils.beyondControl(e);
        } finally {
            if (columnReaderService != null) {
                columnReaderService.clear();
            }
            if (basicTableRelation != null) {
                basicTableRelation.clear();
            }
            if (fieldRelation != null) {
                fieldRelation.clear();
            }
        }
    }

    private boolean isResourceExist(ICubeResourceLocation location) {
        return discovery.isResourceExist(location);
    }

    public long getCubeVersion() {
        return cubeVersion.getCubeVersion();
    }

    @Override
    public void addVersion(long version) {
        cubeVersion.addVersion(version);
        cubeVersion.forceRelease();
    }

    @Override
    public void clear() {
        cubeVersion.clear();
        cacheTableReader.clear();
    }

    @Override
    public Boolean isVersionAvailable() {
        return cubeVersion.isVersionAvailable();
    }
}
