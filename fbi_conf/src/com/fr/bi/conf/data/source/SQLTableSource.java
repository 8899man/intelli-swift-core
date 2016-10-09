package com.fr.bi.conf.data.source;

import com.fr.base.TableData;
import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.stable.constant.BIBaseConstant;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.data.db.IPersistentTable;
import com.fr.bi.stable.data.db.ServerLinkInformation;
import com.fr.bi.stable.utils.BIDBUtils;
import com.fr.bi.stable.utils.DecryptBi;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.json.JSONObject;

/**
 * Created by GUY on 2015/3/2.
 */
public class SQLTableSource extends ServerTableSource {

    public static final String XML_TAG = "SQLTableSource";

    @BICoreField
    private String sql;
    @BICoreField
    private String sqlConnection;
    @BICoreField
    private String sqlName;
    @BICoreField
    private String enSQL;

    public SQLTableSource() {
        super();
    }

    public String getQuery() {
        return sql;
    }

    public String getSqlConnection() {
        return sqlConnection;
    }

    @Override
    public JSONObject createJSON() throws Exception {
        JSONObject jo = super.createJSON();
        //为了兼容
        String sqlStr = enSQL;
        if(enSQL == null) {
            sqlStr = DecryptBi.encrypt(sql, "sh");
        }
        jo.put("sql", sqlStr);
        jo.put("dataLinkName", sqlConnection);
        jo.put("connection_name", DBConstant.CONNECTION.SQL_CONNECTION);
        jo.put("table_name", sqlName);
        return jo;
    }

    public void parseJSON(JSONObject jo, long userId) throws Exception {
        if (jo.has("sql")) {
            enSQL = jo.getString("sql");
            sql = DecryptBi.decrypt(enSQL, "sh");
        }
        if (jo.has("dataLinkName")) {
            sqlConnection = jo.getString("dataLinkName");
        }
        if (jo.has("table_name")) {
            sqlName = jo.getString("table_name");
        }
    }


    @Override
    public IPersistentTable getPersistentTable() {
        if (dbTable == null) {
            dbTable = BIDBUtils.getServerBITable(sqlConnection, sql, fetchObjectCore().getID().getIdentityValue());
        }
        return dbTable;
    }

    @Override
    protected TableData getTableData() {
        try {
            ServerLinkInformation serverLinkInformation = new ServerLinkInformation(getSqlConnection(), getQuery());
            return serverLinkInformation.createDBTableData();
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public int getType() {
        return BIBaseConstant.TABLETYPE.SQL;
    }

    @Override
    public String getTableName() {
        return this.sqlName;
    }
}