/**
 * @class BI.BusinessPackageAdd
 * @extend BI.BusinessPackage
 * @params {Function} handler
 */
BI.BusinessPackageAdd = BI.inherit(BI.IconButton, {
    _defaultConfig: function () {
        var conf = BI.BusinessPackageAdd.superclass._defaultConfig.apply(this, arguments);
        return BI.extend(conf, {
            baseCls: (conf.baseCls || ""),
            height: 140,
            width: 150
        })
    },

    _init: function () {
        var self = this, o = this.options;
        BI.BusinessPackageAdd.superclass._init.apply(this, arguments);
        var addButton = BI.createWidget({
            type: "bi.icon_button",
            cls: function () {
                return o.disabled ? "business-package-add-disable-icon" : "business-package-add-icon"
            },
            iconHeight: 75,
            iconWidth: 90
        });

        addButton.on(BI.IconButton.EVENT_CHANGE, function () {
            self.fireEvent(BI.BusinessPackageAdd.EVENT_CHANGE)
        });

        BI.createWidget({
            type: "bi.absolute",
            element: this.element,
            items: [{
                el: addButton,
                top: 0,
                bottom: 0,
                left: 0,
                right: 0
            }]
        })
    }

});
BI.BusinessPackageAdd.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.business_pack_add", BI.BusinessPackageAdd);