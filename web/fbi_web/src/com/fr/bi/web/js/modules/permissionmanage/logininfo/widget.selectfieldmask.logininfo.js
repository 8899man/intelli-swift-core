/**
 * Created by Young's on 2016/5/23.
 */
BI.LoginInfoSelectDataWithMask = BI.inherit(BI.Widget, {
    _defaultConfig: function(){
        return BI.extend(BI.LoginInfoSelectDataWithMask.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-login-info-select-data-with-mask"
        })
    },

    _init: function(){
        BI.LoginInfoSelectDataWithMask.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.selectDataPane = BI.createWidget({
            type: "bi.login_info_select_single_field"
        });
        this.selectDataPane.on(BI.LoginInfoSelectSingleField.EVENT_CLICK_ITEM, function(){
            self.fireEvent(BI.LoginInfoSelectDataWithMask.EVENT_CHANGE, arguments);
        });

        var wrapper = BI.createWidget({
            type: "bi.vtape",
            cls: "select-data-wrapper",
            items: [{
                el: this.selectDataPane,
                height: "fill"
            }, {
                el: this._createSelectDataBottom(),
                height: 50
            }],
            width: 240
        });
        this.mask = BI.createWidget({
            type: "bi.absolute",
            items: [{
                el: {
                    type: "bi.default",
                    cls: "select-data-mask"
                },
                top: 0,
                left: 0,
                bottom: 0,
                right: 0
            }]
        });
        BI.createWidget({
            type: "bi.absolute",
            element: this.element,
            items: [{
                el: this.mask,
                top: 0,
                right: 0,
                bottom: 0,
                left: 0
            }, {
                el: wrapper,
                top: 10,
                bottom: 10,
                left: 10
            }]
        })
    },

    _createSelectDataBottom: function(){
        var self = this;
        var cancelButton = BI.createWidget({
            type: "bi.button",
            level: "ignore",
            width: 150,
            height: 30,
            text: BI.i18nText("BI-Basic_Cancel")
        });
        cancelButton.on(BI.Button.EVENT_CHANGE, function(){
            self.fireEvent(BI.LoginInfoSelectDataWithMask.EVENT_VALUE_CANCEL);
        });
        return BI.createWidget({
            type: "bi.horizontal_auto",
            cls: "select-data-button-group",
            items: [cancelButton],
            vgap: 10
        })
    },

    destroy: function(){
        this.selectDataPane.destroy();
    }
});
BI.LoginInfoSelectDataWithMask.EVENT_CHANGE = "EVENT_CHANGE";
BI.LoginInfoSelectDataWithMask.EVENT_VALUE_CANCEL = "EVENT_VALUE_CANCEL";
$.shortcut("bi.login_info_select_data_with_mask", BI.LoginInfoSelectDataWithMask);