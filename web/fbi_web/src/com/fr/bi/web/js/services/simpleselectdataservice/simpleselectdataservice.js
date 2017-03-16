/**
 * 简单字段选择服务
 *
 * Created by GUY on 2016/5/30.
 *
 * @class BI.SimpleSelectDataService
 * @extend BI.Widget
 */
BI.SimpleSelectDataService = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.SimpleSelectDataService.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-simple-select-data-service",
            isDefaultInit: false,
            tablesCreator: function () {
                return [];
            },
            fieldsCreator: function () {
                return [];
            }
        })
    },

    _init: function () {
        BI.SimpleSelectDataService.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.searcher = BI.createWidget({
            type: "bi.simple_select_data_searcher",
            element: this.element,
            itemsCreator: function (op, populate) {
                if (BI.isKey(op.searchType) && BI.isKey(op.keyword)) {
                    var result = self._getSearchResult(op.searchType, op.keyword);
                    populate(result.finded, result.matched);
                    return;
                }
                if (!op.node) {//根节点， 根据业务包找所有的表
                    populate(self._getTablesStructure());
                    return;
                }
                if (BI.isKey(op.node._keyword)) {
                    populate(self._getFieldsStructureByTableIdAndKeyword(op.node.id, op.node._keyword), op.node._keyword);
                    return;
                }
                if (BI.isNotNull(op.node.isParent)) {
                    populate(self._getFieldsStructureByTableId(op.node.id));
                }
            }
        });
        this.searcher.on(BI.SelectDataSearcher.EVENT_CLICK_ITEM, function (value, ob) {
            self.fireEvent(BI.SimpleSelectDataService.EVENT_CLICK_ITEM, arguments);
        });
        if (o.isDefaultInit === true) {
            this.populate();
        }
    },

    _getTitleByFieldId: function (fieldId) {
        var fieldName = BI.Utils.getFieldNameByID(fieldId);
        var tableId = BI.Utils.getTableIdByFieldID(fieldId);
        var tableName = BI.Utils.getTableNameByID(tableId);
        return tableName + "." + fieldName || "";
    },

    /**
     * 搜索结果
     * @param type
     * @param keyword
     * @param packageName
     * @returns {{finded: Array, matched: Array}}
     * @private
     */
    _getSearchResult: function (type, keyword) {
        var self = this, o = this.options;
        var searchResult = [], matchResult = [];
        //选择了表
        if (type & BI.SelectDataSearchSegment.SECTION_TABLE) {
            var items = self._getTablesStructure();
            var result = BI.Func.getSearchResult(items, keyword);
            searchResult = result.finded;
            matchResult = result.matched;
        } else {
            var map = {}, field2TableMap = {};
            var tables = o.tablesCreator();
            var items = [];
            BI.each(tables, function (i, table) {
                var fields = self._getFieldsStructureByTableId(table.id || table.value);
                BI.each(fields, function (i, filed) {
                    field2TableMap[filed.id || filed.value] = table;
                });
                items = items.concat(fields);
            });
            var result = BI.Func.getSearchResult(items, keyword);
            BI.each(result.matched.concat(result.finded), function (j, finded) {
                if (!map[finded.pId]) {
                    searchResult.push(BI.extend({
                        id: finded.pId,
                        wId: o.wId,
                        text: BI.Utils.getTableNameByID(finded.pId) || BI.Utils.getFieldNameByID(finded.pId) || "",
                        title: BI.Utils.getTableNameByID(finded.pId) || BI.Utils.getFieldNameByID(finded.pId) || "",
                        value: finded.pId,
                        type: "bi.simple_select_data_level0_node",
                        layer: 0
                    }, field2TableMap[finded.id || finded.value], {
                        isParent: true,
                        open: true,
                        _keyword: keyword
                    }));
                    map[finded.pId] = true;
                }
            });
            //searchResult = searchResult.concat(result.matched).concat(result.finded);
            matchResult = matchResult.concat(result.matched);
        }
        return {
            finded: searchResult,
            matched: matchResult
        }
    },

    /**
     * 业务包中，所有表
     * @param packageId
     * @returns {Array}
     * @private
     */
    _getTablesStructure: function () {
        var self = this, o = this.options;
        var tablesStructure = [];
        var currentTables = o.tablesCreator();
        BI.each(currentTables, function (i, table) {
            tablesStructure.push(BI.extend({
                id: table.id,
                type: "bi.simple_select_data_level0_node",
                layer: 0,
                text: BI.Utils.getTableNameByID(table.id) || "",
                title: BI.Utils.getTableNameByID(table.id) || "",
                value: table.id,
                isParent: true,
                open: false
            }, table));
        });
        return tablesStructure;
    },

    _getFieldsStructureByTableIdAndKeyword: function (tableId, keyword) {
        var fieldStructure = [];
        var self = this, o = this.options;
        var fields = o.fieldsCreator(tableId);
        var fieldMap = {}, map = {};
        var newFields = BI.PackageSelectDataService.getAllRelativeFields(tableId, fields, map);

        BI.each(newFields, function (i, field) {
            var fid = field.id;
            var fieldName = BI.Utils.getFieldNameByID(fid) || "";
            fieldStructure.push(fieldMap[fid] = BI.extend({
                id: fid,
                pId: tableId,
                wId: o.wId,
                type: "bi.detail_select_data_level0_item",
                layer: 1,
                fieldType: BI.Utils.getFieldTypeByID(fid),
                text: fieldName,
                title: self._getTitleByFieldId(fid),
                value: fid
            }, field))
        });

        if (BI.isNotEmptyObject(map)) {
            BI.each(fields, function (i, field) {
                var id = field.id;
                if (BI.isNotEmptyArray(map[id])) {
                    var fieldName = BI.Utils.getFieldNameByID(id) || "";
                    fieldStructure.push({
                        id: id,
                        pId: tableId,
                        type: "bi.expander",
                        text: fieldName,
                        el: BI.extend({
                            wId: o.wId,
                            text: fieldName,
                            keyword: keyword,
                            title: self._getTitleByFieldId(id),
                            fieldType: BI.Utils.getFieldTypeByID(id),
                            value: id
                        }, field, {
                            type: "bi.select_data_level1_date_node",
                            layer: 1,
                            isParent: true,
                            open: false
                        }),
                        popup: {
                            type: "bi.select_data_loader",
                            items: self._getSelfCircleFieldsByFieldId(id, map[id] || [])
                        }
                    });
                }
            });
        }
        var result = BI.Func.getSearchResult(fieldStructure, keyword);
        fields = result.matched.concat(result.finded);
        fieldStructure = [];
        BI.each(fields, function (i, f) {
            if (fieldMap[f.pId]) {
                fieldStructure.push(fieldMap[f.pId]);
            }
            fieldStructure.push(f);
        });
        return fieldStructure;
    },

    _getSelfCircleFieldsByFieldId: function (fieldId, foregion) {
        var self = this, o = this.options;
        foregion || (foregion = []);
        var tableId = BI.Utils.getTableIdByFieldID(fieldId);
        var fieldStructure = [];
        BI.each(foregion, function (i, f) {
            var fid = f.id;
            var fieldName = BI.Utils.getFieldNameByID(fid) || "";
            fieldStructure.push(BI.extend({
                id: fid,
                pId: tableId,
                wId: o.wId,
                type: "bi.detail_select_data_level1_item",
                layer: 2,
                fieldType: BI.Utils.getFieldTypeByID(fid),
                text: fieldName,
                title: self._getTitleByFieldId(fid),
                value: fid
            }, f));
        });
        return fieldStructure;
    },

    _getFieldsStructureByTableId: function (tableId) {
        var fieldStructure = [];
        var self = this, o = this.options;

        var viewFields = [];
        var fields = o.fieldsCreator(tableId);
        var map = {};
        var newFields = BI.PackageSelectDataService.getAllRelativeFields(tableId, fields, map);

        BI.each(newFields, function (i, field) {
            var fid = field.id;
            if (viewFields.contains(fid)) {
                return;
            }
            var fieldName = BI.Utils.getFieldNameByID(fid) || "";
            fieldStructure.push(BI.extend({
                id: fid,
                pId: tableId,
                wId: o.wId,
                type: "bi.detail_select_data_level0_item",
                layer: 1,
                fieldType: BI.Utils.getFieldTypeByID(fid),
                text: fieldName,
                title: self._getTitleByFieldId(fid),
                value: fid
            }, field));
        });

        if (BI.isNotEmptyObject(map)) {
            BI.each(fields, function (i, field) {
                var id = field.id;
                if (BI.isNotEmptyArray(map[id])) {
                    var fieldName = BI.Utils.getFieldNameByID(id) || "";
                    fieldStructure.push({
                        id: id,
                        pId: tableId,
                        type: "bi.expander",
                        text: fieldName,
                        el: BI.extend({
                            wId: o.wId,
                            text: fieldName,
                            title: self._getTitleByFieldId(id),
                            fieldType: BI.Utils.getFieldTypeByID(id),
                            value: id
                        }, field, {
                            type: "bi.select_data_level1_date_node",
                            layer: 1,
                            isParent: true,
                            open: false
                        }),
                        popup: {
                            type: "bi.select_data_loader",
                            items: self._getSelfCircleFieldsByFieldId(id, map[id] || [])
                        }
                    });
                }
            });
        }
        return fieldStructure;
    },

    setEnable: function (b) {
        BI.SimpleSelectDataService.superclass.setEnable.apply(this, arguments);
        this.searcher.setEnable(b);
    },

    setEnabledValue: function (v) {
        this.searcher.setEnabledValue(v);
    },

    stopSearch: function () {
        this.searcher.stopSearch();
    },

    populate: function () {
        this.searcher.populate.apply(this.searcher, arguments);
    }
});
BI.SimpleSelectDataService.EVENT_CLICK_ITEM = "EVENT_CLICK_ITEM";
$.shortcut("bi.simple_select_data_service", BI.SimpleSelectDataService);
