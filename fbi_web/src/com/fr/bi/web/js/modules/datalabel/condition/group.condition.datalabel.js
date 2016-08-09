/**
 * Created by lfhli on 2016/7/15.
 */
BI.DataLabelConditionGroup = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        var conf = BI.DataLabelConditionGroup.superclass._defaultConfig.apply(this, arguments);
        return BI.extend(conf, {
            baseCls: "data-label-group"
        });
    },

    _init: function () {
        BI.DataLabelConditionGroup.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.buttonGroup = BI.createWidget({
            type: "bi.button_group",
            cls: "",
            element: this.element,
            items: o.items,
            layouts: [{
                type: "bi.vertical",
                hgap: 10
            }]
        });

        this.buttons = this.buttonGroup.getAllButtons();
    },

    addItem: function () {
        var o = this.options;
        var item = {
            type: "bi.data_label_no_type_field_filter_item",
            dId: o.dId
        };
        this.buttonGroup.addItems([item]);
        this.buttons = this.buttonGroup.getAllButtons();
    },

    populate: function () {
        var o = this.options;
        var conditions = BI.Utils.getDatalabelByID(o.dId);
        var items = [];
        BI.each(conditions, function (idx, cdt) {
            var type = BI.DataLabelFilterItemFactory.createFilterItemByFilterType(cdt.filter_type);
            items.push({
                type: type.type,
                dId: o.dId,
                field_id: cdt.field_id,
                filter_type: cdt.filter_type,
                filter_value: cdt.filter_value,
                filter_range: cdt.filter_range,
                style_setting: cdt.style_setting
            });
        });

        this.buttonGroup.addItems(items);
        this.buttons = this.buttonGroup.getAllButtons();
    },

    getValue: function () {
        var result = [];
        BI.each(this.buttons, function (i, el) {
            if (el.getValue() !== "") {
                result.push(el.getValue());
            }
        });
        return result;
    }
});
BI.DataLabelConditionGroup.EVENT_CHANGE = "BI.DataLabelConditionGroup.EVENT_CHANGE";
$.shortcut("bi.data_label_condition_group", BI.DataLabelConditionGroup);