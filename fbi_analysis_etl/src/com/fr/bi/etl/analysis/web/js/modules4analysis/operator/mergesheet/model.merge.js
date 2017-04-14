/**
 * Created by windy on 2017/4/12.
 */
BI.AnalysisETLMergeSheetModel = BI.inherit(BI.OB, {

    _defaultConfig: function () {
        var v =  {
            mergeType:BICst.ETL_JOIN_STYLE.LEFT_JOIN
        }
        return v;
    },

    _init : function () {
        BI.AnalysisETLMergeSheetModel.superclass._init.apply(this, arguments);
        this.populate(this.options)
    },

    get: function(key){
        return this.options[key];
    },

    set: function(key, value){
        this.options[key] = value;
    },

    getCopyValue: function(key){
        return BI.deepClone(this.options[key]);
    },

    getChildValue: function(key){
        return this.get(key).update();
    },

    createPreviewData : function () {
        var tables = this.get(ETLCst.PARENTS);
        var merge = this.update();
        merge["mergeColumns"] = this._createMergeColumns();
        merge["leftColumns"] = this._createLeftColumns();
        return {
            left :tables[0],
            right:tables[1],
            merge : merge
        }
    },

    rename : function (idx , name) {
        var columns = this.get("columns");
        columns[idx].fieldName = name;
        return this.setValue("columns", columns)
    },

    getMergeFieldsName : function () {
        var fields = this.get(BI.AnalysisETLMergeSheetModel.MERGE_FIELDS).getCopyValue(ETLCst.FIELDS);
        return  this._createNameArray(fields)
    },

    _createNameArray : function (fields) {
        var tables = this.get(ETLCst.PARENTS);
        var allFields = BI.map(tables, function (idx, item) {
            return BI.Utils.getFieldArrayFromTable(item);
        })
        return BI.map(fields, function (j, item) {
            return BI.map(item, function (idx, item) {
                return allFields[idx][item].fieldName
            })
        })
    },



    getAllSheets : function () {
        return BI.map(this.get("tables"), function (idx, item) {
            return {
                value: item.value,
                text: item.tableName
            }
        })
    },

    getSheets : function () {
        return this.getCopyValue("sheets");
    },

    _getSheets : function () {
        return BI.map(this.get(ETLCst.PARENTS), function (idx, item) {
            return item.value;
        })
    },

    setCurrent2Sheet : function (v) {
        var changed =  this.setValue("sheets", v)
        if(changed === true) {
            var self = this;
            var getTablesBySheetId = function (sheets) {
                var tables = [];
                BI.each(sheets, function (idx, item) {
                    tables.push(getTableById(item))
                })
                return tables;
            }

            var getTableById = function (id) {
                return BI.find(self.get("tables"), function (idx, item) {
                    return item.value === id;
                })
            }
            var tables = getTablesBySheetId(v);
            this.set(ETLCst.PARENTS, tables);
            var mergeModel =  this.get(BI.AnalysisETLMergeSheetModel.MERGE_FIELDS);
            mergeModel.set("tables", tables)
            mergeModel.reset()
        }
        return changed;
    },

    _createMergeColumns : function () {
        var res = [];
        BI.each(this.get("columns"), function (idx, item) {
            if(item._src.length === 2) {
                res.push({
                    index:idx,
                    mergeBy:[item._src[0].fieldName, item._src[1].fieldName]
                })
            }
        })
        return res;
    },

    _createLeftColumns : function () {
        var res = [];
        BI.each(this.get("columns"), function (idx, item) {
            if(item._src.length === 1 && item._src[0].table_index === 0) {
                res.push(idx)
            }
        })
        return res;
    },

    _createMergeFields : function () {
        var res = [];
        var self = this;
        if(BI.isNull(this.get("columns"))){
            return null;
        }
        BI.each(this.get("columns"), function (idx, item) {
            if(item._src.length === 2) {
                res.push([self._getFieldIndex(0, item._src[0].fieldName), self._getFieldIndex(1, item._src[1].fieldName)])
            }
        })
        return res;
    },

    _getFieldIndex : function (idx, fieldName) {
        return BI.findIndex(this.get(ETLCst.PARENTS)[idx][ETLCst.FIELDS], function (idx, item) {
            return item.fieldName === fieldName;
        })
    },

    refreshColumnName : function () {
        var allFields = this.get(BI.AnalysisETLMergeSheetModel.MERGE_FIELDS).getAllFields();
        var oldColumns = this.get("columns");
        var columns = [];
        var tables = this.get(ETLCst.PARENTS);
        var table1Fields = BI.Utils.getFieldArrayFromTable(tables[0]);
        var table2Fields = BI.Utils.getFieldArrayFromTable(tables[1]);
        var getNameArray = function (columns) {
            var array = [];
            BI.each(columns, function (idx, item) {
                array.push(item.fieldName)
            })
            return array;
        }
        BI.each(allFields, function (idx, item) {
            var newFields;
            if(item[0] > -1 && item[1] > -1) {
                newFields = BI.deepClone(table1Fields[item[0]]);
                newFields._src = [BI.extend(BI.deepClone(table1Fields[item[0]]),{
                    table_index: 0
                }), BI.extend(BI.deepClone(table2Fields[item[1]]), {
                    table_index : 1
                })];
            } else if(item[0] > -1) {
                newFields = BI.deepClone(table1Fields[item[0]]);
                newFields._src = [BI.extend(BI.deepClone(table1Fields[item[0]]), {
                    table_index: 0
                })];
            } else {
                newFields = BI.deepClone(table2Fields[item[1]]);
                newFields._src = [BI.extend(BI.deepClone(table2Fields[item[1]]), {
                    table_index: 1
                })];
            }

            var oldColumn = BI.find(oldColumns, function (i, item) {
                var osrc = item._src;
                var nsrc = newFields._src;
                if (osrc.length !== nsrc.length){
                    return false;
                }
                var same = true;
                BI.some(osrc, function (i, itm) {
                    if (itm.fieldType !== nsrc[i].fieldType || itm.fieldName !== nsrc[i].fieldName){
                        same = false;
                        return true;
                    }
                })
                return same;
            })

            newFields.fieldName = BI.Utils.createDistinctName(getNameArray(columns), BI.isNotNull(oldColumn) ? oldColumn.fieldName : newFields.fieldName)
            columns.push(newFields)
        })
        this.set("columns", columns);
    },

    update : function () {
        //处理格式转换
        var mergeType = this.getCopyValue(BI.AnalysisETLMergeSheetModel.MERGE_TYPE).update();
        var json = BI.deepClone(this.options);
        json[BI.AnalysisETLMergeSheetModel.MERGE_TYPE] = mergeType["merge"];
        json[ETLCst.FIELDS] = json["columns"]
        json["etlType"] = ETLCst.ETL_TYPE.MERGE_SHEET;
        delete json["tables"];
        delete json[BI.AnalysisETLMergeSheetModel.MERGE_FIELDS];
        json["tableName"] = json["name"]
        json["operator"] = BI.deepClone(json)
        return json;
    },

    getJoinFieldsLength : function() {
        var fields = this.get(BI.AnalysisETLMergeSheetModel.MERGE_FIELDS);
        if(BI.isNotNull(fields)) {
            return fields.getFieldsLength();
        }
        else{
            return 0;
        }
    },

    populate: function(model){
        this.options = model;
        var operator = this.get("operator");
        if(BI.isNotNull(operator)) {
            this.set(BI.AnalysisETLMergeSheetModel.MERGE_TYPE, operator[BI.AnalysisETLMergeSheetModel.MERGE_TYPE]);
            this.set("columns", operator["columns"]);
        }
        //处理格式转换
        this.set(BI.AnalysisETLMergeSheetModel.MERGE_TYPE, new BI.AnalysisETLMergeSheetCommonModel({
            merge: this.get(BI.AnalysisETLMergeSheetModel.MERGE_TYPE) || BICst.ETL_JOIN_STYLE.LEFT_JOIN
        }));
        var baseTable = this.get("tables") || this.get(ETLCst.PARENTS);
        this.set("tables", baseTable)
        this.set("sheets", this._getSheets())
        var tables = this.get(ETLCst.PARENTS)
        var m = {tables:tables};
        m[ETLCst.FIELDS] = this._createMergeFields();
        this.set(BI.AnalysisETLMergeSheetModel.MERGE_FIELDS, new BI.AnalysisETLMergeSheetFieldsModel(m))
    }
})
BI.AnalysisETLMergeSheetModel.MERGE_TYPE = "mergeType";
BI.AnalysisETLMergeSheetModel.MERGE_FIELDS ="mergeFields";