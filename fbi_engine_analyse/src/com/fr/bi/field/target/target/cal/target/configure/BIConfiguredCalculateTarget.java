package com.fr.bi.field.target.target.cal.target.configure;

import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.field.target.target.BIAbstractTarget;
import com.fr.bi.field.target.target.cal.BICalculateTarget;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.utils.BITravalUtils;
import com.fr.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public abstract class BIConfiguredCalculateTarget extends BICalculateTarget {

    /**
     * 计算使用的指标
     */
    @BICoreField
    private String target_id;

    /**
     * 从第几个分组位置开始算, 默认为0;
     */
    private int start_group = BIReportConstant.TARGET_TYPE.CAL_POSITION.ALL;

    @Override
    public void parseJSON(JSONObject jo, long userId) throws Exception {
        super.parseJSON(jo, userId);
        if (jo.has("_src")) {
            JSONObject srcJo = jo.optJSONObject("_src");
            this.target_id = srcJo.optJSONObject("expression").optJSONArray("ids").optString(0);
        }
        if (jo.has("type")) {
            switch (jo.optInt("type")) {
                case BIReportConstant.TARGET_TYPE.RANK_IN_GROUP:
                    this.start_group = BIReportConstant.TARGET_TYPE.CAL_POSITION.INGROUP;
                    break;
                case BIReportConstant.TARGET_TYPE.SUM_OF_ABOVE_IN_GROUP:
                    this.start_group = BIReportConstant.TARGET_TYPE.CAL_POSITION.INGROUP;
                    break;
                case BIReportConstant.TARGET_TYPE.SUM_OF_ALL_IN_GROUP:
                    this.start_group = BIReportConstant.TARGET_TYPE.CAL_POSITION.INGROUP;
                    break;
                case BIReportConstant.TARGET_TYPE.MONTH_ON_MONTH_RATE:
                    this.start_group = BIReportConstant.TARGET_TYPE.CAL_POSITION.INGROUP;
                    break;
                case BIReportConstant.TARGET_TYPE.MONTH_ON_MONTH_VALUE:
                    this.start_group = BIReportConstant.TARGET_TYPE.CAL_POSITION.INGROUP;
                    break;

            }

        }
    }

    protected String getCalTargetName() {
        return target_id;
    }

    public int getStart_group() {
        return start_group;
    }

    @Override
    public List<BIAbstractTarget> createCalculateUseTarget(BIAbstractTarget[] targets) {
        List<BIAbstractTarget> list = new ArrayList<BIAbstractTarget>();
        BIAbstractTarget target = BITravalUtils.getTargetByName(target_id, targets);
        if (target != null) {
            list.add(target);
        }
        return list;
    }

    @Override
    public boolean calculateAllPage() {
        return start_group == BIReportConstant.TARGET_TYPE.CAL_POSITION.ALL;
    }

    public void setStart_group(int start_group) {
        this.start_group = start_group;
    }
}