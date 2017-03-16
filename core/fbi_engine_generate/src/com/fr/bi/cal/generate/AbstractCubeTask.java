package com.fr.bi.cal.generate;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.BICubeConfigureCenter;
import com.finebi.cube.conf.CubeBuildStuff;
import com.finebi.cube.impl.conf.CubeBuildStuffComplete;
import com.finebi.cube.relation.BICubeGenerateRelation;
import com.finebi.cube.relation.BITableSourceRelation;
import com.fr.bi.base.BIUser;
import com.fr.bi.cal.generate.index.IndexGenerator;
import com.fr.bi.cal.generate.relation.RelationGenerator;
import com.fr.bi.cal.stable.cube.file.TableCubeFile;
import com.fr.bi.conf.provider.BIConfigureManagerCenter;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.engine.CubeTask;
import com.fr.bi.stable.utils.CubeBaseUtils;
import com.fr.general.DateUtils;
import com.fr.json.JSONObject;

import java.util.*;


public abstract class AbstractCubeTask implements CubeTask {

    private static final long serialVersionUID = -6885958858349067492L;

    private String name = UUID.randomUUID().toString();

    private Date start;

    private Date end;
    protected BIUser biUser;
    private CubeBuildStuff cubeBuild;


    public AbstractCubeTask(long userId) {
        biUser = new BIUser(userId);
    }


    @Override
    public JSONObject createJSON() throws Exception {
        JSONObject jo = new JSONObject();
        jo.put("type", getTaskType());
        jo.put("id", getTaskId());
        if (start != null) {
            jo.put("start", start.getTime());
        }
        if (end != null) {
            jo.put("end", end.getTime());
        }
        return jo;
    }

    @Override
    public void start() {
        start = new Date();
        cubeBuild = new CubeBuildStuffComplete(biUser);
        BICubeConfigureCenter.getPackageManager().startBuildingCube(biUser.getUserId());
        BIConfigureManagerCenter.getLogManager().logStart(getUserId());
    }

    @Override
    public void end() {
        end = new Date();
        BICubeConfigureCenter.getPackageManager().finishGenerateCubes(biUser.getUserId());
        Set<BITableSourceRelation> relations = new HashSet<BITableSourceRelation>();
        for (BICubeGenerateRelation relation : cubeBuild.getCubeGenerateRelationSet()) {
            relations.add(relation.getRelation());
        }
        BICubeConfigureCenter.getTableRelationManager().finishGenerateCubes(biUser.getUserId(), relations);
        BIConfigureManagerCenter.getLogManager().logEnd(getUserId());
    }


    @Override
    public String getTaskId() {
        return name;
    }

    protected abstract Map<Integer, Set<CubeTableSource>> getGenerateTables();


    @Override
    public void run() {
        loadIndex(getGenerateTables());
        loadRelation();
    }

    protected void loadIndex(Map<Integer, Set<CubeTableSource>> tables) {
        if (tables == null || tables.isEmpty()) {
            return;
        }

        //多线程取数
        BILoggerFactory.getLogger().info("start sync data from database");
        long start = System.currentTimeMillis();
        List<IndexGenerator> threadList = new ArrayList<IndexGenerator>();
        for (Map.Entry<Integer, Set<CubeTableSource>> entry : tables.entrySet()) {
            List<IndexGenerator> ilist = new ArrayList<IndexGenerator>();
            for (CubeTableSource source : entry.getValue()) {
                IndexGenerator generator = createGenerator(source);
                if (generator != null) {
                    threadList.add(generator);
                    ilist.add(generator);
                }
            }
            try {
                CubeBaseUtils.invokeCubeThreads(ilist);
            } catch (Exception e) {
                BILoggerFactory.getLogger().error(e.getMessage(), e);
            }
        }

        BILoggerFactory.getLogger().info("data sync complete! cost :" + DateUtils.timeCostFrom(start));

        BIConfigureManagerCenter.getLogManager().logIndexStart(biUser.getUserId());
        //生成索引
        for (IndexGenerator ob : threadList) {
            ob.generateIndex();
        }
    }

    protected abstract boolean checkCubeVersion(TableCubeFile cube);

    protected abstract IndexGenerator createGenerator(CubeTableSource source);

    protected void loadRelation() {
        BIConfigureManagerCenter.getLogManager().logRelationStart(biUser.getUserId());
        //生成关联
        new RelationGenerator(biUser.getUserId()).generateCube();
    }
}
