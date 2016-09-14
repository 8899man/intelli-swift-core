package com.finebi.cube.gen.oper;

import com.finebi.cube.conf.BICubeConfigureCenter;
import com.finebi.cube.conf.pack.data.IBusinessPackageGetterService;
import com.finebi.cube.conf.table.BIBusinessTable;
import com.finebi.cube.exception.BICubeColumnAbsentException;
import com.finebi.cube.exception.BICubeIndexException;
import com.finebi.cube.exception.BICubeRelationAbsentException;
import com.finebi.cube.exception.IllegalRelationPathException;
import com.finebi.cube.impl.pubsub.BIProcessor;
import com.finebi.cube.message.IMessage;
import com.finebi.cube.relation.BITableSourceRelation;
import com.finebi.cube.structure.*;
import com.fr.bi.conf.log.BILogManager;
import com.fr.bi.conf.provider.BILogManagerProvider;
import com.fr.bi.conf.report.widget.RelationColumnKey;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.exception.BITablePathEmptyException;
import com.fr.bi.stable.gvi.GVIFactory;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.gvi.RoaringGroupValueIndex;
import com.fr.bi.stable.gvi.traversal.SingleRowTraversalAction;
import com.fr.bi.stable.utils.code.BILogger;
import com.fr.bi.stable.utils.program.BINonValueUtils;
import com.fr.fs.control.UserControl;
import com.fr.general.ComparatorUtils;
import com.fr.stable.bridge.StableFactory;

import java.util.*;

/**
 * This class created on 2016/4/8.
 *
 * @author Connery
 * @since 4.0
 */
public class BITablePathIndexBuilder extends BIProcessor {
    protected Cube cube;
    protected BICubeTablePath relationPath;

    public BITablePathIndexBuilder(Cube cube, BICubeTablePath relationPath) {
        this.cube = cube;
        this.relationPath = relationPath;
    }

    @Override
    public Object mainTask(IMessage lastReceiveMessage) {
        buildRelationPathIndex();
        return null;
    }

    @Override
    public void release() {

    }

