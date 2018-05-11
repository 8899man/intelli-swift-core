package com.finebi.conf.impl;

import com.finebi.base.constant.FineEngineType;
import com.finebi.conf.internalimp.bean.table.UpdatePreviewTableBean;
import com.finebi.conf.internalimp.bean.update.UpdatePreview;
import com.finebi.conf.internalimp.response.update.TableUpdateSetting;
import com.finebi.conf.internalimp.update.GlobalUpdateInfo;
import com.finebi.conf.internalimp.update.GlobalUpdateLog;
import com.finebi.conf.internalimp.update.GlobalUpdateSetting;
import com.finebi.conf.internalimp.update.TableUpdateInfo;
import com.finebi.conf.internalimp.update.UpdateLog;
import com.finebi.conf.internalimp.update.UpdateNeedSpace;
import com.finebi.conf.internalimp.update.UpdateStatus;
import com.finebi.conf.provider.SwiftRelationPathConfProvider;
import com.finebi.conf.provider.SwiftTableManager;
import com.finebi.conf.service.engine.update.EngineUpdateManager;
import com.finebi.conf.structure.analysis.table.FineAnalysisTable;
import com.finebi.conf.structure.bean.table.FineBusinessTable;
import com.fr.swift.adaptor.space.SwiftSpaceManager;
import com.fr.swift.adaptor.struct.ShowResultSet;
import com.fr.swift.adaptor.transformer.DataSourceFactory;
import com.fr.swift.adaptor.transformer.RelationSourceFactory;
import com.fr.swift.cube.io.ResourceDiscovery;
import com.fr.swift.cube.queue.CubeTasks;
import com.fr.swift.cube.task.Task;
import com.fr.swift.cube.task.TaskKey;
import com.fr.swift.cube.task.impl.LocalTaskPool;
import com.fr.swift.cube.task.impl.SchedulerTaskPool;
import com.fr.swift.generate.preview.SwiftDataPreviewer;
import com.fr.swift.increment.Increment;
import com.fr.swift.log.SwiftLogger;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.manager.ProviderManager;
import com.fr.swift.provider.IndexStuffInfoProvider;
import com.fr.swift.reliance.RelationPathReliance;
import com.fr.swift.reliance.RelationReliance;
import com.fr.swift.reliance.SourceReliance;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.RelationSource;
import com.fr.swift.source.Row;
import com.fr.swift.source.SourcePath;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftResultSet;
import com.fr.swift.source.SwiftSourceTransfer;
import com.fr.swift.source.container.SourceContainerManager;
import com.fr.swift.source.db.QueryDBSource;
import com.fr.swift.source.manager.IndexStuffProvider;
import com.fr.swift.utils.RelationRelianceFactory;
import com.fr.swift.utils.SourceRelianceFactory;
import com.fr.swift.utils.TableUpdateLogUtil;
import com.fr.swift.utils.UpdateSpaceInfoUtil;
import com.fr.third.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class created on 2018-1-12 14:17:13
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI Analysis 1.0
 */
public class SwiftUpdateManager implements EngineUpdateManager {

    private static final SwiftLogger LOGGER = SwiftLoggers.getLogger(SwiftUpdateManager.class);

    @Autowired
    private SwiftTableManager tableManager;
    @Autowired
    private SwiftRelationPathConfProvider relationPathConfProvider;
    @Autowired
    private SwiftSpaceManager spaceManager;

    @Override
    public Map<FineBusinessTable, TableUpdateInfo> getTableUpdateInfo() {
        return null;
    }

    @Override
    public List<TableUpdateInfo> getTableUpdateInfo(FineBusinessTable table) {
        TableUpdateInfo tableUpdateInfo = new TableUpdateInfo();
        tableUpdateInfo.setTableName(table.getName());
        List<TableUpdateInfo> tableUpdateInfoList = new ArrayList<TableUpdateInfo>();
        tableUpdateInfoList.add(tableUpdateInfo);
        return tableUpdateInfoList;
    }

