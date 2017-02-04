/**
 * Created by Young's on 2016/4/15.
 */
BI.DetailTableHeader = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.DetailTableHeader.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-detail-table-header"
        })
    },

    _init: function () {
        BI.DetailTableHeader.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        var dId = o.dId;
        var name = o.text;
        var combo = BI.createWidget();
        if (!BI.Utils.isCalculateTargetByDimensionID(dId)) {
            combo = BI.createWidget({
                type: "bi.sort_filter_detail_combo",
                dId: dId
            });
            combo.on(BI.SortFilterDetailCombo.EVENT_CHANGE, function (v) {
                o.sortFilterChange(v);
            });
        }
        var styleSettings = BI.Utils.getDimensionSettingsByID(dId);
        var st = this._getNumLevelByLevel(styleSettings.numLevel) + (styleSettings.unit || "");
        if (BI.isNotEmptyString(st)) {
            name = name + "(" + st + ")";
        }
        BI.createWidget({
            type: "bi.htape",
            element: this.element,
            items: [{
                el: {
                    type: "bi.label",
                    text: name,
                    title: name,
                    cls: "header-cell-text",
                    whiteSpace: "nowrap",
                    textAlign: "center",
                    lgap: 5,
                    height: 25
                }
            }, {
                el: {
                    type: "bi.center_adapt",
                    items: [combo],
                    width: 25,
                    height: 25
                },
                width: 25
            }]
        });

        //表格样式
        if (BI.isNotNull(o.styles) && BI.isObject(o.styles)) {
            this.element.css(o.styles);
        }
    },

    _getNumLevelByLevel: function (level) {
        var numLevel = "";
        BI.each(BICst.TARGET_STYLE_LEVEL, function (i, ob) {
            if (ob.value === level && level !== BICst.TARGET_STYLE.NUM_LEVEL.NORMAL) {
                numLevel = ob.text;
            }
        });
        return numLevel;
    }
});
$.shortcut("bi.detail_table_header", BI.DetailTableHeader);