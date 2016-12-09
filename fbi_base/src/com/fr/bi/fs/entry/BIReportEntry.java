package com.fr.bi.fs.entry;

import com.fr.bi.fs.BIDAOUtils;
import com.fr.bi.fs.BIReportNode;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.data.core.db.dml.Table;
import com.fr.data.core.db.tableObject.AbstractTableObject;
import com.fr.data.core.db.tableObject.ColumnSize;
import com.fr.data.dao.CommonFieldColumnMapper;
import com.fr.data.dao.FieldColumnMapper;
import com.fr.data.dao.ObjectTableMapper;
import com.fr.data.dao.PrimaryKeyFCMapper;
import com.fr.fs.web.platform.entry.BaseEntry;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;

import java.sql.Types;
import java.util.List;

/**
 * Created by Young's on 2016/6/6.
 */
public class BIReportEntry extends BaseEntry {
    private static final int HASH = 31;
    public static final String TYPE_PREFIX = EntryConstants.BIREPORT + "";
    public static final String TABLE_NAME = "FR_BIREPORTENTRY";
    public static final String REPORTNAME = "reportName";
    public static final String COVERID = "mobileCoverId";
    public static final int COVERIDTYPE = Types.VARCHAR;
    private static final int COLUMNSIZE_ID = 10;
    private static final int COLUMNSIZE_STRING = 255;
    private static final int COLUMNSIZE_BOOLEAN = 1;
    private static final int COLUMNSIZE_INTEGER = 5;

    private long reportId;
    private long createBy;
    private String reportName = null;
    private String mobileCoverId;
    private boolean systemReport = true;

    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(long createBy) {
        this.createBy = createBy;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getMobileCoverId() {
        return mobileCoverId;
    }
    @Override
    public void setMobileCoverId(String mobileCoverId) {
        this.mobileCoverId = mobileCoverId;
    }

    public void setSystemReport(boolean systemReport){
        this.systemReport=systemReport;
    }

    public boolean isSystemReport(){
        return systemReport;
    }


    public static final ObjectTableMapper TABLE_MAPPER = new ObjectTableMapper(
            BIReportEntry.class, new Table(TABLE_NAME),
            new FieldColumnMapper[] {
                    new PrimaryKeyFCMapper("id", BaseEntry.IDTYPE, new ColumnSize(COLUMNSIZE_ID)),
                    new CommonFieldColumnMapper("parentId", BaseEntry.PARENTIDTYPE,
                            BaseEntry.PARENTID, new ColumnSize(COLUMNSIZE_ID), false),
                    new CommonFieldColumnMapper("displayName", BaseEntry.DISPLAYNAMETYPE,
                            BaseEntry.DISPLAYNAME, new ColumnSize(COLUMNSIZE_STRING), false),
                    new CommonFieldColumnMapper("reportName", Types.VARCHAR,
                            REPORTNAME, new ColumnSize(COLUMNSIZE_STRING), false),
                    new CommonFieldColumnMapper("reportId", Types.BIGINT,
                            "reportId", new ColumnSize(COLUMNSIZE_ID), false),
                    new CommonFieldColumnMapper("systemReport", Types.BOOLEAN,
                            "systemReport", new ColumnSize(COLUMNSIZE_BOOLEAN), false),
                    new CommonFieldColumnMapper("description", BaseEntry.DESCRIPTIONTYPE,
                            BaseEntry.DESCRIPTION, new ColumnSize(COLUMNSIZE_STRING), true),
                    new CommonFieldColumnMapper("sortindex", BaseEntry.SORTINDEXTYPE,
                            BaseEntry.SORTINDEX, new ColumnSize(10), true),
                    new CommonFieldColumnMapper("mobileCoverId", COVERIDTYPE,
                            COVERID, new ColumnSize(50), true),
                    new CommonFieldColumnMapper("createBy", Types.BIGINT,
                            "createBy", new ColumnSize(COLUMNSIZE_ID), false)
            }, AbstractTableObject.CHECK_TABLE_EXSIT_ON_TABLENAME);


    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);
        if(jo.has("reportId")){
            this.reportId = jo.getLong("reportId");
        }
        if(jo.has("reportName")){
            this.reportName = jo.getString("reportName");
        }
        if(jo.has("createBy")) {
            this.createBy = jo.getLong("createBy");
        }
    }

    public JSONObject createJSONConfig() throws JSONException {
        JSONObject jo = createShowJSONConfig();
        //这边的reportName还是不要用保存的了，因为用户会修改的
        String reportName = this.reportName;
        try {
            List<BIReportNode> nodeList = BIDAOUtils.getBIDAOManager().findByUserID(this.createBy);
            for(int i = 0; i < nodeList.size(); i++){
                BIReportNode node = nodeList.get(i);
                if(node.getId() == this.reportId) {
                    reportName = node.getReportName();
                    break;
                }
            }
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
        jo.put("reportName", reportName);
        return jo;
    }

    public JSONObject createShowJSONConfig() throws JSONException {
        JSONObject jo = super.createJSONConfig();
        jo.put("mobileCoverId", mobileCoverId);
        jo.put("nodeicon", "bi");
        jo.put("reportId", this.getReportId());
        jo.put("createBy", this.getCreateBy());
        jo.put("bilink", "?op=fr_bi&cmd=bi_init&id=" + this.getReportId()
                + "&show=_bi_show_&createBy=" + this.getCreateBy());
        return jo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BIReportEntry that = (BIReportEntry) o;

        if (reportId != that.reportId) return false;
        if (createBy != that.createBy) return false;
        if (reportName != null ? !reportName.equals(that.reportName) : that.reportName != null) return false;
        return mobileCoverId != null ? mobileCoverId.equals(that.mobileCoverId) : that.mobileCoverId == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (reportId ^ (reportId >>> 32));
        result = 31 * result + (int) (createBy ^ (createBy >>> 32));
        result = 31 * result + (reportName != null ? reportName.hashCode() : 0);
        result = 31 * result + (mobileCoverId != null ? mobileCoverId.hashCode() : 0);
        return result;
    }

    @Override
    public String getTypePrefix() {
        return TYPE_PREFIX;
    }

    @Override
    public int getEntryType() {
        return EntryConstants.BIREPORT;
    }
}
