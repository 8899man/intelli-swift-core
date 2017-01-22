BIDezi.PaneModel = BI.inherit(BI.Model, {
    _defaultConfig: function () {
        return BI.extend(BIDezi.PaneModel.superclass._defaultConfig.apply(this), {
            layoutType: BICst.DASHBOARD_LAYOUT_GRID,
            layoutRatio: {},
            widgets: {},
            globalStyle: {},
            version: BICst.VERSION
        });
    },

    _static: function () {
        var self = this;
        return {
            childType: function (type) {
                switch (type) {
                    case BICst.WIDGET.TABLE:
                    case BICst.WIDGET.CROSS_TABLE:
                    case BICst.WIDGET.COMPLEX_TABLE:
                    case BICst.WIDGET.AXIS:
                    case BICst.WIDGET.ACCUMULATE_AXIS:
                    case BICst.WIDGET.PERCENT_ACCUMULATE_AXIS:
                    case BICst.WIDGET.COMPARE_AXIS:
                    case BICst.WIDGET.FALL_AXIS:
                    case BICst.WIDGET.BAR:
                    case BICst.WIDGET.ACCUMULATE_BAR:
                    case BICst.WIDGET.COMPARE_BAR:
                    case BICst.WIDGET.LINE:
                    case BICst.WIDGET.AREA:
                    case BICst.WIDGET.ACCUMULATE_AREA:
                    case BICst.WIDGET.PERCENT_ACCUMULATE_AREA:
                    case BICst.WIDGET.COMPARE_AREA:
                    case BICst.WIDGET.RANGE_AREA:
                    case BICst.WIDGET.COMBINE_CHART:
                    case BICst.WIDGET.MULTI_AXIS_COMBINE_CHART:
                    case BICst.WIDGET.PIE:
                    case BICst.WIDGET.DONUT:
                    case BICst.WIDGET.MAP:
                    case BICst.WIDGET.GIS_MAP:
                    case BICst.WIDGET.DASHBOARD:
                    case BICst.WIDGET.BUBBLE:
                    case BICst.WIDGET.FORCE_BUBBLE:
                    case BICst.WIDGET.SCATTER:
                    case BICst.WIDGET.RADAR:
                    case BICst.WIDGET.ACCUMULATE_RADAR:
                    case BICst.WIDGET.FUNNEL:
                    case BICst.WIDGET.PARETO:
                    case BICst.WIDGET.HEAT_MAP:
                        return BICst.WIDGET.TABLE;
                }
                return type;
            },
            operatorIndex: function () {
                return self.operatorIndex;
            },
            undoRedoSet: function () {
                return self.isUndoRedoSet;
            }
        }
    },

    _init: function () {
        BIDezi.PaneModel.superclass._init.apply(this, arguments);
        var self = this;
        this.operatorIndex = 0;
        this.saveDebounce = BI.debounce(function (record) {
            var records = Data.SharingPool.cat("records") || new BI.Queue(10);
            records.splice(self.operatorIndex + 1);
            records.push(record);
            Data.SharingPool.put("records", records);
            self.operatorIndex = records.size() - 1;
        }, 100);

        this.isIniting = true;
    },

    _generateWidgetName: function (widgetName) {
        return BI.Func.createDistinctName(this.cat("widgets"), widgetName);
    },

    local: function () {
        var self = this;
        if (this.has("dashboard")) {
            var dashboard = this.get("dashboard");
            var widgets = this.get("widgets");
            var newWidgets = {};
            var regions = dashboard.regions;
            delete dashboard.regions;
            BI.each(regions, function (i, region) {
                if (BI.isNotNull(widgets[region.id])) {
                    widgets[region.id].bounds = {
                        left: region.left,
                        top: region.top,
                        width: region.width,
                        height: region.height
                    };
                    newWidgets[region.id] = widgets[region.id];
                }
            });
            this.set(BI.extend({"widgets": newWidgets}, dashboard));
            return true;
        }
        if (this.has("addWidget")) {
            var widget = this.get("addWidget");
            var widgets = this.get("widgets");
            var wId = widget.id;
            var info = widget.info;
            if (!widgets[wId]) {
                widgets[wId] = info;
                widgets[wId].name = self._generateWidgetName(widgets[wId].name);
                widgets[wId].init_time = new Date().getTime();
                //添加查询按钮的时候在此保存一下当前的查询条件
                if (info.type === BICst.WIDGET.QUERY) {
                    Data.SharingPool.put("control_filters", BI.Utils.getControlCalculations());
                }
            }
            this.set({"widgets": widgets});
            return true;
        }
        if (this.has("undo")) {
            this.get("undo");
            this._undoRedoOperator(true);
            return true;
        }
        if (this.has("redo")) {
            this.get("redo");
            this._undoRedoOperator(false);
            return true;
        }
        if(this.has("undoRedoSet")){
            this.isUndoRedoSet = this.get("undoRedoSet");
            return true;
        }
        return false;
    },

    _undoRedoOperator: function (isUndo) {
        isUndo === true ? this.operatorIndex-- : this.operatorIndex++;
        var ob = Data.SharingPool.cat("records").getElementByIndex(this.operatorIndex);
        this.isUndoRedoSet = true;
        this._shareData(ob);
        this.set(ob);
    },

    splice: function (old, key1, key2) {
        if (key1 === "widgets") {
            var widgets = this.get("widgets");
            var wids = BI.keys(widgets);
            BI.each(widgets, function (i, widget) {
                BI.remove(widget.linkages, function (j, linkage) {
                    return !wids.contains(linkage.to);
                });
            });
            this.set("widgets", widgets);
        }
        this.refresh();
        if (key1 === "widgets") {
            BI.Broadcasts.send(BICst.BROADCAST.WIDGETS_PREFIX + key2);
            //全局组件增删事件
            BI.Broadcasts.send(BICst.BROADCAST.WIDGETS_PREFIX);
        }
    },

    similar: function (ob, key1, key2) {
        if (key1 === "widgets") {
            return BI.Utils.getWidgetCopyByID(key2);
        }
    },

    duplicate: function (copy, key1, key2) {
        this.refresh();
        if (key1 === "widgets") {
            BI.Broadcasts.send(BICst.BROADCAST.WIDGETS_PREFIX + key2);
            //全局组件增删事件
            BI.Broadcasts.send(BICst.BROADCAST.WIDGETS_PREFIX);
        }
    },

    change: function (changed, pre) {
        if (this.isUndoRedoSet === true) {
            return;
        }
        this.refresh();
        if (BI.has(changed, "widgets")) {
            if (BI.size(changed.widgets) !== BI.size(pre.widgets)) {
                //全局组件增删事件
                BI.Broadcasts.send(BICst.BROADCAST.WIDGETS_PREFIX);
            }
        }
        if (BI.has(changed, "globalStyle")) {
            BI.Broadcasts.send(BICst.BROADCAST.GLOBAL_STYLE_PREFIX, changed.globalStyle);
        }
    },

    _shareData: function (data) {
        var dims = {};
        BI.each(data.widgets, function (id, widget) {
            BI.extend(dims, widget.dimensions);
        });
        Data.SharingPool.put("dimensions", dims);
        Data.SharingPool.put("widgets", data.widgets);
        Data.SharingPool.put("layoutType", data.layoutType);
        Data.SharingPool.put("layoutRatio", data.layoutRatio);
        Data.SharingPool.put("globalStyle", data.globalStyle);
    },

    refresh: function () {
        var widgets = this.cat("widgets"),
            layoutType = this.cat("layoutType"),
            layoutRatio = this.cat("layoutRatio"),
            globalStyle = this.cat("globalStyle");

        var data = {
            widgets: widgets,
            layoutType: layoutType,
            layoutRatio: layoutRatio,
            globalStyle: globalStyle
        };

        this._shareData(data);
        if (this.isIniting) {
            this.isIniting = false;
            //初始放一个control_filters（如果有查询按钮）
            if (BI.Utils.isQueryControlExist()) {
                Data.SharingPool.put("control_filters", BI.Utils.getControlCalculations());
            }
        }

        //用于undo redo
        this.saveDebounce(data);
    }
});