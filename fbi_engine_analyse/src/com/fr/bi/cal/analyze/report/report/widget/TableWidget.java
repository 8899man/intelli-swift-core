package com.fr.bi.cal.analyze.report.report.widget;


import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.cal.analyze.cal.result.BIComplexExecutData;
import com.fr.bi.cal.analyze.cal.result.ComplexExpander;
import com.fr.bi.cal.analyze.cal.result.CrossExpander;
import com.fr.bi.cal.analyze.cal.table.PolyCubeECBlock;
import com.fr.bi.cal.analyze.executor.BIEngineExecutor;
import com.fr.bi.cal.analyze.executor.paging.PagingFactory;
import com.fr.bi.cal.analyze.executor.table.*;
import com.fr.bi.cal.analyze.report.report.widget.chart.excelExport.layout.table.basic.TableDataForExport;
import com.fr.bi.cal.analyze.report.report.widget.chart.excelExport.layout.table.operation.TableDataBuilder;
import com.fr.bi.cal.analyze.report.report.widget.table.BITableReportSetting;
import com.fr.bi.cal.analyze.session.BISession;
import com.fr.bi.common.persistent.xml.BIIgnoreField;
import com.fr.bi.conf.report.style.DetailChartSetting;
import com.fr.bi.conf.report.widget.field.dimension.BIDimension;
import com.fr.bi.conf.session.BISessionProvider;
import com.fr.bi.field.target.target.BISummaryTarget;
import com.fr.bi.field.target.target.cal.target.configure.BIConfiguredCalculateTarget;
import com.fr.bi.field.target.target.cal.target.configure.BIPeriodConfiguredCalculateTarget;
import com.fr.bi.stable.constant.BIJSONConstant;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.report.key.TargetGettingKey;
import com.fr.bi.stable.utils.BITravalUtils;
import com.fr.json.JSONArray;
import com.fr.json.JSONObject;
import com.fr.report.poly.TemplateBlock;
import com.fr.web.core.SessionDealWith;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * BI表格控件
 *
 * @author Daniel-pc
 */
public class TableWidget extends BISummaryWidget {
    /**
     * 保存列字段等内容
     */
    @BICoreField
    private BITableReportSetting data = new BITableReportSetting();

    private int[] pageSpinner = new int[5];

    private int operator = BIReportConstant.TABLE_PAGE_OPERATOR.REFRESH;

    private int table_type = BIReportConstant.TABLE_WIDGET.GROUP_TYPE;

    @Override
    public void setPageSpinner(int index, int value) {
        this.pageSpinner[index] = value;
    }

    @BIIgnoreField
    private transient BIDimension[] usedDimension;
    @BIIgnoreField
    private transient BISummaryTarget[] usedTargets;

    private DetailChartSetting settings = new DetailChartSetting();


    @Override
    public BIDimension[] getViewDimensions() {
        if (usedDimension != null) {
            return usedDimension;
        }
        BIDimension[] dimensions = getDimensions();
        if (data != null) {
            String[] array = data.getRow();
            ArrayList<BIDimension> usedDimensions = new ArrayList<BIDimension>();
            for (int i = 0; i < array.length; i++) {
                BIDimension dimension = BITravalUtils.getTargetByName(array[i], dimensions);
                if (dimension.isUsed()) {
                    usedDimensions.add(dimension);
                }
            }
            dimensions = usedDimensions.toArray(new BIDimension[usedDimensions.size()]);
        }
        usedDimension = dimensions;
        return dimensions;
    }

    @Override
    public BISummaryTarget[] getViewTargets() {
        if (usedTargets != null) {
            return usedTargets;
        }
        BISummaryTarget[] targets = getTargets();
        if (data != null) {
            String[] array = data.getSummary();
            ArrayList<BISummaryTarget> usedTargets = new ArrayList<BISummaryTarget>();
            for (int i = 0; i < array.length; i++) {
                BISummaryTarget target = BITravalUtils.getTargetByName(array[i], targets);
                if (target.isUsed()) {
                    usedTargets.add(target);
                }
            }
            targets = usedTargets.toArray(new BISummaryTarget[usedTargets.size()]);
        }
        usedTargets = targets;
        return targets;
    }

    public BIDimension[] getViewTopDimensions() {
        BIDimension[] dimensions = getDimensions();
        if (data != null) {
            String[] array = data.getColumn();
            ArrayList<BIDimension> usedDimensions = new ArrayList<BIDimension>();
            for (int i = 0; i < array.length; i++) {
                BIDimension dimension = BITravalUtils.getTargetByName(array[i], dimensions);
                if (dimension.isUsed()) {
                    usedDimensions.add(dimension);
                }
            }
            return usedDimensions.toArray(new BIDimension[usedDimensions.size()]);
        }
        return dimensions;
    }


