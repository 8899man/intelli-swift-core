/**
 * @class BI.AccumulateRadarChartSetting
 * @extends BI.Widget
 * 堆积雷达样式
 */
BI.AccumulateRadarChartSetting = BI.inherit(BI.AbstractChartSetting, {

    _defaultConfig: function () {
        return BI.extend(BI.AccumulateRadarChartSetting.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-charts-setting"
        })
    },

    _init: function () {
        BI.AccumulateRadarChartSetting.superclass._init.apply(this, arguments);
        var self = this, constant = BI.AbstractChartSetting;

        this.colorSelect = BI.createWidget({
            type: "bi.chart_setting_select_color_combo",
            width: 130
        });
        this.colorSelect.populate();

        this.colorSelect.on(BI.ChartSettingSelectColorCombo.EVENT_CHANGE, function () {
            self.fireEvent(BI.AccumulateRadarChartSetting.EVENT_CHANGE);
        });

        this.chartStyleGroup = BI.createWidget({
            type: "bi.button_group",
            items: BI.createItems(BICst.AXIS_STYLE_GROUP, {
                type: "bi.icon_button",
                extraCls: "chart-style-font",
                width: constant.BUTTON_WIDTH,
                height: constant.BUTTON_HEIGHT,
                iconWidth: constant.ICON_WIDTH,
                iconHeight: constant.ICON_HEIGHT
            }),
            layouts: [{
                type: "bi.vertical_adapt",
                height: constant.SINGLE_LINE_HEIGHT
            }]
        });
        this.chartStyleGroup.on(BI.ButtonGroup.EVENT_CHANGE, function () {
            self.fireEvent(BI.LineAreaChartSetting.EVENT_CHANGE);
        });

        this.chartTypeGroup = BI.createWidget({
            type: "bi.button_group",
            items: BI.createItems(BICst.ACC_RADAR_CHART_STYLE_GROUP, {
                type: "bi.icon_button",
                extraCls: "chart-style-font",
                width: constant.BUTTON_WIDTH,
                height: constant.BUTTON_HEIGHT,
                iconWidth: constant.ICON_WIDTH,
                iconHeight: constant.ICON_HEIGHT
            }),
            layouts: [{
                type: "bi.vertical_adapt",
                height: constant.SINGLE_LINE_HEIGHT
            }]
        });
        this.chartTypeGroup.on(BI.ButtonGroup.EVENT_CHANGE, function () {
            self.fireEvent(BI.LineAreaChartSetting.EVENT_CHANGE);
        });

        var tableStyle = BI.createWidget({
            type: "bi.horizontal_adapt",
            columnSize: [100],
            cls: "single-line-settings",
            items: [{
                type: "bi.label",
                text: BI.i18nText("BI-Table_Sheet_Style"),
                lgap: constant.SIMPLE_H_LGAP,
                textAlign: "left",
                cls: "line-title"
            }, {
                type: "bi.left",
                cls: "detail-style",
                items: BI.createItems([{
                    type: "bi.label",
                    text: BI.i18nText("BI-Chart_Color"),
                    cls: "attr-names"
                }, {
                    el: {
                        type: "bi.vertical_adapt",
                        items: [this.colorSelect]
                    },
                    lgap: constant.SIMPLE_H_GAP
                }, {
                    type: "bi.label",
                    text: BI.i18nText("BI-Table_Style"),
                    cls: "attr-names",
                    lgap: constant.SIMPLE_H_GAP
                }, {
                    el: {
                        type: "bi.vertical_adapt",
                        items: [this.chartStyleGroup]
                    },
                    lgap: constant.SIMPLE_H_GAP
                }, {
                    type: "bi.label",
                    text: BI.i18nText("BI-Type"),
                    cls: "attr-names",
                    lgap: constant.SIMPLE_H_GAP
                }, {
                    el: {
                        type: "bi.vertical_adapt",
                        items: [this.chartTypeGroup]
                    },
                    lgap: constant.SIMPLE_H_GAP
                }], {
                    height: constant.SINGLE_LINE_HEIGHT
                })
            }]
        });

        //格式和数量级
        this.lYAxisStyle = BI.createWidget({
            type: "bi.segment",
            width: constant.FORMAT_SEGMENT_WIDTH,
            height: constant.BUTTON_HEIGHT,
            items: BICst.TARGET_STYLE_FORMAT
        });

        this.lYAxisStyle.on(BI.Segment.EVENT_CHANGE, function () {
            self.fireEvent(BI.AccumulateRadarChartSetting.EVENT_CHANGE);
        });

        this.numberLevellY = BI.createWidget({
            type: "bi.segment",
            width: constant.NUMBER_LEVEL_SEGMENT_WIDTH,
            height: constant.BUTTON_HEIGHT,
            items: BICst.TARGET_STYLE_LEVEL
        });

        this.numberLevellY.on(BI.Segment.EVENT_CHANGE, function () {
            self.fireEvent(BI.AccumulateRadarChartSetting.EVENT_CHANGE);
        });

        this.showCustomScale = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Scale_Customize"),
            width: 115
        });

        this.showCustomScale.on(BI.Controller.EVENT_CHANGE, function () {
            self.customScale.setVisible(this.isSelected());
            self.fireEvent(BI.AccumulateRadarChartSetting.EVENT_CHANGE)
        });

        this.customScale = BI.createWidget({
            type: "bi.custom_scale"
        });

        this.customScale.on(BI.CustomScale.EVENT_CHANGE, function () {
            self.fireEvent(BI.AccumulateRadarChartSetting.EVENT_CHANGE)
        });

        var lYAxis = BI.createWidget({
            type: "bi.horizontal_adapt",
            columnSize: [80],
            cls: "single-line-settings",
            items: [{
                type: "bi.label",
                textHeight: constant.SINGLE_LINE_HEIGHT,
                lgap: constant.SIMPLE_H_LGAP,
                textAlign: "left",
                text: BI.i18nText("BI-Value_Axis"),
                cls: "line-title"
            }, {
                type: "bi.left",
                cls: "detail-style",
                items: BI.createItems([{
                    type: "bi.label",
                    text: BI.i18nText("BI-Format"),
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.lYAxisStyle]
                }, {
                    type: "bi.label",
                    text: BI.i18nText("BI-Num_Level"),
                    lgap: constant.SIMPLE_H_GAP,
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.numberLevellY]
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.showCustomScale]
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.customScale]
                }], {
                    height: constant.SINGLE_LINE_HEIGHT
                }),
                lgap: constant.SIMPLE_H_GAP
            }]
        });

        //图例
        this.legend = BI.createWidget({
            type: "bi.segment",
            width: constant.LEGEND_SEGMENT_WIDTH,
            height: constant.BUTTON_HEIGHT,
            items: BICst.CHART_LEGEND
        });

        this.legend.on(BI.Segment.EVENT_CHANGE, function () {
            self.fireEvent(BI.AccumulateRadarChartSetting.EVENT_CHANGE);
        });

        //数据标签
        this.showDataLabel = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Show_Data_Label"),
            width: 115
        });

        this.showDataLabel.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.AccumulateRadarChartSetting.EVENT_CHANGE);
        });

        //网格线
        this.gridLine = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Show_Grid_Line"),
            width: 115
        });

        this.gridLine.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.AccumulateRadarChartSetting.EVENT_CHANGE);
        });

        var showElement = BI.createWidget({
            type: "bi.horizontal_adapt",
            columnSize: [80],
            cls: "single-line-settings",
            items: [{
                type: "bi.label",
                text: BI.i18nText("BI-Element_Show"),
                lgap: constant.SIMPLE_H_LGAP,
                textAlign: "left",
                textHeight: constant.SINGLE_LINE_HEIGHT,
                cls: "line-title"
            }, {
                type: "bi.left",
                cls: "detail-style",
                items: BI.createItems([{
                    type: "bi.label",
                    text: BI.i18nText("BI-Legend_Normal"),
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.legend]
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.showDataLabel]
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.gridLine]
                }], {
                    height: constant.SINGLE_LINE_HEIGHT
                }),
                lgap: constant.SIMPLE_H_GAP
            }]
        });

        //联动传递指标过滤条件
        this.transferFilter = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Bind_Target_Condition"),
            width: 170
        });
        this.transferFilter.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.AccumulateRadarChartSetting.EVENT_CHANGE);
        });

        var otherAttr = BI.createWidget({
            type: "bi.left_right_vertical_adapt",
            cls: "single-line-settings",
            items: {
                left: [{
                    type: "bi.label",
                    text: BI.i18nText("BI-Interactive_Attr"),
                    cls: "line-title"
                }, this.transferFilter]
            },
            height: constant.SINGLE_LINE_HEIGHT,
            lhgap: constant.SIMPLE_H_GAP
        });

        BI.createWidget({
            type: "bi.vertical",
            element: this.element,
            items: [tableStyle, lYAxis, showElement, otherAttr],
            hgap: 10
        })
    },

    populate: function () {
        var wId = this.options.wId;
        var tIds = BI.Utils.getAllTargetDimensionIDs(wId);
        this.transferFilter.setSelected(BI.Utils.getWSTransferFilterByID(wId));
        this.colorSelect.setValue(BI.Utils.getWSChartColorByID(wId));
        this.chartStyleGroup.setValue(BI.Utils.getWSChartStyleByID(wId));
        this.chartTypeGroup.setValue(BI.Utils.getWSChartRadarTypeByID(wId));
        this.lYAxisStyle.setValue(BI.Utils.getWSLeftYAxisStyleByID(wId));
        this.numberLevellY.setValue(BI.Utils.getWSLeftYAxisNumLevelByID(wId));
        this.legend.setValue(BI.Utils.getWSChartLegendByID(wId));
        this.showDataLabel.setSelected(BI.Utils.getWSShowDataLabelByID(wId));
        this.gridLine.setSelected(BI.Utils.getWSShowGridLineByID(wId));
        this.showCustomScale.setSelected(BI.Utils.getWSShowCustomScale(wId));
        this.customScale.setValue(BI.Utils.getWSCustomScale(wId));
        this.customScale.setVisible(BI.Utils.getWSShowCustomScale(wId));

    },

    getValue: function () {
        return {
            transfer_filter: this.transferFilter.isSelected(),
            chart_color: this.colorSelect.getValue()[0],
            chart_style: this.chartStyleGroup.getValue()[0],
            chart_radar_type: this.chartTypeGroup.getValue()[0],
            left_y_axis_style: this.lYAxisStyle.getValue()[0],
            left_y_axis_number_level: this.numberLevellY.getValue()[0],
            chart_legend: this.legend.getValue()[0],
            show_data_label: this.showDataLabel.isSelected(),
            show_grid_line: this.gridLine.isSelected(),
            show_custom_scale: this.showCustomScale.isSelected(),
            custom_scale: this.customScale.getValue()
        }
    },

    setValue: function (v) {
        this.transferFilter.setSelected(v.transfer_filter);
        this.colorSelect.setValue(v.chart_color);
        this.chartStyleGroup.setValue(v.chart_style);
        this.chartTypeGroup.setValue(v.chart_radar_type);
        this.lYAxisStyle.setValue(v.left_y_axis_style);
        this.numberLevellY.setValue(v.left_y_axis_number_level);
        this.legend.setValue(v.chart_legend);
        this.showDataLabel.setSelected(v.show_data_label);
        this.gridLine.setSelected(v.show_grid_line);
        this.showCustomScale.setSelected(v.show_custom_scale);
        this.customScale.setValue(v.custom_scale)
    }
});
BI.AccumulateRadarChartSetting.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.accumulate_radar_chart_setting", BI.AccumulateRadarChartSetting);