    @Override
    public TableUpdateInfo getPackageUpdateInfo(String packageId) {
        return new TableUpdateInfo();
    }

    @Override
    public void saveUpdateSetting(TableUpdateInfo updateInfo, FineBusinessTable table) throws Exception {
        Map<FineBusinessTable, TableUpdateInfo> infoMap = new HashMap<FineBusinessTable, TableUpdateInfo>();
        infoMap.put(table, updateInfo);
        saveUpdateSetting(infoMap);
    }

    @Override
    public void triggerTableUpdate(TableUpdateInfo updateInfo, FineBusinessTable table) {

    }

    @Override
    public void saveUpdateSetting(Map<FineBusinessTable, TableUpdateInfo> infoMap) throws Exception {

        SourceContainerManager updateSourceContainer = new SourceContainerManager();
        Map<String, List<Increment>> incrementMap = new HashMap<String, List<Increment>>();

        DataSourceFactory.transformDataSources(infoMap, updateSourceContainer, incrementMap);

        List<DataSource> baseDataSourceList = new ArrayList<DataSource>(updateSourceContainer.getDataSourceContainer().getAllSources());

        List<DataSource> allDataSourceList = DataSourceFactory.transformDataSources(tableManager.getAllTable());

        SourceReliance sourceReliance = SourceRelianceFactory.generateSourceReliance(baseDataSourceList, allDataSourceList, incrementMap);

        List<RelationSource> relationSources = RelationSourceFactory.transformRelationSources(relationPathConfProvider.getAllRelations());

        List<SourcePath> sourcePaths = RelationSourceFactory.transformSourcePaths(relationPathConfProvider.getAllRelationPaths());

        // FIXME 传表的责任链，只更新和表有关的关联，单表更新可能无法更新到关联
        RelationReliance relationReliance = RelationRelianceFactory.generateRelationReliance(relationSources, sourceReliance);

        RelationPathReliance relationPathReliance = RelationRelianceFactory.generateRelationPathReliance(sourcePaths, relationReliance);

        IndexStuffProvider indexStuffProvider = new IndexStuffInfoProvider(updateSourceContainer, incrementMap, sourceReliance, relationReliance, relationPathReliance);

        ProviderManager.getManager().registProvider(0, indexStuffProvider);
    }

    @Override
    public void saveTableUpdateSetting(TableUpdateSetting tableUpdateSetting) throws Exception {
        FineBusinessTable fineBusinessTable = new SwiftTableManager().getSingleTable(tableUpdateSetting.getTableName());
        Map<FineBusinessTable, TableUpdateInfo> infoMap = new HashMap<FineBusinessTable, TableUpdateInfo>();
        infoMap.put(fineBusinessTable, tableUpdateSetting.getSettings().get(tableUpdateSetting.getTableName()));
        this.saveUpdateSetting(infoMap);
    }

