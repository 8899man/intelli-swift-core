package com.fr.bi.cal.analyze.executor.table;

import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.base.Style;
import com.fr.bi.base.FinalInt;
import com.fr.bi.cal.analyze.cal.index.loader.CubeIndexLoader;
import com.fr.bi.cal.analyze.cal.index.loader.MetricGroupInfo;
import com.fr.bi.cal.analyze.cal.result.*;
import com.fr.bi.cal.analyze.executor.iterator.TableCellIterator;
import com.fr.bi.cal.analyze.executor.iterator.StreamPagedIterator;
import com.fr.bi.cal.analyze.executor.paging.Paging;
import com.fr.bi.cal.analyze.executor.utils.ExecutorUtils;
import com.fr.bi.cal.analyze.report.report.widget.TableWidget;
import com.fr.bi.cal.analyze.session.BISession;
import com.fr.bi.cal.report.engine.CBCell;
import com.fr.bi.conf.report.style.BITableStyle;
import com.fr.bi.conf.report.widget.field.dimension.BIDimension;
import com.fr.bi.field.target.target.BISummaryTarget;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.report.key.TargetGettingKey;
import com.fr.general.DateUtils;
import com.fr.general.GeneralUtils;
import com.fr.general.Inter;
import com.fr.json.JSONObject;
import com.fr.stable.ExportConstants;

import java.util.*;

public class CrossExecutor extends AbstractTableWidgetExecutor<NewCrossRoot> {

    private BIDimension[] rowDimension;
    private BIDimension[] colDimension;
    private CrossExpander expander;

    public CrossExecutor(TableWidget widget, BIDimension[] usedRows,
                         BIDimension[] usedColumn,
                         Paging paging, BISession session, CrossExpander expander) {
        super(widget, paging, session);
        this.rowDimension = usedRows;
        this.colDimension = usedColumn;
        this.expander = expander;
    }

    @Override
    public TableCellIterator createCellIterator4Excel() throws Exception {
        NewCrossRoot node = getCubeNode();
        if (node == null) {
            return new TableCellIterator(0, 0);
        }

        int len = usedSumTarget.length;
        TargetGettingKey[] keys = new TargetGettingKey[len];
        boolean isWholeCol = keys.length == 0 || !widget.getChartSetting().showColTotal();
        boolean isWholeRow = keys.length == 0 || !widget.getChartSetting().showRowTotal();
        int columnLen = (isWholeCol ? node.getTop().getTotalLength() :
                node.getTop().getTotalLengthWithSummary()) * Math.max(1, keys.length) + rowDimension.length + widget.isOrder();
        int rowLen = (isWholeRow ? node.getLeft().getTotalLength() :
                node.getLeft().getTotalLengthWithSummary()) + colDimension.length + 1;

        final TableCellIterator iter = new TableCellIterator(columnLen, rowLen);
        new Thread() {
            public void run() {
                try {
                    FinalInt start = new FinalInt();
                    FinalInt rowIdx = new FinalInt();
                    StreamPagedIterator pagedIterator = iter.getIteratorByPage(start.value);
                    NewCrossRoot[] newCrossRoots = new NewCrossRoot[1];
                    newCrossRoots[0] = getCubeNode();
                    generateTitle(newCrossRoots, widget, colDimension, rowDimension, usedSumTarget, pagedIterator, rowIdx);
                    rowIdx.value++;
                    generateCells(newCrossRoots, widget, rowDimension, rowDimension.length, iter, start, rowIdx, 0);
                } catch (Exception e) {
                    BILoggerFactory.getLogger().error(e.getMessage(), e);
                } finally {
                    iter.finish();
                }
            }
        }.start();
        return iter;
    }

