package com.fr.bi.conf.data.source.operator.add.selfrelation;

import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.conf.data.source.operator.create.AbstractCreateTableETLOperator;
import com.fr.bi.stable.constant.DBConstant;
import com.fr.bi.stable.data.db.IPersistentTable;
import com.fr.bi.stable.data.db.PersistentField;
import com.fr.bi.stable.utils.BIDBUtils;
import com.fr.general.ComparatorUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.stable.xml.XMLPrintWriter;
import com.fr.stable.xml.XMLableReader;

import java.security.MessageDigest;
import java.sql.Types;
import java.util.*;

/**
 * Created by GUY on 2015/3/5.
 */
public abstract class AbstractFieldUnionRelationOperator extends AbstractCreateTableETLOperator {
    private static final long serialVersionUID = -4723723974508198197L;
    @BICoreField
    protected LinkedHashMap<String, Integer> fields = new LinkedHashMap<String, Integer>();
    @BICoreField
    protected String idFieldName;
    @BICoreField
    protected List<String> showFields = new ArrayList<String>();

    @BICoreField
    protected int columnType;
    @BICoreField
    protected String fieldName;

    AbstractFieldUnionRelationOperator(long userId) {
        super(userId);
    }

    AbstractFieldUnionRelationOperator() {
        super();
    }


    protected void readFields(XMLableReader reader) {
        if (ComparatorUtils.equals(reader.getTagName(), "floor")) {
            String floorName = reader.getAttrAsString("name", "");
            int length = reader.getAttrAsInt("length", 0);
            fields.put(floorName, length);
        }
        if (ComparatorUtils.equals(reader.getTagName(), "showfield")) {
            String field = reader.getAttrAsString("name", "");
            showFields.add(field);
        }
    }

    protected void writeFields(XMLPrintWriter writer) {
        Iterator<String> floorIterator = this.fields.keySet().iterator();
        while (floorIterator.hasNext()) {
            writer.startTAG("floor");
            String floorName = floorIterator.next();
            writer.attr("name", floorName);
            writer.end();
        }
        for (String s : showFields) {
            writer.startTAG("showfield");
            writer.attr("name", s);
            writer.end();
        }

    }

    /**
     * 将JSON对象转换成java对象
     *
     * @param jo json对象
     * @throws Exception 报错
     */
    @Override
    public void parseJSON(JSONObject jo) throws Exception {
        if (jo.has("field_name")) {
            fieldName = jo.getString("field_name");
        }
        if (jo.has("field_type")) {
            columnType = jo.getInt("field_type");
        }
        if (jo.has("showfields")) {
            JSONArray ja = jo.getJSONArray("showfields");
            for (int i = 0; i < ja.length(); i++) {
                showFields.add(ja.getString(i));
            }
        }
    }

    @Override
    public JSONObject createJSON() throws Exception {
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        jo.put("showfields", ja);
        for (String s : showFields) {
            ja.put(s);
        }
        jo.put("field_type", columnType);
        jo.put("field_name", fieldName);
        return jo;
    }

    protected void digestFields(MessageDigest digest) {
        digest.update(idFieldName.getBytes());
        Iterator<Map.Entry<String, Integer>> iter = fields.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Integer> entry = iter.next();
            digest.update(entry.getKey().getBytes());
            digest.update(String.valueOf(entry.getValue()).getBytes());
        }
        for (String s : showFields) {
            digest.update(s.getBytes());
        }
    }

    @Override
    public IPersistentTable getBITable(IPersistentTable[] tables) {
        IPersistentTable persistentTable = getBITable();
        Iterator<Map.Entry<String, Integer>> it;
        for (IPersistentTable t : tables) {
            int type = DBConstant.CLASS.INTEGER;
            if (t.getField(idFieldName) != null) {
                type = t.getField(idFieldName).getBIType();
            }
            for (PersistentField field : t.getFieldList()) {
                persistentTable.addColumn(field);
            }
            for (String s : showFields) {
                it = fields.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Integer> entry = it.next();
                    persistentTable.addColumn(new UnionRelationPersistentField(s + "-" + entry.getKey(), BIDBUtils.biTypeToSql(type), entry.getValue()));
                }
            }
        }
        return persistentTable;
    }

    @Override
    public void readXML(XMLableReader reader) {
        super.readXML(reader);
        if (reader.isAttr()) {
            fieldName = reader.getAttrAsString("field_name", "");
            this.columnType = reader.getAttrAsInt("column_type", 0);
//            md5 = reader.getAttrAsString("md5", StringUtils.EMPTY);
        }
    }

    @Override
    public void writeXML(XMLPrintWriter writer) {
        super.writeXML(writer);
        writer.attr("field_name", fieldName);
        writer.attr("column_type", columnType);
    }
}