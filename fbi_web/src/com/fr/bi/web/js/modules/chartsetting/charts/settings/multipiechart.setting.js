/**
 * @class BI.MultiPieChartSetting
 * @extends BI.Widget
 * 多层饼图样式
 */
BI.MultiPieChartSetting = BI.inherit(BI.AbstractChartSetting, {

    _defaultConfig: function () {
        return BI.extend(BI.MultiPieChartSetting.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-charts-setting bi-pie-chart-setting"
        })
    },

    _init: function () {
        BI.MultiPieChartSetting.superclass._init.apply(this, arguments);
        var self = this, o = this.options, constant = BI.AbstractChartSetting;

        //显示组件标题
        this.showName = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Show_Chart_Title"),
            cls: "attr-names",
            logic: {
                dynamic: true
            }
        });
        this.showName.on(BI.Controller.EVENT_CHANGE, function () {
            self.widgetTitle.setVisible(this.isSelected());
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE);
        });

        //组件标题
        this.widgetName = BI.createWidget({
            type: "bi.sign_editor",
            cls: "title-input",
            width: 120
        });

        this.widgetName.on(BI.SignEditor.EVENT_CHANGE, function () {
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE)
        });

        //详细设置
        this.widgetNameStyle = BI.createWidget({
            type: "bi.show_title_detailed_setting_combo"
        });

        this.widgetNameStyle.on(BI.ShowTitleDetailedSettingCombo.EVENT_CHANGE, function () {
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE)
        });

        this.widgetTitle = BI.createWidget({
            type: "bi.left",
            items: [this.widgetName, this.widgetNameStyle],
            hgap: constant.SIMPLE_H_GAP
        });

        var widgetTitle = BI.createWidget({
            type: "bi.left",
            cls: "single-line-settings",
            items: BI.createItems([{
                type: "bi.vertical_adapt",
                items: [this.showName]
            }, {
                type: "bi.vertical_adapt",
                items: [this.widgetTitle]
            }], {
                height: constant.SINGLE_LINE_HEIGHT
            }),
            hgap: constant.SIMPLE_H_GAP
        });

        this.chartColor = BI.createWidget({
            type: "bi.chart_setting_select_color_combo",
            width: 130
        });
        this.chartColor.populate();

        this.chartColor.on(BI.ChartSettingSelectColorCombo.EVENT_CHANGE, function () {
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE);
        });

        //层级渐变
        this.chartStyle = BI.createWidget({
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
        this.chartStyle.on(BI.ButtonGroup.EVENT_CHANGE, function () {
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE);
        });

        this.gradientType = BI.createWidget({
            type: "bi.button_group",
            items: BI.createItems(BICst.MULTI_PIE_GRADIENT_STYLE_GROUP, {
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
        this.gradientType.on(BI.ButtonGroup.EVENT_CHANGE, function () {
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE);
        });

        //内径大小
        this.innerRadius = BI.createWidget({
            type: "bi.sign_editor",
            width: constant.EDITOR_WIDTH,
            height: constant.EDITOR_HEIGHT,
            cls: "unit-input",
            errorText: BI.i18nText("BI-Please_Enter_Number_1_To_100"),
            validationChecker: function (v) {
                if (BI.isNaturalNumber(v)) {
                    return BI.parseInt(v) <= 100 && BI.parseInt(v) >= 0;
                }
                return false;
            }
        });

        this.innerRadius.on(BI.SignEditor.EVENT_CONFIRM, function () {
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE);
        });

        //总角度
        this.totalAngle = BI.createWidget({
            type: "bi.segment",
            width: 180,
            height: constant.BUTTON_HEIGHT,
            items: BICst.PIE_TOTAL_ANGLE
        });

        this.totalAngle.on(BI.Segment.EVENT_CHANGE, function () {
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE);
        });

        //组件背景
        this.widgetBG = BI.createWidget({
            type: "bi.global_style_index_background"
        });
        this.widgetBG.on(BI.GlobalStyleIndexBackground.EVENT_CHANGE, function () {
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE);
        });

        var tableStyle = BI.createWidget({
            type: "bi.horizontal_adapt",
            columnSize: [80],
            cls: "single-line-settings",
            items: [{
                type: "bi.label",
                text: BI.i18nText("BI-Basic_Chart"),
                lgap: constant.SIMPLE_H_LGAP,
                textAlign: "left",
                cls: "line-title"
            }, {
                type: "bi.left",
                cls: "detail-style",
                items: BI.createItems([{
                    type: "bi.label",
                    text: BI.i18nText("BI-Color_Setting"),
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.chartColor]
                }, {
                    type: "bi.label",
                    text: BI.i18nText("BI-Style"),
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.chartStyle]
                }, {
                    type: "bi.label",
                    text: BI.i18nText("BI-Hierarchical_Gradient"),
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.gradientType]
                }, {
                    type: "bi.label",
                    text: BI.i18nText("BI-Inner_Radius_Size"),
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.innerRadius]
                }, {
                    type: "bi.label",
                    text: BI.i18nText("BI-Total_Angle"),
                    lgap: constant.SIMPLE_H_GAP,
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.totalAngle]
                }, {
                    type: "bi.label",
                    text: BI.i18nText("BI-Widget_Background_Colour"),
                    cls: "attr-names",
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.widgetBG]
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
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE);
        });

        //图例详细设置
        this.legendStyle = BI.createWidget({
            type: "bi.legend_detailed_setting_combo"
        });

        this.legendStyle.on(BI.LegendDetailedSettingCombo.EVENT_CHANGE, function () {
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE)
        });

        //数据标签
        this.showDataLabel = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Show_Data_Label"),
            width: 115
        });

        this.showDataLabel.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE);
        });

        //数据点提示详细设置
        this.tooltipStyle = BI.createWidget({
            type: "bi.tooltip_detailed_setting_combo",
            wId: o.wId,
        });

        this.tooltipStyle.on(BI.TooltipDetailedSettingCombo.EVENT_CHANGE, function () {
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE)
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
                    items: [this.legendStyle]
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.showDataLabel]
                }/*, {
                 type: "bi.label",
                 text: BI.i18nText("BI-Tooltip"),
                 cls: "attr-names"
                 }, {
                 type: "bi.vertical_adapt",
                 items: [this.tooltipSetting]
                 }*/], {
                    height: constant.SINGLE_LINE_HEIGHT
                }),
                lgap: constant.SIMPLE_H_GAP
            }]
        });

        //联动传递过滤条件
        this.transferFilter = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Bind_Target_Condition"),
            width: 170
        });
        this.transferFilter.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.MultiPieChartSetting.EVENT_CHANGE);
        });

        this.clickZoom = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Click_Zoom"),
            width: 150
        });

        this.clickZoom.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.RectTreeChartSetting.EVENT_CHANGE)
        });

        var otherAttr = BI.createWidget({
            type: "bi.left_right_vertical_adapt",
            cls: "single-line-settings",
            items: {
                left: [{
                    type: "bi.label",
                    text: BI.i18nText("BI-Interactive_Attr"),
                    cls: "line-title"
                }, this.transferFilter, this.clickZoom]
            },
            height: constant.SINGLE_LINE_HEIGHT,
            lhgap: constant.SIMPLE_H_GAP
        });

        BI.createWidget({
            type: "bi.vertical",
            element: this.element,
            items: [widgetTitle, tableStyle, showElement, otherAttr],
            hgap: 10
        })
    },

    populate: function () {
        var wId = this.options.wId;
        this.showName.setSelected(BI.Utils.getWSShowNameByID(wId));
        this.widgetTitle.setVisible(BI.Utils.getWSShowNameByID(wId));
        this.widgetName.setValue(BI.Utils.getWidgetNameByID(wId));
        this.widgetNameStyle.setValue(BI.Utils.getWSTitleDetailSettingByID(wId));

        this.widgetBG.setValue(BI.Utils.getWSWidgetBGByID(wId));
        this.chartColor.setValue(BI.Utils.getWSChartColorByID(wId));
        this.chartStyle.setValue(BI.Utils.getWSChartStyleByID(wId));
        this.gradientType.setValue(BI.Utils.getWSMultiPieGradienTypeByID(wId));
        this.totalAngle.setValue(BI.Utils.getWSChartTotalAngleByID(wId));
        this.innerRadius.setValue(BI.Utils.getWSChartInnerRadiusByID(wId));
        this.legend.setValue(BI.Utils.getWSChartLegendByID(wId));
        this.legendStyle.setValue(BI.Utils.getWSChartLegendStyleByID(wId));
        this.showDataLabel.setSelected(BI.Utils.getWSChartShowDataLabelByID(wId));

        this.transferFilter.setSelected(BI.Utils.getWSTransferFilterByID(wId));
        this.clickZoom.setSelected(BI.Utils.getWSChartClickZoomByID(wId));
    },

    getValue: function () {
        return {
            showName: this.showName.isSelected(),
            widgetName: this.widgetName.getValue(),
            widgetNameStyle: this.widgetNameStyle.getValue(),
            widgetBG: this.widgetBG.getValue(),

            chartColor: this.chartColor.getValue()[0],
            chartStyle: this.chartStyle.getValue()[0],
            gradientType: this.gradientType.getValue()[0],
            //pieChartType: this.pieChartType.getValue()[0],
            totalAngle: this.totalAngle.getValue()[0],
            innerRadius: this.innerRadius.getValue(),
            legend: this.legend.getValue()[0],
            legendStyle: this.legendStyle.getValue(),
            showDataLabel: this.showDataLabel.isSelected(),

            transferFilter: this.transferFilter.isSelected(),
            clickZoom: this.clickZoom.isSelected()
        }
    }
});
BI.MultiPieChartSetting.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.multi_pie_chart_setting", BI.MultiPieChartSetting);
