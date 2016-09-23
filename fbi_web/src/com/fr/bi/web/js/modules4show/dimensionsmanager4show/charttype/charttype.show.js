/**
 * @class BI.ChartTypeShow
 * @extend BI.Widget
 * 选择图表类型组
 */
BI.ChartTypeShow = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.ChartTypeShow.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-chart-type"
        })
    },

    _init: function () {
        BI.ChartTypeShow.superclass._init.apply(this, arguments);
        var self = this, o = this.options;

        this.buttonTree = BI.createWidget({
            type: "bi.button_tree",
            element: this.element,
            items: this._formatItems(BI.deepClone(BICst.DASHBOARD_WIDGETS[0])),
            layouts: [{
                type: "bi.horizontal",
                scrollx: false,
                scrollable: false,
                vgap: 3,
                hgap: 3
            }]
        });
        this.buttonTree.on(BI.ButtonTree.EVENT_CHANGE, function () {
            self.fireEvent(BI.ChartType.EVENT_CHANGE, arguments);
        })
    },

    //只能显示表格和其自身  交互说的神逻辑 0.0
    _formatItems: function (items) {
        var self = this, o = this.options;
        var result = [];
        var wId = o.wId;
        //这个type需要从original_show_widgets中取
        var originalShowWidgets = Data.SharingPool.get("original_show_widgets");
        var widget = originalShowWidgets[wId];
        var wType = widget.type;

        BI.each(items, function (i, item) {
            if (BI.isNotEmptyArray(item.children)) {
                var foundType = false, matchItem = {};
                BI.each(item.children, function (i, child) {
                    child.iconClass = child.cls;
                    child.iconWidth = 20;
                    child.iconHeight = 20;
                    if (child.value === wType) {
                        foundType = true;
                        matchItem = child;
                    }
                });
                if (item.value === BICst.WIDGET.TABLE) {
                    result.push(BI.extend({
                        type: "bi.icon_combo",
                        width: 40,
                        iconClass: item.cls,
                        items: item.children,
                        iconWidth: 24,
                        iconHeight: 24
                    }, item, {
                        cls: "chart-type-combo"
                    }));
                } else if (foundType) {
                    matchItem.iconWidth = 25;
                    matchItem.iconHeight = 25;
                    result.push(BI.extend({
                        type: "bi.icon_button",
                        width: 40
                    }, matchItem, {
                        cls: matchItem.cls + " chart-type-icon"
                    }));
                }
            } else {
                if (item.value === wType) {
                    var subType = BI.Utils.getWidgetSubTypeByID(wId);
                    //地图
                    if (BI.isNotNull(subType)) {
                        item.value = subType;
                        item.cls = MapConst.INNER_MAP_INFO.MAP_LAYER[subType] === 0 ? "drag-map-china-icon" : "drag-map-svg-icon";
                        item.title = self._getMapNameBySubType(subType);
                    }
                    result.push(BI.extend({
                        type: "bi.icon_button",
                        width: 40,
                        iconWidth: 24,
                        iconHeight: 24
                    }, item, {
                        cls: item.cls + " chart-type-icon"
                    }));
                }
            }
        });
        return result;
    },

    _getMapNameBySubType: function(subType) {
        if (BI.isNotNull(MapConst.CUSTOM_MAP_INFO.MAP_TYPE_NAME[subType])) {
            return MapConst.CUSTOM_MAP_INFO.MAP_TYPE_NAME[subType];
        }
        if (BI.isNotNull(MapConst.INNER_MAP_INFO.MAP_TYPE_NAME[subType])) {
            return MapConst.INNER_MAP_INFO.MAP_TYPE_NAME[subType];
        }
    },

    getValue: function () {
        return this.buttonTree.getValue()[0];
    },

    setValue: function (v) {
        this.buttonTree.setValue(v);
    }
});
BI.ChartTypeShow.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.chart_type_show", BI.ChartTypeShow);