    @Override
    public void savePackageUpdateSetting(String packId, TableUpdateInfo info) {
        try {
            List<FineBusinessTable> tables = tableManager.getAllTableByPackId(packId);
            Map<FineBusinessTable, TableUpdateInfo> infoMap = new HashMap<FineBusinessTable, TableUpdateInfo>();
            for (FineBusinessTable table : tables) {
                infoMap.put(table, info);
            }
            saveUpdateSetting(infoMap);
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    @Override
    public void triggerPackageUpdate(String packId) {
    }

    @Override
    public Map<String, UpdateStatus> getTableUpdateStatus(FineBusinessTable table) {
        return null;
    }

    @Override
    public UpdateStatus getTableUpdateState(String tableName) {
        return null;
    }

    @Override
    public UpdateStatus getPackUpdateStatus(String packId) {
        return new UpdateStatus();
    }

    @Override
    public List<UpdateLog> getTableUpdateLog(FineBusinessTable table) {
        try {
            DataSource dataSource = DataSourceFactory.getDataSource(table);
            List<UpdateLog> updateLogs = new ArrayList<UpdateLog>();
            int round = CubeTasks.getCurrentRound();
            for (int i = 1; i <= round; i++) {
                TaskKey taskKey = CubeTasks.newBuildTableTaskKey(dataSource, i);

                Task task = SchedulerTaskPool.getInstance().get(taskKey);
                if (task != null) {
                    UpdateLog updateLog = new UpdateLog();
                    updateLog.setName(table.getName());
                    updateLog.setEndTime(task.getEndTime());
                    updateLogs.add(updateLog);
                }
            }
            return updateLogs;
        } catch (Exception e) {
            LOGGER.error(e);
            return null;
        }
    }


    @Override
    public void triggerAllUpdate(TableUpdateInfo info) {
        Map<FineBusinessTable, TableUpdateInfo> infoMap = new HashMap<FineBusinessTable, TableUpdateInfo>();
        for (FineBusinessTable fineBusinessTable : tableManager.getAllTable()) {
            if (!(fineBusinessTable instanceof FineAnalysisTable)) {
                infoMap.put(fineBusinessTable, new TableUpdateInfo());
            }
        }
        if (!infoMap.isEmpty()) {
            try {
                saveUpdateSetting(infoMap);
            } catch (Exception e) {
                LOGGER.error(e);
            }
        } else {
            LOGGER.info("No table need to be updated!");
        }
    }

    @Override
    public GlobalUpdateSetting getUpdateInfo() {
        return null;
    }

    @Override
    public void updateAll(GlobalUpdateSetting info) {
    }

    @Override
    public GlobalUpdateInfo checkGlobalUpdateInfo() {
        GlobalUpdateInfo globalUpdateInfo = new GlobalUpdateInfo();
        globalUpdateInfo.setHaskTask(TableUpdateLogUtil.hasTask());
        return globalUpdateInfo;
    }

    //todo 临时处理，最好再改下task的结构和逻辑
    //todo 现在拿的都是metadata的表和字段，没有去拿业务表，更没有去拿业务包
    @Override
    public GlobalUpdateLog getGlobalUpdateLog() {
        GlobalUpdateLog globalUpdateLog = new GlobalUpdateLog();
        boolean hasTask = TableUpdateLogUtil.hasTask();
        globalUpdateLog.setHasTask(hasTask);
        Set<Integer> rounds = new HashSet<Integer>();
        if (!hasTask) {
            int round = CubeTasks.getCurrentRound();
            rounds.add(round);
        } else {
            rounds.addAll(TableUpdateLogUtil.getRunningRounds());
        }
        Collection<TaskKey> allTaskKey = LocalTaskPool.getInstance().allTasks();

        Map<TaskKey, Task> transportTaskMap = new HashMap<TaskKey, Task>();
        Map<TaskKey, Task> indexTaskMap = new HashMap<TaskKey, Task>();
        Map<TaskKey, Task> mergeTaskMap = new HashMap<TaskKey, Task>();

        for (TaskKey taskKey : allTaskKey) {
            if (rounds.contains(taskKey.getRound())) {
                Task task = LocalTaskPool.getInstance().get(taskKey);
                switch (taskKey.operation()) {
                    case TRANSPORT_TABLE:
                        transportTaskMap.put(taskKey, task);
                        break;
                    case INDEX_COLUMN:
                        indexTaskMap.put(taskKey, task);
                        break;
                    case MERGE_COLUMN_DICT:
                        mergeTaskMap.put(taskKey, task);
                        break;
                    default:
                }
            }
        }
        globalUpdateLog.setTransportInfo(TableUpdateLogUtil.getTranSportInfo(transportTaskMap));
        globalUpdateLog.setIndexInfo(TableUpdateLogUtil.getIndexInfo(indexTaskMap, mergeTaskMap));

        globalUpdateLog.setProcess(TableUpdateLogUtil.getProcess(globalUpdateLog));

        try {
            globalUpdateLog.setSpace(UpdateSpaceInfoUtil.getUpdateSpaceInfo(spaceManager));
        } catch (Exception e) {
            LOGGER.error("Calculate space failed!", e);
        }

        return globalUpdateLog;
    }


    @Override
    public UpdateNeedSpace getUpdateNeedSpace() {
        return null;
    }

    @Override
    public boolean shouldUpdate() {
        return false;
    }

    @Override
    public FineEngineType getEngineType() {
        return FineEngineType.Cube;
    }

    @Override
    public UpdatePreview getUpdatePreview(UpdatePreviewTableBean updatePreviewTableBean) {
        try {
            DataSource queryDBSource = new QueryDBSource(updatePreviewTableBean.getSql(), updatePreviewTableBean.getConnectionName());
            SwiftSourceTransfer transfer = SwiftDataPreviewer.createPreviewTransfer(queryDBSource, 100);
            SwiftResultSet resultSet = ShowResultSet.of(transfer.createResultSet());
            Object[] data = new Object[100];
            Object[] fieldNames = new Object[queryDBSource.getMetadata().getColumnCount()];
            for (int i = 1; i <= queryDBSource.getMetadata().getColumnCount(); i++) {
                fieldNames[i - 1] = queryDBSource.getMetadata().getColumnName(i);
            }
            int count = 0;
            while (resultSet.next()) {
                Row row = resultSet.getRowData();
                List<Object> rowList = new ArrayList<Object>();
                SwiftMetaData meta = queryDBSource.getMetadata();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    rowList.add(row.getValue(i - 1));
                }
                data[count] = rowList;
                count++;
            }

            UpdatePreview updatePreview = new UpdatePreview(updatePreviewTableBean.getSql(), data, fieldNames);
            return updatePreview;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new UpdatePreview();
        }
    }

    @Override
    public String getUpdatePath() {
        return ResourceDiscovery.getInstance().getCubePath();
    }

    @Override
    public void updatePath(String newPath) {
        if (ResourceDiscovery.getInstance().checkCubePath(newPath)) {
            ResourceDiscovery.getInstance().setCubePath(newPath);
        }
//        FineBusinessTableRelationPath path = relationPathConfProvider.getPath(newPath);
//        List<RelationSource> relationSources = new ArrayList<RelationSource>();
//        if (null != path) {
//            try {
//                List<FineBusinessTableRelation> relations = path.getFineBusinessTableRelations();
//                List<DataSource> dataSources = new ArrayList<DataSource>();
//                for (FineBusinessTableRelation relation : relations) {
//                    if (relation.getRelationType() == BICommonConstants.RELATION_TYPE.MANY_TO_ONE) {
//                        dataSources.add(DataSourceFactory.transformDataSource(relation.getPrimaryBusinessTable()));
//                    } else {
//                        dataSources.add(DataSourceFactory.transformDataSource(relation.getForeignBusinessTable()));
//                    }
//                    relationSources.add(RelationSourceFactory.transformRelationSourcesFromRelation(relation));
//                }
//                FineBusinessTableRelation relation = relations.get(0);
//                if (relation.getRelationType() == BICommonConstants.RELATION_TYPE.MANY_TO_ONE) {
//                    dataSources.add(DataSourceFactory.transformDataSource(relation.getForeignBusinessTable()));
//                } else {
//                    dataSources.add(DataSourceFactory.transformDataSource(relation.getPrimaryBusinessTable()));
//                }
//                RelationReliance relationReliance = new RelationReliance(relationSources, dataSources);
//                RelationNodeUtils.calculateRelationNode(relationReliance);
//                RelationPathReliance relationPathReliance = new RelationPathReliance(RelationSourceFactory.transformSourcePaths(Arrays.asList(path)), relationReliance);
//                RelationNodeUtils.calculateRelationPathNode(relationPathReliance);
//                // fixme 调更新
//            } catch (Exception e) {
//                LOGGER.error(e.getMessage(), e);
//            }
//
//        }
    }
}