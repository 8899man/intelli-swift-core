package com.finebi.cube.gen.oper;

import com.finebi.cube.conf.BICubeConfigureCenter;
import com.finebi.cube.conf.pack.data.IBusinessPackageGetterService;
import com.finebi.cube.conf.table.BIBusinessTable;
import com.finebi.cube.impl.pubsub.BIProcessor;
import com.finebi.cube.message.IMessage;
import com.finebi.cube.relation.BITableSourceRelation;
import com.finebi.cube.structure.*;
import com.finebi.cube.structure.column.BIColumnKey;
import com.finebi.cube.structure.column.ICubeColumnEntityService;
import com.fr.bi.conf.log.BILogManager;
import com.fr.bi.conf.provider.BILogManagerProvider;
import com.fr.bi.conf.report.widget.RelationColumnKey;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.gvi.GVIFactory;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.gvi.traversal.SingleRowTraversalAction;
import com.fr.bi.stable.utils.code.BILogger;
import com.fr.bi.stable.utils.program.BINonValueUtils;
import com.fr.fs.control.UserControl;
import com.fr.general.ComparatorUtils;
import com.fr.stable.bridge.StableFactory;

import java.util.*;

/**
 * This class created on 2016/4/7.
 *
 * @author Connery
 * @since 4.0
 */
public class BIRelationIndexGenerator extends BIProcessor {
    protected Cube cube;
    protected BICubeRelation relation;

    public BIRelationIndexGenerator(Cube cube, BICubeRelation relation) {
        this.cube = cube;
        this.relation = relation;
    }

    @Override
    public Object mainTask(IMessage lastReceiveMessage) {
        BILogManager biLogManager = StableFactory.getMarkedObject(BILogManagerProvider.XML_TAG, BILogManager.class);
        biLogManager.logRelationStart(UserControl.getInstance().getSuperManagerID());
        long t = System.currentTimeMillis();
        RelationColumnKey relationColumnKeyInfo = null;
        try {
            relationColumnKeyInfo = getRelationColumnKeyInfo();
        } catch (Exception e) {
            BILogger.getLogger().error("get relationColumnKey failed! relation information used as listed:" + relation.getPrimaryTable().getSourceID() + "." + relation.getPrimaryField().getColumnName() + " to " + relation.getForeignTable().getSourceID() + "." + relation.getForeignField().getColumnName());
            BILogger.getLogger().error(e.getMessage());
        }
        try {
            buildRelationIndex();
            long costTime = System.currentTimeMillis() - t;
            biLogManager.infoRelation(relationColumnKeyInfo, costTime, UserControl.getInstance().getSuperManagerID());
            return null;
        } catch (Exception e) {
            try {
                biLogManager.errorRelation(relationColumnKeyInfo, e.getMessage(), UserControl.getInstance().getSuperManagerID());
            } catch (Exception e1) {
                BILogger.getLogger().error(e1.getMessage(), e1);
            }
            BILogger.getLogger().error(e.getMessage(), e);
            throw BINonValueUtils.beyondControl(e.getMessage(), e);
        }
    }

    public RelationColumnKey getRelationColumnKeyInfo() {
        BITableSourceRelation tableRelation = getTableRelation(this.relation);
        ICubeFieldSource field = tableRelation.getPrimaryField();
        List<BITableSourceRelation> relations = new ArrayList<BITableSourceRelation>();
        relations.add(tableRelation);
        return new RelationColumnKey(field, relations);
    }

    private BITableSourceRelation getTableRelation(BICubeRelation relation) {
        ICubeFieldSource primaryField = null;
        ICubeFieldSource foreignField = null;
        CubeTableSource primaryTable = null;
        CubeTableSource foreignTable = null;
        Set<CubeTableSource> allTableSource = getAllTableSource();
        for (CubeTableSource cubeTableSource : allTableSource) {
            if (ComparatorUtils.equals(relation.getPrimaryTable().getSourceID(), cubeTableSource.getSourceID())) {
                primaryTable = cubeTableSource;
                Set<CubeTableSource> primarySources = new HashSet<CubeTableSource>();
                primarySources.add(cubeTableSource);
                for (ICubeFieldSource iCubeFieldSource : primaryTable.getFacetFields(primarySources)) {
                    if (ComparatorUtils.equals(iCubeFieldSource.getFieldName(), relation.getPrimaryField().getColumnName())) {
                        primaryField = iCubeFieldSource;
                    }
                }
                break;
            }
        }
        for (CubeTableSource cubeTableSource : allTableSource) {
            if (ComparatorUtils.equals(relation.getForeignTable().getSourceID(), cubeTableSource.getSourceID())) {
                foreignTable = cubeTableSource;
                Set<CubeTableSource> foreignSource = new HashSet<CubeTableSource>();
                foreignSource.add(cubeTableSource);
                for (ICubeFieldSource iCubeFieldSource : foreignTable.getFacetFields(foreignSource)) {
                    if (ComparatorUtils.equals(iCubeFieldSource.getFieldName(), relation.getForeignField().getColumnName())) {
                        foreignField = iCubeFieldSource;
                    }
                }
                break;
            }
        }
        BITableSourceRelation biTableSourceRelation = new BITableSourceRelation(primaryField, foreignField, primaryTable, foreignTable);
        return biTableSourceRelation;
    }

