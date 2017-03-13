/**
 * @class BI.DetailStringDimensionCombo
 * @extend BI.Widget
 * 明细表文本维度的combo
 */
BI.DetailStringDimensionCombo = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.DetailStringDimensionCombo.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-detail-string-dimension-combo"
        })
    },

    _rebuildItems: function(){
        var o = this.options;
        var fieldId = BI.Utils.getFieldIDByDimensionID(o.dId);
        var fieldName = BI.Utils.getFieldNameByID(fieldId);
        var tableName = BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(fieldId));
        return [
            [{
                text: BI.i18nText("BI-Filter_Setting"),
                value: BICst.DETAIL_STRING_COMBO.FILTER,
                cls: "filter-h-font"
            }],
            [{
                text: BI.i18nText("BI-Hyperlink"),
                value: BICst.DETAIL_STRING_COMBO.HYPERLINK,
                cls: "hyper-link-font"
            }],
            [{
                text: BI.i18nText("BI-Show_Field"),
                value: BICst.DETAIL_STRING_COMBO.SHOW_FIELD,
                cls: BI.Utils.isDimensionUsable(this.options.dId) ? "widget-combo-show-title-font" : ""
            }],
            [{
                text: BI.i18nText("BI-Rename"),
                value: BICst.DETAIL_STRING_COMBO.RENAME
            }],
            [{
                text: BI.i18nText("BI-Remove"),
                value: BICst.DETAIL_STRING_COMBO.DELETE,
                cls: "delete-h-font"
            }],
            [{
                text: BI.i18nText("BI-This_Target_From") + ":" + tableName + "."  + fieldName,
                title: BI.i18nText("BI-This_Target_From") + ":" + tableName + "."  + fieldName,
                tipType: "success",
                cls: "dimension-from-font",
                value: BICst.DETAIL_STRING_COMBO.INFO,
                disabled: true
            }]
        ];
    },

    _init: function () {
        BI.DetailStringDimensionCombo.superclass._init.apply(this, arguments);
        var self = this, o = this.options;

        this.combo = BI.createWidget({
            type: "bi.down_list_combo",
            element: this.element,
            height: 25,
<<<<<<< HEAD
            iconCls: "detail-dimension-set-font"
=======
            iconCls: "detail-dimension-set-font",
            items: [
                [{
                    text: BI.i18nText("BI-Filter_Setting"),
                    value: BICst.DETAIL_STRING_COMBO.FILTER,
                    cls: "filter-h-font"
                }],
                [{
                    text: BI.i18nText("BI-Hyperlink"),
                    value: BICst.DETAIL_STRING_COMBO.HYPERLINK,
                    cls: "hyper-link-font"
                }],
                [{
                    text: BI.i18nText("BI-Remove"),
                    value: BICst.DETAIL_STRING_COMBO.DELETE,
                    cls: "delete-h-font"
                }],
                [{
                    text: BI.i18nText("BI-This_Target_From") + ":" + tableName + "."  + fieldName,
                    title: BI.i18nText("BI-This_Target_From") + ":" + tableName + "."  + fieldName,
                    tipType: "success",
                    cls: "dimension-from-font",
                    value: BICst.DETAIL_STRING_COMBO.INFO,
                    disabled: true
                }]
            ]
>>>>>>> 67b55d486e769f445942f15883303ca839ffd092
        });
        this.combo.on(BI.DownListCombo.EVENT_CHANGE, function (v) {
            self.fireEvent(BI.DetailStringDimensionCombo.EVENT_CHANGE, v);
        });

        this.combo.on(BI.DownListCombo.EVENT_BEFORE_POPUPVIEW,function(){
            this.populate(self._rebuildItems());
        });
    },

    getValue: function () {
        return this.combo.getValue();
    }

});
BI.DetailStringDimensionCombo.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.detail_string_dimension_combo", BI.DetailStringDimensionCombo);