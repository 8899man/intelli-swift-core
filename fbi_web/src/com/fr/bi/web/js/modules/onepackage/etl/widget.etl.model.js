/**
 * Created by Young's on 2016/3/11.
 */
BI.ETLModel = BI.inherit(FR.OB, {
    _init: function () {
        BI.ETLModel.superclass._init.apply(this, arguments);
        var o = this.options;
        this.id = o.id;
        this.table = o.table;
        this._prepareData();
    },

    _prepareData: function () {
        var self = this;
        var finalTable = [this.table];
        this.tablesMap = {};
        this.fields = this.table.fields;
        this._addId2Tables(finalTable, this.tablesMap);
        this.allTableIds = this._getTablesId(finalTable, []);
        this.allTables = [finalTable];

        //这里的转义只是放了当前表的所有转义信息
        this.translations = {};
        this.translations[this.id] = BI.Utils.getTransNameById4Conf(this.id);
        BI.each(this.fields, function (i, fs) {
            BI.each(fs, function (j, field) {
                var fieldName = BI.Utils.getTransNameById4Conf(field.id);
                if (BI.isNotNull(fieldName)) {
                    self.translations[field.id] = fieldName;
                }
            });
        });
    },

    getId: function () {
        return this.id;
    },

    isNewTable: function () {
        return this.isNew;
    },

    getTableData: function () {
        return BI.deepClone(this.tableData);
    },

    getFields: function () {
        return BI.deepClone(this.fields);
    },

    getTranslations: function () {
        return this.translations;
    },

    setFields: function (fields) {
        var self = this;
        BI.each(fields, function (i, fs) {
            BI.each(fs, function (j, field) {
                field.id = self._getCurrentFieldIdByFieldInfo(field);
                field.is_usable = BI.isNotNull(BI.Utils.getFieldUsableById4Conf(field.id)) ?
                    BI.Utils.getFieldUsableById4Conf(field.id) : true;
                field.table_id = self.id;
            });
        });
        var preFieldIds = BI.pluck(BI.flatten(this.fields), "id"),
            newFieldIds = BI.pluck(BI.flatten(fields), "id");
        if (newFieldIds.length < preFieldIds) {
            var diffFieldIds = BI.difference(preFieldIds, newFieldIds);
            BI.each(diffFieldIds, function (i, id) {
                BI.Utils.removeRelationByFieldId4Conf(id);
            });
        }
        this.fields = fields;
    },

    setTranslations: function (translations) {
        this.translations = translations;
    },

    setTableName: function (name) {
        this.translations[this.id] = name;
    },

    setFieldsUsable: function (usedFields) {
        BI.each(this.fields, function (i, fs) {
            BI.each(fs, function (j, field) {
                field.is_usable = usedFields.contains(field.id);
            });
        });
    },

    /**
     * 行列转化用的，原始字段名和转义名的一一对应
     */
    constructFieldNameAndTranslationFieldNameRelation: function () {
        var fieldsIdName = [];
        BI.each(this.getFields(), function (idx, arr) {
            BI.each(arr, function (id, field) {
                fieldsIdName[field.field_name] = BI.Utils.getTransNameById4Conf(field.id);
            });
        });
        return fieldsIdName;
    },

    /**
     * 会减少字段的etl操作不提供带关联的字段
     *
     */
    constructFieldNamesWhichHasRelation: function () {
        var fieldsName = [];
        var primKeyMap = this.relations.primKeyMap, foreignKeyMap = this.relations.foreignKeyMap;
        BI.each(this.getFields(), function (idx, arr) {
            BI.each(arr, function (id, field) {
                if (BI.has(primKeyMap, field.id) || BI.has(foreignKeyMap, field.id)) {
                    fieldsName.push(field.field_name);
                }
            });
        });
        return fieldsName;
    },

    //为了复用字段id
    _getCurrentFieldIdByFieldInfo: function (field) {
        if (BI.isNotNull(BI.Utils.getFieldNameById4Conf(field.id))) {
            return field.id;
        }
        return BI.Utils.getFieldIdByNameAndTableId4Conf(field.field_name, this.id) || BI.UUID();
    },

    getAllTables: function () {
        return BI.deepClone(this.allTables);
    },

    getAllTableIds: function () {
        return BI.deepClone(this.allTableIds);
    },

    getTablesMap: function () {
        return BI.deepClone(this.tablesMap);
    },

    getTableById: function (id) {
        return BI.deepClone(this.tablesMap[id]);
    },

    getFieldById: function(id) {
        var field;
        BI.each(this.fields, function(i, fs) {
            BI.each(fs, function(j, f) {
                if (f.id === id) {
                    field = f;
                }
            });
        });
        return field;
    },

    // relation不考虑取消的问题实时的保存
    setRelations: function (fieldId, relations) {
        BI.Utils.saveRelations4Conf(relations, fieldId);
        //同步到后台
        BI.Utils.updateRelation4Conf({
            id: fieldId,
            relations: relations
        }, BI.emptyFn, BI.emptyFn);
    },

    setTranslationsByETLValue: function (etl) {
        var self = this;
        if (BI.has(etl, "etl_type") && BI.isEqual(etl.etl_type, "convert")) {
            var etlValue = etl.etl_value;
            var translations = this.getTranslations();
            var transText = [], text = [];
            var assertArray = function (array) {
                if (BI.isEmpty(array[1])) {
                    array[1] = array[0];
                }
                return array;
            };

            BI.each(etlValue.lc_values, function (idx, lc) {
                lc = assertArray(lc);
                BI.each(etlValue.columns, function (id, co) {
                    co = assertArray(co);
                    transText.push(lc[1] + "-" + co[1]);
                    text.push(lc[0] + "-" + co[0]);
                });
            });

            BI.each(transText, function (idx, name) {
                if (!BI.contains(text, name)) {
                    translations[getFieldIdByFieldName(text[idx])] = name;
                }
            });
            this.setTranslations(translations);
        }

        function getFieldIdByFieldName(field_name) {
            var id = null;
            BI.find(self.fields, function (idx, fieldArray) {
                return BI.find(fieldArray, function (i, field) {
                    if (field.field_name === field_name) {
                        id = field.id;
                        return true;
                    }
                    return false;
                });
            });
            return id;
        }
    },

    getUpdateSettings: function () {
        return BI.deepClone(this.updateSettings);
    },

    setUpdateSettings: function (updateSettings) {
        this.updateSettings = updateSettings;
    },


    getAllFields: function () {
        return BI.deepClone(this.allFields);
    },

    getExcelView: function () {
        return BI.deepClone(this.excelView);
    },

    getFieldNamesByTableId: function (tId) {
        var table = this.getTableById(tId);
        var fieldNames = [];
        if (BI.isNotNull(table)) {
            BI.each(table.fields, function (i, fs) {
                BI.each(fs, function (j, field) {
                    fieldNames.push(field.field_name);
                });
            });
        }
        return fieldNames;
    },

    modifyExcelData: function (tId, fullFileName) {
        var table = this.getTableById(tId);
        table.full_file_name = fullFileName;
        this.saveTableById(tId, table);
    },

    isValidTableTranName: function (name) {
        var self = this;
        var packId = BI.Utils.getPackageIdByTableId4Conf(this.id);
        var tableIds = BI.Utils.getTablesIdByPackageId4Conf(packId);
        var isValid = true;
        BI.some(tableIds, function (i, tId) {
            if (tId !== self.id && BI.Utils.getTransNameById4Conf(tId) === name) {
                isValid = false;
                return true;
            }
        });
        return isValid;
    },

    /**
     * 添加新表 考虑业务包表 可以把相关的关联和转义全部放进来
     * @param tables
     */
    addNewTables: function (tables) {
        var self = this;
        BI.each(tables, function (i, table) {
            if (BI.isNotNull(table.id)) {
                self._addPackageTable(table);
            }
            var newIdsMap = {};
            self._addUUID2Tables([table], newIdsMap);
            BI.extend(self.tablesMap, newIdsMap);
            self.allTables.push([table]);
            self.allTableIds = self.getAllTableIds().concat(self._getTablesId([table], []));
        });
    },

    setExcelView: function (excelView) {
        this.excelView = excelView;
    },

    removeOneTable: function (tId) {
        var self = this;
        BI.some(this.allTables, function (i, tables) {
            //需要看是否含有子节点，1、没有直接删除 2、有的话还要把直接子节点拿出来放到allTables里面
            if (tables[0].id === tId) {
                self.allTables.splice(i, 1);
                var childTables = tables[0].tables;
                BI.each(childTables, function (j, cTable) {
                    self.allTables.push([cTable]);
                });
                BI.some(self.allTableIds, function (k, id) {
                    if (id === tId) {
                        self.allTableIds.splice(k, 1);
                        return true;
                    }
                });
                return true;
            }
        });
    },

    saveTableById: function (tId, data) {
        var self = this;
        if (BI.isNotNull(data.translations)) {
            this.setTranslations(data.translations);
        }
        if (BI.isNotNull(data.relations)) {
            this.setRelations(data.relations);
        }

        if (this.getAllTables().length > 1 &&
            (data.etl_type === BICst.ETL_OPERATOR.UNION || data.etl_type === BICst.ETL_OPERATOR.JOIN)) {
            var sAllTables = [];
            BI.each(this.getAllTables(), function (i, tables) {
                if (BI.isNotNull(tables) && tables[0].id === tId || !self._getTablesId(data.tables, []).contains(tables[0].id)) {
                    sAllTables.push(tables);
                }
            });
            this.allTables = sAllTables;
        }

        //changed的子节点需要全部替换为UUID，避免出现重复的
        self._addUUID2Tables(data.tables, {});

        //遍历一下这个etl树，找到修改的节点，替换掉
        self._replaceNodeInAllTables(BI.extend(data, {id: tId}));
        this.tablesMap = {};
        this.allTableIds = [];
        BI.each(this.getAllTables(), function (i, tables) {
            self._addId2Tables(tables, self.tablesMap);
            self.allTableIds = self.getAllTableIds().concat(self._getTablesId(tables, []));
        });
    },

    refresh4Fields: function (data) {
        var self = this;
        var fields = data.fields, oFields = this.fields;

        function getFieldId(name, fields) {
            var fieldId = BI.UUID();
            BI.some(fields, function (i, fs) {
                return BI.some(fs, function (j, field) {
                    if (field.field_name === name) {
                        fieldId = field.id;
                        return true;
                    }
                });
            });
            return fieldId;
        }

        BI.each(fields, function (i, fs) {
            BI.each(fs, function (j, field) {
                field.id = getFieldId(field.field_name, oFields);
                self.allFields[field.id] = field;
            });
        });
    },

    //添加业务包表
    _addPackageTable: function (table) {
        var self = this;
        var id = BI.UUID(), tableId = table.id;
        table.id = id;
        var fieldIds = [], oFields = BI.deepClone(table.fields);
        BI.each(table.fields, function (j, fs) {
            BI.each(fs, function (k, field) {
                var fId = BI.UUID();
                fieldIds.push(fId);
                //字段的转义
                if (BI.isNotNull(field.id) && BI.isNotNull(self.translations[field.id])) {
                    self.translations[fId] = self.translations[field.id];
                }

                field.id = fId;
                field.table_id = id;
                self.allFields[fId] = field;
            });
        });

        var connectionSet = this.relations.connectionSet,
            primaryKeyMap = this.relations.primKeyMap,
            foreignKeyMap = this.relations.foreignKeyMap;
        var addedConns = [], addedPriMap = {}, addedForMap = {};
        BI.each(connectionSet, function (k, keys) {
            var copyRelation = self._getCopyOfRelation(keys, oFields, fieldIds, tableId, id);
            if (BI.isNotEmptyObject(copyRelation)) {
                addedConns.push(copyRelation);
            }
        });
        this.relations.connectionSet = connectionSet.concat(addedConns);
        BI.each(primaryKeyMap, function (pfId, maps) {
            var addedPris = [], nPKId = null;
            BI.each(maps, function (k, keys) {
                var copyRelation = self._getCopyOfRelation(keys, oFields, fieldIds, tableId, id);
                if (BI.isNotEmptyObject(copyRelation)) {
                    nPKId = copyRelation.primaryKey.field_id;
                    addedPris.push(copyRelation);
                }
            });
            if (addedPris.length > 0 && BI.isNotNull(nPKId)) {
                addedPriMap[nPKId] = addedPris;
            }
        });
        BI.each(addedPriMap, function (pkId, ms) {
            var pkMaps = self.relations.primKeyMap[pkId];
            if (BI.isNotNull(pkMaps)) {
                self.relations.primKeyMap[pkId] = pkMaps.concat(ms);
            } else {
                self.relations.primKeyMap[pkId] = ms;
            }
        });
        BI.each(foreignKeyMap, function (ffId, maps) {
            var addedFors = [], nFKId = null;
            BI.each(maps, function (k, keys) {
                var copyRelation = self._getCopyOfRelation(keys, oFields, fieldIds, tableId, id);
                if (BI.isNotEmptyObject(copyRelation)) {
                    nFKId = copyRelation.foreignKey.field_id;
                    addedFors.push(copyRelation);
                }
            });
            if (addedFors.length > 0 && BI.isNotNull(nFKId)) {
                addedForMap[nFKId] = addedFors;
            }
        });
        BI.each(addedForMap, function (fkId, ms) {
            var fkMaps = self.relations.foreignKeyMap[fkId];
            if (BI.isNotNull(fkMaps)) {
                self.relations.foreignKeyMap[fkId] = fkMaps.concat(ms);
            } else {
                self.relations.foreignKeyMap[fkId] = ms;
            }
        });
    },

    //copy一份relation
    _getCopyOfRelation: function (keys, oFields, fieldIds, oTableId, nTableId) {
        var self = this;
        var primKey = keys.primaryKey, foreignKey = keys.foreignKey;
        var relation = {};
        BI.each(oFields, function (i, ofs) {
            BI.each(ofs, function (j, oField) {
                if (oField.id === primKey.field_id) {
                    var nPK = {}, nFK = BI.deepClone(foreignKey);
                    BI.each(fieldIds, function (k, fid) {
                        if (self.allFields[fid] && self.allFields[primKey.field_id] &&
                            self.allFields[fid].field_name === self.allFields[primKey.field_id].field_name) {
                            nPK = {
                                field_id: fid,
                                table_id: nTableId
                            }
                        }
                    });
                    relation = {
                        primaryKey: nPK,
                        foreignKey: nFK
                    }
                }
                if (oField.id === foreignKey.field_id) {
                    var nPK = BI.deepClone(primKey), nFK = {};
                    BI.each(fieldIds, function (k, fid) {
                        if (self.allFields[fid] && self.allFields[foreignKey.field_id] &&
                            self.allFields[fid].field_name === self.allFields[foreignKey.field_id].field_name) {
                            nFK = {
                                field_id: fid,
                                table_id: nTableId
                            }
                        }
                    });
                    relation = {
                        primaryKey: nPK,
                        foreignKey: nFK
                    }
                }
            });
        });
        return relation;
    },

    _getDistinctTableName: function (name) {
        var self = this;
        var allTableNameTrans = [];
        var currentPackTables = BI.Utils.getCurrentPackageTables4Conf();
        var translations = this.getTranslations();
        BI.each(currentPackTables, function (tid, table) {
            if (tid !== self.getId()) {
                allTableNameTrans.push({name: translations[tid]});
            }
        });
        return BI.Func.createDistinctName(allTableNameTrans, name);
    },

    //自己有id的table使用原来的
    _addId2Tables: function (tables, ids, isTemp) {
        var self = this;
        BI.each(tables, function (i, table) {
            var id = table.id || BI.UUID();
            if (BI.isNotNull(table.tables)) {
                self._addId2Tables(tables[i].tables, ids, true);
                tables[i] = BI.extend(table, {
                    id: id,
                    tables: tables[i].tables,
                    temp_name: table.temp_name || (table.tables[0].temp_name + "_" + table.etl_type)
                });
            } else {
                tables[i] = BI.extend(table, {
                    id: id,
                    temp_name: isTemp ?
                        (table.temp_name || BI.Utils.getTransNameById4Conf(id) || table.table_name) :
                        (BI.Utils.getTransNameById4Conf(id) || table.table_name)
                });
            }
            ids[id] = tables[i];
        });
    },

    //给tables添加新的uuid
    _addUUID2Tables: function (tables, ids) {
        var self = this;
        BI.each(tables, function (i, table) {
            var id = BI.UUID();
            if (BI.isNotNull(table.tables)) {
                self._addUUID2Tables(tables[i].tables, ids);
                tables[i] = BI.extend(table, {
                    id: id,
                    tables: tables[i].tables
                });
            } else {
                tables[i] = BI.extend(table, {
                    id: id
                });
            }
            ids[id] = tables[i];
        });
    },

    _replaceNode: function (tables, newNode) {
        var self = this;
        BI.some(tables, function (i, table) {
            if (table.id === newNode.id) {
                tables[i] = newNode;
                return true;
            }
            if (BI.isNotNull(table.tables)) {
                tables[i].tables = self._replaceNode(table.tables, newNode);
            }

        });
        return tables;
    },

    _replaceNodeInAllTables: function (newNode) {
        var self = this;
        var allTables = BI.deepClone(this.getAllTables());
        BI.each(allTables, function (i, tables) {
            allTables[i] = self._replaceNode(tables, newNode);
        });
        this.allTables = allTables;
    },

    _getTablesId: function (tables, tableIds) {
        var self = this;
        BI.each(tables, function (i, table) {
            if (BI.isNotNull(table.tables)) {
                tableIds = self._getTablesId(table.tables, tableIds);
            }
            tableIds.push(table.id);
        });
        return tableIds;
    },

    getValue: function () {
        var finalTable = this.getAllTables()[0][0];
        var data = {
            id: this.getId(),
            translations: this.getTranslations(),
            relations: this.getRelations(),
            all_fields: this.getAllFields(),
            fields: this.getFields(),
            excel_view: this.getExcelView(),
            update_settings: this.getUpdateSettings()
        };
        if (BI.isNotNull(finalTable.etl_type)) {
            data.etl_type = finalTable.etl_type;
            data.etl_value = finalTable.etl_value;
            data.tables = finalTable.tables;
            data.connection_name = finalTable.connection_name;
        } else {
            data = BI.extend(finalTable, data);
        }
        return data;
    },

    saveTable: function (callback) {
        var self = this;
        var mask = BI.createWidget({
            type: "bi.loading_mask",
            masker: BICst.BODY_ELEMENT,
            text: BI.i18nText("BI-Loading")
        });
        var table = this.getAllTables()[0][0];
        table.id = this.id;
        BI.Utils.updateOneTable4Conf({
            table: table,
            translations: self.translations,
            excel_view: {},
            update_settings: {}
        }, function () {
            callback();
        }, function () {
            mask.destroy();
        })
    }
});
