/**
 * @class BI.SelectDataWithMask
 * @extend BI.Widget
 * 带有蒙版的选择字段（设置表关联）
 */
BI.SelectDataWithMask = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.SelectDataWithMask.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-select-data-with-mask"
        })
    },

    _init: function () {
        BI.SelectDataWithMask.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.field = o.field;
        var maskId = o.maskId;

        this.selectDataPane = BI.createWidget({
            type: "bi.package_select_data_service",
            showRelativeTables: false,
            showExcelView: false,
            showDateGroup: false,
            packageCreator: function () {
                return BI.Utils.getAllGroupedPackagesTreeJSON4Conf();
            },
            tablesCreator: function (packageId) {
                return self._getTablesStructureByPackId(packageId);
            },
            fieldsCreator: function (tableId, opt) {
                return self._getFieldsStructureByTableId(tableId, opt);
            }
        });
        this.selectDataPane.on(BI.PackageSelectDataService.EVENT_CLICK_ITEM, function () {
            self.fireEvent(BI.SelectDataWithMask.EVENT_CHANGE, arguments);
        });

        var selectdataWrapper = BI.createWidget({
            type: "bi.vtape",
            cls: "select-data-wrapper",
            items: [{
                el: this.selectDataPane,
                height: "fill"
            }, {
                el: this._createSelectDataBottom(),
                height: 50
            }]
        })

        selectdataWrapper.element.resizable({
            handles: "e",
            minWidth: 200,
            maxWidth: 400,
            autoHide: true,
            helper: "bi-resizer",
            start: function () {
            },
            resize: function (e, ui) {
            },
            stop: function (e, ui) {
                items[1].width = ui.size.width;
                selectdataWrapper.resize();
            }
        });

        var items = [{
            el: {
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
            },
            top: 0,
            right: 0,
            bottom: 0,
            left: 0
        }, {
            el: selectdataWrapper,
            top: 10,
            bottom: 10,
            width: 240,
            left: 10
        }];

        BI.createWidget({
            type: "bi.absolute",
            element: this.element,
            items: items
        });

        BI.Maskers.hide(maskId);
        var mask = BI.createWidget({
            type: "bi.loading_mask",
            masker: BICst.BODY_ELEMENT,
            text: BI.i18nText("BI-Loading")
        });
        BI.Utils.getAllPackages(function (packs) {
            BI.Maskers.show(maskId);
            self.packs = packs;
            //选中当前业务包
            self.selectDataPane.setPackage(BI.Utils.getCurrentPackageId4Conf());
        }, function () {
            mask.destroy();
        });

    },

    _createSelectDataBottom: function () {
        var self = this;
        var cancelButton = BI.createWidget({
            type: "bi.button",
            level: "ignore",
            width: 150,
            height: 30,
            text: BI.i18nText("BI-Cancel")
        });
        cancelButton.on(BI.Button.EVENT_CHANGE, function () {
            self.fireEvent(BI.SelectDataWithMask.EVENT_VALUE_CANCEL);
        });
        return BI.createWidget({
            type: "bi.horizontal_auto",
            cls: "select-data-button-group",
            items: [cancelButton],
            vgap: 10
        })
    },

    _isTableOpen: function (table) {
        var self = this;
        return BI.some(table.fields, function (i, fs) {
            return BI.some(fs, function (j, field) {
                return field.id === self.field.id;
            });
        });
    },

    _getTablesStructureByPackId: function (pId) {
        var self = this;
        var tablesStructure = [];
        var tableIds = BI.Utils.getTablesIdByPackageId4Conf(pId);
        BI.each(tableIds, function (i, id) {
            tablesStructure.push({
                id: id,
                type: "bi.select_data_level0_node",
                text: BI.Utils.getTransNameById4Conf(id),
                title: BI.Utils.getTransNameById4Conf(id),
                value: id,
                isParent: true,
                open: self._isTableOpen(id),
                disabled: id === self.field.table_id,
                warningTitle: BI.i18nText("BI-Can_Not_Relation_Self")
            });
        });
        return tablesStructure;
    },

    _getFieldsStructureByTableId: function (tableId) {
        var fieldStructure = [];
        var fieldType = this.field.field_type;
        var relationFields = BI.Utils.getRelationFieldsByFieldId4Conf(this.field.id);
        var fields = BI.Utils.getFieldsByTableId4Conf(tableId);
        BI.each(fields, function (i, field) {
            if (field.field_type === fieldType) {
                fieldStructure.push({
                    id: field.id,
                    pId: tableId,
                    type: "bi.select_data_level0_item",
                    fieldType: fieldType,
                    text: BI.Utils.getTransNameById4Conf(field.id) || field.field_name,
                    title: BI.Utils.getTransNameById4Conf(field.id) || field.field_name,
                    value: {
                        field_id: field.id
                    },
                    disabled: relationFields.contains(field.id),
                    warningTitle: BI.i18nText("BI-Already_Relation_With_Current_Field")
                })
            }
        });
    },

    destroy: function () {
        this.selectDataPane.destroy();
    }
});
BI.SelectDataWithMask.EVENT_CHANGE = "EVENT_CHANGE";
BI.SelectDataWithMask.EVENT_VALUE_CANCEL = "EVENT_VALUE_CANCEL";
$.shortcut("bi.select_data_with_mask", BI.SelectDataWithMask);
