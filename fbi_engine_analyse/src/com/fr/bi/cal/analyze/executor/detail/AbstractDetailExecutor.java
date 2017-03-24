package com.fr.bi.cal.analyze.executor.detail;

import com.finebi.cube.api.ICubeTableService;
import com.finebi.cube.conf.field.BIBusinessField;
import com.finebi.cube.conf.field.BusinessField;
import com.finebi.cube.conf.table.BusinessTable;
import com.finebi.cube.relation.BITableRelation;
import com.finebi.cube.relation.BITableSourceRelation;
import com.fr.bi.cal.analyze.executor.BIAbstractExecutor;
import com.fr.bi.cal.analyze.executor.paging.Paging;
import com.fr.bi.cal.analyze.executor.utils.ExecutorUtils;
import com.fr.bi.cal.analyze.report.report.widget.BIDetailWidget;
import com.fr.bi.cal.analyze.session.BISession;
import com.fr.bi.cal.report.engine.CBBoxElement;
import com.fr.bi.cal.report.engine.CBCell;
import com.fr.bi.conf.report.style.BITableStyle;
import com.fr.bi.conf.report.style.ChartSetting;
import com.fr.bi.conf.report.style.TargetStyle;
import com.fr.bi.conf.report.widget.field.target.detailtarget.BIDetailTarget;
import com.fr.bi.conf.report.widget.field.target.filter.TargetFilter;
import com.fr.bi.field.BIAbstractTargetAndDimension;
import com.fr.bi.field.BIStyleTarget;
import com.fr.bi.field.dimension.calculator.NoneDimensionCalculator;
import com.fr.bi.field.target.detailtarget.field.BINumberDetailTarget;
import com.fr.bi.field.target.detailtarget.formula.BINumberFormulaDetailTarget;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.constant.CellConstant;
import com.fr.bi.stable.gvi.GVIUtils;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.result.DimensionCalculator;
import com.fr.bi.stable.utils.algorithem.BIComparatorUtils;
import com.fr.bi.util.BIConfUtils;
import com.fr.general.ComparatorUtils;
import com.fr.general.Inter;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by GUY on 2015/4/16.
 */
public abstract class AbstractDetailExecutor extends BIAbstractExecutor<JSONObject> {


    protected transient BusinessTable target;
    protected transient BIDetailTarget[] viewDimension;
    protected transient String[] sortTargets;
    private transient GroupValueIndex currentGvi;
    protected transient long userId;
    protected BIDetailWidget widget;

    public AbstractDetailExecutor(BIDetailWidget widget, Paging paging, BISession session) {
        super(widget, paging, session);
        this.target = widget.getTargetDimension();
        this.widget = widget;
        this.session = session;

        this.viewDimension = widget.getViewDimensions();
        this.sortTargets = widget.getSortTargets();
        this.userId = session.getUserId();
    }

    protected GroupValueIndex createDetailViewGvi() {
        if (currentGvi == null) {
            ICubeTableService ti = getLoader().getTableIndex(target.getTableSource());
            GroupValueIndex gvi = ti.getAllShowIndex();
            for (int i = 0; i < this.viewDimension.length; i++) {
                BIDetailTarget target = this.viewDimension[i];
                TargetFilter filterValue = target.getFilter();
                if (filterValue != null) {
                    BusinessField dataColumn = target.createColumnKey();
                    List<BITableRelation> simpleRelations = target.getRelationList(this.target, this.userId);
                    gvi = GVIUtils.AND(gvi, filterValue.createFilterIndex(new NoneDimensionCalculator(dataColumn, BIConfUtils.convert2TableSourceRelation(simpleRelations)), this.target, getLoader(), this.userId));
                }
            }
            Map<String, TargetFilter> filterMap = widget.getTargetFilterMap();
            for (Map.Entry<String, TargetFilter> entry : filterMap.entrySet()) {
                String targetId = entry.getKey();
                BIDetailTarget target = getTargetById(targetId);
                if (target != null) {
                    BusinessField dataColumn = target.createColumnKey();
                    List<BITableRelation> simpleRelations = target.getRelationList(this.target, this.userId);
                    gvi = GVIUtils.AND(gvi, entry.getValue().createFilterIndex(new NoneDimensionCalculator(dataColumn, BIConfUtils.convert2TableSourceRelation(simpleRelations)), this.target, getLoader(), this.userId));
                }
            }
            gvi = GVIUtils.AND(gvi,
                    widget.createFilterGVI(new DimensionCalculator[]{new NoneDimensionCalculator(new BIBusinessField(this.target, StringUtils.EMPTY),
                            new ArrayList<BITableSourceRelation>())}, this.target, getLoader(), this.userId));
            currentGvi = gvi;
        }
        return currentGvi;
    }

