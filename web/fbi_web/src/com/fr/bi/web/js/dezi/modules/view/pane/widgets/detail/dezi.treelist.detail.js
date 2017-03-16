/**
 * Created by zcf on 2016/12/21.
 */
BIDezi.TreeListDetailView=BI.inherit(BI.View,{

    constants: {
        DETAIL_NORTH_HEIGHT: 40,
        DETAIL_TAB_WIDTH: 100,
        DETAIL_TAB_HEIGHT: 40,
        DETAIL_WEST_WIDTH: 280,
        DETAIL_DATA_STYLE_HEIGHT: 240,
        DETAIL_GAP_NORMAL: 10,
        DETAIL_PANE_HORIZONTAL_GAP: 10

    },

    _defaultConfig: function () {
        return BI.extend(BIDezi.TreeListDetailView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-widget-attribute-setter"
        })
    },

    _init: function () {
        BIDezi.TreeListDetailView.superclass._init.apply(this, arguments);
    },

    _render: function (vessel) {
        var mask = BI.createWidget();
        mask.element.__buildZIndexMask__(0);
        var west = this._buildWest();
        var items = [{
            el: west,
            width: this.constants.DETAIL_WEST_WIDTH
        }, {
            type: "bi.vtape",
            items: [{
                el: this._buildNorth(), height: this.constants.DETAIL_NORTH_HEIGHT
            }, {
                el: this._buildCenter()
            }]
        }];
        var htape = BI.createWidget({
            type: "bi.htape",
            cls: "widget-attribute-setter-container",
            items: items
        });
        west.element.resizable({
            handles: "e",
            minWidth: 200,
            maxWidth: 400,
            autoHide: true,
            helper: "bi-resizer",
            start: function () {
            },
            resize: function (e, ui) {
            },
            stop: function (e, ui) {
                items[0].width = ui.size.width;
                htape.resize();
            }
        });
        BI.createWidget({
            type: "bi.absolute",
            element: vessel,
            items: [{
                el: mask,
                left: 0,
                right: 0,
                top: 0,
                bottom: 0
            }, {
                el: htape,
                left: 20,
                right: 20,
                top: 20,
                bottom: 20
            }]
        });
    },

    _buildNorth: function () {
        var self = this;
        this.title = BI.createWidget({
            type: "bi.label",
            textAlign: "left",
            cls: "widget-top-name",
            height: 25,
            text: this.model.get("name")
        });
        var shrink = BI.createWidget({
            type: "bi.button",
            height: 25,
            title: BI.i18nText('BI-Return_To_Dashboard'),
            text: BI.i18nText('BI-Detail_Set_Complete'),
            handler: function () {
                self.notifyParentEnd();
            }
        });
        return BI.createWidget({
            type: "bi.left_right_vertical_adapt",
            items: {
                left: [this.title],
                right: [shrink]
            },
            lhgap: this.constants.DETAIL_PANE_HORIZONTAL_GAP,
            rhgap: this.constants.DETAIL_PANE_HORIZONTAL_GAP
        });
    },

    _buildWest: function () {
        return BI.createWidget({
            type: "bi.absolute",
            items: [{
                el: {
                    type: "bi.tree_select_data",
                    wId: this.model.get("id"),
                    cls: "widget-select-data-pane"
                },
                left: this.constants.DETAIL_PANE_HORIZONTAL_GAP,
                top: this.constants.DETAIL_GAP_NORMAL,
                bottom: this.constants.DETAIL_GAP_NORMAL,
                right: this.constants.DETAIL_PANE_HORIZONTAL_GAP
            }],
            cls: "widget-attr-west"
        });
    },

    _buildCenter: function () {
        var treeList = this._createCombo();
        var top = BI.createWidget({
            type: "bi.vtape",
            cls: "widget-top-wrapper",
            items: [{
                el: {
                    type: "bi.button_group",
                    items: BI.createItems([{
                        text: BI.i18nText("BI-Data"),
                        selected: true
                    }], {
                        type: "bi.line_segment_button",
                        height: this.constants.DETAIL_TAB_HEIGHT
                    }),
                    height: this.constants.DETAIL_TAB_HEIGHT,
                    layouts: [{
                        type: "bi.absolute_center_adapt",
                        items: [{
                            type: "bi.center",
                            width: this.constants.DETAIL_TAB_WIDTH,
                            height: this.constants.DETAIL_TAB_HEIGHT
                        }]
                    }]
                },
                height: this.constants.DETAIL_TAB_HEIGHT
            }, {
                el: this._createRegion()
            }]
        });

        return BI.createWidget({
            type: "bi.absolute",
            cls: "widget-attr-center",
            items: [{
                el: {
                    type: "bi.vtape",
                    items: [{
                        el: top,
                        height: this.constants.DETAIL_DATA_STYLE_HEIGHT - this.constants.DETAIL_NORTH_HEIGHT
                    }, {
                        el: {
                            type: "bi.absolute",
                            cls: "widget-center-wrapper",
                            items: [{
                                el: treeList,
                                left: 10,
                                right: 10,
                                top: 10,
                                bottom:10
                            }]
                        }
                    }],
                    vgap: 10
                },
                left: 0,
                right: this.constants.DETAIL_PANE_HORIZONTAL_GAP,
                top: 0,
                bottom: this.constants.DETAIL_GAP_NORMAL
            }]
        });
    },

    _createRegion: function () {
        var self = this;
        var dimensionsVessel = {};
        this.dimensionsManager = BI.createWidget({
            type: "bi.tree_dimensions_manager",
            wId: this.model.get("id"),
            dimensionCreator: function (dId, regionType, op) {
                if (op && BI.isNotNull(op.relationItem)) {
                    self.model.set("setRelation", {
                        dId: dId,
                        relationItem: op.relationItem
                    });
                }

                var dimensions = self.model.cat("dimensions");
                if(BI.isArray(dId)){
                    BI.each(dId, function(idx, d){
                        createSubVessel(d);
                    });
                    if (BI.isNotEmptyArray(BI.difference(dId, BI.keys(dimensions)))) {
                        self.model.set("addDimension", {
                            dId: dId,
                            regionType: regionType,
                            src: op
                        });
                    }
                    return null;
                }else{
                    createSubVessel(dId);
                    if (!BI.has(dimensions, dId)) {
                        self.model.set("addDimension", {
                            dId: dId,
                            regionType: regionType,
                            src: op
                        });
                    }
                    return dimensionsVessel[dId];
                }

                function createSubVessel(dimensionId){
                    if (!dimensionsVessel[dimensionId]) {
                        dimensionsVessel[dimensionId] = BI.createWidget({
                            type: "bi.layout"
                        });
                        self.addSubVessel(dimensionId, dimensionsVessel[dimensionId]);
                    }
                }

            }
        });

        this.dimensionsManager.on(BI.TreeDimensionsManager.EVENT_CHANGE, function () {
            var values = this.getValue();
            self.model.set(values);
        });


        return this.dimensionsManager;
    },

    _createCombo: function () {
        var self = this;
        this.treeList = BI.createWidget({
            type: "bi.select_tree_data_list",
            wId: this.model.get("id")
        });
        this.treeList.on(BI.SelectTreeDataList.EVENT_CHANGE, function () {
            self.model.set("value", self.treeList.getValue());
        });
        return this.treeList;
    },

    splice: function (old, key1, key2) {
        if (key1 === "dimensions") {
            this.dimensionsManager.populate();
            this.treeList.populate();
        }
    },


    change: function (changed, prev) {
        if (BI.has(changed, "dimensions")) {
            this.treeList.populate();
        }
        if (BI.has(changed, "dimensions") || BI.has(changed, "view")) {
            this.dimensionsManager.populate();
            this._refreshDimensions();
        }
        if (BI.has(changed, "value")) {
            this.treeList.populate();
        }
        if (BI.has(changed, "dimensions")) {
            this._checkDataBind();
        }
    },

    _checkDataBind: function () {
        if(BI.size(this.model.get("dimensions")) > 0){
            this.treeList.setEnable(true);
        }else{
            this.treeList.setEnable(false);
        }
    },


    local: function () {
        if (this.model.has("addDimension")) {
            this.model.get("addDimension");
            return true;
        }
        if (this.model.has("sorted")) {
            this.model.get("sorted");
            return true;
        }
        return false;
    },

    _refreshDimensions: function () {
        var self = this;
        BI.each(self.model.cat("view"), function (regionType, dids) {
            BI.each(dids, function (i, dId) {
                self.skipTo(regionType + "/" + dId, dId, "dimensions." + dId, {}, {force: true});
            });
        });
    },

    refresh: function () {
        this.dimensionsManager.populate();
        this._refreshDimensions();
        this._checkDataBind();
        this.treeList.populate();
    }
});