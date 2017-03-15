/**
 * 地图数据标签详细设置的popup
 * Created by AstronautOO7 on 2016/11/28.
 */
BI.MapDataLabelDetailedSettingPopup = BI.inherit(BI.Widget, {

    _defaultConfig: function() {
        return BI.extend(BI.MapDataLabelDetailedSettingPopup.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-detailed-setting bi-map-data-label-detailed-setting-popup"
        })
    },

    _init: function() {
        BI.MapDataLabelDetailedSettingPopup.superclass._init.apply(this, arguments);
        var self = this, o = this.options;

        //分类名
        this.showBlockName = BI.createWidget({
            type: "bi.multi_select_item",
            width: 75,
            value: BI.i18nText("BI-Category_Name")
        });

        this.showBlockName.on(BI.Controller.EVENT_CHANGE, function() {
            o.onChange();
        });

        //系列名
        this.showSeriesName = BI.createWidget({
            type: "bi.multi_select_item",
            width: 75,
            value: BI.i18nText("BI-Series_Name")
        });

        this.showSeriesName.on(BI.Controller.EVENT_CHANGE, function() {
            o.onChange();
        });

        //值
        this.showSize = BI.createWidget({
            type: "bi.multi_select_item",
            width: 50,
            value: BI.i18nText("BI-Value")
        });

        this.showSize.on(BI.Controller.EVENT_CHANGE, function() {
            o.onChange();
        });

        //百分比
        this.showPercentage = BI.createWidget({
            type: "bi.multi_select_item",
            width: 75,
            value: BI.i18nText("BI-Percentage")
        });

        this.showPercentage.on(BI.Controller.EVENT_CHANGE, function() {
            o.onChange();
        });

        var show = BI.createWidget({
            type: "bi.left",
            items: [this.showBlockName, this.showSeriesName, this.showSize, this.showPercentage]
        });

        var showWrapper = this._createWrapper(BI.i18nText("BI-Show_Content"), show);

        //字体设置
        this.textStyle = BI.createWidget({
            type: "bi.data_label_text_toolbar",
            cls: "detailed-setting-popup",
            width: 230
        });
        this.textStyle.on(BI.DataLabelTextToolBar.EVENT_CHANGE, function () {
            o.onChange();
        });
        var textStyleWrapper = this._createWrapper(BI.i18nText("BI-Set_Font"), this.textStyle);

        this.centerItems = BI.createWidget({
            type: "bi.vertical",
            element: this.element,
            items: BI.createItems([
                showWrapper,
                textStyleWrapper
            ]),
            hgap: 5
        });
    },

    _createWrapper: function (name, widget) {
        return {
            type: "bi.left",
            items: [{
                type: "bi.label",
                text: name + ":",
                textAlign: "left",
                height: 30,
                width: 60
            }, widget],
            vgap: 5
        }
    },

    getValue: function() {
        return {
            showBlockName: this.showBlockName.isSelected(),
            showSeriesName: this.showSeriesName.isSelected(),
            showSize: this.showSize.isSelected(),
            showPercentage: this.showPercentage.isSelected(),
            textStyle: this.textStyle.getValue(),
        }
    },

    setValue: function(v) {
        v || (v = {});
        this.showBlockName.setSelected(v.showBlockName);
        this.showSeriesName.setSelected(v.showSeriesName);
        this.showSize.setSelected(v.showSize);
        this.showPercentage.setSelected(v.showPercentage);
        this.textStyle.setValue(v.textStyle)
    }

});
$.shortcut("bi.map_data_label_detailed_setting_popup", BI.MapDataLabelDetailedSettingPopup);