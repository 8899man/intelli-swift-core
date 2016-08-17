/**
 * @class BI.CountTargetCombo
 * @extend BI.Widget
 * 记录数指标下拉
 */
BI.CountTargetCombo = BI.inherit(BI.AbstractDimensionTargetCombo, {

    constants: {
        CHART_TYPE_POSITION: 1,
        CordonPos: 2
    },

    defaultItem: function(){
        var o = this.options;
        var fieldName = BI.Utils.getFieldNameByID(this.field_id);
        var tableName = BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(this.field_id));
        var fromText = BI.i18nText("BI-This_Target_From") + ": " + tableName + "."  + fieldName;
        return [
            [{
                el: {
                    text: BI.i18nText("BI-Count_Depend"),
                    value: BICst.TARGET_COMBO.DEPEND_TYPE,
                    iconCls1: ""
                },
                children: []
            }],
            [{
                el: {
                    text: BI.i18nText("BI-Chart_Type"),
                    value: BICst.TARGET_COMBO.CHART_TYPE,
                    iconCls1: "",
                    disabled: true
                },
                children: [{
                    text: BI.i18nText("BI-Column_Chart"),
                    value: BICst.WIDGET.AXIS,
                    cls: "dot-e-font"
                }, {
                    text: BI.i18nText("BI-Stacked_Chart"),
                    value: BICst.WIDGET.ACCUMULATE_AXIS,
                    cls: "dot-e-font"
                }, {
                    text: BI.i18nText("BI-Line_Chart"),
                    value: BICst.WIDGET.LINE,
                    cls: "dot-e-font"
                }, {
                    text: BI.i18nText("BI-Area_Chart"),
                    value: BICst.WIDGET.AREA,
                    cls: "dot-e-font"
                }, {
                    text: BI.i18nText("BI-Accumulate_Area"),
                    value: BICst.WIDGET.ACCUMULATE_AREA,
                    cls: "dot-e-font"
                }]
            }],
            [{
                text: BI.i18nText("BI-Style_Setting"),
                value: BICst.TARGET_COMBO.STYLE_SETTING,
                warningTitle: BI.i18nText("BI-Disable_in_Big_Data_Mode"),
                cls: "style-set-h-font"
            }],
            [{
                text: BI.i18nText("BI-Filter_Number_Summary"),
                value: BICst.TARGET_COMBO.FILTER,
                cls: "filter-h-font"
            }],
            //[{
            //    text: BI.i18nText("BI-Display"),
            //    value: BICst.TARGET_COMBO.DISPLAY,
            //    cls: "dot-ha-font"
            //}, {
            //    text: BI.i18nText("BI-Hidden"),
            //    value: BICst.TARGET_COMBO.HIDDEN,
            //    cls: "dot-ha-font"
            //}],
            [{
                text: BI.i18nText("BI-Copy"),
                value: BICst.TARGET_COMBO.COPY,
                cls: "copy-h-font"
            }],
            [{
                text: BI.i18nText("BI-Remove"),
                value: BICst.TARGET_COMBO.DELETE,
                cls: "delete-h-font"
            }],
            [{
                text: fromText,
                title: fromText,
                tipType: "success",
                value: BICst.TARGET_COMBO.INFO,
                cls: "dimension-from-font",
                disabled: true
            }]
        ]
    },

    _defaultConfig: function(){
        return BI.extend(BI.CountTargetCombo.superclass._defaultConfig.apply(this, arguments), {

        })
    },

    _init: function(){
        BI.CountTargetCombo.superclass._init.apply(this, arguments);
        this.field_id = BI.Utils.getFieldIDByDimensionID(this.options.dId);
    },

    _rebuildItems: function(){
        var o = this.options;
        var wId = BI.Utils.getWidgetIDByDimensionID(o.dId);
        var tableId = BI.Utils.getTableIDByDimensionID(o.dId);
        var fieldIds = BI.Utils.getStringFieldIDsOfTableID(tableId).concat(BI.Utils.getNumberFieldIDsOfTableID(tableId));
        var children = [];
        var minimalist = BI.Utils.getWSMinimalistByID(wId);
        var bigDataMode = BI.Utils.getWSBigDataModelByID(wId);
        children.push({
            text: BI.i18nText("BI-Total_Row_Count"),
            value: BI.Utils.getCountFieldIDsOfTableID(tableId),
            cls: "dot-e-font"
        });
        BI.each(fieldIds, function(idx, fieldId){
            children.push({
                text: BI.Utils.getFieldNameByID(fieldId),
                value: fieldId,
                cls: "dot-e-font"
            });
        });

        var id = BI.Utils.getFieldIDByDimensionID(o.dId);
        var selectedValue = BI.Utils.getFieldTypeByDimensionID(o.dId) !== BICst.COLUMN.COUNTER ? BI.Utils.getFieldNameByID(id) : BI.i18nText("BI-Total_Row_Count");

        var dependItem = {};

        var items = this.defaultItem();
        var wType = BI.Utils.getWidgetTypeByID(BI.Utils.getWidgetIDByDimensionID(this.options.dId));
        var regionType = BI.Utils.getRegionTypeByDimensionID(o.dId);
        switch (wType) {
            case BICst.WIDGET.AXIS:
            case BICst.WIDGET.ACCUMULATE_AXIS:
            case BICst.WIDGET.PERCENT_ACCUMULATE_AXIS:
            case BICst.WIDGET.COMPARE_AXIS:
            case BICst.WIDGET.FALL_AXIS:
            case BICst.WIDGET.LINE:
            case BICst.WIDGET.AREA:
            case BICst.WIDGET.ACCUMULATE_AREA:
            case BICst.WIDGET.COMPARE_AREA:
            case BICst.WIDGET.RANGE_AREA:
            case BICst.WIDGET.PERCENT_ACCUMULATE_AREA:
                items[this.constants.CordonPos][0].cls = "";
                items[this.constants.CordonPos][0] = {
                    el: items[this.constants.CordonPos][0],
                    children: [{
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Horizontal") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    }]
                };
                if(minimalist) {
                    items[this.constants.CordonPos][0].setWarningTitle(BI.i18nText("BI-Unmodified_in_Minimalist_Mode"));
                    items[this.constants.CordonPos][0].disabled = true
                }
                BI.removeAt(items, this.constants.CHART_TYPE_POSITION);
                break;
            case BICst.WIDGET.BAR:
            case BICst.WIDGET.ACCUMULATE_BAR:
            case BICst.WIDGET.COMPARE_BAR:
                items[this.constants.CordonPos][0].cls = "";
                items[this.constants.CordonPos][0] = {
                    el: items[this.constants.CordonPos][0],
                    children: [{
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Vertical") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    }]
                };
                if(minimalist) {
                    items[this.constants.CordonPos][0].disabled = true
                }
                BI.removeAt(items, this.constants.CHART_TYPE_POSITION);
                break;
            case BICst.WIDGET.COMBINE_CHART:
            case BICst.WIDGET.MULTI_AXIS_COMBINE_CHART:
                items[this.constants.CordonPos][0].cls = "";
                items[this.constants.CordonPos][0] = {
                    el: items[this.constants.CordonPos][0],
                    children: [{
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Horizontal") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    }]
                };
                if(minimalist) {
                    items[this.constants.CordonPos][0].disabled = true
                }
                items[this.constants.CHART_TYPE_POSITION][0].el.disabled = false;
                break;
            case BICst.WIDGET.SCATTER:
            case BICst.WIDGET.BUBBLE:
                var text = BI.i18nText("BI-Horizontal");
                switch (regionType) {
                    case BICst.REGION.TARGET1:
                        text = BI.i18nText("BI-Horizontal");
                        break;
                    case BICst.REGION.TARGET2:
                        text = BI.i18nText("BI-Vertical");
                        break;
                    case BICst.REGION.TARGET3:
                        BI.removeAt(items, this.constants.CordonPos);
                        BI.removeAt(items, this.constants.CHART_TYPE_POSITION);
                        return addDependency();
                }
                items[this.constants.CordonPos][0].cls = "";
                items[this.constants.CordonPos][0] = {
                    el: items[this.constants.CordonPos][0],
                    children: [{
                        text: BI.i18nText("BI-Cordon") + "(" + text + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    }]
                };
                if(bigDataMode) {
                    items[this.constants.CordonPos][0].disabled = true;
                }
                BI.removeAt(items, this.constants.CHART_TYPE_POSITION);
                break;
            case BICst.WIDGET.GIS_MAP:
            case BICst.WIDGET.DONUT:
            case BICst.WIDGET.PIE:
            case BICst.WIDGET.DASHBOARD:
            case BICst.WIDGET.RADAR:
            case BICst.WIDGET.ACCUMULATE_RADAR:
                BI.removeAt(items, this.constants.CHART_TYPE_POSITION);
                BI.removeAt(items, 1);
                break;
            default:
                BI.removeAt(items, this.constants.CHART_TYPE_POSITION);
                break;
        }

        return addDependency();

        function addDependency() {
            BI.find(items, function(idx, item){
                dependItem = BI.find(item, function(id, it){
                    var itE = BI.stripEL(it);
                    return itE.value === BICst.TARGET_COMBO.DEPEND_TYPE;
                });
                return BI.isNotNull(dependItem);
            });

            dependItem.el.text = BI.i18nText("BI-Count_Depend") + "(" + selectedValue +")";
            dependItem.children = children;

            return items;
        }
    },

    _assertChartType: function (val) {
        val || (val = {});
        if(BI.isNull(val.type)){
            val.type = BICst.WIDGET.AXIS;
        }
        return val;
    },

    _createValue: function () {
        var o = this.options;
        var chartType = BI.Utils.getDimensionStyleOfChartByID(o.dId);
        chartType = this._assertChartType(chartType);

        var result = {};

        result.chartType = {
            value: BICst.TARGET_COMBO.CHART_TYPE,
            childValue: chartType.type
        };
        result.group = {
            value: BICst.TARGET_COMBO.DEPEND_TYPE,
            childValue: BI.Utils.getFieldIDByDimensionID(o.dId)
        };
        return [result.chartType, result.group];
    }
});
$.shortcut("bi.count_target_combo", BI.CountTargetCombo);