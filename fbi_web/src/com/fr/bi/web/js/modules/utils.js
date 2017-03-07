!(function () {
    //运行时用到的工具类
    BI.Utils = {};
    BI.extend(BI.Utils, {
        /**
         * lic相关
         */
        hasLicence: function () {
            return Data.SharingPool.get("reg", "hasLic");
        },

        supportBasic: function () {
            return Data.SharingPool.get("reg", "supportBasic");
        },

        supportBigData: function () {
            return Data.SharingPool.get("reg", "supportBigData");
        },

        supportCalculateTarget: function () {
            return Data.SharingPool.get("reg", "supportCalculateTarget");
        },

        supportDatabaseUnion: function () {
            return Data.SharingPool.get("reg", "supportDatabaseUnion");
        },

        supportExcelView: function () {
            return Data.SharingPool.get("reg", "supportExcelView");
        },

        supportGeneralControl: function () {
            return Data.SharingPool.get("reg", "supportGeneralControl");
        },

        supportIncrementUpdate: function () {
            return Data.SharingPool.get("reg", "supportIncrementUpdate");
        },

        supportMobileClient: function () {
            return Data.SharingPool.get("reg", "supportMobileClient");
        },

        supportMultiStatisticsWidget: function () {
            return Data.SharingPool.get("reg", "supportMultiStatisticsWidget");
        },

        supportOLAPTable: function () {
            return Data.SharingPool.get("reg", "supportOLAPTable");
        },

        supportReportShare: function () {
            return Data.SharingPool.get("reg", "supportReportShare");
        },

        supportSimpleControl: function () {
            return Data.SharingPool.get("reg", "supportSimpleControl");
        },

        getDefaultChartConfig: function () {
            return Data.SharingPool.cat("plateConfig");
        },

        getAllGroupedPackagesTreeJSON: function () {
            var groupMap = Pool.groups, packages = Pool.packages;
            var packStructure = [], groupedPacks = [];
            var groups = BI.sortBy(groupMap, function (id, item) {
                return item.init_time;
            });
            BI.each(groups, function (i, group) {
                packStructure.push({
                    id: group.id,
                    text: group.name,
                    isParent: true
                });
                BI.each(group.children, function (i, item) {
                    packStructure.push({
                        id: item.id,
                        text: packages[item.id].name,
                        value: item.id,
                        pId: group.id
                    });
                    groupedPacks.push(item.id);
                })
            });
            BI.each(packages, function (id, pack) {
                var isGrouped = false;
                BI.any(groupedPacks, function (i, pId) {
                    if (pId === id) {
                        isGrouped = true;
                        return false;
                    }
                });
                if (!isGrouped) {
                    packStructure.push({
                        id: pack.id,
                        text: pack.name,
                        value: pack.id
                    })
                }
            });
            return packStructure;
        },

        /**
         * 模板相关
         */
        getCurrentTemplateId: function () {
            return Data.SharingPool.get("reportId");
        },

        getWidgetsByTemplateId: function (tId, callback) {
            if (tId === this.getCurrentTemplateId()) {
                callback(Data.SharingPool.cat("widgets"));
            } else {
                Data.BufferPool.getWidgetsByTemplateId(tId, callback);
            }
        },

        getAllTemplates: function (callback) {
            Data.BufferPool.getAllTemplates(callback);
        },

        getAllReportsData: function (callback) {
            Data.Req.reqAllReportsData(function (data) {
                callback(data);
            });
        },

        /**
         * 业务包相关
         */
        getAllPackageIDs: function () {
            return BI.keys(Pool.packages);
        },

        getCurrentSelectPackageID: function () {
            var id = BI.Cache.getItem(BICst.CACHE.PACKAGE_PREFIX + this.getCurrentTemplateId());
            var ids = this.getAllPackageIDs();
            if (ids.contains(id)) {
                return id;
            }
            return ids[0];
        },

        setCurrentSelectPackageID: function (pId) {
            var key = BICst.CACHE.PACKAGE_PREFIX + this.getCurrentTemplateId();
            BI.Cache.setItem(key, pId);
        },

        getPackageNameByID: function (packageId) {
            if (BI.isNotNull(Pool.packages[packageId])) {
                return Pool.packages[packageId].name;
            }
        },

        getTableIDsOfPackageID: function (packageId) {
            if (BI.isNotNull(Pool.packages[packageId])) {
                return BI.pluck(Pool.packages[packageId].tables, "id");
            }
        },

        /**
         * 表相关
         */
        getTableNameByID: function (tableId) {
            var translations = Pool.translations;
            return translations[tableId];
        },

        getConnectionNameByTableId: function (tableId) {
            if (BI.isNotNull(Pool.tables[tableId])) {
                var connectionName = Pool.tables[tableId].connection_name;
                return connectionName || "";
            }
            return "";
        },

        getFieldsByTableId: function (tableId) {
            if (BI.isNotNull(Pool.tables[tableId])) {
                return Pool.tables[tableId].fields[0] || [];
            }
            return [];
        },

        getFieldIDsOfTableID: function (tableId) {
            if (BI.isNotNull(Pool.tables[tableId])) {
                var fields = Pool.tables[tableId].fields;
                return BI.pluck(fields[0].concat(fields[1]).concat(fields[2]), "id");
            }
        },

        getStringFieldIDsOfTableID: function (tableId) {
            var self = this;
            if (BI.isNotNull(Pool.tables[tableId])) {
                var fields = Pool.tables[tableId].fields;
                return BI.filter(BI.pluck(fields[0], "id"), function (idx, id) {
                    return self.getFieldTypeByID(id) === BICst.COLUMN.STRING;
                });
            }
            return [];
        },

        getNumberFieldIDsOfTableID: function (tableId) {
            var self = this;
            if (BI.isNotNull(Pool.tables[tableId])) {
                var fields = Pool.tables[tableId].fields;
                return BI.filter(BI.pluck(fields[0], "id"), function (idx, id) {
                    return self.getFieldTypeByID(id) === BICst.COLUMN.NUMBER;
                });
            }
            return [];
        },

        getDateFieldIDsOfTableID: function (tableId) {
            var self = this;
            if (BI.isNotNull(Pool.tables[tableId])) {
                var fields = Pool.tables[tableId].fields;
                return BI.filter(BI.pluck(fields[0], "id"), function (idx, id) {
                    return self.getFieldTypeByID(id) === BICst.COLUMN.DATE;
                });
            }
            return [];
        },

        getCountFieldIDsOfTableID: function (tableId) {
            if (BI.isNotNull(Pool.tables[tableId])) {
                var fields = Pool.tables[tableId].fields;
                return BI.pluck(fields[3], "id");
            }
            return [];
        },

        getSortedFieldIdsOfOneTableByTableId: function (tableId) {
            var translations = Pool.translations;
            var fieldIds = this.getFieldIDsOfTableID(tableId);
            var transIds = [];
            BI.each(fieldIds, function (i, fId) {
                if (BI.isNotNull(translations[fId])) {
                    transIds.push(fId);
                }
            });
            var filterFiledIds = [];
            BI.each(fieldIds, function (i, fId) {
                if (BI.isNull(translations[fId])) {
                    filterFiledIds.push(fId);
                }
            });
            var tNum = [], tString = [], tDate = [], fNum = [], fString = [], fDate = [];
            BI.each(transIds, function (i, id) {
                switch (BI.Utils.getFieldTypeByID(id)) {
                    case BICst.COLUMN.NUMBER:
                        tNum.push(id);
                        break;
                    case BICst.COLUMN.STRING:
                        tString.push(id);
                        break;
                    case BICst.COLUMN.DATE:
                        tDate.push(id);
                        break;
                }
            });
            BI.each(filterFiledIds, function (i, id) {
                switch (BI.Utils.getFieldTypeByID(id)) {
                    case BICst.COLUMN.NUMBER:
                        fNum.push(id);
                        break;
                    case BICst.COLUMN.STRING:
                        fString.push(id);
                        break;
                    case BICst.COLUMN.DATE:
                        fDate.push(id);
                        break;
                }
            });
            return tNum.concat(tString).concat(tDate).concat(fNum).concat(fString).concat(fDate);
        },

        getExcelViewByTableId: function (tableId) {
            var views = Pool.excel_views || {};
            return views[tableId];
        },

        isSelfCircleTableByTableId: function (tableId) {
            var paths = BI.Utils.getPathsFromTableAToTableB(tableId, tableId);
            if (paths.length === 0) {
                return false;
            }
            return !BI.find(paths, function (idx, path) {
                return path.length > 1;
            });
        },

        /**
         * 字段相关
         */
        getFieldNameByID: function (fieldId) {
            var translations = Pool.translations;
            var field = Pool.fields[fieldId];
            var fieldName = translations[fieldId];
            if (BI.isNull(fieldName) && BI.isNotNull(field)) {
                fieldName = field.field_name;
                if (field.field_type === BICst.COLUMN.COUNTER) {
                    //记录数 表名+"记录数"fbi_Records
                    var tableName = this.getTableNameByID(field.table_id);
                    fieldName = tableName + BI.i18nText("BI-Records");
                }
            }
            return fieldName;
        },

        getOriginalFieldNameByID: function (fieldId) {
            var field = Pool.fields[fieldId];
            if (BI.isNotNull(field)) {
                return field.field_name;
            }
        },

        getFieldTypeByID: function (fieldId) {
            if (BI.isNotNull(Pool.fields[fieldId])) {
                return Pool.fields[fieldId].field_type;
            }
        },

        getFieldIsUsableByID: function (fieldId) {
            if (BI.isNotNull(Pool.fields[fieldId])) {
                return Pool.fields[fieldId].is_usable;
            }
        },

        getFieldIsCircleByID: function (fieldId) {
            if (BI.isNotNull(Pool.fields[fieldId])) {
                return Pool.fields[fieldId].isCircle;
            }
        },

        getTableIdByFieldID: function (fieldId) {
            if (BI.isNotNull(Pool.fields[fieldId])) {
                return Pool.fields[fieldId].table_id;
            }
        },

        getAllFieldIDs: function () {
            return BI.keys(Pool.fields);
        },


        /**
         * 组件相关
         */
        getAllWidgetIDs: function () {
            return BI.keys(Data.SharingPool.get("widgets"));
        },

        getWidgetBoundsByID: function (wid) {
            return Data.SharingPool.get("widgets", wid, "bounds") || {};
        },

        getWidgetLinkageByID: function (wid) {
            return Data.SharingPool.get("widgets", wid, "linkages") || [];
        },

        getWidgetViewByID: function (wid) {
            return Data.SharingPool.get("widgets", wid, "view") || {};
        },

        getWidgetTypeByID: function (wid) {
            return Data.SharingPool.get("widgets", wid, "type");
        },

        getWidgetSubTypeByID: function (wid) {
            return Data.SharingPool.get("widgets", wid, "sub_type");
        },

        getWidgetNameByID: function (wid) {
            var widget = Data.SharingPool.cat("widgets", wid);
            if (BI.isNotNull(widget)) {
                return Data.SharingPool.get("widgets", wid, "name");
            }
        },

        getWidgetValueByID: function (wid) {
            var widget = Data.SharingPool.cat("widgets", wid);
            if (BI.isNotNull(widget)) {
                return Data.SharingPool.get("widgets", wid, "value");
            }
        },

        getAllLinkageFromIdsByID: function (wid) {
            var self = this, fromIds = [];
            var linkages = this.getWidgetLinkageByID(wid);
            BI.each(linkages, function (i, link) {
                fromIds.push(link.from);
                fromIds = fromIds.concat(self.getAllLinkageFromIdsByID(link.to));
            });
            return fromIds;
        },

        checkWidgetNameByID: function (name, wId) {
            var allWIds = this.getAllWidgetIDs();
            var self = this, isValid = true;
            BI.some(allWIds, function (i, id) {
                if (self.isControlWidgetByWidgetId(id) === self.isControlWidgetByWidgetId(wId)
                    && self.getWidgetNameByID(id) === name
                    && wId !== id) {
                    isValid = false;
                    return true;
                }
            });
            return isValid;
        },

        isControlWidgetByWidgetId: function (wid) {
            var widgetType = this.getWidgetTypeByID(wid);
            return widgetType === BICst.WIDGET.STRING ||
                widgetType === BICst.WIDGET.NUMBER ||
                widgetType === BICst.WIDGET.DATE ||
                widgetType === BICst.WIDGET.MONTH ||
                widgetType === BICst.WIDGET.QUARTER ||
                widgetType === BICst.WIDGET.TREE ||
                widgetType === BICst.WIDGET.YEAR ||
                widgetType === BICst.WIDGET.YMD ||
                widgetType === BICst.WIDGET.GENERAL_QUERY;
        },

        isControlWidgetByWidgetType: function (widgetType) {
            return widgetType === BICst.WIDGET.STRING ||
                widgetType === BICst.WIDGET.NUMBER ||
                widgetType === BICst.WIDGET.DATE ||
                widgetType === BICst.WIDGET.MONTH ||
                widgetType === BICst.WIDGET.QUARTER ||
                widgetType === BICst.WIDGET.TREE ||
                widgetType === BICst.WIDGET.YEAR ||
                widgetType === BICst.WIDGET.YMD ||
                widgetType === BICst.WIDGET.GENERAL_QUERY;
        },

        isQueryControlExist: function () {
            var self = this, isQueryExist = false;
            BI.some(this.getAllWidgetIDs(), function (i, wId) {
                if (self.getWidgetTypeByID(wId) === BICst.WIDGET.QUERY) {
                    return isQueryExist = true;
                }
            });
            return isQueryExist;
        },

        getWidgetDimensionsByID: function (wid) {
            return Data.SharingPool.get("widgets", wid, "dimensions") || {};
        },

        getWidgetSortByID: function (wid) {
            return Data.SharingPool.get("widgets", wid, "sort") || {};
        },

        getWidgetSortSequenceByID: function (wid) {
            return Data.SharingPool.get("widgets", wid, "sort_sequence") || [];
        },

        isShowWidgetRealDataByID: function (wid) {
            return Data.SharingPool.get("widgets", wid, "real_data");
        },

        //获取指定widget的拷贝,拷贝信息只包含widget的自身信息，如维度指标及其相关属性
        //不包含widge间的信息,如widget间的联动什么的
        getWidgetCopyByID: function (wid) {
            var self = this;
            var widget = Data.SharingPool.get("widgets", wid);
            if (BI.isNotNull(widget)) {
                var obj = {};
                obj.type = widget.type;
                obj.name = BI.Func.createDistinctName(Data.SharingPool.get("widgets"), widget.name);
                var dimTarIdMap = {};
                var dimensions = {};
                var view = {};
                BI.each(widget.dimensions, function (idx) {
                    var copy = createDimensionsAndTargets(idx);
                    dimensions[copy.id] = copy.dimension;
                });
                BI.each(widget.view, function (region, dimIds) {
                    view[region] = [];
                    BI.each(dimIds, function (idx, dId) {
                        view[region].push(dimTarIdMap[dId]);
                    });
                });
                obj.dimensions = dimensions;
                obj.view = view;
                obj.bounds = {
                    height: widget.bounds.height,
                    width: widget.bounds.width,
                    left: widget.bounds.left,
                    top: widget.bounds.top
                };
                obj.settings = widget.settings;
                obj.value = widget.value;
                //组件表头上指标的排序和过滤
                //BI-3341 看测试哪边的数据不知道为什么表头上的sort存了个空对象，而实际上对表头指标选择排序方式无论如何也不会出现空对象
                //先在这边加个判断
                if (BI.has(widget, "sort") && BI.isNotNull(widget.sort) && BI.isNotEmptyObject(widget.sort)) {
                    obj.sort = BI.extend({}, widget.sort, {
                        sort_target: createDimensionsAndTargets(widget.sort.sort_target).id
                    })
                }

                if (BI.has(widget, "sort_sequence") && BI.isNotNull(widget.sort_sequence)) {
                    obj.sort_sequence = [];
                    BI.each(widget.sort_sequence, function (idx, dId) {
                        obj.sort_sequence.push(createDimensionsAndTargets(dId).id);
                    })
                }

                if (BI.has(widget, "filter_value") && BI.isNotNull(widget.filter_value)) {
                    var filterValue = {};
                    BI.each(widget.filter_value, function (target_id, filter_value) {
                        var newId = createDimensionsAndTargets(target_id).id;
                        filterValue[newId] = checkFilter(filter_value, target_id, newId);
                    });
                    obj.filter_value = filterValue;
                }

                return obj;
            }

            function checkFilter(oldFilter, dId, newId) {
                var filter = {};
                var filterType = oldFilter.filter_type, filterValue = oldFilter.filter_value;
                filter.filter_type = oldFilter.filter_type;
                if (filterType === BICst.FILTER_TYPE.AND || filterType === BICst.FILTER_TYPE.OR) {
                    filter.filter_value = [];
                    BI.each(filterValue, function (i, value) {
                        filter.filter_value.push(checkFilter(value, dId, newId));
                    });
                } else {
                    BI.extend(filter, oldFilter);
                    //防止死循环
                    if (BI.has(oldFilter, "target_id")) {
                        if (oldFilter.target_id !== dId) {
                            var result = createDimensionsAndTargets(oldFilter.target_id);
                            filter.target_id = result.id;
                        } else {
                            filter.target_id = newId;
                        }
                    }
                    //维度公式过滤所用到的指标ID也要替换掉
                    if (BI.has(oldFilter, "formula_ids")) {
                        var ids = oldFilter.formula_ids || [];
                        if (BI.isNotEmptyArray(ids) && BI.isNull(BI.Utils.getFieldTypeByID(ids[0]))) {
                            BI.each(ids, function (id, tId) {
                                var result = createDimensionsAndTargets(tId);
                                filter.filter_value = filter.filter_value.replaceAll(tId, result.id);
                                filter.formula_ids[id] = result.id;
                            });
                        }
                    }
                }
                return filter;
            }

            function createDimensionsAndTargets(idx) {
                var newId = dimTarIdMap[idx] || BI.UUID();
                var dimension = BI.deepClone(widget.dimensions[idx]);
                if (BI.has(dimTarIdMap, idx) && BI.has(dimensions, [dimTarIdMap[idx]])) {
                    return {id: dimTarIdMap[idx], dimension: dimensions[dimTarIdMap[idx]]};
                }
                switch (widget.dimensions[idx].type) {
                    case BICst.TARGET_TYPE.STRING:
                    case BICst.TARGET_TYPE.NUMBER:
                    case BICst.TARGET_TYPE.DATE:
                        if (BI.has(widget.dimensions[idx], "dimension_map")) {
                            dimension.dimension_map = {};
                            BI.each(widget.dimensions[idx].dimension_map, function (id, map) {
                                //明细表dimensionMap存的key是tableId，与汇总表区分
                                if (self.isDimensionExist(id)) {
                                    var result = createDimensionsAndTargets(id);
                                    dimension.dimension_map[result.id] = map;
                                } else {
                                    dimension.dimension_map[id] = map;
                                }
                            });
                        }
                        if (BI.has(widget.dimensions[idx], "filter_value") && BI.isNotNull(widget.dimensions[idx].filter_value)) {
                            dimension.filter_value = checkFilter(widget.dimensions[idx].filter_value, dimTarIdMap[idx] || idx, newId);
                        }
                        if (BI.has(widget.dimensions[idx], "sort")) {
                            dimension.sort = BI.deepClone(widget.dimensions[idx].sort);
                            if (BI.has(dimension.sort, "sort_target")) {
                                if (dimension.sort.sort_target === idx) {
                                    dimension.sort.sort_target = newId;
                                } else {
                                    var result = createDimensionsAndTargets(dimension.sort.sort_target);
                                    dimension.sort.sort_target = result.id;
                                }
                            }
                        }
                        break;
                    case BICst.TARGET_TYPE.FORMULA:
                    case BICst.TARGET_TYPE.YEAR_ON_YEAR_RATE:
                    case BICst.TARGET_TYPE.MONTH_ON_MONTH_RATE:
                    case BICst.TARGET_TYPE.YEAR_ON_YEAR_VALUE:
                    case BICst.TARGET_TYPE.MONTH_ON_MONTH_VALUE:
                    case BICst.TARGET_TYPE.SUM_OF_ABOVE:
                    case BICst.TARGET_TYPE.SUM_OF_ABOVE_IN_GROUP:
                    case BICst.TARGET_TYPE.SUM_OF_ALL:
                    case BICst.TARGET_TYPE.SUM_OF_ALL_IN_GROUP:
                    case BICst.TARGET_TYPE.RANK:
                    case BICst.TARGET_TYPE.RANK_IN_GROUP:
                        var expression = dimension._src.expression;
                        BI.each(expression.ids, function (id, tId) {
                            var result = createDimensionsAndTargets(tId);
                            if (BI.has(expression, "formula_value")) {
                                expression.formula_value = expression.formula_value.replaceAll(tId, result.id);
                            }
                            expression.ids[id] = result.id;
                        });
                        break;
                }
                dimension.dId = newId;
                dimTarIdMap[idx] = newId;
                return {id: newId, dimension: dimension};
            }
        },

        //global style ---- start ----
        getGlobalStyle: function () {
            return Data.SharingPool.get("globalStyle") || {};
        },

        getGSMainBackground: function () {
            var gs = this.getGlobalStyle();
            return gs.mainBackground;
        },

        getGSWidgetBackground: function () {
            var gs = this.getGlobalStyle();
            return gs.widgetBackground;
        },

        getGSChartFont: function () {
            var gs = this.getGlobalStyle();
            return BI.extend(gs.chartFont, {
                "fontFamily": "Microsoft YaHei, Hiragino Sans GB W3",
                "fontSize": "12px"
            });
        },

        getGSTitleBackground: function () {
            var gs = this.getGlobalStyle();
            return gs.titleBackground;
        },

        getGSTitleFont: function () {
            var gs = this.getGlobalStyle();
            return gs.titleFont;
        },

        getGSNamePos: function () {
            var titleFont = this.getGSTitleFont();
            if (BI.isNotNull(titleFont)) {
                if (titleFont["text-align"] === "left") {
                    return BICst.DASHBOARD_WIDGET_NAME_POS_LEFT
                }
                if (titleFont["text-align"] === "center") {
                    return BICst.DASHBOARD_WIDGET_NAME_POS_CENTER
                }
            }
            return BICst.DASHBOARD_WIDGET_NAME_POS_LEFT
        },

        //global style ---- end ----

        getCalculateValue: function (did) {
            return Data.SharingPool.get("calculateValue", did) || []
        },

        //settings  ---- start ----
        getWSTableFormByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.table_form) ? ws.table_form :
                BICst.DEFAULT_CHART_SETTING.table_form;
        },

        getWSThemeColorByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.theme_color) ? ws.theme_color :
                BICst.DEFAULT_CHART_SETTING.theme_color;
        },

        getWSTableStyleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.table_style) ? ws.table_style :
                BICst.DEFAULT_CHART_SETTING.table_style;
        },

        getWSShowNumberByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_number) ? ws.show_number :
                BICst.DEFAULT_CHART_SETTING.show_number;
        },

        getWSShowRowTotalByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_row_total) ? ws.show_row_total :
                BICst.DEFAULT_CHART_SETTING.show_row_total;
        },

        getWSShowColTotalByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_col_total) ? ws.show_col_total :
                BICst.DEFAULT_CHART_SETTING.show_col_total;
        },

        getWSOpenRowNodeByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.open_row_node) ? ws.open_row_node :
                BICst.DEFAULT_CHART_SETTING.open_row_node;
        },

        getWSOpenColNodeByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.open_col_node) ? ws.open_col_node :
                BICst.DEFAULT_CHART_SETTING.open_col_node;
        },

        getWSMaxRowByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.max_row) ? ws.max_row :
                BICst.DEFAULT_CHART_SETTING.max_row;
        },

        getWSMaxColByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.max_col) ? ws.max_col :
                BICst.DEFAULT_CHART_SETTING.max_col;
        },

        getWSFreezeDimByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.freeze_dim) ? ws.freeze_dim :
                BICst.DEFAULT_CHART_SETTING.freeze_dim;
        },

        getWSFreezeFirstColumnById: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.freeze_first_column) ? ws.freeze_first_column :
                BICst.DEFAULT_CHART_SETTING.freeze_first_column;
        },

        getWSShowRulesByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.rules_display) ? ws.rules_display :
                BICst.DEFAULT_CHART_SETTING.bubble_display;
        },

        getWSBubbleStyleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.bubble_style) ? ws.bubble_style :
                BICst.DEFAULT_CHART_SETTING.bubble_style;
        },

        getWSTransferFilterByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.transfer_filter) ? ws.transfer_filter :
                BICst.DEFAULT_CHART_SETTING.transfer_filter;
        },

        getWSShowNameByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_name) ? ws.show_name :
                BICst.DEFAULT_CHART_SETTING.show_name;
        },

        getWSNamePosByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            var gsNamePos = this.getGSNamePos();
            return ws.name_pos
                || gsNamePos
                || BICst.DEFAULT_CHART_SETTING.name_pos;
        },

        getWSColumnSizeByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.column_size) ? ws.column_size : [];
        },

        getWSChartColorByID: function (wid) {
            var self = this;

            function getDefaultColor() {
                var defaultChartConfig = self.getDefaultChartConfig();
                var type = defaultChartConfig.defaultColor;
                if (BI.isKey(type)) {
                    var finded = BI.find(defaultChartConfig.styleList, function (i, style) {
                        return style.value === type;
                    });
                    if (finded) {
                        return finded.colors;
                    }
                }
                if (defaultChartConfig.styleList.length > 0) {
                    return defaultChartConfig.styleList[0].colors;
                }
            }

            var gs = this.getGlobalStyle();
            var ws = this.getWidgetSettingsByID(wid);
            return ws.chart_color
                || gs.chartColor
                || getDefaultColor()
                || BICst.DEFAULT_CHART_SETTING.chart_color;
        },

        getWSChartStyleByID: function (wid) {
            var self = this;

            function getChartStyle() {
                var defaultChartConfig = self.getDefaultChartConfig();
                return defaultChartConfig.chartStyle;
            }

            var ws = this.getWidgetSettingsByID(wid);
            var chartStyle;
            if (BI.isNotNull(ws.chart_style)) {
                return ws.chart_style;
            }
            var gs = this.getGlobalStyle();
            if (BI.isNotNull(gs.chartStyle)) {
                return gs.chartStyle
            }
            if (BI.isNotNull(chartStyle = getChartStyle())) {
                return chartStyle;
            }
            return BICst.DEFAULT_CHART_SETTING.chart_style;
        },

        getWSChartLineTypeByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.chart_line_type) ? ws.chart_line_type :
                BICst.DEFAULT_CHART_SETTING.chart_line_type;
        },

        getWSChartPieTypeByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.chart_pie_type) ? ws.chart_pie_type :
                BICst.DEFAULT_CHART_SETTING.chart_pie_type;
        },

        getWSChartRadarTypeByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.chart_radar_type) ? ws.chart_radar_type :
                BICst.DEFAULT_CHART_SETTING.chart_radar_type;
        },

        getWSChartDashboardTypeByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.chart_dashboard_type) ? ws.chart_dashboard_type :
                BICst.DEFAULT_CHART_SETTING.chart_dashboard_type;
        },

        getWSChartTotalAngleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.chart_total_angle) ? ws.chart_total_angle :
                BICst.DEFAULT_CHART_SETTING.chart_total_angle;
        },

        getWSChartInnerRadiusByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.chart_inner_radius) ? ws.chart_inner_radius :
                BICst.DEFAULT_CHART_SETTING.chart_inner_radius;
        },

        getWSLeftYAxisStyleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.left_y_axis_style) ? ws.left_y_axis_style :
                BICst.DEFAULT_CHART_SETTING.left_y_axis_style;
        },

        getWSXAxisStyleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.x_axis_style) ? ws.x_axis_style :
                BICst.DEFAULT_CHART_SETTING.x_axis_style;
        },

        getWSRightYAxisStyleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.right_y_axis_style) ? ws.right_y_axis_style :
                BICst.DEFAULT_CHART_SETTING.right_y_axis_style;
        },

        getWSRightYAxis2StyleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.right_y_axis_second_style) ? ws.right_y_axis_second_style :
                BICst.DEFAULT_CHART_SETTING.right_y_axis_second_style;
        },

        getWSRightYAxisNumLevelByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.right_y_axis_number_level) ? ws.right_y_axis_number_level :
                BICst.DEFAULT_CHART_SETTING.right_y_axis_number_level;
        },

        getWSRightYAxis2NumLevelByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.right_y_axis_second_number_level) ? ws.right_y_axis_second_number_level :
                BICst.DEFAULT_CHART_SETTING.right_y_axis_second_number_level;
        },

        getWSLeftYAxisNumLevelByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.left_y_axis_number_level) ? ws.left_y_axis_number_level :
                BICst.DEFAULT_CHART_SETTING.left_y_axis_number_level;
        },

        getWSNumberOfPointerByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.number_of_pointer) ? ws.number_of_pointer :
                BICst.POINTER.ONE;
        },

        getWSScaleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.auto_custom) ? ws.auto_custom :
                BICst.SCALE_SETTING.AUTO
        },

        getWSDashboardStylesByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.style_conditions) ? ws.style_conditions :
                BICst.DASHBOARD_STYLE_CONDITIONS
        },

        getWSMapStylesByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.map_styles) ? ws.map_styles :
                BICst.MAP_STYLE_CONDITIONS
        },


        getWSDashboardNumLevelByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.dashboard_number_level) ? ws.dashboard_number_level :
                BICst.DEFAULT_CHART_SETTING.dashboard_number_level;
        },

        getWSXAxisNumLevelByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.x_axis_number_level) ? ws.x_axis_number_level :
                BICst.DEFAULT_CHART_SETTING.x_axis_number_level;
        },

        getWSLeftYAxisUnitByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.left_y_axis_unit) ? ws.left_y_axis_unit :
                BICst.DEFAULT_CHART_SETTING.left_y_axis_unit;
        },

        getWSDashboardUnitByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.dashboard_unit) ? ws.dashboard_unit :
                BICst.DEFAULT_CHART_SETTING.dashboard_unit;
        },

        getWSMinScaleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.min_scale) ? ws.min_scale :
                BICst.DEFAULT_CHART_SETTING.min_scale
        },

        getWSMaxScaleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.max_scale) ? ws.max_scale :
                BICst.DEFAULT_CHART_SETTING.max_scale
        },

        getWSShowPercentageByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_percentage) ? ws.show_percentage :
                BICst.DEFAULT_CHART_SETTING.percentage_not_show
        },

        getWSXAxisUnitByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.x_axis_unit) ? ws.x_axis_unit :
                BICst.DEFAULT_CHART_SETTING.x_axis_unit;
        },

        getWSRightYAxisUnitByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.right_y_axis_unit) ? ws.right_y_axis_unit :
                BICst.DEFAULT_CHART_SETTING.right_y_axis_unit;
        },

        getWSRightYAxis2UnitByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.right_y_axis_second_unit) ? ws.right_y_axis_second_unit :
                BICst.DEFAULT_CHART_SETTING.right_y_axis_second_unit;
        },

        getWSShowLeftYAxisTitleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_left_y_axis_title) ? ws.show_left_y_axis_title :
                BICst.DEFAULT_CHART_SETTING.show_left_y_axis_title;
        },

        getWSShowRightYAxisTitleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_right_y_axis_title) ? ws.show_right_y_axis_title :
                BICst.DEFAULT_CHART_SETTING.show_right_y_axis_title;
        },

        getWSShowRightYAxis2TitleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_right_y_axis_second_title) ? ws.show_right_y_axis_second_title :
                BICst.DEFAULT_CHART_SETTING.show_right_y_axis_second_title;
        },

        getWSLeftYAxisTitleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.left_y_axis_title) ? ws.left_y_axis_title :
                BICst.DEFAULT_CHART_SETTING.left_y_axis_title;
        },

        getWSRightYAxisTitleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.right_y_axis_title) ? ws.right_y_axis_title :
                BICst.DEFAULT_CHART_SETTING.right_y_axis_title;
        },

        getWSRightYAxis2TitleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.right_y_axis_second_title) ? ws.right_y_axis_second_title :
                BICst.DEFAULT_CHART_SETTING.right_y_axis_second_title;
        },

        getWSShowXAxisTitleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_x_axis_title) ? ws.show_x_axis_title :
                BICst.DEFAULT_CHART_SETTING.show_x_axis_title;
        },

        getWSXAxisTitleByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.x_axis_title) ? ws.x_axis_title :
                BICst.DEFAULT_CHART_SETTING.x_axis_title;
        },

        getWSLeftYAxisReversedByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.left_y_axis_reversed) ? ws.left_y_axis_reversed :
                BICst.DEFAULT_CHART_SETTING.left_y_axis_reversed;
        },

        getWSRightYAxisReversedByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.right_y_axis_reversed) ? ws.right_y_axis_reversed :
                BICst.DEFAULT_CHART_SETTING.right_y_axis_reversed;
        },

        getWSRightYAxis2ReversedByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.right_y_axis_second_reversed) ? ws.right_y_axis_second_reversed :
                BICst.DEFAULT_CHART_SETTING.right_y_axis_second_reversed;
        },

        getWSChartLegendByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.chart_legend) ? ws.chart_legend :
                BICst.DEFAULT_CHART_SETTING.chart_legend;
        },

        getWSShowDataLabelByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_data_label) ? ws.show_data_label :
                BICst.DEFAULT_CHART_SETTING.show_data_label;
        },

        getWSShowDataTableByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_data_table) ? ws.show_data_table :
                BICst.DEFAULT_CHART_SETTING.show_data_table;
        },

        getWSShowGridLineByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_grid_line) ? ws.show_grid_line :
                BICst.DEFAULT_CHART_SETTING.show_grid_line;
        },

        getWSNumberSeparatorsByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.num_separators) ? ws.num_separators :
                BICst.DEFAULT_CHART_SETTING.num_separators;
        },

        getWSRightNumberSeparatorsByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.right_num_separators) ? ws.right_num_separators :
                BICst.DEFAULT_CHART_SETTING.right_num_separators;
        },

        getWSRight2NumberSeparatorsByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.right2_num_separators) ? ws.right2_num_separators :
                BICst.DEFAULT_CHART_SETTING.right_num_separators;
        },

        getWSMinimalistByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.minimalist_model) ? ws.minimalist_model :
                BICst.DEFAULT_CHART_SETTING.minimalist_model
        },

        getWSShowCustomScale: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_custom_scale) ? ws.show_custom_scale :
                BICst.DEFAULT_CHART_SETTING.show_custom_scale;
        },

        getWSCustomScale: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.custom_scale) ? ws.custom_scale :
            {}
        },

        getWSShowZoomByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_zoom) ? ws.show_zoom :
                BICst.DEFAULT_CHART_SETTING.show_zoom;
        },

        getWSTextDirectionByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.text_direction) ? ws.text_direction :
                BICst.DEFAULT_CHART_SETTING.text_direction;
        },

        getWSShowBackgroundByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.show_background_layer) ? ws.show_background_layer :
                BICst.DEFAULT_CHART_SETTING.show_background_layer;
        },

        getWSBackgroundLayerInfoByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.background_layer_info) ? ws.background_layer_info :
                BICst.DEFAULT_CHART_SETTING.background_layer_info;
        },

        getWSNullContinueByID: function (wid) {
            var ws = this.getWidgetSettingsByID(wid);
            return BI.isNotNull(ws.null_continue) ? ws.null_continue :
                BICst.DEFAULT_CHART_SETTING.null_continue;
        },

        //settings  ---- end ----

        getWidgetSettingsByID: function (wid) {
            return Data.SharingPool.get("widgets", wid, "settings") || {};
        },

        getWidgetInitTimeByID: function (wid) {
            return Data.SharingPool.get("widgets", wid, "init_time") || new Date().getTime();
        },

        getClickedByID: function (wid) {
            return Data.SharingPool.get("widgets", wid, "clicked") || {};
        },

        getDrillByID: function (wid) {
            var self = this;
            var clicked = this.getClickedByID(wid);
            var drills = {};
            BI.each(clicked, function (dId, value) {
                if (self.isDimensionExist(dId) && self.isDimensionByDimensionID(dId)) {
                    drills[dId] = value;
                }
            });
            return drills;
        },

        //获取组件中所有维度的钻取链A->B->C
        getDrillList: function (wid) {
            var drillMap = BI.Utils.getDrillByID(wid);
            var map = {};
            BI.each(drillMap, function (drId, ds) {
                map[drId] = [];
                BI.each(ds, function (idx, obj) {
                    map[drId].push(obj.dId)
                });
            });
            return map;
        },

        getLinkageValuesByID: function (wid) {
            var self = this;
            var clicked = this.getClickedByID(wid);
            var drills = {};
            BI.each(clicked, function (dId, value) {
                if (self.isDimensionExist(dId) && !self.isDimensionByDimensionID(dId)) {
                    drills[dId] = value;
                }
            });
            return drills;
        },

        getDrillDownDIdsByWidgetId: function (wid) {
            var allDims = BI.Utils.getAllDimDimensionIDs(wid);
            var allUsedDims = BI.Utils.getAllUsableDimDimensionIDs(wid);
            var result = [];
            if (allDims.length > allUsedDims.length) {
                var drillMap = this.getDrillByID(wid);
                var drilledIds = [];
                BI.each(drillMap, function (drId, ds) {
                    BI.each(ds, function (i, drs) {
                        drilledIds.push(drs.dId);
                    });
                });
                BI.each(allDims, function (i, dim) {
                    if (!allUsedDims.contains(dim) && !drilledIds.contains(dim)) {
                        result.push(dim);
                    }
                });
            }
            return result;
        },

        getDrillUpDimensionIdByDimensionId: function (dId) {
            var widgetId = BI.Utils.getWidgetIDByDimensionID(dId);
            var allDims = BI.Utils.getAllDimDimensionIDs(widgetId);
            var allUsedDims = BI.Utils.getAllUsableDimDimensionIDs(widgetId);
            var updrillId = null;
            if (allDims.length > allUsedDims.length) {
                var drillMap = BI.Utils.getDrillByID(widgetId);
                BI.any(drillMap, function (drId, ds) {
                    if (ds.length > 0 && (dId === drId || ds[ds.length - 1].dId === dId)) {
                        if (ds.length > 1) {
                            updrillId = ds[ds.length - 2].dId;
                        } else {
                            updrillId = drId;
                        }
                        return true;
                    }
                });
            }
            return updrillId;
        },

        //根据text dId 获取clicked 处理分组的情况
        getClickedValue4Group: function (v, dId) {
            var group = this.getDimensionGroupByID(dId);
            var fieldType = this.getFieldTypeByDimensionID(dId);
            var clicked = v;

            if (BI.isNotNull(group)) {
                if (fieldType === BICst.COLUMN.STRING) {
                    var details = group.details,
                        ungroup2Other = group.ungroup2Other,
                        ungroup2OtherName = group.ungroup2OtherName;
                    if (ungroup2Other === BICst.CUSTOM_GROUP.UNGROUP2OTHER.SELECTED &&
                        ungroup2OtherName === v) {
                        clicked = BICst.UNGROUP_TO_OTHER;
                    }
                    BI.some(details, function (i, detail) {
                        if (detail.value === v) {
                            clicked = detail.id;
                            return true;
                        }
                    });
                } else if (fieldType === BICst.COLUMN.NUMBER) {
                    var groupValue = group.group_value, groupType = group.type;
                    if (groupType === BICst.GROUP.CUSTOM_NUMBER_GROUP) {
                        var groupNodes = groupValue.group_nodes, useOther = groupValue.use_other;
                        if (useOther === v) {
                            clicked = BICst.UNGROUP_TO_OTHER;
                        }
                        BI.some(groupNodes, function (i, node) {
                            if (node.group_name === v) {
                                clicked = node.id;
                                return true;
                            }
                        });
                    }
                }
            }
            return clicked;
        },

        getWidgetFilterValueByID: function (wid) {
            if (this.isWidgetExistByID(wid)) {
                return Data.SharingPool.get("widgets", wid, "filter_value") || {};
            }
            return {};
        },

        getAllDimensionIDs: function (wid) {
            if (!wid) {
                return BI.keys(Data.SharingPool.cat("dimensions"))
            }
            if (this.isWidgetExistByID(wid)) {
                return BI.keys(Data.SharingPool.cat("widgets", wid, "dimensions"));
            }
            return [];
        },

        getAllUsedFieldIds: function () {
            var allDIds = this.getAllDimensionIDs();
            var fields = [];
            BI.each(allDIds, function (i, dId) {
                fields.push(BI.Utils.getFieldIDByDimensionID(dId));
            });
            return fields;
        },

        isWidgetExistByID: function (wid) {
            return this.getAllWidgetIDs().contains(wid);
        },

        //是否所有数据存在（配置部分将数据修改的情况）
        isAllFieldsExistByWidgetID: function (wid) {
            var self = this;
            var allDimIds = this.getAllDimensionIDs(wid);
            return !BI.some(allDimIds, function (i, dId) {
                return checkDimension(dId)
            });

            function checkDimension(dId) {
                var dType = BI.Utils.getDimensionTypeByID(dId);
                if (dType === BICst.TARGET_TYPE.STRING ||
                    dType === BICst.TARGET_TYPE.NUMBER ||
                    dType === BICst.TARGET_TYPE.DATE ||
                    dType === BICst.TARGET_TYPE.COUNTER) {
                    var fieldId = BI.Utils.getFieldIDByDimensionID(dId);
                    if (BI.isNull(Pool.fields[fieldId])) {
                        return true;
                    }
                } else {
                    //计算指标
                    var expression = BI.Utils.getExpressionByDimensionID(dId);
                    var fIds = expression.ids;
                    return BI.some(fIds, function (j, fId) {
                        var dId = fId;
                        var id = BI.Utils.getFieldIDByDimensionID(dId);
                        if (BI.isNotNull(self.getDimensionTypeByID(dId))) {
                            checkDimension(dId)
                        } else if (BI.isNull(Pool.fields[id])) {
                            return false;
                        }

                    });
                }
            }
        },

        //获取某组件下所有的维度
        getAllDimDimensionIDs: function (wid) {
            var result = [];
            var views = Data.SharingPool.get("widgets", wid, "view");
            BI.each(views, function (i, dim) {
                if (i >= BI.parseInt(BICst.REGION.DIMENSION1) && i < (BI.parseInt(BICst.REGION.TARGET1))) {
                    result = result.concat(dim);
                }
            });
            return result;
        },
        //获取某组件下所有的指标
        getAllTargetDimensionIDs: function (wid) {
            var result = [];
            var views = Data.SharingPool.get("widgets", wid, "view");
            BI.each(views, function (i, tar) {
                if (i >= (BI.parseInt(BICst.REGION.TARGET1))) {
                    result = result.concat(tar);
                }
            });
            return result;
        },

        getAllBaseDimensionIDs: function (wid) {
            var self = this;
            var result = [];
            var ids = this.getAllDimensionIDs(wid);
            var _set = [BICst.TARGET_TYPE.STRING,
                BICst.TARGET_TYPE.NUMBER,
                BICst.TARGET_TYPE.DATE, BICst.TARGET_TYPE.COUNTER];
            BI.each(ids, function (i, id) {
                var type = self.getDimensionTypeByID(id);
                if (_set.contains(type)) {
                    result.push(id);
                }
            });
            return result;
        },

        getAllUsableDimensionIDs: function (wid) {
            var self = this, dimIds = [], usableIds = [];
            if (!wid) {
                dimIds = BI.keys(Data.SharingPool.get("dimensions"));
            } else {
                dimIds = BI.keys(Data.SharingPool.get("widgets", wid, "dimensions"));
            }
            BI.each(dimIds, function (i, dId) {
                self.isDimensionUsable(dId) && (usableIds.push(dId));
            });
            return usableIds;
        },

        //获取某组件下所有可用的维度
        getAllUsableDimDimensionIDs: function (wid) {
            var self = this, usableDims = [];
            var allDims = this.getAllDimDimensionIDs(wid);
            BI.each(allDims, function (i, dId) {
                self.isDimensionUsable(dId) && (usableDims.push(dId));
            });
            return usableDims;
        },

        //获取某组件下所有可用的指标
        getAllUsableTargetDimensionIDs: function (wid) {
            var self = this, usableDims = [];
            var allDims = this.getAllTargetDimensionIDs(wid);
            BI.each(allDims, function (i, dId) {
                self.isDimensionUsable(dId) && (usableDims.push(dId));
            });
            return usableDims;
        },

        /**
         * 维度相关
         */
        getWidgetIDByDimensionID: function (dId) {
            var self = this;
            if (!this._dimension2WidgetMap) {
                this._dimension2WidgetMap = {};
            }
            if (BI.isNotNull(this._dimension2WidgetMap[dId])) {
                return this._dimension2WidgetMap[dId];
            }
            var widgets = this.getAllWidgetIDs();
            var wid = BI.find(widgets, function (i, wid) {
                var dims = self.getAllDimensionIDs(wid);
                return BI.find(dims, function (j, id) {
                    return dId == id;
                })
            });
            this._dimension2WidgetMap[dId] = wid;
            return wid;
        },

        getDimensionNameByID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "name");
            }

        },

        isDimensionUsable: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "used");
            }
        },

        isDimensionExist: function (did) {
            return this.getAllDimensionIDs().contains(did);
        },

        getDimensionSortByID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "sort") || {};
            }
            return {};
        },

        getDimensionSrcByID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "_src") || {};
            }
            return {};
        },

        getDimensionGroupByID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "group") || {};
            }
            return {};

        },

        getDimensionTypeByID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "type");
            }

        },

        getDimensionFilterValueByID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "filter_value") || {};
            }
            return {};
        },

        getDimensionSettingsByID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "settings") || {};
            }
            return {};
        },

        getDimensionHyperLinkByID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "hyperlink") || {};
            }
            return {};
        },

        getFieldTypeByDimensionID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                var fieldId = this.getFieldIDByDimensionID(did);
                if (BI.isKey(fieldId)) {
                    return this.getFieldTypeByID(fieldId);
                }
                return BICst.COLUMN.NUMBER;
            }
        },


        getDimensionStyleOfChartByID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "style_of_chart");
            }

        },

        getDimensionCordonByID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "cordon");
            }

        },

        getDimensionPositionByID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "position");
            }

        },

        //获取维度或指标所对应的字段id
        getFieldIDByDimensionID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "_src", "field_id");
            }

        },

        //获得计算指标对应的公式内容
        getExpressionByDimensionID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "_src", "expression");
            }
        },

        //获取计算指标用到的指标IDs
        getExpressionValuesByDimensionID: function (dId) {
            var expression = this.getExpressionByDimensionID(dId);
            if (BI.isNotNull(expression)) {
                return expression.ids || [];
            }
            return [];
        },

        //获取维度或指标所对应的表id
        getTableIDByDimensionID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return BI.Utils.getTableIdByFieldID(BI.Utils.getFieldIDByDimensionID(did));
            }

        },

        getDimensionMapByDimensionID: function (did) {
            if (BI.isNotNull(Data.SharingPool.cat("dimensions", did))) {
                return Data.SharingPool.get("dimensions", did, "dimension_map") || {};
            }
            return {};
        },

        isDimensionByDimensionID: function (dId) {
            var wId = this.getWidgetIDByDimensionID(dId);
            var views = this.getWidgetViewByID(wId);
            var region = 0;
            BI.some(views, function (reg, view) {
                if (view.contains(dId)) {
                    region = reg;
                    return true;
                }
            });
            return BI.parseInt(region) >= BI.parseInt(BICst.REGION.DIMENSION1) &&
                BI.parseInt(BICst.REGION.TARGET1) > BI.parseInt(region);
        },

        isTargetByDimensionID: function (dId) {
            var wId = this.getWidgetIDByDimensionID(dId);
            var views = this.getWidgetViewByID(wId);
            var type = this.getDimensionTypeByID(dId);
            var _set = [BICst.TARGET_TYPE.STRING,
                BICst.TARGET_TYPE.NUMBER,
                BICst.TARGET_TYPE.DATE];
            var region = 0;
            BI.some(views, function (reg, view) {
                if (view.contains(dId)) {
                    region = reg;
                    return true;
                }
            });
            return BI.parseInt(region) >= BI.parseInt(BICst.REGION.TARGET1) && _set.contains(type);
        },

        isCalculateTargetByDimensionID: function (dId) {
            var wId = this.getWidgetIDByDimensionID(dId);
            var type = this.getDimensionTypeByID(dId);
            var _set = [BICst.TARGET_TYPE.FORMULA,
                BICst.TARGET_TYPE.MONTH_ON_MONTH_RATE,
                BICst.TARGET_TYPE.MONTH_ON_MONTH_VALUE,
                BICst.TARGET_TYPE.RANK,
                BICst.TARGET_TYPE.RANK_IN_GROUP,
                BICst.TARGET_TYPE.SUM_OF_ABOVE,
                BICst.TARGET_TYPE.SUM_OF_ABOVE_IN_GROUP,
                BICst.TARGET_TYPE.SUM_OF_ALL,
                BICst.TARGET_TYPE.SUM_OF_ALL_IN_GROUP,
                BICst.TARGET_TYPE.YEAR_ON_YEAR_RATE,
                BICst.TARGET_TYPE.YEAR_ON_YEAR_VALUE
            ];
            return _set.contains(type);
        },

        isCounterTargetByDimensionID: function (dId) {
            var wId = this.getWidgetIDByDimensionID(dId);
            var views = this.getWidgetViewByID(wId);
            var type = this.getDimensionTypeByID(dId);
            var _set = [BICst.TARGET_TYPE.COUNTER
            ];
            var region = 0;
            BI.some(views, function (reg, view) {
                if (view.contains(dId)) {
                    region = reg;
                    return true;
                }
            });
            return BI.parseInt(region) >= BI.parseInt(BICst.REGION.TARGET1) && _set.contains(type);
        },

        isSrcUsedBySrcID: function (srcId) {
            var result = BI.find(this.getAllDimensionIDs(), function (i, dId) {
                var src = Data.SharingPool.get("dimensions", dId, "_src");
                return src.id === srcId;
            });
            return BI.isNotNull(result);
        },

        getTargetRelationByDimensionID: function (dId, targetID) {
            var dimensionMap = BI.Utils.getDimensionMapByDimensionID(dId);
            var target_relation;
            if (BI.isNull(targetID)) {
                target_relation = BI.firstObject(dimensionMap).target_relation;
            } else {
                target_relation = dimensionMap[targetID].target_relation;
            }
            return target_relation;
        },

        getDimensionTypeByFieldID: function (fieldId) {
            var fieldType = BI.Utils.getFieldTypeByID(fieldId);
            switch (fieldType) {
                case BICst.COLUMN.STRING:
                    return BICst.TARGET_TYPE.STRING;
                case BICst.COLUMN.COUNTER:
                    return BICst.TARGET_TYPE.COUNTER;
                case BICst.COLUMN.NUMBER:
                    return BICst.TARGET_TYPE.NUMBER;
                case BICst.COLUMN.DATE:
                    return BICst.TARGET_TYPE.DATE;
                default:
                    return BICst.TARGET_TYPE.NUMBER;
            }
        },

        getRegionDimensionIdsByDimensionID: function (dId) {
            var wId = BI.Utils.getWidgetIDByDimensionID(dId);
            var view = BI.Utils.getWidgetViewByID(wId);
            var regionType = BI.findKey(view, function (regionType, dIds) {
                if (BI.contains(dIds, dId)) {
                    return true
                }
            });
            return view[regionType];
        },

        getRegionTypeByDimensionID: function (dId) {
            var wId = BI.Utils.getWidgetIDByDimensionID(dId);
            var view = BI.Utils.getWidgetViewByID(wId);
            return BI.findKey(view, function (regionType, dIds) {
                if (BI.contains(dIds, dId)) {
                    return true
                }
            });
        },

        getWidgetIconClsByWidgetId: function (wId) {
            var widgetType = BI.Utils.getWidgetTypeByID(wId);
            switch (widgetType) {
                case BICst.WIDGET.TABLE:
                    return "drag-group-icon";
                case BICst.WIDGET.CROSS_TABLE:
                    return "drag-cross-icon";
                case BICst.WIDGET.COMPLEX_TABLE:
                    return "drag-complex-icon";
                case BICst.WIDGET.DETAIL:
                    return "drag-detail-icon";
                case BICst.WIDGET.AXIS:
                    return "drag-axis-icon";
                case BICst.WIDGET.ACCUMULATE_AXIS:
                    return "drag-axis-accu-icon";
                case BICst.WIDGET.PERCENT_ACCUMULATE_AXIS:
                    return "drag-axis-percent-accu-icon";
                case BICst.WIDGET.COMPARE_AXIS:
                    return "drag-axis-compare-icon";
                case BICst.WIDGET.FALL_AXIS:
                    return "drag-axis-fall-icon";
                case BICst.WIDGET.BAR:
                    return "drag-bar-icon";
                case BICst.WIDGET.ACCUMULATE_BAR:
                    return "drag-bar-accu-icon";
                case BICst.WIDGET.COMPARE_BAR:
                    return "drag-bar-compare-icon";
                case BICst.WIDGET.PIE:
                    return "drag-pie-icon";
                case BICst.WIDGET.MAP:
                    return "drag-map-china-icon";
                case BICst.WIDGET.GIS_MAP:
                    return "drag-map-gis-icon";
                case BICst.WIDGET.DASHBOARD:
                    return "drag-dashboard-icon";
                case BICst.WIDGET.DONUT:
                    return "drag-donut-icon";
                case BICst.WIDGET.BUBBLE:
                    return "drag-bubble-icon";
                case BICst.WIDGET.FORCE_BUBBLE:
                    return "drag-bubble-force-icon";
                case BICst.WIDGET.SCATTER:
                    return "drag-scatter-icon";
                case BICst.WIDGET.RADAR:
                    return "drag-radar-icon";
                case BICst.WIDGET.ACCUMULATE_RADAR:
                    return "drag-radar-accu-icon";
                case BICst.WIDGET.LINE:
                    return "drag-line-icon";
                case BICst.WIDGET.AREA:
                    return "drag-area-icon";
                case BICst.WIDGET.ACCUMULATE_AREA:
                    return "drag-area-accu-icon";
                case BICst.WIDGET.PERCENT_ACCUMULATE_AREA:
                    return "drag-area-percent-accu-icon";
                case BICst.WIDGET.COMPARE_AREA:
                    return "drag-area-compare-icon";
                case BICst.WIDGET.RANGE_AREA:
                    return "drag-area-range-icon";
                case BICst.WIDGET.COMBINE_CHART:
                    return "drag-combine-icon";
                case BICst.WIDGET.MULTI_AXIS_COMBINE_CHART:
                    return "drag-combine-mult-icon";
                case BICst.WIDGET.FUNNEL:
                    return "drag-funnel-icon";
                case BICst.WIDGET.IMAGE:
                    return "drag-image-icon";
                case BICst.WIDGET.WEB:
                    return "drag-web-icon";
                case BICst.WIDGET.CONTENT:
                    return "drag-input-icon";

            }
        },


        //获取某维度或指标是否被其他维度或指标（计算指标）使用的指标
        getDimensionUsedByOtherDimensionsByDimensionID: function (dId) {
            var self = this;
            var wId = this.getWidgetIDByDimensionID(dId);
            var ids = [], result = [];
            switch (this.getWidgetTypeByID(wId)) {
                case BICst.WIDGET.DETAIL:
                    ids = this.getAllDimensionIDs(wId);
                    BI.each(ids, function (i, id) {
                        var tids = self.getExpressionValuesByDimensionID(id);
                        if (tids.contains(dId)) {
                            result.push(id);
                        }
                    });
                    return result;
                default:
                    if (this.isDimensionByDimensionID(dId)) {
                        return [];
                    }
                    ids = this.getAllTargetDimensionIDs(wId);
                    BI.each(ids, function (i, id) {
                        var tids = self.getExpressionValuesByDimensionID(id);
                        if (tids.contains(dId)) {
                            result.push(id);
                        }
                    });
                    return result;
            }
        },


        /**
         * 关联相关
         */
        getFirstRelationPrimaryIdFromRelations: function (path) {
            var relation = BI.first(path);
            var primaryId = "";
            if (BI.isNotNull(relation)) {
                primaryId = relation.primaryKey.field_id;
            }
            return primaryId;
        },

        getLastRelationForeignIdFromRelations: function (path) {
            var foreignId;
            var relation = BI.last(path);
            BI.isNotNull(relation) && (foreignId = relation.foreignKey.field_id);
            return foreignId;
        },

        getPrimaryIdFromRelation: function (relation) {
            var primaryId;
            BI.isNotNull(relation.primaryKey) && (primaryId = relation.primaryKey.field_id);
            return primaryId;
        },

        getForeignIdFromRelation: function (relation) {
            var foreignId;
            BI.isNotNull(relation.foreignKey) && (foreignId = relation.foreignKey.field_id);
            return foreignId;
        },

        getPrimaryRelationTablesByTableID: function (tableId) {
            var primaryTables = [];
            BI.each(Pool.foreignRelations[tableId], function (tId, relations) {
                if (relations.length > 0) {
                    primaryTables.push(tId);
                }
            });
            return BI.uniq(primaryTables);
        },

        getForeignRelationTablesByTableID: function (tableId) {
            var foreignTables = [];
            BI.each(Pool.relations[tableId], function (tId, relations) {
                if (relations.length > 0) {
                    foreignTables.push(tId);
                }
            });
            return BI.uniq(foreignTables);
        },

        getPathsFromTableAToTableB: function (from, to) {
            var relations = Pool.relations;
            if (BI.isNull(from) || BI.isNull(to)) {
                return [];
            }
            if (BI.isNull(relations[from])) {
                return [];
            }
            if (BI.isNull(relations[from][to])) {
                return [];
            }
            return removeCircleInPath();

            function removeCircleInPath() {
                var relationOrder = [];
                return BI.filter(relations[from][to], function (idx, path) {
                    var orders = [];
                    var hasCircle = BI.any(path, function (id, relation) {
                        var prev = BI.Utils.getTableIdByFieldID(BI.Utils.getPrimaryIdFromRelation(relation));
                        var last = BI.Utils.getTableIdByFieldID(BI.Utils.getForeignIdFromRelation(relation));
                        var result = BI.find(relationOrder, function (i, order) {
                            if (order[0] === last && order[1] === prev) {
                                return true;
                            }
                        });
                        orders.push([prev, last]);
                        return BI.isNotNull(result);
                    });
                    if (hasCircle === false) {
                        relationOrder = BI.concat(relationOrder, orders);
                    }
                    return hasCircle === false;
                });
            }

        },

        getPathsFromFieldAToFieldB: function (from, to) {
            var self = this;
            if (BI.isNull(from) || BI.isNull(to)) {
                return [];
            }
            var tableA = BI.Utils.getTableIdByFieldID(from);
            var tableB = BI.Utils.getTableIdByFieldID(to);
            var path = this.getPathsFromTableAToTableB(tableA, tableB);
            if (tableA === tableB) {        //同一张表
                return [[{
                    primaryKey: {field_id: from, table_id: self.getTableIdByFieldID(from)},
                    foreignKey: {field_id: to, table_id: self.getTableIdByFieldID(to)}
                }]]
            }
            return path;

            //获取自循环生成的层级所在的关联
            function getRelationOfselfCircle(from, to, paths) {
                return BI.find(paths, function (idx, path) {
                    return BI.find(path, function (id, relation) {
                        var foreignId = self.getForeignIdFromRelation(relation);
                        return foreignId === from || foreignId === to;
                    });
                })
            }

            //对自循环表检测路径合法依据：路径中的a个关联中是否存在外键为primKey
            function checkPathAvailable(paths, primKey, foreign) {
                var result = BI.find(paths, function (idx, path) {
                    return BI.find(path, function (id, relation) {
                        var foreignKey = self.getForeignIdFromRelation(relation);
                        return foreignKey === primKey || foreignKey === foreign;
                    });
                });
                return BI.isNull(result);
            }
        },

        getCommonPrimaryTablesByTableIDs: function (tableIds) {
            var self = this;
            var commonTables = [];
            var primaryTableMap = {};
            BI.each(tableIds, function (i, tableId) {
                primaryTableMap[tableId] = self.getPrimaryRelationTablesByTableID(tableId);
                primaryTableMap[tableId].splice(0, 0, tableId);
            });
            BI.each(primaryTableMap, function (id, primaryTables) {
                var primaryTableMapCp = BI.deepClone(primaryTableMap);
                primaryTables.push(id);
                BI.each(primaryTables, function (i, primaryTableId) {
                    var isCommonTable = true;
                    BI.findKey(primaryTableMapCp, function (id, tables) {
                        if (!BI.contains(tables, primaryTableId)) {
                            isCommonTable = false;
                            return true
                        }
                    });
                    if (isCommonTable === true) {
                        (!BI.contains(commonTables, primaryTableId)) && commonTables.push(primaryTableId);
                    }
                });
            });
            return commonTables;
        },

        getCommonForeignTablesByTableIDs: function (tableIds) {
            var self = this;
            var commonTables = [];
            var foreignTableMap = {};
            BI.each(tableIds, function (i, tableId) {
                foreignTableMap[tableId] = self.getForeignRelationTablesByTableID(tableId);
                foreignTableMap[tableId].splice(0, 0, tableId);
            });
            BI.each(foreignTableMap, function (id, foreignTables) {
                var foreignTableMapCp = BI.deepClone(foreignTableMap);
                foreignTables.push(id);
                BI.each(foreignTables, function (i, foreignTableId) {
                    var isCommonTable = true;
                    BI.findKey(foreignTableMapCp, function (id, tables) {
                        if (!BI.contains(tables, foreignTableId)) {
                            isCommonTable = false;
                            return true
                        }
                    });
                    if (isCommonTable === true) {
                        (!BI.contains(commonTables, foreignTableId)) && commonTables.push(foreignTableId);
                    }
                });
            });
            return commonTables;
        },

        //表是否属于相关子表和主表
        isTableInRelativeTables: function (tableIds, target) {
            var self = this;
            var commonIds = BI.Utils.getCommonForeignTablesByTableIDs(tableIds);
            return BI.some(commonIds, function (i, tId) {
                if (tId === target) {
                    return true;
                }
                var ids = self.getPrimaryRelationTablesByTableID(tId);
                if (ids.contains(target)) {
                    return true;
                }
            });
        },

        //获取相关的主表和子表，包括tableIds的公共子表以及这些子表的所有主表
        getRelativePrimaryAndForeignTableIDs: function (tableIds) {
            var self = this;
            var result = [];
            var commonIds = this.getCommonForeignTablesByTableIDs(tableIds);
            result = result.concat(commonIds);
            BI.each(commonIds, function (i, tId) {
                var ids = self.getPrimaryRelationTablesByTableID(tId);
                result = result.concat(ids);
            });
            return result;
        },

        /**
         * 数据相关
         */

        getPreviewTableDataByTableId: function (tableId, callback) {
            //构造一个明细表
            var self = this;
            var fields = this.getSortedFieldIdsOfOneTableByTableId(tableId);
            var dimensions = {}, view = {10000: []};
            BI.each(fields, function (i, fieldId) {
                var id = BI.UUID();
                var dimensionMap = {}, group = {};
                dimensionMap[tableId] = {
                    target_relation: []
                };
                if (self.getFieldTypeByID(fieldId) === BICst.COLUMN.DATE) {
                    group.type = BICst.GROUP.YMDHMS;
                }
                var dType = BICst.TARGET_TYPE.STRING;
                switch (self.getFieldTypeByID(fieldId)) {
                    case BICst.COLUMN.DATE:
                        dType = BICst.TARGET_TYPE.DATE;
                        break;
                    case BICst.COLUMN.NUMBER:
                        dType = BICst.TARGET_TYPE.NUMBER;
                }
                dimensions[id] = {
                    name: id,
                    _src: {
                        field_id: fieldId,
                        table_id: tableId
                    },
                    type: dType,
                    used: true,
                    dimension_map: dimensionMap,
                    group: group
                };
                view[10000].push(id);
            });
            var widget = {
                type: BICst.WIDGET.DETAIL,
                bounds: {
                    height: 0,
                    width: 0,
                    left: 0,
                    top: 0
                },
                name: "__StatisticWidget__" + BI.UUID(),
                page: 0,
                dimensions: dimensions,
                view: view
            };
            Data.Req.reqWidgetSettingByData({widget: widget}, function (res) {
                callback(res.data);
            });
        },

        getDataByFieldID: function (fid, callback) {
            var d = {
                type: BICst.WIDGET.TABLE,
                bounds: {
                    height: 0,
                    width: 0,
                    left: 0,
                    top: 0
                },
                name: "__StatisticWidget__" + BI.UUID(),
                page: -1,
                dimensions: {
                    1234567: {
                        name: "__Dimension__",
                        _src: {
                            field_id: fid
                        },
                        type: BICst.COLUMN.STRING,
                        used: true
                    }
                },
                view: {
                    10000: [1234567]
                }
            };
            Data.Req.reqWidgetSettingByData({widget: d}, function (data) {
                callback(BI.pluck(data.data.c, "n"));
            });
        },

        getNoGroupedDataByDimensionID: function (dId, callback) {
            var dimension = Data.SharingPool.get("dimensions", dId);
            dimension.group = {type: BICst.GROUP.ID_GROUP};
            dimension.filter_value = {};
            dimension.used = true;
            var dimensions = {};
            dimensions[dId] = dimension;
            var view = {};
            view[BICst.REGION.DIMENSION1] = [dId];

            var targetIds = this.getAllTargetDimensionIDs(this.getWidgetIDByDimensionID(dId));
            BI.each(targetIds, function (idx, targetId) {
                dimensions[targetId] = Data.SharingPool.get("dimensions", targetId);
                dimensions[targetId].filter_value = {};
                if (!BI.has(view, BICst.REGION.TARGET1)) {
                    view[BICst.REGION.TARGET1] = [];
                }
                view[BICst.REGION.TARGET1].push(targetId);
            });

            this.getWidgetDataByWidgetInfo(dimensions, view, function (data) {
                callback(BI.pluck(data.data.c, "n"));
            }, {page: BICst.TABLE_PAGE_OPERATOR.ALL_PAGE});

        },

        getDataByDimensionID: function (dId, callback) {
            var wid = this.getWidgetIDByDimensionID(dId);
            var dimension = Data.SharingPool.get("dimensions", dId);
            dimension.filter_value = {};
            dimension.used = true;
            var widget = Data.SharingPool.get("widgets", wid);
            widget.page = -1;
            widget.real_data = true;
            widget.dimensions = {};
            widget.dimensions[dId] = dimension;
            widget.view = {};
            widget.view[BICst.REGION.DIMENSION1] = [dId];

            var targetIds = this.getAllTargetDimensionIDs(this.getWidgetIDByDimensionID(dId));
            BI.each(targetIds, function (idx, targetId) {
                widget.dimensions[targetId] = Data.SharingPool.get("dimensions", targetId);
                widget.dimensions[targetId].filter_value = {};
                if (!BI.has(widget.view, BICst.REGION.TARGET1)) {
                    widget.view[BICst.REGION.TARGET1] = [];
                }
                widget.view[BICst.REGION.TARGET1].push(targetId);
            });

            Data.Req.reqWidgetSettingByData({widget: widget}, function (data) {
                callback(BI.pluck(data.data.c, "n"));
            });
        },

        getWidgetDataByDimensionInfo: function (src, options) {
            var name = "__StatisticWidget__" + BI.UUID();
            var data = {
                type: BICst.WIDGET.STRING,
                bounds: {
                    height: 0,
                    width: 0,
                    left: 0,
                    top: 0
                },
                name: name,
                dimensions: {
                    "1234567": BI.extend({
                        name: "__Dimension__",
                        _src: src,
                        type: BICst.TARGET_TYPE.STRING
                    }, options)
                },
                view: {
                    10000: ["1234567"]
                }
            };

            var options = {};

            return {
                setDimensionType: function (type) {
                    data.dimensions["1234567"].type = type;
                },

                setSrc: function (src) {
                    data.dimensions["1234567"]._src = src;
                },

                setFilterValue: function (filter_value) {
                    data.dimensions["1234567"].filter_value = filter_value;
                },

                setSort: function (sort) {
                    data.dimensions["1234567"].sort = sort;
                },

                setOptions: function (opt) {
                    options = opt;
                },

                first: function (call) {
                    var copy = BI.deepClone(data);
                    copy.page = BICst.TABLE_PAGE_OPERATOR.REFRESH;
                    Data.Req.reqWidgetSettingByData({widget: BI.extend(copy, {text_options: options})}, function (res) {
                        call(res);
                    });
                },

                prev: function (call) {
                    var copy = BI.deepClone(data);
                    copy.page = BICst.TABLE_PAGE_OPERATOR.ROW_PRE;
                    Data.Req.reqWidgetSettingByData({widget: BI.extend(copy, {text_options: options})}, function (res) {
                        call(res);
                    });
                },

                next: function (call) {
                    var copy = BI.deepClone(data);
                    copy.page = BICst.TABLE_PAGE_OPERATOR.ROW_NEXT;
                    Data.Req.reqWidgetSettingByData({widget: BI.extend(copy, {text_options: options})}, function (res) {
                        call(res);
                    });
                },

                all: function (call) {
                    var copy = BI.deepClone(data);
                    copy.page = BICst.TABLE_PAGE_OPERATOR.ALL_PAGE;
                    Data.Req.reqWidgetSettingByData({widget: BI.extend(copy, {text_options: options})}, function (res) {
                        call(res);
                    });
                }
            };
        },

        getWidgetDataByWidgetInfo: function (dimensions, view, callback, options) {
            var self = this;
            options || (options = {});
            var data = {
                bounds: {
                    left: 0,
                    top: 0,
                    width: 0,
                    height: 0
                },
                name: "__StatisticWidget__" + BI.UUID(),
                dimensions: dimensions,
                filter: {
                    filter_type: BICst.FILTER_TYPE.AND,
                    filter_value: self.getControlCalculations(options.id)
                },
                view: view
            };
            Data.Req.reqWidgetSettingByData({widget: BI.extend(data, options)}, function (res) {
                callback(res);
            })
        },

        getControlCalculations: function (notcontain) {
            var self = this, filterValues = [];
            //控件
            var widgetIds = this.getAllWidgetIDs();
            BI.each(widgetIds, function (i, id) {
                if (!self.isControlWidgetByWidgetId(id)) {
                    return;
                }
                if (id === notcontain) {
                    return;
                }
                //去掉自身和在自身之后创建的控件
                if (BI.isNotNull(notcontain) && self.isControlWidgetByWidgetId(notcontain)
                    && self.getWidgetInitTimeByID(id) > self.getWidgetInitTimeByID(notcontain)) {
                    return;
                }
                var value = self.getWidgetValueByID(id);
                if (BI.isNotNull(value)) {
                    var dimensionIds = self.getAllDimensionIDs(id);
                    BI.each(dimensionIds, function (i, dimId) {
                        var fValue = value, fType = "";
                        if (BI.isNull(fValue) || BI.isEmptyString(value) || BI.isEmptyObject(value) || !checkValueValid(self.getWidgetTypeByID(id), value)) {
                            return;
                        }
                        var filter = null;
                        switch (self.getWidgetTypeByID(id)) {
                            case BICst.WIDGET.STRING:
                                fType = BICst.TARGET_FILTER_STRING.BELONG_VALUE;
                                filter = {
                                    filter_type: fType,
                                    filter_value: fValue,
                                    _src: {field_id: self.getFieldIDByDimensionID(dimId)}
                                };
                                break;
                            case BICst.WIDGET.NUMBER:
                                fType = BICst.TARGET_FILTER_NUMBER.BELONG_VALUE;
                                filter = {
                                    filter_type: fType,
                                    filter_value: fValue,
                                    _src: {field_id: self.getFieldIDByDimensionID(dimId)}
                                };
                                break;
                            case BICst.WIDGET.DATE:
                                fType = BICst.FILTER_DATE.BELONG_DATE_RANGE;
                                var start = fValue.start, end = fValue.end;
                                fValue = {};
                                if (BI.isNotNull(start)) {
                                    start = parseComplexDate(start);
                                    fValue.start = start;
                                }
                                if (BI.isNotNull(end)) {
                                    end = parseComplexDate(end);
                                    fValue.end = end;
                                }
                                filter = {
                                    filter_type: fType,
                                    filter_value: fValue,
                                    _src: {field_id: self.getFieldIDByDimensionID(dimId)}
                                };
                                break;
                            case BICst.WIDGET.MONTH:
                                fType = BICst.FILTER_DATE.EQUAL_TO;
                                var year = fValue.year, month = fValue.month;
                                if (BI.isNumeric(year)) {
                                    filterValues.push({
                                        filter_type: BICst.FILTER_DATE.EQUAL_TO,
                                        filter_value: {group: BICst.GROUP.Y, values: year},
                                        _src: {field_id: self.getFieldIDByDimensionID(dimId)}
                                    });
                                }
                                if (!BI.isNumeric(month)) {
                                    return;
                                }
                                fValue = {group: BICst.GROUP.M, values: month + 1};
                                filter = {
                                    filter_type: fType,
                                    filter_value: fValue,
                                    _src: {field_id: self.getFieldIDByDimensionID(dimId)}
                                };
                                break;
                            case BICst.WIDGET.QUARTER:
                                fType = BICst.FILTER_DATE.EQUAL_TO;
                                var quarter = fValue.quarter, year = fValue.year;
                                if (BI.isNumeric(year)) {
                                    filterValues.push({
                                        filter_type: BICst.FILTER_DATE.EQUAL_TO,
                                        filter_value: {group: BICst.GROUP.Y, values: year},
                                        _src: {field_id: self.getFieldIDByDimensionID(dimId)}
                                    });
                                }
                                if (!BI.isNumeric(quarter)) {
                                    return;
                                }
                                fValue = {group: BICst.GROUP.S, values: quarter};
                                filter = {
                                    filter_type: fType,
                                    filter_value: fValue,
                                    _src: {field_id: self.getFieldIDByDimensionID(dimId)}
                                };
                                break;
                            case BICst.WIDGET.YEAR:
                                fType = BICst.FILTER_DATE.EQUAL_TO;
                                fValue = {group: BICst.GROUP.Y, values: fValue};
                                filter = {
                                    filter_type: fType,
                                    filter_value: fValue,
                                    _src: {field_id: self.getFieldIDByDimensionID(dimId)}
                                };
                                break;
                            case BICst.WIDGET.YMD:
                                fType = BICst.FILTER_DATE.EQUAL_TO;
                                fValue = {group: BICst.GROUP.YMD, values: parseComplexDate(fValue)};
                                filter = {
                                    filter_type: fType,
                                    filter_value: fValue,
                                    _src: {field_id: self.getFieldIDByDimensionID(dimId)}
                                };
                                break;
                        }
                        BI.isNotNull(filter) && filterValues.push(filter);
                    });

                    //树控件过滤条件设置,不能对每个纬度单独设置过滤条件
                    if (self.getWidgetTypeByID(id) === BICst.WIDGET.TREE) {
                        var viewDimensionIds = self.getWidgetViewByID(id)[BICst.REGION.DIMENSION1];
                        var treeValue = [];
                        createTreeFilterValue(treeValue, value, 0, viewDimensionIds);
                        filter = {
                            filter_type: BICst.FILTER_TYPE.OR,
                            filter_value: treeValue
                        };
                        filterValues.push(filter);
                    }

                    if (value.length === 1) {
                        var filter = value[0];
                        if (BI.isNotNull(parseFilter(filter))) {
                            filterValues.push(filter);
                        }
                    }
                }
            });
            return filterValues;

            function createTreeFilterValue(result, v, floor, dimensionIds, fatherFilterValue) {
                BI.each(v, function (value, child) {
                        var leafFilterObj = {
                            filter_type: BICst.TARGET_FILTER_STRING.BELONG_VALUE,
                            filter_value: {
                                type: BI.Selection.Multi,
                                value: [value]
                            },
                            // _src: {field_id: self.getFieldIDByDimensionID(dimensionIds[floor])}
                            _src: self.getDimensionSrcByID(dimensionIds[floor])
                        };
                        if (BI.isEmptyObject(child)) {
                            var filterObj = {
                                filter_type: BICst.FILTER_TYPE.AND,
                                filter_value: []
                            };
                            filterObj.filter_value.push(leafFilterObj);
                            BI.isNotNull(fatherFilterValue) && filterObj.filter_value.push(fatherFilterValue);
                            result.push(filterObj);
                        } else {
                            createTreeFilterValue(result, child, floor + 1, dimensionIds, leafFilterObj);
                        }
                    }
                );
            }

            function checkValueValid(type, value) {
                switch (type) {
                    case BICst.WIDGET.NUMBER:
                        return !(BI.isEmptyString(value.min) && BI.isEmptyString(value.max));
                    default:
                        return true;
                }
            }
        },

        getWidgetCalculationByID: function (wid) {
            var self = this;
            var widget = Data.SharingPool.get("widgets", wid);
            var filterValues = [];

            //对于维度的条件，很有可能是一个什么属于分组 这边处理 （没放到构造的地方处理是因为“其他”）
            function parseStringFilter4Group(dId, value) {
                var group = BI.Utils.getDimensionGroupByID(dId);
                var details = group.details;
                var groupMap = {};
                BI.each(details, function (i, detail) {
                    groupMap[detail.id] = [];
                    BI.each(detail.content, function (j, content) {
                        groupMap[detail.id].push(content.value);
                    });
                });
                var groupNames = BI.keys(groupMap), ungroupName = group.ungroup2OtherName;
                if (group.ungroup2Other === BICst.CUSTOM_GROUP.UNGROUP2OTHER.SELECTED) {
                    // groupNames.push(ungroupName);
                    groupNames.push(BICst.UNGROUP_TO_OTHER);
                }
                // 对于drill和link 一般value的数组里只有一个值
                var v = value[0];
                if (groupNames.contains(v)) {
                    if (v === BICst.UNGROUP_TO_OTHER) {
                        var vs = [];
                        BI.each(groupMap, function (gk, gv) {
                            gk !== v && (vs = vs.concat(gv));
                        });
                        return {
                            filter_type: BICst.TARGET_FILTER_STRING.NOT_BELONG_VALUE,
                            filter_value: {type: BI.Selection.Multi, value: vs},
                            _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                        }
                    }
                    return {
                        filter_type: BICst.TARGET_FILTER_STRING.BELONG_VALUE,
                        filter_value: {type: BI.Selection.Multi, value: groupMap[v]},
                        _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                    }
                }
                return {
                    filter_type: BICst.TARGET_FILTER_STRING.BELONG_VALUE,
                    filter_value: {type: BI.Selection.Multi, value: value},
                    _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                }
            }

            //计算数值自动分组时候的最大最小值
            function calculateMinMax4Auto(min, max) {
                var min = Math.abs(min) + "";
                var max = Math.abs(max) + "";
                var minCount = min.split(".")[0].length;
                var maxCount = max.split(".")[0].length;
                var count = minCount > maxCount ? minCount : maxCount;
                var magnify = 1;
                //缩小补零
                var s = "0.";
                while (count - minCount > 0) {
                    s += "0";
                    minCount++;
                }
                min = min.replace(".", "");
                min = s + min;
                s = "0.";
                while (count - maxCount > 0) {
                    s += "0";
                    maxCount++;
                }
                max = max.replace(".", "");
                max = s + max;

                //后面补零对齐
                var zeros = max.length - min.length;
                if (zeros > 0) {
                    while (zeros-- > 0) {
                        min += "0";
                    }
                } else {
                    while (zeros++ < 0) {
                        max += "0";
                    }
                }
                //截零
                var i = max.length - 1, add = "0.";
                while (min[i] === "0" && max[i] === "0" && this.min != 0 && this.max != 0) {
                    i--;
                }

                //截位/截位+1
                min = this.min < 0 ? -(cutBig(min)) : cutSmall(min);
                max = this.max < 0 ? -(cutSmall(max)) : cutBig(max);

                while (count-- > 0) {
                    magnify *= 10;
                }

                return {
                    max: max.mul(magnify),
                    min: min.mul(magnify)
                }

                function cutSmall(val) {
                    return BI.parseFloat(val.substring(0, i));
                }

                function cutBig(val) {
                    if (val[i] === "0") {
                        return BI.parseFloat(val);
                    }
                    val = val.substring(0, i);
                    var length = val.length - 2;
                    while (--length > 0) {
                        add += "0";
                    }
                    add += "1";
                    if (val[i - 1] === ".") {
                        return BI.parseFloat(val) + 1;
                    }
                    return BI.parseFloat(val) + BI.parseFloat(add);
                }
            }

            function parseNumberFilter4Group(dId, v) {
                var value = v[0];
                var group = BI.Utils.getDimensionGroupByID(dId);
                var groupValue = group.group_value, groupType = group.type;
                var groupMap = {};
                if (BI.isNull(groupValue) && BI.isNull(groupType)) {
                    //没有分组为自动分组 但是这个时候维度中无相关分组信息，暂时截取来做
                    var sIndex = value.indexOf("-");
                    if (sIndex === -1) {  //空分组
                        return {
                            filter_type: BICst.TARGET_FILTER_NUMBER.IS_NULL,
                            filter_value: {},
                            _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                        }
                    }
                    var min = value.slice(0, sIndex), max = value.slice(sIndex + 1);
                    return {
                        filter_type: BICst.TARGET_FILTER_NUMBER.BELONG_VALUE,
                        filter_value: {
                            min: min,
                            max: max,
                            closemin: true,
                            closemax: false
                        },
                        _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                    }
                }
                if (groupType === BICst.GROUP.AUTO_GROUP) {
                    //坑爹，要自己算分组名称出来
                    var groupInterval = groupValue.group_interval;
                    var maxMin = calculateMinMax4Auto(groupValue.min, groupValue.max);
                    var max = maxMin.max, min = maxMin.min;
                    while (min < max) {
                        var newMin = min + BI.parseFloat(groupInterval);
                        groupMap[min + "-" + newMin] = {
                            min: min,
                            max: newMin,
                            closemin: true,
                            closemax: newMin >= max
                        };
                        min = newMin;
                    }
                    if (BI.isNull(groupMap[value])) {
                        return {
                            filter_type: BICst.TARGET_FILTER_NUMBER.IS_NULL,
                            filter_value: {},
                            _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                        }
                    } else {
                        return {
                            filter_type: BICst.TARGET_FILTER_NUMBER.BELONG_VALUE,
                            filter_value: groupMap[value],
                            _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                        };
                    }
                }
                if (groupType === BICst.GROUP.ID_GROUP) {
                    if (BI.isNull(value) || BI.isEmptyString(value)) {
                        return {
                            filter_type: BICst.TARGET_FILTER_NUMBER.IS_NULL,
                            filter_value: {},
                            _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                        };
                    } else {
                        return {
                            filter_type: BICst.TARGET_FILTER_NUMBER.BELONG_VALUE,
                            filter_value: {
                                min: BI.parseFloat(value),
                                max: BI.parseFloat(value),
                                closemin: true,
                                closemax: true
                            },
                            _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                        }
                    }
                }
                var groupNodes = groupValue.group_nodes, useOther = groupValue.use_other;
                var oMin, oMax;
                BI.each(groupNodes, function (i, node) {
                    i === 0 && (oMin = node.min);
                    i === groupNodes.length - 1 && (oMax = node.max);
                    groupMap[node.id] = {
                        min: node.min,
                        max: node.max,
                        closemin: node.closemin,
                        closemax: node.closemax
                    }
                });
                if (BI.isNotNull(groupMap[value])) {
                    return {
                        filter_type: BICst.TARGET_FILTER_NUMBER.BELONG_VALUE,
                        filter_value: groupMap[value],
                        _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                    };
                } else if (value === BICst.UNGROUP_TO_OTHER) {
                    //反选数值区间，使用多个反条件并集
                    var vs = [];
                    BI.each(groupMap, function (id, v) {
                        vs.push({
                            filter_type: BICst.TARGET_FILTER_NUMBER.NOT_BELONG_VALUE,
                            filter_value: v,
                            _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                        });
                    });
                    return {
                        filter_type: BICst.FILTER_TYPE.AND,
                        filter_value: vs
                    };
                } else if (BI.isNumeric(value)) {
                    //自定义分组后不勾选剩余值分组到其他
                    return {
                        filter_type: BICst.TARGET_FILTER_NUMBER.BELONG_VALUE,
                        filter_value: {
                            min: BI.parseFloat(value),
                            max: BI.parseFloat(value),
                            closemin: true,
                            closemax: true
                        },
                        _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                    }
                } else if (BI.isNull(value) || BI.isEmptyString(value)) {
                    return {
                        filter_type: BICst.TARGET_FILTER_NUMBER.IS_NULL,
                        filter_value: {},
                        _src: {field_id: BI.Utils.getFieldIDByDimensionID(dId)}
                    };
                }
            }

            function parseSimpleFilter(v) {
                var dId = v.dId;
                var dType = self.getDimensionTypeByID(dId);
                switch (dType) {
                    case BICst.TARGET_TYPE.STRING:
                        return parseStringFilter4Group(dId, v.value);
                    case BICst.TARGET_TYPE.NUMBER:
                        return parseNumberFilter4Group(dId, v.value);
                    case BICst.TARGET_TYPE.DATE:
                        var dGroup = self.getDimensionGroupByID(dId);
                        var groupType = dGroup.type;
                        return {
                            filter_type: BICst.FILTER_DATE.EQUAL_TO,
                            filter_value: {values: v.value[0], group: groupType},
                            _src: {field_id: BI.Utils.getFieldIDByDimensionID(v.dId)}
                        };
                }
            }

            //钻取条件  对于交叉表，要考虑的不仅仅是used，还有行表头与列表头之间的钻取问题
            var drill = this.getDrillByID(wid);
            if (BI.isNotNull(drill) && widget.type !== BICst.WIDGET.MAP) {
                BI.each(drill, function (drId, drArray) {
                    if (drArray.length === 0) {
                        return;
                    }
                    BI.isNotNull(widget.dimensions[drId]) && (widget.dimensions[drId].used = false);
                    BI.each(drArray, function (i, drill) {
                        if (BI.isNotNull(widget.dimensions[drill.dId])) {
                            widget.dimensions[drill.dId].used = (i === drArray.length - 1);
                            var drillRegionType = self.getRegionTypeByDimensionID(drId);
                            //从原来的region中pop出来
                            var tempRegionType = self.getRegionTypeByDimensionID(drill.dId);
                            var dIndex = widget.view[drillRegionType].indexOf(drId);
                            BI.remove(widget.view[tempRegionType], drill.dId);
                            // if (drillRegionType === tempRegionType) {
                            widget.view[drillRegionType].splice(dIndex, 0, drill.dId);
                            // } else {
                            //     widget.view[drillRegionType].push(drill.dId);
                            // }
                        }
                        BI.each(drArray[i].values, function (i, v) {
                            var filterValue = parseSimpleFilter(v);
                            if (BI.isNotNull(filterValue)) {
                                filterValues.push(filterValue);
                            }
                        });
                    });
                });
            }

            //所有控件过滤条件（考虑有查询按钮的情况）
            filterValues = filterValues.concat(
                this.isQueryControlExist() && !this.isControlWidgetByWidgetId(wid) ?
                    Data.SharingPool.get("control_filters") : this.getControlCalculations(wid));

            //联动 由于这个clicked现在放到了自己的属性里，直接拿就好了
            var linkages = this.getLinkageValuesByID(wid);
            BI.each(linkages, function (cId, linkValue) {
                BI.each(linkValue, function (i, v) {
                    var filterValue = parseSimpleFilter(v);
                    if (BI.isNotNull(filterValue)) {
                        filterValues.push(filterValue);
                    }
                });
                var transferFilter = BI.Utils.getWSTransferFilterByID(BI.Utils.getWidgetIDByDimensionID(cId));
                if (transferFilter === true) {
                    var tarFilter = BI.Utils.getDimensionFilterValueByID(cId);
                    if (BI.isNotNull(tarFilter)) {
                        parseFilter(tarFilter);
                        if (BI.isNotNull(tarFilter) && BI.isNotEmptyObject(tarFilter)) {
                            filterValues.push(tarFilter);
                        }
                    }
                }
            });

            //联动传递指标过滤条件  找到联动链上的所有的组件，获取当前点击的指标的过滤条件  感觉有点浮夸的功能
            var allLinksWIds = [];

            function getLinkedIds(wid, links) {
                var allWIds = BI.Utils.getAllWidgetIDs();
                BI.each(allWIds, function (i, aWid) {
                    var linkages = BI.Utils.getWidgetLinkageByID(aWid);
                    BI.each(linkages, function (i, link) {
                        if (link.to === wid) {
                            links.push(BI.Utils.getWidgetIDByDimensionID(link.from));
                            getLinkedIds(BI.Utils.getWidgetIDByDimensionID(link.from), links);
                        }
                    });
                });
            }

            getLinkedIds(wid, allLinksWIds);
            BI.each(allLinksWIds, function (i, lId) {
                // 并不是拿到所有的指标的过滤条件
                // if (self.getWSTransferFilterByID(lId) === true) {
                //     var tarIds = BI.Utils.getAllTargetDimensionIDs(lId);
                //     BI.each(tarIds, function (i, tarId) {
                //         var tarFilter = BI.Utils.getDimensionFilterValueByID(tarId);
                //         if (BI.isNotEmptyObject(tarFilter)) {
                //             parseFilter(tarFilter);
                //             filterValues.push(tarFilter);
                //         }
                //     })
                // }

                var lLinkages = BI.Utils.getLinkageValuesByID(lId);
                BI.each(lLinkages, function (cId, linkValue) {
                    var lTransferFilter = BI.Utils.getWSTransferFilterByID(BI.Utils.getWidgetIDByDimensionID(cId));
                    if (lTransferFilter === true) {
                        var lTarFilter = BI.Utils.getDimensionFilterValueByID(cId);
                        if (BI.isNotNull(lTarFilter)) {
                            parseFilter(lTarFilter);
                            filterValues.push(lTarFilter);
                        }
                    }
                });

                //还应该拿到所有的联动过来的组件的钻取条件 也是给跪了
                //联动过来的组件的联动条件被删除，忽略钻取条件
                var linkDrill = self.getDrillByID(lId);
                var notIgnore = BI.some(linkages, function (ldid, link) {
                    return lId === self.getWidgetIDByDimensionID(ldid);
                });
                if (notIgnore && BI.isNotNull(linkDrill) && BI.isNotEmptyObject(linkDrill)) {
                    BI.each(linkDrill, function (drId, drArray) {
                        if (drArray.length === 0) {
                            return;
                        }
                        BI.each(drArray, function (i, drill) {
                            BI.each(drArray[i].values, function (i, v) {
                                var filterValue = parseSimpleFilter(v);
                                if (BI.isNotNull(filterValue)) {
                                    filterValues.push(filterValue);
                                }
                            });
                        });
                    });
                }
            });


            //联动过来的维度的过滤条件
            function getLinkFatherWidget(wid, childId) {
                var id;
                var links = self.getWidgetLinkageByID(wid);
                BI.some(links, function (i, link) {
                    if (link.to === childId) {
                        id = wid;
                        return true;
                    } else {
                        id = getLinkFatherWidget(link.to, childId);
                    }
                });
                return id;
            }


            widget.linkedWidget = {};
            BI.each(linkages, function (lTId, link) {
                var pWid = getLinkFatherWidget(self.getWidgetIDByDimensionID(lTId), wid);
                var pWidget = self.getWidgetCalculationByID(pWid);
                widget.linkedWidget = pWidget;
                // filterValues.push({
                //     filter_type: "8080",
                //     filter_value: pWidget
                // });
                // filterValues = filterValues.concat(self.getDimensionsFilterByTargetId(lTId));
            });

            //日期类型的过滤条件
            var dimensions = widget.dimensions;
            BI.each(dimensions, function (dId, dimension) {
                var filterValue = dimension.filter_value || {};
                parseFilter(filterValue);
            });

            //考虑表头上指标过滤条件的日期类型
            var target_filter = widget.filter_value;
            BI.each(target_filter, function (tId, filter) {
                parseFilter(filter)
            });

            //标红维度的处理
            var dIds = this.getAllDimDimensionIDs(wid);
            BI.each(dIds, function (idx, dId) {
                var dimensionMap = self.getDimensionMapByDimensionID(dId);
                var valid = true;
                //树控件和明细表
                if (widget.type === BICst.WIDGET.DETAIL || widget.type === BICst.WIDGET.TREE) {
                    BI.each(dimensionMap, function (tableId, obj) {
                        var targetRelation = obj.target_relation;
                        var pId = self.getFirstRelationPrimaryIdFromRelations(targetRelation);
                        var fId = self.getLastRelationForeignIdFromRelations(targetRelation);
                        var paths = self.getPathsFromFieldAToFieldB(pId, fId);
                        if (!BI.deepContains(paths, targetRelation)) {
                            //维度和某个指标之间设置了路径但是路径在配置处被删了
                            if (paths.length >= 1) {
                                widget.dimensions[dId].dimension_map[tableId].target_relation = paths[0];
                            }
                        }
                    })
                } else {
                    var tIds = self.getAllTargetDimensionIDs(wid);
                    BI.any(tIds, function (idx, tId) {
                        if (!self.isCalculateTargetByDimensionID(tId)) {
                            //维度和某个指标之间没有设置路径
                            if (!BI.has(dimensionMap, tId)) {
                                var fieldId = BI.Utils.getFieldIDByDimensionID(dId);
                                var paths = BI.Utils.getPathsFromFieldAToFieldB(fieldId, BI.Utils.getFieldIDByDimensionID(tId))
                                if (paths.length === 1) {
                                    widget.dimensions[dId].dimension_map[tId] = {
                                        _src: {field_id: fieldId},
                                        target_relation: paths
                                    };
                                } else {
                                    valid = false;
                                    return true;
                                }
                            } else {
                                var targetRelation = dimensionMap[tId].target_relation;
                                BI.any(targetRelation, function (id, path) {
                                    var pId = self.getFirstRelationPrimaryIdFromRelations(path);
                                    var fId = self.getLastRelationForeignIdFromRelations(path);
                                    var paths = self.getPathsFromFieldAToFieldB(pId, fId);
                                    if (!BI.deepContains(paths, path)) {
                                        //维度和某个指标之间设置了路径但是路径在配置处被删了
                                        if (paths.length === 1) {
                                            widget.dimensions[dId].dimension_map[tId].target_relation.length = id;
                                            widget.dimensions[dId].dimension_map[tId].target_relation.push(paths[0]);
                                        } else {
                                            valid = false;
                                            return true;
                                        }
                                    }
                                })
                            }
                        }
                    });
                }
                if (valid === false) {
                    widget.dimensions[dId].used = false;
                }
            });

            //gis地图按分组表来算，而非交叉表
            if (widget.type === BICst.WIDGET.GIS_MAP) {
                if (BI.isNotEmptyArray(widget.view[BICst.REGION.DIMENSION2])) {
                    widget.view[BICst.REGION.DIMENSION1] = widget.view[BICst.REGION.DIMENSION1] || [];
                    widget.view[BICst.REGION.DIMENSION1] = BI.concat(widget.view[BICst.REGION.DIMENSION1], widget.view[BICst.REGION.DIMENSION2]);
                    widget.view[BICst.REGION.DIMENSION2] = [];
                }
            }

            widget.filter = {filter_type: BICst.FILTER_TYPE.AND, filter_value: filterValues};
            widget.real_data = true;

            return widget;
        },

        getWidgetDataByID: (function () {
            var cache = {};
            return function (wid, callbacks, options) {
                options || (options = {});
                var key = BI.UUID();
                if (!BI.Utils.isControlWidgetByWidgetId(wid)) {
                    key = wid;
                }
                cache[key] = callbacks;
                Data.Req.reqWidgetSettingByData({widget: BI.extend(this.getWidgetCalculationByID(wid), options)}, function (data) {
                    if (cache[key] === callbacks) {
                        callbacks.success(data);
                        delete cache[key];
                    } else {
                        callbacks.error && callbacks.error(data);
                    }
                    callbacks.done && callbacks.done(data);
                });
            }
        })(),

        /**
         * 组件与表的关系
         */
        broadcastAllWidgets2Refresh: function (force) {
            var self = this;
            var allWidgetIds = this.getAllWidgetIDs();
            if (force === true || this.isQueryControlExist() === false) {
                BI.each(allWidgetIds, function (i, wId) {
                    if (!self.isControlWidgetByWidgetId(wId)) {
                        BI.Broadcasts.send(BICst.BROADCAST.REFRESH_PREFIX + wId);
                    }
                });
            }
        },

        isTableUsableByWidgetID: function (tableId, wId) {
            var self = this;
            var dIds = this.getAllDimensionIDs(wId);
            var noneCalculateTargetIds = [];
            BI.each(dIds, function (i, dId) {
                var dimensionType = self.getDimensionTypeByID(dId);
                switch (dimensionType) {
                    case BICst.TARGET_TYPE.DATE:
                    case BICst.TARGET_TYPE.STRING:
                    case BICst.TARGET_TYPE.NUMBER:
                        noneCalculateTargetIds.push(dId);
                }
            });
            if (noneCalculateTargetIds.length < 1) {
                return true;
            }
            var tIds = [];
            BI.each(noneCalculateTargetIds, function (id, dId) {
                tIds.push(self.getTableIDByDimensionID(dId));
            });
            return this.isTableInRelativeTables(tIds, tableId);
        },

        getDimensionsFilterByTargetId: function (tId) {
            var self = this;
            var dimensionIds = this.getAllDimDimensionIDs(this.getWidgetIDByDimensionID(tId));
            var dFilters = [];
            BI.each(dimensionIds, function (i, dId) {
                var dimensionMap = self.getDimensionMapByDimensionID(dId);
                if (BI.isNotNull(dimensionMap[tId])) {
                    var dFilterValue = self.getDimensionFilterValueByID(dId);
                    if (BI.isNotEmptyObject(dFilterValue)) {
                        parseDimensionFilter4Linkage(dFilterValue, dimensionMap[tId]._src, dId);
                        dFilters.push(dFilterValue);
                    }
                }
            });
            return dFilters;

            function parseDimensionFilter4Linkage(dFilter, src, dId) {
                if (dFilter.filter_type === BICst.FILTER_TYPE.AND ||
                    dFilter.filter_type === BICst.FILTER_TYPE.OR) {
                    BI.each(dFilter.filter_value, function (i, fValue) {
                        parseDimensionFilter4Linkage(fValue, src, dId);
                    });
                } else {
                    if (dFilter.target_id === dId) {
                        dFilter._src = src;
                    } else {
                        dFilter.filter_type = BICst.FILTER_TYPE.EMPTY_CONDITION;
                    }
                }
            }
        }

    });

    //获取复杂日期的值
    function parseComplexDate(v) {
        if (v.type === BICst.MULTI_DATE_PARAM) {
            return parseComplexDateForParam(v.value);
        } else {
            return parseComplexDateCommon(v);
        }
        function parseComplexDateForParam(value) {
            var widgetInfo = value.widgetInfo, offset = value.offset;
            if (BI.isNull(widgetInfo) || BI.isNull(offset)) {
                return;
            }
            var paramDate;
            var wWid = widgetInfo.wId, se = widgetInfo.startOrEnd;
            if (BI.isNotNull(wWid) && BI.isNotNull(se)) {
                var wWValue = BI.Utils.getWidgetValueByID(wWid);
                if (BI.isNull(wWValue) || BI.isEmptyObject(wWValue)) {
                    return;
                }
                if (se === BI.MultiDateParamPane.start && BI.isNotNull(wWValue.start)) {
                    paramDate = parseComplexDateCommon(wWValue.start);
                }
                if (se === BI.MultiDateParamPane.end && BI.isNotNull(wWValue.end)) {
                    paramDate = parseComplexDateCommon(wWValue.end);
                }
            } else {
                if (BI.isNull(widgetInfo.wId) || BI.isNull(BI.Utils.getWidgetValueByID(widgetInfo.wId))) {
                    return;
                }
                paramDate = parseComplexDateCommon(BI.Utils.getWidgetValueByID(widgetInfo.wId));
            }
            if (BI.isNotNull(paramDate)) {
                return parseComplexDateCommon(offset, new Date(paramDate));
            }
        }

        function parseComplexDateCommon(v, consultedDate) {
            var type = v.type, value = v.value;
            var date = BI.isNull(consultedDate) ? new Date() : consultedDate;
            var currY = date.getFullYear(), currM = date.getMonth(), currD = date.getDate();
            date = new Date(date.getFullYear(), date.getMonth(), date.getDate());
            if (BI.isNull(type) && isValidDate(v)) {
                return new Date(v.year, v.month, v.day).getTime();
            }
            switch (type) {
                case BICst.MULTI_DATE_YEAR_PREV:
                    return new Date(currY - 1 * value, currM, currD).getTime();
                case BICst.MULTI_DATE_YEAR_AFTER:
                    return new Date(currY + 1 * value, currM, currD).getTime();
                case BICst.MULTI_DATE_YEAR_BEGIN:
                    return new Date(currY, 0, 1).getTime();
                case BICst.MULTI_DATE_YEAR_END:
                    return new Date(currY, 11, 31).getTime();

                case BICst.MULTI_DATE_MONTH_PREV:
                    return new Date().getBeforeMultiMonth(value).getTime();
                case BICst.MULTI_DATE_MONTH_AFTER:
                    return new Date().getAfterMultiMonth(value).getTime();
                case BICst.MULTI_DATE_MONTH_BEGIN:
                    return new Date(currY, currM, 1).getTime();
                case BICst.MULTI_DATE_MONTH_END:
                    return new Date(currY, currM, (date.getLastDateOfMonth()).getDate()).getTime();

                case BICst.MULTI_DATE_QUARTER_PREV:
                    return new Date().getBeforeMulQuarter(value).getTime();
                case BICst.MULTI_DATE_QUARTER_AFTER:
                    return new Date().getAfterMulQuarter(value).getTime();
                case BICst.MULTI_DATE_QUARTER_BEGIN:
                    return new Date().getQuarterStartDate().getTime();
                case BICst.MULTI_DATE_QUARTER_END:
                    return new Date().getQuarterEndDate().getTime();

                case BICst.MULTI_DATE_WEEK_PREV:
                    return date.getOffsetDate(-7 * value).getTime();
                case BICst.MULTI_DATE_WEEK_AFTER:
                    return date.getOffsetDate(7 * value).getTime();

                case BICst.MULTI_DATE_DAY_PREV:
                    return date.getOffsetDate(-1 * value).getTime();
                case BICst.MULTI_DATE_DAY_AFTER:
                    return date.getOffsetDate(1 * value).getTime();
                case BICst.MULTI_DATE_DAY_TODAY:
                    return date.getTime();
                case BICst.MULTI_DATE_CALENDAR:
                    return new Date(value.year, value.month, value.day).getTime();

            }
        }

        function isValidDate(v) {
            return BI.isNotNull(v.year) && BI.isNotNull(v.month) && BI.isNotNull(v.day);
        }
    }

    //format date type filter
    function parseFilter(filter) {
        var filterType = filter.filter_type, filterValue = filter.filter_value;
        if (filterType === BICst.FILTER_TYPE.AND || filterType === BICst.FILTER_TYPE.OR) {
            var indexOfInvalid = [];
            BI.each(filterValue, function (i, value) {
                if (BI.isNull(parseFilter(value))) {
                    indexOfInvalid.push(i);
                }
            });
            BI.removeAt(filterValue, indexOfInvalid);
        }
        if (BI.isNull(filterValue)) {
            return;
        }
        if (filterType === BICst.FILTER_DATE.BELONG_DATE_RANGE || filterType === BICst.FILTER_DATE.NOT_BELONG_DATE_RANGE) {
            var start = filterValue.start, end = filterValue.end;
            if (BI.isNotNull(start)) {
                filterValue.start = parseComplexDate(start);
            }
            if (BI.isNotNull(end)) {
                var endTime = parseComplexDate(end);
                if (BI.isNotNull(endTime)) {
                    filterValue.end = new Date(endTime).getOffsetDate(1).getTime() - 1
                } else {
                    delete filterValue.end;
                }
            }
        }
        if (filterType === BICst.FILTER_DATE.BELONG_WIDGET_VALUE || filterType === BICst.FILTER_DATE.NOT_BELONG_WIDGET_VALUE) {
            var filterWId = filterValue.wId, filterValueType = filterValue.filter_value.type;
            var wValue = BI.Utils.getWidgetValueByID(filterWId);
            if (!BI.Utils.isWidgetExistByID(filterWId) || BI.isNull(wValue)) {
                return;
            }
            switch (filterValueType) {
                case BICst.SAME_PERIOD:
                    if (BI.isNotNull(wValue.start)) {
                        filterValue.start = parseComplexDate(wValue.start);
                    }
                    if (BI.isNotNull(wValue.end)) {
                        var endTime = parseComplexDate(wValue.end);
                        if (BI.isNotNull(endTime)) {
                            filterValue.end = new Date(endTime).getOffsetDate(1).getTime() - 1;
                        } else {
                            delete filterValue.end;
                        }
                    }
                    break;
                case BICst.LAST_SAME_PERIOD:
                    if (BI.isNotNull(wValue.start) && BI.isNotNull(wValue.end)) {
                        var s = parseComplexDate(wValue.start);
                        var e = parseComplexDate(wValue.end);
                        if (BI.isNotNull(s) && BI.isNotNull(e)) {
                            filterValue.start = new Date(2 * s - e).getOffsetDate(-1).getTime();
                        } else {
                            delete filterValue.start
                        }
                        if (BI.isNotNull(s)) {
                            filterValue.end = new Date(s).getTime() - 1;
                        } else {
                            delete filterValue.end;
                        }
                    } else if (BI.isNotNull(wValue.start)) {
                        var s = parseComplexDate(wValue.start);
                        delete filterValue.start;
                        if (BI.isNotNull(s)) {
                            filterValue.end = s - 1;
                        } else {
                            delete filterValue.end;
                        }
                    } else if (BI.isNotNull(wValue.end)) {
                        var e = parseComplexDate(wValue.end);
                        delete filterValue.end;
                        if (BI.isNotNull(e)) {
                            filterValue.end = e;
                        }
                        filterValue.start = parseComplexDate(wValue.end);
                    }
                    break;
                case BICst.YEAR_QUARTER:
                case BICst.YEAR_MONTH:
                case BICst.YEAR_WEEK:
                case BICst.YEAR_DAY:
                case BICst.MONTH_WEEK:
                case BICst.MONTH_DAY:
                case BICst.YEAR:
                    var date = getDateControlValue(filterWId);
                    if (BI.isNotNull(date)) {
                        var value = getOffSetDateByDateAndValue(date, filterValue.filter_value);
                        filterValue.start = value.start;
                        if (BI.isNotNull(value.end)) {
                            filterValue.end = new Date(value.end).getOffsetDate(1).getTime() - 1;
                        }
                    }
                    break;
            }
        }
        if (filterType === BICst.FILTER_DATE.EARLY_THAN) {
            var date = getDateControlValue(filterValue.wId);
            if (BI.isNotNull(date)) {
                var value = getOffSetDateByDateAndValue(date, filterValue.filter_value);
                if (BI.isNotNull(value.start)) {
                    filterValue.end = new Date(value.start).getTime() - 1;
                }
            }
        }
        if (filterType === BICst.FILTER_DATE.LATER_THAN) {
            var date = getDateControlValue(filterValue.wId);
            if (BI.isNotNull(date)) {
                var value = getOffSetDateByDateAndValue(date, filterValue.filter_value);
                if (BI.isNotNull(value.start)) {
                    filterValue.start = new Date(value.start).getTime();
                }
            }
        }
        if (filterType === BICst.FILTER_DATE.EQUAL_TO || filterType === BICst.FILTER_DATE.NOT_EQUAL_TO) {
            if (BI.isNull(filterValue)) {
                filterValue = {};
            } else {
                filterValue.values = parseComplexDate(filterValue);
                filterValue.group = BICst.GROUP.YMD;
            }
        }

        //数值类型为空忽略此条件
        if ((filterType === BICst.TARGET_FILTER_NUMBER.EQUAL_TO ||
            filterType === BICst.TARGET_FILTER_NUMBER.NOT_EQUAL_TO) &&
            BI.isEmptyString(filterValue)) {
            return;
        }

        if ((filterType === BICst.TARGET_FILTER_NUMBER.BELONG_VALUE ||
            BICst.TARGET_FILTER_NUMBER.NOT_BELONG_VALUE) &&
            (BI.isEmptyString(filterValue.min) && BI.isEmptyString(filterValue.max))
        ) {
            return;
        }

        return filter;
        //日期偏移值
        function getOffSetDateByDateAndValue(date, value) {
            var tool = new BI.ParamPopupView();
            var type = value.type, value = value.value;
            var fPrevOrAfter = value.foffset === 0 ? -1 : 1;
            var sPrevOrAfter = value.soffset === 0 ? -1 : 1;
            var start, end;
            start = end = date;
            var ydate = new Date((date.getFullYear() + fPrevOrAfter * value.fvalue), date.getMonth(), date.getDate());
            switch (type) {
                case BICst.YEAR:
                    start = new Date((date.getFullYear() + fPrevOrAfter * value.fvalue), 0, 1);
                    end = new Date(start.getFullYear(), 11, 31);
                    break;
                case BICst.YEAR_QUARTER:
                    ydate = tool._getOffsetQuarter(ydate, sPrevOrAfter * value.svalue);
                    start = tool._getQuarterStartDate(ydate);
                    end = tool._getQuarterEndDate(ydate);
                    break;
                case BICst.YEAR_MONTH:
                    ydate = tool._getOffsetMonth(ydate, sPrevOrAfter * value.svalue);
                    start = new Date(ydate.getFullYear(), ydate.getMonth(), 1);
                    end = new Date(ydate.getFullYear(), ydate.getMonth(), (ydate.getLastDateOfMonth()).getDate());
                    break;
                case BICst.YEAR_WEEK:
                    start = ydate.getOffsetDate(sPrevOrAfter * 7 * value.svalue);
                    end = start.getOffsetDate(6);
                    break;
                case BICst.YEAR_DAY:
                    start = ydate.getOffsetDate(sPrevOrAfter * value.svalue);
                    end = start;
                    break;
                case BICst.MONTH_WEEK:
                    var mdate = tool._getOffsetMonth(date, fPrevOrAfter * value.fvalue);
                    start = mdate.getOffsetDate(sPrevOrAfter * 7 * value.svalue);
                    end = start.getOffsetDate(6);
                    break;
                case BICst.MONTH_DAY:
                    var mdate = tool._getOffsetMonth(date, fPrevOrAfter * value.fvalue);
                    start = mdate.getOffsetDate(sPrevOrAfter * value.svalue);
                    end = start;
                    break;
            }
            return {
                start: start.getTime(),
                end: end.getTime()
            }
        }

        //获取日期控件的值
        function getDateControlValue(wid) {
            if (!BI.Utils.isWidgetExistByID(wid)) {
                return null;
            }
            var widgetType = BI.Utils.getWidgetTypeByID(wid);
            var wValue = BI.Utils.getWidgetValueByID(wid);
            var date = null;
            switch (widgetType) {
                case BICst.WIDGET.YEAR:
                    if (BI.isNumeric(wValue)) {
                        date = new Date(wValue, 0, 1);
                    }
                    break;
                case BICst.WIDGET.MONTH:
                    if (BI.isNotNull(wValue) && BI.isNumeric(wValue.year)) {
                        date = new Date(wValue.year, BI.isNumeric(wValue.month) ? wValue.month : 0, 1);
                    }
                    break;
                case BICst.WIDGET.QUARTER:
                    if (BI.isNotNull(wValue) && BI.isNumeric(wValue.year)) {
                        var quarter = wValue.quarter;
                        date = new Date(wValue.year, BI.isNumeric(quarter) ? (quarter * 3 - 1) : 0, 1);
                    }
                    break;
                case BICst.WIDGET.YMD:
                    if (BI.isNotNull(wValue)) {
                        var v = parseComplexDate(wValue);
                        if (BI.isNotNull(v)) {
                            date = new Date(v);
                            date = new Date(date.getFullYear(), date.getMonth(), date.getDate());
                        }
                    }
                    break;
            }
            return date;
        }
    }
})();
