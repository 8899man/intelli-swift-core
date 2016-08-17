package com.finebi.cube.conf;

import com.finebi.cube.ICubeConfiguration;
import com.finebi.cube.conf.table.BIBusinessTable;
import com.finebi.cube.conf.table.BusinessTable;
import com.finebi.cube.impl.conf.CalculateDependManager;
import com.finebi.cube.impl.conf.CubePreConditionsCheckManager;
import com.finebi.cube.relation.BITableRelation;
import com.finebi.cube.relation.BITableRelationPath;
import com.finebi.cube.relation.BITableSourceRelation;
import com.finebi.cube.relation.BITableSourceRelationPath;
import com.fr.bi.exception.BIKeyAbsentException;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.exception.BITablePathConfusionException;
import com.fr.bi.stable.exception.BITableRelationConfusionException;
import com.fr.bi.stable.utils.code.BILogger;
import com.fr.bi.stable.utils.file.BIFileUtils;
import com.fr.bi.stable.utils.file.BIPathUtils;
import com.fr.bi.stable.utils.program.BINonValueUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by kary on 16/7/11.
 */
public abstract class AbstractCubeBuild implements CubeBuild {
    private long userId;
    protected Set<CubeTableSource> sources = new HashSet<CubeTableSource>();
    private Set allBusinessTable = new HashSet<BIBusinessTable>();
    protected Set<BITableRelationPath> allRelationPathSet = new HashSet<BITableRelationPath>();
    protected Map<CubeTableSource, Map<String, ICubeFieldSource>> tableDBFieldMaps = new HashMap<CubeTableSource, Map<String, ICubeFieldSource>>();
    protected CalculateDependTool calculateDependTool;

    public AbstractCubeBuild(long userId) {
        this.userId = userId;
        init(userId);
        setSources();
        fullTableDBFields();
    }

    private void init(long userId) {
        allBusinessTable = BICubeConfigureCenter.getPackageManager().getAllTables(userId);
        calculateDependTool = new CalculateDependManager();
        try {
            allRelationPathSet = BICubeConfigureCenter.getTableRelationManager().getAllTablePath(userId);
        } catch (BITableRelationConfusionException e) {
            BILogger.getLogger().error(e.getMessage());
        } catch (BITablePathConfusionException e) {
            BILogger.getLogger().error(e.getMessage());
        }
    }

    @Override
    public ICubeConfiguration getCubeConfiguration() {
        return BICubeConfiguration.getTempConf(Long.toString(userId));
    }

    protected Set<List<Set<CubeTableSource>>> calculateTableSource(Set<CubeTableSource> tableSources) {
        Iterator<CubeTableSource> it = tableSources.iterator();
        Set<List<Set<CubeTableSource>>> depends = new HashSet<List<Set<CubeTableSource>>>();
        while (it.hasNext()) {
            CubeTableSource tableSource = it.next();
            depends.add(tableSource.createGenerateTablesList());
        }
        return depends;
    }

    @Override
    public Map<CubeTableSource, Long> getVersions() {
        Set<CubeTableSource> allTable = getAllSingleSources();
        Map<CubeTableSource, Long> result = new HashMap<CubeTableSource, Long>();
        Long version = System.currentTimeMillis();
        for (CubeTableSource table : allTable) {
            result.put(table, version);
        }
        return result;
    }

    @Override
    public boolean preConditionsCheck() {
        CubePreConditionsCheck check = new CubePreConditionsCheckManager();
        File cubeFile = new File(BIPathUtils.createBasePath());
        boolean spaceCheck = check.HDSpaceCheck(cubeFile);
        boolean connectionCheck = check.ConnectionCheck();
        return spaceCheck && connectionCheck;
    }

    @Override
    public Set<CubeTableSource> getSources() {
        return this.sources;
    }

    public static Set<CubeTableSource> set2Set(Set<List<Set<CubeTableSource>>> set) {
        Set<CubeTableSource> result = new HashSet<CubeTableSource>();
        Iterator<List<Set<CubeTableSource>>> outIterator = set.iterator();
        while (outIterator.hasNext()) {
            Iterator<Set<CubeTableSource>> middleIterator = outIterator.next().iterator();
            while (middleIterator.hasNext()) {
                result.addAll(middleIterator.next());
            }
        }
        return result;
    }

    @Override
    public boolean isSingleTable() {
        return false;
    }



    @Override
    public boolean replaceOldCubes() {
        ICubeConfiguration tempConf = BICubeConfiguration.getTempConf(Long.toString(userId));
        ICubeConfiguration advancedConf = BICubeConfiguration.getConf(Long.toString(userId));
        if (new File(advancedConf.getRootURI().getPath()).exists()) {
            BIFileUtils.delete(new File(advancedConf.getRootURI().getPath()));
        }
        try {
            BIFileUtils.renameFolder(new File(tempConf.getRootURI().getPath()), new File(advancedConf.getRootURI().getPath()));
        } catch (IOException e) {
            BILogger.getLogger().error(e.getMessage());
        }
        return true;
    }