    /**
     * @param roots         ComplexCrossExecutor复用此方法时需要的参数
     * @param widget        ComplexCrossExecutor复用此方法时需要的参数
     * @param colDimension  ComplexCrossExecutor复用此方法时需要的参数
     * @param rowDimension  ComplexCrossExecutor复用此方法时需要的参数
     * @param usedSumTarget ComplexCrossExecutor复用此方法时需要的参数
     * @param pagedIterator
     * @param rowIdx
     * @throws Exception
     */
    public static void generateTitle(NewCrossRoot[] roots, TableWidget widget, BIDimension[] colDimension, BIDimension[] rowDimension,
                                     BISummaryTarget[] usedSumTarget, StreamPagedIterator pagedIterator, FinalInt rowIdx) throws Exception {
        int rootsLen = roots.length;
        CrossHeader[] tops = new CrossHeader[rootsLen];
        for (int i = 0; i < rootsLen; i++) {
            tops[i] = roots[i].getTop();
        }
        Style style = BITableStyle.getInstance().getTitleDimensionCellStyle(0);
        if (widget.isOrder() == 1) {
            CBCell cell = ExecutorUtils.createCell(Inter.getLocText("BI-Number_Index"), 0, colDimension.length + 1, 0, 1, style);
            pagedIterator.addCell(cell);
        }

        int colDimLen = 0;
        while (colDimLen < colDimension.length) {
            CBCell cell = ExecutorUtils.createCell(colDimension[colDimLen].getText(), rowIdx.value, 1, widget.isOrder(), rowDimension.length, style);
            pagedIterator.addCell(cell);
            FinalInt columnIdx = new FinalInt();
            columnIdx.value = rowDimension.length + widget.isOrder();
            for (int i = 0; i < rootsLen; i++) {
                tops[i] = (CrossHeader) tops[i].getFirstChild();
                //列表头
                getColDimensionsTitle(widget, usedSumTarget, pagedIterator, tops[i], rowIdx.value, columnIdx, style);
            }
            rowIdx.value++;
            colDimLen++;
        }

        for (int i = 0; i < rowDimension.length; i++) {
            CBCell cell = ExecutorUtils.createCell(rowDimension[i].getText(), rowIdx.value, 1, i + widget.isOrder(), 1, style);
            pagedIterator.addCell(cell);
        }
        if (widget.getViewTargets().length > 1) {
            FinalInt targetsTitleColumnIdx = new FinalInt();
            targetsTitleColumnIdx.value = rowDimension.length + widget.isOrder();
            for (int i = 0; i < rootsLen; i++) {
                getTargetsTitle(usedSumTarget, pagedIterator, tops[i], rowIdx.value, targetsTitleColumnIdx, style);
            }
        }
    }

    private static void getColDimensionsTitle(TableWidget widget, BISummaryTarget[] usedSumTarget, StreamPagedIterator pagedIterator,
                                              CrossHeader top, int rowIdx, FinalInt columnIdx, Style style) {
        int targetNum = widget.getViewTargets().length;

        CrossHeader temp = top;
        while (temp != null) {
            int columnSpan = temp.getTotalLengthWithSummary() * targetNum;
            Object data = temp.getData();
            BIDimension[] dims = widget.getViewTopDimensions();
            BIDimension dim = dims[rowIdx];
            String v = dim.toString(dim.getValueByType(data));
            if (dim.getGroup().getType() == BIReportConstant.GROUP.YMD && GeneralUtils.string2Number(v) != null) {
                v = DateUtils.DATEFORMAT2.format(new Date(GeneralUtils.string2Number(v).longValue()));
            }
            int rowSpan = (rowIdx == dims.length - 1) ? (usedSumTarget.length == 1 ? 2 : 1) : 1;
            CBCell cell = ExecutorUtils.createCell(v, rowIdx, rowSpan, columnIdx.value, columnSpan, style);
            pagedIterator.addCell(cell);
            columnIdx.value += columnSpan;
            generateTitleSumCells(temp, widget, pagedIterator, rowIdx, columnIdx);
            temp = (CrossHeader) temp.getSibling();
        }
    }

    private static void generateTitleSumCells(CrossHeader temp, TableWidget widget, StreamPagedIterator pagedIterator, int rowIdx, FinalInt columnIdx) {
        if (checkIfGenerateTitleSumCells(temp) && temp.getParent().getChildLength() != 1) {
            Style style = BITableStyle.getInstance().getYSumStringCellStyle();
            CBCell cell = ExecutorUtils.createCell(Inter.getLocText("BI-Summary_Values"), rowIdx, temp.getDeep(), columnIdx.value, widget.getViewTargets().length, style);
            pagedIterator.addCell(cell);
        }
        adjustColumnIdx(temp, widget, columnIdx);
    }

