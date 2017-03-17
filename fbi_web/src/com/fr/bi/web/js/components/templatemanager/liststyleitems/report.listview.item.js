/**
 * 模板文件控件
 *
 * Created by GUY on 2016/1/29.
 *
 * @class BI.ReportListViewItem
 * @extends BI.Single
 */
BI.ReportListViewItem = BI.inherit(BI.BasicButton, {

    _defaultConfig: function () {
        return BI.extend(BI.ReportListViewItem.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-template-manager-file-item bi-list-item cursor-pointer",
            height: 40,
            disableSelected: true,
            validationChecker: BI.emptyFn,
            id: null,
            value: null,
            buildUrl: null
        })
    },

    _init: function () {
        BI.ReportListViewItem.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.status = o.status;
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
            width: 200,
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
            o.onRenameReport(this.getValue());
            this.setTitle(this.getValue());
        });

        if (o.isAdmin === false) {
            var markCls = "report-apply-hangout-ing-font";
            this.hangout = BI.createWidget({
                type: "bi.icon_change_button",
                cls: "template-item-icon",
                title: function () {
                    if (self.status === BICst.REPORT_STATUS.NORMAL) {
                        return BI.i18nText("BI-Report_Hangout_Applying");
                    }
                    if (self.status === BICst.REPORT_STATUS.APPLYING) {
                        return BI.i18nText("BI-Cancel_Apply_Hangout");
                    }
                    if (self.status === BICst.REPORT_STATUS.HANGOUT) {
                        return BI.i18nText("BI-Has_Hangouted");
                    }
                },
                stopPropagation: true,
                invisible: true
            });
            if (o.status === BICst.REPORT_STATUS.HANGOUT) {
                markCls = "report-hangout-font";
                this.hangout.setEnable(false);
                this.hangout.setWarningTitle(BI.i18nText("BI-Hangout_Report_Can_Not_Mark"));
            }
            this.markButton = BI.createWidget({
                type: "bi.icon_change_button",
                cls: "template-item-mark-icon",
                title: function () {
                    if (self.status === BICst.REPORT_STATUS.APPLYING) {
                        return BI.i18nText("BI-Report_Hangout_Applying");
                    }
                    if (self.status === BICst.REPORT_STATUS.HANGOUT) {
                        return BI.i18nText("BI-Has_Hangouted");
                    }
                },
                stopPropagation: true,
                width: 12,
                height: 12
            });
            this.markButton.setIcon(markCls);
            this.hangout.on(BI.IconChangeButton.EVENT_CHANGE, function () {
                o.onClickHangout();
                self._onClickHangout();
            });
            this._refreshHangout();

            //查看已分享
            var sharedButton = BI.createWidget({
                type: "bi.icon_button",
                cls: "report-cancel-share-font template-item-icon",
                title: BI.i18nText("BI-Cancel_Shared_Users"),
                width: 16,
                height: 16,
                invisible: true,
                stopPropagation: true
            });
            sharedButton.on(BI.IconButton.EVENT_CHANGE, function () {
                var id = BI.UUID();
                var sharedUsers = BI.createWidget({
                    type: "bi.edit_shared_pane",
                    shared: o.shared
                });
                sharedUsers.on(BI.EditSharedPane.EVENT_CLOSE, function () {
                    BI.Popovers.remove(id);
                });
                sharedUsers.on(BI.EditSharedPane.EVENT_SAVE, function () {
                    o.editSharedUsers(this.getValue());
                    BI.Popovers.remove(id);
                });
                BI.Popovers.create(id, sharedUsers, {width: 600, height: 500}).open(id);
            });
            if (BI.isNull(o.shared) || o.shared.length === 0) {
                sharedButton.setEnable(false);
                sharedButton.setWarningTitle(BI.i18nText("BI-The_Report_Not_Shared"));
            }
        }

        var copyButton = BI.createWidget({
            type: 'bi.copy_link_icon_button',
            cls: "template-item-icon",
            buildUrl: o.buildUrl
        });

        var renameIcon = BI.createWidget({
            type: "bi.icon_button",
            cls: 'report-rename-font template-item-icon',
            title: BI.i18nText("BI-Rename"),
            invisible: true,
            stopPropagation: true
        });
        renameIcon.on(BI.IconButton.EVENT_CHANGE, function () {
            BI.requestAsync("fr_bi", "check_report_edit", {id: o.id, createBy: o.createBy}, function (res) {
                if (BI.isNotNull(res.result) && res.result.length > 0) {
                    BI.Msg.toast(BI.i18nText("BI-Report_Editing_Cannot_Rename", res.result), "warning");
                } else {
                    self.editor.focus();
                }
            });
        });

        var deleteIcon = BI.createWidget({
            type: "bi.icon_button",
            cls: 'remove-report-font template-item-icon',
            title: BI.i18nText("BI-Remove")
        });
        var deleteCombo = BI.createWidget({
            type: "bi.bubble_combo",
            el: deleteIcon,
            popup: {
                type: "bi.bubble_bar_popup_view",
                buttons: [{
                    value: BI.i18nText(BI.i18nText("BI-Sure")),
                    handler: function () {
                        deleteCombo.hideView();
                        BI.requestAsync("fr_bi", "check_report_edit", {id: o.id, createBy: o.createBy}, function (res) {
                            if (BI.isNotNull(res.result) && res.result.length > 0) {
                                BI.Msg.toast(BI.i18nText("BI-Report_Editing_Cannot_Remove", res.result), "warning");
                            } else {
                                o.onDeleteReport.apply(self, arguments);
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
                        cls: "template-file-item-label",
                        text: BI.i18nText("BI-Confirm_Delete_Report"),
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

        if (o.status === BICst.REPORT_STATUS.HANGOUT) {
            deleteCombo.setEnable(false);
            deleteIcon.setWarningTitle(BI.i18nText("BI-Hangout_Report_Can_Not_Delete"));
        }

        var timeText = BI.createWidget({
            type: 'bi.label',
            cls: "template-file-item-date",
            text: FR.date2Str(new Date(o.lastModify), "yyyy.MM.dd HH:mm:ss")
        });

        this.element.hover(function () {
            copyButton.setVisible(true);
            renameIcon.setVisible(true);
            deleteCombo.setVisible(true);
            self.hangout && self.hangout.setVisible(true);
            sharedButton && sharedButton.setVisible(true);
        }, function () {
            copyButton.setVisible(false);
            renameIcon.setVisible(false);
            deleteCombo.setVisible(false);
            self.hangout && self.hangout.setVisible(false);
            sharedButton && sharedButton.setVisible(false);
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
                    type: "bi.absolute",
                    items: [{
                        el: {
                            type: "bi.icon_button",
                            cls: (o.description === "true" ? "real-time-font" : "file-font") + " template-item-icon",
                            iconWidth: 16,
                            iconHeight: 16
                        },
                        top: 12,
                        left: 12
                    }, {
                        el: this.markButton || BI.createWidget(),
                        bottom: 6,
                        right: 6
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
                items: [sharedButton, this.hangout, copyButton, renameIcon, deleteCombo]
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
        BI.ReportListViewItem.superclass.doClick.apply(this, arguments);
        o.onClickReport.apply(self, arguments);
    },

    _onClickHangout: function () {
        if (this.status === BICst.REPORT_STATUS.NORMAL) {
            this.status = BICst.REPORT_STATUS.APPLYING;
        } else {
            this.status = BICst.REPORT_STATUS.NORMAL;
        }
        this._refreshHangout();
    },

    _refreshHangout: function () {
        if (this.status === BICst.REPORT_STATUS.NORMAL) {
            this.hangout.setIcon("report-apply-hangout-ing-font");
            if (BI.isNotNull(this.markButton)) {
                this.markButton.setVisible(false);
            }
        }
        if (this.status === BICst.REPORT_STATUS.APPLYING) {
            this.hangout.setIcon("report-hangout-ing-mark-font");
            if (BI.isNotNull(this.markButton)) {
                this.markButton.setIcon("report-hangout-ing-mark-font");
                this.markButton.setVisible(true);
            }
        }
        if (this.status === BICst.REPORT_STATUS.HANGOUT) {
            this.hangout.setIcon("report-apply-hangout-normal-font");
            this.markButton && this.markButton.setVisible(true);
        }
    },

    isSelected: function () {
        return this.checkbox.isSelected();
    },

    setSelected: function (v) {
        this.checkbox.setSelected(v);
    },

    getText: function () {
        return this.editor.getValue();
    },

    doRedMark: function (keyword) {
        this.editor.doRedMark(keyword);
    },

    destroy: function () {
        BI.ReportListViewItem.superclass.destroy.apply(this, arguments);
    }
});
$.shortcut("bi.report_list_view_item", BI.ReportListViewItem);