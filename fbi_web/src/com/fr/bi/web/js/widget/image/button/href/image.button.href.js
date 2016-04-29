/**
 * Created by GameJian on 2016/1/28.
 */
BI.ImageButtonHref = BI.inherit(BI.Single, {

    _defaultConfig: function () {
        return BI.extend(BI.ImageButtonHref.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-image-button-href",
            title: BI.i18nText("BI-Add_Href")
        })
    },

    _init: function () {
        BI.ImageButtonHref.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.trigger = BI.createWidget({
            type: "bi.icon_button",
            cls: "img-href-font",
            title: o.title,
            height: 32,
            width: 32
        });

        this.input = BI.createWidget({
            type: "bi.href_editor",
            width: 255,
            height: 30
        });

        this.combo = BI.createWidget({
            type: "bi.combo",
            element: this.element,
            direction: "bottom",
            adjustYOffset: 3,
            offsetStyle: "right",
            el: this.trigger,
            popup: {
                el: this.input,
                stopPropagation: false,
                minWidth: 255
            }
        });

        this.combo.on(BI.Combo.EVENT_AFTER_POPUPVIEW, function (){
            self.input.focus()
        });

        this.combo.on(BI.Combo.EVENT_BEFORE_HIDEVIEW, function (){
            self.fireEvent(BI.ImageButtonHref.EVENT_CHANGE , arguments)
        })
    },
    
    getValue: function() {
        return this.input.getValue().toString()
    },

    setValue: function(url) {
        this.input.setValue(url)
    }
});
BI.ImageButtonHref.EVENT_CHANGE = "BI.ImageButtonHref.EVENT_CHANGE";
$.shortcut("bi.image_button_href", BI.ImageButtonHref);