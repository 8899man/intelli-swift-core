/**
 * Created by roy on 16/1/20.
 */
BI.BusinessPackageButton = BI.inherit(BI.BasicButton, {
    _defaultConfig: function () {
        var conf = BI.BusinessPackageButton.superclass._defaultConfig.apply(this, arguments);
        return BI.extend(conf, {
            baseCls: (conf.baseCls || "") + " bi-business-package",
            height: 140,
            width: 150
        })
    },

    _init: function () {
        var o = this.options, self = this;
        BI.BusinessPackageButton.superclass._init.apply(this, arguments);
        this.renameButton = BI.createWidget({
            type: "bi.icon_button",
            cls: "rename-font-package rename-font-style",
            title: BI.i18nText("BI-Table_Rename"),
            iconWidth: 20,
            iconHeight: 20,
            stopPropagation: true,
            invisible: true
        });

        this.renameButton.on(BI.IconButton.EVENT_CHANGE, function () {
            self.packageNameEditor.focus();
            self.packageNameEditor.element.addClass("editor-border");
        });

        this.deleteCombo = BI.createWidget({
            type: "bi.bubble_combo",
            el: {
                type: "bi.icon_button",
                cls: "delete-font-package delete-font-style",
                title: BI.i18nText("BI-Delete_Package"),
                iconWidth: 20,
                iconHeight: 20
            },
            popup: {
                type: "bi.bubble_bar_popup_view",
                buttons: [{
                    value: BI.i18nText(BI.i18nText("BI-Sure")),
                    handler: function () {
                        self.deleteCombo.hideView();
                        self.fireEvent(BI.BusinessPackageButton.EVENT_CLICK_DELETE, self);
                    }
                }, {
                    value: BI.i18nText("BI-Cancel"),
                    level: "ignore",
                    handler: function () {
                        self.deleteCombo.hideView();
                    }
                }],
                el: {
                    type: "bi.vertical_adapt",
                    items: [{
                        type: "bi.label",
                        text: BI.i18nText("BI-Is_Delete_Package"),
                        cls: "package-delete-label",
                        textAlign: "left",
                        width: 300
                    }],
                    width: 300,
                    height: 100,
                    hgap: 20
                },
                maxHeight: 140,
                minWidth: 340
            },
            invisible: true,
            stopPropagation: true
        });

        var packageButton = BI.createWidget({
            type: "bi.center_adapt",
            cls: "business-package-icon",
            items: [{
                el: {
                    type: "bi.icon",
                    width: 90,
                    height: 75
                }
            }]
        });

        packageButton.on(BI.IconButton.EVENT_CHANGE, function () {
            self.fireEvent(BI.BusinessPackageButton.EVENT_CHANGE, self)
        });

        this.packageNameEditor = BI.createWidget({
            type: "bi.shelter_editor",
            height: 25,
            value: o.text,
            textAlign: "center",
            allowBlank: false,
            validationChecker: function (value) {
                var packages = BI.Utils.getAllPackageIDs4Conf();
                return !BI.some(packages, function (i, pID) {
                    if (BI.Utils.getPackageNameByID4Conf(pID) === value && o.value != pID) {
                        return true
                    }
                })
            },
<<<<<<< HEAD
            errorText: function(v){
                if(BI.isEmptyString(v)){
                    return BI.i18nText("BI-Busi_Package_Name_Not_Empty");
                }else{
                    return BI.i18nText("BI-Busi_Package_Name_Not_Repeat")
=======
            allowBlank: false,
            errorText: function(v){
                if(BI.isEmptyString(v)){
                    return BI.i18nText("BI-Value_Cannot_Be_Null");
                }else{
                    return BI.i18nText("BI-Busi_Package_Name_Not_Repeat");
>>>>>>> 67b55d486e769f445942f15883303ca839ffd092
                }
            }
        });

        this.packageNameEditor.on(BI.ShelterEditor.EVENT_CONFIRM, function () {
            self.packageNameEditor.element.removeClass("editor-border");
            var value = self.packageNameEditor.getValue();
            var id = self.attr("value");
            self.fireEvent(BI.BusinessPackageButton.EVENT_EDITOR_CONFIRM, value, id);
        });


        this.tableNumLabel = BI.createWidget({
            type: "bi.label",
            cls: "business-package-table-num-label",
            value: o.table_count
        });

        this.checkboxIcon = BI.createWidget({
            type: "bi.center_adapt",
            cls: o.forceNotSelected === false ? "package-not-selected-font" : "",
            items: [{
                el: {
                    type: "bi.icon",
                    height: 16,
                    width: 16
                }
            }],
            invisible: true,
            height: 16,
            width: 16
        });

        BI.createWidget({
            type: "bi.absolute",
            cls: "bi-business-package-button",
            element: this.element,
            items: [{
                el: packageButton,
                left: 30,
                right: 30,
                top: 30,
                bottom: 30
            }, {
                el: this.packageNameEditor,
                left: 30,
                right: 30,
                top: 110,
                bottom: 0
            }, {
                el: this.deleteCombo,
                right: 0,
                top: 0
            }, {
                el: this.renameButton,
                right: 0,
                top: 30
            }, {
                el: this.tableNumLabel,
                right: 40,
                bottom: 40
            }, {
                el: this.checkboxIcon,
                left: 0,
                top: 0
            }]
        });

        this.element.hover(function () {
            self.hover();
        }, function () {
            self.dishover();
        });

    },

    doClick: function () {
        var o = this.options;
        BI.BusinessPackageButton.superclass.doClick.apply(this, arguments);
        if (o.forceNotSelected) {
            return;
        }
        if (this.isSelected()) {
            this.checkboxIcon.element.removeClass("package-not-selected-font");
            this.checkboxIcon.element.addClass("package-selected-font");
        } else {
            this.checkboxIcon.element.removeClass("package-selected-font");
            this.checkboxIcon.element.addClass("package-not-selected-font");
        }
    },

    setFocus: function () {
        this.packageNameEditor.focus();
    },

    hover: function () {
        BI.BusinessPackageButton.superclass.hover.apply(this, arguments);
        this.checkboxIcon.setVisible(true);
        this.deleteCombo.setVisible(true);
        this.renameButton.setVisible(true);
    },

    dishover: function () {
        BI.BusinessPackageButton.superclass.dishover.apply(this, arguments);
        if (!this.isSelected()) {
            this.checkboxIcon.setVisible(false);
        }
        this.deleteCombo.setVisible(false);
        this.renameButton.setVisible(false);
    },

    doHighLight: function () {
        this.packageNameEditor.doHighLight();
    },

    unHighLight: function () {
        this.packageNameEditor.unHighLight();
    },

    getValue: function () {
        return this.packageNameEditor.getValue();
    }
});
BI.BusinessPackageButton.EVENT_CLICK_DELETE = "EVENT_CLICK_DELETE";
BI.BusinessPackageButton.EVENT_CHANGE = "EVENT_CHANGE";
BI.BusinessPackageButton.EVENT_EDITOR_CONFIRM = "EVENT_EDITOR_CONFIRM";
$.shortcut("bi.business_package_button", BI.BusinessPackageButton);