    private static void adjustColumnIdx(CrossHeader temp, TableWidget widget, FinalInt columnIdx) {
        if (checkIfGenerateTitleSumCells(temp)) {
            if (temp.getParent().getChildLength() != 1) {
                columnIdx.value += widget.getViewTargets().length;
            }
            if (temp.getParent() != null) {
                adjustColumnIdx((CrossHeader) temp.getParent(), widget, columnIdx);
            }
        }
    }

    private static boolean checkIfGenerateTitleSumCells(CrossHeader header) {
        //到根节点停止
        boolean isNotRoot = header.getParent() != null;
        //isLastSum 是否是最后一行汇总行
        boolean isLastSum = header.getSibling() == null;
        //判断空值 比较当前节点和下一个兄弟节点是否有同一个父亲节点
        boolean needSumCell = isNotRoot && header.getSibling() != null && header.getSibling().getParent() != null && (header.getParent() != header.getSibling().getParent());
        return isNotRoot && (isLastSum || needSumCell);
    }

    private static void getTargetsTitle(BISummaryTarget[] usedSumTarget,
                                        StreamPagedIterator pagedIterator, CrossHeader top, int rowIdx, FinalInt columnIdx, Style style) {
        CrossHeader temp = top;
        Style sumStyle = BITableStyle.getInstance().getYSumStringCellStyle();
        int lengthWithSum = top.getTotalLength();
        while (temp != null) {
            for (int i = 0; i < lengthWithSum; i++) {
                generateTargetTitleWithSum(usedSumTarget, "", pagedIterator, rowIdx, columnIdx, style);
            }
            if (checkIfGenerateTitleSumCells(temp) && temp.getParent().getChildLength() != 1) {
                generateTargetTitleWithSum(usedSumTarget, Inter.getLocText("BI-Summary_Values") + ":", pagedIterator, rowIdx, columnIdx, sumStyle);
            }
            temp = (CrossHeader) temp.getSibling();
        }
        if (usedSumTarget.length != 0) {
            generateTargetTitleWithSum(usedSumTarget, Inter.getLocText("BI-Summary_Values") + ":", pagedIterator, rowIdx, columnIdx, sumStyle);
        }
    }

    private static void generateTargetTitleWithSum(BISummaryTarget[] usedSumTarget, String text, StreamPagedIterator pagedIterator, int rowIdx, FinalInt columnIdx, Style style) {
        for (BISummaryTarget anUsedSumTarget : usedSumTarget) {
            CBCell cell = ExecutorUtils.createCell(text + anUsedSumTarget.getText(), rowIdx, 1, columnIdx.value++, 1, style);
            pagedIterator.addCell(cell);
        }
    }

