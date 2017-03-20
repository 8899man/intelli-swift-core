/**
 * 模板文件夹控件
 *
 * @class BI.FolderListViewItem
 * @extends BI.Single
 */
BI.FolderListViewItem = BI.inherit(BI.BasicButton, {

    constants: {
        minGap: 10,
        maxGap: 20
    },
    _defaultConfig: function () {
        return BI.extend(BI.FolderListViewItem.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-template-manager-folder-item bi-list-item cursor-pointer",
            height: 40,
            disableSelected: true,
            validationChecker: BI.emptyFn,
            id: null,
            value: null
        })
    },

    _init: function () {
        BI.FolderListViewItem.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.checkbox = BI.createWidget({
            type: "bi.checkbox",
            stopPropagation: true
        });
        this.checkbox.on(BI.Controller.EVENT_CHANGE, function () {
            arguments[2] = self;
            self.fireEvent(BI.Controller.EVENT_CHANGE, arguments);
        });
        this.editor = BI.createWidget({
            type: "bi.shelter_editor",
            width: 230,
            height: o.height,
            value: o.text,
            title: o.text,
            allowBlank: false,
            validationChecker: function (v) {
                return o.validationChecker(v);
            }
        });
        this.editor.on(BI.ShelterEditor.EVENT_ERROR, function () {
            self.editor.setErrorText(BI.i18nText("BI-File_Name_Cannot_Be_Repeated"));
        });
        this.editor.on(BI.ShelterEditor.EVENT_CONFIRM, function () {
            o.onRenameFolder(this.getValue());
            this.setTitle(this.getValue());
        });

        var renameIcon = BI.createWidget({
            type: "bi.icon_button",
            cls: 'report-rename-font template-item-icon',
            title: BI.i18nText("BI-Basic_Rename"),
            invisible: true,
            stopPropagation: true
        });
        renameIcon.on(BI.IconButton.EVENT_CHANGE, function () {
            BI.requestAsync("fr_bi", "check_report_edit", {id: o.id}, function (res) {
                if (BI.isNotNull(res.result) && res.result.length > 0) {
                    BI.Msg.toast(BI.i18nText("BI-Folder_Editing_Cannot_Rename", res.result), "warning");
                } else {
                    self.editor.focus();
                }
            });
        });

        var deleteCombo = BI.createWidget({
            type: "bi.bubble_combo",
            el: {
                type: "bi.icon_button",
                cls: 'remove-report-font template-item-icon',
                title: BI.i18nText("BI-Basic_Remove")
            },
            popup: {
                type: "bi.bubble_bar_popup_view",
                buttons: [{
                    value: BI.i18nText(BI.i18nText("BI-Basic_Sure")),
                    handler: function () {
                        BI.requestAsync("fr_bi", "check_report_edit", {id: o.id}, function (res) {
                            if (BI.isNotNull(res.result) && res.result.length > 0) {
                                BI.Msg.toast(BI.i18nText("BI-Folder_Editing_Cannot_Remove", res.result), "warning");
                            } else {
                                o.onDeleteFolder.apply(self, arguments);
                            }
                        });
                    }
                }, {
                    value: BI.i18nText("BI-Basic_Cancel"),
                    level: "ignore",
                    handler: function () {
                        deleteCombo.hideView();
                    }
                }],
                el: {
                    type: "bi.vertical_adapt",
                    items: [{
                        type: "bi.label",
                        text: BI.i18nText("BI-Confirm_Delete_Folder"),
                        cls: "template-folder-label",
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

        var timeText = BI.createWidget({
            type: 'bi.label',
            cls: "template-folder-item-date",
            text: FR.date2Str(new Date(o.lastModify), "yyyy.MM.dd HH:mm:ss")
        });
        this.tree = new BI.Tree();
        this.tree.initTree([{
            id: o.id,
            children: o.children
        }]);
        this.selectValue = [];

        this.element.hover(function () {
            renameIcon.setVisible(true);
            deleteCombo.setVisible(true);
        }, function () {
            renameIcon.setVisible(false);
            deleteCombo.setVisible(false);
        });

        this.blankSpace = BI.createWidget({
            type: "bi.text_button",
            text: "",
            height: 40
        });

        BI.createWidget({
            type: "bi.htape",
            element: this.element,
            items: [{
                el: {
                    type: "bi.center_adapt",
                    items: [this.checkbox],
                    height: 40,
                    width: 50
                },
                width: 50
            }, {
                el: {
                    type: "bi.center_adapt",
                    items: [{
                        type: "bi.icon_button",
                        cls: "folder-font",
                        iconWidth: 16,
                        iconHeight: 16
                    }],
                    height: 40,
                    width: 40
                },
                width: 40
            }, {
                el: this.editor,
                width: 230
            }, {
                el: this.blankSpace,
                width: 50
            }, {
                type: "bi.vertical_adapt",
                hgap: 20,
                items: [renameIcon, deleteCombo]
            }, {
                el: {
                    type: "bi.left_right_vertical_adapt",
                    items: {
                        right: [timeText]
                    },
                    llgap: 20,
                    rrgap: 20
                },
                width: 320
            }]
        });
    },

    doClick: function () {
        var self = this, o = this.options;
        BI.FolderListViewItem.superclass.doClick.apply(this, arguments);
        o.onClickItem.apply(self, arguments);
    },

    isSelected: function () {
        return this.checkbox.isSelected();
    },

    setSelected: function (v) {
        this.checkbox.setSelected(v);
    },

    focus: function () {
        this.editor.focus();
    },

    getText: function () {
        return this.editor.getValue();
    },

    destroy: function () {
        BI.FolderListViewItem.superclass.destroy.apply(this, arguments);
    }
});
$.shortcut("bi.folder_list_view_item", BI.FolderListViewItem);