package com.finebi.cube.gen.arrange;

import com.finebi.cube.common.log.BILogger;
import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.BICubeConfiguration;
import com.finebi.cube.conf.CubeGenerationManager;
import com.finebi.cube.data.disk.BICubeDiskDiscovery;
import com.finebi.cube.exception.BIRegisterIsForbiddenException;
import com.finebi.cube.exception.BITopicAbsentException;
import com.finebi.cube.gen.mes.*;
import com.finebi.cube.gen.oper.*;
import com.finebi.cube.gen.oper.watcher.BICubeBuildFinishWatcher;
import com.finebi.cube.gen.oper.watcher.BIDataSourceBuildFinishWatcher;
import com.finebi.cube.gen.oper.watcher.BIPathBuildFinishWatcher;
import com.finebi.cube.gen.oper.watcher.BITableSourceBuildWatcher;
import com.finebi.cube.impl.operate.BIOperation;
import com.finebi.cube.location.BICubeResourceRetrieval;
import com.finebi.cube.location.ICubeResourceRetrievalService;
import com.finebi.cube.relation.BICubeGenerateRelation;
import com.finebi.cube.relation.BICubeGenerateRelationPath;
import com.finebi.cube.relation.BITableSourceRelation;
import com.finebi.cube.relation.BITableSourceRelationPath;
import com.finebi.cube.router.status.IStatusTag;
import com.finebi.cube.router.topic.ITopicTag;
import com.finebi.cube.structure.BICube;
import com.finebi.cube.structure.BITableKey;
import com.finebi.cube.structure.Cube;
import com.finebi.cube.structure.CubeTableEntityService;
import com.finebi.cube.structure.column.BIColumnKey;
import com.finebi.cube.utils.BICubePathUtils;
import com.finebi.cube.utils.BICubeRelationUtils;
import com.finebi.cube.utils.BITableKeyUtils;
import com.fr.bi.conf.data.source.ExcelTableSource;
import com.fr.bi.conf.manager.update.source.UpdateSettingSource;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.engine.CubeTask;
import com.fr.bi.stable.engine.CubeTaskType;
import com.fr.bi.stable.utils.program.BINonValueUtils;
import com.fr.fs.control.UserControl;

import java.util.*;

/**
 * This class created on 2016/4/12.
 *
 * @author Connery
 * @since 4.0
 */
public class BICubeOperationManager {
    private static BILogger logger = BILoggerFactory.getLogger(BICubeOperationManager.class);
    private Cube cube;
    private Cube integrityCube;
    private BIOperation<Object> cubeBuildFinishOperation;
    private BIOperation<Object> pathBuildFinishWatcher;
    private BIDataSourceBuildFinishWatcher dataSourceBuildFinishWatcher;
    private Set<CubeTableSource> registeredTransportTable;
    private Set<CubeTableSource> registeredFieldIndex;

    private Set<CubeTableSource> originalTableSet;
    private Map<CubeTableSource, BIOperation> tableSourceWatchers;
    private Map<CubeTableSource, Long> versionMap;
    private Map<CubeTableSource, UpdateSettingSource> updateSettingSourceMap;
//    private Map<CubeTableSource, com.fr.data.impl.Connection> connectionMap;

    public BICubeOperationManager(Cube cube, Cube integrityCube, Set<CubeTableSource> originalTableSet) {
        this.cube = cube;
        this.integrityCube = integrityCube;
        registeredTransportTable = new HashSet<CubeTableSource>();
        registeredFieldIndex = new HashSet<CubeTableSource>();
        this.originalTableSet = originalTableSet;
        tableSourceWatchers = new HashMap<CubeTableSource, BIOperation>();
    }

    public void setVersionMap(Map<CubeTableSource, Long> versionMap) {
        this.versionMap = versionMap;
    }

    public void setUpdateSettingSourceMap(Map<CubeTableSource, UpdateSettingSource> updateSettingSourceMap) {
        this.updateSettingSourceMap = updateSettingSourceMap;
    }

//    public void setConnectionMap(Map<CubeTableSource, Connection> connectionMap) {
//        this.connectionMap = connectionMap;
//    }

    public void initialWatcher() {
        cubeBuildFinishOperation = generateCubeFinishOperation();
        pathBuildFinishWatcher = generatePathFinishOperation();
        dataSourceBuildFinishWatcher = getDataSourceBuildFinishWatcher();
    }

