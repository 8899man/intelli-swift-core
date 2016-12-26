/**
 * Created by zcf on 12/22/2016.
 */
BIDezi.DatePaneDetailModel=BI.inherit(BI.Model,{
    _defaultConfig: function () {
        return BI.extend(BIDezi.DatePaneDetailModel.superclass._defaultConfig.apply(this, arguments), {
            name: "",
            dimensions: {},
            view: {},
            type: BICst.WIDGET.DATE_PANE,
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
                var result = BI.find(changed.dimensions, function (did, dimension) {
                    return !BI.has(prev.dimensions, did);
                });
                if (BI.isNotNull(result)) {
                    BI.Broadcasts.send(BICst.BROADCAST.SRC_PREFIX + result._src.id, true);
                }
            }
            if (BI.size(changed.dimensions) < BI.size(prev.dimensions)) {
                var res = BI.find(prev.dimensions, function (did, dimension) {
                    return !BI.has(changed.dimensions, did);
                });
                if (BI.isNotNull(res)) {
                    BI.Broadcasts.send(BICst.BROADCAST.SRC_PREFIX + res._src.id);
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
            var dimension = this.get("addDimension");
            var view = this.get("view");
            var src = dimension.src;
            var dId = dimension.dId;
            var dimensions = this.get("dimensions");
            if (!dimensions[dId]) {
                //维度指标基本属性
                dimensions[dId] = {
                    name: src.name,
                    _src: src._src,
                    type: src.type
                };
                if (!view[BICst.REGION.DIMENSION1]) {
                    view[BICst.REGION.DIMENSION1] = [];
                }
                view[BICst.REGION.DIMENSION1].push(dId);
                this.set({"dimensions": dimensions, view: view});
            }
            return true;
        }
        return false;
    },

    _init: function () {
        BIDezi.DatePaneDetailModel.superclass._init.apply(this, arguments);
    }
});