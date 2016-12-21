package com.fr.bi.conf.data.source;

import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.base.TableData;
import com.fr.bi.base.BIBasicCore;
import com.fr.bi.base.BICore;
import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.common.inter.Traversal;
import com.fr.bi.common.persistent.xml.BIIgnoreField;
import com.fr.bi.conf.VT4FBI;
import com.fr.bi.conf.base.datasource.BIConnectionManager;
import com.fr.bi.conf.log.BILogManager;
import com.fr.bi.conf.manager.update.source.UpdateSettingSource;
import com.fr.bi.conf.provider.BILogManagerProvider;
import com.fr.bi.data.DBQueryExecutor;
import com.fr.bi.stable.constant.BIBaseConstant;
import com.fr.bi.stable.constant.BIJSONConstant;
import com.fr.bi.stable.constant.CubeConstant;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.data.db.BIDataValue;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.data.db.IPersistentTable;
import com.fr.bi.stable.data.db.SqlSettedStatement;
import com.fr.bi.stable.data.source.AbstractTableSource;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.utils.BIDBUtils;
import com.fr.bi.stable.utils.SQLRegUtils;
import com.fr.data.core.db.dialect.Dialect;
import com.fr.data.core.db.dialect.DialectFactory;
import com.fr.data.core.db.dml.Table;
import com.fr.data.impl.Connection;
import com.fr.data.impl.DBTableData;
import com.fr.data.impl.EmbeddedTableData;
import com.fr.file.DatasourceManager;
import com.fr.fs.control.UserControl;
import com.fr.general.ComparatorUtils;
import com.fr.general.Inter;
import com.fr.general.data.DataModel;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.stable.bridge.StableFactory;
import com.fr.stable.xml.XMLPrintWriter;
import com.fr.stable.xml.XMLableReader;

import java.util.*;

/**
 * Created by GUY on 2015/2/28.
 */
public class DBTableSource extends AbstractTableSource {

    public static final String XML_TAG = "DBTableSource";
    /**
     *
     */
    private static final long serialVersionUID = -337260203343265208L;
    @BICoreField
    protected String dbName;
    @BICoreField
    protected String tableName;
    protected UpdateSettingSource updateSettingSource;
    @BIIgnoreField
    protected com.fr.data.impl.Connection connection;

    public DBTableSource() {
        super();
    }

    public DBTableSource(String dbName, String tableName) {
        super();
        this.dbName = dbName;
        this.tableName = tableName;
        fetchObjectCore();
    }

    public String getDbName() {
        return dbName;
    }

    public String getTableName() {
        return tableName;
    }


    /**
     * @return
     */
    public static BICore getCore(String coreID) {

        return BIBasicCore.generateValueCore(coreID);
    }

    @Override
    public IPersistentTable getPersistentTable() {
        if (dbTable == null) {
            BILoggerFactory.getLogger(DBTableSource.class).info("The table:" + this.getTableName() + "extract data from db");
            dbTable = BIDBUtils.getDBTable(dbName, tableName);
        }
        return dbTable;
    }

    /**
     * 根据sources获取fields, 用来生成cube,判断cube版本
     *
     * @param sources generatingobjects 的packs的sources
     * @return 字段
     */
    @Override
    public ICubeFieldSource[] getFieldsArray(Set<CubeTableSource> sources) {
        ICubeFieldSource[] allFields = super.getFieldsArray(sources);
        if (sources == null || sources.isEmpty()) {
            return allFields;
        }
        Iterator<CubeTableSource> it = sources.iterator();
        Set<String> usedFields = new HashSet<String>();
        while (it.hasNext()) {
            usedFields.addAll(((it.next())).getUsedFields(this));
        }
        ArrayList<ICubeFieldSource> fields = new ArrayList<ICubeFieldSource>();
        for (ICubeFieldSource field : allFields) {
            if (usedFields.contains(field.getFieldName())) {
                fields.add(field);
            }
        }
        return fields.toArray(new ICubeFieldSource[fields.size()]);
    }


    @Override
    public int getType() {
        return BIBaseConstant.TABLETYPE.DB;
    }

    void dealWithOneData(Traversal<BIDataValue> travel, ICubeFieldSource[] fields, BIDataValue v) {
        if (!VT4FBI.supportBigData() && v.getRow() >= CubeConstant.LICMAXROW) {
            throw new RuntimeException(Inter.getLocText("BI-Not_Support_10w_data"));
        }
        if (travel != null) {
            if (v.getValue() == null && fields[v.getCol()].getFieldType() == DBConstant.COLUMN.STRING) {
                v = new BIDataValue(v.getRow(), v.getCol(), "");
            }
            travel.actionPerformed(v);
        }

    }

