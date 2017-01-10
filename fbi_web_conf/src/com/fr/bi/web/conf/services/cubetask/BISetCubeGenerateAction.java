package com.fr.bi.web.conf.services.cubetask;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.CubeGenerationManager;
import com.fr.bi.cal.generate.CubeBuildManager;
import com.fr.bi.conf.provider.BIConfigureManagerCenter;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.constant.Status;
import com.fr.bi.web.conf.AbstractBIConfigureAction;
import com.fr.fs.web.service.ServiceUtils;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BISetCubeGenerateAction extends AbstractBIConfigureAction {

    @Override
    public String getCMD() {
        return "set_cube_generate";
    }

    @Override
    protected void actionCMDPrivilegePassed(HttpServletRequest req,
                                            HttpServletResponse res) throws Exception {
        long userId = ServiceUtils.getCurrentUserID(req);
        CubeGenerationManager.getCubeManager().setStatus(userId, Status.PREPARING);
        String baseTableSourceId = WebUtils.getHTTPRequestParameter(req, "baseTableSourceId");
        int updateType = WebUtils.getHTTPRequestIntParameter(req, "updateType");
        boolean cubeBuild = cubeTaskBuild(userId, baseTableSourceId, updateType);
        WebUtils.printAsJSON(res, new JSONObject().put("result", cubeBuild));
    }

    private boolean cubeTaskBuild(long userId, String baseTableSourceId, int updateType) {
        try {
            if (StringUtils.isEmpty(baseTableSourceId)) {
                new CubeBuildManager().CubeBuildStaff(userId);
            } else {
                new CubeBuildManager().addSingleTableTask(userId, baseTableSourceId, updateType);
            }
            BIConfigureManagerCenter.getCubeConfManager().updatePackageLastModify();
            BIConfigureManagerCenter.getCubeConfManager().updateMultiPathLastCubeStatus(BIReportConstant.MULTI_PATH_STATUS.NOT_NEED_GENERATE_CUBE);
            BIConfigureManagerCenter.getCubeConfManager().persistData(userId);
            return true;
        } catch (Exception e) {
            CubeGenerationManager.getCubeManager().setStatus(userId, Status.WRONG);
            BILoggerFactory.getLogger(this.getClass()).error("cube task build failed" + "\n" + e.getMessage(), e);
            return false;
        } finally {
            CubeGenerationManager.getCubeManager().setStatus(userId, Status.END);
        }
    }

}
