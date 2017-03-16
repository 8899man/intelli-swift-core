Data.Req = BIReq = {

    reqTableById: function (id, callback) {
        BI.requestAsync("fr_bi_base", "get_table", {id: id}, function (res) {
            callback(res);
        });
    },

    reqTablesByPackId: function (packId, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_brief_tables_of_one_package", {id: packId}, function (res) {
            callback(res);
        }, complete)
    },

    reqSimpleTablesByPackId: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_simple_tables_of_one_package", data, function (res) {
            callback(res);
        }, complete)
    },

    reqTableInfoByTableId: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_table_info", data, function (res) {
            callback(res);
        }, complete);
    },

    reqReleaseTableLock: function (data) {
        BI.requestAsync("fr_bi_configure", "cancel_edit_table", data, BI.emptyFn, BI.emptyFn);
    },

    reqTablesDetailInfoByPackId: function (packName, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_detail_tables_of_one_package", {name: packName}, function (res) {
            callback(res);
        }, complete)
    },

    reqTableByConnSchemaTName: function (connName, schemaName, tableName, callback) {
        var args = {
            connection_name: connName,
            schema_name: schemaName,
            table_name: tableName
        };
        BI.requestAsync("fr_bi_configure", "get_table_field_by_table_info", args, function (res) {
            callback(res);
        })
    },

    reqConnectionName: function (callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_connection_names", "", function (res) {
            callback(res);
        }, complete);
    },

    reqTablesByConnectionName: function (connectionName, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_all_translated_tables_by_connection", {connectionName: connectionName}, function (res) {
            callback(res);
        }, complete);
    },

    reqTablesDetailInfoByTables: function (tables, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_field_info_4_new_tables", {tables: tables}, function (res) {
            callback(res);
        }, complete);
    },

    reqTablesDetailInfoByTables4Refresh: function (tables, callback, complete) {
        return BI.requestAsync("fr_bi_configure", "refresh_table_fields", {tables: tables}, function (res) {
            callback(res);
        }, complete);
    },

    reqWidgetSettingByData: function (data, callback, complete) {
        BI.requestAsync("fr_bi_dezi", "widget_setting", data, function (res) {
            callback(res);
        }, complete);
    },

    reqDeziNumberFieldMinMaxValueByfieldId: function (data, callback, complete) {
        BI.requestAsync("fr_bi_dezi", "dezi_get_field_min_max_value", data, function (res) {
            callback(res);
        }, complete);
    },

    reqFieldsDataByData: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_field_value", data, function (res) {
            callback(res);
        }, complete);
    },

    reqFieldsDataByFieldId: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_field_value_by_field_id", data, function (res) {
            callback(res);
        }, complete);
    },

    reqCubeStatusCheck: function (table, callback, complete) {
        BI.requestAsync("fr_bi_configure", "check_cube_table_status", {table: table}, function (res) {
            callback(res);
        }, complete)
    },

    reqIsTableExist: function (table, callback, complete) {
        BI.requestAsync("fr_bi_configure", "check_table_exist", {table: table}, function (res) {
            callback(res);
        }, complete);
    },

    reqPreviewDataByTableAndFields: function (table, fields, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_preview_table_conf", {table: table, fields: fields}, function (res) {
            callback(res);
        }, complete)
    },

    reqCircleLayerLevelInfoByTableAndCondition: function (table, layerInfo, callback, complete) {
        BI.requestAsync("fr_bi_configure", "create_fields_union", {
            table: table,
            id_field_name: layerInfo.id_field_name,
            parentid_field_name: layerInfo.parentid_field_name,
            divide_length: layerInfo.divide_length,
            fetch_union_length: layerInfo.fetch_union_length
        }, function (res) {
            callback(res);
        }, complete);
    },

    reqTestConnectionByLink: function (link, callback, complete) {
        BI.requestAsync("fr_bi_configure", "test_data_link", {linkData: link}, function (res) {
            callback(res);
        }, complete)
    },

    reqTestConnectionByLinkName: function (name, callback, complete) {
        BI.requestAsync("fr_bi_configure", "test_data_link_name", {name: name}, function (res) {
            callback(res);
        }, complete)
    },

    reqSchemasByLink: function (link, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_schemas_by_link", {linkData: link}, function (res) {
            callback(res);
        }, complete);
    },

    reqNumberFieldMaxMinValue: function (table, fieldName, callback, complete) {
        BI.requestAsync("fr_bi_configure", "number_max_min", {
            table: table,
            fieldName: fieldName
        }, function (res) {
            callback(res);
        }, complete)
    },

    reqTablesOfOnePackage: function (pId, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_tables_of_one_package", {
            id: pId
        }, function (res) {
            callback(res);
        }, complete)
    },

    reqFieldsOfOneTable: function (tableId, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_fields_of_one_table", {
            id: tableId
        }, function (res) {
            callback(res);
        }, complete)
    },

    reqUpdateRelation: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "update_relation", data, function () {
            callback();
        }, complete);
    },

    reqUpdateTablesOfOnePackage: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "update_tables_in_package", data, function (res) {
            callback(res);
        }, complete)
    },

    reqUpdateExcelTableCube: function (data, callback, complete) {
        BI.requestAsync("fr_bi_dezi", "update_excel_table_cube_by_table_id", data, function (res) {
            callback(res);
        }, complete)
    },

    reqGetExcelHTMLView: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_excel_html_view", data, function (res) {
            callback(res);
        }, complete)
    },

    reqDeziSaveFileGetExcelData: function (data, callback, complete) {
        BI.requestAsync("fr_bi_dezi", "dezi_save_file_get_excel_data", data, function (res) {
            callback(res);
        }, complete)
    },

    reqSaveFileGetExcelData: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "save_file_get_excel_data", data, function (res) {
            callback(res);
        }, complete)
    },

    reqSaveFileGetExcelViewData: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "save_file_get_excel_view_data", data, function (res) {
            callback(res);
        }, complete)
    },

    reqSaveFileGetExcelData: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "save_file_get_excel_data", data, function (res) {
            callback(res);
        }, complete)
    },

    reqExcelDataByFileName: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_excel_data_by_file_name", data, function (res) {
            callback(res);
        }, complete)
    },

    reqSaveDataLink: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "modify_data_link", data, function () {
            callback();
        }, complete)
    },

    reqAddNewTables: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "add_new_tables", data, function (res) {
            callback(res);
        }, complete);
    },

    reqRemoveTable: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "remove_table", data, function (res) {
            callback();
        }, complete);
    },

    reqUpdateOneTable: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "update_one_table", data, function (res) {
            callback();
        }, complete);
    },

    reqUpdatePackageName: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "update_package_name", data, function (res) {
            callback(res);
        }, complete);
    },

    reqCubePath: function (callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_cube_path", {}, function (res) {
            callback(res.cubePath);
        }, complete)
    },

    reqCheckCubePath: function (path, callback, complete) {
        BI.requestAsync("fr_bi_configure", "check_cube_path", {fileName: path}, function (res) {
            callback(res.cubePath);
        }, complete)
    },

    reqSaveCubePath: function (path, callback, complete) {
        BI.requestAsync("fr_bi_configure", "set_cube_path", {fileName: path}, function (res) {
            callback(res);
        }, complete)
    },

    reqSaveLoginField: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "save_login_field", data, function (res) {
            callback();
        }, complete)
    },

    reqServerSetPreviewBySql: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "preview_server_link", data, function (res) {
            callback(res);
        }, complete)
    },

    reqAllTemplates: function (callback, complete) {
        BI.requestAsync('fr_bi', 'get_folder_report_list_4_reuse', {}, function (items) {
            callback(items);
        }, complete)
    },

    reqAllReportsData: function (callback, complete) {
        BI.requestAsync("fr_bi", "get_all_reports_data", {}, function (data) {
            callback(data);
        }, complete);
    },

    reqWidgetsByTemplateId: function (tId, callback, complete) {
        BI.requestAsync("fr_bi", "get_widget_from_template", {id: tId}, function (data) {
            callback(data);
        }, complete);
    },

    reqTranslationsRelationsFields: function (callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_translations_relations_fields_4_conf", {}, function (data) {
            callback(data);
        }, complete)
    },

    reqUpdatePreviewSqlResult: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_preview_table_update", data, function (res) {
            callback(res);
        }, complete)
    },

    reqUpdateSettingById: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_update_setting", data, function (res) {
            callback(res);
        }, complete);
    },

    reqModifyUpdateSetting: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "modify_update_setting", data, function (res) {
            callback(res);
        }, complete)
    },

    reqCubeLog: function (callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_cube_log", {}, function (res) {
            callback(res);
        }, complete)
    },

    reqSavePackageAuthority: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "save_package_authority", data, function (res) {
            callback(res);
        }, complete);
    },

    reqAllBusinessPackages: function (callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_all_business_packages", {}, function (res) {
            callback(res);
        }, complete);
    },

    getTableNamesOfAllPackages: function (callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_table_names_of_all_packages", {}, function (res) {
            callback(res);
        }, complete);
    },

    reqGenerateCubeByTable: function (tableInfo, callback, complete) {
        BI.requestAsync("fr_bi_configure", "set_cube_generate", {
                baseTableSourceId: tableInfo.baseTable.md5,
                isETL: tableInfo.isETL,
                // tableId: BI.isNull(tableInfo.ETLTable) ? "" : tableInfo.ETLTable.id,
                tableId: tableInfo.isETL ? tableInfo.ETLTable.id : tableInfo.baseTable.id,
                updateType: tableInfo.updateType
            },
            function (res) {
                callback(res);
            }, complete)
        ;
    },
    reqGenerateCube: function (callback, complete) {
        BI.requestAsync("fr_bi_configure", "set_cube_generate", {}, function (res) {
            callback(res);
        }, complete);
    },

    reqPrimaryTablesByTable: function (table, callback, complete) {
        BI.requestAsync("fr_bi_configure", "get_primary_tables_by_table", table, function (res) {
            callback(res);
        }, complete);
    },

    reqGetChartPreStyle: function () {
        return BI.requestSync('fr_bi_base', 'get_config_setting', null);
    },

    reqCheckTableInUse: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "remove_table_in_use_check", data, function (res) {
            callback(res);
        }, complete);
    },

    reqUpdateTablesTranOfPackage: function (data, callback, complete) {
        BI.requestAsync("fr_bi_configure", "update_tables_tran_of_package", data, function (res) {
            callback(res);
        }, complete);
    }
};
