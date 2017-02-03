/**
 * Created by GameJian on 2016/3/14.
 */
BIDezi.WebWidgetView = BI.inherit(BI.View, {
    _defaultConfig: function () {
        return BI.extend(BIDezi.WebWidgetView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-dashboard-widget"
        })
    },

    _init: function () {
        BIDezi.WebWidgetView.superclass._init.apply(this, arguments);
        var self = this;
        this._broadcasts = [];
        this._broadcasts.push(BI.Broadcasts.on(BICst.BROADCAST.WIDGET_SELECTED_PREFIX, function () {
            if (!self.web.element.parent().parent().parent().hasClass("selected")) {
                self.web.setToolbarVisible(false);
            }
        }));
    },

    change: function () {

    },

    _render: function (vessel) {
        var self = this;
        this.web = BI.createWidget({
            type: "bi.web_page",
            element: vessel
        });

        this.web.on(BI.WebPage.EVENT_DESTROY, function () {
            BI.Msg.confirm("", BI.i18nText("BI-Sure_Delete_Current_Component"), function (v) {
                if (v === true) {
                    self.model.destroy();
                }
            });
        });

        this.web.on(BI.WebPage.EVENT_VALUE_CHANGE, function () {
            self.model.set("url", self.web.getValue())
        });

        this.web.element.hover(function () {
            self.web.setToolbarVisible(true);
        }, function () {
            if (!self.web.element.parent().parent().parent().hasClass("selected") && !self.web.isSelected()) {
                self.web.setToolbarVisible(false);
            }
        });
    },

    local: function () {
        if (this.model.has("expand")) {
            this.model.get("expand");
            return true;
        }
        if (this.model.has("layout")) {
            this.model.get("layout");
            return true;
        }
        return false;
    },

    refresh: function () {
        var self = this;
        BI.delay(function () {
            self.web.setValue(self.model.get("url"))
        }, 0);
    },

    destroyed: function () {
        BI.each(this._broadcasts, function (I, removeBroadcast) {
            removeBroadcast();
        });
        this._broadcasts = [];
    }
});