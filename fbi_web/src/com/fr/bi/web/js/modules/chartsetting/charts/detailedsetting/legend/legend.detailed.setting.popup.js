/**
 * 图例详细设置
 * Created by AstronautOO7 on 2016/10/12.
 */
BI.LegendDetailedSettingPopup = BI.inherit(BI.Widget, {

    _defaultConfig: function() {
        return BI.extend(BI.LegendDetailedSettingPopup.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-detailed-setting bi-legend-detailed-setting-popup"
        })
    },

    _init: function() {
        BI.LegendDetailedSettingPopup.superclass._init.apply(this, arguments);
        var self = this;

        //字体设置
        this.textStyle = BI.createWidget({
            type: "bi.data_label_text_toolbar",
            cls: "detailed-setting-popup",
            width: 230
        });
        this.textStyle.on(BI.DataLabelTextToolBar.EVENT_CHANGE, function () {
            self.fireEvent(BI.LegendDetailedSettingPopup.EVENT_CHANGE)
        });
        var textStyleWrapper = this._createWrapper(BI.i18nText("BI-Set_Font"), this.textStyle);

        this.centerItems = BI.createWidget({
            type: "bi.vertical",
            element: this.element,
            items: [
                textStyleWrapper
            ],
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
            legend_style: this.textStyle.getValue()
        }
    },

    setValue: function(v) {
        v || (v = {});
        this.textStyle.setValue(v.legend_style)
    }

});
BI.LegendDetailedSettingPopup.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.legend_detailed_setting_popup", BI.LegendDetailedSettingPopup);