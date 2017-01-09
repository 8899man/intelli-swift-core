package com.fr.bi.web.conf.services;

import com.finebi.cube.api.BICubeManager;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.conf.data.source.TableSourceFactory;
import com.fr.bi.stable.constant.BIJSONConstant;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.web.conf.AbstractBIConfigureAction;
import com.fr.bi.web.conf.utils.BIWebConfUtils;
import com.fr.fs.web.service.ServiceUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/**
 * Created by sheldon on 14-5-7.
 * 单表预览
 */
public class BIGetPreviewTableDataConfAction extends AbstractBIConfigureAction {
    @Override
    protected void actionCMDPrivilegePassed(HttpServletRequest req, HttpServletResponse res) throws Exception {
        long userId = ServiceUtils.getCurrentUserID(req);
        CubeTableSource source = TableSourceFactory.createTableSource(new JSONObject(WebUtils.getHTTPRequestParameter(req, BIJSONConstant.JSON_KEYS.TABLE)), userId);
        JSONArray ja = new JSONObject(WebUtils.getHTTPRequestParameter(req, BIJSONConstant.JSON_KEYS.TABLE)).getJSONArray(BIJSONConstant.JSON_KEYS.FIELDS);
        ArrayList<String> fields = new ArrayList<String>();
        for (int i = 0; i < ja.length(); i++) {
            JSONArray tmp = new JSONArray(ja.getString(i));
            for (int j = 0; j < tmp.length(); j++) {
                fields.add(new JSONObject(tmp.getString(j)).getString(BIJSONConstant.JSON_KEYS.FIELD_NAME));
            }
        }
        JSONObject jo = null;
        if (BIWebConfUtils.checkCubeVersion(source, userId)) {
            jo = source.createPreviewJSONFromCube(fields, BICubeManager.getInstance().fetchCubeLoader(userId));
        } else {
            if (source.canExecute()) {
                jo = source.createPreviewJSON(fields, BICubeManager.getInstance().fetchCubeLoader(userId), userId);
            }else {
                BILoggerFactory.getLogger().error("table SQL preview failed! table name: "+source.getTableName());
            }
        }
        WebUtils.printAsJSON(res, jo);
    }

    @Override
    public String getCMD() {
        return "get_preview_table_conf";
    }
}