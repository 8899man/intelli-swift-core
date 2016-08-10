/**
 * Created by GUY on 2016/2/2.
 *
 * @class BI.MapTypeCombo
 * @extend BI.Widget
 */
BI.MapTypeCombo = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.MapTypeCombo.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-map-type-combo",
            width: 25,
            height: 25
        })
    },

    _init: function () {
        BI.MapTypeCombo.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.trigger = BI.createWidget({
            type: "bi.icon_combo_trigger",
            iconClass: "drag-map-china-icon",
            title: o.title,
            items: o.items,
            width: o.width,
            height: o.height,
            iconWidth: o.iconWidth,
            iconHeight: o.iconHeight
        });
        this.popup = BI.createWidget({
            type: "bi.map_type_popup"
        });
        this.popup.on(BI.MapTypePopup.EVENT_CHANGE, function () {
            self.setValue(self.popup.getValue());
            self.mapTypeCombo.hideView();
            self.fireEvent(BI.MapTypeCombo.EVENT_CHANGE);
        });
        this.popup.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.Controller.EVENT_CHANGE, arguments);
        });
        this.mapTypeCombo = BI.createWidget({
            type: "bi.combo",
            element: this.element,
            direction: "bottom",
            adjustLength: 3,
            el: this.trigger,
            popup: {
                el: this.popup,
                maxHeight: 300
            }
        });
    },

    showView: function () {
        this.mapTypeCombo.showView();
    },

    hideView: function () {
        this.mapTypeCombo.hideView();
    },

    setValue: function (v) {
        this.mapTypeCombo.setValue(v);
    },

    setEnable: function (v) {
        BI.MapTypeCombo.superclass.setEnable.apply(this, arguments);
        this.mapTypeCombo.setEnable(v);
    },

    getValue: function () {
        return this.mapTypeCombo.getValue();
    }
});
BI.MapTypeCombo.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.map_type_combo", BI.MapTypeCombo);