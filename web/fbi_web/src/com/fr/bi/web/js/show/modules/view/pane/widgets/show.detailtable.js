/**
 * @class BIShow.DetailTableView
 * @extends BI.View
 * @type {*|void|Object}
 */
BIShow.DetailTableView = BI.inherit(BI.View, {

    _constants: {
        SHOW_CHART: 1,
        SHOW_FILTER: 2,
        TOOL_ICON_WIDTH: 20,
        TOOL_ICON_HEIGHT: 20
    },

    _defaultConfig: function () {
        return BI.extend(BIShow.DetailTableView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-dashboard-widget"
        })
    },

    _init: function () {
        BIShow.DetailTableView.superclass._init.apply(this, arguments);
        var self = this, wId = this.model.get("id");
        BI.Broadcasts.on(BICst.BROADCAST.REFRESH_PREFIX + wId, function () {
            self._refreshTableAndFilter();
        });
        BI.Broadcasts.on(BICst.BROADCAST.LINKAGE_PREFIX + wId, function (dId, v) {
            // var clicked = self.model.get("clicked") || {};
            // var allFromIds = BI.Utils.getAllLinkageFromIdsByID(BI.Utils.getWidgetIDByDimensionID(dId));
            // //这条链上所有的其他clicked都应当被清掉
            // BI.each(clicked, function (cid, click) {
            //     if (allFromIds.contains(cid)) {
            //         delete clicked[cid];
            //     }
            // });
            // if (BI.isNull(v)) {
            //     delete clicked[dId];
            // } else {
            //     clicked[dId] = v;
            // }

            // 2016.12.1 young 都清除掉，每次都是往上找到所有的联动条件
            var clicked = BI.Utils.getLinkageValuesByID(BI.Utils.getWidgetIDByDimensionID(dId));
            if (BI.isNotNull(v)) {
                clicked[dId] = v;
            }
            self.model.set("clicked", clicked);
        });
        BI.Broadcasts.on(BICst.BROADCAST.RESET_PREFIX + wId, function () {
            self.model.set("clicked", {});
        });
    },


    _render: function (vessel) {
        var self = this;
        this._buildWidgetTitle();
        this._createTools();
        this.table = BI.createWidget({
            type: "bi.detail_table",
            wId: this.model.get("id"),
            status: BICst.WIDGET_STATUS.SHOW
        });
        this.tablePopulate = BI.debounce(BI.bind(this.table.populate, this.table), 0);
        this.table.on(BI.DetailTable.EVENT_CHANGE, function (ob) {
            self.model.set(ob);
        });

        this.widget = BI.createWidget({
            type: "bi.absolute",
            element: vessel,
            items: [{
                el: this.tools,
                top: 0,
                right: 10
            }, {
                el: this.titleWrapper,
                left: 0,
                top: 0,
                right: 0
            }, {
                el: this.table,
                left: 10,
                right: 10,
                top: 50,
                bottom: 10
            }]
        });
        this.widget.element.hover(function () {
            self.tools.setVisible(true);
        }, function () {
            if (!self.widget.element.parent().parent().parent().hasClass("selected")) {
                self.tools.setVisible(false);
            }
        });
    },

    _buildWidgetTitle: function () {
        var self = this;
        var id = this.model.get("id");
        if (!this.title) {
            this.title = BI.createWidget({
                type: "bi.shelter_editor",
                cls: BI.Utils.getWSNamePosByID(id) === BICst.DASHBOARD_WIDGET_NAME_POS_CENTER ?
                    "dashboard-title-center" : "dashboard-title-left",
                value: BI.Utils.getWidgetNameByID(id),
                textAlign: "left",
                height: 25,
                allowBlank: false,
                errorText: function (v) {
                    if (BI.isNotNull(v) && BI.trim(v) !== "") {
                        return BI.i18nText("BI-Widget_Name_Can_Not_Repeat");
                    }
                    return BI.i18nText("BI-Widget_Name_Can_Not_Null");
                },
                validationChecker: function (v) {
                    return BI.Utils.checkWidgetNameByID(v, id);
                }
            });
            this.titleWrapper = BI.createWidget({
                type: "bi.absolute",
                height: 35,
                cls: "dashboard-widget-title",
                items: [{
                    el: this.title,
                    left: 10,
                    top: 10,
                    right: 10
                }]
            });
            this.title.on(BI.ShelterEditor.EVENT_CHANGE, function () {
                self.model.set("name", this.getValue());
            });
        } else {
            this.title.setValue(BI.Utils.getWidgetNameByID(id));
        }
    },

    _createTools: function () {
        var self = this, wId = this.model.get("id");

        this.maximize = BI.createWidget({
            type: "bi.maximization_4show",
            wId: wId,
            status: BICst.WIDGET_STATUS.SHOW
        });
        this.maximize.on(BI.Maximization4Show.EVENT_SET, function (widget) {
            self.model.set(widget);
        });

        var expand = BI.createWidget({
            type: "bi.dimension_switch_show",
            wId: wId,
            popupCreator: function () {
                var vessel = BI.createWidget({
                    type: "bi.layout"
                });
                self.addSubVessel("detail", vessel).skipTo("detail", "detail", "detail", {}, {id: wId});
                return vessel;
            }
        });
        // var expand = BI.createWidget({
        //     type: "bi.icon_button",
        //     width: this._constants.TOOL_ICON_WIDTH,
        //     height: this._constants.TOOL_ICON_HEIGHT,
        //     title: BI.i18nText("BI-Detailed_Setting"),
        //     cls: "widget-combo-detail-font dashboard-title-detail"
        // });
        // expand.on(BI.IconButton.EVENT_CHANGE, function () {
        //     self._expandWidget();
        // });

        var filterIcon = BI.createWidget({
            type: "bi.icon_button",
            cls: "widget-tools-filter-font dashboard-title-detail",
            title: BI.i18nText("BI-Show_Filters"),
            width: this._constants.TOOL_ICON_WIDTH,
            height: this._constants.TOOL_ICON_HEIGHT
        });
        filterIcon.on(BI.IconButton.EVENT_CHANGE, function () {
            if (BI.isNull(self.filterPane)) {
                self.filterPane = BI.createWidget({
                    type: "bi.widget_filter",
                    wId: self.model.get("id")
                });
                self.filterPane.on(BI.WidgetFilter.EVENT_REMOVE_FILTER, function (widget) {
                    self.model.set(widget);
                });
                BI.createWidget({
                    type: "bi.absolute",
                    element: self.table,
                    items: [{
                        el: self.filterPane,
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0
                    }]
                });
                triangle.setVisible(true);
                return;
            }
            self.filterPane.setVisible(!self.filterPane.isVisible());
            triangle.setVisible(self.filterPane.isVisible());
        });

        var triangle = BI.createWidget({
            type: "bi.layout",
            cls: "filter-triangle-bottom",
            invisible: true
        });

        BI.createWidget({
            type: "bi.absolute",
            element: filterIcon,
            items: [{
                el:{
                    type: "bi.horizontal_auto",
                    height: 10,
                    items:[triangle]
                },
                left: 0,
                right: 0,
                bottom: -9
            }]
        });

        var excel = BI.createWidget({
            type: "bi.icon_button",
            cls: "widget-tools-export-excel-font dashboard-title-detail",
            title: BI.i18nText("BI-Export_As_Excel"),
            width: this._constants.TOOL_ICON_WIDTH,
            height: this._constants.TOOL_ICON_HEIGHT
        });
        excel.on(BI.IconButton.EVENT_CHANGE, function () {
            window.location = FR.servletURL + "?op=fr_bi_dezi&cmd=bi_export_excel&sessionID=" + Data.SharingPool.get("sessionID") + "&name="
                + window.encodeURIComponent(self.model.get("name"));
        });

        this.tools = BI.createWidget({
            type: "bi.left",
            cls: "operator-region",
            items: [this.maximize, filterIcon, expand, excel],
            hgap: 3
        });
        this.tools.setVisible(false);
    },

    _refreshTableAndFilter: function () {
        BI.isNotNull(this.filterPane) && this.filterPane.populate();
        this.tablePopulate();
    },

    _refreshLayout: function () {
        var showTitle = BI.Utils.getWSShowNameByID(this.model.get("id"));
        if (showTitle === false) {
            this.title.setVisible(false);
            this.widget.attr("items")[0].top = 0;
            this.widget.attr("items")[2].top = 20;
        } else {
            this.title.setVisible(true);
            this.widget.attr("items")[0].top = 10;
            this.widget.attr("items")[2].top = 35;
        }
        this.widget.resize();
    },

    _refreshTitlePosition: function () {
        var pos = BI.Utils.getWSNamePosByID(this.model.get("id"));
        var cls = pos === BICst.DASHBOARD_WIDGET_NAME_POS_CENTER ?
            "dashboard-title-center" : "dashboard-title-left";
        this.title.element.removeClass("dashboard-title-left")
            .removeClass("dashboard-title-center").addClass(cls);
    },

    _refreshWidgetTitle: function () {
        var id = this.model.get("id");
        var titleSetting = this.model.get("settings").widgetNameStyle || {};
        this.title.setTextStyle(titleSetting.titleWordStyle || {});

        this.titleWrapper.element.css({"background": this._getBackgroundValue(titleSetting.titleBG)});
    },

    _refreshWidgetBG: function () {
        var widgetBG = this.model.get("settings").widgetBG || {};
        this.element.css({"background": this._getBackgroundValue(widgetBG)})
    },

    _getBackgroundValue: function (bg) {
        if (!bg) {
            return "";
        }
        switch (bg.type) {
            case BICst.BACKGROUND_TYPE.COLOR:
                return bg.value;
            case BICst.BACKGROUND_TYPE.IMAGE:
                return "url(" + FR.servletURL + "?op=fr_bi&cmd=get_uploaded_image&image_id=" + bg["value"] + ")";
        }
        return "";
    },

    _expandWidget: function () {
        var wId = this.model.get("id");
        BIShow.FloatBoxes.open("detail", "detail", {}, this, {
            id: wId
        })
    },


    listenEnd: function () {

    },

    change: function (changed) {
        if (BI.has(changed, "bounds")) {
            this.table.resize();
        }
        if (BI.has(changed, "clicked") || BI.has(changed, "filter_value")) {
            this._refreshTableAndFilter();
            this.maximize.populate();
        }
        if (BI.has(changed, "dimensions") ||
            BI.has(changed, "sort_sequence")) {
            this.tablePopulate();
            this.maximize.populate();
        }
        if (BI.has(changed, "settings") && (changed.settings.widgetNameStyle !== prev.settings.widgetNameStyle)) {
            this._refreshWidgetTitle()
        }
        if (BI.has(changed, "settings") && (changed.settings.widgetBG !== prev.settings.widgetBG)) {
            this._refreshWidgetBG()
        }
    },

    local: function () {
        return false;
    },

    refresh: function () {
        this._buildWidgetTitle();
        this._refreshWidgetTitle();
        this._refreshWidgetBG();
        this.tablePopulate();
        this._refreshLayout();
        this._refreshTitlePosition();
    }
});