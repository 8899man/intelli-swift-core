package com.fr.swift.api.rpc.impl;

import com.fr.swift.SwiftContext;
import com.fr.swift.annotation.SwiftApi;
import com.fr.swift.api.rpc.TableService;
import com.fr.swift.api.rpc.bean.Column;
import com.fr.swift.basics.annotation.ProxyService;
import com.fr.swift.basics.base.selector.ProxySelector;
import com.fr.swift.config.SwiftConfigConstants;
import com.fr.swift.config.bean.MetaDataColumnBean;
import com.fr.swift.config.bean.SwiftMetaDataBean;
import com.fr.swift.config.bean.SwiftTablePathBean;
import com.fr.swift.config.oper.impl.RestrictionFactoryImpl;
import com.fr.swift.config.service.SwiftCubePathService;
import com.fr.swift.config.service.SwiftMetaDataService;
import com.fr.swift.config.service.SwiftSegmentService;
import com.fr.swift.config.service.SwiftTablePathService;
import com.fr.swift.db.AlterTableAction;
import com.fr.swift.db.SwiftDatabase;
import com.fr.swift.db.Table;
import com.fr.swift.db.impl.AddColumnAction;
import com.fr.swift.db.impl.DropColumnAction;
import com.fr.swift.event.global.TruncateEvent;
import com.fr.swift.exception.meta.SwiftMetaDataAbsentException;
import com.fr.swift.selector.ClusterSelector;
import com.fr.swift.service.listener.RemoteSender;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftMetaDataColumn;
import com.fr.swift.source.core.MD5Utils;
import com.fr.swift.util.Crasher;
import com.fr.swift.util.FileUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author yee
 * @date 2018/8/27
 */
@ProxyService(value = TableService.class, type = ProxyService.ServiceType.EXTERNAL)
@SwiftApi
public class TableServiceImpl implements TableService {
    private SwiftMetaDataService swiftMetaDataService = SwiftContext.get().getBean(SwiftMetaDataService.class);
    private SwiftCubePathService cubePathService = SwiftContext.get().getBean(SwiftCubePathService.class);
    private SwiftTablePathService tablePathService = SwiftContext.get().getBean(SwiftTablePathService.class);
    private SwiftSegmentService segmentService = SwiftContext.get().getBean("segmentServiceProvider", SwiftSegmentService.class);

    @Override
    @SwiftApi
    public SwiftMetaData detectiveMetaData(SwiftDatabase schema, String tableName) throws SwiftMetaDataAbsentException {
        List<SwiftMetaData> metaDataList = swiftMetaDataService.find(RestrictionFactoryImpl.INSTANCE.eq(SwiftConfigConstants.MetaDataConfig.COLUMN_TABLE_NAME, tableName),
                RestrictionFactoryImpl.INSTANCE.eq(SwiftConfigConstants.MetaDataConfig.COLUMN_SWIFT_SCHEMA, schema));
        if (metaDataList.isEmpty()) {
            throw new SwiftMetaDataAbsentException(tableName);
        }
        return metaDataList.get(0);
    }

    @Override
    @SwiftApi
    public List<String> detectiveAllTableNames(SwiftDatabase schema) {
        List<SwiftMetaData> metaDataList = swiftMetaDataService.find(RestrictionFactoryImpl.INSTANCE.eq(SwiftConfigConstants.MetaDataConfig.COLUMN_SWIFT_SCHEMA, schema));
        if (metaDataList.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<String> tableNames = new ArrayList<String>();
            try {
                for (SwiftMetaData metaData : metaDataList) {
                    tableNames.add(metaData.getTableName());
                }
            } catch (Exception ignore) {
            }
            return tableNames;
        }
    }

