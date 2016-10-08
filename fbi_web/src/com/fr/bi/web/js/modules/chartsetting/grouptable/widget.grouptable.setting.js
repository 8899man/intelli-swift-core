/**
 * created by young
 * 分组表的样式设置
 */
BI.GroupTableSetting = BI.inherit(BI.Widget, {

    constant: {
        SINGLE_LINE_HEIGHT: 60,
        SIMPLE_H_GAP: 10,
        SIMPLE_L_GAP: 2,
        CHECKBOX_WIDTH: 16,
        EDITOR_WIDTH: 60,
        EDITOR_HEIGHT: 26,
        BUTTON_WIDTH: 40,
        BUTTON_HEIGHT: 30,
        ICON_WIDTH: 24,
        ICON_HEIGHT: 24
    },

    _defaultConfig: function () {
        return BI.extend(BI.GroupTableSetting.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-group-table-setting"
        })
    },

    _init: function () {
        BI.GroupTableSetting.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        //显示组件标题
        this.showTitle = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Show_Chart_Title"),
            cls: "attr-names",
            logic: {
                dynamic: true
            }
        });
        this.showTitle.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.GroupTableSetting.EVENT_CHANGE);
        });

        //组件标题
        this.title = BI.createWidget({
            type: "bi.sign_editor",
            width: this.constant.EDITOR_WIDTH,
            height: this.constant.EDITOR_HEIGHT,
        });

        this.title.on(BI.SignEditor.EVENT_CHANGE, function() {
            self.fireEvent(BI.GroupTableSetting.EVENT_CHANGE)
        });

        //详细设置
        this.titleDetailSettting = BI.createWidget({
            type: "bi.show_title_detailed_setting_combo"
        });

        this.titleDetailSettting.on(BI.ShowTitleDetailedSettingCombo.EVENT_CHANGE, function () {
            self.fireEvent(BI.GroupTableSetting.EVENT_CHANGE)
        });

        var widgetTitle = BI.createWidget({
            type: "bi.left",
            cls: "single-line-settings",
            items: BI.createItems([{
                type: "bi.label",
                text: BI.i18nText("BI-Widget_Title"),
                cls: "line-title",
            }, {
                type: "bi.vertical_adapt",
                items: [this.showTitle]
            }, {
                type: "bi.vertical_adapt",
                items: [this.title]
            },{
                type: "bi.vertical_adapt",
                items: [this.titleDetailSettting]
            }], {
                height: this.constant.SINGLE_LINE_HEIGHT
            }),
            hgap: this.constant.SIMPLE_H_GAP
        });

        //类型——横向、纵向展开
        this.tableFormGroup = BI.createWidget({
            type: "bi.button_group",
            items: BI.createItems(BICst.TABLE_FORM_GROUP, {
                type: "bi.icon_button",
                extraCls: "table-form-font table-style-font",
                width: this.constant.BUTTON_WIDTH,
                height: this.constant.BUTTON_HEIGHT,
                iconWidth: this.constant.ICON_WIDTH,
                iconHeight: this.constant.ICON_HEIGHT
            }),
            layouts: [{
                type: "bi.vertical_adapt",
                height: this.constant.SINGLE_LINE_HEIGHT
            }]
        });
        this.tableFormGroup.on(BI.ButtonGroup.EVENT_CHANGE, function () {
            self.fireEvent(BI.GroupTableSetting.EVENT_CHANGE);
        });
        //主题颜色
        this.colorSelector = BI.createWidget({
            type: "bi.color_chooser",
            width: this.constant.BUTTON_HEIGHT,
            height: this.constant.BUTTON_HEIGHT
        });
        this.colorSelector.on(BI.ColorChooser.EVENT_CHANGE, function () {
            self.fireEvent(BI.GroupTableSetting.EVENT_CHANGE);
        });
        //风格——1、2、3
        this.tableSyleGroup = BI.createWidget({
            type: "bi.button_group",
            items: BI.createItems(BICst.TABLE_STYLE_GROUP, {
                type: "bi.icon_button",
                extraCls: "table-style-font",
                width: this.constant.BUTTON_WIDTH,
                height: this.constant.BUTTON_HEIGHT,
                iconWidth: this.constant.ICON_WIDTH,
                iconHeight: this.constant.ICON_HEIGHT
            }),
            layouts: [{
                type: "bi.vertical_adapt",
                height: this.constant.SINGLE_LINE_HEIGHT
            }]
        });
        this.tableSyleGroup.on(BI.ButtonGroup.EVENT_CHANGE, function () {
            self.fireEvent(BI.GroupTableSetting.EVENT_CHANGE);
        });
        var tableStyle = BI.createWidget({
            type: "bi.left",
            cls: "single-line-settings",
            items: [{
                type: "bi.label",
                text: BI.i18nText("BI-Table_Sheet_Style"),
                cls: "line-title",
                height: this.constant.SINGLE_LINE_HEIGHT
            }, {
                type: "bi.label",
                text: BI.i18nText("BI-Type"),
                cls: "attr-names",
                height: this.constant.SINGLE_LINE_HEIGHT
            }, this.tableFormGroup, {
                type: "bi.label",
                text: BI.i18nText("BI-Theme_Color"),
                cls: "attr-names",
                height: this.constant.SINGLE_LINE_HEIGHT
            }, {
                type: "bi.vertical_adapt",
                items: [this.colorSelector],
                height: this.constant.SINGLE_LINE_HEIGHT
            }, {
                type: "bi.label",
                text: BI.i18nText("BI-Table_Style"),
                cls: "attr-names",
                height: this.constant.SINGLE_LINE_HEIGHT
            }, this.tableSyleGroup],
            hgap: this.constant.SIMPLE_H_GAP
        });

        //显示序号
        this.showNumber = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Display_Sequence_Number"),
            cls: "attr-names",
            logic: {
                dynamic: true
            }
        });
        this.showNumber.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.GroupTableSetting.EVENT_CHANGE);
        });
        //显示汇总
        this.showRowTotal = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Show_Total_Row"),
            cls: "attr-names",
            logic: {
                dynamic: true
            }
        });
        this.showRowTotal.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.GroupTableSetting.EVENT_CHANGE);
        });
        //展开所有行表头节点
        this.openRowNode = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Open_All_Row_Header_Node"),
            cls: "attr-names",
            logic: {
                dynamic: true
            }
        });
        this.openRowNode.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.GroupTableSetting.EVENT_CHANGE);
        });
        //单页最大行数
        this.maxRow = BI.createWidget({
            type: "bi.sign_editor",
            width: this.constant.EDITOR_WIDTH,
            height: this.constant.EDITOR_HEIGHT,
            cls: "max-row-input",
            errorText: BI.i18nText("BI-Please_Enter_Number_1_To_100"),
            allowBlank: false,
            validationChecker: function (v) {
                return BI.isInteger(v) && v > 0 && v <= 100;
            }
        });
        this.maxRow.on(BI.SignEditor.EVENT_CHANGE, function () {
            self.fireEvent(BI.GroupTableSetting.EVENT_CHANGE);
        });
        var show = BI.createWidget({
            type: "bi.left",
            cls: "single-line-settings",
            items: BI.createItems([{
                type: "bi.label",
                text: BI.i18nText("BI-Element_Show"),
                cls: "line-title"
            }, {
                type: "bi.vertical_adapt",
                items: [this.showNumber]
            }, {
                type: "bi.vertical_adapt",
                items: [this.showRowTotal]
            }, {
                type: "bi.vertical_adapt",
                items: [this.openRowNode]
            }, {
                type: "bi.vertical_adapt",
                items: [{
                    type: "bi.label",
                    text: BI.i18nText("BI-Page_Max_Row"),
                    cls: "attr-names"
                }, {
                    type: "bi.center_adapt",
                    items: [this.maxRow],
                    width: this.constant.EDITOR_WIDTH
                }],
                lgap: 5
            }], {
                height: this.constant.SINGLE_LINE_HEIGHT
            }),
            hgap: this.constant.SIMPLE_H_GAP
        });

        //冻结维度
        this.freezeDim = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Freeze_Table_Dimensions"),
            cls: "attr-names",
            logic: {
                dynamic: true
            }
        });
        this.freezeDim.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.GroupTableSetting.EVENT_CHANGE);
        });
        //联动传递指标过滤条件
        this.transferFilter = BI.createWidget({
            type: "bi.multi_select_item",
            value: BI.i18nText("BI-Bind_Target_Condition"),
            cls: "attr-names",
            logic: {
                dynamic: true
            }
        });
        this.transferFilter.on(BI.Checkbox.EVENT_CHANGE, function () {
            self.fireEvent(BI.GroupTableSetting.EVENT_CHANGE);
        });
        var otherAttr = BI.createWidget({
            type: "bi.left",
            cls: "single-line-settings",
            items: BI.createItems([{
                type: "bi.label",
                text: BI.i18nText("BI-Interactive_Attr"),
                cls: "line-title"
            }, {
                type: "bi.vertical_adapt",
                items: [this.freezeDim]
            }, {
                type: "bi.vertical_adapt",
                items: [this.transferFilter]
            }], {
                height: this.constant.SINGLE_LINE_HEIGHT
            }),
            hgap: this.constant.SIMPLE_H_GAP
        });
        BI.createWidget({
            type: "bi.vertical",
            element: this.element,
            items: [widgetTitle, tableStyle, show, otherAttr],
            hgap: 10
        })
    },

    getValue: function () {
        return {
            show_title: this.showTitle.isSelected(),
            title: this.title.getValue(),
            title_detail: this.titleDetailSettting.getValue(),
            table_form: this.tableFormGroup.getValue()[0],
            theme_color: this.colorSelector.getValue(),
            table_style: this.tableSyleGroup.getValue()[0],
            show_number: this.showNumber.isSelected(),
            show_row_total: this.showRowTotal.isSelected(),
            open_row_node: this.openRowNode.isSelected(),
            max_row: this.maxRow.getValue(),
            freeze_dim: this.freezeDim.isSelected(),
            transfer_filter: this.transferFilter.isSelected()
        }
    },

    populate: function () {
        var wId = this.options.wId;
        this.showTitle.setSelected(BI.Utils.getWSShowTitleByID(wId));
        this.title.setValue(BI.Utils.getWSTitleNameByID(wId));
        this.titleDetailSettting.setValue(BI.Utils.getWSDetailSettingByID(wId));
        this.tableFormGroup.setValue(BI.Utils.getWSTableFormByID(wId));
        this.colorSelector.setValue(BI.Utils.getWSThemeColorByID(wId));
        this.tableSyleGroup.setValue(BI.Utils.getWSTableStyleByID(wId));
        this.showNumber.setSelected(BI.Utils.getWSShowNumberByID(wId));
        this.showRowTotal.setSelected(BI.Utils.getWSShowRowTotalByID(wId));
        this.openRowNode.setSelected(BI.Utils.getWSOpenRowNodeByID(wId));
        this.maxRow.setValue(BI.Utils.getWSMaxRowByID(wId));
        this.freezeDim.setSelected(BI.Utils.getWSFreezeDimByID(wId));
        this.transferFilter.setSelected(BI.Utils.getWSTransferFilterByID(wId));
    },

    setValue: function (v) {
        this.showTitle.setSelected(v.show_title);
        this.title.setValue(v.title);
        this.titleDetailSettting.setValue(v.title_detail);
        this.tableFormGroup.setValue(v.table_form);
        this.colorSelector.setValue(v.theme_color);
        this.tableSyleGroup.setValue(v.table_style);
        this.showNumber.setSelected(v.show_number);
        this.showRowTotal.setSelected(v.show_row_total);
        this.openRowNode.setSelected(v.open_row_node);
        this.maxRow.setValue(v.max_row);
        this.freezeDim.setSelected(v.freeze_dim);
        this.transferFilter.setSelected(v.transfer_filter);
    }
});
BI.GroupTableSetting.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.group_table_setting", BI.GroupTableSetting);