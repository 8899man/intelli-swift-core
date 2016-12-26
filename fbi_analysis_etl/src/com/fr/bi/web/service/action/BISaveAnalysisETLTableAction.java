package com.fr.bi.web.service.action;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.pack.data.BIBusinessPackage;
import com.finebi.cube.conf.table.BusinessTable;
import com.finebi.cube.conf.utils.BILogHelper;
import com.fr.bi.base.BIUser;
import com.fr.bi.conf.provider.BIConfigureManagerCenter;
import com.fr.bi.etl.analysis.Constants;
import com.fr.bi.etl.analysis.conf.AnalysisBusiTable;
import com.fr.bi.etl.analysis.data.AnalysisCubeTableSource;
import com.fr.bi.etl.analysis.data.AnalysisETLSourceFactory;
import com.fr.bi.etl.analysis.manager.BIAnalysisETLManagerCenter;
import com.fr.bi.exception.BIKeyAbsentException;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.fs.web.service.ServiceUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * Created by 小灰灰 on 2016/4/7.
 */
public class BISaveAnalysisETLTableAction extends AbstractAnalysisETLAction {
    @Override
    public void actionCMD(HttpServletRequest req, HttpServletResponse res, String sessionID) throws Exception {
        final long userId = ServiceUtils.getCurrentUserID(req);
        String tableId = WebUtils.getHTTPRequestParameter(req, "id");
        String newId = WebUtils.getHTTPRequestParameter(req, "new_id");
        String tableName = WebUtils.getHTTPRequestParameter(req, "name");
        String describe = WebUtils.getHTTPRequestParameter(req, "describe");
        AnalysisBusiTable table = null;
        CubeTableSource source = null;
        if (StringUtils.isEmpty(newId)) {
            table = new AnalysisBusiTable(tableId, userId);
            table.setDescribe(describe);
            String tableJSON = WebUtils.getHTTPRequestParameter(req, "table");
            JSONObject jo = new JSONObject(tableJSON);
            JSONArray items = jo.getJSONArray(Constants.ITEMS);
            BIAnalysisETLManagerCenter.getAliasManagerProvider().setAliasName(tableId, tableName, userId);
            source = AnalysisETLSourceFactory.createTableSource(items, userId);
            table.setSource(source);
        } else {
            table = new AnalysisBusiTable(newId, userId);
            BIAnalysisETLManagerCenter.getAliasManagerProvider().setAliasName(newId, tableName, userId);
            AnalysisBusiTable oldTable = BIAnalysisETLManagerCenter.getBusiPackManager().getTable(tableId, userId);
            source = oldTable.getSource();
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(source.createJSON());
            AnalysisCubeTableSource newSource = AnalysisETLSourceFactory.createTableSource(jsonArray, userId);
            table.setSource(newSource);
            table.setDescribe(oldTable.getDescribe());
        }
        BILoggerFactory.getLogger(BISaveAnalysisETLTableAction.class).info("*********Add AnalysisETL table*******");
        BIAnalysisETLManagerCenter.getBusiPackManager().addTable(table);
        BILoggerFactory.getLogger(BISaveAnalysisETLTableAction.class).info("The added table is: " + logTable(table));
        BILoggerFactory.getLogger(BISaveAnalysisETLTableAction.class).info("*********Add AnalysisETL table*******");
        BIAnalysisETLManagerCenter.getDataSourceManager().addTableSource(table, source);
        refreshTables(userId);
        try {
            BIAnalysisETLManagerCenter.getUserETLCubeManagerProvider().checkTableIndex((AnalysisCubeTableSource) source, new BIUser(userId));
        } catch (Exception e) {
            BILoggerFactory.getLogger().error("etl update failed");
        }
        JSONObject result = getResult(userId, tableName, table);
        WebUtils.printAsJSON(res, result);
        new Thread() {
            public void run() {
                BIAnalysisETLManagerCenter.getAliasManagerProvider().persistData(userId);
                BIAnalysisETLManagerCenter.getBusiPackManager().persistData(userId);
                BIAnalysisETLManagerCenter.getDataSourceManager().persistData(userId);
            }
        }.start();
    }

    private JSONObject getResult(long userId, String tableName, AnalysisBusiTable table) throws Exception {
        JSONObject result = new JSONObject();
        JSONObject packages = BIAnalysisETLManagerCenter.getBusiPackManager().createPackageJSON(userId);
        JSONObject translations = new JSONObject();
        translations.put(table.getID().getIdentity(), tableName);
        JSONObject tableJSONWithFieldsInfo = table.createJSONWithFieldsInfo(userId);
        JSONObject tableFields = tableJSONWithFieldsInfo.getJSONObject("tableFields");
        JSONObject tables = new JSONObject();
        tables.put(table.getID().getIdentity(), tableFields);
        JSONObject fields = tableJSONWithFieldsInfo.getJSONObject("fieldsInfo");
        result.put("packages", packages);
        result.put("translations", translations);
        result.put("tables", tables);
        result.put("fields", fields);
        return result;
    }

    private void refreshTables(long userId) throws BIKeyAbsentException {
        Set<BusinessTable> businessTables = BIAnalysisETLManagerCenter.getBusiPackManager().getAllTables(userId);
        if (businessTables != null) {
            for (BusinessTable t : businessTables) {
                try {
                    AnalysisCubeTableSource s = (AnalysisCubeTableSource) BIAnalysisETLManagerCenter.getDataSourceManager().getTableSource(t);
                    s.refreshWidget();
                    t.setSource(s);
                } catch (Exception e) {
                    BILoggerFactory.getLogger(BISaveAnalysisETLTableAction.class).error("Refresh AnalysisETLTableSource Widget failed" + "\n" + "The Failed table is: " + logTable(t));
                }

            }
        }
        BIAnalysisETLManagerCenter.getUserETLCubeManagerProvider().refresh();
        BIConfigureManagerCenter.getCubeConfManager().updatePackageLastModify();
    }

    @Override
    public String getCMD() {
        return "save_table";
    }

    private String logTable(BusinessTable table) {
        try {
            return BILogHelper.logAnalysisETLTable(table) +
                    "\n" + "*********Fields of AnalysisETL table*******" +
                    BILogHelper.logAnalysisETLTableField(table, "") +
                    "\n" + "*********Fields of AnalysisETL table*******";
        } catch (Exception e) {
            BILoggerFactory.getLogger(BIBusinessPackage.class).error(e.getMessage(), e);
            return "";
        }
    }
}