    private BIDetailTarget getTargetById(String id) {
        BIDetailTarget target = null;
        for (int i = 0; i < viewDimension.length; i++) {
            if (BIComparatorUtils.isExactlyEquals(viewDimension[i].getValue(), id)) {
                target = viewDimension[i];
            }
        }
        return target;
    }


    //创建一个数字格
    private void createNumberCellElement(StreamPagedIterator iter, int rowIndex, int row) {
        CBCell cell = new CBCell(rowIndex);
        cell.setColumn(0);
        cell.setRow(row);
        cell.setRowSpan(1);
        cell.setColumnSpan(1);
        cell.setCellGUIAttr(BITableStyle.getInstance().getCellAttr());
        cell.setStyle(BITableStyle.getInstance().getDimensionCellStyle(true, cell.getRow() % 2 == 1));
        List tcellList = new ArrayList();
        tcellList.add(cell);
        CBBoxElement cbox = new CBBoxElement(tcellList);
        cell.setBoxElement(cbox);
        iter.addCell(cell);
    }

    protected void fillOneLine(StreamPagedIterator iter, int row, Object[] ob, int rowNumber) {
        if (widget.isOrder() > 0) {
            createNumberCellElement(iter, rowNumber, row);
        }
        for (int i = 0; i < viewDimension.length; i++) {
            BIDetailTarget t = viewDimension[i];
            Object v = ob[i];
            v = viewDimension[i].createShowValue(v);
            ChartSetting chartSetting = null;
            int numLevel = BIReportConstant.TARGET_STYLE.NUM_LEVEL.NORMAL;
            if (t instanceof BINumberDetailTarget) {
                chartSetting = ((BINumberDetailTarget) viewDimension[i]).getChartSetting();
            }
            if (t instanceof BINumberFormulaDetailTarget) {
                chartSetting = ((BINumberFormulaDetailTarget) viewDimension[i]).getChartSetting();
            }
            if (chartSetting != null) {
                JSONObject settings = chartSetting.getSettings();
                numLevel = settings.optInt("num_level", BIReportConstant.TARGET_STYLE.NUM_LEVEL.NORMAL);
                v = ExecutorUtils.formatExtremeSumValue(v, numLevel);
            }

            CBCell cell = new CBCell(v == null ? NONEVALUE : v.toString());
            cell.setRow(row);
            cell.setColumn(i + widget.isOrder());
            cell.setRowSpan(1);
            cell.setColumnSpan(1);
            cell.setCellGUIAttr(BITableStyle.getInstance().getCellAttr());
            List cellList = new ArrayList();
            cellList.add(cell);
            //TODO CBBoxElement需要整合减少内存
            CBBoxElement cbox = new CBBoxElement(cellList);
            if (t.useHyperLink()) {
                cell.setNameHyperlinkGroup(t.createHyperLinkNameJavaScriptGroup(v));
            }
            if (t instanceof BINumberDetailTarget || t instanceof BINumberFormulaDetailTarget) {
                cell.setStyle(BITableStyle.getInstance().getNumberCellStyle(v, cell.getRow() % 2 == 1, t.useHyperLink(), numLevel == BIReportConstant.TARGET_STYLE.NUM_LEVEL.PERCENT));
                BIStyleTarget sumCol = (BIStyleTarget) t;
                TargetStyle style = sumCol.getStyle();
                if (style != null) {
                    style.changeCellStyle(cell);
                }
            } else {
                cell.setStyle(BITableStyle.getInstance().getDimensionCellStyle(cell.getValue() instanceof Number, cell.getRow() % 2 == 1, t.useHyperLink()));
            }
            cbox.setType(CellConstant.CBCELL.ROWFIELD);
            cell.setBoxElement(cbox);
            iter.addCell(cell);
        }
    }

