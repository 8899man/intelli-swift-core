package com.fr.bi.web.base.services;

import com.finebi.cube.conf.BICubeConfigureCenter;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.web.base.AbstractBIBaseAction;
import com.fr.fs.web.service.ServiceUtils;
import com.fr.json.JSONArray;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by GUY on 2015/3/31.
 */
public class BIGetTransNameAction extends AbstractBIBaseAction {

    /**
     * ids:["1","2","3"]
     *
     * @return
     */
    @Override
    public String getCMD() {
        return "get_trans";
    }

    @Override
    protected void actionCMDPrivilegePassed(HttpServletRequest req, HttpServletResponse res) throws Exception {
        long userId = ServiceUtils.getCurrentUserID(req);
        JSONArray result = new JSONArray();
        try {
            String ids = WebUtils.getHTTPRequestParameter(req, "ids");
            JSONArray ja = new JSONArray(ids);
            for (int i = 0, len = ja.length(); i < len; i++) {
                result.put(BICubeConfigureCenter.getAliasManager().getAliasName(ja.optString(i), userId));
            }
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }

        WebUtils.printAsJSON(res, result);
    }
}