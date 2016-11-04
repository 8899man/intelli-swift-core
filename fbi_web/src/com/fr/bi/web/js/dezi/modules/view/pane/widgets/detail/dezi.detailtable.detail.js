/**
 * @class BIDezi.DetailTableDetailView
 * @extends BI.View
 * 明细表的详细设置————诡异的命名
 */
BIDezi.DetailTableDetailView = BI.inherit(BI.View, {

    constants: {
        DETAIL_NORTH_HEIGHT: 40,
        DETAIL_TAB_HEIGHT: 40,
        DETAIL_WEST_WIDTH: 280,
        DETAIL_DATA_STYLE_HEIGHT: 240,
        DETAIL_GAP_NORMAL: 10,
        DETAIL_PANE_HORIZONTAL_GAP: 10

    },

    _defaultConfig: function () {
        return BI.extend(BIDezi.DetailTableDetailView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-widget-attribute-setter"
        })
    },

    _init: function () {
        BIDezi.DetailTableDetailView.superclass._init.apply(this, arguments);
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
            height: 22,
            width: 400,
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
                    type: "bi.detail_detail_table_select_data",
                    cls: "widget-select-data-pane",
                    wId: this.model.get("id")
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
        var self = this;
        var table = this._createTable();
        var tab = BI.createWidget({
            type: "bi.data_style_tab",
            cls: "widget-top-wrapper",
            wId: this.model.get("id"),
            cardCreator: BI.bind(self._createTabs, this)
        });

        return BI.createWidget({
            type: "bi.absolute",
            cls: "widget-attr-center",
            items: [{
                el: {
                    type: "bi.border",
                    items: {
                        north: {
                            el: tab,
                            height: this.constants.DETAIL_DATA_STYLE_HEIGHT,
                            bottom: this.constants.DETAIL_GAP_NORMAL
                        },
                        center: table
                    }
                },
                left: 0,
                right: this.constants.DETAIL_PANE_HORIZONTAL_GAP,
                top: this.constants.DETAIL_GAP_NORMAL,
                bottom: this.constants.DETAIL_GAP_NORMAL
            }]
        });
    },


    _createTypeAndData: function () {
        var self = this;
        var dimensionsVessel = {};
        this.dimensionsManager = BI.createWidget({
            type: "bi.dimensions_manager_control",
            wId: this.model.get("id"),
            dimensionCreator: function (dId, regionType, op) {
                var relationItem = op.relationItem;
                if (BI.isNotNull(relationItem)) {
                    self.model.set("setRelation", {
                        dId: dId,
                        relationItem: op.relationItem
                    });
                }

                if (!dimensionsVessel[dId]) {
                    dimensionsVessel[dId] = BI.createWidget({
                        type: "bi.layout"
                    });
                    var dimensions = self.model.cat("dimensions");
                    if (!BI.has(dimensions, dId)) {
                        self.model.set("addDimension", {
                            dId: dId,
                            regionType: regionType,
                            src: op
                        });
                    }
                }
                self.addSubVessel(dId, dimensionsVessel[dId]).skipTo(regionType + "/" + dId, dId, "dimensions." + dId);
                return dimensionsVessel[dId];

            }
        });

        this.dimensionsManager.on(BI.DimensionsManagerControl.EVENT_CHANGE, function () {
            var values = this.getValue();
            self.model.set(values);
            this.populate();
        });


        return this.dimensionsManager;
    },


    /**
     * 图表样式设置
     * @returns {*}
     * @private
     */
    _createStyle: function () {
        var self = this;
        this.chartSetting = BI.createWidget({
            type: "bi.chart_setting",
            chartType: this.model.get("type"),
            settings: this.model.get("settings"),
            wId: this.model.get("id")
        });
        this.chartSetting.populate();
        this.chartSetting.on(BI.ChartSetting.EVENT_CHANGE, function (v) {
            var name = v.widget_title;
            self.model.set({"settings": BI.extend(self.model.get("settings"), v), "name": name});
        });
        return this.chartSetting;
    },


    splice: function (old, key1, key2) {
        if (key1 === "dimensions") {
            this.dimensionsManager.populate();
        }
    },

    _createTabs: function (v) {
        switch (v) {
            case BICst.DETAIL_TAB_TYPE_DATA:
                return this._createTypeAndData();
            case BICst.DETAIL_TAB_STYLE:
                return this._createStyle();
        }
    },

    _createTable: function () {
        var self = this;
        var table = BI.createWidget({
            type: "bi.detail_table",
            cls: "widget-center-wrapper",
            wId: this.model.get("id"),
            status: BICst.WIDGET_STATUS.DETAIL
        });
        table.on(BI.DetailTable.EVENT_CHANGE, function (ob) {
            self.model.set(ob);
        });
        this.tablePopulate = BI.debounce(BI.bind(table.populate, table), 0);
        return table;
    },


    change: function (changed, prev) {
        if (BI.has(changed, "dimensions") ||
            BI.has(changed, "sort_sequence") ||
            BI.has(changed, "view") ||
            BI.has(changed, "filter_value") ||
            (BI.has(changed, "target_relation"))) {
            this.tablePopulate();
        }
        if (BI.has(changed, "settings")) {
            this.tablePopulate();
            this.title.setText(changed.settings.widget_title);
        }
    },


    local: function () {
        if (this.model.has("addDimension")) {
            this.model.get("addDimension");
            return true;
        }
        if (this.model.has("addCalculate")) {
            this.model.get("addCalculate");
            return true;
        }
        if (this.model.has("sorted")) {
            this.model.get("sorted");
            return true;
        }
        return false;
    },

    refresh: function () {
        var self = this;
        this.dimensionsManager.populate();
        this.tablePopulate();
    }
});