    private Set<CubeTableSource> getAllTableSource() {
        Set<CubeTableSource> cubeTableSourceSet = new HashSet<CubeTableSource>();
        Set<IBusinessPackageGetterService> packs = BICubeConfigureCenter.getPackageManager().getAllPackages(UserControl.getInstance().getSuperManagerID());
        for (IBusinessPackageGetterService pack : packs) {
            Iterator<BIBusinessTable> tIt = pack.getBusinessTables().iterator();
            while (tIt.hasNext()) {
                BIBusinessTable table = tIt.next();
                cubeTableSourceSet.add(table.getTableSource());
            }
        }
        return cubeTableSourceSet;
    }


    @Override
    public void release() {

    }

    private void buildRelationIndex() {
        CubeTableEntityGetterService primaryTable = null;
        CubeTableEntityGetterService foreignTable = null;
        ICubeColumnEntityService primaryColumn = null;
        ICubeColumnEntityService foreignColumn = null;
        BICubeRelationEntity tableRelation = null;
        try {
            BIColumnKey primaryKey = relation.getPrimaryField();
            BIColumnKey foreignKey = relation.getForeignField();
            ITableKey primaryTableKey = relation.getPrimaryTable();
            ITableKey foreignTableKey = relation.getForeignTable();
            primaryTable = cube.getCubeTable(primaryTableKey);
            foreignTable = cube.getCubeTable(foreignTableKey);
            /**
             * 关联的主字段对象
             */
            primaryColumn = (ICubeColumnEntityService) cube.getCubeColumn(primaryTableKey, primaryKey);
            /**
             * 关联的子字段对象
             */
            foreignColumn = (ICubeColumnEntityService) cube.getCubeColumn(foreignTableKey, foreignKey);
            /**
             * 表间关联对象
             */
            tableRelation = (BICubeRelationEntity) cube.getCubeRelation(primaryTableKey, relation);

            final GroupValueIndex appearPrimaryValue = GVIFactory.createAllEmptyIndexGVI();
            /**
             * 主表的行数
             */
            int primaryRowCount = primaryTable.getRowCount();
            int[] reverse = new int[foreignTable.getRowCount()];
            for (int row = 0; row < primaryRowCount; row++) {
                /**
                 * 关联主字段的value值
                 */
                Object primaryColumnValue = primaryColumn.getOriginalObjectValueByRow(row);
                /**
                 * value值在子字段中的索引位置
                 */

                int position = -1;
                if (foreignColumn.sizeOfGroup() > 0) {
                    position = foreignColumn.getPositionOfGroupByGroupValue(primaryColumnValue);
                }

                /**
                 * 依据索引位置，取出索引
                 */
                GroupValueIndex foreignGroupValueIndex;
                if (position != -1) {
                    foreignGroupValueIndex = foreignColumn.getBitmapIndex(position);
                } else {
                    foreignGroupValueIndex = GVIFactory.createAllEmptyIndexGVI();
                }
                appearPrimaryValue.or(foreignGroupValueIndex);
                tableRelation.addRelationIndex(row, foreignGroupValueIndex);
                initReverseIndex(reverse, row, foreignGroupValueIndex);
            }
            GroupValueIndex nullIndex = appearPrimaryValue.NOT(foreignTable.getRowCount());
            buildReverseIndex(tableRelation, reverse);
            tableRelation.addRelationNULLIndex(0, nullIndex);
        } catch (Exception e) {
            try {
                BILogger.getLogger().error("error relation :" + relation.toString() + " the exception is:" + "relation information used as listed:" + relation.getPrimaryTable().getSourceID() + "." + relation.getPrimaryField().getColumnName() + " to " + relation.getForeignTable().getSourceID() + "." + relation.getForeignField().getColumnName());
            } catch (Exception e1) {
                BILogger.getLogger().error(e1.getMessage(), e1);
            }
            throw BINonValueUtils.beyondControl(e.getMessage(), e);
        } finally {
            if (primaryTable != null) {
                ((CubeTableEntityService) primaryTable).forceReleaseWriter();
                primaryTable.clear();
            }
            if (foreignTable != null) {
                ((CubeTableEntityService) foreignTable).forceReleaseWriter();

                foreignTable.clear();
            }
            if (primaryColumn != null) {
                primaryColumn.forceReleaseWriter();
                primaryColumn.clear();
            }
            if (foreignColumn != null) {
                foreignColumn.forceReleaseWriter();
                foreignColumn.clear();
            }
            if (tableRelation != null) {
                tableRelation.forceReleaseWriter();
                tableRelation.clear();
            }

        }

    }

    private void initReverseIndex(final int[] index, final int row, GroupValueIndex gvi) {
        gvi.Traversal(new SingleRowTraversalAction() {
            @Override
            public void actionPerformed(int rowIndex) {
                index[rowIndex] = row;
            }
        });
    }

    private void buildReverseIndex(ICubeRelationEntityService tableRelation, int[] index) {
        for (int i = 0; i < index.length; i++) {
            tableRelation.addReverseIndex(i, index[i]);
        }
    }

}