    public void setSources() {
        for (Object biBusinessTable : allBusinessTable) {
            BusinessTable table = (BusinessTable) biBusinessTable;
            sources.add(table.getTableSource());
        }
    }


    private void fullTableDBFields() {
        Iterator<CubeTableSource> tableSourceIterator = sources.iterator();
        while (tableSourceIterator.hasNext()) {
            CubeTableSource tableSource = tableSourceIterator.next();
            ICubeFieldSource[] BICubeFieldSources = tableSource.getFieldsArray(sources);
            Map<String, ICubeFieldSource> name2Field = new HashMap<String, ICubeFieldSource>();
            for (int i = 0; i < BICubeFieldSources.length; i++) {
                ICubeFieldSource field = BICubeFieldSources[i];
                name2Field.put(field.getFieldName(), field);
            }
            tableDBFieldMaps.put(tableSource, name2Field);
        }
    }

    protected BITableSourceRelation convertRelation(BITableRelation relation) {
        CubeTableSource primaryTable;
        CubeTableSource foreignTable;
        try {
            primaryTable = BICubeConfigureCenter.getDataSourceManager().getTableSource(relation.getPrimaryField().getTableBelongTo());
            foreignTable = BICubeConfigureCenter.getDataSourceManager().getTableSource(relation.getForeignField().getTableBelongTo());
        } catch (BIKeyAbsentException e) {
            throw BINonValueUtils.beyondControl(e);
        }
        ICubeFieldSource primaryField = tableDBFieldMaps.get(primaryTable).get(relation.getPrimaryField().getFieldName());
        ICubeFieldSource foreignField = tableDBFieldMaps.get(foreignTable).get(relation.getForeignField().getFieldName());
        boolean isSourceRelationValid = null != primaryField && null != foreignField && null != primaryTable && null != foreignTable;
        if (!istableRelationValid(relation) || !isSourceRelationValid) {
//                throw new BIRuntimeException("tableSourceRelation invalid");
            BILogger.getLogger().error("tableSourceRelation invalid!!!");
            return null;
        }
        BITableSourceRelation biTableSourceRelation = new BITableSourceRelation(
                primaryField,
                foreignField,
                primaryTable,
                foreignTable
        );
        primaryField.setTableBelongTo(primaryTable);
        foreignField.setTableBelongTo(foreignTable);
        return biTableSourceRelation;
    }


    protected boolean istableRelationValid(BITableRelation relation) {
        BusinessTable primaryTable = relation.getPrimaryTable();
        BusinessTable foreignTable = relation.getForeignTable();
        return allBusinessTable.contains(primaryTable) && allBusinessTable.contains(foreignTable);
    }

    protected BITableSourceRelationPath convertPath(BITableRelationPath path) throws BITablePathConfusionException {
        BITableSourceRelationPath tableSourceRelationPath = new BITableSourceRelationPath();
        for (BITableRelation biTableRelation : path.getAllRelations()) {
            BITableSourceRelation biTableSourceRelation =convertRelation(biTableRelation);
            if (null==biTableRelation){
                return null;
            }
                tableSourceRelationPath.addRelationAtTail(biTableSourceRelation);
        }
        return tableSourceRelationPath;
    }

    protected Set<BITableSourceRelation> removeDuplicateRelations(Set<BITableSourceRelation> tableRelations) {
        Set<BITableSourceRelation> set = new HashSet<BITableSourceRelation>();
        Map sourceIdMap = new HashMap<String, BITableSourceRelation>();
        for (BITableSourceRelation relation : tableRelations) {
            try {
                if (!sourceIdMap.containsKey(relation.toString())) {
                    set.add(relation);
                    sourceIdMap.put(relation.toString(), relation);
                }
            } catch (NullPointerException e) {
                BILogger.getLogger().error(e.getMessage(), e);
                continue;
            }
        }
        sourceIdMap.clear();
        return set;
    }

    protected Set<BITableSourceRelationPath> removeDuplicateRelationPaths(Set<BITableSourceRelationPath> paths) {
        Set<BITableSourceRelationPath> set = new HashSet<BITableSourceRelationPath>();
        Map sourceIdMap = new HashMap<String, BITableSourceRelationPath>();
        for (BITableSourceRelationPath path : paths) {
            try {
                if (!sourceIdMap.containsKey(path.getSourceID())) {
                    set.add(path);
                    sourceIdMap.put(path.getSourceID(), path);
                }
            } catch (NullPointerException e) {
                BILogger.getLogger().error(e.getMessage(), e);
                continue;
            }
        }
        sourceIdMap.clear();
        return set;
    }
}
