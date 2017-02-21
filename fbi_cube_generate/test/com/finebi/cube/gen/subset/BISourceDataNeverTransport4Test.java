package com.finebi.cube.gen.subset;

import com.finebi.cube.ICubeConfiguration;
import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.gen.oper.BISourceDataNeverTransport;
import com.finebi.cube.location.BICubeLocation;
import com.finebi.cube.message.IMessage;
import com.finebi.cube.structure.Cube;
import com.finebi.cube.tools.BICubeConfigurationTool;
import com.finebi.cube.tools.BILocationBuildTestTool;
import com.finebi.cube.tools.BIProjectPathTool;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.utils.file.BIFileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * This class created on 2016/8/03.
 */
public class BISourceDataNeverTransport4Test extends BISourceDataNeverTransport {

    public BISourceDataNeverTransport4Test(Cube cube, CubeTableSource tableSource, Set<CubeTableSource> allSources, Set<CubeTableSource> parentTableSource) {
        super(cube, tableSource, allSources, parentTableSource, 1);
    }



    @Override
    public Object mainTask(IMessage lastReceiveMessage) {
        try {
            copyFromOldCubes();
            recordTableInfo();
            tableEntityService.addVersion(version);
        } catch (Exception e) {
        } finally {
            return null;
        }
    }
    protected void copyFromOldCubes() {
        ICubeConfiguration tempConf = new ICubeConfiguration() {
            @Override
            public URI getRootURI() {
                return URI.create(BILocationBuildTestTool.buildWrite(BIProjectPathTool.projectPath, "temptable").getAbsolutePath());
            }
        };

        ICubeConfiguration advancedConf = new BICubeConfigurationTool();
        try {
            BICubeLocation from = new BICubeLocation(advancedConf.getRootURI().getPath().toString(), tableSource.getSourceID());
            BICubeLocation to = new BICubeLocation(tempConf.getRootURI().getPath().toString(), tableSource.getSourceID());
            BIFileUtils.copyFolder(new File(from.getAbsolutePath()),new File(to.getAbsolutePath()));
        } catch (IOException e) {
            BILoggerFactory.getLogger().error(e.getMessage(),e);
        } catch (URISyntaxException e) {
            BILoggerFactory.getLogger().error(e.getMessage(),e);
        }

    }
}
