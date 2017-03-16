package com.fr.bi.web.conf.services.datalink;

import com.fr.bi.conf.base.datasource.BIConnectionManager;
import com.fr.bi.web.conf.AbstractBIConfigureAction;
import com.fr.fs.control.UserControl;
import com.fr.fs.web.service.ServiceUtils;
import com.fr.general.ComparatorUtils;
import com.fr.json.JSONObject;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class BIGetDataLinkAction extends AbstractBIConfigureAction {

    @Override
    public String getCMD() {
        return "get_data_link";
    }

    @Override
    protected void actionCMDPrivilegePassed(HttpServletRequest req, HttpServletResponse res) throws Exception {
        JSONObject jo = JSONObject.create();
        WebUtils.printAsJSON(res, jo.put("links", BIConnectionManager.getBIConnectionManager().createJSON()));
    }

}