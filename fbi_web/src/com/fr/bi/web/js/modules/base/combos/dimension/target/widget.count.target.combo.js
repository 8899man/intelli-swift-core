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

    defaultItem: function () {
        var o = this.options;
        var fieldName = BI.Utils.getFieldNameByID(this.field_id);
        var tableName = BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(this.field_id));
        var fromText = BI.i18nText("BI-This_Target_From") + ": " + tableName + "." + fieldName;
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
                    value: BICst.WIDGET.COLUMN,
                    cls: "dot-e-font"
                }, {
                    text: BI.i18nText("BI-Stacked_Chart"),
                    value: BICst.WIDGET.ACCUMULATE_COLUMN,
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
                warningTitle: BI.i18nText("BI-Unmodified_in_Mini_Mode"),
                cls: "style-set-h-font"
            }],
            [{
                text: BI.i18nText("BI-Filter_Number_Summary"),
                title: BI.i18nText("BI-Target_Filter_Tip"),
                value: BICst.TARGET_COMBO.FILTER,
                cls: "filter-h-font"
            }],
            [{
                text: BI.i18nText("BI-Show_Field"),
                value: BICst.TARGET_COMBO.SHOW_FIELD,
                cls: BI.Utils.isDimensionUsable(this.options.dId) ? "widget-combo-show-title-font" : ""
            }],
            [{
                text: BI.i18nText("BI-Rename"),
                value: BICst.TARGET_COMBO.RENAME
            }],
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

    _defaultConfig: function () {
        return BI.extend(BI.CountTargetCombo.superclass._defaultConfig.apply(this, arguments), {})
    },

    _init: function () {
        BI.CountTargetCombo.superclass._init.apply(this, arguments);
        this.field_id = BI.Utils.getFieldIDByDimensionID(this.options.dId);
    },

    _rebuildItems: function () {
        var o = this.options;
        var tableId = BI.Utils.getTableIDByDimensionID(o.dId);
        var fieldIds = BI.filter(BI.Utils.getSortedFieldIdsOfOneTableByTableId(tableId), function (idx, fId) {
            return BI.Utils.getFieldTypeByID(fId) === BICst.COLUMN.STRING || BI.Utils.getFieldTypeByID(fId) === BICst.COLUMN.NUMBER;
        });
        var children = [];
        var wId = BI.Utils.getWidgetIDByDimensionID(o.dId);
        var dataLable = BI.Utils.getWSChartShowDataLabelByID(wId);
        var minimalist = BI.Utils.getWSMinimalistByID(wId);
        var bigDataMode = BI.Utils.getWSChartBigDataModeByID(wId);
        children.push({
            text: BI.i18nText("BI-Total_Row_Count"),
            value: BI.Utils.getCountFieldIDsOfTableID(tableId)[0],
            cls: "dot-e-font"
        });
        BI.each(fieldIds, function (idx, fieldId) {
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
            case BICst.WIDGET.COLUMN:
            case BICst.WIDGET.ACCUMULATE_COLUMN:
            case BICst.WIDGET.PERCENT_ACCUMULATE_COLUMN:
            case BICst.WIDGET.COMPARE_COLUMN:
            case BICst.WIDGET.FALL_COLUMN:
                items[this.constants.CordonPos][0].cls = "";
                items[this.constants.CordonPos][0] = {
                    el: items[this.constants.CordonPos][0],
                    children: [{
                        text: BI.i18nText("BI-Style_And_NumberLevel"),
                        value: BICst.TARGET_COMBO.STYLE_AND_NUMBER_LEVEL
                    }, {
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Horizontal") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    }, {
                        text: BI.i18nText("BI-Data_Label"),
                        value: BICst.TARGET_COMBO.DATA_LABEL,
                        warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                    }, {
                        text: BI.i18nText("BI-Data_Image"),
                        value: BICst.TARGET_COMBO.DATA_IMAGE,
                        warningTitle: BI.i18nText("BI-Data_Image_Donnot_Show")
                    }]
                };
                if (minimalist) {
                    items[this.constants.CordonPos][0].disabled = true
                }
                if (!dataLable) {
                    items[this.constants.CordonPos][0].children[1].disabled = true
                }
                BI.removeAt(items, this.constants.CHART_TYPE_POSITION);
                break;
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
                        text: BI.i18nText("BI-Style_And_NumberLevel"),
                        value: BICst.TARGET_COMBO.STYLE_AND_NUMBER_LEVEL
                    }, {
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Horizontal") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    }, {
                        text: BI.i18nText("BI-Data_Label"),
                        value: BICst.TARGET_COMBO.DATA_LABEL,
                        warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                    }]
                };
                if (minimalist) {
                    items[this.constants.CordonPos][0].disabled = true
                }
                if (!dataLable) {
                    items[this.constants.CordonPos][0].children[1].disabled = true
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
                        text: BI.i18nText("BI-Style_And_NumberLevel"),
                        value: BICst.TARGET_COMBO.STYLE_AND_NUMBER_LEVEL
                    }, {
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Vertical") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    }, {
                        text: BI.i18nText("BI-Data_Label"),
                        value: BICst.TARGET_COMBO.DATA_LABEL,
                        warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                    }, {
                        text: BI.i18nText("BI-Data_Image"),
                        value: BICst.TARGET_COMBO.DATA_IMAGE,
                        warningTitle: BI.i18nText("BI-Data_Image_Donnot_Show")
                    }]
                };
                if (minimalist) {
                    items[this.constants.CordonPos][0].disabled = true
                }
                if (!dataLable) {
                    items[this.constants.CordonPos][0].children[1].disabled = true
                }
                BI.removeAt(items, this.constants.CHART_TYPE_POSITION);
                break;
            case BICst.WIDGET.COMBINE_CHART:
                items[this.constants.CordonPos][0].cls = "";
                items[this.constants.CordonPos][0] = {
                    el: items[this.constants.CordonPos][0],
                    children: [{
                        text: BI.i18nText("BI-Style_And_NumberLevel"),
                        value: BICst.TARGET_COMBO.STYLE_AND_NUMBER_LEVEL
                    }, {
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Horizontal") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    }, {
                        text: BI.i18nText("BI-Data_Label"),
                        value: BICst.TARGET_COMBO.DATA_LABEL,
                        warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                    }, {
                        text: BI.i18nText("BI-Data_Image"),
                        value: BICst.TARGET_COMBO.DATA_IMAGE,
                        warningTitle: BI.i18nText("BI-Data_Image_Donnot_Show")
                    }]
                };
                if (minimalist) {
                    items[this.constants.CordonPos][0].disabled = true
                }
                if (!dataLable) {
                    items[this.constants.CordonPos][0].children[1].disabled = true
                }
                items[this.constants.CHART_TYPE_POSITION][0].el.disabled = false;
                break;
            case BICst.WIDGET.MULTI_AXIS_COMBINE_CHART:
                items[this.constants.CordonPos][0] = {
                    el: {
                        text: BI.i18nText("BI-Style_Setting"),
                        value: BICst.TARGET_COMBO.STYLE_AND_NUMBER_LEVEL,
                        cls: ""
                    },
                    children: [{
                        text: BI.i18nText("BI-Style_And_NumberLevel"),
                        value: BICst.TARGET_COMBO.STYLE_SETTING
                    }, {
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Horizontal") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    }]
                };
                items[0][this.constants.CHART_TYPE_POSITION] = {
                    el: {
                        text: BI.i18nText("BI-Chart_Type"),
                        value: BICst.TARGET_COMBO.CHART_TYPE,
                        iconCls1: "",
                        disabled: false
                    },
                    children: [{
                        text: BI.i18nText("BI-Column_Chart"),
                        value: BICst.WIDGET.COLUMN,
                        cls: "dot-e-font"
                    }, {
                        text: BI.i18nText("BI-Line_Chart"),
                        value: BICst.WIDGET.LINE,
                        cls: "dot-e-font"
                    }, {
                        text: BI.i18nText("BI-Area_Chart"),
                        value: BICst.WIDGET.AREA,
                        cls: "dot-e-font"
                    }]
                };
                if(minimalist){
                    items[this.constants.CordonPos][0].disabled = true
                }
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
                        items[this.constants.CordonPos][0].cls = "";
                        items[this.constants.CordonPos][0] = {
                            el: items[this.constants.CordonPos][0],
                            children: [{
                                text: BI.i18nText("BI-Style_And_NumberLevel"),
                                value: BICst.TARGET_COMBO.STYLE_AND_NUMBER_LEVEL
                            }, {
                                text: BI.i18nText("BI-Data_Label"),
                                value: BICst.TARGET_COMBO.DATA_LABEL_OTHER,
                                warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                            }]
                        };
                        if (!dataLable) {
                            items[this.constants.CordonPos][0].children.disabled = true
                        }
                        BI.removeAt(items, this.constants.CHART_TYPE_POSITION);
                        return addDependency();
                }
                items[this.constants.CordonPos][0].cls = "";
                items[this.constants.CordonPos][0] = {
                    el: items[this.constants.CordonPos][0],
                    children: [{
                        text: BI.i18nText("BI-Style_And_NumberLevel"),
                        value: BICst.TARGET_COMBO.STYLE_AND_NUMBER_LEVEL
                    }, {
                        text: BI.i18nText("BI-Cordon") + "(" + text + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    }, {
                        text: BI.i18nText("BI-Data_Label"),
                        value: BICst.TARGET_COMBO.DATA_LABEL_OTHER,
                        warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                    }]
                };
                if (bigDataMode) {
                    items[this.constants.CordonPos][0] = {
                        text: BI.i18nText("BI-Style_Setting"),
                        value: BICst.CALCULATE_TARGET_COMBO.FORM_SETTING,
                        warningTitle: BI.i18nText("BI-Unmodified_in_BigData_Mode"),
                        cls: "style-set-h-font"
                    };
                    items[this.constants.CordonPos][0].disabled = true;
                }
                if (!dataLable) {
                    items[this.constants.CordonPos][0].children[1].disabled = true
                }
                BI.removeAt(items, this.constants.CHART_TYPE_POSITION);
                break;
            case BICst.WIDGET.DONUT:
            case BICst.WIDGET.PIE:
            case BICst.WIDGET.MULTI_PIE:
            case BICst.WIDGET.TREE_MAP:
            case BICst.WIDGET.GAUGE:
            case BICst.WIDGET.RADAR:
            case BICst.WIDGET.FORCE_BUBBLE:
            case BICst.WIDGET.ACCUMULATE_RADAR:
                BI.removeAt(items, this.constants.CHART_TYPE_POSITION);
                items[1][0] = {
                        text: BI.i18nText("BI-Style_And_NumberLevel"),
                        value: BICst.TARGET_COMBO.STYLE_AND_NUMBER_LEVEL
                };
                break;
            default:
                BI.removeAt(items, this.constants.CHART_TYPE_POSITION);
                break;
        }

        return addDependency();

        function addDependency() {
            BI.find(items, function (idx, item) {
                dependItem = BI.find(item, function (id, it) {
                    var itE = BI.stripEL(it);
                    return itE.value === BICst.TARGET_COMBO.DEPEND_TYPE;
                });
                return BI.isNotNull(dependItem);
            });

            dependItem.el.text = BI.i18nText("BI-Count_Depend") + "(" + selectedValue + ")";
            dependItem.children = children;

            return items;
        }
    },

    _assertChartType: function (val) {
        val || (val = {});
        if (BI.isNull(val.type)) {
            val.type = BICst.WIDGET.COLUMN;
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
