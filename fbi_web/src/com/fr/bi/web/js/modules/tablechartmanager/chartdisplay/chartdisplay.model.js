BI.ChartDisplayModel = BI.inherit(FR.OB, {

    constants: {
        BUBBLE_REGION_COUNT: 4,
        SCATTER_REGION_COUNT: 3
    },

    _init: function () {
        BI.ChartDisplayModel.superclass._init.apply(this, arguments);
    },

    _getShowTarget: function () {
        var self = this, o = this.options;
        var view = BI.Utils.getWidgetViewByID(o.wId);
        this.cataDid = BI.find(view[BICst.REGION.DIMENSION1], function (idx, did) {
            return BI.Utils.isDimensionUsable(did);
        });
        this.seriesDid = BI.find(view[BICst.REGION.DIMENSION2], function (idx, did) {
            return BI.Utils.isDimensionUsable(did);
        });
        this.targetIds = [];
        BI.each(view, function (regionType, arr) {
            if (regionType >= BICst.REGION.TARGET1) {
                self.targetIds = BI.concat(self.targetIds, arr);
            }
        });
        return this.targetIds = BI.filter(this.targetIds, function (idx, tId) {
            return BI.Utils.isDimensionUsable(tId);
        });

    },

    _formatDataForMap: function (data, currentLayer) {
        var self = this, o = this.options;
        var targetIds = this._getShowTarget();
        var result = [];
        currentLayer++;
        if (BI.has(data, "c")) {
            var obj = (data.c)[0];
            var view = BI.Utils.getWidgetViewByID(o.wId);
            var columnSizeArray = BI.makeArray(BI.isNull(obj) ? 0 : BI.size(obj.s), 0);
            result = BI.map(columnSizeArray, function (idx, value) {
                var type = null;
                if (BI.has(view, BICst.REGION.TARGET2) && BI.contains(view[BICst.REGION.TARGET2], targetIds[idx])) {
                    type = BICst.WIDGET.BUBBLE;
                }
                var adjustData = [];
                BI.each(data.c, function (id, item) {
                    var res = {};
                    var y = (BI.isFinite(item.s[idx]) ? item.s[idx] : 0);
                    if (BI.isNull(assertValue(item.s[idx]))) {
                        return;
                    }
                    if (BI.has(view, BICst.REGION.TARGET2) && BI.contains(view[BICst.REGION.TARGET2], targetIds[idx])) {
                        switch (type) {
                            case BICst.WIDGET.BUBBLE:
                            case BICst.WIDGET.COLUMN:
                            case BICst.WIDGET.PIE:
                            default:
                                res = {
                                    x: item.n,
                                    xValue: item.n,
                                    y: y,
                                    yValue: y,
                                    targetIds: [targetIds[idx]],
                                    dId: self.dimIds[currentLayer - 1],
                                    drillDid: self.dimIds[currentLayer]
                                };
                        }
                    } else {
                        res = {
                            x: item.n,
                            xValue: item.n,
                            y: y,
                            yValue: y,
                            targetIds: [targetIds[idx]],
                            dId: self.dimIds[currentLayer - 1],
                            drillDid: self.dimIds[currentLayer]
                        };
                    }
                    if (BI.has(item, "c")) {
                        res.drilldown = {};
                        res.drilldown.series = self._formatDataForMap(item, currentLayer);
                        var type = MapConst.INNER_MAP_INFO.MAP_NAME[res.x];
                        res.drilldown.geo = {
                            data: BI.isNull(type) ? MapConst.CUSTOM_MAP_INFO.MAP_PATH[MapConst.CUSTOM_MAP_INFO.MAP_NAME[res.x]] : MapConst.INNER_MAP_INFO.MAP_PATH[type],
                            name: res.x
                        };
                    }
                    adjustData.push(res);
                });
                var obj = {};
                obj.data = adjustData;
                BI.isNotNull(type) && (obj.type = "bubble");
                obj.name = BI.Utils.getDimensionNameByID(targetIds[idx]);
                obj.settings = BI.Utils.getDimensionSettingsByID(targetIds[idx]);
                return obj;
            });
        }
        return result;

        function assertValue(v) {
            if (BI.isNull(v)) {
                return;
            }
            if (!BI.isFinite(v)) {
                return 0;
            }
            return v;
        }
    },

    _formatDataForGISMap: function (data) {
        var self = this, o = this.options;
        var targetIds = this._getShowTarget();
        if (BI.has(data, "c")) {
            var obj = (data.c)[0];
            var columnSizeArray = BI.makeArray(BI.isNull(obj) ? 0 : BI.size(obj.s), 0);
            return BI.map(columnSizeArray, function (idx, value) {
                var adjustData = BI.map(data.c, function (id, item) {
                    var x = item.n;
                    var obj = {
                        x: x,
                        xValue: x,
                        y: item.s[idx],
                        yValue: item.s[idx],
                        targetIds: [targetIds[idx]]
                    };
                    if(BI.has(item, "c") && BI.isNotEmptyArray(item.c) && BI.isNotEmptyString(item.c[0].n)){
                        obj.z = obj.zValue = item.c[0].n;
                    }
                    return obj;
                });
                var obj = {};
                obj.data = adjustData;
                obj.name = BI.Utils.getDimensionNameByID(targetIds[idx]);
                return obj;
            });
        }
        return [];
    },

    _formatDataForAxis: function (da) {
        var self = this, o = this.options;
        var data = this._formatDataForCommon(da);
        if (BI.isEmptyArray(data)) {
            return [];
        }
        var view = BI.Utils.getWidgetViewByID(o.wId);
        var array = [];
        BI.each(this.targetIds, function (idx, tId) {
            if (BI.has(view, BICst.REGION.TARGET1) && BI.contains(view[BICst.REGION.TARGET1], tId)) {
                array.length === 0 && array.push([]);
                if (checkSeriesExist()) {
                    array[0] = data;
                } else {
                    array[0].push(data[idx])
                }
            }
            if (BI.has(view, BICst.REGION.TARGET2) && BI.contains(view[BICst.REGION.TARGET2], tId)) {
                while (array.length < 2) {
                    array.push([]);
                }
                if (checkSeriesExist()) {
                    array[1] = data;
                } else {
                    array[1].push(data[idx])
                }
            }
            if (BI.has(view, BICst.REGION.TARGET3) && BI.contains(view[BICst.REGION.TARGET3], tId)) {
                while (array.length < 3) {
                    array.push([]);
                }
                if (checkSeriesExist()) {
                    array[2] = data;
                } else {
                    array[2].push(data[idx])
                }
            }
        });
        return array;

        function checkSeriesExist() {
            var view = BI.Utils.getWidgetViewByID(o.wId);
            var result = BI.find(view[BICst.REGION.DIMENSION2], function (idx, dId) {
                return BI.Utils.isDimensionUsable(dId);
            });
            return BI.isNotNull(result);
        }
    },

    _formatDataForBubble: function (data) {
        var self = this, o = this.options;
        var targetIds = this._getShowTarget();
        var view = BI.Utils.getWidgetViewByID(o.wId);
        var result = BI.find(view, function (region, arr) {
            return BI.isEmptyArray(arr);
        });
        if (BI.isNotNull(result) || BI.size(view) < this.constants.BUBBLE_REGION_COUNT) {
            return [];
        }
        return [BI.map(data.c, function (idx, item) {
            var obj = {};
            var name = item.n, seriesName = item.n;
            var drillcataDimId = self._getDrillDimensionId(BI.Utils.getDrillByID(o.wId)[self.cataDid]);
            var dGroup = BI.Utils.getDimensionGroupByID(self.cataDid);
            if (BI.isNotNull(drillcataDimId)) {
                dGroup = BI.Utils.getDimensionGroupByID(drillcataDimId);
            }
            if (BI.isNotNull(dGroup)) {
                name = self._getFormatDateText(dGroup.type, name);
            }
            var x = (BI.isFinite(item.s[1]) ? item.s[1] : 0);
            var y = (BI.isFinite(item.s[0]) ? item.s[0] : 0);
            obj.data = [{
                x: x,
                xValue: x,
                y: y,
                yValue: y,
                size: (BI.isFinite(item.s[2]) ? item.s[2] : 0),
                z: name,
                zValue: seriesName,
                dimensionIds: BI.isNull(drillcataDimId) ? [self.cataDid] : [drillcataDimId],
                targetIds: [targetIds[0], targetIds[1], targetIds[2]]
            }];
            obj.name = name;
            return obj;
        })];
    },

    _formatDataForScatter: function (data) {
        var self = this, o = this.options;
        var targetIds = this._getShowTarget();
        var view = BI.Utils.getWidgetViewByID(o.wId);
        var result = BI.find(view, function (region, arr) {
            return BI.isEmptyArray(arr);
        });
        if (BI.isNotNull(result) || BI.size(view) < this.constants.SCATTER_REGION_COUNT) {
            return [];
        }
        return [BI.map(data.c, function (idx, item) {
            var obj = {};
            var name = item.n, seriesName = item.n;
            var drillcataDimId = self._getDrillDimensionId(BI.Utils.getDrillByID(o.wId)[self.cataDid]);
            var dGroup = BI.Utils.getDimensionGroupByID(self.cataDid);
            if (BI.isNotNull(drillcataDimId)) {
                dGroup = BI.Utils.getDimensionGroupByID(drillcataDimId);
            }
            if (BI.isNotNull(dGroup)) {
                name = self._getFormatDateText(dGroup.type, name);
            }
            obj.name = name;
            var x = (BI.isFinite(item.s[1]) ? item.s[1] : 0);
            var y = (BI.isFinite(item.s[0]) ? item.s[0] : 0);
            obj.data = [{
                x: x,
                xValue: x,
                y: y,
                yValue: y,
                z: name,
                zValue: seriesName,
                dimensionIds: BI.isNull(drillcataDimId) ? [self.cataDid] : [drillcataDimId],
                targetIds: [targetIds[0], targetIds[1]]
            }];
            return obj;
        })];
    },

    _getFormatDateText: function(type, text){
        switch (type) {
            case BICst.GROUP.S:
                text = BICst.FULL_QUARTER_NAMES[text - 1];
                break;
            case BICst.GROUP.M:
                text = BICst.FULL_MONTH_NAMES[text - 1];
                break;
            case BICst.GROUP.W:
                text = BICst.FULL_WEEK_NAMES[text - 1];
                break;
            case BICst.GROUP.YMD:
                var date = new Date(BI.parseInt(text));
                text = date.print("%Y-%X-%d");
                break;
        }
        return text;
    },

    _getDrillDimensionId: function (drill) {
        if (BI.isEmptyArray(drill) || BI.isNull(drill)) {
            return null;
        }
        return drill[drill.length - 1].dId;
    },

    _formatDataForCommon: function (data) {
        var self = this, o = this.options;
        var targetIds = this._getShowTarget();
        var drillcataDimId = this._getDrillDimensionId(BI.Utils.getDrillByID(o.wId)[self.cataDid]);
        var drillseriDimId = this._getDrillDimensionId(BI.Utils.getDrillByID(o.wId)[self.seriesDid]);
        var cataGroup = BI.Utils.getDimensionGroupByID(self.cataDid);
        var seriesGroup = BI.Utils.getDimensionGroupByID(self.seriesDid);
        if (BI.isNotNull(drillcataDimId)) {
            cataGroup = BI.Utils.getDimensionGroupByID(drillcataDimId);
        }
        if (BI.isNotNull(drillseriDimId)) {
            seriesGroup = BI.Utils.getDimensionGroupByID(drillseriDimId);
        }
        if (BI.has(data, "t")) {
            var top = data.t, left = data.l;
            return BI.map(top.c, function (id, tObj) {
                if (BI.isNull(tObj.c)) {
                    return {
                        data: [],
                        name: BI.Utils.getDimensionNameByID(tObj.n)
                    };
                }
                var name = tObj.n, seriesName = tObj.n;
                if (BI.isNotNull(seriesGroup)) {
                    name = self._getFormatDateText(seriesGroup.type, name);
                }
                var data = [];
                if (BI.has(left, "c")) {
                    data = BI.map(left.c, function (idx, obj) {
                        var value = obj.n, x = obj.n;
                        var seriesValue = obj.s.c[id].s[0];
                        if (BI.isNotNull(cataGroup)) {
                            x = self._getFormatDateText(cataGroup.type, x);
                        }
                        var y = (BI.isNull(seriesValue) || BI.isFinite(seriesValue)) ? seriesValue : 0;
                        return {
                            "x": x,
                            "xValue": value,
                            "y": y,
                            "yValue": y,
                            "z": name,
                            "zValue": seriesName,
                            dimensionIds: [drillcataDimId || self.cataDid, drillseriDimId || self.seriesDid],
                            targetIds: [targetIds[0]]
                        };
                    });
                } else {
                    var leftSeriesValue = left.s.c[id].s[0];
                    var y = (BI.isNull(leftSeriesValue) || BI.isFinite(leftSeriesValue)) ? leftSeriesValue : 0;
                    data = [{
                        "x": "",
                        "xValue": "",
                        "y": y,
                        "yValue": y,
                        "z": name,
                        "zValue": seriesName,
                        dimensionIds: [drillseriDimId || self.seriesDid],
                        targetIds: [targetIds[0]]
                    }]
                }
                var obj = {};
                obj.data = data;
                obj.name = name;
                return obj;
            });
        }
        if (BI.has(data, "c")) {
            var obj = (data.c)[0];
            var columnSizeArray = BI.makeArray(BI.isNull(obj) ? 0 : BI.size(obj.s), 0);
            return BI.map(columnSizeArray, function (idx, value) {
                var adjustData = BI.map(data.c, function (id, item) {
                    var value = item.n, x = item.n;
                    var seriesValue = item.s[idx];
                    if (BI.isNotNull(cataGroup)) {
                        x = self._getFormatDateText(cataGroup.type, x);
                    }
                    var y = (BI.isNull(seriesValue) || BI.isFinite(seriesValue)) ? seriesValue : 0;
                    return {
                        x: x,
                        xValue: value,
                        y: y,
                        yValue: y,
                        z: BI.Utils.getDimensionNameByID(targetIds[idx]),
                        zValue: BI.Utils.getDimensionNameByID(targetIds[idx]),
                        dimensionIds: [drillcataDimId || self.cataDid],
                        targetIds: [targetIds[idx]]
                    };
                });
                var obj = {};
                obj.data = adjustData;
                obj.name = BI.Utils.getDimensionNameByID(targetIds[idx]);
                return obj;
            });
        }
        if (BI.has(data, "s")) {
            return BI.map(data.s, function (idx, value) {
                var y = (BI.isFinite(value) ? value : 0);
                y = (BI.isNull(self.cataDid) && BI.isNull(self.seriesDid)) ? y : "";
                return {
                    name: BI.Utils.getDimensionNameByID(targetIds[idx]),
                    data: [{
                        x: "",
                        xValue: "",
                        y: y,
                        yValue: y,
                        dimensionIds: [],
                        targetIds: [targetIds[idx]]
                    }]
                };
            });
        }
        return [];
    },

    getToolTip: function (type) {
        switch (type) {
            case BICst.WIDGET.SCATTER:
                if (this.targetIds.length < 2) {
                    return [];
                } else {
                    return [BI.Utils.getDimensionNameByID(this.targetIds[1]), BI.Utils.getDimensionNameByID(this.targetIds[0])];
                }
            case BICst.WIDGET.BUBBLE:
                if (this.targetIds.length < 3) {
                    return [];
                } else {
                    return [BI.Utils.getDimensionNameByID(this.targetIds[1]), BI.Utils.getDimensionNameByID(this.targetIds[0]), BI.Utils.getDimensionNameByID(this.targetIds[2])];
                }
            default:
                return "";
        }
    },

    getCordon: function () {
        var o = this.options;
        var cordon = {};
        var result = [];
        BI.each(BI.Utils.getAllDimensionIDs(o.wId), function (idx, dId) {
            if (!BI.Utils.isDimensionUsable(dId)) {
                return;
            }
            var items = BI.map(BI.Utils.getDimensionCordonByID(dId), function (id, cor) {
                return {
                    text: cor.cordon_name,
                    value: cor.cordon_value,
                    color: cor.cordon_color
                }
            });
            var regionType = BI.Utils.getRegionTypeByDimensionID(dId);
            if (BI.isNotEmptyArray(items)) {
                BI.has(cordon, regionType) === false && (cordon[regionType] = []);
                cordon[regionType] = BI.concat(cordon[regionType], items);
            }
        });
        var type = BI.Utils.getWidgetTypeByID(o.wId);
        if (type === BICst.WIDGET.SCATTER || type === BICst.WIDGET.BUBBLE) {
            result.push(BI.isNull(cordon[BICst.REGION.TARGET2]) ? [] : cordon[BICst.REGION.TARGET2]);
            result.push(BI.isNull(cordon[BICst.REGION.TARGET1]) ? [] : cordon[BICst.REGION.TARGET1]);
            return result;
        }
        if (type === BICst.WIDGET.BAR || type === BICst.WIDGET.ACCUMULATE_BAR) {
            result.push(BI.isNull(cordon[BICst.REGION.TARGET1]) ? [] : cordon[BICst.REGION.TARGET1]);
            result.push(BI.isNull(cordon[BICst.REGION.DIMENSION1]) ? [] : cordon[BICst.REGION.DIMENSION1]);
            return result;
        }
        if (type === BICst.WIDGET.COMPARE_BAR) {
            var negativeAxis = BI.isNull(cordon[BICst.REGION.TARGET1]) ? [] : cordon[BICst.REGION.TARGET1];
            var positiveAxis = BI.isNull(cordon[BICst.REGION.TARGET2]) ? [] : cordon[BICst.REGION.TARGET2]
            result.push(BI.concat(negativeAxis, positiveAxis));
            result.push(BI.isNull(cordon[BICst.REGION.DIMENSION1]) ? [] : cordon[BICst.REGION.DIMENSION1]);
            return result;
        }
        result.push(BI.isNull(cordon[BICst.REGION.DIMENSION1]) ? [] : cordon[BICst.REGION.DIMENSION1]);
        result.push(BI.isNull(cordon[BICst.REGION.TARGET1]) ? [] : cordon[BICst.REGION.TARGET1]);
        result.push(BI.isNull(cordon[BICst.REGION.TARGET2]) ? [] : cordon[BICst.REGION.TARGET2]);
        result.push(BI.isNull(cordon[BICst.REGION.TARGET3]) ? [] : cordon[BICst.REGION.TARGET3]);
        return result;
    },

    parseChartData: function (data) {
        var self = this, o = this.options;
        switch (BI.Utils.getWidgetTypeByID(o.wId)) {
            case BICst.WIDGET.ACCUMULATE_COLUMN:
            case BICst.WIDGET.ACCUMULATE_AREA:
            case BICst.WIDGET.ACCUMULATE_RADAR:
            case BICst.WIDGET.COLUMN:
            case BICst.WIDGET.LINE:
            case BICst.WIDGET.AREA:
            case BICst.WIDGET.PERCENT_ACCUMULATE_COLUMN:
            case BICst.WIDGET.PERCENT_ACCUMULATE_AREA:
            case BICst.WIDGET.COMPARE_COLUMN:
            case BICst.WIDGET.COMPARE_AREA:
            case BICst.WIDGET.FALL_COLUMN:
            case BICst.WIDGET.RANGE_AREA:
            case BICst.WIDGET.BAR:
            case BICst.WIDGET.ACCUMULATE_BAR:
            case BICst.WIDGET.COMPARE_BAR:
            case BICst.WIDGET.COMBINE_CHART:
            case BICst.WIDGET.DONUT:
            case BICst.WIDGET.RADAR:
            case BICst.WIDGET.PIE:
            case BICst.WIDGET.MULTI_AXIS_COMBINE_CHART:
            case BICst.WIDGET.GAUGE:
            case BICst.WIDGET.FORCE_BUBBLE:
                return this._formatDataForAxis(data);

            //return this._formatDataForDashBoard(data);
            case BICst.WIDGET.BUBBLE:
                return this._formatDataForBubble(data);
            case BICst.WIDGET.SCATTER:
                return this._formatDataForScatter(data);
            case BICst.WIDGET.MAP:
                var da = this._formatDataForMap(data, 0);
                return BI.isEmptyArray(da) ? da : [da];
            case BICst.WIDGET.GIS_MAP:
                var da = this._formatDataForGISMap(data);
                return BI.isEmptyArray(da) ? da : [da];
        }
    },

    _refreshDimsInfo: function () {
        var self = this, o = this.options;
        this.dimIds = [];
        this.crossDimIds = [];
        var view = BI.Utils.getWidgetViewByID(o.wId);
        var drill = BI.Utils.getDrillByID(o.wId);

        BI.each(view[BICst.REGION.DIMENSION1], function (i, dId) {
            BI.Utils.isDimensionUsable(dId) && (self.dimIds.push(dId));
        });
        BI.each(view[BICst.REGION.DIMENSION2], function (i, dId) {
            BI.Utils.isDimensionUsable(dId) && (self.crossDimIds.push(dId));
        });
        if (BI.Utils.getWidgetTypeByID(o.wId) === BICst.WIDGET.MAP) {
            //地图不需要调整dimIds
        } else {
            BI.each(drill, function (drId, drArray) {
                if (drArray.length !== 0) {
                    var dIndex = self.dimIds.indexOf(drId), cIndex = self.crossDimIds.indexOf(drId);
                    BI.remove(self.dimIds, drId);
                    BI.remove(self.crossDimIds, drId);
                    BI.each(drArray, function (i, dr) {
                        var tempDrId = dr.dId;
                        if (i === drArray.length - 1) {
                            if (BI.Utils.getRegionTypeByDimensionID(drId) === BICst.REGION.DIMENSION1) {
                                self.dimIds.splice(dIndex, 0, tempDrId);
                            } else {
                                self.crossDimIds.splice(cIndex, 0, tempDrId);
                            }
                        } else {
                            BI.remove(self.dimIds, tempDrId);
                            BI.remove(self.crossDimIds, tempDrId);
                        }
                    });
                }
            });
        }
    },

    getWidgetData: function (type, callback) {
        var self = this, o = this.options;
        var options = {};
        this._refreshDimsInfo();
        var realData = true;
        if (o.status === BICst.WIDGET_STATUS.DETAIL) {
            realData = BI.Utils.isShowWidgetRealDataByID(o.wId) || false;
        }
        BI.Utils.getWidgetDataByID(o.wId, {
            success: function (jsonData) {
                if (BI.isNotNull(jsonData.error)) {
                    callback(jsonData);
                    return;
                }
                var data = self.parseChartData(jsonData.data);
                var types = [];
                var targetIds = self._getShowTarget();
                var count = 0;
                BI.each(data, function (idx, da) {
                    var t = [];
                    BI.each(da, function (id, d) {
                        if (type === BICst.WIDGET.MULTI_AXIS_COMBINE_CHART || type === BICst.WIDGET.COMBINE_CHART) {
                            var chart = BI.Utils.getDimensionStyleOfChartByID(targetIds[count] || targetIds[0]) || {};
                            t.push(chart.type || BICst.WIDGET.COLUMN);
                        } else {
                            t.push(type);
                        }
                        count++;
                    });
                    types.push(t);
                });
                if (BI.isEmptyArray(types)) {
                    types.push([type]);
                }
                BI.each(data, function (idx, item) {
                    var i = BI.UUID();
                    var type = types[idx];
                    BI.each(item, function (id, it) {
                        (type[id] === BICst.WIDGET.ACCUMULATE_AREA || type[id] === BICst.WIDGET.ACCUMULATE_COLUMN) && BI.extend(it, {stack: i});
                    });
                });
                if (type === BICst.WIDGET.MAP) {
                    var subType = BI.Utils.getWidgetSubTypeByID(o.wId);
                    if (BI.isNull(subType)) {
                        BI.find(MapConst.INNER_MAP_INFO.MAP_LAYER, function (path, layer) {
                            if (layer === 0) {
                                subType = path;
                                return true;
                            }
                        });
                    }
                    var name = MapConst.INNER_MAP_INFO.MAP_TYPE_NAME[subType];
                    if (BI.isNull(name)) {
                        name = MapConst.CUSTOM_MAP_INFO.MAP_TYPE_NAME[subType]
                    }
                    options.initDrillPath = [name];
                    var drill = BI.values(BI.Utils.getDrillByID(o.wId))[0];
                    BI.each(drill, function (idx, dri) {
                        options.initDrillPath.push(dri.values[0].value[0]);
                    });
                    options.geo = {
                        data: MapConst.INNER_MAP_INFO.MAP_PATH[subType] || MapConst.CUSTOM_MAP_INFO.MAP_PATH[subType],
                        name: MapConst.INNER_MAP_INFO.MAP_TYPE_NAME[subType] || MapConst.CUSTOM_MAP_INFO.MAP_TYPE_NAME[subType]
                    }
                }
                if (type === BICst.WIDGET.GIS_MAP) {
                    options.geo = {
                        "tileLayer": "http://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}",
                        "attribution": "<a><img src=\"http://webapi.amap.com/theme/v1.3/mapinfo_05.png\">&copy; 2016 AutoNavi</a>"
                    };
                }
                //var opts = Data.Utils.getWidgetData(jsonData.data, {
                //    type: BI.Utils.getWidgetTypeByID(o.wId),
                //    sub_type: BI.Utils.getWidgetSubTypeByID(o.wId),
                //    view: BI.Utils.getWidgetViewByID(o.wId),
                //    clicked: BI.Utils.getClickedByID(o.wId),
                //    settings: BI.Utils.getWidgetSettingsByID(o.wId),
                //    dimensions: BI.Utils.getWidgetDimensionsByID(o.wId)
                //});
                //callback(opts.types, opts.data, opts.options);
                callback(types, data, options);
            }
        }, {
            expander: {
                x: {
                    type: true,
                    value: [[]]
                },
                y: {
                    type: true,
                    value: [[]]
                }
            },
            page: -1,
            real_data: realData
        });
    },

    getLinkageInfo: function (obj) {
        var o = this.options;
        this._refreshDimsInfo();
        var dId = [], clicked = [];
        var clickeddId = obj.dId || this.dimIds[0];
        switch (BI.Utils.getWidgetTypeByID(o.wId)) {
            case BICst.WIDGET.BUBBLE:
            case BICst.WIDGET.SCATTER:
                dId = obj.targetIds;
                clicked = [{
                    dId: clickeddId,
                    value: [BI.Utils.getClickedValue4Group(obj.zValue, clickeddId)]
                }];
                break;
            case BICst.WIDGET.MAP:
            case BICst.WIDGET.GIS_MAP:
                dId = obj.targetIds;
                clicked = [{
                    dId: clickeddId,
                    value: [BI.Utils.getClickedValue4Group(obj.xValue, clickeddId)]
                }];
                break;
            default:
                dId = obj.targetIds;
                if (BI.isNotNull(this.cataDid)) {
                    clicked = [{
                        dId: clickeddId,
                        value: [BI.Utils.getClickedValue4Group(obj.xValue, clickeddId)]
                    }];
                }
                if (BI.isNotNull(this.seriesDid)) {
                    clicked.push({
                        dId: obj.dId || this.crossDimIds[0],
                        value: [BI.Utils.getClickedValue4Group(obj.zValue, obj.dId || this.crossDimIds[0])]
                    })
                }
                break;
        }
        if (BI.isNull(dId) || BI.isNull(clicked)) {
            return {};
        } else {
            return {dId: dId, clicked: clicked};
        }
    }

});