    /**
     * @param roots         ComplexCrossExecutor复用此方法时需要的参数
     * @param widget        ComplexCrossExecutor复用此方法时需要的参数
     * @param rowDimensions ComplexCrossExecutor复用此方法时需要的参数
     * @param maxDimLen     ComplexCrossExecutor复用此方法时需要的参数 列表头中维度最多区域中维度的长度
     * @param iter
     * @param start
     * @param rowIdx        ComplexCrossExecutor复用此方法时需要的参数 记录行数
     * @param order         ComplexCrossExecutor复用此方法时需要的参数 记录序号
     * @throws Exception
     */
    public static void generateCells(NewCrossRoot[] roots, TableWidget widget, BIDimension[] rowDimensions, int maxDimLen,
                                     TableCellIterator iter, FinalInt start, FinalInt rowIdx, int order) throws Exception {
        //判断奇偶行需要用到标题的行数
        int titleRowSpan = rowIdx.value;
        CrossHeader[] crossNodes = new CrossHeader[roots.length];
        for (int i = 0, j = roots.length; i < j; i++) {
            CrossHeader node = roots[i].getLeft();
            while (node.getChildLength() != 0) {
                node = (CrossHeader) node.getFirstChild();
            }
            crossNodes[i] = node;
        }
        int[] oddEven = new int[rowDimensions.length];
        oddEven[0] = order;
        Object[] dimensionNames = new Object[rowDimensions.length];
        while (crossNodes[0] != null) {
            FinalInt columnIdx = new FinalInt();
            columnIdx.value = rowDimensions.length + widget.isOrder();
            int newRow = rowIdx.value & ExportConstants.MAX_ROWS_2007 - 1;
            if (newRow == 0) {
                iter.getIteratorByPage(start.value).finish();
                start.value++;
            }
            StreamPagedIterator pagedIterator = iter.getIteratorByPage(start.value);
            CrossNode tempFirstNode = null;
            for (int i = 0, j = crossNodes.length; i < j; i++) {
                CrossNode temp = crossNodes[i].getValue();
                //第一次出现表头时创建cell
                CrossHeader parent = crossNodes[i];
                if (i == 0) {
                    generateDimensionName(parent, rowDimensions, pagedIterator, dimensionNames, oddEven, rowIdx, columnIdx,
                            widget.isOrder(), maxDimLen);
                }
                generateTopChildren(widget, temp, pagedIterator, rowIdx.value, columnIdx, titleRowSpan);
                if(i == 0) {
                    tempFirstNode = crossNodes[0].getValue();
                }
                crossNodes[i] = (CrossHeader) crossNodes[i].getSibling();
            }
            rowIdx.value++;
            generateRowSumCells(tempFirstNode, widget, pagedIterator, rowIdx, rowDimensions.length - 1 + widget.isOrder(), maxDimLen);
        }
    }

    private static void generateRowSumCells(CrossNode temp, TableWidget widget, StreamPagedIterator pagedIterator, FinalInt rowIdx, int columnIdx,int maxColumnDimensionsLength) {
        if ((widget.getViewTargets().length != 0) && checkIfGenerateRowSumCell(temp)) {
            if (temp.getLeftParent().getLeftChildLength() != 1) {
                Style style = BITableStyle.getInstance().getYSumStringCellStyle();
                if (widget.isOrder() == 1 && temp.getBottomSibling() == null) {
                    CBCell cell = ExecutorUtils.createCell(Inter.getLocText("BI-Summary_Values"), rowIdx.value, 1, 0, 1, style);
                    pagedIterator.addCell(cell);
                }
                CBCell cell = ExecutorUtils.createCell(Inter.getLocText("BI-Summary_Values"), rowIdx.value, 1, columnIdx, maxColumnDimensionsLength- columnIdx + widget.isOrder(), style);
                pagedIterator.addCell(cell);
                FinalInt sumIdx = new FinalInt();
                sumIdx.value = maxColumnDimensionsLength + widget.isOrder();
                generateTopChildren(widget, temp.getLeftParent(), pagedIterator, rowIdx.value, sumIdx, 1);
                rowIdx.value++;
            }
            //开辟新内存，不对temp进行修改
            CrossNode parent = temp.getLeftParent();
            generateRowSumCells(parent, widget, pagedIterator, rowIdx, columnIdx - 1, maxColumnDimensionsLength);
        }
    }

    private static boolean checkIfGenerateRowSumCell(CrossNode temp) {
        //到根节点停止
        boolean isNotRoot = temp.getLeftParent() != null;
        //isLastSum 是否是最后一行汇总行
        boolean isLastSum = temp.getBottomSibling() == null;
        //判断空值 比较当前节点和下一个兄弟节点是否有同一个父亲节点
        boolean needSumCell = isNotRoot && temp.getBottomSibling() != null && temp.getBottomSibling().getLeftParent() != null && (temp.getLeftParent() != temp.getBottomSibling().getLeftParent());
        return isNotRoot && (isLastSum || needSumCell);
    }

