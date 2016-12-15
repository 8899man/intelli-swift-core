/**
 * 选择文本字段面板
 *
 * Created by GUY on 2016/5/10.
 * @class BI.SelectString4RealTime
 * @extend BI.Widget
 */
BI.SelectString4RealTime = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.SelectString4RealTime.superclass._defaultConfig.apply(this, arguments), {
            wId: ""
        })
    },

    _init: function () {
        BI.SelectString4RealTime.superclass._init.apply(this, arguments);
        var self = this, o = this.options;

        this.service = BI.createWidget({
            type: "bi.package_select_data_service",
            element: this.element,
            wId: o.wId,
            showRelativeTables: false,
            showExcelView: false,
            showDateGroup: false,
            tablesCreator: function (packageId) {
                var ids = BI.Utils.getTableIDsOfPackageID(packageId);
                return BI.map(ids, function (i, id) {
                    return {
                        id: id,
                        type: "bi.detail_select_data_level0_node_4_realtime"
                    }
                })
            },
            fieldsCreator: function (tableId, opt) {
                opt = opt || {};
                var ids = BI.Utils.getStringFieldIDsOfTableID(tableId);
                var result = [];
                BI.each(ids, function (i, fid) {
                    if (BI.Utils.getFieldIsUsableByID(fid) === true) {
                        result.push({
                            id: fid,
                            type: opt.isRelation ? "bi.select_date_level1_item" : "bi.select_date_level0_item"
                        })
                    }
                });
                return result;
            }
        });
    }
});

$.shortcut("bi.select_string_4_realtime", BI.SelectString4RealTime);
