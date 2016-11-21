/**
 * @class BI.DetailSelectDataPane
 * @extend BI.Widget
 * 选择字段
 */
BI.DetailSelectDataPane = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.DetailSelectDataPane.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-detail-select-data-pane",
            wId: ""
        })
    },

    _init: function () {
        BI.DetailSelectDataPane.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.service = BI.createWidget({
            type: "bi.package_select_data_service",
            element: this.element,
            wId: o.wId,
            showRelativeTables: true,
            showExcelView: true,
            showDateGroup: true,
            tablesCreator: function (packageId, opt) {
                if (opt.isRelation === true) {
                    var tIds = BI.Utils.getPrimaryRelationTablesByTableID(packageId);
                    return BI.map(tIds, function (i, id) {
                        return {
                            id: id,
                            type: "bi.detail_select_data_level1_node"
                        }
                    })
                }
                var ids = BI.Utils.getTableIDsOfPackageID(packageId);
                return BI.map(ids, function (i, id) {
                    return {
                        id: id
                    }
                })
            },
            fieldsCreator: function (tableId, opt) {
                var ids = BI.Utils.getSortedFieldIdsOfOneTableByTableId(tableId);
                ids = BI.Utils.getCountFieldIDsOfTableID(tableId).concat(ids);
                var result = [];
                BI.each(ids, function (i, fid) {
                    if (BI.Utils.getFieldIsUsableByID(fid) === true) {
                        result.push({
                            id: fid
                        })
                    }
                });
                return result;
            }
        });
    }
});
$.shortcut("bi.detail_select_data_pane", BI.DetailSelectDataPane);