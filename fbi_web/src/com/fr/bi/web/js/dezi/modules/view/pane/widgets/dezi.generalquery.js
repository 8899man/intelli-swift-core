/**
 * Created by Young's on 2016/5/9.
 */
BIDezi.GeneralQueryView = BI.inherit(BI.View, {
    _defaultConfig: function () {
        return BI.extend(BIDezi.GeneralQueryView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-dashboard-widget bi-control-widget"
        })
    },

    _init: function () {
        BIDezi.GeneralQueryView.superclass._init.apply(this, arguments);
        var self = this;
        BI.Broadcasts.on(BICst.BROADCAST.RESET_PREFIX + this.model.get("id"), function () {
            self._resetValue();
        });

        //全局样式
        BI.Broadcasts.on(BICst.BROADCAST.GLOBAL_STYLE_PREFIX, function (globalStyle) {
            self._refreshGlobalStyle(globalStyle);
        });
    },

    _render: function (vessel) {
        var self = this;
        this._buildWidgetTitle();
        this._createTools();

        this.filter = BI.createWidget({
            type: "bi.general_query_filter"
        });
        this.filter.on(BI.GeneralQueryFilter.EVENT_CHANGE, function () {
            self.model.set("value", this.getValue());
        });

        this.widget = BI.createWidget({
            type: "bi.absolute",
            element: vessel,
            items: [{
                el: this.titleWrapper,
                top: 0,
                left: 0,
                right: 0
            }, {
                el: this.filter,
                top: 10,
                left: 10,
                right: 10,
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
            if (!self.widget.element.parent().parent().parent().hasClass("selected")) {
                self.tools.setVisible(false);
            }
        });
        BI.Broadcasts.on(BICst.BROADCAST.WIDGET_SELECTED_PREFIX, function () {
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
            this.title.on(BI.ShelterEditor.EVENT_CONFIRM, function () {
                self.model.set("name", this.getValue());
            });
        } else {
            this.title.setValue(BI.Utils.getWidgetNameByID(this.model.get("id")));
        }
    },

    _createTools: function () {
        var self = this;
        this.tools = BI.createWidget({
            type: "bi.widget_combo",
            cls: "operator-region",
            wId: this.model.get("id")
        });
        this.tools.on(BI.WidgetCombo.EVENT_CHANGE, function (type) {
            switch (type) {
                case BICst.DASHBOARD_CONTROL_CLEAR:
                    self._resetValue();
                    break;
                case BICst.DASHBOARD_WIDGET_RENAME:
                    self.title.focus();
                    break;
                case BICst.DASHBOARD_WIDGET_COPY:
                    self.model.copy();
                    break;
                case BICst.DASHBOARD_WIDGET_DELETE:
                    BI.Msg.confirm(BI.i18nText("BI-Prompt"), BI.i18nText("BI-Sure_Delete") + self.model.get("name") + "?", function (v) {
                        if (v === true) {
                            self.model.destroy();
                            BI.Utils.broadcastAllWidgets2Refresh();
                        }
                    });
                    break;
            }
        });
        this.tools.setVisible(false);
    },

    _refreshGlobalStyle: function () {
        this._refreshTitlePosition();
    },

    _refreshTitlePosition: function () {
        var pos = BI.Utils.getGSNamePos();
        var cls = pos === BICst.DASHBOARD_WIDGET_NAME_POS_CENTER ?
            "dashboard-title-center" : "dashboard-title-left";
        this.title.element.removeClass("dashboard-title-left")
            .removeClass("dashboard-title-center").addClass(cls);
    },

    _resetValue: function () {
        var value = this.model.get("value");

        function resetValue(filters) {
            BI.each(filters, function (i, filter) {
                var fType = filter.filter_type;
                var fValue = filter.filter_value;
                if (fType === BICst.FILTER_TYPE.AND || fType === BICst.FILTER_TYPE.OR) {
                    resetValue(fValue);
                    return;
                }
                delete filter.filter_value;
            });
        }

        resetValue(value);
        this.model.set("value", value);
        this.refresh();
    },

    local: function () {
        if (this.model.has("expand")) {
            this.model.get("expand");
            return true;
        }
        return false;
    },

    change: function (changed, prev, context, options) {
        if (BI.has(changed, "value")) {
            BI.Utils.broadcastAllWidgets2Refresh();
        }
    },

    refresh: function () {
        this._buildWidgetTitle();
        this._refreshGlobalStyle();
        this.filter.populate(this.model.get("value"));
    }
});