    public boolean useRealData() {
        return data.useRealData();
    }


    /**
     * 有无编号
     *
     * @return 编号
     */
    @Override
    public int isOrder() {
        return data.isOrder();
    }

    public BIEngineExecutor getExecutor(BISession session) {
        boolean calculateTarget = targetSort != null || !targetFilterMap.isEmpty();
        CrossExpander expander = new CrossExpander(complexExpander.getXExpander(0), complexExpander.getYExpander(0));
        boolean hasTarget = calculateTarget || getViewTargets().length > 0;
        if (this.table_type == BIReportConstant.TABLE_WIDGET.COMPLEX_TYPE) {
            return createComplexExecutor(session, hasTarget, complexExpander, expander);
        } else {
            return createNormalExecutor(session, hasTarget, getViewDimensions(), getViewTopDimensions(), expander);
        }
    }

    /**
     * 返回费复杂报表时的excute
     *
     * @param hasTarget 是否需要指标
     * @return 表格处理excute
     */
    public BIEngineExecutor createComplexExecutor(BISession session, boolean hasTarget, ComplexExpander complexExpander, CrossExpander expander) {
        BIEngineExecutor executor;
        int summaryLen = getViewTargets().length;
        ArrayList<ArrayList<String>> row = data.getComplex_x_dimension();
        ArrayList<ArrayList<String>> column = data.getComplex_y_dimension();
        BIComplexExecutData rowData = new BIComplexExecutData(row, dimensions);
        BIComplexExecutData columnData = new BIComplexExecutData(column, dimensions);

        if (rowData.getDimensionArrayLength() <= 1 && columnData.getDimensionArrayLength() <= 1) {
            return this.createNormalExecutor(session, hasTarget, rowData.getDimensionArray(0), columnData.getDimensionArray(0), expander);
        }
        boolean b0 = !column.isEmpty() && row.isEmpty() && hasTarget;
        boolean b1 = !column.isEmpty() && row.isEmpty() && summaryLen == 0;
        boolean b2 = !row.isEmpty() && column.isEmpty() && hasTarget;
        boolean b3 = !row.isEmpty() && column.isEmpty() && summaryLen == 0;
        if (b0) {
            executor = new ComplexHorGroupExecutor(this, PagingFactory.createPaging(PagingFactory.PAGE_PER_GROUP_20, operator), column, session, complexExpander);
        } else if (b1) {
            executor = new ComplexHorGroupNoneExecutor(this, PagingFactory.createPaging(PagingFactory.PAGE_PER_GROUP_20, operator), column, session, complexExpander);
        } else if (b2) {
            executor = new ComplexGroupExecutor(this, PagingFactory.createPaging(PagingFactory.PAGE_PER_GROUP_20, operator), row, session, complexExpander);
        } else if (b3) {
            executor = new ComplexGroupNoneExecutor(this, PagingFactory.createPaging(PagingFactory.PAGE_PER_GROUP_20, operator), row, session, complexExpander);
        } else {
            executor = new ComplexCrossExecutor(this, PagingFactory.createPaging(PagingFactory.PAGE_PER_GROUP_20, operator), row, column, session, complexExpander);
        }
        return executor;
    }


    private BIEngineExecutor createNormalExecutor(BISession session, boolean hasTarget, BIDimension[] usedRows, BIDimension[] usedColumn, CrossExpander expander) {
        BIEngineExecutor executor;
        int summaryLen = getViewTargets().length;
        boolean b0 = usedColumn.length > 0 && usedRows.length == 0 && hasTarget;
        boolean b1 = usedColumn.length >= 0 && usedRows.length == 0 && summaryLen == 0;
        boolean b2 = usedRows.length >= 0 && usedColumn.length == 0;
        boolean b3 = usedRows.length >= 0 && usedColumn.length == 0 && summaryLen == 0;
        if (b0) {
            executor = new HorGroupExecutor(this, PagingFactory.createPaging(PagingFactory.PAGE_PER_GROUP_20, operator), session, expander);
        } else if (b1) {
            executor = new HorGroupNoneTargetExecutor(this, PagingFactory.createPaging(PagingFactory.PAGE_PER_GROUP_20, operator), session, expander);
        } else if (b2) {
            executor = new GroupExecutor(this, PagingFactory.createPaging(PagingFactory.PAGE_PER_GROUP_20, operator), session, expander);
        } else if (b3) {
            executor = new GroupNoneTargetExecutor(this, PagingFactory.createPaging(PagingFactory.PAGE_PER_GROUP_20, operator), session, expander);
        } else {
            executor = new CrossExecutor(this, usedRows, usedColumn, PagingFactory.createPaging(PagingFactory.PAGE_PER_GROUP_20, operator), session, expander);
        }

        return executor;
    }

