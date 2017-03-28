package com.finebi.cube.impl.conf;


import com.finebi.cube.ICubeConfiguration;
import com.finebi.cube.conf.AbstractCubeBuildStuff;
import com.finebi.cube.conf.BICubeConfiguration;
import com.finebi.cube.conf.CubeBuildStuff;
import com.finebi.cube.relation.*;
import com.finebi.cube.utils.BIDataStructTranUtils;
import com.fr.bi.base.BIUser;
import com.fr.bi.conf.manager.update.source.UpdateSettingSource;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.engine.CubeTaskType;
import com.fr.bi.stable.utils.program.BIStringUtils;

import java.util.*;

/**
 * Created by kary on 16/6/1.
 * 主要用于实时报表的生成
 */
public class CubeBuildStuffRealTime extends AbstractCubeBuildStuff implements CubeBuildStuff {

    private Set<CubeTableSource> allSingleSources;
    private ICubeConfiguration cubeConfiguration;
    private BIUser biUser;
    Set<List<Set<CubeTableSource>>> dependTableResource;
    private String sourceId;

    public CubeBuildStuffRealTime(CubeTableSource cubeTableSource, ICubeConfiguration cubeConfiguration, long userId) {
        super(userId);
        sourceId = cubeTableSource.getSourceID();
        this.biUser = new BIUser(userId);
        this.cubeConfiguration = cubeConfiguration;
        Set<CubeTableSource> sourceSet = new HashSet<CubeTableSource>();
        sourceSet.add(cubeTableSource);
        this.allSingleSources = BIDataStructTranUtils.set2Set(calculateTableSource(sourceSet));
        init();
    }

    private void init() {
        this.dependTableResource = calculateTableSource(BIDataStructTranUtils.set2Set(calculateTableSource(allSingleSources)));

    }

    public CubeBuildStuffRealTime(CubeTableSource cubeTableSource, long userId) {
        super(userId);
        this.biUser = new BIUser(userId);
        this.cubeConfiguration = BICubeConfiguration.getConf(Long.toString(biUser.getUserId()));
        Set<CubeTableSource> sourceSet = new HashSet<CubeTableSource>();
        sourceSet.add(cubeTableSource);
        this.allSingleSources = BIDataStructTranUtils.set2Set(calculateTableSource(sourceSet));
        init();
    }

    public Set<BITableSourceRelationPath> getTableSourceRelationPathSet() {
        return new HashSet<BITableSourceRelationPath>();
    }


    @Override
    public Set<CubeTableSource> getSingleSourceLayers() {
        return allSingleSources;
    }

    @Override
    public Set<BITableSourceRelation> getTableSourceRelationSet() {
        return new HashSet<BITableSourceRelation>();
    }

    @Override
    public Set<CubeTableSource> getSystemTableSources() {
        return allSingleSources;
    }

    @Override
    public Set<List<Set<CubeTableSource>>> getDependTableResource() {
        return dependTableResource;
    }

    @Override
    public ICubeConfiguration getCubeConfiguration() {
        return cubeConfiguration;
    }


    @Override
    public Set<BICubeGenerateRelationPath> getCubeGenerateRelationPathSet() {
        return new HashSet<BICubeGenerateRelationPath>();
    }

    @Override
    public Set<BICubeGenerateRelation> getCubeGenerateRelationSet() {
        return new HashSet<BICubeGenerateRelation>();
    }

    @Override
    public boolean preConditionsCheck() {
        return true;
    }

    @Override
    public boolean copyFileFromOldCubes() {
        return false;
    }

    @Override
    public boolean replaceOldCubes() {
        return true;
    }

    /*
    * 对实时报表来说，所有更新都是全量更新
     */
    @Override
    public Map<CubeTableSource, UpdateSettingSource> getUpdateSettingSources() {
        return new HashMap<CubeTableSource, UpdateSettingSource>();
    }

    public String getCubeTaskId() {
        return BIStringUtils.append(DBConstant.CUBE_UPDATE_TYPE.SINGLETABLE_UPDATE, sourceId);
    }

    @Override
    public CubeTaskType getTaskType() {
        return CubeTaskType.INSTANT;
    }

    @Override
    public Set<String> getTaskTableSourceIds() {
        return getDependTableSourceIdSet(dependTableResource);
    }
}