    private static void generateDimensionName(CrossHeader parent, BIDimension[] rowDimension, StreamPagedIterator pagedIterator,
                                              Object[] dimensionNames, int[] oddEven, FinalInt rowIdx, FinalInt columnIdx,
                                              int isOrder, int maxDimLen) {
        int i = rowDimension.length;
        while (parent.getParent() != null) {
            int rowSpan = parent.getTotalLengthWithSummary();
            Object data = parent.getData();
            BIDimension dim = rowDimension[--i];
            Object v = dim.getValueByType(data);
            if (dim.getGroup().getType() == BIReportConstant.GROUP.YMD && GeneralUtils.string2Number(v.toString()) != null) {
                v = DateUtils.DATEFORMAT2.format(new Date(GeneralUtils.string2Number(v.toString()).longValue()));
            }
            if (v != dimensionNames[i] || (i == dimensionNames.length - 1)) {
                oddEven[i]++;
                //不应该加一 为了和前台展示统一 奇偶行的颜色互换
                Style style = BITableStyle.getInstance().getDimensionCellStyle(false, (oddEven[i] + 1) % 2 == 0);
                CBCell cell = ExecutorUtils.createCell(v, rowIdx.value, rowSpan, i + isOrder, 1, style);
                pagedIterator.addCell(cell);
                //复杂表两个区域的维度的情况下 需要设置最后一个维度单元格columnSpan
                if (i == dimensionNames.length - 1) {
                    int diff = maxDimLen - rowDimension.length;
                    cell.setColumnSpan(diff + 1);
                    //后面指标的位置需要向右偏移
                    columnIdx.value += diff;
                }
                //创建序号并和顶级维度共享rowSpan
                if (i == 0 && isOrder == 1) {
                    CBCell orderCell = ExecutorUtils.createCell(oddEven[0], rowIdx.value, rowSpan, 0, 1, style);
                    pagedIterator.addCell(orderCell);
                }
                dimensionNames[i] = v;
            }
            parent = (CrossHeader) parent.getParent();
        }
    }

    private static void generateTopChildren(TableWidget widget, CrossNode temp, StreamPagedIterator pagedIterator,
                                            int rowIdx, FinalInt columnIdx, int titleRowSpan) {
        if (temp.getTopFirstChild() != null) {
            int topChildrenLen = temp.getTopChildLength();
            for (int i = 0; i < topChildrenLen; i++) {
                generateTopChildren(widget, temp.getTopChild(i), pagedIterator, rowIdx, columnIdx, titleRowSpan);
            }
        } else {
            Style style = Style.getInstance();
            for (TargetGettingKey key : widget.getTargetsKey()) {
                Object v = temp.getSummaryValue(key);
                boolean isPercent = widget.getChartSetting().getNumberLevelByTargetId(key.getTargetName()) == BIReportConstant.TARGET_STYLE.NUM_LEVEL.PERCENT;
                style = BITableStyle.getInstance().getNumberCellStyle(v, (rowIdx - titleRowSpan + 1) % 2 == 1, isPercent);
                CBCell cell = ExecutorUtils.createCell(v, rowIdx, 1, columnIdx.value, 1, style);
                pagedIterator.addCell(cell);
                columnIdx.value++;
            }
            generateColumnSumCell(temp, widget, pagedIterator, widget.getTargetsKey(), rowIdx, columnIdx, style);
        }
    }

    private static void generateColumnSumCell(CrossNode temp, TableWidget widget, StreamPagedIterator pagedIterator, TargetGettingKey[] keys, int rowIdx, FinalInt columnIdx, Style style) {
        if (widget.getViewTargets().length != 0 && checkIfGenerateColumnSumCell(temp)) {
            if (temp.getTopParent().getTopChildLength() != 1) {
                for (TargetGettingKey key : keys) {
                    Object data = temp.getTopParent().getSummaryValue(key);
                    CBCell cell = ExecutorUtils.createCell(data, rowIdx, 1, columnIdx.value++, 1, style);
                    pagedIterator.addCell(cell);
                }
            }
            CrossNode parent = temp.getTopParent();
            generateColumnSumCell(parent, widget, pagedIterator, keys, rowIdx, columnIdx, style);
        }
    }

    private static boolean checkIfGenerateColumnSumCell(CrossNode temp) {
        //到根节点停止
        boolean isNotRoot = temp.getTopParent() != null;
        //isLastSum 是否是最后一行汇总行
        boolean isLastSum = temp.getRightSibling() == null;
        //判断空值 比较当前节点和下一个兄弟节点是否有同一个父亲节点
        boolean needSumCell = isNotRoot && temp.getRightSibling() != null && temp.getRightSibling().getTopParent() != null && (temp.getTopParent() != temp.getRightSibling().getTopParent());
        return isNotRoot && (isLastSum || needSumCell);
    }

