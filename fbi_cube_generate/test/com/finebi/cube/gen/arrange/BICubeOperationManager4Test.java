package com.finebi.cube.gen.arrange;

import com.finebi.cube.gen.oper.*;
import com.finebi.cube.gen.oper.watcher.BICubeBuildFinishWatcher;
import com.finebi.cube.gen.oper.watcher.BIDataSourceBuildFinishWatcher;
import com.finebi.cube.gen.oper.watcher.BIPathBuildFinishWatcher;
import com.finebi.cube.gen.subset.*;
import com.finebi.cube.gen.subset.watcher.BICubeBuildFinishWatcher4Test;
import com.finebi.cube.gen.subset.watcher.BIDataSourceBuildFinish4Test;
import com.finebi.cube.gen.subset.watcher.BIPathBuildFinishWatcher4Test;
import com.finebi.cube.relation.BITableSourceRelation;
import com.finebi.cube.relation.BITableSourceRelationPath;
import com.finebi.cube.structure.Cube;
import com.finebi.cube.structure.column.BIColumnKey;
import com.fr.bi.conf.manager.update.source.UpdateSettingSource;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.source.CubeTableSource;

import java.util.Set;

/**
 * This class created on 2016/4/13.
 *
 * @author Connery
 * @since 4.0
 */
public class BICubeOperationManager4Test extends BICubeOperationManager {
    public BICubeOperationManager4Test(Cube cube, Set<CubeTableSource> originalTableSet) {
        super(cube, originalTableSet);
    }

    @Override
    protected BIRelationIndexGenerator getRelationBuilder(Cube cube, BITableSourceRelation relation) {
        return new BIRelationIndexBuilder4Test(cube, null);
    }

    @Override
    protected BIFieldIndexGenerator getFieldIndexBuilder(Cube cube, CubeTableSource tableSource, ICubeFieldSource BICubeFieldSource, BIColumnKey targetColumnKey) {
        return new BIFieldIndexBuilder4Test(cube, tableSource, BICubeFieldSource, targetColumnKey);
    }


    @Override
    protected BISourceDataTransport getDataTransportBuilder(Cube cube, CubeTableSource tableSource, Set<CubeTableSource> allSources, Set<CubeTableSource> parent, long version, UpdateSettingSource updateSetting) {
        return new BISourceDataTransport4Test(cube, tableSource, allSources, parent);
    }

    @Override
    protected BITablePathIndexBuilder getTablePathBuilder(Cube cube, BITableSourceRelationPath tablePath) {
        return new BITablePathIndexBuilder4Test(cube, null);
    }

    @Override
    protected BIFieldPathIndexBuilder getFieldPathBuilder(Cube cube, ICubeFieldSource field, BITableSourceRelationPath tablePath) {
        return new BIFieldPathIndexBuilder4Test(cube, field, null);
    }

    @Override
    protected BIDataSourceBuildFinishWatcher getDataSourceBuildFinishWatcher() {
        return new BIDataSourceBuildFinish4Test();
    }

    @Override
    protected BICubeBuildFinishWatcher getCubeBuildFinishWatcher() {
        return new BICubeBuildFinishWatcher4Test();
    }

    @Override
    protected BIPathBuildFinishWatcher getPathBuildFinishWatcher() {
        return new BIPathBuildFinishWatcher4Test();
    }

}
