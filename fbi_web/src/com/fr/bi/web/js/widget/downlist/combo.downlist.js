/**
 * Created by roy on 15/8/14.
 */
BI.DownListCombo = BI.inherit(BI.Widget, {
    constants: {
        height: 25
    },
    _defaultConfig: function () {
        return BI.extend(BI.DownListCombo.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-down-list-combo",
            invalid: false,
            items: [],
            el:{}
        })
    },

    _init: function () {
        BI.DownListCombo.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.popupview = BI.createWidget({
            type: "bi.down_list_popup",
            items: o.items,
            chooseType: o.chooseType
        });

        this.popupview.on(BI.DownListPopup.EVENT_CHANGE, function (value) {
            self.fireEvent(BI.DownListCombo.EVENT_CHANGE, value);
            self.downlistcombo.hideView();
        });

        this.popupview.on(BI.DownListPopup.EVENT_SON_VALUE_CHANGE, function (value, fatherValue) {
            self.fireEvent(BI.DownListCombo.EVENT_SON_VALUE_CHANGE, value, fatherValue);
            self.downlistcombo.hideView();
        });


        this.downlistcombo = BI.createWidget({
            element: this.element,
            type: 'bi.combo',
            isNeedAdjustWidth: false,

            el: BI.createWidget(o.el, {
                type: "bi.icon_trigger",
                extraCls: o.iconCls ? o.iconCls : "pull-down-font",
                height: this.constants.height
            }),
            popup: {
                el: this.popupview,
                stopPropagation: true,
                maxHeight: 400
            }
        });

        this.downlistcombo.on(BI.Combo.EVENT_BEFORE_POPUPVIEW,function(){
            self.fireEvent(BI.DownListCombo.EVENT_BEFORE_POPUPVIEW);
        });
    },

    populate: function (items) {
        this.popupview.populate(items);
    },

    setValue: function (v) {
        this.popupview.setValue(v);
    },
    getValue: function () {
        return this.popupview.getValue()
    }
});
BI.DownListCombo.EVENT_CHANGE = "EVENT_CHANGE";
BI.DownListCombo.EVENT_SON_VALUE_CHANGE = "EVENT_SON_VALUE_CHANGE";
BI.DownListCombo.EVENT_BEFORE_POPUPVIEW = "EVENT_BEFORE_POPUPVIEW";

$.shortcut("bi.down_list_combo", BI.DownListCombo);