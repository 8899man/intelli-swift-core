package com.fr.bi.cal.analyze.report.report.widget.chart.export.item.constructor;

import com.fr.bi.cal.analyze.report.report.widget.chart.export.item.ITableHeader;
import com.fr.bi.cal.analyze.report.report.widget.chart.export.item.ITableItem;
import com.fr.bi.conf.report.conf.BIWidgetSettings;
import com.fr.bi.conf.report.widget.BIWidgetStyle;
import com.fr.json.JSONCreator;

import java.util.List;

/**
 * Created by Kary on 2017/5/23.
 */
public interface DataConstructor extends JSONCreator{
    int getWidgetType();

    List<ITableHeader> getHeaders();

    List<ITableItem> getItems();

    List<ITableHeader> getCrossHeaders();

    List<ITableItem> getCrossItems();

    BIWidgetStyle getWidgetSettings();
}