    @Override
    public long read(final Traversal<BIDataValue> travel, final ICubeFieldSource[] fields, ICubeDataLoader loader) {
        long rowCount = 0;
        BILogManager biLogManager = StableFactory.getMarkedObject(BILogManagerProvider.XML_TAG, BILogManager.class);
        long t = System.currentTimeMillis();
        try {
            rowCount = DBQueryExecutor.getInstance().runSQL(BIDBUtils.getSQLStatement(dbName, tableName), fields, new Traversal<BIDataValue>() {
                @Override
                public void actionPerformed(BIDataValue v) {
                    try {
                        dealWithOneData(travel, fields, v);
                    } catch (Exception e) {
                        BILoggerFactory.getLogger().error(e.getMessage(), e);
                    }
                }
            });
            if (fields.length > 0) {
                biLogManager.infoTableReading(fields[0].getTableBelongTo().getPersistentTable(), System.currentTimeMillis() - t, UserControl.getInstance().getSuperManagerID());
            }
        } catch (Throwable e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
        }
        return rowCount;
    }

    public long read4Part(Traversal<BIDataValue> traversal, ICubeFieldSource[] fields, String SQL, long oldCount) {
        oldCount = dealWithInsert(traversal, fields, SQL, oldCount, connection);
        return oldCount;
    }


    protected long dealWithInsert(final Traversal<BIDataValue> traversal, final ICubeFieldSource[] fields, String SQL, long rowCount, com.fr.data.impl.Connection connection) {
        BILogManager biLogManager = StableFactory.getMarkedObject(BILogManagerProvider.XML_TAG, BILogManager.class);
        long t = System.currentTimeMillis();
        try {
            SQLRegUtils regUtils = new SQLRegUtils(SQL);
            if (!regUtils.isSql()) {
                BILoggerFactory.getLogger().error("SQL syntax error");
                return 0;
            }
            SqlSettedStatement sqlStatement = new SqlSettedStatement(connection);
            sqlStatement.setSql(SQL);
            rowCount = DBQueryExecutor.getInstance().runSQL(sqlStatement, fields, new Traversal<BIDataValue>() {
                @Override
                public void actionPerformed(BIDataValue v) {
                    try {
                        dealWithOneData(traversal, fields, v);
                    } catch (Exception e) {
                        BILoggerFactory.getLogger().error(e.getMessage(), e);
                    }
                }
            }, (int) rowCount);
            if (fields.length > 0) {
                biLogManager.infoTableReading(fields[0].getTableBelongTo().getPersistentTable(), System.currentTimeMillis() - t, UserControl.getInstance().getSuperManagerID());
            }
        } catch (Throwable e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
        }
        return rowCount;
    }

    /**
     * 获取某个字段的distinct值
     *
     * @param fieldName
     * @param userId
     */
    @Override
    public Set getFieldDistinctNewestValues(String fieldName, ICubeDataLoader loader, long userId) {
        final HashSet set = new HashSet();
        ICubeFieldSource field = getFields().get(fieldName);
        if (field == null) {
            return set;
        }
        final boolean isStringType = field.getFieldType() == DBConstant.COLUMN.STRING;
        DBQueryExecutor.getInstance().runSQL(BIDBUtils.getDistinctSQLStatement(dbName, tableName, fieldName), new ICubeFieldSource[]{field}, new Traversal<BIDataValue>() {
            @Override
            public void actionPerformed(BIDataValue data) {
                if (isStringType && data.getValue() == null) {
                    set.add("");
                } else {
                    set.add(data.getValue());
                }
            }
        });
        return set;
    }

    protected TableData createPreviewTableData() throws Exception {
        Connection connection = BIConnectionManager.getInstance().getConnection(dbName);
        java.sql.Connection conn = connection.createConnection();
        Dialect dialect = DialectFactory.generateDialect(conn);
        Table table = new Table(BIConnectionManager.getInstance().getSchema(dbName), tableName);
        String query = "SELECT *  FROM " + dialect.table2SQL(table);
        return new DBTableData(connection, query);
    }

