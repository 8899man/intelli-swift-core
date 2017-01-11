/**
 * Created by 小灰灰 on 2016/4/11.
 */
BI.ETLReq = {
    reqSaveTable: function (data, callback) {
        data.sessionID = Data.SharingPool.get("sessionID");
        if (data.table && data.table[ETLCst.ITEMS]) {
            data.table[ETLCst.ITEMS][0].operator.sessionID = Data.SharingPool.get("sessionID");
        }
        BI.requestAsync("fr_bi_analysis_etl", "save_table", data, function (res) {
            BI.Utils.afterSaveTable(res);
            callback();
        })
    },

    reqRenameTable: function (data, callback) {
        var d = BI.deepClone(data);
        data.sessionID = Data.SharingPool.get("sessionID");
        BI.requestAsync("fr_bi_analysis_etl", "rename_table", data, function () {
            BI.Utils.afterReNameTable(d.id, d.name, d.describe);
            callback();
        })
    },

    reqDeleteTable: function (data, callback) {
        data.sessionID = Data.SharingPool.get("sessionID");
        var mask = BI.createWidget({
            type: "bi.etl_loading_mask",
            masker: 'body',
            text: BI.i18nText("BI-Loading")
        });
        BI.requestAsync("fr_bi_analysis_etl", "get_used_tables", data, function (res) {
            if (mask != null) {
                mask.destroy()
            }
            var text = BI.isNull(res.table) ? BI.i18nText('BI-Is_Delete_Table') : BI.i18nText('BI-ETL_Sure_Delete_Used_Table', res.table);
            BI.Msg.confirm(BI.i18nText("BI-Warning"), text, function (v) {
                if (v === true) {
                    var mask = BI.createWidget({
                        type: "bi.etl_loading_mask",
                        masker: 'body',
                        text: BI.i18nText("BI-Loading")
                    });
                    BI.requestAsync("fr_bi_analysis_etl", "delete_table", data, function () {
                        BI.Utils.afterDeleteTable(data.id);
                        callback();
                        mask.destroy();
                    })
                }
            })
        })


    },

    reqEditTable: function (data, callback) {
        data.sessionID = Data.SharingPool.get("sessionID");
        var mask = BI.createWidget({
            type: "bi.etl_loading_mask",
            masker: 'body',
            text: BI.i18nText("BI-Loading")
        });
        BI.requestAsync("fr_bi_analysis_etl", "edit_table", data, function (res) {
            if (mask != null) {
                mask.destroy()
            }
            // 当前编辑的螺旋分析被其他的螺旋分析正在使用 用于选字段禁用
            Pool.current_edit_etl_used = res.usedTables;
            if (res['used']) {
                BI.Msg.confirm(BI.i18nText("BI-Warning"), BI.i18nText("BI-ETL_Table_Edit_Warning"), function (v) {
                    if (v === true) {
                        callback(res);
                    }
                });
            } else {
                callback(res);
            }
        })
    },

    reqPreviewTable: function (data, callback) {
        data.sessionID = Data.SharingPool.get("sessionID");
        if (data[ETLCst.ITEMS] && data[ETLCst.ITEMS].length > 0 && data[ETLCst.ITEMS][0].operator) {
            data[ETLCst.ITEMS][0].operator.sessionID = Data.SharingPool.get("sessionID");
        }
        if (data[ETLCst.ITEMS][0][ETLCst.FIELDS].length === 0) {
            callback({
                value: []
            });
            return;
        }
        BI.requestAsync("fr_bi_analysis_etl", "preview_table", data, function (res) {
            callback(res);
        })
    },

    reqFieldValues: function (data, callback) {
        data.sessionID = Data.SharingPool.get("sessionID");
        BI.requestAsync("fr_bi_analysis_etl", "get_field_value", data, function (res) {
            callback(res);
        });
    },

    reqFieldMinMaxValues: function (data, callback) {
        data.sessionID = Data.SharingPool.get("sessionID");
        BI.requestAsync("fr_bi_analysis_etl", "get_field_min_max_value", data, function (res) {
            callback(res);
        });
    },

    reqTableStatus: function (data, callback) {
        data.sessionID = Data.SharingPool.get("sessionID");
            BI.requestAsync("fr_bi_analysis_etl", "get_cube_status", data, function (res) {
                callback(res);
            });
    },

    reqAllAnalysisTableProcesses: function (data, callback) {
        data.sessionID = Data.SharingPool.get("sessionID");
        BI.requestAsync("fr_bi_analysis_etl", "check_all_table_status", data, function (res) {
            callback(res);
        });
    }

}