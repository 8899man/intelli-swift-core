//控件详细设置界面选择图表类型
BIDezi.DateRangeDetailModel = BI.inherit(BI.Model, {
    _defaultConfig: function () {
        return BI.extend(BIDezi.DateRangeDetailModel.superclass._defaultConfig.apply(this, arguments), {
            name: "",
            dimensions: {},
            view: {},
            type: BICst.WIDGET.DATE,
            value: {}
        });
    },

    _static: function () {

    },

    change: function (changed, prev) {
        if (BI.has(changed, "dimensions")) {
            if (BI.size(changed.dimensions) !== BI.size(prev.dimensions)) {
                BI.Broadcasts.send(BICst.BROADCAST.DIMENSIONS_PREFIX + this.get("id"));
                BI.Broadcasts.send(BICst.BROADCAST.DIMENSIONS_PREFIX);
            }
            if (BI.size(changed.dimensions) >= BI.size(prev.dimensions)) {
                var result = BI.filter(changed.dimensions, function (did, dimension) {
                    return !BI.has(prev.dimensions, did);
                });
                if (BI.isNotEmptyArray(result)) {
                    BI.each(result, function(idx, dimension){
                        BI.Broadcasts.send(BICst.BROADCAST.SRC_PREFIX + dimension._src.id, true);
                    });
                }
            }
        }
    },

    splice: function (old, key1, key2) {
        if (key1 === "dimensions") {
            var views = this.get("view");
            BI.each(views, function (region, arr) {
                BI.remove(arr, function (i, id) {
                    return key2 === id;
                })
            });
            this.set("view", views);
        }
        if (key1 === "dimensions") {
            BI.Broadcasts.send(BICst.BROADCAST.SRC_PREFIX + old._src.id);
            BI.Broadcasts.send(BICst.BROADCAST.DIMENSIONS_PREFIX + this.get("id"));
            //全局维度增删事件
            BI.Broadcasts.send(BICst.BROADCAST.DIMENSIONS_PREFIX);
        }
    },

    local: function () {
        if (this.has("addDimension")) {
            var addDimensions = this.get("addDimension");
            var dimensions = this.get("dimensions");
            var view = this.get("view");
            var srcs = BI.isArray(addDimensions.src) ? addDimensions.src : [addDimensions.src];
            var dIds = BI.isArray(addDimensions.dId) ? addDimensions.dId : [addDimensions.dId];
            BI.each(dIds, function (idx, dId) {
                if (!dimensions[dId]) {
                    var src = srcs[idx];
                    dimensions[dId] = {
                        name: src.name,
                        _src: src._src,
                        type: src.type
                    };
                    if (!view[BICst.REGION.DIMENSION1]) {
                        view[BICst.REGION.DIMENSION1] = [];
                    }
                    view[BICst.REGION.DIMENSION1].push(dId);

                }
            });
            this.set({"dimensions": dimensions, view: view});
            return true;
        }
        return false;
    },

    _init: function () {
        BIDezi.DateRangeDetailModel.superclass._init.apply(this, arguments);
    }
});