package com.fr.bi.conf.report;

import com.finebi.cube.conf.field.BusinessField;
import com.finebi.cube.conf.table.BusinessTable;
import com.fr.bi.cal.report.report.poly.BIPolyWorkSheet;
import com.fr.bi.common.BICoreService;
import com.fr.bi.conf.report.widget.field.BITargetAndDimension;
import com.fr.bi.conf.session.BISessionProvider;
import com.fr.bi.base.provider.ParseJSONWithUID;
import com.fr.json.JSONObject;
import com.fr.main.impl.WorkBook;
import com.fr.stable.FCloneable;

import java.awt.*;
import java.util.List;

/**
 * BI的控件接口
 * 处理控件各项属性，生成聚合报表块
 *
 * @author Daniel-pc
 */
public interface BIWidget extends ParseJSONWithUID, FCloneable, BICoreService {

    /**
     * 返回Widget的ID
     *
     * @return 注释
     */
    String getWidgetName();

    void setWidgetName(String newName);


    <T extends BITargetAndDimension> T[] getDimensions();

    <T extends BITargetAndDimension> T[] getViewDimensions();

    <T extends BITargetAndDimension> T[] getTargets();

    <T extends BITargetAndDimension> T[] getViewTargets();

    /**
     * @return 所有用到的表
     */
    List<BusinessTable> getUsedTableDefine();

    /**
     * @return 所有用到的字段
     */
    List<BusinessField> getUsedFieldDefine();

    int isOrder();

    /**
     * @return 计算结果Node，页码信息
     */
    JSONObject createDataJSON(BISessionProvider session) throws Exception;

    /**
     * 生成控件对应的定义模板用于计算
     *
     * @return 注释
     */
    WorkBook createWorkBook(BISessionProvider session);

    BIPolyWorkSheet createWorkSheet(BISessionProvider session);

    WidgetType getType();

    /**
     * 显示汇总行
     * @return
     */
    boolean showRowToTal();

    /**
     * 显示汇总列
     * @return
     */
    boolean showColumnTotal();

    void refreshColumns();

    void refreshSources();

    void reSetDetailTarget();

    Rectangle getRect();
}