    protected void createCellTitle(CBCell[][] cbcells, int cellType) {
        BIDetailTarget[] viewDimension = widget.getViewDimensions();

        for (int i = 0; i < viewDimension.length; i++) {
            String dimensionName = ((BIAbstractTargetAndDimension) viewDimension[i]).getText();
            CBCell cell = new CBCell(dimensionName);
            cell.setColumn(i + widget.isOrder());
            cell.setRow(0);
            cell.setRowSpan(1);
            cell.setColumnSpan(1);
            cell.setCellGUIAttr(BITableStyle.getInstance().getCellAttr());
            cell.setStyle(BITableStyle.getInstance().getDimensionCellStyle(cell.getValue() instanceof Number, false));
            List cellList = new ArrayList();
            cellList.add(cell);
            CBBoxElement cbox = new CBBoxElement(cellList);
            cbox.setName(viewDimension[i].getValue());
            cbox.setType(cellType);
            cbox.setSortTargetName(viewDimension[i].getValue());
            cbox.setSortTargetValue("[]");
            cell.setBoxElement(cbox);
            cbcells[cell.getColumn()][cell.getRow()] = cell;
        }
    }

    protected List<CBCell> createCellTitle(int cellType) {
        List<CBCell> cells = new LinkedList<CBCell>();
        BIDetailTarget[] viewDimension = widget.getViewDimensions();
        if (widget.isOrder() > 0) {
            CBCell cell = new CBCell(Inter.getLocText("BI-Number_Index"));
            cell.setColumn(0);
            cell.setRow(0);
            cell.setRowSpan(1);
            cell.setColumnSpan(1);
            cell.setCellGUIAttr(BITableStyle.getInstance().getCellAttr());
            cell.setStyle(BITableStyle.getInstance().getTitleDimensionCellStyle(0));
            List cellList = new ArrayList();
            cellList.add(cell);
            CBBoxElement cbox = new CBBoxElement(cellList);
            cbox.setType(cellType);
            cell.setBoxElement(cbox);
            cells.add(cell);
        }
        for (int i = 0; i < viewDimension.length; i++) {
            BIDetailTarget dimension = viewDimension[i];
            String dimensionName = ((BIAbstractTargetAndDimension) viewDimension[i]).getText();
            ChartSetting chartSetting = null;
            if (dimension instanceof BINumberDetailTarget) {
                chartSetting = ((BINumberDetailTarget) viewDimension[i]).getChartSetting();
            }
            if (dimension instanceof BINumberFormulaDetailTarget) {
                chartSetting = ((BINumberFormulaDetailTarget) viewDimension[i]).getChartSetting();
            }
            if (chartSetting != null) {
                JSONObject settings = chartSetting.getSettings();
                int numLevel = settings.optInt("num_level", 0);
                String unit = settings.optString("unit", StringUtils.EMPTY);
                String levelAndUnit = ExecutorUtils.formatLevelAndUnit(numLevel, unit);
                if (!ComparatorUtils.equals(levelAndUnit, StringUtils.EMPTY)) {
                    dimensionName = dimensionName + "(" + levelAndUnit + ")";
                }
            }
            CBCell cell = new CBCell(dimensionName);
            cell.setColumn(i + widget.isOrder());
            cell.setRow(0);
            cell.setRowSpan(1);
            cell.setColumnSpan(1);
            cell.setCellGUIAttr(BITableStyle.getInstance().getCellAttr());
            cell.setStyle(BITableStyle.getInstance().getTitleDimensionCellStyle(0));
            List cellList = new ArrayList();
            cellList.add(cell);
            CBBoxElement cbox = new CBBoxElement(cellList);
            cbox.setType(cellType);
            cell.setBoxElement(cbox);
            cells.add(cell);
        }
        return cells;
    }
}