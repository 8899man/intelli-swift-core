/**
 * @class BI.LoadingCancelMask
 * @extend BI.Widget
 * 带有取消按钮的正在加载mask
 */
BI.LoadingCancelMask = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.LoadingCancelMask.superclass._defaultConfig.apply(this, arguments), {})
    },

    _init: function () {
        BI.LoadingCancelMask.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        var cancelButton = BI.createWidget({
            type: "bi.button",
            level: "ignore",
            width: 100,
            height: 30,
            text: BI.i18nText("BI-Cancel")
        });
        cancelButton.on(BI.Button.EVENT_CHANGE, function () {
            self.fireEvent(BI.LoadingCancelMask.EVENT_VALUE_CANCEL);
            self.destroy();
        });
        var mask = BI.Maskers.create(this.getName(), o.masker);
        BI.createWidget({
            type: "bi.absolute",
            element: mask,
            items: [{
                el: {
                    type: "bi.layout",
                    cls: "bi-loading-main-background"
                },
                top: 0,
                left: 0,
                bottom: 0,
                right: 0
            }, {
                el: {
                    type: "bi.center_adapt",
                    cls: "bi-loading-mask-content",
                    items: [{
                        el: {
                            type: "bi.vertical",
                            items: [{
                                type: "bi.center_adapt",
                                cls: "loading-bar-icon",
                                items: [{
                                    type: "bi.icon",
                                    width: 208,
                                    height: 30
                                }]
                            }, {
                                type: "bi.label",
                                cls: "loading-bar-label",
                                text: o.text,
                                height: 30
                            }, {
                                type: "bi.center_adapt",
                                items: [cancelButton]
                            }],
                            vgap: 10
                        }
                    }]
                },
                top: 0,
                left: 0,
                bottom: 0,
                right: 0
            }]
        });
        BI.Maskers.show(this.getName());
        BI.nextTick(function () {
            BI.Maskers.show(self.getName());
        });
    },

    destroy: function () {
        BI.Maskers.remove(this.getName());
    }
});
BI.LoadingCancelMask.EVENT_VALUE_CANCEL = "EVENT_VALUE_CANCEL";
$.shortcut("bi.loading_cancel_mask", BI.LoadingCancelMask);