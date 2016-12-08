/**
 * Created by roy on 16/4/1.
 */
BI.DetailDetailTableSelectDataPane = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.DetailDetailTableSelectDataPane.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-detail-detail-select-data",
            wId: ""
        })
    },

    _init: function () {
        BI.DetailDetailTableSelectDataPane.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.service = BI.createWidget({
            type: "bi.package_select_data_service",
            element: this.element,
            wId: o.wId,
            showRelativeTables: true,
            showExcelView: true,
            showDateGroup: true,
            showTime: true,
            tablesCreator: function (packageIdOrTableId, isRelation) {
                if (isRelation === true) {
                    var tIds = BI.Utils.getPrimaryRelationTablesByTableID(packageIdOrTableId);
                    return BI.map(tIds, function (i, id) {
                        return {
                            id: id,
                            type: "bi.detail_detail_select_data_level1_node"
                        }
                    })
                }
                var ids = BI.Utils.getTableIDsOfPackageID(packageIdOrTableId);
                return BI.map(ids, function (i, id) {
                    return {
                        id: id,
                        type: "bi.detail_detail_select_data_level0_node"
                    }
                })
            },
            fieldsCreator: function (tableId, isRelation) {
                var ids = BI.Utils.getSortedFieldIdsOfOneTableByTableId(tableId);
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
$.shortcut("bi.detail_detail_table_select_data", BI.DetailDetailTableSelectDataPane);