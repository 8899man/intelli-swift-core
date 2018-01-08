package com.fr.bi.fs;

import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.data.dao.DAOBean;
import com.fr.fs.web.ShowType;
import com.fr.fs.web.platform.entry.Entry;
import com.fr.general.ComparatorUtils;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;
import com.fr.stable.xml.XMLPrintWriter;
import com.fr.stable.xml.XMLableReader;

import java.util.Date;

/**
 * 保存BIreport的Node
 *
 * @author Daniel-pc
 */
public class BIReportNode extends DAOBean implements Entry {
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
	 */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((createtime == null) ? 0 : createtime.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((parentid == null ? 0 : parentid.hashCode()));
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((reportname == null) ? 0 : reportname.hashCode());
        result = prime * result + (int) (templateid ^ (templateid >>> 32));
        result = prime * result + (int) (userid ^ (userid >>> 32));
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return equalsReportNode((BIReportNode) obj);
    }


    public boolean equalsReportNode(BIReportNode other) {
        if (createtime == null) {
            if (other.createtime != null) {
                return false;
            }
        } else if (!ComparatorUtils.equals(createtime, other.createtime)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!ComparatorUtils.equals(description, other.description)) {
            return false;
        }
        if (parentid != other.parentid) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!ComparatorUtils.equals(path, other.path)) {
            return false;
        }
        if (reportname == null) {
            if (other.reportname != null) {
                return false;
            }
        } else if (!ComparatorUtils.equals(reportname, other.reportname)) {
            return false;
        }
        if (templateid != other.templateid) {
            return false;
        }
        if (userid != other.userid) {
            return false;
        }
        if (username == null) {
            if (other.username != null) {
                return false;
            }
        } else if (!ComparatorUtils.equals(username, other.username)) {
            return false;
        }
        return true;
    }

    /**
     *
     */
    private static final long serialVersionUID = -3662026856074945968L;
    private String path;
    private long userid;
    private String username;
    private long templateid;
    private String reportname;
    private String parentid;
    //创建时间
    private Date createtime;
    //描述内容
    private String description;
    private Date modifytime;
    //标识是否需要挂载的状态
    private int status = BIReportConstant.REPORT_STATUS.NORMAL;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getUserId() {
        return userid;
    }

    public void setUserId(long userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getTemplateid() {
        return templateid;
    }

    public void setTemplateid(long templateid) {
        this.templateid = templateid;
    }

    public String getReportName() {
        return reportname;
    }

    public void setReportName(String reportname) {
        this.reportname = reportname;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastModifyTime() {
        return modifytime;
    }

    public void setLastModifyTime(Date modifytime) {
        this.modifytime = modifytime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 默认构造函数
     */
    public BIReportNode() {
    }

    /**
     * 构造
     *
     * @param userId
     * @param parentId
     * @param reportName
     * @param path
     */
    public BIReportNode(long userId, String parentId, String reportName, String path, String description) {
        this.path = path;
        this.userid = userId;
        this.parentid = parentId;
        this.createtime = new Date();
        this.reportname = reportName;
        this.username = StringUtils.EMPTY;
        this.description = description;
        updateLastModifyTime();
    }

    /**
     * 构造
     *
     * @param userId
     * @param reportName
     * @param path
     */
    public BIReportNode(long userId, String reportName, String path, String description) {
        this.path = path;
        this.userid = userId;
        this.createtime = new Date();
        this.reportname = reportName;
        this.description = description;
        this.username = StringUtils.EMPTY;
        updateLastModifyTime();
    }

    /**
     * 构造
     *
     * @param id
     */
    public BIReportNode(long id) {
        this.id = id;
    }


    @Override
    protected int hashCode4Properties() {
        return 0;
    }

    /**
     * eqauls比较
     */
    @Override
    public boolean equals4Properties(Object obj) {
        if (!(obj instanceof BIReportNode)) {
            return false;
        }
        //lastModify时间就不要比较了
        return super.equals4NoPersistence(obj);
    }

    /**
     * 更新最后修改时间
     */
    public void updateLastModifyTime() {
        this.modifytime = new Date();
    }

    @Override
    public long getParentId() {
        return 0;
    }

    @Override
    public int getEntryType() {
        return 0;
    }

    @Override
    public ShowType getShowType() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return reportname;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getSortindex() {
        return 0;
    }

    @Override
    public void setSortindex(long l) {

    }

    @Override
    public void setMobileCoverId(String s) {

    }

    @Override
    public JSONObject createShowJSONConfig() throws JSONException {
        return createJSONConfig();
    }

    @Override
    public JSONObject createJSONConfig() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put("buildUrl", this.buildUrl());
        jo.put("bih5link", this.getH5Link());
        jo.put("id", id);
        jo.put("pId", this.parentid);
        jo.put("text", this.reportname);
        jo.put("lastModify", modifytime.getTime());
        jo.put("responed", status);
        jo.put("value", id);
        jo.put("createBy", this.userid);
        jo.put("description", this.description);
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jsonObject) throws JSONException {

    }

    /* (non-Javadoc)
     * @see com.fr.fs.adhoc.ADHOCReportNode#buildUrl()
     */
    protected String buildUrl() {
        StringBuffer sb = new StringBuffer();
        sb.append("?op=fr_bi&cmd=bi_init&id=").append(this.id);
        sb.append("&createBy=").append(this.getUserId());
        return sb.toString();
    }

    private String getH5Link() {
        StringBuffer sb = new StringBuffer();
        sb.append("?op=fr_bi_h5&cmd=h5_init&id=").append(this.id);
        sb.append("&createBy=").append(this.getUserId());
        return sb.toString();
    }

    @Override
    public void readXML(XMLableReader xmLableReader) {

    }

    @Override
    public void writeXML(XMLPrintWriter xmlPrintWriter) {

    }

    @Override
    public BIReportNode clone() throws CloneNotSupportedException {
        BIReportNode node = (BIReportNode) super.clone();
        node.path = path;
        node.userid = userid;
        node.username = username;
        node.templateid = templateid;
        node.reportname = reportname;
        node.parentid = parentid;
        node.createtime = new Date(createtime.getTime());
        node.description = description;
        node.modifytime = new Date(modifytime.getTime());
        node.status = status;
        return node;
    }

}
