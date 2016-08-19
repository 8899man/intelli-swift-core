BI.ChartDisplayModel = BI.inherit(FR.OB, {

    constants: {
        BUBBLE_REGION_COUNT: 4,
        SCATTER_REGION_COUNT: 3
    },

    _init: function(){
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
                    if(BI.isNull(assertValue(item.s[idx]))){
                        return;
                    }
                    if (BI.has(view, BICst.REGION.TARGET2) && BI.contains(view[BICst.REGION.TARGET2], targetIds[idx])) {
                        switch(type){
                            case BICst.WIDGET.BUBBLE:
                            case BICst.WIDGET.AXIS:
                            case BICst.WIDGET.PIE:
                            default:
                                res = {
                                    x: item.n,
                                    y: (BI.isFinite(item.s[idx]) ? item.s[idx] : 0),
                                    targetIds: [targetIds[idx]],
                                    dId: self.dimIds[currentLayer - 1],
                                    drillDid: self.dimIds[currentLayer]
                                };
                        }
                    }else{
                        res = {
                            x: item.n,
                            y: (BI.isFinite(item.s[idx]) ? item.s[idx] : 0),
                            targetIds: [targetIds[idx]],
                            settings: BI.Utils.getDimensionSettingsByID(targetIds[idx]),
                            dId: self.dimIds[currentLayer - 1],
                            drillDid: self.dimIds[currentLayer]
                        };
                    }
                    if(BI.has(item, "c")){
                        res.drilldown = {};
                        res.drilldown.series = self._formatDataForMap(item, currentLayer);
                        res.drilldown.geo = {
                            data: BICst.MAP_PATH[BICst.MAP_NAME[res.x]],
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

        function assertValue(v){
            if(BI.isNull(v)){
                return;
            }
            if(!BI.isFinite(v)){
                return 0;
            }
            return v;
        }
    },

    _formatDataForGISMap: function(data){
        var self = this, o = this.options;
        var targetIds = this._getShowTarget();
        if (BI.has(data, "t")) {
            var top = data.t, left = data.l;
            var init = BI.map(top.c, function (id, tObj) {
                var data = [];
                BI.each(left.c, function (idx, obj) {
                    var x = obj.n;
                    BI.each(obj.s.c[id].s, function(i, o){
                        if(BI.isNotNull(o) && BI.isNotNull(x)){
                            data.push({
                                "x": x,
                                "z": tObj.n,
                                "y": o,
                                targetIds: [targetIds[i]]
                            });
                        }
                    });
                });
                var name = tObj.n;
                var obj = {};
                obj.data = data;
                obj.name = name;
                return obj;
            });
            var result = [];
            var size = 0;
            if(init.length > 0){
                size = targetIds.length;
            }
            BI.each(BI.makeArray(size, null), function(idx, index){
                var res = {data: [], name: BI.Utils.getDimensionNameByID(targetIds[idx])};
                BI.each(init, function(id, obj){
                    res.data.push(obj.data[idx]);
                });
                result.push(res);
            });
            return result;
        }
        if (BI.has(data, "c")) {
            var obj = (data.c)[0];
            var columnSizeArray = BI.makeArray(BI.isNull(obj) ? 0 : BI.size(obj.s), 0);
            return BI.map(columnSizeArray, function (idx, value) {
                var adjustData = BI.map(data.c, function (id, item) {
                    var x = item.n;
                    return {
                        x: x,
                        y: item.s[idx],
                        targetIds: [targetIds[idx]]
                    };
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
        data = this._showDataLabel(data);
        if (BI.isEmptyArray(data)) {
            return [];
        }
        var view = BI.Utils.getWidgetViewByID(o.wId);
        var array = [];
        BI.each(this.targetIds, function (idx, tId) {
            if (BI.has(view, BICst.REGION.TARGET1) && BI.contains(view[BICst.REGION.TARGET1], tId)) {
                array.length === 0 && array.push([]);
                if(checkSeriesExist()){
                    array[0] = data;
                }else{
                    array[0].push(data[idx])
                }
            }
            if(BI.has(view, BICst.REGION.TARGET2) && BI.contains(view[BICst.REGION.TARGET2], tId)) {
                while (array.length < 2){array.push([]);}
                if(checkSeriesExist()){
                    array[1] = data;
                }else{
                    array[1].push(data[idx])
                }
            }
            if(BI.has(view, BICst.REGION.TARGET3) && BI.contains(view[BICst.REGION.TARGET3], tId)){
                while (array.length < 3){array.push([]);}
                if(checkSeriesExist()){
                    array[2] = data;
                }else{
                    array[2].push(data[idx])
                }
            }
        });
        return array;

        function checkSeriesExist(){
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
            var dGroup = BI.Utils.getDimensionGroupByID(self.cataDid);
            if (BI.isNotNull(dGroup) && dGroup.type === BICst.GROUP.YMD) {
                var date = new Date(BI.parseInt(name));
                name = date.print("%Y-%X-%d");
            }
            obj.data = [{
                x: (BI.isFinite(item.s[1]) ? item.s[1] : 0),
                y: (BI.isFinite(item.s[0]) ? item.s[0] : 0),
                z: (BI.isFinite(item.s[2]) ? item.s[2] : 0),
                seriesName: seriesName,
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
            var dGroup = BI.Utils.getDimensionGroupByID(self.cataDid);
            if (BI.isNotNull(dGroup) && dGroup.type === BICst.GROUP.YMD) {
                var date = new Date(BI.parseInt(name));
                name = date.print("%Y-%X-%d");
            }
            obj.name = name;
            obj.data = [{
                x: (BI.isFinite(item.s[1]) ? item.s[1] : 0),
                y: (BI.isFinite(item.s[0]) ? item.s[0] : 0),
                seriesName: seriesName,
                targetIds: [targetIds[0], targetIds[1]]
            }];
            return obj;
        })];
    },

    _getDrillDimensionId: function (drill) {
        if(BI.isEmptyArray(drill) || BI.isNull(drill)){
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
        if(BI.isNotNull(drillcataDimId)){
            cataGroup = BI.Utils.getDimensionGroupByID(drillcataDimId);
        }
        if(BI.isNotNull(drillseriDimId)){
            seriesGroup = BI.Utils.getDimensionGroupByID(drillseriDimId);
        }
        if (BI.has(data, "t")) {
            var top = data.t, left = data.l;
            return BI.map(top.c, function (id, tObj) {
                if(BI.isNull(tObj.c)){
                    return {
                        data: [],
                        name: BI.Utils.getDimensionNameByID(tObj.n)
                    };
                }
                var name = tObj.n, seriesName = tObj.n;
                if (BI.isNotNull(seriesGroup) && seriesGroup.type === BICst.GROUP.YMD) {
                    var date = new Date(BI.parseInt(name));
                    name = date.print("%Y-%X-%d");
                }
                var data = BI.map(left.c, function (idx, obj) {
                    var value = obj.n, x = obj.n;
                    if (BI.isNotNull(cataGroup) && cataGroup.type === BICst.GROUP.YMD) {
                        var date = new Date(BI.parseInt(x));
                        x = date.print("%Y-%X-%d");
                    }
                    return {
                        "x": x,
                        "y": (BI.isFinite(obj.s.c[id].s[0]) ? obj.s.c[id].s[0] : 0),
                        "value": value,
                        seriesName: seriesName,
                        targetIds: [targetIds[0]]
                    };
                });
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
                    if (BI.isNotNull(cataGroup) && cataGroup.type === BICst.GROUP.YMD) {
                        var date = new Date(BI.parseInt(x));
                        x = date.print("%Y-%X-%d");
                    }
                    return {
                        x: x,
                        y: (BI.isFinite(item.s[idx]) ? item.s[idx] : 0),
                        value: value,
                        seriesName: BI.Utils.getDimensionNameByID(targetIds[idx]),
                        targetIds: [targetIds[idx]]
                    };
                });
                var obj = {};
                obj.data = adjustData;
                obj.name = BI.Utils.getDimensionNameByID(targetIds[idx]);
                return obj;
            });
        }
        if(BI.has(data, "s")){
            var type = BI.Utils.getWidgetTypeByID(o.wId);
            if(type === BICst.WIDGET.PIE){
                var adjustData = BI.map(data.s, function (idx, value) {
                    return {
                        x: BI.Utils.getDimensionNameByID(targetIds[idx]),
                        y: (BI.isFinite(value) ? value : 0),
                        targetIds: [targetIds[idx]]
                    };
                });
                var obj = {};
                obj.data = adjustData;
                return [obj];
            }else{
                return BI.map(data.s, function (idx, value) {
                    return {
                        name: BI.Utils.getDimensionNameByID(targetIds[idx]),
                        data: [{
                            x: "",
                            y: (BI.isFinite(value) ? value : 0),
                            targetIds: [targetIds[idx]]
                        }]
                    };
                });
            }
        }
        return [];
    },

    _formatDataForDashBoard: function (data) {
        var self = this, o = this.options;
        var targetIds = this._getShowTarget();
        var cataGroup = BI.Utils.getDimensionGroupByID(self.cataDid);
        var drillcataDimId = this._getDrillDimensionId(BI.Utils.getDrillByID(o.wId)[self.cataDid]);
        if(BI.isNotNull(drillcataDimId)){
            cataGroup = BI.Utils.getDimensionGroupByID(drillcataDimId);
        }
        if (BI.has(data, "c")) {
            var adjustData = BI.map(data.c, function (id, item) {
                var seriesName = item.n;
                if (BI.isNotNull(cataGroup) && cataGroup.type === BICst.GROUP.YMD) {
                    var date = new Date(BI.parseInt(seriesName));
                    seriesName = date.print("%Y-%X-%d");
                }
                var data = [{
                    x: BI.Utils.getDimensionNameByID(targetIds[0]),
                    y: (BI.isFinite(item.s[0]) ? item.s[0] : 0),
                    seriesName: item.n,
                    targetIds: [targetIds[0]]
                }];
                var obj = {};
                obj.data = data;
                obj.name = seriesName;
                return obj;
            });
            return BI.isEmptyArray(adjustData) ? [] : [adjustData];
        }
        if (BI.has(data, "s")) {
            var obj = {};
            obj.name = "";
            obj.data = BI.map(data.s, function (idx, value) {
                return {
                    x: BI.Utils.getDimensionNameByID(targetIds[idx]),
                    y: (BI.isFinite(value) ? value : 0),
                    seriesName: BI.Utils.getDimensionNameByID(targetIds[idx]),
                    targetIds: [targetIds[idx]]
                };
            });
            return BI.isEmptyArray(obj.data) ? [] : [[obj]];
        }
        return [];
    },

    _showDataLabel: function (data) {
        console.log(data);
        var self = this, o = this.options;
        var hasSeries = false;
        BI.each(BI.Utils.getWidgetViewByID(o.wId)[20000], function (idx, dId) {
            if (BI.Utils.isDimensionUsable(dId)) {
                hasSeries = true
            }
        });
        BI.each(BI.Utils.getAllUsableTargetDimensionIDs(o.wId), function (idx, dId) {
            BI.each(BI.Utils.getDatalabelByID(dId), function (id, dataLabel) {
                if (BI.isEmpty(dataLabel.target_id)) {
                    return;
                }
                if (hasSeries && BI.Utils.getWidgetViewByID(o.wId)[20000].indexOf(dataLabel.target_id) != -1) { //有系列过滤
                    BI.each(data, function (i, item) {
                        filterForSeries(item, dataLabel);
                    });
                } else {
                    BI.each(data, function (i, item) {
                        BI.each(item.data, function (k, dt) {
                            filterForSelf(dt, dataLabel);
                            filterForClassify(dt, dataLabel);
                        })
                    });
                }
            })
        });
        return data;
        
        function filterForSelf(data, label) {
            switch (label.filter_type) {
                case BICst.DATA_LABEL_FILTER_NUMBER.BELONG_VALUE:
                    var min = label.filter_value.closemin ? data.y >= label.filter_value.min : data.y > label.filter_value.min;
                    var max = label.filter_value.closemax ? data.y <= label.filter_value.max : data.y < label.filter_value.max;
                    if (min && max) {
                       createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_NUMBER.NOT_BELONG_VALUE:
                    var min = label.filter_value.closemin ? data.y < label.filter_value.min : data.y <= label.filter_value.min;
                    var max = label.filter_value.closemax ? data.y > label.filter_value.max : data.y >= label.filter_value.max;
                    if (min || max) {
                        createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_NUMBER.EQUAL_TO:
                    if (data.y == label.filter_value) {
                        createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_NUMBER.NOT_EQUAL_TO:
                    if (data.y != label.filter_value) {
                        createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_NUMBER.IS_NULL:
                    if (data.y == null) {
                        createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_NUMBER.NOT_NULL:
                    if (data.y != null) {
                        createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_NUMBER.LARGE_OR_EQUAL_CAL_LINE:
                    break;
                case BICst.DATA_LABEL_FILTER_NUMBER.SMALL_THAN_CAL_LINE:
                    break;
                case BICst.DATA_LABEL_FILTER_NUMBER.TOP_N:
                    break;
            }
        }
        function filterForClassify(data, label) {
            switch (label.filter_type) {
                case BICst.DATA_LABEL_FILTER_STRING.BELONG_VALUE:
                    if (!label.filter_value.value || label.filter_value.value.indexOf(data.value) != -1) {
                        createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.NOT_BELONG_VALUE:
                    if (label.filter_value.value && label.filter_value.value.indexOf(data.value) == -1) {
                        createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.CONTAIN:
                    if (!label.filter_value || data.value.indexOf(label.filter_value) != -1 && label.filter_value) {
                        createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.NOT_CONTAIN:
                    if (!label.filter_value || data.value.indexOf(label.filter_value) == -1) {
                        createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.IS_NULL:
                    if (data.value == null) {
                        createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.NOT_NULL:
                    if (data.value != null) {
                        createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.BEGIN_WITH:
                    if (!label.filter_value || data.value.startWith(label.filter_value)) {
                        createDataLabel(data, label);
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.END_WITH:
                    if (!label.filter_value || data.value.endWith(label.filter_value || "")) {
                        createDataLabel(data, label);
                    }
                    break;
            }
        }
        function filterForSeries(data, label) {
            switch (label.filter_type) {
                case BICst.DATA_LABEL_FILTER_STRING.BELONG_VALUE:
                    if (!label.filter_value.value || label.filter_value.value.indexOf(data.name) != -1) {
                        BI.each(data.data, function (idx, item) {
                            createDataLabel(item, label);
                        })
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.NOT_BELONG_VALUE:
                    if (label.filter_value.value && label.filter_value.value.indexOf(data.name) == -1) {
                        BI.each(data.data, function (idx, item) {
                            createDataLabel(item, label);
                        })
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.CONTAIN:
                    if (!label.filter_value || data.name.indexOf(label.filter_value) != -1 && label.filter_value) {
                        BI.each(data.data, function (idx, item) {
                            createDataLabel(item, label);
                        })
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.NOT_CONTAIN:
                    if (!label.filter_value || data.name.indexOf(label.filter_value) == -1) {
                        BI.each(data.data, function (idx, item) {
                            createDataLabel(item, label);
                        })
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.IS_NULL:
                    if (data.name == null) {
                        BI.each(data.data, function (idx, item) {
                            createDataLabel(item, label);
                        })
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.NOT_NULL:
                    if (data.name != null) {
                        BI.each(data.data, function (idx, item) {
                            createDataLabel(item, label);
                        })
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.BEGIN_WITH:
                    if (!label.filter_value || data.name.startWith(label.filter_value)) {
                        BI.each(data.data, function (idx, item) {
                            createDataLabel(item, label);
                        })
                    }
                    break;
                case BICst.DATA_LABEL_FILTER_STRING.END_WITH:
                    if (!label.filter_value || data.name.endWith(label.filter_value || "")) {
                        BI.each(data.data, function (idx, item) {
                            createDataLabel(item, label);
                        })
                    }
                    break;
            }
        }
        function createDataLabel(data, label) {
            var dataLables = {
                enabled: true,
                align: "outside",
                useHtml: true,
                style: {},
                formatter: "function(){return '<div>"+data.y+"</div>'}"
            };
            switch (label.style_setting.type) {
                case BICst.DATA_LABEL_STYLE_TYPE.TEXT:
                    dataLables.style = label.style_setting.textStyle;
                    break;
                case BICst.DATA_LABEL_STYLE_TYPE.IMG:
                    dataLables.formatter = "function(){return '<div><img width=\"20px\" height=\"20px\" src=\""+label.style_setting.imgStyle.src+"\"></div>';}";
                    break;
            }
            data.dataLabels =  dataLables;
        }
    },

    getToolTip: function (type) {
        var o = this.options;
        switch (type) {
            case BICst.WIDGET.SCATTER:
                if(this.targetIds.length < 2){
                    return "";
                }else{
                    return "function(){ return this.seriesName+'<div>(X)" + BI.Utils.getDimensionNameByID(this.targetIds[1]) +":'+ this.x +'</div><div>(Y)"
                        + BI.Utils.getDimensionNameByID(this.targetIds[0]) +":'+ this.y +'</div>'}";
                }
            case BICst.WIDGET.BUBBLE:
                if(this.targetIds.length < 3){
                    return "";
                }else{
                    return "function(){ return this.seriesName+'<div>(X)" + BI.Utils.getDimensionNameByID(this.targetIds[1]) +":'+ this.x +'</div><div>(Y)"
                        + BI.Utils.getDimensionNameByID(this.targetIds[0]) +":'+ this.y +'</div><div>(" + BI.i18nText("BI-Size") + ")" + BI.Utils.getDimensionNameByID(this.targetIds[2])
                        + ":'+ this.size +'</div>'}";
                }
            default:
                return "";
        }
    },

    getCordon: function () {
        var o = this.options;
        var cordon = {};
        var result = [];
        BI.each(BI.Utils.getAllDimensionIDs(o.wId), function(idx, dId){
            if(!BI.Utils.isDimensionUsable(dId)){
                return;
            }
            var items = BI.map(BI.Utils.getDimensionCordonByID(dId), function(id, cor){
                return {
                    text: cor.cordon_name,
                    value: cor.cordon_value,
                    color: cor.cordon_color
                }
            });
            var regionType = BI.Utils.getRegionTypeByDimensionID(dId);
            if(BI.isNotEmptyArray(items)){
                BI.has(cordon, regionType) === false && (cordon[regionType] = []);
                cordon[regionType] = BI.concat(cordon[regionType], items);
            }
        });
        var type = BI.Utils.getWidgetTypeByID(o.wId);
        if(type === BICst.WIDGET.SCATTER || type === BICst.WIDGET.BUBBLE){
            result.push(BI.isNull(cordon[BICst.REGION.TARGET2]) ? [] : cordon[BICst.REGION.TARGET2]);
            result.push(BI.isNull(cordon[BICst.REGION.TARGET1]) ? [] : cordon[BICst.REGION.TARGET1]);
            return result;
        }
        if(type === BICst.WIDGET.BAR || type ===BICst.WIDGET.ACCUMULATE_BAR){
            result.push(BI.isNull(cordon[BICst.REGION.TARGET1]) ? [] : cordon[BICst.REGION.TARGET1]);
            result.push(BI.isNull(cordon[BICst.REGION.DIMENSION1]) ? [] : cordon[BICst.REGION.DIMENSION1]);
            return result;
        }
        if(type === BICst.WIDGET.COMPARE_BAR){
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
            case BICst.WIDGET.ACCUMULATE_AXIS:
            case BICst.WIDGET.ACCUMULATE_AREA:
            case BICst.WIDGET.ACCUMULATE_RADAR:
            case BICst.WIDGET.AXIS:
            case BICst.WIDGET.LINE:
            case BICst.WIDGET.AREA:
            case BICst.WIDGET.PERCENT_ACCUMULATE_AXIS:
            case BICst.WIDGET.PERCENT_ACCUMULATE_AREA:
            case BICst.WIDGET.COMPARE_AXIS:
            case BICst.WIDGET.COMPARE_AREA:
            case BICst.WIDGET.FALL_AXIS:
            case BICst.WIDGET.RANGE_AREA:
            case BICst.WIDGET.BAR:
            case BICst.WIDGET.ACCUMULATE_BAR:
            case BICst.WIDGET.COMPARE_BAR:
            case BICst.WIDGET.COMBINE_CHART:
            case BICst.WIDGET.DONUT:
            case BICst.WIDGET.RADAR:
            case BICst.WIDGET.PIE:
            case BICst.WIDGET.MULTI_AXIS_COMBINE_CHART:
            case BICst.WIDGET.DASHBOARD:
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

    _refreshDimsInfo: function(){
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
        
    },

    //clicked 中的值，如果是分组名使用分组对应的id
    _parseClickedValue4Group: function (v, dId) {
        var group = BI.Utils.getDimensionGroupByID(dId);
        var fieldType = BI.Utils.getFieldTypeByDimensionID(dId);
        var clicked = v;

        if (BI.isNotNull(group)) {
            if (fieldType === BICst.COLUMN.STRING) {
                var details = group.details,
                    ungroup2Other = group.ungroup2Other,
                    ungroup2OtherName = group.ungroup2OtherName;
                if (ungroup2Other === BICst.CUSTOM_GROUP.UNGROUP2OTHER.SELECTED &&
                    ungroup2OtherName === v) {
                    clicked = BICst.UNGROUP_TO_OTHER;
                }
                BI.some(details, function (i, detail) {
                    if (detail.value === v) {
                        clicked = detail.id;
                        return true;
                    }
                });
            } else if (fieldType === BICst.COLUMN.NUMBER) {
                var groupValue = group.group_value, groupType = group.type;
                if (groupType === BICst.GROUP.CUSTOM_NUMBER_GROUP) {
                    var groupNodes = groupValue.group_nodes, useOther = groupValue.use_other;
                    if (useOther === v) {
                        clicked = BICst.UNGROUP_TO_OTHER;
                    }
                    BI.some(groupNodes, function (i, node) {
                        if (node.group_name === v) {
                            clicked = node.id;
                            return true;
                        }
                    });
                }
            }
        }
        return clicked;
    },

    getWidgetData: function(type, callback){
        var self = this, o = this.options;
        var options = {};
        this._refreshDimsInfo();
        var realData = true;
        if(o.status === BICst.WIDGET_STATUS.DETAIL) {
            realData = BI.Utils.isShowWidgetRealDataByID(o.wId) || false;
        }
        BI.Utils.getWidgetDataByID(o.wId, function (jsonData) {
            if(BI.isNotNull(jsonData.error)) {
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
                        t.push(chart.type || BICst.WIDGET.AXIS);
                    } else {
                        t.push(type);
                    }
                    count++;
                });
                types.push(t);
            });
            if(BI.isEmptyArray(types)){
                types.push([type]);
            }
            BI.each(data, function(idx, item){
                var i = BI.UUID();
                var type = types[idx];
                BI.each(item, function(id, it){
                    (type[id] === BICst.WIDGET.ACCUMULATE_AREA || type[id] === BICst.WIDGET.ACCUMULATE_AXIS) && BI.extend(it, {stack: i});
                });
            });
            if(type === BICst.WIDGET.MAP){
                var subType = BI.Utils.getWidgetSubTypeByID(o.wId) || BICst.MAP_TYPE.CHINA;
                options.initDrillPath = [BICst.MAP_TYPE_NAME[subType]];
                var drill = BI.values(BI.Utils.getDrillByID(o.wId))[0];
                BI.each(drill, function(idx, dri){
                    options.initDrillPath.push(dri.values[0].value[0]);
                });
                options.geo = {
                    data: BICst.MAP_PATH[subType],
                    name: BICst.MAP_TYPE_NAME[subType] || BICst.MAP_TYPE_NAME[BICst.MAP_TYPE.CHINA]
                }
            }
            if(type === BICst.WIDGET.GIS_MAP){
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

    getLinkageInfo: function(obj){
        var o = this.options;
        this._refreshDimsInfo();
        var dId = [], clicked = [];
        var clickeddId = obj.dId || this.dimIds[0];
        switch (BI.Utils.getWidgetTypeByID(o.wId)) {
            case BICst.WIDGET.BUBBLE:
            case BICst.WIDGET.SCATTER:
            case BICst.WIDGET.DASHBOARD:
                dId = obj.targetIds;
                clicked = [{
                    dId: clickeddId,
                    value: [this._parseClickedValue4Group(obj.seriesName, clickeddId)]
                }];
                break;
            case BICst.WIDGET.MAP:
            case BICst.WIDGET.GIS_MAP:
                dId = obj.targetIds;
                clicked = [{
                    dId: clickeddId,
                    value: [this._parseClickedValue4Group(obj.x, clickeddId)]
                }];
                break;
            default:
                dId = obj.targetIds;
                clicked = [{
                    dId: clickeddId,
                    value: [this._parseClickedValue4Group(obj.value || obj.x, clickeddId)]
                }];
                if (BI.isNotNull(this.seriesDid)) {
                    clicked.push({
                        dId: obj.dId || this.crossDimIds[0],
                        value: [this._parseClickedValue4Group(obj.seriesName, obj.dId || this.crossDimIds[0])]
                    })
                }
                break;
        }
        if(BI.isNull(dId) || BI.isNull(clicked)){
            return {};
        }else{
            return {dId: dId, clicked: clicked};
        }
    }

});