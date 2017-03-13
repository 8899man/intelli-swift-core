package com.fr.bi.web.report.services;

import com.fr.bi.fs.BIDAOUtils;
import com.fr.bi.fs.BIReportNode;
import com.fr.bi.fs.BITemplateFolderNode;
import com.fr.bi.fs.HSQLBITemplateFolderDAO;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.utils.algorithem.BISortUtils;
import com.fr.fs.base.entity.User;
import com.fr.fs.control.EntryControl;
import com.fr.fs.control.UserControl;
import com.fr.fs.web.service.ServiceUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.web.core.ActionNoSessionCMD;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Baron.
 */
public class BIGetReportAndFolderAction extends ActionNoSessionCMD {

    @Override
    public void actionCMD(HttpServletRequest req, HttpServletResponse res) throws Exception {
        long userId = ServiceUtils.getCurrentUserID(req);
        JSONArray ja = new JSONArray();
        List<BITemplateFolderNode> folderList = HSQLBITemplateFolderDAO.getInstance().findFolderByUserID(userId);
        for(int i = 0; i < folderList.size(); i++){
            ja.put(folderList.get(i).createJSONConfig());
        }

        List<BIReportNode> nodeList = BIDAOUtils.getBIDAOManager().findByUserID(userId);
        BISortUtils.sortByModifyTime(nodeList);
        if (nodeList == null) {
            nodeList = new ArrayList<BIReportNode>();
        }
        JSONArray allEntry = EntryControl.getInstance().getRootNode().createAllEntryJSONArray(UserControl.getInstance().getSuperManagerID(), true);
        for(int i = 0; i < nodeList.size(); i++){
            BIReportNode node = nodeList.get(i);
            /*处理授权文件被删除的情况时用到*/
//            node.setParentid("-1");
//            BIDAOUtils.saveOrUpDate(node, userId);
            if (node.getStatus() == BIReportConstant.REPORT_STATUS.HANGOUT &&
                    checkReportStatus(node.getId(), node.getUserId(), allEntry)) {
                node.setStatus(BIReportConstant.REPORT_STATUS.NORMAL);
            }
            JSONObject nodeJO = node.createJSONConfig();
            nodeJO.put("shared", getSharedUsers(node.getId(), userId));
            ja.put(nodeJO);
        }
        WebUtils.printAsJSON(res, ja);
    }

    private JSONArray getSharedUsers(long reportId, long createBy) throws Exception{
        List<User> users = BIDAOUtils.getBIDAOManager().getSharedUsersByReport(reportId, createBy);
        JSONArray ja = new JSONArray();
        if(users != null) {
            for(int i = 0; i < users.size(); i++){
                User user = users.get(i);
                if(user.getId() != createBy) {
                    JSONObject jo = user.createJSON4Share();
                    jo.put("roles", UserControl.getInstance().getAllSRoleNames(user.getId()));
                    ja.put(jo);
                }
            }
        }
        return ja;
    }

    private boolean checkReportStatus(long reportId, long createBy, JSONArray entries) throws Exception {
        boolean needReset = true;
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            JSONArray childNodes = entry.optJSONArray("ChildNodes");
            if (childNodes != null && checkReportStatus(reportId, createBy, childNodes) == false) {
                needReset = false;
                break;
            }
            long rId = entry.optLong("reportId");
            long uId = entry.optLong("createBy");
            if (reportId == rId && uId == createBy) {
                needReset = false;
                break;
            }
        }
        return needReset;

    }

    @Override
    public String getCMD() {
        return "get_folder_report_list";
    }

}