package com.fr.bi.cal.analyze.report.report.widget.chart.export.item;

import com.fr.bi.cal.analyze.report.report.widget.chart.export.style.BITableItemStyle;
import com.fr.bi.cal.analyze.report.report.widget.chart.export.style.ITableStyle;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;

/**
 * Created by Kary on 2017/2/13.
 */
public class BITableHeader implements ITableHeader {
    private String text;
    private String dID;
    private ITableStyle styles;
    private boolean isUsed;
    private boolean isSum;

    public BITableHeader() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getdID() {
        return dID;
    }

    public void setdID(String dID) {
        this.dID = dID;
    }

    public ITableStyle getStyles() {
        return styles;
    }

    public void setStyles(ITableStyle style) {
        this.styles = style;
    }

    @Override
    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    @Override
    public JSONObject createJSON() throws Exception {
        JSONObject jo = new JSONObject();
        jo.put("dId", getdID());
        jo.put("styles", null != getStyles() ? getStyles().createJSON() : "");
        jo.put("text", getText());
        jo.put("isSum", isSum);
        return jo;
    }

    @Override
    public boolean isSum() {
        return isSum;
    }

    @Override
    public void setSum(boolean sum) {
        isSum = sum;
    }

    @Override
    public void parseJson(JSONObject json) throws JSONException {
        if (json.has("dId")) {
            setdID(json.getString("dId"));
        }
        if (json.has("styles")) {
            BITableItemStyle styles = new BITableItemStyle();
            styles.parse(json.getJSONObject("styles"));
            setStyles(styles);
        }
        if (json.has("text")) {
            setText(json.getString("text"));
        }
        isSum = json.optBoolean("isSum", false);
    }
}
