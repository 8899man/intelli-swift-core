/**
 * @class BI.MapSetting
 * @extends BI.Widget
 * 地图样式
 */
BI.MapSetting = BI.inherit(BI.AbstractChartSetting, {

    _defaultConfig: function () {
        return BI.extend(BI.MapSetting.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-charts-setting bi-map-chart-setting"
        })
    },

    _init: function () {
        BI.MapSetting.superclass._init.apply(this, arguments);
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
            self.fireEvent(BI.MapSetting.EVENT_CHANGE);
        });

        //组件标题
        this.widgetName = BI.createWidget({
            type: "bi.sign_editor",
            cls: "title-input",
            width: 120
        });

        this.widgetName.on(BI.SignEditor.EVENT_CHANGE, function () {
            self.fireEvent(BI.MapSetting.EVENT_CHANGE)
        });

        //详细设置
        this.widgetNameStyle = BI.createWidget({
            type: "bi.show_title_detailed_setting_combo"
        });

        this.widgetNameStyle.on(BI.ShowTitleDetailedSettingCombo.EVENT_CHANGE, function () {
            self.fireEvent(BI.MapSetting.EVENT_CHANGE)
        });

        this.widgetTitle = BI.createWidget({
            type: "bi.left",
            items: [this.widgetName, this.widgetNameStyle],
            hgap: constant.SIMPLE_H_GAP
        });

        //组件背景
        this.widgetBG = BI.createWidget({
            type: "bi.global_style_index_background"
        });
        this.widgetBG.on(BI.GlobalStyleIndexBackground.EVENT_CHANGE, function () {
            self.fireEvent(BI.MapSetting.EVENT_CHANGE);
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
            }, {
                type: "bi.label",
                text: BI.i18nText("BI-Basic_Background"),
                cls: "line-title",
            },{
                type: "bi.vertical_adapt",
                items: [this.widgetBG]
            }], {
                height: constant.SINGLE_LINE_HEIGHT
            }),
            hgap: constant.SIMPLE_H_GAP
        });

        //主题颜色
        this.chartColor = BI.createWidget({
            type: "bi.color_chooser",
            width: constant.BUTTON_HEIGHT,
            height: constant.BUTTON_HEIGHT
        });

        this.chartColor.on(BI.ColorChooser.EVENT_CHANGE, function () {
            self.fireEvent(BI.MapSetting.EVENT_CHANGE);
        });

        var theme = BI.createWidget({
            type: "bi.horizontal_adapt",
            columnSize: [80],
            cls: "single-line-settings",
            items: [{
                type: "bi.label",
                text: BI.i18nText("BI-Basic_Chart"),
                lgap: 5,
                textAlign: "left",
                textHeight: 60,
                cls: "line-title"
            }, {
                type: "bi.left",
                cls: "detail-style",
                items: BI.createItems([{
                    type: "bi.label",
                    text: BI.i18nText("BI-Theme_Color"),
                    lgap: 0,
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.chartColor]
                }], {
                    height: constant.SINGLE_LINE_HEIGHT
                }),
                lgap: constant.SIMPLE_H_GAP
            }]
        });

        this.styleRadio = BI.createWidget({
            type: "bi.button_group",
            items: BI.createItems(BICst.CHART_SCALE_SETTING, {
                type: "bi.single_select_radio_item",
                width: 100,
                height: constant.BUTTON_HEIGHT
            }),
            layouts: [{
                type: "bi.horizontal_adapt",
                height: constant.BUTTON_HEIGHT
            }]
        });

        this.styleRadio.on(BI.ButtonGroup.EVENT_CHANGE, function (v) {
            self._doClickButton(v);
            self.fireEvent(BI.MapSetting.EVENT_CHANGE)
        });

        this.addConditionButton = BI.createWidget({
            type: "bi.button",
            text: BI.i18nText("BI-Add_Condition"),
            height: constant.BUTTON_HEIGHT
        });

        this.addConditionButton.on(BI.Button.EVENT_CHANGE, function () {
            self.conditions.addItem();
            self.fireEvent(BI.MapSetting.EVENT_CHANGE)
        });

        this.mapStyles = BI.createWidget({
            type: "bi.chart_add_condition_group",
            width: "100%"
        });

        this.mapStyles.on(BI.ChartAddConditionGroup.EVENT_CHANGE, function () {
            self.fireEvent(BI.MapSetting.EVENT_CHANGE)
        });

        var interval = BI.createWidget({
            type: "bi.left",
            cls: "detail-style",
            items: BI.createItems([{
                type: "bi.vertical_adapt",
                items: [this.styleRadio]
            }, {
                type: "bi.vertical_adapt",
                items: [this.addConditionButton]
            }, this.mapStyles], {
                height: constant.SINGLE_LINE_HEIGHT
            }),
            lgap: constant.SIMPLE_H_GAP
        });

        var intervalSetting = BI.createWidget({
            type: "bi.horizontal_adapt",
            cls: "single-line-settings",
            columnSize: [80],
            verticalAlign: "top",
            items: [{
                type: "bi.label",
                text: BI.i18nText("BI-Interval_Setting"),
                textHeight: constant.SINGLE_LINE_HEIGHT,
                textAlign: "left",
                lgap: 5,
                cls: "line-title"
            }, interval]
        });

        //图例
        this.legend = BI.createWidget({
            type: "bi.segment",
            width: constant.LEGEND_SEGMENT_WIDTH,
            height: constant.BUTTON_HEIGHT,
            items: BICst.CHART_LEGEND
        });

        this.legend.on(BI.Segment.EVENT_CHANGE, function () {
            self.fireEvent(BI.MapSetting.EVENT_CHANGE);
        });

        //数据标签
        this.showDataLabel = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Show_Data_Label"),
            logic: {
                dynamic: true
            }
        });

        this.showDataLabel.on(BI.Controller.EVENT_CHANGE, function () {
            self.dataLabelSetting.setVisible(this.isSelected());
            self.fireEvent(BI.MapSetting.EVENT_CHANGE);
        });

        this.dataLabelSetting = BI.createWidget({
            type: "bi.data_label_detailed_setting_combo",
            wId: o.wId,
        });

        this.dataLabelSetting.on(BI.DataLabelDetailedSettingCombo.EVENT_CHANGE, function () {
            self.fireEvent(BI.MapSetting.EVENT_CHANGE);
        });

        //显示背景图层
        this.isShowBackgroundLayer = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-SHOW_BACKGROUND_LAYER"),
            logic: {
                dynamic: true
            }
        });

        this.isShowBackgroundLayer.on(BI.Controller.EVENT_CHANGE, function () {
            this.isSelected() ? self.backgroundLayerInfo.setVisible(true) : self.backgroundLayerInfo.setVisible(false);
            self.fireEvent(BI.MapSetting.EVENT_CHANGE);
        });

        this.backgroundLayerInfo = BI.createWidget({
            type: "bi.text_value_combo",
            width: constant.COMBO_WIDTH,
            height: constant.EDITOR_HEIGHT
        });
        this.backgroundLayerInfo.on(BI.TextValueCombo.EVENT_CHANGE, function () {
            self.fireEvent(BI.MapSetting.EVENT_CHANGE);
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
                textHeight: 60,
                cls: "line-title"
            }, {
                type: "bi.left",
                cls: "detail-style",
                items: BI.createItems([{
                    type: "bi.label",
                    text: BI.i18nText("BI-Legend_Normal"),
                    lgap: constant.SIMPLE_H_GAP,
                    cls: "attr-names"
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.legend]
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.showDataLabel]
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.dataLabelSetting]
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.isShowBackgroundLayer]
                }, {
                    type: "bi.vertical_adapt",
                    items: [this.backgroundLayerInfo]
                }], {
                    height: constant.SINGLE_LINE_HEIGHT
                }),
                lgap: constant.SIMPLE_H_GAP
            }]
        });

        //联动传递过滤条件
        this.transferFilter = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Bind_Target_Condition"),
            logic: {
                dynamic: true
            }
        });
        this.transferFilter.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.MapSetting.EVENT_CHANGE);
        });

        var otherAttr = BI.createWidget({
            type: "bi.left",
            cls: "single-line-settings",
            items: BI.createItems([{
                type: "bi.label",
                text: BI.i18nText("BI-Interactive_Attr"),
                cls: "line-title",
                lgap: constant.SIMPLE_H_GAP
            }, {
                type: "bi.vertical_adapt",
                items: [this.transferFilter],
                lgap: 30
            }], {
                height: constant.SINGLE_LINE_HEIGHT
            })
        });

        BI.createWidget({
            type: "bi.vertical",
            element: this.element,
            items: [widgetTitle, theme, intervalSetting, showElement, otherAttr],
            hgap: constant.SIMPLE_H_GAP
        })
    },

    _doClickButton: function (v) {
        switch (v) {
            case BICst.SCALE_SETTING.AUTO:
                this.addConditionButton.setVisible(false);
                this.mapStyles.setVisible(false);
                break;
            case BICst.SCALE_SETTING.CUSTOM:
                this.addConditionButton.setVisible(true);
                this.mapStyles.setVisible(true);
                break;
        }
    },

    _setNumberLevel: function () {
        var wId = this.options.wId;
        var targetIDs = BI.Utils.getAllUsableTargetDimensionIDs(wId);
        var styleSettings = BI.Utils.getDimensionSettingsByID(targetIDs[0]);
        switch (styleSettings.numLevel) {
            case BICst.TARGET_STYLE.NUM_LEVEL.NORMAL:
                this.mapStyles.setNumTip("");
                break;
            case BICst.TARGET_STYLE.NUM_LEVEL.TEN_THOUSAND:
                this.mapStyles.setNumTip(BI.i18nText("BI-Basic_Wan"));
                break;
            case BICst.TARGET_STYLE.NUM_LEVEL.MILLION:
                this.mapStyles.setNumTip(BI.i18nText("BI-Basic_Million"));
                break;
            case BICst.TARGET_STYLE.NUM_LEVEL.YI:
                this.mapStyles.setNumTip(BI.i18nText("BI-Basic_Yi"));
                break;
            case BICst.TARGET_STYLE.NUM_LEVEL.PERCENT:
                this.mapStyles.setNumTip(BI.i18nText("%"));
                break;
        }
    },

    populate: function () {
        var wId = this.options.wId;
        this.showName.setSelected(BI.Utils.getWSShowNameByID(wId));
        this.widgetTitle.setVisible(BI.Utils.getWSShowNameByID(wId));
        this.widgetName.setValue(BI.Utils.getWidgetNameByID(wId));
        this.widgetNameStyle.setValue(BI.Utils.getWSTitleDetailSettingByID(wId));

        this.widgetBG.setValue(BI.Utils.getWSWidgetBGByID(wId));
        this.chartColor.setValue(BI.Utils.getWSThemeColorByID(wId));
        this.styleRadio.setValue(BI.Utils.getWSChartStyleRadioByID(wId));
        this._doClickButton(BI.Utils.getWSChartStyleRadioByID(wId));
        this.mapStyles.setValue(BI.Utils.getWSChartMapStylesByID(wId));
        this.legend.setValue(BI.Utils.getWSChartLegendByID(wId));
        this.showDataLabel.setSelected(BI.Utils.getWSChartShowDataLabelByID(wId));
        this.dataLabelSetting.setValue(BI.Utils.getWSChartDataLabelSettingByID(wId));
        this.dataLabelSetting.setVisible(BI.Utils.getWSChartShowDataLabelByID(wId));
        this.isShowBackgroundLayer.setSelected(BI.Utils.getWSShowBackgroundByID(wId));
        this.isShowBackgroundLayer.isSelected() ? this.backgroundLayerInfo.setVisible(true) : this.backgroundLayerInfo.setVisible(false);
        this._setNumberLevel();
        var items = BI.map(MapConst.WMS_INFO, function (name, obj) {
            if (obj.type === BICst.TILELAYER_SERVER) {
                return {
                    text: name,
                    title: name,
                    value: name
                }
            }
            if (obj.type === BICst.WMS_SERVER) {
                return {
                    text: name,
                    title: name,
                    value: name
                }
            }
        });
        this.backgroundLayerInfo.populate(items);
        this.backgroundLayerInfo.setValue(BI.Utils.getWSChartMapBackgroundLayerInfoByID(wId));

        this.transferFilter.setSelected(BI.Utils.getWSTransferFilterByID(wId));
    },

    getValue: function () {
        return {
            showName: this.showName.isSelected(),
            widgetName: this.widgetName.getValue(),
            widgetNameStyle: this.widgetNameStyle.getValue(),

            widgetBG: this.widgetBG.getValue(),
            chartColor: this.chartColor.getValue(),
            styleRadio: this.styleRadio.getValue()[0],
            mapStyles: this.mapStyles.getValue(),
            legend: this.legend.getValue()[0],
            showDataLabel: this.showDataLabel.isSelected(),
            dataLabelSetting: this.dataLabelSetting.getValue(),
            isShowBackgroundLayer: this.isShowBackgroundLayer.isSelected(),
            backgroundLayerInfo: this.backgroundLayerInfo.getValue()[0],

            transferFilter: this.transferFilter.isSelected(),
        }
    }
});
BI.MapSetting.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.map_setting", BI.MapSetting);
