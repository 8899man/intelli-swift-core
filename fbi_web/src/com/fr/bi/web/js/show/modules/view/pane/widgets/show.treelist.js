/**
 * Created by zcf on 2016/12/21.
 */
BIShow.TreeListView = BI.inherit(BI.View, {

    _constants: {
        TOOL_ICON_WIDTH: 20,
        TOOL_ICON_HEIGHT: 20
    },

    _defaultConfig: function () {
        return BI.extend(BIShow.TreeListView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-dashboard-widget bi-control-widget"
        })
    },

    _init: function () {
        BIShow.TreeListView.superclass._init.apply(this, arguments);
        var self = this;
        BI.Broadcasts.on(BICst.BROADCAST.RESET_PREFIX + this.model.get("id"), function () {
            self._resetValue();
        });

        BI.Broadcasts.on(BICst.BROADCAST.REFRESH_PREFIX + this.model.get("id"), function (wId) {
            self.treeList.populate();
        });
    },

    _render: function (vessel) {
        var self = this;
        this._buildWidgetTitle();
        this._createTools();

        this.treeList = BI.createWidget({
            type: "bi.select_tree_data_list",
            wId: this.model.get("id")
        });
        this.treeList.on(BI.SelectTreeDataList.EVENT_CHANGE, function () {
            self.model.set("value", self.treeList.getValue());
        });


        this.widget = BI.createWidget({
            type: "bi.absolute",
            element: vessel,
            items: [{
                el: this.titleWrapper,
                left: 0,
                top: 0,
                right: 0
            }, {
                el: this.treeList,
                top: 10,
                right: 10,
                left: 10,
                bottom: 10
            }, {
                el: this.tools,
                top: 0,
                right: 10
            }]
        });
        this.widget.element.hover(function () {
            self.tools.setVisible(true);
        }, function () {
            if (!self.widget.element.parent().parent().hasClass("selected")) {
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
                cls: "dashboard-title-left",
                value: BI.Utils.getWidgetNameByID(id),
                textAlign: "left",
                height: 25,
                allowBlank: false,
                errorText: BI.i18nText("BI-Control_Widget_Name_Can_Not_Repeat"),
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
            this.title.setValue(BI.Utils.getWidgetNameByID(this.model.get("id")));
        }
    },

    _refreshTitlePosition: function () {
        var pos = BI.Utils.getGSNamePos();
        var cls = pos === BICst.DASHBOARD_WIDGET_NAME_POS_CENTER ?
            "dashboard-title-center" : "dashboard-title-left";
        this.title.element.removeClass("dashboard-title-left")
            .removeClass("dashboard-title-center").addClass(cls);
    },

    _createTools: function () {
        var self = this;
        this.tools = BI.createWidget({
            type: "bi.icon_button",
            cls: "widget-tools-clear-font dashboard-title-detail",
            title: BI.i18nText("BI-Clear_Selected_Value"),
            width: this._constants.TOOL_ICON_WIDTH,
            height: this._constants.TOOL_ICON_HEIGHT
        });
        this.tools.on(BI.IconButton.EVENT_CHANGE, function () {
            self._resetValue();
        });
        this.tools.setVisible(false);
    },

    _refreshLayout: function () {
        var bounds = this.model.get("bounds");
        var height = bounds.height, width = bounds.width;
        var widgetName = this.model.get("name");
        var minComboWidth = 70;     //默认combo的最小宽度
        var minNameWidth = 30;      //默认editor的最小宽度
        var nameWidth = BI.DOM.getTextSizeWidth(widgetName, 16);
        // width =  5 + 10 + (4 + nameWidth + 4) + 10 + comboWidth + 10 + 5
        if (height < 100) {
            this.widget.attr("items")[1].top = 10;
            if (width < minComboWidth + minNameWidth + 48) {
                this.treeList.setVisible(false);
                this.widget.attr("items")[0].right = 10;
            } else if (width < nameWidth + minComboWidth + 48) {
                this.treeList.setVisible(true);
                this.widget.attr("items")[0].right = minComboWidth + 25;
                this.widget.attr("items")[1].left = width - 15 - minComboWidth;
            } else {
                this.treeList.setVisible(true);
                this.widget.attr("items")[0].right = width - 43 - nameWidth;
                this.widget.attr("items")[1].left = 33 + nameWidth;
            }
        } else {
            this.widget.attr("items")[0].right = 10;
            this.widget.attr("items")[1].top = 35;
            this.widget.attr("items")[1].left = 10;
        }
        this.widget.resize();
    },

    _resetValue: function () {
        this.model.set("value", {});
        this.refresh();
    },

    change: function (changed) {
        if (BI.has(changed, "bounds")) {
            this._refreshLayout();
            this.treeList.resize();
        }
        if (BI.has(changed, "value") || BI.has(changed, "dimensions")) {
            BI.Utils.broadcastAllWidgets2Refresh();
        }
    },

    local: function () {
        return false;
    },

    refresh: function () {
        this._refreshLayout();
        this._buildWidgetTitle();
        this._refreshTitlePosition();
        this.treeList.populate();
    }
});