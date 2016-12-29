/**
 * @class BI.PackageTableRelationsPane
 * @extend BI.Widget
 * 单个业务包界面所有表关联
 */
BI.PackageTableRelationsPane = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.PackageTableRelationsPane.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-package-table-relations-pane"
        })
    },

    _init: function () {
        BI.PackageTableRelationsPane.superclass._init.apply(this, arguments);
        this.model = new BI.PackageTableRelationsPaneModel({});
        var self = this;
        this.relationView = BI.createWidget({
            type: "bi.relation_view"
        });
        this.relationView.on(BI.RelationView.EVENT_CHANGE, function (v) {
            self.fireEvent(BI.PackageTableRelationsPane.EVENT_CLICK_TABLE, v);
        });
        BI.createWidget({
            type: "bi.vertical",
            element: this.element,
            items: [this.relationView]
        })
    },

    _createItemsByTableIdsAndRelations: function () {
        var self = this;
        var items = [];
        var tableIds = this.model.getTableIds();
        var fieldsMap = this.model.getFieldsMap();
        var relations = this.model.getRelations();
        var connectSet = relations.connectionSet;
        var allFields = this.model.getAllFields();
        var regionHandler = function () {
            self.fireEvent(BI.PackageTableRelationsPane.EVENT_CLICK_TABLE, this.options.value);
        };
        //var allTableSet = [];
        var degrees = getTableIdsDegree();
        var calcDegree = {};
        var distinctTableIds = [];
        var relationTableSet = [];
        this._getAllRelationTablesByTables(tableIds, relationTableSet);
        BI.each(relationTableSet, function (idx, tId) {
            calcDegree[tId] = 0;
        });
        BI.each(relationTableSet, function (idx, tId) {
            if (BI.contains(distinctTableIds, tId)) {
                return;
            }
            var primFields = self.getFieldsInPrimKeyMap(fieldsMap[tId]);
            var foreFields = self.getFieldsInForeignMap(fieldsMap[tId]);
            if (BI.isNull(fieldsMap[tId])) {
                BI.each(connectSet, function (idx, obj) {
                    var primaryId = obj.primaryKey.field_id;
                    var foreignId = obj.foreignKey.field_id;
                    if (BI.isNotNull(allFields[primaryId]) &&
                        BI.isNotNull(allFields[foreignId])) {
                        var primTableId = allFields[primaryId].table_id;
                        var foreignTableId = allFields[foreignId].table_id;
                        if (tId === primTableId && BI.contains(tableIds, foreignTableId)) {
                            items.push({
                                primary: {
                                    region: tId,
                                    regionText: self.model.getTableTranName(tId),
                                    regionTitle: self.model.getTableTranName(tId),
                                    value: primaryId,
                                    text: self.model.getFieldTranName(primaryId),
                                    title: self.model.getFieldTranName(primaryId)
                                },
                                foreign: {
                                    region: allFields[foreignId].table_id,
                                    regionText: self.model.getTableTranName(allFields[foreignId].table_id),
                                    regionTitle: self.model.getTableTranName(allFields[foreignId].table_id),
                                    value: foreignId,
                                    text: self.model.getFieldTranName(foreignId),
                                    title: self.model.getFieldTranName(foreignId),
                                    regionHandler: regionHandler
                                }
                            });
                        }
                    }
                });
                return;
            }
            if (BI.isEmptyArray(primFields) && BI.isEmptyArray(foreFields)) {
                items.push({
                    primary: {
                        region: tId,
                        regionText: self.model.getTableTranName(tId),
                        regionTitle: self.model.getTableTranName(tId),
                        regionHandler: regionHandler
                    }
                });
            } else {
                items = BI.concat(items, getViewItemsByTableId(tId, []));
            }
            distinctTableIds.push(tId);
        });
        return items;

        function getTableIdsDegree() {
            var degree = {};
            BI.each(tableIds, function (idx, tId) {
                degree[tId] = 0;
            });
            BI.each(connectSet, function (idx, obj) {
                var foreignId = obj.foreignKey.field_id;
                if (BI.has(allFields, foreignId)) {
                    var tableId = allFields[foreignId].table_id;
                    if (!BI.has(degree, tableId)) {
                        degree[tableId] = 0;
                    }
                    degree[tableId]++;
                }
            });
            return degree;
        }

        function getViewItemsByTableId(tId, visitSet) {
            var rels = self.getRelationsByPrimaryId(tId);
            var items = [];
            BI.each(rels, function (idx, rel) {
                var primaryId = rel.primaryKey.field_id, foreignId = rel.foreignKey.field_id;
                if (BI.isNotNull(allFields[primaryId]) && BI.isNotNull(allFields[foreignId])) {
                    var foreignTableId = allFields[foreignId].table_id;
                    //是未访问过的节点且入度未满
                    if (!BI.contains(visitSet, foreignTableId) && !BI.contains(distinctTableIds, tId) && calcDegree[foreignTableId] !== degrees[foreignTableId]) {
                        //自循环
                        if (allFields[primaryId].table_id === allFields[foreignId].table_id) {
                            items.push({
                                primary: {
                                    region: allFields[primaryId].table_id,
                                    regionText: self.model.getTableTranName(allFields[primaryId].table_id),
                                    regionTitle: self.model.getTableTranName(allFields[primaryId].table_id),
                                    value: primaryId,
                                    text: self.model.getFieldTranName(primaryId),
                                    title: self.model.getFieldTranName(primaryId),
                                    regionHandler: regionHandler
                                },
                                foreign: {
                                    region: BI.UUID(),
                                    regionText: self.model.getTableTranName(allFields[foreignId].table_id),
                                    regionTitle: self.model.getTableTranName(allFields[foreignId].table_id),
                                    value: foreignId,
                                    text: self.model.getFieldTranName(foreignId),
                                    title: self.model.getFieldTranName(foreignId)
                                }
                            });
                        } else {
                            var primaryItem = {
                                region: allFields[primaryId].table_id,
                                regionText: self.model.getTableTranName(allFields[primaryId].table_id),
                                regionTitle: self.model.getTableTranName(allFields[primaryId].table_id),
                                value: primaryId,
                                text: self.model.getFieldTranName(primaryId),
                                title: self.model.getFieldTranName(primaryId),
                                regionHandler: regionHandler
                            };
                            var foreignItem = {
                                region: allFields[foreignId].table_id,
                                regionText: self.model.getTableTranName(allFields[foreignId].table_id),
                                regionTitle: self.model.getTableTranName(allFields[foreignId].table_id),
                                value: foreignId,
                                text: self.model.getFieldTranName(foreignId),
                                title: self.model.getFieldTranName(foreignId),
                                regionHandler: regionHandler
                            };
                            if (!BI.contains(self.model.getTableIds(), allFields[foreignId].table_id)) {
                                delete foreignItem.regionHandler;
                            }
                            items.push({
                                primary: primaryItem,
                                foreign: foreignItem
                            });
                            var visittable = BI.concat(visitSet, [tId]);
                            if (!BI.contains(visittable, foreignTableId) && calcDegree[foreignTableId] !== degrees[foreignTableId]) {
                                calcDegree[foreignTableId]++;
                                items = BI.concat(items, getViewItemsByTableId(foreignTableId, visittable));
                                distinctTableIds.pushDistinct(foreignTableId);
                            }
                        }
                    }
                }
            });
            return items;
        }
    },

    getFieldsInPrimKeyMap: function (fieldIds) {
        var relations = this.model.getRelations();
        var primKeyMap = relations.primKeyMap;
        return BI.filter(fieldIds, function (idx, fieldId) {
            return BI.has(primKeyMap, fieldId);
        });
    },

    getFieldsInForeignMap: function (fieldIds) {
        var relations = this.model.getRelations();
        var foreignKeyMap = relations.foreignKeyMap;
        return BI.filter(fieldIds, function (idx, fieldId) {
            return BI.has(foreignKeyMap, fieldId);
        });
    },

    getRelationsByPrimaryId: function (tId) {
        var rel = [];
        var relations = this.model.getRelations();
        var primKeyMap = relations.primKeyMap;
        var fieldsMap = this.model.getFieldsMap();
        var primFields = this.getFieldsInPrimKeyMap(fieldsMap[tId]);
        BI.each(primFields, function (idx, fieldId) {
            rel = BI.concat(rel, primKeyMap[fieldId]);
        });
        return rel;
    },

    _getAllRelationTablesByTables: function (tableIds, resultTables) {
        var self = this;
        var allFields = this.model.getAllFields();
        var relations = this.model.getRelations();
        var primKeyMap = relations.primKeyMap;
        var foreignKeyMap = relations.foreignKeyMap;
        BI.each(tableIds, function (idx, tableId) {
            if (BI.contains(resultTables, tableId)) {
                return;
            }
            resultTables.push(tableId);
            var fieldsMap = self.model.getFieldsMap();
            var primFields = self.getFieldsInPrimKeyMap(fieldsMap[tableId]);
            var foreFields = self.getFieldsInForeignMap(fieldsMap[tableId]);
            BI.each(BI.concat(primFields, foreFields), function (id, fieldId) {
                var rels = [];
                if (BI.has(primKeyMap, fieldId)) {
                    rels = primKeyMap[fieldId];
                    BI.each(rels, function (i, rel) {
                        if (!BI.contains(resultTables, rel.foreignKey.field_id)) {
                            var tId = allFields[rel.foreignKey.field_id].table_id;
                            self._getAllRelationTablesByTables([tId], resultTables);
                        }
                    })
                }
                if (BI.has(foreignKeyMap, fieldId)) {
                    rels = foreignKeyMap[fieldId];
                    BI.each(rels, function (i, rel) {
                        if (!BI.contains(resultTables, rel.primaryKey.field_id)) {
                            var tId = allFields[rel.primaryKey.field_id].table_id;
                            self._getAllRelationTablesByTables([tId], resultTables);
                        }
                    })
                }
            });
        });
    },

    populate: function (items) {
        var self = this, o = this.options;
        this.model.populate(items);
        this.model.getTableNamesOfAllPackages(function () {
            self.relationView.populate(self._createItemsByTableIdsAndRelations());
        });
    },

    getValue: function () {

    },

    setValue: function () {

    }
});
BI.PackageTableRelationsPane.EVENT_CLICK_TABLE = "EVENT_CLICK_TABLE";
$.shortcut("bi.package_table_relations_pane", BI.PackageTableRelationsPane);