package com.fr.bi.web.report.utils;

import com.fr.bi.cal.analyze.session.BISession;
import com.fr.bi.conf.base.datasource.BIConnectionManager;
import com.fr.bi.conf.report.BIFSReportProvider;
import com.fr.bi.fs.*;
import com.fr.bi.stable.utils.algorithem.BISortUtils;
import com.fr.bi.stable.utils.file.BIPictureUtils;
import com.fr.fs.control.UserControl;
import com.fr.general.DateUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.stable.StableUtils;
import com.fr.stable.bridge.StableFactory;
import com.fr.web.core.SessionDealWith;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by Young's on 2015/9/10.
 */
public class BIFSReportUtils implements BIFSReportProvider {
    @Override
    public long createNewBIReport(BIDesignReport BIReport, long userId, String reportName, String parentId, String description) throws Exception {
        return 0;
    }

    @Override
    public long updateExistBIReport(BIDesignReport BIReport, long userId, String sessionId) throws Exception {
        return 0;
    }

    @Override
    public JSONArray getAllFoldersAndReports(long userId) throws Exception {
        return null;
    }

    @Override
    public long doSaveFileAndObject(BIDesignReport BIReport, BIReportNode node, long userId) throws Exception {
        return 0;
    }

    @Override
    public BIReportNode findReportNode(long reportId, long userId) throws Exception {
        return null;
    }

    @Override
    public JSONObject getBIReportNodeJSON(BIReportNode node) throws Exception {
        return null;
    }
//    public static final String XML_TAG = "BIFSReportUtils";
//    private final static long NO_USE_ID = -1L;
//    private static BIFSReportUtils manager;
//
//    public static BIFSReportUtils getInstance() {
//        synchronized (BIFSReportUtils.class) {
//            if (manager == null) {
//                manager = new BIFSReportUtils();
//            }
//            return manager;
//        }
//    }
//
//    @Override
//    public long createNewBIReport(BIDesignReport BIReport, long userId, String reportName, String parentId, String description) throws Exception{
//        String path = generateUnExistFile(userId, BIReport.getSuffix());
//        BIReportNode biReportNode = new BIReportNode(userId, parentId, reportName, path, description);
//        return doSaveFileAndObject(BIReport, biReportNode, userId);
//    }
//
//    @Override
//    public long updateExistBIReport(BIDesignReport BIReport, long userId, String sessionId) throws Exception{
//        if(sessionId == null){
//            return NO_USE_ID;
//        }
//        BISession session = (BISession) SessionDealWith.getSessionIDInfor(sessionId);
//        if(session == null){
//            return NO_USE_ID;
//        }
//        BIReportNode biReportNode = session.getReportNode();
//        BIReportNodeLockDAO lockDAO = StableFactory.getMarkedObject(BIReportNodeLockDAO.class.getName(), BIReportNodeLockDAO.class);
//        boolean isLockSuccess = lockDAO.lock(sessionId, userId, biReportNode.getId());
//        //被其他的lock住了返回不保存
//        if(!isLockSuccess){
//            return NO_USE_ID;
//        }
//        long createrId = biReportNode.getUserId();
//        //管理员可以修改其他人的 但是非管理员只能修改自己的
//        if(userId != UserControl.getInstance().getSuperManagerID()
//                && createrId != userId){
//            return NO_USE_ID;
//        }
//        biReportNode.updateLastModifyTime();
//        return doSaveFileAndObject(BIReport, biReportNode, createrId);
//    }
//
//    @Override
//    public JSONArray getAllFoldersAndReports(long userId) throws Exception{
//        JSONArray ja = new JSONArray();
//        List<BITemplateFolderNode> folderList = HSQLBITemplateFolderDAO.getInstance().findFolderByUserID(userId);
//        for(int i = 0; i < folderList.size(); i++){
//            ja.put(folderList.get(i).createJSONConfig());
//        }
//
//        List<BIReportNode> nodeList = BIDAOUtils.getBIFSReportManager().findByUserID(userId);
//        BISortUtils.sortByModifyTime(nodeList);
//        for(int i = 0; i < nodeList.size(); i++){
//            ja.put(nodeList.get(i).createJSONConfig());
//        }
//        return ja;
//    }
//
//    @Override
//    public long doSaveFileAndObject(BIDesignReport BIReport, BIReportNode node, long userId) throws Exception{
//        File baseFile = BIFileRepository.getInstance().getBIDirFile(userId);
//        File file = new File(baseFile, node.getPath());
//        Object lock = BIPictureUtils.getImageLock(file.getAbsolutePath());
//        synchronized (lock) {
//            BIReport.writeFile(file);
//        }
//        BIDAOUtils.getBIFSReportManager().saveOrUpDate(node, userId);
//        return node.getId();
//    }
//
//    @Override
//    public BIReportNode findReportNode(long id, long userId) {
//        return null;
//    }
//
//    @Override
//    public JSONObject getBIReportNodeJSON(BIReportNode node) {
//        return null;
//    }
//
//    private String generateUnExistFile(long id, String suffix) throws Exception {
//        File parent = BIFileRepository.getInstance().getBIDirFile(id);
//        int baseFileLength = parent.getAbsolutePath().length() + 1;
//        if (!parent.exists()) {
//            StableUtils.mkdirs(parent);
//        }
//        File file = new File(parent, DateUtils.getDate2AllIncludeSSS(new Date()) + "_" + new Random().nextInt(1000) + suffix);
//        while (file.exists()) {
//            file = new File(parent, DateUtils.getDate2AllIncludeSSS(new Date()) + "_" + new Random().nextInt(1000) + suffix);
//        }
//        return file.getAbsolutePath().substring(baseFileLength);
//    }
}