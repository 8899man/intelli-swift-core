package com.fr.bi.web.report.services;

import com.fr.bi.fs.BISharedReportDAO;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.fs.control.UserControl;
import com.fr.fs.web.service.ServiceUtils;
import com.fr.general.Decrypt;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.web.core.ActionNoSessionCMD;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by 小灰灰 on 2015/8/4.
 */
public class BIShareTemplateSaveAction extends ActionNoSessionCMD {
    @Override
    public void actionCMD(HttpServletRequest req, HttpServletResponse res) throws Exception {
        long userId = ServiceUtils.getCurrentUserID(req);
        String templateIdsString = WebUtils.getHTTPRequestParameter(req, "reports");
        String userIdsString = WebUtils.getHTTPRequestParameter(req, "users");
        boolean isEdit = WebUtils.getHTTPRequestBoolParameter(req, "edit_shared");

        JSONObject result = new JSONObject();
        templateIdsString = Decrypt.decrypt(templateIdsString, "neilsx");
        userIdsString = Decrypt.decrypt(userIdsString, "neilsx");
        JSONArray jaTemplateIds = new JSONArray(templateIdsString);
        JSONArray jaUserIds = new JSONArray(userIdsString);
        long[] templateIds = new long[jaTemplateIds.length()];
        long[] userIds = new long[jaUserIds.length()];
        for (int i = 0, len = userIds.length; i < len; i++) {
            userIds[i] = jaUserIds.getLong(i);
        }
        try {
            for(int j = 0; j < templateIds.length; j++) {
                UserControl.getInstance().getOpenDAO(BISharedReportDAO.class).resetSharedByReportIdAndUsers(jaTemplateIds.getLong(j), userId, userIds, isEdit);
            }
            result.put("result", true);
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(),e);
        }
        WebUtils.printAsJSON(res, result);
    }

    @Override
    public String getCMD() {
        return "template_folder_share";
    }
}