    @Override
    public NewCrossRoot getCubeNode() throws Exception {
        long start = System.currentTimeMillis();
        if (getSession() == null) {
            return null;
        }
        int len = usedSumTarget.length;
        Map<String, TargetGettingKey> targetsMap = new HashMap<String, TargetGettingKey>();
        TargetGettingKey[] keys = new TargetGettingKey[len];
        for (int i = 0; i < len; i++) {
            keys[i] = usedSumTarget[i].createTargetGettingKey();
            targetsMap.put(usedSumTarget[i].getValue(), keys[i]);
        }
        int calpage = paging.getOperator();

        NewCrossRoot node = CubeIndexLoader.getInstance(session.getUserId()).loadPageCrossGroup(createTarget4Calculate(), rowDimension, colDimension, allSumTarget, calpage, widget.useRealData(), session, expander, widget);

        if (widget.useTargetSort()) {
            node = node.createSortedNode(widget.getTargetSort(), targetsMap);
        }
        clearNullSummary(node.getLeft(), keys);
        clearNullSummary(node.getTop(), keys);
        BILoggerFactory.getLogger().info(DateUtils.timeCostFrom(start) + ": cal time");
        return node;
    }

    @Override
    public JSONObject createJSONObject() throws Exception {
        return getCubeNode().toJSONObject(rowDimension, colDimension, widget.getTargetsKey());
    }

    @Override
    public List<MetricGroupInfo> getLinkedWidgetFilterGVIList() throws Exception {
        if (getSession() == null) {
            return null;
        }
        int calPage = paging.getOperator();
        List<NodeAndPageInfo> infoList = CubeIndexLoader.getInstance(session.getUserId()).getPageCrossGroupInfoList(createTarget4Calculate(), rowDimension, colDimension, allSumTarget, calPage, widget.useRealData(), session, expander, widget);
        ArrayList<MetricGroupInfo> gviList = new ArrayList<MetricGroupInfo>();
        for (NodeAndPageInfo info : infoList) {
            gviList.addAll(info.getIterator().getRoot().getMetricGroupInfoList());
        }
        return gviList;

    }

    /**
     * @param rowData 行值
     * @param colData 列值
     * @return
     * @throws Exception
     */
    public NewCrossRoot getStopOnRowNode(Object[] rowData, Object colData[]) throws Exception {
        // 行的
        if (session == null) {
            return null;
        }
        int rowLength = widget.getViewDimensions().length;
        int summaryLength = usedSumTarget.length;
        int columnLen = rowLength + summaryLength;
        if (columnLen == 0) {
            return null;
        }
        int calPage = paging.getOperator();
        CubeIndexLoader cubeIndexLoader = CubeIndexLoader.getInstance(session.getUserId());
        //cubeIndexLoader.getGroupNodeWidthGvi(widget,);
        Node l = cubeIndexLoader.getStopWhenGetRowNode(rowData, widget, createTarget4Calculate(), widget.getViewDimensions(),
                allDimensions, allSumTarget, calPage, session, CrossExpander.ALL_EXPANDER.getYExpander());

        rowLength = widget.getViewTopDimensions().length;
        columnLen = rowLength + summaryLength;
        if (columnLen == 0) {
            return null;
        }

        Node t = cubeIndexLoader.getStopWhenGetRowNode(colData, widget, createTarget4Calculate(), widget.getViewTopDimensions(),
                allDimensions, allSumTarget, calPage, session, CrossExpander.ALL_EXPANDER.getYExpander());

        return new NewCrossRoot(l.createCrossHeader(), t.createCrossHeader());
    }

    private void clearNullSummary(CrossHeader left, TargetGettingKey[] keys) {
        for (TargetGettingKey key : keys) {
            if (left.getSummaryValue(key) == null) {
                left.getValue().setSummaryValue(key, null);
            }
        }
        for (int i = 0; i < left.getChildLength(); i++) {
            clearNullSummary((CrossHeader) left.getChild(i), keys);
        }
    }

}