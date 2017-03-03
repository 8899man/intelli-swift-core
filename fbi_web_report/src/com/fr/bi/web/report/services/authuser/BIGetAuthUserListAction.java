package com.fr.bi.web.report.services.authuser;

import com.fr.bi.conf.fs.FBIConfig;
import com.fr.json.JSONArray;
import com.fr.web.core.ActionNoSessionCMD;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Young's on 2016/7/6.
 */
public class BIGetAuthUserListAction extends ActionNoSessionCMD {
    @Override
    public void actionCMD(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String keyword = WebUtils.getHTTPRequestParameter(req, "keyword");
        int userMode = WebUtils.getHTTPRequestIntParameter(req, "mode");
        if (keyword == null) {
            keyword = "";
        }
        JSONArray ja = FBIConfig.getProviderInstance().getUserAuthorAttr().getUserAuthJaByMode(userMode, keyword);
        WebUtils.printAsJSON(res, ja);
    }

    @Override
    public String getCMD() {
        return "get_auth_user_list";
    }
}