    private void buildRelationPathIndex() {
        if (relationPath.size() >= 2) {
            BILogManager biLogManager = StableFactory.getMarkedObject(BILogManagerProvider.XML_TAG, BILogManager.class);
            biLogManager.logRelationStart(UserControl.getInstance().getSuperManagerID());
            long t = System.currentTimeMillis();
            CubeRelationEntityGetterService lastRelationEntity = null;
            CubeRelationEntityGetterService frontRelationPathReader = null;
            ICubeRelationEntityService targetPathEntity = null;
            RelationColumnKey columnKeyInfo = getRelationColumnKeyInfo();
            try {
                int primaryRowCount = getPrimaryTableRowCount();
                lastRelationEntity = buildLastRelationReader();
                frontRelationPathReader = buildFrontRelationPathReader();
                targetPathEntity = buildTargetRelationPathWriter();
                final GroupValueIndex appearPrimaryValue = GVIFactory.createAllEmptyIndexGVI();
                int[] reverse = new int[getJuniorTableRowCount()];
                for (int row = 0; row < primaryRowCount; row++) {
                    GroupValueIndex frontGroupValueIndex = frontRelationPathReader.getBitmapIndex(row);
                    GroupValueIndex resultGroupValueIndex = getTableLinkedOrGVI(frontGroupValueIndex, lastRelationEntity);
                    appearPrimaryValue.or(resultGroupValueIndex);
                    targetPathEntity.addRelationIndex(row, resultGroupValueIndex);
                    initReverseIndex(reverse, row, resultGroupValueIndex);
                }
                GroupValueIndex nullIndex = appearPrimaryValue.NOT(reverse.length);
                buildReverseIndex(targetPathEntity, reverse);
                targetPathEntity.addRelationNULLIndex(0, nullIndex);
                long costTime = System.currentTimeMillis() - t;
                try {
                    biLogManager.infoRelation(columnKeyInfo, costTime, UserControl.getInstance().getSuperManagerID());
                } catch (Exception e) {
                    BILogger.getLogger().error(e.getMessage());
                }
            } catch (Exception e) {
                try {
                    biLogManager.errorRelation(columnKeyInfo, e.getMessage(), UserControl.getInstance().getSuperManagerID());
                } catch (Exception e1) {
                    BILogger.getLogger().error(e1.getMessage());

                }
                throw BINonValueUtils.beyondControl(e.getMessage(), e);
            } finally {

                if (lastRelationEntity != null) {
                    lastRelationEntity.clear();
                }
                if (frontRelationPathReader != null) {
                    frontRelationPathReader.clear();
                }
                if (targetPathEntity != null) {
                    targetPathEntity.forceReleaseWriter();
                    targetPathEntity.clear();
                }
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

    public static GroupValueIndex getTableLinkedOrGVI(GroupValueIndex currentIndex, final ICubeIndexDataGetterService reader) {
        if (currentIndex != null) {

            final GroupValueIndex result = new RoaringGroupValueIndex();
            currentIndex.Traversal(new SingleRowTraversalAction() {
                @Override
                public void actionPerformed(int rowIndices) {
                    try {
                        result.or(reader.getBitmapIndex(rowIndices));
                    } catch (BICubeIndexException e) {
                        BILogger.getLogger().error(e.getMessage(), e);
                    }
                }
            });
            return result;
        }
        return null;
    }

    protected int getPrimaryTableRowCount() throws BITablePathEmptyException {
        ITableKey primaryTableKey = relationPath.getFirstRelation().getPrimaryTable();
        CubeTableEntityGetterService primaryTable = cube.getCubeTable(primaryTableKey);
        int rowCount = primaryTable.getRowCount();
        primaryTable.clear();
        return rowCount;
    }

    protected int getJuniorTableRowCount() throws BITablePathEmptyException {
        ITableKey primaryTableKey = relationPath.getLastRelation().getForeignTable();
        CubeTableEntityGetterService primaryTable = cube.getCubeTable(primaryTableKey);
        int rowCount = primaryTable.getRowCount();
        primaryTable.clear();
        return rowCount;
    }

    private CubeRelationEntityGetterService buildLastRelationReader() throws
            BICubeRelationAbsentException, BICubeColumnAbsentException, IllegalRelationPathException, BITablePathEmptyException {
        BICubeRelation lastRelation = relationPath.getLastRelation();
        ITableKey lastPrimaryKey = lastRelation.getPrimaryTable();
        return cube.getCubeRelation(lastPrimaryKey, lastRelation);
    }

    private ICubeRelationEntityService buildTargetRelationPathWriter() throws
            BICubeRelationAbsentException, BICubeColumnAbsentException, IllegalRelationPathException, BITablePathEmptyException {
        BICubeTablePath frontRelation = new BICubeTablePath();
        frontRelation.copyFrom(relationPath);
        ITableKey firstPrimaryKey = relationPath.getFirstRelation().getPrimaryTable();
        return (ICubeRelationEntityService) cube.getCubeRelation(firstPrimaryKey, frontRelation);
    }

    private CubeRelationEntityGetterService buildFrontRelationPathReader() throws
            BICubeRelationAbsentException, BICubeColumnAbsentException, IllegalRelationPathException, BITablePathEmptyException {
        ITableKey firstPrimaryKey = relationPath.getFirstRelation().getPrimaryTable();
        BICubeTablePath frontRelation = buildFrontRelation();
        return cube.getCubeRelation(firstPrimaryKey, frontRelation);
    }

    private BICubeTablePath buildFrontRelation() throws BITablePathEmptyException {
        BICubeTablePath frontRelation = new BICubeTablePath();
        frontRelation.copyFrom(relationPath);
        frontRelation.removeLastRelation();
        return frontRelation;
    }

    public RelationColumnKey getRelationColumnKeyInfo() {
        List<BITableSourceRelation> relations = new ArrayList<BITableSourceRelation>();
        ICubeFieldSource field = null;
        for (BICubeRelation relation : this.relationPath.getAllRelations()) {
            BITableSourceRelation tableRelation = null;
            try {
                tableRelation = getTableRelation(relation);
            } catch (Exception e) {
                BILogger.getLogger().error("get relationColumnKey failed! relation information used as listed:" + relation.getPrimaryTable().getSourceID() + "." + relation.getPrimaryField().getColumnName() + " to " + relation.getForeignTable().getSourceID() + "." + relation.getForeignField().getColumnName());
            }
            field = tableRelation.getPrimaryKey();
            relations.add(tableRelation);
        }
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

}