    public void subscribeStartMessage() {
        try {
            cubeBuildFinishOperation.subscribe(BICubeBuildTopicTag.START_BUILD_CUBE);
        } catch (BITopicAbsentException e) {
            e.printStackTrace();
        } catch (BIRegisterIsForbiddenException e) {
            e.printStackTrace();
        }
    }


    public void generateDataSource(Set<List<Set<CubeTableSource>>> tableSourceSet) {
        if (null != tableSourceSet && !tableSourceSet.isEmpty()) {
            registeredTransportTable.clear();
            registeredFieldIndex.clear();
            tableSourceWatchers.clear();
            generateTransportBuilder(tableSourceSet);
            generateFieldIndexBuilder(tableSourceSet);
            generateDataSourceFinishBuilder(tableSourceSet);
            subscribeDataSourceFinish();
        }
    }

    private boolean isGenerated(CubeTableSource tableSource) {
        return registeredTransportTable.contains(tableSource);

    }

    private boolean isFieldIndexGenerated(CubeTableSource tableSource) {
        return registeredFieldIndex.contains(tableSource);

    }

    private void addGeneratedTable(CubeTableSource tableSource) {
        registeredTransportTable.add(tableSource);
    }

    private void addGeneratedFieldIndex(CubeTableSource tableSource) {
        registeredFieldIndex.add(tableSource);
    }

    /**
     * TODO 重构ITableSource结构，这种序列关系封装一下都行
     *
     * @param tableSourceSet list是指父类带有序列的集合。list内部的Set是指当前序列可能有多个
     *                       TableSource构成。最外层的Set是指多个List。
     */
    private void generateTransportBuilder(Set<List<Set<CubeTableSource>>> tableSourceSet) {
        Iterator<List<Set<CubeTableSource>>> it = tableSourceSet.iterator();
        while (it.hasNext()) {
            List<Set<CubeTableSource>> tableSourceList = it.next();
            generateSingleTransport(tableSourceList);
        }
    }

    /**
     * 设置一个表队列
     *
     * @param tableSourceSet 一条有先后关系的表层级。
     *                       例如：
     *                       a
     *                       b,c
     *                       d
     *                       e
     *                       由5张表组成的层级关系。b,c设成前，a表必须生成好。
     *                       d表生成前，bc表需要生成好。
     */
    private void generateSingleTransport(List<Set<CubeTableSource>> tableSourceSet) {
        Iterator<Set<CubeTableSource>> it = tableSourceSet.iterator();
        Set<CubeTableSource> parentTables = null;
        while (it.hasNext()) {
            Set<CubeTableSource> sameLevelTable = it.next();
            Iterator<CubeTableSource> sameLevelTableIt = sameLevelTable.iterator();
            /**
             * 遍历当期层级的表
             */
            while (sameLevelTableIt.hasNext()) {
                /**
                 * 当期层级的表
                 */
                CubeTableSource tableSource = sameLevelTableIt.next();
                if (!isGenerated(tableSource)) {
//                    BIOperation<Object> operation = new BIOperation<Object>(
//                            tableSource.getSourceID(),
//                            getDataTransportBuilder(cube, addConnection(tableSource), originalTableSet, parentTables, getVersion(tableSource), getUpdateSetting(tableSource)));
                    BIOperation<Object> operation = new BIOperation<Object>(
                            tableSource.getSourceID(),
                            getDataTransportBuilder(cube, tableSource, originalTableSet, parentTables, getVersion(tableSource), getUpdateSetting(tableSource)));

                    operation.setOperationTopicTag(BICubeBuildTopicTag.DATA_TRANSPORT_TOPIC);
                    operation.setOperationFragmentTag(BIFragmentUtils.generateFragment(BICubeBuildTopicTag.DATA_TRANSPORT_TOPIC, tableSource));
                    try {
                        if (parentTables == null) {
                            operation.subscribe(BICubeBuildTopicTag.START_BUILD_CUBE);
                        } else {
                            Iterator<CubeTableSource> parentTablesIt = parentTables.iterator();
                            while (parentTablesIt.hasNext()) {
                                CubeTableSource parentTable = parentTablesIt.next();
                                ITopicTag topicTag = BICubeBuildTopicTag.DATA_SOURCE_TOPIC;
                                operation.subscribe(BIStatusUtils.generateStatusFinish(topicTag
                                        , parentTable.getSourceID()));
                            }
                        }

                        watchTable(tableSource, BIStatusUtils.generateStatusFinish(BICubeBuildTopicTag.DATA_TRANSPORT_TOPIC
                                , tableSource.getSourceID()));
                        addGeneratedTable(tableSource);
                    } catch (Exception e) {
                        throw BINonValueUtils.beyondControl(e.getMessage(), e);
                    }
                }
            }
            if (parentTables == null) {
                parentTables = new HashSet<CubeTableSource>();
            }
            parentTables.addAll(sameLevelTable);
        }
    }


