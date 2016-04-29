AdaptiveArrangementView = BI.inherit(BI.View, {
    _defaultConfig: function () {
        return BI.extend(AdaptiveArrangementView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-mvc-adaptive-arrangement bi-mvc-layout"
        })
    },

    _init: function () {
        AdaptiveArrangementView.superclass._init.apply(this, arguments);
    },

    _createItem: function () {
        var self = this;
        var id = BI.UUID();
        var item = BI.createWidget({
            type: "bi.text_button",
            id: id,
            cls: "layout-bg" + BI.random(1, 8),
            handler: function () {
                self.arrangement.deleteRegion(id);
            }
        });
        item.setValue(item.attr("id"));
        return item;
    },

    _render: function (vessel) {
        var self = this;
        this.arrangement = BI.createWidget({
            type: "bi.adaptive_arrangement",
            cls: "mvc-border",
            width: 800,
            height: 400,
            items: []
        });
        var drag = BI.createWidget({
            type: "bi.label",
            cls: "mvc-border",
            width: 50,
            height: 25,
            text: "drag me"
        });

        drag.element.draggable({
            revert: true,
            cursorAt: {left: 0, top: 0},
            drag: function (e, ui) {
                self.arrangement.setPosition({
                    left: ui.position.left,
                    top: ui.position.top
                })
            },
            stop: function (e, ui) {
                self.arrangement.addRegion({
                    el: self._createItem()
                });
            },
            helper: function (e) {
                var helper = self.arrangement.getHelper();
                return helper.element;
            }
        });

        BI.createWidget({
            type: "bi.absolute",
            element: vessel,
            items: [{
                el: drag,
                left: 30,
                top: 450
            }, {
                el: this.arrangement,
                left: 30,
                top: 30
            }, {
                el: {
                    type: "bi.button",
                    text: "回撤",
                    height: 25,
                    handler: function () {
                        //self.arrangement.revoke();
                    }
                },
                left: 130,
                top: 450
            }, {
                el: {
                    type: "bi.button",
                    text: "getAllRegions",
                    height: 25,
                    handler: function () {
                        var items = [];
                        BI.each(self.arrangement.getAllRegions(), function (i, region) {
                            items.push({
                                id: region.id,
                                left: region.left,
                                top: region.top,
                                width: region.width,
                                height: region.height
                            });
                        });
                        BI.Msg.toast(JSON.stringify(items));
                    }
                },
                left: 230,
                top: 450
            }, {
                el: {
                    type: "bi.button",
                    text: "relayout",
                    height: 25,
                    handler: function () {
                        self.arrangement.relayout();
                    }
                },
                left: 330,
                top: 450
            }]
        });
    }
});

AdaptiveArrangementModel = BI.inherit(BI.Model, {});