/**
 * Created by fay on 2016/8/1.
 */
BI.DataLabelFilterSelectField = BI.inherit(BI.Widget, {
    _constant: {
        SELF_FIELD: 1,
        OTHER_FIELD: 2
    },

    _defaultConfig: function () {
        return BI.extend(BI.DataLabelFilterSelectField.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-data-label-filter-select-field"
        });
    },

    _init: function () {
        BI.DataLabelFilterSelectField.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.service = BI.createWidget({
            type: "bi.simple_select_data_service",
            element: this.element,
            isDefaultInit: true,
            tablesCreator: function () {
                return [{
                    id: self._constant.SELF_FIELD,
                    type: "bi.dimension_select_data_level0_node",
                    text: BI.i18nText("BI-Base_Self"),
                    value: BI.i18nText("BI-Base_Self"),
                    isParent: true,
                    fontType: BI.DimensionSelectDataLevel0Node.SERIES,
                    open: true
                }, {
                    id: self._constant.OTHER_FIELD,
                    type: "bi.dimension_select_data_level0_node",
                    text: BI.i18nText("BI-Basic_Dimension") + "/" + BI.i18nText("BI-Base_Target"),
                    value: BI.i18nText("BI-Basic_Dimension") + "/" + BI.i18nText("BI-Base_Target"),
                    isParent: true,
                    fontType: BI.DimensionSelectDataLevel0Node.CLASSIFY,
                    open: true
                }]
            },
            fieldsCreator: function (tableId) {
                if (self._constant.SELF_FIELD === tableId) {
                    return [{
                        id: o.dId,
                        pId: self._constant.SELF_FIELD,
                        type: "bi.select_data_level0_item",
                        fieldType: BI.Utils.getFieldTypeByDimensionID(o.dId),
                        text: BI.i18nText("BI-Base_Self"),
                        title: BI.i18nText("BI-Base_Self"),
                        value: o.dId
                    }]
                } else {
                    var widgetId = BI.Utils.getWidgetIDByDimensionID(o.dId);
                    var result = [];
                    BI.each(BI.Utils.getAllDimDimensionIDs(widgetId), function (i, dId) {
                        result.push({
                            id: dId,
                            pId: self._constant.OTHER_FIELD,
                            type: "bi.select_data_level0_item",
                            fieldType: BI.Utils.getFieldTypeByDimensionID(dId),
                            text: BI.Utils.getDimensionNameByID(dId),
                            title: BI.Utils.getDimensionNameByID(dId),
                            value: dId
                        });
                    });
                    return result;
                }
            }
        });
        this.service.on(BI.SimpleSelectDataService.EVENT_CLICK_ITEM, function () {
            self.fireEvent(BI.DataLabelFilterSelectField.EVENT_CLICK_ITEM, arguments);
        });
    }
});
BI.DataLabelFilterSelectField.EVENT_CLICK_ITEM = "EVENT_CLICK_ITEM";
$.shortcut("bi.data_label_filter_select_field", BI.DataLabelFilterSelectField);