    private BIOperation buildTableWatcher(CubeTableSource tableSource) {
        BIOperation<Object> operation = new BIOperation<Object>(
                tableSource.getSourceID(),
                getTableWatcherBuilder(cube.getCubeTableWriter(new BITableKey(tableSource))));
        ITopicTag topicTag = BICubeBuildTopicTag.DATA_SOURCE_TOPIC;
        operation.setOperationTopicTag(topicTag);
        operation.setOperationFragmentTag(BIFragmentUtils.generateFragment(topicTag, tableSource));
        return operation;
    }

    private BIOperation getTableSourceWatcher(CubeTableSource tableSource) {
        if (tableSourceWatchers.containsKey(tableSource)) {
            return tableSourceWatchers.get(tableSource);
        } else {
            BIOperation operation = buildTableWatcher(tableSource);
            tableSourceWatchers.put(tableSource, operation);
            return operation;
        }
    }


    private void generateFieldIndexBuilder(Set<List<Set<CubeTableSource>>> tableSourceSet) {
        Iterator<List<Set<CubeTableSource>>> it = tableSourceSet.iterator();
        while (it.hasNext()) {
            generateSingleFieldIndex(it.next());
        }
    }

    private void generateSingleFieldIndex(List<Set<CubeTableSource>> tableSourceSet) {
        Iterator<Set<CubeTableSource>> it = tableSourceSet.iterator();
        while (it.hasNext()) {
            Set<CubeTableSource> sameLevelTable = it.next();
            Iterator<CubeTableSource> sameLevelTableIt = sameLevelTable.iterator();
            while (sameLevelTableIt.hasNext()) {
                CubeTableSource tableSource = sameLevelTableIt.next();
                if (!isFieldIndexGenerated(tableSource)) {
                    ICubeFieldSource[] fields = tableSource.getFieldsArray(originalTableSet);
                    for (int i = 0; i < fields.length; i++) {
                        ICubeFieldSource field = fields[i];
                        Iterator<BIColumnKey> columnKeyIterator = BIColumnKey.generateColumnKey(field).iterator();
                        while (columnKeyIterator.hasNext()) {
                            BIColumnKey targetColumnKey = columnKeyIterator.next();
                            BIOperation<Object> operation = new BIOperation<Object>(
                                    tableSource.getSourceID() + "_" + targetColumnKey.getFullName(),
                                    getFieldIndexBuilder(cube, tableSource, field, targetColumnKey));
                            ITopicTag topicTag = BITopicUtils.generateTopicTag(tableSource);
                            operation.setOperationTopicTag(topicTag);
                            operation.setOperationFragmentTag(BIFragmentUtils.generateFragment(topicTag, targetColumnKey.getFullName()));
                            try {
                                operation.subscribe(BIStatusUtils.generateStatusFinish(BICubeBuildTopicTag.DATA_TRANSPORT_TOPIC, tableSource.getSourceID()));
                            } catch (Exception e) {
                                throw BINonValueUtils.beyondControl(e.getMessage(), e);
                            }
                            watchTable(tableSource, BIStatusUtils.generateStatusFinish(topicTag, targetColumnKey.getFullName()));
                            addGeneratedFieldIndex(tableSource);
                        }
                    }
                }
            }
        }
    }

