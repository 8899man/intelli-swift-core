/**
 * custom scale
 * Created by GameJian on 2016/7/19.
 */
BI.ComboCustomScale = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.ComboCustomScale.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-combo-custom-scale"
        })
    },

    _init: function () {
        BI.ComboCustomScale.superclass._init.apply(this, arguments);
        var self = this, o = this.options;

        this.label = BI.createWidget({
            type: "bi.label",
            cls: "combo-custom-scale-label",
            text: o.text,
            textHeight: 25,
            textAlign: "left"
        });

        this.pane = BI.createWidget({
            type: "bi.custom_scale_formula_pane",
            wId: o.wId,
            width: 500,
            height: 360
        });

        this.pane.on(BI.CustomScaleFormulaPane.EVENT_CHANGE, function () {
            self._calculate(self.pane.getAnalyzeContent());
            self.combo.hideView();
            self.fireEvent(BI.ComboCustomScale.EVENT_CHANGE)
        });

        this.pane.on(BI.CustomScaleFormulaPane.EVENT_VALUE_CANCEL, function () {
            self.combo.hideView()
        });

        this.trigger = BI.createWidget({
            type: "bi.custom_scale_trigger",
            text: o.text
        });

        this.trigger.on(BI.CustomScaleTrigger.EVENT_CHANGE, function () {
            self.fireEvent(BI.ComboCustomScale.EVENT_CHANGE)
        });

        this.combo = BI.createWidget({
            type: "bi.combo",
            width: 110,
            isNeedAdjustWidth: false,
            isNeedAdjustHeight: false,
            el: this.trigger,
            popup: {
                el: this.pane
            },
            adjustYOffset: 5
        });

        this.combo.on(BI.Combo.EVENT_AFTER_POPUPVIEW, function () {
            self.pane.refresh()
        });

        BI.createWidget({
            type: "bi.left_right_vertical_adapt",
            element: this.element,
            items: {
                left: [this.label],
                right: [this.combo]
            },
            rlgap: 10,
            rrgap: 20
        })
    },

    _calculate: function (v) {
        v = v || [];
        var formula = "";
        BI.each(v, function (id, item) {
            var fieldRegx = /\$[\{][^\]*[\}]/;
            var str = item.match(fieldRegx);
            if (BI.isNotEmptyArray(str)) {
                var value = BI.Utils.getCalculateValue(item.substring(2, 18));
                var type = item.substring(18, item.length - 1);
                formula += value[type]
            } else {
                formula += item
            }
        });
        this.trigger.setValue(eval(formula) || "")
    },

    setTitle: function (title) {
        this.trigger.setTitle(title)
    },

    getValue: function () {
        return {
            formula: this.pane.getValue(),
            scale: this.trigger.getValue()
        }
    },

    setValue: function (v) {
        this.pane.setValue(v.formula || "");
        this._calculate(this.pane.getAnalyzeContent())
    }
});
BI.ComboCustomScale.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.combo_custom_scale", BI.ComboCustomScale);