    @Override
    public JSONObject createPreviewJSON(ArrayList<String> fields, ICubeDataLoader loader, long userId) throws Exception {
        JSONObject jo = new JSONObject();
        EmbeddedTableData emTableData = null;
        DataModel dm = null;
        try {
            emTableData = EmbeddedTableData.embedify(createPreviewTableData(), null, BIBaseConstant.PREVIEW_COUNT);
            dm = emTableData.createDataModel(null);
            JSONArray fieldNames = new JSONArray();
            JSONArray values = new JSONArray();
            jo.put(BIJSONConstant.JSON_KEYS.FIELDS, fieldNames);
            jo.put(BIJSONConstant.JSON_KEYS.VALUE, values);
            int colLen = dm.getColumnCount();
            int rolLen = Math.min(dm.getRowCount(), BIBaseConstant.PREVIEW_COUNT);

            for (int col = 0; col < colLen; col++) {
                String name = dm.getColumnName(col);
                if (!fields.isEmpty() && !fields.contains(name)) {
                    continue;
                }
                fieldNames.put(name);
                JSONArray value = new JSONArray();
                values.put(value);
                for (int row = 0; row < rolLen; row++) {
                    boolean isString = false;
                    if (getFields().containsKey(name) && getFields().get(name).getFieldType() == DBConstant.COLUMN.STRING) {
                        isString = true;
                    }
                    Object val = dm.getValueAt(row, col);
                    value.put((isString && val == null) ? "" : val);
                }
            }
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), "table preview failed!");
            return jo;
        } finally {
            if (null != dm) {
                dm.release();
            }
            if (null != emTableData) {
                emTableData.clear();
            }
        }

        return jo;
    }

    @Override
    public TableData createTableData(List<String> fields, ICubeDataLoader loader, long userId) throws Exception {
        Connection connection = BIConnectionManager.getInstance().getConnection(dbName);
        java.sql.Connection conn = connection.createConnection();
        Dialect dialect = DialectFactory.generateDialect(conn);
        Table table = new Table(BIConnectionManager.getInstance().getSchema(dbName), tableName);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fields.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(dialect.column2SQL(fields.get(i)));
        }
        String query = "SELECT " + (fields == null || fields.isEmpty() ? "*" : sb.toString()) + " FROM " + dialect.table2SQL(table);
        return new DBTableData(connection, query);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(dbName).append(".").append(tableName);
        return sb.toString();
    }

    /**
     * 将Java对象转换成JSON对象
     *
     * @return json对象
     * @throws Exception
     */
    @Override
    public JSONObject createJSON() throws Exception {
        JSONObject jo = super.createJSON();
        jo.put("connection_name", dbName);
        jo.put("table_name", tableName);
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo, long userId) throws Exception {
        super.parseJSON(jo, userId);
        if (jo.has("connection_name")) {
            dbName = jo.getString("connection_name");
        }
        if (jo.has("table_name")) {
            tableName = jo.getString("table_name");
        }

    }

    @Override
    public void readXML(XMLableReader reader) {
        super.readXML(reader);
        if (reader.isAttr()) {
            if (ComparatorUtils.equals(reader.getTagName(), XML_TAG)) {
                this.dbName = reader.getAttrAsString("dbName", "");
                this.tableName = reader.getAttrAsString("tableName", "");
            }
        }
    }

    @Override
    public void writeXML(XMLPrintWriter writer) {
        writer.startTAG(XML_TAG);
        super.writeXML(writer);
        writer.attr("dbName", dbName).attr("tableName", tableName);
        writer.end();
    }

    public Connection getConnection() {
        return DatasourceManager.getInstance().getConnection(dbName);
    }

    // TODO: 2016/11/9 判断表的字段和数据库中的能否对得上
    @Override
    public boolean canExecute() throws Exception {
        try {
            getConnection().testConnection();
        } catch (Exception e) {
            return false;
        }
//        List<ICubeFieldSource> fields = new ArrayList<ICubeFieldSource>(getFacetFields(null));
//        return testSQL(getConnection(), getSqlString(fields));
        return true;
    }

    protected String getSqlString(List<ICubeFieldSource> fields) throws Exception {
        Connection connection = BIConnectionManager.getInstance().getConnection(dbName);
        java.sql.Connection conn = connection.createConnection();
        Dialect dialect = DialectFactory.generateDialect(conn);
        Table table = new Table(BIConnectionManager.getInstance().getSchema(dbName), tableName);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fields.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(dialect.column2SQL(fields.get(i).getFieldName()));
        }
        String query = "SELECT " + (fields == null || fields.isEmpty() ? "*" : sb.toString()) + " FROM " + dialect.table2SQL(table);
        return query;
    }

    protected boolean testSQL(com.fr.data.impl.Connection connection, String SQL) throws Exception {
        SqlSettedStatement sqlStatement = new SqlSettedStatement(connection);
        sqlStatement.setSql(SQL);
        return DBQueryExecutor.getInstance().testSQL(sqlStatement);
    }

    @Override
    public boolean hasAbsentFields() {
        Map<String, ICubeFieldSource> originalFields = getFields();
        Map<String, ICubeFieldSource> persistFields = getFieldFromPersistentTable();
        boolean isFieldAbsent=false;
        for (String fieldName : originalFields.keySet()) {
            if (!persistFields.containsKey(fieldName)||!persistFields.get(fieldName).equals(originalFields.get(fieldName))){
                BILoggerFactory.getLogger(this.getClass()).error("The field the name is:" + fieldName + " is absent in table:" + getTableName() + " table ID:" + this.getSourceID());
                isFieldAbsent=true;
            }
        }
        return isFieldAbsent;
    }
}
