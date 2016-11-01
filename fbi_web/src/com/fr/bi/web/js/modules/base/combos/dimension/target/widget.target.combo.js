/**
 * @class BI.TargetCombo
 * @extend BI.Widget
 * 指标下拉
 */
BI.TargetCombo = BI.inherit(BI.AbstractDimensionTargetCombo, {

    constants: {
        CHART_TYPE_POSITION: 1,
        CordonPos: 1
    },

    defaultItems: function(){
        var fieldId = BI.Utils.getFieldIDByDimensionID(this.options.dId);
        var text = BI.i18nText("BI-This_Target_From");
        var fieldName = BI.Utils.getFieldNameByID(fieldId);
        if(BI.isNotNull(fieldName)){
            var tableName = BI.Utils.getTableNameByID(BI.Utils.getTableIdByFieldID(fieldId));
            text = BI.i18nText("BI-This_Target_From") + ": " + tableName + "." + fieldName
        }
        return [
            [{
                el: {
                    text: BI.i18nText("BI-Summary_Style"),
                    value: BICst.TARGET_COMBO.SUMMERY_TYPE,
                    iconCls1: ""
                },
                children: [{
                    text: BI.i18nText("BI-Qiu_Sum"),
                    value: BICst.SUMMARY_TYPE.SUM,
                    cls: "dot-e-font"
                }, {
                    text: BI.i18nText("BI-Qiu_Avg"),
                    value: BICst.SUMMARY_TYPE.AVG,
                    cls: "dot-e-font"
                }, {
                    text: BI.i18nText("BI-Qiu_Max"),
                    value: BICst.SUMMARY_TYPE.MAX,
                    cls: "dot-e-font"
                }, {
                    text: BI.i18nText("BI-Qiu_Min"),
                    value: BICst.SUMMARY_TYPE.MIN,
                    cls: "dot-e-font"
                }]
            }, {
                el: {
                    text: BI.i18nText("BI-Chart_Type"),
                    value: BICst.TARGET_COMBO.CHART_TYPE,
                    iconCls1: ""
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
                warningTitle: BI.i18nText("BI-Unmodified_in_Current_Mode"),
                cls: "style-set-h-font"
            }],
            [{
                text: BI.i18nText("BI-Filter_Number_Summary"),
                value: BICst.TARGET_COMBO.FILTER,
                title: BI.i18nText("BI-Target_Filter_Tip"),
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
                text: text,
                tipType: "success",
                value: BICst.TARGET_COMBO.INFO,
                cls: "dimension-from-font",
                disabled: true
            }]
        ];
    },

    _defaultConfig: function(){
        return BI.extend(BI.TargetCombo.superclass._defaultConfig.apply(this, arguments), {

        })
    },

    _init: function(){
        BI.TargetCombo.superclass._init.apply(this, arguments);
    },

    _assertGroup: function (val) {
        val || (val = {});
        val.type || (val.type = BICst.SUMMARY_TYPE.SUM);
        return val;
    },

    _assertChartType:function(val){
        val || (val = {});
        val.type || (val.type = BICst.WIDGET.AXIS);
        return val;
    },

    _rebuildItems: function(){
        var self = this, o = this.options;
        var item = this.defaultItems();
        var wId = BI.Utils.getWidgetIDByDimensionID(o.dId);
        var regionType = BI.Utils.getRegionTypeByDimensionID(o.dId);
        var wType = BI.Utils.getWidgetTypeByID(wId);
        var view = BI.Utils.getWidgetViewByID(wId);
        var minimalist = BI.Utils.getWSMinimalistByID(wId);
        var dataLable = BI.Utils.getWSShowDataLabelByID(wId);
        var bigDataMode = BI.Utils.getWSBigDataModelByID(wId);
        var result = BI.find(view[BICst.REGION.TARGET2], function (idx, did) {
            return did === o.dId;
        });
        switch (wType) {
            case BICst.WIDGET.AXIS:
            case BICst.WIDGET.ACCUMULATE_AXIS:
            case BICst.WIDGET.PERCENT_ACCUMULATE_AXIS:
            case BICst.WIDGET.COMPARE_AXIS:
            case BICst.WIDGET.FALL_AXIS:
                item[this.constants.CordonPos][0] = {
                    el: {
                        text: BI.i18nText("BI-Style_Setting"),
                        value: BICst.TARGET_COMBO.STYLE_SETTING,
                        warningTitle: BI.i18nText("BI-Unmodified_in_Current_Mode"),
                        cls: ""
                    },
                    children: [{
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Horizontal") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    },{
                        text: BI.i18nText("BI-Data_Label"),
                        value: BICst.TARGET_COMBO.DATA_LABEL,
                        warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                    },{
                        text: BI.i18nText("BI-Data_Image"),
                        value: BICst.TARGET_COMBO.DATA_IMAGE,
                        warningTitle: BI.i18nText("BI-Data_Image_Donnot_Show")
                    }]
                };
                if(minimalist){
                    item[this.constants.CordonPos][0].disabled = true
                }
                if(!dataLable){
                    item[this.constants.CordonPos][0].children[1].disabled = true
                }
                BI.removeAt(item[0], this.constants.CHART_TYPE_POSITION);
                break;
            case BICst.WIDGET.LINE:
            case BICst.WIDGET.AREA:
            case BICst.WIDGET.ACCUMULATE_AREA:
            case BICst.WIDGET.COMPARE_AREA:
            case BICst.WIDGET.RANGE_AREA:
            case BICst.WIDGET.PERCENT_ACCUMULATE_AREA:
                item[this.constants.CordonPos][0] = {
                    el: {
                        text: BI.i18nText("BI-Style_Setting"),
                        value: BICst.TARGET_COMBO.STYLE_SETTING,
                        warningTitle: BI.i18nText("BI-Unmodified_in_Current_Mode"),
                        cls: ""
                    },
                    children: [{
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Horizontal") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    },{
                        text: BI.i18nText("BI-Data_Label"),
                        value: BICst.TARGET_COMBO.DATA_LABEL,
                        warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                    }]
                };
                if(minimalist){
                    item[this.constants.CordonPos][0].disabled = true
                }
                if(!dataLable){
                    item[this.constants.CordonPos][0].children[1].disabled = true
                }
                BI.removeAt(item[0], this.constants.CHART_TYPE_POSITION);
                break;
            case BICst.WIDGET.BAR:
            case BICst.WIDGET.ACCUMULATE_BAR:
            case BICst.WIDGET.COMPARE_BAR:
                item[this.constants.CordonPos][0] = {
                    el: {
                        text: BI.i18nText("BI-Style_Setting"),
                        value: BICst.TARGET_COMBO.STYLE_SETTING,
                        warningTitle: BI.i18nText("BI-Unmodified_in_Current_Mode"),
                        cls: ""
                    },
                    children: [{
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Vertical") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    },{
                        text: BI.i18nText("BI-Data_Label"),
                        value: BICst.TARGET_COMBO.DATA_LABEL,
                        warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                    },{
                        text: BI.i18nText("BI-Data_Image"),
                        value: BICst.TARGET_COMBO.DATA_IMAGE,
                        warningTitle: BI.i18nText("BI-Data_Image_Donnot_Show")
                    }]
                };
                if(minimalist){
                    item[this.constants.CordonPos][0].disabled = true
                }
                if(!dataLable){
                    item[this.constants.CordonPos][0].children[1].disabled = true
                }
                BI.removeAt(item[0], this.constants.CHART_TYPE_POSITION);
                break;
            case BICst.WIDGET.COMBINE_CHART:
                item[this.constants.CordonPos][0] = {
                    el: {
                        text: BI.i18nText("BI-Style_Setting"),
                        value: BICst.TARGET_COMBO.STYLE_SETTING,
                        warningTitle: BI.i18nText("BI-Unmodified_in_Current_Mode"),
                        cls: ""
                    },
                    children: [{
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Horizontal") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    },{
                        text: BI.i18nText("BI-Data_Label"),
                        value: BICst.TARGET_COMBO.DATA_LABEL,
                        warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                    },{
                        text: BI.i18nText("BI-Data_Image"),
                        value: BICst.TARGET_COMBO.DATA_IMAGE,
                        warningTitle: BI.i18nText("BI-Data_Image_Donnot_Show")
                    }]
                };
                if(minimalist){
                    item[this.constants.CordonPos][0].disabled = true
                }
                if(!dataLable){
                    item[this.constants.CordonPos][0].children[1].disabled = true
                }
                item[0][this.constants.CHART_TYPE_POSITION].disabled = false;
                break;
            case BICst.WIDGET.MULTI_AXIS_COMBINE_CHART:
                item[this.constants.CordonPos][0] = {
                    el: {
                        text: BI.i18nText("BI-Style_Setting"),
                        value: BICst.TARGET_COMBO.STYLE_SETTING,
                        warningTitle: BI.i18nText("BI-Unmodified_in_Current_Mode"),
                        cls: ""
                    },
                    children: [{
                        text: BI.i18nText("BI-Cordon") + "(" + BI.i18nText("BI-Horizontal") + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    },{
                        text: BI.i18nText("BI-Data_Label"),
                        value: BICst.TARGET_COMBO.DATA_LABEL,
                        warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                    },{
                        text: BI.i18nText("BI-Data_Image"),
                        value: BICst.TARGET_COMBO.DATA_IMAGE,
                        warningTitle: BI.i18nText("BI-Data_Image_Donnot_Show")
                    }]
                };
                item[0][this.constants.CHART_TYPE_POSITION] = {
                    el: {
                        text: BI.i18nText("BI-Chart_Type"),
                        value: BICst.TARGET_COMBO.CHART_TYPE,
                        iconCls1: "",
                        disabled: false
                    },
                    children: [{
                        text: BI.i18nText("BI-Column_Chart"),
                        value: BICst.WIDGET.AXIS,
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
                    item[this.constants.CordonPos][0].disabled = true
                }
                if(!dataLable){
                    item[this.constants.CordonPos][0].children[1].disabled = true
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
                        item[this.constants.CordonPos][0] = {
                            el: {
                                text: BI.i18nText("BI-Style_Setting"),
                                warningTitle: BI.i18nText("BI-Unmodified_in_Current_Mode"),
                                value: BICst.TARGET_COMBO.STYLE_SETTING,
                                cls: ""
                            },
                            children: [{
                                text: BI.i18nText("BI-Style_And_NumberLevel"),
                                value: BICst.TARGET_COMBO.STYLE_AND_NUMBER_LEVEL
                            },{
                                text: BI.i18nText("BI-Data_Label"),
                                value: BICst.TARGET_COMBO.DATA_LABEL_OTHER,
                                warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                            }]
                        };
                        if(!dataLable){
                            item[this.constants.CordonPos][0].children.disabled = true
                        }
                        BI.removeAt(item[0], this.constants.CHART_TYPE_POSITION);
                        return item;
                }
                item[this.constants.CordonPos][0] = {
                    el: {
                        text: BI.i18nText("BI-Style_Setting"),
                        warningTitle: BI.i18nText("BI-Unmodified_in_Current_Mode"),
                        value: BICst.TARGET_COMBO.STYLE_SETTING,
                        cls: ""
                    },
                    children: [{
                        text: BI.i18nText("BI-Cordon") + "(" + text + ")",
                        value: BICst.TARGET_COMBO.CORDON
                    },{
                        text: BI.i18nText("BI-Data_Label"),
                        value: BICst.TARGET_COMBO.DATA_LABEL_OTHER,
                        warningTitle: BI.i18nText("BI-Data_Label_Donnot_Show")
                    }]
                };
                if(bigDataMode) {
                    item[this.constants.CordonPos][0].disabled = true;
                }
                if(!dataLable){
                    item[this.constants.CordonPos][0].children[1].disabled = true
                }
                BI.removeAt(item[0], this.constants.CHART_TYPE_POSITION);
                break;
            case BICst.WIDGET.MAP:
                BI.removeAt(item[0], this.constants.CHART_TYPE_POSITION);
                break;
            case BICst.WIDGET.DONUT:
            case BICst.WIDGET.PIE:
                BI.removeAt(item[0], this.constants.CHART_TYPE_POSITION);
                item[this.constants.CordonPos][0] = {
                        text: BI.i18nText("BI-Style_And_NumberLevel"),
                        value: BICst.TARGET_COMBO.STYLE_AND_NUMBER_LEVEL
                };
                break;
            case BICst.WIDGET.GIS_MAP:
            case BICst.WIDGET.DASHBOARD:
            case BICst.WIDGET.RADAR:
            case BICst.WIDGET.FORCE_BUBBLE:
            case BICst.WIDGET.ACCUMULATE_RADAR:
                BI.removeAt(item[0], this.constants.CHART_TYPE_POSITION);
                BI.removeAt(item, 1);
                break;
            default:
                BI.removeAt(item[0], this.constants.CHART_TYPE_POSITION);
                break;
        }
        return addSummaryType();

        function addSummaryType() {
            var summaryItem = {};
            BI.find(item, function(idx, ite){
                summaryItem = BI.find(ite, function(id, it){
                    var itE = BI.stripEL(it);
                    return itE.value === BICst.TARGET_COMBO.SUMMERY_TYPE;
                });
                return BI.isNotNull(summaryItem);
            });
            var selectedValue = BI.i18nText("BI-Qiu_Sum");
            switch (self._assertGroup(BI.Utils.getDimensionGroupByID(o.dId)).type) {
                case BICst.SUMMARY_TYPE.SUM:
                    selectedValue = BI.i18nText("BI-Qiu_Sum");
                    break;
                case BICst.SUMMARY_TYPE.AVG:
                    selectedValue = BI.i18nText("BI-Qiu_Avg");
                    break;
                case BICst.SUMMARY_TYPE.MAX:
                    selectedValue = BI.i18nText("BI-Qiu_Max");
                    break;
                case BICst.SUMMARY_TYPE.MIN:
                    selectedValue = BI.i18nText("BI-Qiu_Min");
                    break;
            }
            summaryItem.el.text = BI.i18nText("BI-Summary_Style") + "(" + selectedValue + ")";
            return item;
        }
    },

    _createValue: function () {
        var o = this.options;
        var group = BI.Utils.getDimensionGroupByID(o.dId);
        var chartType = BI.Utils.getDimensionStyleOfChartByID(o.dId);
        group = this._assertGroup(group);
        chartType = this._assertChartType(chartType);

        var result = {};

        result.chartType = {
            value: BICst.TARGET_COMBO.CHART_TYPE,
            childValue: chartType.type
        };
        result.group = {
            value: BICst.TARGET_COMBO.SUMMERY_TYPE,
            childValue: group.type
        };
        return [result.chartType, result.group];
    }
});
$.shortcut("bi.target_combo", BI.TargetCombo);
