/**
 * @class BI.DimensionNumberCombo
 * @extend BI.AbstractDimensionCombo
 * @abstract
 */
BI.DimensionNumberCombo = BI.inherit(BI.AbstractDimensionCombo, {

    constants: {
        customSortPos : 2,
        CordonPos: 2
    },

    config:{
        ASCEND : BICst.DIMENSION_NUMBER_COMBO.ASCEND,
        DESCEND: BICst.DIMENSION_NUMBER_COMBO.DESCEND,
        SORT_BY_CUSTOM : BICst.DIMENSION_NUMBER_COMBO.SORT_BY_CUSTOM,
        GROUP_BY_VALUE : BICst.DIMENSION_NUMBER_COMBO.GROUP_BY_VALUE,
        GROUP_SETTING : BICst.DIMENSION_NUMBER_COMBO.GROUP_SETTING,
        POSITION_BY_ADDRESS: BICst.DIMENSION_NUMBER_COMBO.ADDRESS,
        POSITION_BY_LNG_LAT: BICst.DIMENSION_NUMBER_COMBO.LNG_LAT,
        POSITION_BY_LNG: BICst.DIMENSION_NUMBER_COMBO.LNG,
        POSITION_BY_LAT: BICst.DIMENSION_NUMBER_COMBO.LAT
    },

    _defaultConfig: function(){
        return BI.extend(BI.DimensionNumberCombo.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-dimension-number-combo"
        })
    },

    _init: function(){
        BI.DimensionNumberCombo.superclass._init.apply(this, arguments);
    },

    _assertGroup: function(val){
        val || (val = {});
        if(BI.isNull(val.type)){
            val.type = BICst.GROUP.CUSTOM_NUMBER_GROUP;
        }
        return val;
    },

    _assertSort: function(val){
        val || (val = {});
        if(BI.isNull(val.type)){
            val.type = BICst.SORT.CUSTOM;
        }
        return val;
    },

    _assertAddress: function(val){
        val || (val = {});
        if(BI.isNull(val.type)){
            val.type = BICst.GIS_POSITION_TYPE.LNG_FIRST
        }
        return val;
    },

    _rebuildItems :function(){
        var chartTypes = [
            BICst.WIDGET.ACCUMULATE_AREA,
            BICst.WIDGET.ACCUMULATE_AXIS,
            BICst.WIDGET.ACCUMULATE_BAR,
            BICst.WIDGET.PERCENT_ACCUMULATE_AREA,
            BICst.WIDGET.PERCENT_ACCUMULATE_AXIS
        ];
        var items = BI.DimensionNumberCombo.superclass._rebuildItems.apply(this, arguments), o = this.options;
        if(BI.Utils.getWidgetTypeByID(BI.Utils.getWidgetIDByDimensionID(o.dId)) === BICst.WIDGET.GIS_MAP){
        }else{
            var group = this._assertGroup(BI.Utils.getDimensionGroupByID(o.dId));
            var customSort = items[0][this.constants.customSortPos];
            group.type === BICst.GROUP.ID_GROUP ? customSort.disabled = true : customSort.disabled = false;
        }
        var rType = BI.Utils.getRegionTypeByDimensionID(o.dId);
        var wType = BI.Utils.getWidgetTypeByID(BI.Utils.getWidgetIDByDimensionID(o.dId));
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
            case BICst.WIDGET.COMBINE_CHART:
            case BICst.WIDGET.MULTI_AXIS_COMBINE_CHART:
                if(BI.Utils.isDimensionRegion2ByRegionType(BI.Utils.getRegionTypeByDimensionID(o.dId))){
                    BI.removeAt(items, this.constants.CordonPos);
                }
                break;
            case BICst.WIDGET.BAR:
            case BICst.WIDGET.ACCUMULATE_BAR:
            case BICst.WIDGET.COMPARE_BAR:
                items[this.constants.CordonPos][0].text = BI.i18nText("BI-Basic_Cordon") + "(" + BI.i18nText("BI-Horizontal_Orientation") +")";
                if(BI.Utils.isDimensionRegion2ByRegionType(BI.Utils.getRegionTypeByDimensionID(o.dId))){
                    BI.removeAt(items, this.constants.CordonPos);
                }
                break;
            default:
                BI.removeAt(items, this.constants.CordonPos);
                break;

        }
        if(BI.Utils.isDimensionRegion2ByRegionType(rType) && BI.contains(chartTypes, wType)) {
            items.splice(2, 0, [{
                text: BI.i18nText("BI-Series_Accumulation_Setting"),
                cls: "",
                value: BICst.DIMENSION_NUMBER_COMBO.SERIES_ACCUMULATION_ATTRIBUTE
            }]);
        }
        if(BI.Utils.isDimensionRegion2ByRegionType(rType) && wType === BICst.WIDGET.COMBINE_CHART) {
            items.splice(2, 0, [{
                el: {
                    text: BI.i18nText("BI-Series_Accumulation_Setting"),
                    cls: "",
                    value: BICst.DIMENSION_NUMBER_COMBO.SERIES_ACCUMULATION_ATTRIBUTE
                },
                children:[{
                    text: BI.i18nText("BI-No_Accumulation"),
                    value: BICst.DIMENSION_NUMBER_COMBO.NO_SERIES,
                    cls: "dot-e-font"
                },{
                    text: BI.i18nText("BI-Series_Accumulation"),
                    value: BICst.DIMENSION_NUMBER_COMBO.SERIES_ACCUMULATION,
                    cls: "dot-e-font"
                }]
            }]);
        }
        return items;
    },

    defaultItems: function () {
        var showFieldDisabled = this.checkShowFieldDisabled();
        return [
            [{
                el: {
                    text: BI.i18nText("BI-Basic_Ascend"),
                    value: BICst.DIMENSION_NUMBER_COMBO.ASCEND,
                    iconCls1: "dot-e-font"
                },
                children: []
            }, {
                el: {
                    text: BI.i18nText("BI-Basic_Descend"),
                    value: BICst.DIMENSION_NUMBER_COMBO.DESCEND,
                    iconCls1: "dot-e-font"
                },
                children: []
            }, {
                text: BI.i18nText("BI-Custom_Sort_Dot"),
                value: BICst.DIMENSION_NUMBER_COMBO.SORT_BY_CUSTOM,
                cls: "dot-e-font",
                warningTitle: BI.i18nText("BI-Same_Value_Group")
            }],
            [{
                text: BI.i18nText("BI-Same_Value_A_Group"),
                value: BICst.DIMENSION_NUMBER_COMBO.GROUP_BY_VALUE,
                cls: "dot-e-font"
            },{
                text: BI.i18nText("BI-Grouping_Setting"),
                value: BICst.DIMENSION_NUMBER_COMBO.GROUP_SETTING,
                cls: "dot-e-font"
            }],
            [{
                text: BI.i18nText("BI-Basic_Cordon") + "(" + BI.i18nText("BI-Vertical") +")",
                value: BICst.DIMENSION_NUMBER_COMBO.CORDON,
                cls: ""
            }],
            [{
                text: BI.i18nText("BI-Show_Qualified_Result"),
                title: BI.i18nText("BI-Dimension_Filter_Tip"),
                value: BICst.DIMENSION_NUMBER_COMBO.FILTER,
                cls: "filter-h-font"
            }],
            [{
                text: BI.i18nText("BI-Math_Relationships"),
                value: BICst.DIMENSION_NUMBER_COMBO.DT_RELATION,
                cls: ""
            }],
            [{
                text: BI.i18nText("BI-Show_Field"),
                value: BICst.DIMENSION_NUMBER_COMBO.SHOW_FIELD,
                cls: BI.Utils.isDimensionUsable(this.options.dId) ? "widget-combo-show-title-font" : "",
                disabled: showFieldDisabled,
                tipType: "success",
                title: showFieldDisabled ? BI.i18nText("BI-For_Chart_Multi_Targets_Then_Forbid_Select_Dimension") : BI.i18nText("BI-Show_Field")
            }],
            [{
                text: BI.i18nText("BI-Basic_Copy"),
                value: BICst.DIMENSION_NUMBER_COMBO.COPY,
                cls: "copy-h-font"
            }],
            [{
                text: BI.i18nText("BI-Basic_Remove"),
                value: BICst.DIMENSION_NUMBER_COMBO.DELETE,
                cls: "delete-h-font"
            }],
            [{
                text: BI.i18nText("BI-Dimension_From"),
                value: BICst.DIMENSION_NUMBER_COMBO.INFO,
                tipType: "success",
                cls: "dimension-from-font",
                disabled: true
            }]
        ]
    },

    typeConfig: function(){
        return this.config;
    }
});
$.shortcut("bi.dimension_number_combo", BI.DimensionNumberCombo);