    private void watchTable(CubeTableSource tableSource, IStatusTag tag) {
        BIOperation tableWatcher = getTableSourceWatcher(tableSource);
        try {
            tableWatcher.subscribe(tag);
        } catch (Exception e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }

    private void generateDataSourceFinishBuilder(Set<List<Set<CubeTableSource>>> tableSourceSet) {
        Iterator<List<Set<CubeTableSource>>> it = tableSourceSet.iterator();
        BIOperation<Object> operation = new BIOperation<Object>(
                BICubeBuildTopicTag.DATA_SOURCE_FINISH_TOPIC.getTopicName(),
                dataSourceBuildFinishWatcher);
        operation.setOperationTopicTag(BICubeBuildTopicTag.DATA_SOURCE_FINISH_TOPIC);
        operation.setOperationFragmentTag(BICubeBuildFragmentTag.getCubeOccupiedFragment(BICubeBuildTopicTag.DATA_SOURCE_FINISH_TOPIC));
        while (it.hasNext()) {
            generateSingleSourceFinish(it.next(), operation);
        }
    }

    private void generateSingleSourceFinish(List<Set<CubeTableSource>> tableSourceList, BIOperation<Object> operation) {
        Iterator<Set<CubeTableSource>> it = tableSourceList.iterator();
        while (it.hasNext()) {
            Iterator<CubeTableSource> sameLevelTable = it.next().iterator();
            while (sameLevelTable.hasNext()) {
                CubeTableSource tableSource = sameLevelTable.next();
                try {
                    IStatusTag tag = BIStatusUtils.generateStatusFinish(BICubeBuildTopicTag.DATA_SOURCE_TOPIC,
                            tableSource.getSourceID());
                    if (!operation.isSubscribed(tag)) {
                        operation.subscribe(tag);
                    }
                } catch (Exception e) {
                    throw BINonValueUtils.beyondControl(e);
                }
            }
        }
    }

    private void subscribeDataSourceFinish() {
        try {
            IStatusTag statusTag = BIStatusUtils.generateStatusFinish(BICubeBuildTopicTag.DATA_SOURCE_FINISH_TOPIC,
                    BICubeBuildFragmentTag.getCubeOccupiedFragment(BICubeBuildTopicTag.DATA_SOURCE_FINISH_TOPIC).getFragmentID().getIdentityValue());
            if (!cubeBuildFinishOperation.isSubscribed(statusTag)) {
                cubeBuildFinishOperation.subscribe(statusTag);
            }
        } catch (Exception e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }

    private void subscribePathFinish() {
        try {
            IStatusTag statusTag = BIStatusUtils.generateStatusFinish(BICubeBuildTopicTag.PATH_FINISH_TOPIC,
                    BICubeBuildFragmentTag.getCubeOccupiedFragment(BICubeBuildTopicTag.PATH_FINISH_TOPIC).getFragmentID().getIdentityValue());
            if (!cubeBuildFinishOperation.isSubscribed(statusTag)) {
                cubeBuildFinishOperation.subscribe(statusTag);
            }
        } catch (Exception e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }


    private BIOperation<Object> generateCubeFinishOperation() {
        BIOperation<Object> operation = new BIOperation<Object>(
                BICubeBuildTopicTag.FINISH_BUILD_CUBE.getTopicName(),
                getCubeBuildFinishWatcher());
        operation.setOperationTopicTag(BICubeBuildTopicTag.FINISH_BUILD_CUBE);
        operation.setOperationFragmentTag(BICubeBuildFragmentTag.getCubeOccupiedFragment(BICubeBuildTopicTag.FINISH_BUILD_CUBE));
        return operation;
    }

    private BIOperation<Object> generatePathFinishOperation() {
        BIOperation<Object> operation = new BIOperation<Object>(
                BICubeBuildTopicTag.PATH_FINISH_TOPIC.getTopicName(),
                getPathBuildFinishWatcher());
        operation.setOperationTopicTag(BICubeBuildTopicTag.PATH_FINISH_TOPIC);
        operation.setOperationFragmentTag(BICubeBuildFragmentTag.getCubeOccupiedFragment(BICubeBuildTopicTag.PATH_FINISH_TOPIC));

        return operation;
    }

    /*
    * 同时支持完整依赖和部分依赖
    * */
    public void generateRelationBuilder(Set<BICubeGenerateRelation> relationSet) {
        if (relationSet != null && !relationSet.isEmpty()) {
            Iterator<BICubeGenerateRelation> it = relationSet.iterator();
            while (it.hasNext()) {
                BICubeGenerateRelation relation = it.next();
                try {
                    String sourceID = BIRelationIDUtils.calculateRelationID(relation.getRelation());
                    BIOperation<Object> operation = new BIOperation<Object>(
                            sourceID,
                            getRelationBuilder(cube, integrityCube, relation.getRelation()));
                    operation.setOperationTopicTag(BICubeBuildTopicTag.PATH_TOPIC);
                    operation.setOperationFragmentTag(BIFragmentUtils.generateFragment(BICubeBuildTopicTag.PATH_TOPIC, sourceID));
                    if (null != relation.getDependTableSourceSet() && relation.getDependTableSourceSet().size() != 0) {
                        for (CubeTableSource cubeTableSource : relation.getDependTableSourceSet()) {
                            operation.subscribe(BIStatusUtils.generateStatusFinish(BICubeBuildTopicTag.DATA_SOURCE_TOPIC, cubeTableSource.getSourceID()));
                        }
                    } else {
                        logger.warn("The relation:" + sourceID + " subscribe start message!!!");
                        operation.subscribe(BICubeBuildTopicTag.START_BUILD_CUBE);
                    }
                    pathFinishSubscribe(BIStatusUtils.generateStatusFinish(BICubeBuildTopicTag.PATH_TOPIC, sourceID));
                } catch (Exception e) {
                    try {
                        BILoggerFactory.getLogger().info("the relation info listed");
                        BILoggerFactory.getLogger().info(relation.getRelation().toString());
                        BILoggerFactory.getLogger().info("the tables this relation depends listed");
                        for (CubeTableSource source : relation.getDependTableSourceSet()) {
                            BILoggerFactory.getLogger().info(source.getTableName() + " " + source.getSourceID());
                        }
                    } catch (Exception e1) {
                        BILoggerFactory.getLogger().error(e1.getMessage(), e1);
                    }
                    throw BINonValueUtils.beyondControl(e.getMessage(), e);
                }
            }
            subscribePathFinish();
        }
    }

    private void pathFinishSubscribe(IStatusTag partFinish) {
        try {
            pathBuildFinishWatcher.subscribe(partFinish);
        } catch (Exception e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }

    /*
    * 同时支持完整依赖和部分依赖
    * */
    public void generateTableRelationPath(Set<BICubeGenerateRelationPath> relationPathSet) {
        for (BICubeGenerateRelationPath path : relationPathSet) {
            try {
                String sourceID = BIRelationIDUtils.calculatePathID(path.getBiTableSourceRelationPath());
                BIOperation<Object> operation = new BIOperation<Object>(
                        sourceID,
                        getTablePathBuilder(cube, integrityCube, path.getBiTableSourceRelationPath()));
                operation.setOperationTopicTag(BICubeBuildTopicTag.PATH_TOPIC);
                operation.setOperationFragmentTag(BIFragmentUtils.generateFragment(BICubeBuildTopicTag.PATH_TOPIC, sourceID));
                if (path.getDependRelationPathSet().size() != 0) {
                    for (BITableSourceRelationPath biTableSourceRelationPath : path.getDependRelationPathSet()) {
                        operation.subscribe(BIStatusUtils.generateStatusFinish(BICubeBuildTopicTag.PATH_TOPIC, BIRelationIDUtils.calculatePathID(biTableSourceRelationPath)));
                    }
                    pathFinishSubscribe(BIStatusUtils.generateStatusFinish(BICubeBuildTopicTag.PATH_TOPIC, sourceID));
                } else {
                    operation.subscribe(BICubeBuildTopicTag.START_BUILD_CUBE);
                }
            } catch (Exception e) {
                BILoggerFactory.getLogger().error("the child path this path contained listed");
                for (BITableSourceRelationPath sourceRelationPath : path.getDependRelationPathSet()) {
                    BILoggerFactory.getLogger().error(sourceRelationPath.getSourceID());
                    for (BITableSourceRelation relation : sourceRelationPath.getAllRelations()) {
                        BILoggerFactory.getLogger().error("primaryTable:" + relation.getPrimaryTable().getTableName() + " to foreignTable:" + relation.getForeignTable().getTableName());
                    }
                }
                throw BINonValueUtils.beyondControl(e.getMessage(), e);
            }
            subscribePathFinish();
        }
    }

    long getVersion(CubeTableSource tableSource) {
        if (versionMap != null && versionMap.containsKey(tableSource)) {
            return versionMap.get(tableSource);
        } else {
            return -1;
        }
    }

    public UpdateSettingSource getUpdateSetting(CubeTableSource tableSource) {
        if (updateSettingSourceMap != null && updateSettingSourceMap.containsKey(tableSource)) {
            return updateSettingSourceMap.get(tableSource);
        } else {
            return null;
        }
    }

//    private com.fr.data.impl.Connection getConnection(CubeTableSource tableSource) {
//        if (connectionMap != null && connectionMap.containsKey(tableSource)) {
//            return connectionMap.get(tableSource);
//        } else {
//            return null;
//        }
//    }

    /*为tableSource指定connection*/
//    private CubeTableSource addConnection(CubeTableSource tableSource) {
//        Connection connection = getConnection(tableSource);
//        if (null != connection && (tableSource.getType() == BIBaseConstant.TABLETYPE.SQL || tableSource.getType() == BIBaseConstant.TABLETYPE.DB)) {
//            for (CubeTableSource source : connectionMap.keySet()) {
//                if (source.getSourceID().equals(tableSource.getSourceID())) {
//                    return source;
//                }
//            }
//        }
//        return tableSource;
//    }


    protected BIRelationIndexGenerator getRelationBuilder(Cube cube, Cube integrityCube, BITableSourceRelation relation) {
        return new BIRelationIndexGenerator(cube, integrityCube, BICubeRelationUtils.convert(relation));
    }

    protected BIFieldIndexGenerator getFieldIndexBuilder(Cube cube, CubeTableSource tableSource, ICubeFieldSource BICubeFieldSource, BIColumnKey targetColumnKey) {
        return new BIFieldIndexGenerator(cube, tableSource, BICubeFieldSource, targetColumnKey);
    }

    protected BITableSourceBuildWatcher getTableWatcherBuilder(CubeTableEntityService tableEntityService) {
        return new BITableSourceBuildWatcher(tableEntityService);
    }

    protected BISourceDataTransport getDataTransportBuilder(Cube cube, CubeTableSource tableSource, Set<CubeTableSource> allSources, Set<CubeTableSource> parent, long version, UpdateSettingSource tableUpdateSetting) {
        CubeTask currentTask = CubeGenerationManager.getCubeManager().getGeneratingTask(UserControl.getInstance().getSuperManagerID());
/*若没有更新设置,按默认处理
* 首次更新均为全局更新*/
        if (null == tableUpdateSetting || !(BITableKeyUtils.isTableExisted(tableSource, BICubeConfiguration.getConf(String.valueOf(UserControl.getInstance().getSuperManagerID()))))) {
            return new BISourceDataAllTransport(cube, tableSource, allSources, parent, version);
        }
        /*若设置为不随全局更新的话，那就不更新*/
        else if (currentTask.getTaskType() == CubeTaskType.ALL && tableUpdateSetting.getTogetherOrNever() == DBConstant.SINGLE_TABLE_UPDATE.NEVER) {
            return new BISourceDataNeverTransport(cube, tableSource, allSources, parent, version);
        } else {
            switch (tableUpdateSetting.getUpdateType()) {
                case DBConstant.SINGLE_TABLE_UPDATE_TYPE.ALL: {
                    return new BISourceDataAllTransport(cube, tableSource, allSources, parent, version);
                }
                /*增量更新现在暂时只适用于SQL语句，其他数据集是不能用的*/
                case DBConstant.SINGLE_TABLE_UPDATE_TYPE.PART: {
                    BICubeDiskDiscovery discovery = BICubeDiskDiscovery.getInstance();
                    ICubeResourceRetrievalService resourceRetrievalService = new BICubeResourceRetrieval(BICubeConfiguration.getTempConf(String.valueOf(UserControl.getInstance().getSuperManagerID())));
                    cube = new BICube(resourceRetrievalService, discovery);
                    if (tableSource instanceof ExcelTableSource) {
                        return new BISourceDataAllTransport(cube, tableSource, allSources, parent, version);
                    } else {
                        return new BISourceDataPartTransport(cube, tableSource, allSources, parent, version, tableUpdateSetting);
                    }
                }
                case DBConstant.SINGLE_TABLE_UPDATE_TYPE.NEVER: {
                    return new BISourceDataNeverTransport(cube, tableSource, allSources, parent, version);
                }
                default:
                    return new BISourceDataAllTransport(cube, tableSource, allSources, parent, version);
            }
        }
    }

    protected BITablePathIndexBuilder getTablePathBuilder(Cube cube, Cube integrityCube, BITableSourceRelationPath tablePath) {
        return new BITablePathIndexBuilder(cube, integrityCube, BICubePathUtils.convert(tablePath));
    }

    protected BIFieldPathIndexBuilder getFieldPathBuilder(Cube cube, ICubeFieldSource field, BITableSourceRelationPath tablePath) {
        return new BIFieldPathIndexBuilder(cube, field, BICubePathUtils.convert(tablePath));
    }

    protected BICubeBuildFinishWatcher getCubeBuildFinishWatcher() {
        return new BICubeBuildFinishWatcher();
    }

    protected BIPathBuildFinishWatcher getPathBuildFinishWatcher() {
        return new BIPathBuildFinishWatcher();
    }

    protected BIDataSourceBuildFinishWatcher getDataSourceBuildFinishWatcher() {
        return new BIDataSourceBuildFinishWatcher();
    }
}
