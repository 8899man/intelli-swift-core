/**
 * Created by Young's on 2016/5/5.
 */
BIShow.ResetView = BI.inherit(BI.View, {
    _defaultConfig: function () {
        return BI.extend(BIShow.ResetView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-dashboard-widget"
        })
    },

    _init: function () {
        BIShow.ResetView.superclass._init.apply(this, arguments);
    },

    _render: function (veseel) {
        var self = this;
        var resetButton = BI.createWidget({
            type: "bi.text_button",
            text: BI.i18nText("BI-Basic_Reset"),
            forceCenter: true,
            cls: "query-or-reset-button"
        });
        resetButton.on(BI.Button.EVENT_CHANGE, function () {
            self._resetAllControlValues();
        });
        BI.createWidget({
            type: "bi.absolute",
            element: veseel,
            items: [{
                el: resetButton,
                left: 0,
                right: 0,
                top: 0,
                bottom: 0
            }]
        });
    },

    _resetAllControlValues: function () {
        Data.SharingPool.put("control_filters", []);
        BI.each(BI.Utils.getAllWidgetIDs(), function (i, wid) {
            BI.Broadcasts.send(BICst.BROADCAST.RESET_PREFIX + wid);
        });
        BI.Utils.broadcastAllWidgets2Refresh(true);
    }
});