    @Override
    public JSONObject createDataJSON(BISessionProvider session) throws Exception {
        BIEngineExecutor executor = getExecutor((BISession) session);
        JSONObject jo = new JSONObject();
        if (executor != null) {
            jo.put("data", executor.createJSONObject());
        }
        JSONArray ja = new JSONArray();
        for (int i : pageSpinner) {
            ja.put(i);
        }
        jo.put("page", ja);
        return jo;
    }


    /**
     * 创建表格的Block
     */
    @Override
    protected TemplateBlock createBIBlock(BISession session) {
        return new PolyCubeECBlock(this, session, operator);
    }

    @Override
    public void parseJSON(JSONObject jo, long userId) throws Exception {
        super.parseJSON(jo, userId);
        if (jo.has("view")) {
            data.parseJSON(jo);
        }

        if (jo.has("type")) {
            table_type = jo.optInt("type");
        }

        if (jo.has("page")) {
            this.operator = jo.getInt("page");
        }
        if (jo.has(BIJSONConstant.JSON_KEYS.EXPANDER)) {
            parsExpander(jo);
        }
        if (jo.has("settings")) {
            settings = new DetailChartSetting();
            settings.parseJSON(jo);
        }
        changeCalculateTargetStartGroup();


    }

    public void setComplexExpander(ComplexExpander complexExpander) {
        this.complexExpander = complexExpander;
    }

    private void parsExpander(JSONObject jo) throws Exception {
        complexExpander = new ComplexExpander(data.getColumn().length, data.getRow().length);
        complexExpander.parseJSON(jo.getJSONObject(BIJSONConstant.JSON_KEYS.EXPANDER));
        if (jo.has(BIJSONConstant.JSON_KEYS.CLICKEDVALUE)) {
            JSONArray ja = jo.getJSONArray(BIJSONConstant.JSON_KEYS.CLICKEDVALUE);
            clickValue = new Object[ja.length()];
            for (int i = 0; i < ja.length(); i++) {
                clickValue[i] = ja.getString(i);
            }
        }
    }

    public DetailChartSetting getChartSetting() {
        return settings;
    }

    @Override
    public int getType() {
        return BIReportConstant.WIDGET.TABLE;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public boolean hasVerticalPrePage() {
        return pageSpinner[BIReportConstant.TABLE_PAGE.VERTICAL_PRE] > 0;
    }

    public boolean hasVerticalNextPage() {
        return pageSpinner[BIReportConstant.TABLE_PAGE.VERTICAL_NEXT] > 0;
    }

    public boolean hasHorizonPrePage() {
        return pageSpinner[BIReportConstant.TABLE_PAGE.HORIZON_PRE] > 0;
    }

    public boolean hasHorizonNextPage() {
        return pageSpinner[BIReportConstant.TABLE_PAGE.HORIZON_NEXT] > 0;
    }

    private void changeCalculateTargetStartGroup() {
        boolean changed = false;
        BISummaryTarget[] targets = getTargets();
        if (this.getViewDimensions().length <= 1 && this.getViewDimensions().length != 0) {
            for (int i = 0; i < targets.length; i++) {
                BISummaryTarget target = targets[i];
                if (target instanceof BIConfiguredCalculateTarget && !(target instanceof BIPeriodConfiguredCalculateTarget)) {
                    BIConfiguredCalculateTarget configuredCalculateTarget = (BIConfiguredCalculateTarget) target;
                    if ((configuredCalculateTarget.getStart_group() == 1)) {
                        configuredCalculateTarget.setStart_group(0);
                        changed = true;
                    }
                }
            }
            if (changed) {
                Map<String, TargetGettingKey> targetMap = new ConcurrentHashMap<String, TargetGettingKey>();
                for (int i = 0; i < targets.length; i++) {
                    targets[i].setTargetMap(targetMap);
                    targetMap.put(targets[i].getValue(), targets[i].createSummaryCalculator().createTargetGettingKey());
                }
            }
        }
    }

    @Override
    public void reSetDetailTarget() {
    }

    public JSONObject getPostOptions(String sessionId) throws Exception {
        JSONObject dataJSON = this.createDataJSON((BISessionProvider) SessionDealWith.getSessionIDInfor(sessionId));
        TableDataBuilder builder = new TableDataBuilder(this, dataJSON);
        TableDataForExport tableDataForExport = builder.buildTableData();
        return tableDataForExport.createJsonObject();
    }


}