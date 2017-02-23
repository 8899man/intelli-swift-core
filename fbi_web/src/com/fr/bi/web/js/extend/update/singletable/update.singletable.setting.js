/**
 * Created by Young's on 2016/4/22.
 */
BI.UpdateSingleTableSetting = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.UpdateSingleTableSetting.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-update-single-table-setting"
        })
    },

    _init: function () {
        BI.UpdateSingleTableSetting.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.model = new BI.UpdateSingleTableSettingModel({
            update_setting: o.update_setting,
            table: o.table,
            currentTable: o.currentTable
        });

        //最上面的更新方式下拉框
        this.updateType = BI.createWidget({
            type: "bi.text_value_check_combo",
            height: 30,
            items: [{
                text: BI.i18nText("BI-Always_Updates"),
                value: BICst.SINGLE_TABLE_UPDATE_TYPE.ALL
            }, {
                text: BI.i18nText("BI-Full_Then_Incremental_Update"),
                value: BICst.SINGLE_TABLE_UPDATE_TYPE.PART
            }, {
                text: BI.i18nText("BI-Full_Then_No_Update"),
                value: BICst.SINGLE_TABLE_UPDATE_TYPE.NEVER
            }]
        });
        this.updateType.setValue(this.model.getUpdateType());
        this.updateType.on(BI.TextValueCheckCombo.EVENT_CHANGE, function () {
            var v = this.getValue()[0];
            self.model.setUpdateType(v);
            switch (v) {
                case BICst.SINGLE_TABLE_UPDATE_TYPE.ALL:
                    partUpdate.setVisible(true);
                    timeSetting.setVisible(true);
                    break;
                case BICst.SINGLE_TABLE_UPDATE_TYPE.PART:
                    partUpdate.setVisible(true);
                    timeSetting.setVisible(true);
                    break;
                case BICst.SINGLE_TABLE_UPDATE_TYPE.NEVER:
                    partUpdate.setVisible(true);
                    timeSetting.setVisible(true);
                    break;
            }
            self.fireEvent(BI.UpdateSingleTableSetting.EVENT_CHANGE);
        });

        //增量更新设置面板
        var partUpdate = this._createPartUpdateTab();
        partUpdate.setVisible(true);

        //定时设置
        var timeSetting = this._createTimeSetting();
        partUpdate.setVisible(true);

        var popup = BI.createWidget({
            type: "bi.button_group",
            items: BI.createItems([{
                text: BI.i18nText("BI-Full_Updates"),
                value: BICst.SINGLE_TABLE_UPDATE_TYPE.ALL
            }, {
                text: BI.i18nText("BI-Incremental_Update"),
                value: BICst.SINGLE_TABLE_UPDATE_TYPE.PART
            }], {
                type: "bi.single_select_item",
                height: 25
            }),
            chooseType: BI.Selection.None,
            layouts: [{
                type: "bi.vertical"
            }]
        });

        this.processBar = BI.createWidget({
            type: "bi.progress_text_bar",
            width: 115,
            height: 28
        });
        this.processBar.setVisible(false);

        this.immediateCombo = BI.createWidget({
            type: "bi.combo",
            trigger: "hover",
            el: {
                type: "bi.button",
                text: BI.i18nText("BI-Update_Table_Immedi"),
                height: 28,
                width: 115
            },
            popup: {
                el: popup
            }
        });
        popup.on(BI.ButtonGroup.EVENT_CHANGE, function () {
            self.immediateCombo.hideView();
        });
        this.immediateCombo.on(BI.Combo.EVENT_CHANGE, function (v) {
            self._startSingleUpdate(v);
            var tableInfo = {
                updateType: v,
                baseTable: self.model.table,
                isETL: false
            };
            if (self.model.options.currentTable.connection_name == BICst.CONNECTION.ETL_CONNECTION) {
                tableInfo.isETL = true;
                tableInfo.ETLTable = self.model.currentTable;
            }
            self.fireEvent(BI.UpdateSingleTableSetting.EVENT_CUBE_SAVE, tableInfo, function () {
                self._createCheckInterval();
            });
        });

        BI.createWidget({
            type: "bi.vertical",
            element: this.element,
            items: [{
                type: "bi.htape",
                cls: "update-type",
                items: [{
                    el: {
                        type: "bi.label",
                        text: BI.i18nText("BI-When_Global_Update") + ": ",
                        height: 30,
                        textAlign: "left",
                        cls: "comment-label"
                    },
                    width: 90
                }, {
                    el: this.updateType,
                    width: "fill"
                }, {
                    el: {
                        type: "bi.vertical",
                        items: [this.processBar, this.immediateCombo]
                    },
                    width: 115
                }],
                hgap: 5,
                height: 30
            }, timeSetting, partUpdate],
            hgap: 10,
            vgap: 10
        });
        self._createCheckInterval();
    },

    _createPartUpdateTab: function () {
        var self = this;
        //增量增加、增量删除、增量修改
        this.buttons = BI.createWidget({
            type: "bi.button_group",
            items: BI.createItems([{
                text: BI.i18nText("BI-Incremental_Increase"),
                value: BI.UpdateSingleTableSetting.PART_ADD
            }, {
                text: BI.i18nText("BI-Incremental_Deletion"),
                value: BI.UpdateSingleTableSetting.PART_DELETE
            }, {
                text: BI.i18nText("BI-Incremental_Updates"),
                value: BI.UpdateSingleTableSetting.PART_MODIFY
            }], {
                type: "bi.update_type_button",
                cls: "part-update-type-button",
                height: 28,
                width: 100
            }),
            layouts: [{
                type: "bi.left",
                rgap: 2
            }]
        });
        this.buttons.setValue(BI.UpdateSingleTableSetting.PART_ADD);
        this.buttons.on(BI.ButtonGroup.EVENT_CHANGE, function () {
            self.tab.setSelect(this.getValue()[0]);
        });
        this._showOrHideIcon();

        //上次更新时间参数
        var param = BI.i18nText("BI-Last_Updated");
        var lastUpdateParam = BI.createWidget({
            type: "bi.text_button",
            text: param,
            cls: "param-button",
            height: 25
        });
        lastUpdateParam.on(BI.TextButton.EVENT_CHANGE, function () {
            var v = self.tab.getSelect();
            switch (v) {
                case BI.UpdateSingleTableSetting.PART_ADD:
                    self.partAddSql.insertParam(param);
                    break;
                case BI.UpdateSingleTableSetting.PART_DELETE:
                    self.partDeleteSql.insertParam(param);
                    break;
                case BI.UpdateSingleTableSetting.PART_MODIFY:
                    self.partModifySql.insertParam(param);
                    break;
            }
        });

        //当前更新时间参数
        var currParam = BI.i18nText("BI-Current_Update_Time");
        var currentUpdateParam = BI.createWidget({
            type: "bi.text_button",
            text: currParam,
            cls: "param-button",
            height: 25
        });
        currentUpdateParam.on(BI.TextButton.EVENT_CHANGE, function () {
            var v = self.tab.getSelect();
            switch (v) {
                case BI.UpdateSingleTableSetting.PART_ADD:
                    self.partAddSql.insertParam(currParam);
                    break;
                case BI.UpdateSingleTableSetting.PART_DELETE:
                    self.partDeleteSql.insertParam(currParam);
                    break;
                case BI.UpdateSingleTableSetting.PART_MODIFY:
                    self.partModifySql.insertParam(currParam);
                    break;
            }
        });


        //预览按钮
        var previewButton = BI.createWidget({
            type: "bi.text_button",
            cls: "",
            text: BI.i18nText("BI-Preview"),
            height: 35
        });
        previewButton.on(BI.TextButton.EVENT_CHANGE, function () {
            self._createPreviewPane();
        });

        //对应的三个sql面板
        this.tab = BI.createWidget({
            type: "bi.tab",
            direction: "custom",
            tab: this.buttons,
            cardCreator: BI.bind(this._createPartUpdateCard, this),
            height: 200
        });
        this.tab.setSelect(BI.UpdateSingleTableSetting.PART_ADD);

        return BI.createWidget({
            type: "bi.absolute",
            element: this.tab,
            cls: "part-update-setting",
            items: [{
                el: {
                    type: "bi.left_right_vertical_adapt",
                    cls: "param-preview-tools",
                    items: {
                        left: [{
                            type: "bi.label",
                            text: BI.i18nText("BI-Parameter"),
                            height: 35,
                            cls: "param-comment"
                        }, lastUpdateParam, currentUpdateParam],
                        right: [previewButton]
                    },
                    height: 35,
                    llgap: 10,
                    rrgap: 10
                },
                top: 59,
                left: 0,
                right: 0
            }, {
                el: this.buttons,
                top: 30,
                left: 0
            }, {
                el: {
                    type: "bi.label",
                    text: BI.i18nText("BI-Incremental_Update_Type") + ": ",
                    height: 30,
                    cls: "comment-label"
                },
                top: 0,
                left: 0
            }]
        })
    },

    _createPreviewPane: function () {
        var self = this;
        BI.Popovers.remove(this.model.getId() + "preview");
        var previewPane = BI.createWidget({
            type: "bi.update_preview_pane",
            table: this.model.getTable()
        });
        previewPane.on(BI.UpdatePreviewPane.EVENT_CHANGE, function () {
            BI.Popovers.remove(self.model.getId() + "preview");
            self.fireEvent(BI.UpdateSingleTableSetting.EVENT_CLOSE_PREVIEW);
        });
        BI.Popovers.create(this.model.getId() + "preview", previewPane);
        var sql = "", type = this.tab.getSelect();
        switch (type) {
            case BI.UpdateSingleTableSetting.PART_ADD:
                sql = this.model.getAddSql();
                break;
            case BI.UpdateSingleTableSetting.PART_DELETE:
                sql = this.model.getDeleteSql();
                break;
            case BI.UpdateSingleTableSetting.PART_MODIFY:
                sql = this.model.getModifySql();
                break;
        }
        previewPane.populate(sql, type);
        BI.Popovers.open(this.model.getId() + "preview");
        this.fireEvent(BI.UpdateSingleTableSetting.EVENT_OPEN_PREVIEW);
    },

    _createPartUpdateCard: function (v) {
        var self = this;
        switch (v) {
            case BI.UpdateSingleTableSetting.PART_ADD:
                this.partAddSql = BI.createWidget({
                    type: "bi.code_editor",
                    cls: "sql-container"
                });
                this.partAddSql.setValue(this.model.getAddSql()
                    .replaceAll(BICst.LAST_UPDATE_TIME, BI.i18nText("BI-Last_Updated"))
                    .replaceAll(BICst.CURRENT_UPDATE_TIME, BI.i18nText("BI-Current_Update_Time")));
                this.partAddSql.on(BI.CodeEditor.EVENT_CHANGE, function () {
                    self.model.setAddSql(self.partAddSql.getValue()
                        .replaceAll(BI.i18nText("BI-Last_Updated"), BICst.LAST_UPDATE_TIME)
                        .replaceAll(BI.i18nText("BI-Current_Update_Time"), BICst.CURRENT_UPDATE_TIME));
                    self._showOrHideIcon(v);
                });
                return BI.createWidget({
                    type: "bi.absolute",
                    items: [{
                        el: this.partAddSql,
                        top: 95,
                        left: 0,
                        bottom: 0,
                        right: 0
                    }]
                });
            case BI.UpdateSingleTableSetting.PART_DELETE:
                this.partDeleteSql = BI.createWidget({
                    type: "bi.code_editor",
                    cls: "sql-container"
                });
                this.partDeleteSql.setValue(this.model.getDeleteSql()
                    .replaceAll(BICst.LAST_UPDATE_TIME, BI.i18nText("BI-Last_Updated"))
                    .replaceAll(BICst.CURRENT_UPDATE_TIME, BI.i18nText("BI-Current_Update_Time")));
                this.partDeleteSql.on(BI.CodeEditor.EVENT_CHANGE, function () {
                    self.model.setDeleteSql(self.partDeleteSql.getValue()
                        .replaceAll(BI.i18nText("BI-Last_Updated"), BICst.LAST_UPDATE_TIME)
                        .replaceAll(BI.i18nText("BI-Current_Update_Time"), BICst.CURRENT_UPDATE_TIME));
                    self._showOrHideIcon(v);
                });
                return BI.createWidget({
                    type: "bi.absolute",
                    items: [{
                        el: this.partDeleteSql,
                        top: 95,
                        left: 0,
                        bottom: 0,
                        right: 0
                    }]
                });
            case BI.UpdateSingleTableSetting.PART_MODIFY:
                this.partModifySql = BI.createWidget({
                    type: "bi.code_editor",
                    cls: "sql-container"
                });
                this.partModifySql.setValue(this.model.getModifySql()
                    .replaceAll(BICst.LAST_UPDATE_TIME, BI.i18nText("BI-Last_Updated"))
                    .replaceAll(BICst.CURRENT_UPDATE_TIME, BI.i18nText("BI-Current_Update_Time")));
                this.partModifySql.on(BI.CodeEditor.EVENT_CHANGE, function () {
                    self.model.setModifySql(self.partModifySql.getValue()
                        .replaceAll(BI.i18nText("BI-Last_Updated"), BICst.LAST_UPDATE_TIME)
                        .replaceAll(BI.i18nText("BI-Current_Update_Time"), BICst.CURRENT_UPDATE_TIME));
                    self._showOrHideIcon(v);
                });
                return BI.createWidget({
                    type: "bi.absolute",
                    items: [{
                        el: this.partModifySql,
                        top: 95,
                        left: 0,
                        bottom: 0,
                        right: 0
                    }]
                });
        }
    },

    _showOrHideIcon: function () {
        this.buttons.getAllButtons()[0].toggleIcon(BI.isNotNull(this.model.getAddSql()) && BI.isNotEmptyString(this.model.getAddSql()));
        this.buttons.getAllButtons()[1].toggleIcon(BI.isNotNull(this.model.getDeleteSql()) && BI.isNotEmptyString(this.model.getDeleteSql()));
        this.buttons.getAllButtons()[2].toggleIcon(BI.isNotNull(this.model.getModifySql()) && BI.isNotEmptyString(this.model.getModifySql()));
    },

    _createTimeSetting: function () {
        var self = this;
        var addTime = BI.createWidget({
            type: "bi.button",
            height: 28,
            text: "+" + BI.i18nText("BI-Timing_Set")
        });
        addTime.on(BI.Button.EVENT_CHANGE, function () {
            var item = BI.createWidget({
                type: "bi.single_table_time_setting_item",
                id: BI.UUID(),
                onRemoveSetting: function (id) {
                    self._removeSettingById(id);
                }
            });

            item.on(BI.SingleTableTimeSettingItem.EVENT_CHANGE, function () {
                self.model.setTimeList(self.timeSettingGroup.getValue())
            });
            self.timeSettingGroup.addItems([item]);
            self.model.setTimeList(self.timeSettingGroup.getValue());
        });

        this.timeSettingGroup = BI.createWidget({
            type: "bi.button_group",
            items: self._createTimeSettingListItems(),
            layouts: [{
                type: "bi.vertical"
            }]
        });

        return BI.createWidget({
            type: "bi.vertical",
            cls: "time-setting",
            items: [{
                type: "bi.left_right_vertical_adapt",
                cls: "add-time-toolbar",
                items: {
                    left: [{
                        type: "bi.label",
                        text: BI.i18nText("BI-Single_Update") + ": ",
                        height: 30,
                        hgap: 10,
                        cls: "add-time-comment"
                    }],
                    right: [addTime]
                },
                height: 30
            }, this.timeSettingGroup]
        })
    },

    _removeSettingById: function (id) {
        var allButtons = this.timeSettingGroup.getAllButtons();
        var index = 0;
        BI.some(allButtons, function (i, button) {
            if (button.getValue().id === id) {
                index = i;
                return true;
            }
        });
        this.timeSettingGroup.removeItemAt(index);
    },

    getValue: function () {
        return this.model.getValue();
    },

    _createTimeSettingListItems: function () {
        var self = this;
        var items = [];
        BI.each(this.model.getTimeList(), function (index, valueObj) {
            var item = BI.createWidget(BI.extend(
                valueObj, {
                    type: "bi.single_table_time_setting_item",
                    id: BI.UUID(),
                    onRemoveSetting: function (id) {
                        self._removeSettingById(id);
                    }
                }
            ));
            item.on(BI.SingleTableTimeSettingItem.EVENT_CHANGE, function () {
                self.model.setTimeList(self.timeSettingGroup.getValue())
            });
            items.push(item);
        });
        return items;
    },

    _createCheckInterval: function () {
        var self = this;
        this._clearCheckInterval();
        self.cubeInterval = setInterval(function () {
            self._getTaskStatus();
        }, 2000);
    },

    _getTextByUpdatingType: function () {
        return this.updatingType === BICst.SINGLE_TABLE_UPDATE_TYPE.PART ?
            BI.i18nText("BI-Incremental") : BI.i18nText("BI-Full_Amount");
    },

    _startSingleUpdate: function (v) {
        this.updatingType = v;
        this.immediateCombo.setVisible(false);
        this.processBar.setVisible(true);
        this.processBar.setValue(0);
        this.processBar.setText(BI.i18nText("BI-Wait_For_Update", this._getTextByUpdatingType()));
    },

    //后台无法提供单表的更新进度
    //简单的展现：剩余随机
    _setProcess: function () {
        if (this.processBar.isVisible() === false) {
            this.processBar.setVisible(true);
            this.immediateCombo.setVisible(false);
        }
        var value = this.processBar.getValue();
        value = value + Math.random() * (100 - value) / 2;
        value = BI.parseInt(value);
        this.processBar.setValue(value > 95 ? 95 : value);
        this.processBar.setText(BI.i18nText("BI-Single_Table_Updating", this._getTextByUpdatingType()) + value + "%");
    },

    //更新完成
    _updateComplete: function () {
        var self = this;
        if (this.processBar.isVisible() === false) {
            return;
        }
        this.processBar.setValue(100);
        this.processBar.setText(BI.i18nText("BI-Completed"));
        BI.delay(function () {
            self.processBar.setVisible(false);
            self.processBar.setValue(0);
            self.immediateCombo.setVisible(true);
        }, 1000);
    },

    _getTableInfo: function () {
        var tableInfo = {
            baseTable: this.model.table,
            isETL: false
        };
        if (this.model.options.currentTable.connection_name == BICst.CONNECTION.ETL_CONNECTION) {
            tableInfo.isETL = true;
            tableInfo.ETLTable = this.model.currentTable;
        }
        return tableInfo;
    },

    _getTaskStatus: function () {
        var tableInfo = this._getTableInfo();
        var self = this;
        BI.Utils.reqCubeStatusCheck(tableInfo, function (data) {
                if (!data.hasTask) {
                    self._updateComplete();
                    self._clearCheckInterval();
                } else {
                    self._setProcess();
                }
            }
        )
    },

    _clearCheckInterval: function () {
        if (BI.isNotNull(this.cubeInterval)) {
            clearInterval(this.cubeInterval);
            this.cubeInterval = null;
        }
    }

});
BI.extend(BI.UpdateSingleTableSetting, {
    PART_ADD: 1,
    PART_DELETE: 2,
    PART_MODIFY: 3
});
BI.UpdateSingleTableSetting.EVENT_CHANGE = "EVENT_CHANGE";
BI.UpdateSingleTableSetting.EVENT_CUBE_SAVE = "EVENT_CUBE_SAVE";
BI.UpdateSingleTableSetting.EVENT_OPEN_PREVIEW = "EVENT_OPEN_PREVIEW";
BI.UpdateSingleTableSetting.EVENT_CLOSE_PREVIEW = "EVENT_CLOSE_PREVIEW";
$.shortcut("bi.update_single_table_setting", BI.UpdateSingleTableSetting);
