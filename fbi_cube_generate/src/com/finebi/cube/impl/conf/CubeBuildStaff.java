package com.finebi.cube.impl.conf;

import com.finebi.cube.ICubeConfiguration;
import com.finebi.cube.conf.AbstractCubeBuild;
import com.finebi.cube.conf.BICubeConfiguration;
import com.finebi.cube.conf.BICubeConfigureCenter;
import com.finebi.cube.conf.CalculateDependTool;
import com.finebi.cube.relation.*;
import com.fr.bi.base.BIUser;
import com.fr.bi.conf.provider.BIConfigureManagerCenter;
import com.fr.bi.exception.BIKeyAbsentException;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.utils.code.BILogger;
import com.fr.bi.stable.utils.file.BIFileUtils;
import com.fr.bi.stable.utils.program.BINonValueUtils;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * This class created on 2016/5/23.
 *
 * @author Connery
 * @since 4.0
 * kary 这个是真正意义上完整的全局更新，无论是否有数据，更新所有能更新的
 */
public class CubeBuildStaff extends AbstractCubeBuild implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -2315016175890907748L;
    private Set<CubeTableSource> allSingleSources;

    private Set<BITableSourceRelation> tableSourceRelationSet;
    private Set<BITableRelation> tableRelationSet;
    private Map<CubeTableSource, Set<BITableSourceRelation>> primaryKeyMap;
    private Map<CubeTableSource, Set<BITableSourceRelation>> foreignKeyMap;
    private BIUser biUser;
    private Set<BITableSourceRelationPath> relationPaths;
    private Set<BICubeGenerateRelationPath> cubeGenerateRelationPathSet;
    private Set<BICubeGenerateRelation> cubeGenerateRelationSet;
    /**
     * TableSource之间存在依赖关系，这一点很合理。
     * 这个结构肯定是不好的。
     * 不合理的在于为何要把这个依赖关系用一个List(原来是个Map)，把间接依赖的统统获得。
     * 开发的时候封装一下即可，如果当时不封装，这个结构就镶嵌代码了，随着开发替换代价越高。
     */
    private Set<List<Set<CubeTableSource>>> dependTableResource;


    public CubeBuildStaff(BIUser biUser) {
        super(biUser.getUserId());
        this.biUser = biUser;
        initialCubeStuff();
    }

    @Override
    public Set<BITableRelation> getTableRelationSet() {
        Set<BITableRelation> set = new HashSet<BITableRelation>();
        for (BITableRelation relation : tableRelationSet) {
            try {
                CubeTableSource primaryTable = BICubeConfigureCenter.getDataSourceManager().getTableSource(relation.getPrimaryField().getTableBelongTo());
                CubeTableSource foreignTable = BICubeConfigureCenter.getDataSourceManager().getTableSource(relation.getForeignField().getTableBelongTo());
                ICubeFieldSource primaryField = tableDBFieldMaps.get(primaryTable).get(relation.getPrimaryField().getFieldName());
                ICubeFieldSource foreignField = tableDBFieldMaps.get(foreignTable).get(relation.getForeignField().getFieldName());
                if (tableSourceRelationSet.contains(
                        new BITableSourceRelation(
                                primaryField,
                                foreignField,
                                primaryTable,
                                foreignTable
                        ))) {
                    set.add(relation);
                }
            } catch (BIKeyAbsentException e) {
                BILogger.getLogger().error(e.getMessage(), e);
                continue;
            }
        }
        return set;
    }


    public Set<BICubeGenerateRelationPath> getCubeGenerateRelationPathSet() {
        return this.cubeGenerateRelationPathSet;
    }

    public Set<BICubeGenerateRelation> getCubeGenerateRelationSet() {
        return this.cubeGenerateRelationSet;
    }

    private Set<BITableRelation> filterRelation(Set<BITableRelation> tableRelationSet) {
        Iterator<BITableRelation> iterator = tableRelationSet.iterator();
        Set<BITableRelation> result = new HashSet<BITableRelation>();
        while (iterator.hasNext()) {
            BITableRelation tableRelation = iterator.next();
            if (isTableRelationValid(tableRelation)) {
                result.add(tableRelation);
            }
        }
        return tableRelationSet;
    }

    public void setTableRelationSet(Set<BITableRelation> tableRelationSet) {
        this.tableRelationSet = filterRelation(tableRelationSet);
        this.tableSourceRelationSet = removeDuplicateRelations(convertRelations(this.tableRelationSet));
    }

    public Set<BITableSourceRelationPath> getBiTableSourceRelationPathSet() {
        return relationPaths;
    }

    public void setRelationPaths(Set<BITableSourceRelationPath> relationPaths) {
        this.relationPaths = relationPaths;
    }

    private Set<BITableSourceRelation> convertRelations(Set<BITableRelation> relationSet) {
        Set<BITableSourceRelation> set = new HashSet<BITableSourceRelation>();
        for (BITableRelation relation : relationSet) {
            try {
                BITableSourceRelation tableSourceRelation = convertRelation(relation);
                if (null != tableSourceRelation) {
                    set.add(tableSourceRelation);
                }
            } catch (NullPointerException e) {
                BILogger.getLogger().error(e.getMessage(), e);
                continue;
            }
        }
        return set;
    }


    private Set<BITableSourceRelationPath> convertPaths(Set<BITableRelationPath> paths) {
        Set<BITableSourceRelationPath> set = new HashSet<BITableSourceRelationPath>();
        for (BITableRelationPath path : paths) {
            try {
                BITableSourceRelationPath relationPath = convertPath(path);
                if (null != relationPath) {
                    set.add(relationPath);
                }
            } catch (Exception e) {
                BILogger.getLogger().error(e.getMessage());
            }
        }
        set = removeDuplicateRelationPaths(set);
        return set;
    }

    @Override
    public Set<CubeTableSource> getAllSingleSources() {
        BIConfigureManagerCenter.getLogManager().cubeTableSourceSet(allSingleSources, biUser.getUserId());
        return allSingleSources;
    }

    @Override
    public boolean copyFileFromOldCubes() {
        ICubeConfiguration tempConf = BICubeConfiguration.getTempConf(String.valueOf(biUser.getUserId()));
        if (new File(tempConf.getRootURI().getPath()).exists()) {
            BIFileUtils.delete(new File(tempConf.getRootURI().getPath()));
        }
        BICubeConfiguration advancedTempConf = BICubeConfiguration.getAdvancedTempConf(String.valueOf(biUser.getUserId()));
        if (new File(advancedTempConf.getRootURI().getPath()).exists()) {
            BIFileUtils.delete(new File(tempConf.getRootURI().getPath()));
        }
        return true;
    }

    public void setAllSingleSources(Set<CubeTableSource> allSingleSources) {
        this.allSingleSources = allSingleSources;
    }


    public Set<List<Set<CubeTableSource>>> getDependTableResource() {
        return dependTableResource;
    }

    public void setDependTableResource(Set<List<Set<CubeTableSource>>> dependTableResource) {
        this.dependTableResource = dependTableResource;
    }


    /**
     * @return the tableSourceRelationSet
     */
    @Override
    public Set<BITableSourceRelation> getTableSourceRelationSet() {
        return tableSourceRelationSet;
    }

    /**
     * @return the primaryKeyMap
     */
    public Map<CubeTableSource, Set<BITableSourceRelation>> getPrimaryKeyMap() {
        return primaryKeyMap;
    }

    /**
     * @param primaryKeyMap the primaryKeyMap to set
     */
    public void setPrimaryKeyMap(Map<CubeTableSource, Set<BITableSourceRelation>> primaryKeyMap) {
        this.primaryKeyMap = primaryKeyMap;
    }

    /**
     * @return the foreignKeyMap
     */
    public Map<CubeTableSource, Set<BITableSourceRelation>> getForeignKeyMap() {
        return foreignKeyMap;
    }

    /**
     * @param foreignKeyMap the foreignKeyMap to set
     */
    public void setForeignKeyMap(Map<CubeTableSource, Set<BITableSourceRelation>> foreignKeyMap) {
        this.foreignKeyMap = foreignKeyMap;
    }

    public void initialCubeStuff() {
        try {
            Set<List<Set<CubeTableSource>>> depends = calculateTableSource(getSources());
            setDependTableResource(depends);
            setAllSingleSources(set2Set(depends));
            setTableRelationSet(BICubeConfigureCenter.getTableRelationManager().getAllTableRelation(biUser.getUserId()));
            Map<CubeTableSource, Set<BITableSourceRelation>> primaryKeyMap = new HashMap<CubeTableSource, Set<BITableSourceRelation>>();
            setPrimaryKeyMap(primaryKeyMap);
            Map<CubeTableSource, Set<BITableSourceRelation>> foreignKeyMap = new HashMap<CubeTableSource, Set<BITableSourceRelation>>();
            setForeignKeyMap(foreignKeyMap);
            setRelationPaths(convertPaths(BICubeConfigureCenter.getTableRelationManager().getAllTablePath(biUser.getUserId())));
            calculateDepend();
        } catch (Exception e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }

    private void calculateDepend() {
        CalculateDependTool cal = new CalculateDependManager();
        cubeGenerateRelationSet = new HashSet<BICubeGenerateRelation>();
        for (BITableSourceRelation biTableSourceRelation : this.getTableSourceRelationSet()) {
            this.cubeGenerateRelationSet.add(cal.calRelations(biTableSourceRelation, this.getSources()));
        }
        cubeGenerateRelationPathSet = new HashSet<BICubeGenerateRelationPath>();
        for (BITableSourceRelationPath biTableSourceRelationPath : this.getBiTableSourceRelationPathSet()) {
            BICubeGenerateRelationPath biCubeGenerateRelationPath = cal.calRelationPath(biTableSourceRelationPath, this.tableSourceRelationSet);
            if (null != biCubeGenerateRelationPath && biCubeGenerateRelationPath.getDependRelationPathSet().size() != 0) {
                cubeGenerateRelationPathSet.add(biCubeGenerateRelationPath);
            }
        }
    }

}
