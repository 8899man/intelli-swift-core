package com.fr.bi.web.conf.services.cubetask;

import com.finebi.cube.utils.CubeUpdateUtils;
import com.fr.bi.conf.provider.BIConfigureManagerCenter;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.data.BITableID;
import com.fr.bi.stable.utils.code.BILogger;
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
        String baseTableSourceId = WebUtils.getHTTPRequestParameter(req, "baseTableSourceId");
        String tableId = WebUtils.getHTTPRequestParameter(req, "tableId");
//        Boolean isETL = Boolean.valueOf(WebUtils.getHTTPRequestParameter(req, "isETL"));
        int updateType = WebUtils.getHTTPRequestIntParameter(req, "updateType");
        BIConfigureManagerCenter.getLogManager().logStart(userId);
        try {
            CubeUpdateUtils.recordTableAndRelationInfo(userId);
        } catch (Exception e) {
            BILogger.getLogger().error(e.getMessage(), e);
        }
        boolean cubeBuild;
        if (StringUtils.isEmpty(baseTableSourceId)) {
            cubeBuild = CubeTaskHelper.CubeBuildStaff(userId);
        } else {
            cubeBuild = CubeTaskHelper.CubeBuildSingleTable(userId, new BITableID(tableId), baseTableSourceId, updateType);
        }
        BIConfigureManagerCenter.getCubeConfManager().updatePackageLastModify();
        BIConfigureManagerCenter.getCubeConfManager().updateMultiPathLastCubeStatus(BIReportConstant.MULTI_PATH_STATUS.NOT_NEED_GENERATE_CUBE);
        BIConfigureManagerCenter.getCubeConfManager().persistData(userId);
        JSONObject jsonObject = new JSONObject().put("result", cubeBuild);
        WebUtils.printAsJSON(res, jsonObject);
    }

}
