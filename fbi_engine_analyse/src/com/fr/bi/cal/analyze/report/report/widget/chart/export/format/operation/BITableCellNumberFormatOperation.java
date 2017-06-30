package com.fr.bi.cal.analyze.report.report.widget.chart.export.format.operation;

import com.fr.bi.cal.analyze.report.report.widget.chart.export.format.setting.ICellFormatSetting;
import com.fr.bi.cal.analyze.report.report.widget.chart.export.format.utils.BITableCellFormatHelper;

/**
 * Created by Kary on 2017/4/10.
 */
public class BITableCellNumberFormatOperation extends BITableCellFormatOperation {

    public BITableCellNumberFormatOperation(ICellFormatSetting ICellFormatSetting) {
        this.iCellFormatSetting = ICellFormatSetting;
    }

    @Override
    public String formatItemTextValues(String text) throws Exception {
        return BITableCellFormatHelper.targetValueFormat(iCellFormatSetting.createJSON(), text);
    }

    @Override
    public String formatHeaderText(String headerText) throws Exception {
        return BITableCellFormatHelper.headerTextFormat(iCellFormatSetting.createJSON(), headerText);
    }
}
