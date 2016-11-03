package com.fr.bi.cal.generate.index;

import com.finebi.cube.conf.CubeGenerationManager;
import com.fr.bi.base.BIUser;
import com.fr.bi.cal.loader.CubeGeneratingTableIndexLoader;
import com.fr.bi.cal.stable.cube.file.TableCubeFile;
import com.fr.bi.cal.stable.index.*;
import com.fr.bi.conf.log.BIRecord;
import com.fr.bi.conf.provider.BIConfigureManagerCenter;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.index.CubeGenerator;
import com.fr.bi.stable.log.CubeGenerateStatusProvider;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.stable.utils.file.BIPathUtils;
import com.fr.general.DateUtils;
import com.fr.stable.StringUtils;

import java.util.Arrays;

/**
 * Created by GUY on 2015/3/13.
 */
public class IndexGenerator implements CubeGenerator, java.util.concurrent.Callable<Object>, CubeGenerateStatusProvider {

    protected CubeTableSource source;

    protected TableCubeFile cube;

    protected int version;
    protected BIUser biUser;
    private transient int percent;

    public IndexGenerator(CubeTableSource source, long userId, int version) {
        biUser = new BIUser(userId);
        this.source = source;
        createTableCube();
        this.version = version;
    }

    protected String pathSuffix = StringUtils.EMPTY;//路径后缀

    public IndexGenerator(CubeTableSource source, String pathSuffix, long userId, int version) {
        biUser = new BIUser(userId);
        this.source = source;
        this.pathSuffix = pathSuffix;
        this.version = version;
        createTableCube();
    }

    private IndexGenerator() {
    }

    protected void createTableCube() {
        cube = new TableCubeFile(BIPathUtils.createTableTempPath(source.fetchObjectCore().getID().getIdentityValue(), biUser.getUserId()) + pathSuffix);
    }

    @Override
    public Object call() throws Exception {
        try {
            BILoggerFactory.getLogger().info("now start" + source.toString() + "loading data");
            long start = System.currentTimeMillis();
            generateSimpleCube();
            BIConfigureManagerCenter.getLogManager().infoTableReading(source.getPersistentTable(),
                    System.currentTimeMillis() - start, biUser.getUserId());
            BILoggerFactory.getLogger().info("loading table data:" + source.toString() + "cost:" + DateUtils.timeCostFrom(start));
        } catch (Throwable e) {
            wrong(e);
            BILoggerFactory.getLogger().info("generate:" + source.toString() + " failed");
        }
        return null;
    }

    @Override
    public int getPercent() {
        return percent;
    }

    @Override
    public void setPercent(int percent) {
        this.percent = percent;
    }

    protected void release() {
        setPercent(0);
    }

    protected void wrong(Throwable e) {
        BILoggerFactory.getLogger().error(e.getMessage(), e);
        BIConfigureManagerCenter.getLogManager().errorTable(source.getPersistentTable(), e.getClass().getName() + ":" + e.getMessage() + ":" + Arrays.toString(e.getStackTrace()), biUser.getUserId());
        release();
        cube.delete();
    }

    private void generateSimpleCube() {
        try {
            BIRecord log = BIConfigureManagerCenter.getLogManager().getBILog(biUser.getUserId());
            AbstractIndexGenerator generator = new BeforeIndexGenerator(cube, source,
                    CubeGenerationManager.getCubeManager().getGeneratingObject(biUser.getUserId()).getAllTableSources(), log);
            BIConfigureManagerCenter.getLogManager().infoTable(source.getPersistentTable(), 0, biUser.getUserId());
            BIConfigureManagerCenter.getLogManager().infoTableReading(source.getPersistentTable(), 0, biUser.getUserId());
            generator.generateCube();
            generator = createSimpleIndexGenerator();
            generator.generateCube();
            new FinishIndexGenerator(cube, log).generateCube();
        } catch (Throwable e) {
            wrong(e);
            BILoggerFactory.getLogger().info("generate:" + source.toString() + " failed");
        } finally {
            this.release();
        }
    }

    protected AbstractIndexGenerator createSimpleIndexGenerator() {
        return new SimpleIndexGenerator(cube, source, CubeGenerationManager.getCubeManager().getGeneratingObject(biUser.getUserId()).getAllTableSources(), version, BIConfigureManagerCenter.getLogManager().getBILog(biUser.getUserId()), CubeGeneratingTableIndexLoader.getInstance(biUser.getUserId()));
    }

    public void generateIndex() {
        try {
            BILoggerFactory.getLogger().info("now start" + source.toString() + "indexing");
            BIRecord log = BIConfigureManagerCenter.getLogManager().getBILog(biUser.getUserId());
            long start = System.currentTimeMillis();
            CubeGenerator generator = new GroupIndexGenerator(cube, source, CubeGeneratingTableIndexLoader.getInstance(biUser.getUserId()), log);
            generator.generateCube();
            BILoggerFactory.getLogger().info("indexing completed, cost:" + DateUtils.timeCostFrom(start));
        } catch (Throwable e) {
            wrong(e);
            BILoggerFactory.getLogger().info("generate:" + source.toString() + " failed");
        } finally {
            this.release();
        }
    }

    @Override
    public void generateCube() {
        long start = System.currentTimeMillis();
        BILoggerFactory.getLogger().info("now generating:" + source.toString() + " Cube:");
        setPercent(0);
        cube.delete();
        generateSimpleCube();
        setPercent(50);
        generateIndex();
        setPercent(100);
        BILoggerFactory.getLogger().info("generating:" + source.toString() + "cost：" + DateUtils.timeCostFrom(start));
    }

    @Override
    public String getDetail() {
        return null;
    }

    @Override
    public void setDetail(String s) {

    }
}