    @Override
    @SwiftApi
    public boolean isTableExists(SwiftDatabase schema, String tableName) {
        try {
            return null != detectiveMetaData(schema, tableName);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @SwiftApi
    synchronized
    public int createTable(SwiftDatabase schema, String tableName, List<Column> columns) throws SQLException {
        if (isTableExists(schema, tableName)) {
            Crasher.crash("Table " + tableName + " is already exists");
        }
        if (columns.isEmpty()) {
            Crasher.crash("Table " + tableName + " must contain at lease one column.");
        }
        SwiftMetaDataBean swiftMetaDataBean = new SwiftMetaDataBean();
        swiftMetaDataBean.setSwiftDatabase(schema);
        swiftMetaDataBean.setTableName(tableName);
        String uniqueKey = MD5Utils.getMD5String(new String[]{UUID.randomUUID().toString(), tableName});
        swiftMetaDataBean.setId(uniqueKey);
        List<SwiftMetaDataColumn> columnList = new ArrayList<SwiftMetaDataColumn>();
        for (Column column : columns) {
            String columnName = column.getColumnName();
            if (SwiftConfigConstants.KeyWords.COLUMN_KEY_WORDS.contains(columnName.toLowerCase())) {
                throw new SQLException(String.format("%s is a key word! ", columnName));
            }
            columnList.add(new MetaDataColumnBean(column.getColumnName(), column.getColumnType()));
        }
        swiftMetaDataBean.setFields(columnList);
        if (swiftMetaDataService.addMetaData(uniqueKey, swiftMetaDataBean)) {
            return 1;
        }
        return -1;
    }

    @Override
    @SwiftApi
    public void dropTable(SwiftDatabase schema, String tableName) throws Exception {
        SwiftMetaData metaData = detectiveMetaData(schema, tableName);
        truncateTable(metaData);
        swiftMetaDataService.removeMetaDatas(new SourceKey(metaData.getId()));
    }

    @Override
    @SwiftApi
    public void truncateTable(SwiftDatabase schema, String tableName) throws Exception {
        SwiftMetaData metaData = detectiveMetaData(schema, tableName);
        truncateTable(metaData);
    }

    private void truncateTable(SwiftMetaData metaData) {
        String sourceKey = metaData.getId();
        if (ClusterSelector.getInstance().getFactory().isCluster()) {
            ProxySelector.getInstance().getFactory().getProxy(RemoteSender.class).trigger(new TruncateEvent(sourceKey));
        } else {
            SwiftTablePathBean entity = tablePathService.get(sourceKey);
            int path = 0;
            if (null != entity) {
                path = entity.getTablePath() == null ? 0 : entity.getTablePath();
                tablePathService.removePath(sourceKey);
            }
            segmentService.removeSegments(sourceKey);
            String localPath = String.format("%s/%d/%s", cubePathService.getSwiftPath(), path, sourceKey);
            FileUtil.delete(localPath);
        }
    }

    @Override
    @SwiftApi(enable = false)
    public boolean addColumn(SwiftDatabase schema, String tableName, Column column) throws SQLException {
        SwiftMetaData metaData = detectiveMetaData(schema, tableName);
        if (metaData.getFieldNames().contains(column.getColumnName())) {
            throw new SQLException("Column " + column.getColumnName() + " is already exists!");
        }
        Table table = com.fr.swift.db.impl.SwiftDatabase.getInstance().getTable(new SourceKey(metaData.getId()));
        SwiftMetaDataColumn metaDataColumn = new MetaDataColumnBean(column.getColumnName(), column.getColumnType());
        AlterTableAction action = new AddColumnAction(metaDataColumn);
        return alterTable(table, action);
    }

    @Override
    @SwiftApi(enable = false)
    public boolean dropColumn(SwiftDatabase schema, String tableName, String columnName) throws SQLException {
        SwiftMetaData metaData = detectiveMetaData(schema, tableName);
        SwiftMetaDataColumn metaDataColumn = metaData.getColumn(columnName);
        Table table = com.fr.swift.db.impl.SwiftDatabase.getInstance().getTable(new SourceKey(metaData.getId()));
        AlterTableAction action = new DropColumnAction(metaDataColumn);
        return alterTable(table, action);
    }

    private boolean alterTable(Table table, AlterTableAction action) throws SQLException {
        action.alter(table);
        return true;
    }
}
