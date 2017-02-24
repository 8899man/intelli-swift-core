package com.fr.bi.web.conf.services.dbconnection;

import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.base.FRContext;
import com.fr.bi.conf.base.datasource.BIConnectionManager;
import com.fr.bi.data.MongoDBExtractor;
import com.fr.bi.mongodb.MongoDatabaseConnection;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.web.conf.AbstractBIConfigureAction;
import com.fr.data.core.DataCoreUtils;
import com.fr.data.core.db.TableProcedure;
import com.fr.file.DatasourceManager;
import com.fr.general.ComparatorUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;
import com.fr.stable.ArrayUtils;
import com.fr.stable.StringUtils;
import com.fr.web.utils.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 获取数据连接里面的所有表
 */
public class BIGetAllTranslatedTablesByConnectionAction extends
        AbstractBIConfigureAction {

    @Override
    public String getCMD() {
        return "get_all_translated_tables_by_connection";
    }

    private void dealWithServerTableData(JSONArray ja) throws JSONException {
        JSONObject jo = new JSONObject();
        JSONArray tablesJa = new JSONArray();
        JSONObject groupJo = new JSONObject();
        groupJo.put("group_name", "0");
        JSONArray groupArray = new JSONArray();
        Iterator names = DatasourceManager.getInstance().getTableDataNameIterator();
        while (names.hasNext()) {
            JSONObject table = new JSONObject();
            table.put("value", names.next());
            groupArray.put(table);
        }
        if (groupArray.length() > 0) {
            groupJo.put("group", groupArray);
            tablesJa.put(groupJo);
            jo.put("tables", tablesJa);
            ja.put(jo);
        }
    }

    @Override
    protected void actionCMDPrivilegePassed(HttpServletRequest req,
                                            HttpServletResponse res) throws Exception {
        String connectionName = WebUtils.getHTTPRequestParameter(req, "connectionName");
        JSONArray ja = new JSONArray();
        JSONObject jo = new JSONObject();
        if (isServerTableData(connectionName)) {
            dealWithServerTableData(ja);
        } else {
            com.fr.data.impl.Connection dbc = DatasourceManager.getInstance().getConnection(connectionName);
            TableProcedure[] tps = new TableProcedure[0];
            TableProcedure[] views = new TableProcedure[0];
            String schemaName = BIConnectionManager.getBIConnectionManager().getSchema(connectionName);
            try {
                if (schemaName != null) {
                    jo.put("schema", schemaName);
                    //无schema的同样需要取
                    String[] schemas = DataCoreUtils.getDatabaseSchema(dbc);
                    if (StringUtils.isNotEmpty(schemaName) || schemas.length == 0) {
                        TableProcedure[] sqlTables = DataCoreUtils.getTables(dbc, TableProcedure.TABLE, schemaName, true);
                        tps = ArrayUtils.addAll(tps, sqlTables);
                        views = ArrayUtils.addAll(views, FRContext.getCurrentEnv().getTableProcedure(dbc, TableProcedure.VIEW, schemaName));
                    }
                } else {
                    if (dbc instanceof MongoDatabaseConnection) {
                        List<String> collList = MongoDBExtractor.getInstance().getCollections((MongoDatabaseConnection) dbc);
                        TableProcedure[] tmp = new TableProcedure[collList.size()];
                        for (int i = 0; i < collList.size(); i++) {
                            tmp[i] = new TableProcedure(schemaName,collList.get(i),TableProcedure.TABLE,null);
                        }
                        tps= tmp;
                    } else {
                        tps = FRContext.getCurrentEnv().getTableProcedure(dbc, TableProcedure.TABLE, null);
                        views = FRContext.getCurrentEnv().getTableProcedure(dbc, TableProcedure.VIEW, null);
                    }
                }
            } catch (Exception e) {
                jo.put("error", e.getMessage());
                BILoggerFactory.getLogger().error(e.getMessage(), e);
            }
            //遇到重复的表，过滤掉
            TableProcedure[] result = duplicateRemove(tps, views);
            Map<String, ArrayList<TableProcedure>> tpMap = splitTableProcedureBySchema(result);
            Set<String> set = tpMap.keySet();
            for (String schema : set) {
                dealWithOneSchema(schema, tpMap, ja);
            }
        }
        jo.put("items", ja);
        WebUtils.printAsJSON(res, jo);
    }

    private TableProcedure[] duplicateRemove(TableProcedure[] tps, TableProcedure[] views) {
        TableProcedure[] tmpViews = new TableProcedure[0];
        for (TableProcedure procedure : views) {
            if (!existInTables(procedure, tps)) {
                tmpViews = ArrayUtils.add(views, procedure);
            }
        }
        return ArrayUtils.addAll(tps, tmpViews);
    }

    private boolean existInTables(TableProcedure procedure, TableProcedure[] sqlTables) {
        for (TableProcedure table : sqlTables) {
            if (ComparatorUtils.equals(procedure.getSchema(), table.getSchema()) && ComparatorUtils.equals(procedure.getName(), table.getName())) {
                return true;
            }
        }
        return false;
    }

    private void dealWithOneSchema(String schema, Map<String, ArrayList<TableProcedure>> tpMap, JSONArray ja) {
        List<TableProcedure> tableList = tpMap.get(schema);
        JSONObject schemaObject = new JSONObject();
        JSONArray schemaTableGroupArray = new JSONArray();
        TableProcedure[] tpls = tableList.toArray(new TableProcedure[tableList.size()]);
        try {
            int group = (int) Math.ceil(tpls.length / 100.0);
            for (int i = 0; i < group; i++) {
                JSONObject groupObj = new JSONObject();
                JSONArray groupArray = new JSONArray();
                //存储当前组的第一个和最后一个的名字
                String group_name = "";
                for (int j = 100 * i, length = Math.min(tpls.length, 100 * (i + 1)); j < length; j++) {
                    if (j == 100 * i) {
                        group_name = tpls[j].getName();
                    } else if (j == length - 1 && (length - 100 * i) > 1) {
                        group_name += " " + tpls[j].getName();
                    }
                    JSONObject jo = new JSONObject().put("value", tpls[j].getName()).put("schema", tpls[j].getSchema());
                    groupArray.put(jo);
                }
                groupObj.put("group_name", group_name);
                groupObj.put("group", groupArray);
                schemaTableGroupArray.put(groupObj);
            }
            schemaObject.put("tables", schemaTableGroupArray);
            ja.put(schemaObject);
        } catch (Exception ignore) {

        }
    }

    private Map<String, ArrayList<TableProcedure>> splitTableProcedureBySchema(TableProcedure[] tps) {
        Arrays.sort(tps, new Comparator<TableProcedure>() {
            @Override
            public int compare(TableProcedure tp0, TableProcedure tp1) {
                return ComparatorUtils.compare(getTableProcedureComparatorName(tp0), getTableProcedureComparatorName(tp1));
            }
        });
        Map<String, ArrayList<TableProcedure>> tpMap = new HashMap<String, ArrayList<TableProcedure>>();
        for (TableProcedure tp : tps) {
            String schema = tp.getSchema();
            if (!tpMap.containsKey(schema)) {
                tpMap.put(schema, new ArrayList<TableProcedure>());
            }
            tpMap.get(schema).add(tp);
        }
        return tpMap;
    }

    private String getTableProcedureComparatorName(TableProcedure tp) {
        if (!StringUtils.isEmpty(tp.getSchema())) {
            return tp.getSchema() + "." + tp.getName();
        } else {
            return tp.getName();
        }
    }

    private boolean isServerTableData(String conn) {
        return ComparatorUtils.equals(conn, DBConstant.CONNECTION.SERVER_CONNECTION);
    }

}