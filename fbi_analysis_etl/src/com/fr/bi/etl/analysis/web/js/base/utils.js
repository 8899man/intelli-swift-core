BI.Utils = BI.Utils || {};

BI.extend(BI.Utils, {
    afterSaveTable : function(res){
        BI.each(res, function(i, item){
            BI.extend(Pool[i], item);
        })
        BI.Broadcasts.send(BICst.BROADCAST.PACKAGE_PREFIX);
    },

    afterReNameTable : function (id, name, title) {
        Pool["translations"][id] = name;
        BI.some(Pool["packages"][ETLCst.PACK_ID]['tables'], function (idx, item) {
            if(item.id === id) {
                item.describe = title
                return true;
            }
        })
        BI.Broadcasts.send(BICst.BROADCAST.PACKAGE_PREFIX);
    },

    afterDeleteTable : function (id) {
        delete Pool["tables"][id];
        BI.remove(Pool["packages"][ETLCst.PACK_ID]['tables'], function(i, item){
            return item.id === id
        })
        BI.Broadcasts.send(BICst.BROADCAST.PACKAGE_PREFIX);
    },

    getDescribe : function (id) {
        if (BI.isNotEmptyArray(BI.Utils.getFieldIDsOfTableID(id))){
            var table =  BI.find(Pool["packages"][ETLCst.PACK_ID]['tables'], function(i, item){
                return item.id === id
            })
            return table.describe;
        } else {
            return BI.i18nText('BI-ETL_Temp_Table_Go_On_Editing')
        }
    },

    isTableEditable : function (id) {
        var table =  BI.find(Pool["packages"][ETLCst.PACK_ID]['tables'], function(i, item){
            return item.id === id
        })
        if (BI.isNull(table)){
            return false;
        }
        return table.inedible !== true;
    },

    getAllETLTableNames : function (id) {
        var names = [];
        if (BI.isNull(Pool["packages"][ETLCst.PACK_ID])){
            return names;
        }
        BI.each(Pool["packages"][ETLCst.PACK_ID]['tables'], function(i, item){
            if(item.id !== id) {
                names.push(Pool["translations"][item.id])
            }
        })
        return names;
    },
    getTableTypeByID :function (tableId){
        var source = Pool.tables;
        var table = source[tableId];
        if(!table){
            return BICst.BUSINESS_TABLE_TYPE.NORMAL;
        }
        var key = BICst.JSON_KEYS.TABLE_TYPE;
        if(table[key] === undefined || table[key] === null){
            return ETLCst.BUSINESS_TABLE_TYPE.ANALYSIS_TYPE;
        }
        return table[key];
    },

    getFieldClass: function (type) {
        switch (type) {
            case BICst.COLUMN.STRING:
                return "select-data-field-string-font";
            case BICst.COLUMN.NUMBER:
                return "select-data-field-number-font";
            case BICst.COLUMN.DATE:
                return "select-data-field-date-font";
            default :
                return BI.Utils.getFieldClass(BICst.COLUMN.STRING)
        }
    },

    createDistinctName : function (array, name) {
        var res = name;
        var index = 1;
        while(BI.indexOf(array, res) > -1){
            res = name + index++;
        }
        return res;
    },

    getFieldArrayFromTable : function (table) {
        var fields = [];
        BI.each(table[ETLCst.FIELDS], function (idx, item) {
            fields = BI.concat(fields, item);
        })
        return fields;
    },

    /**
     * 返回数组对象
     * @param tableIds 数组
     * @returns 数组
     */
    getProbablySinglePathTables: function (tableIds) {
        if(BI.isNull(tableIds) || tableIds.length === 0) {
            //不禁用
            return [];
        }
        var fTable = tableIds[0];
        BI.each(tableIds, function (idx, item) {
            var relation = BI.Utils.getPathsFromTableAToTableB(item, fTable);
            if(relation.length === 0) {
                fTable = item;
            }
        });
        var pTables = Pool.foreignRelations[fTable]
        var result = {};
        BI.each(pTables, function (idx, item) {
            if(item.length === 1) {
                result[idx] = true;
            }
        })
        var fTables = Pool.relations[fTable]
        BI.each(fTables, function (idx, item) {
            if(item.length === 1) {
                result[idx] = true;
            }
        })
        result[fTable] = true;
        return BI.map(result, function (idx, item) {
            return idx;
        })
    },

    getTextFromFormulaValue: function (formulaValue, fieldItems) {
        if (BI.isNull(formulaValue) || BI.isNull(fieldItems)){
            return '';
        }
        var formulaString = "";
        var regx = /\$[\{][^\}]*[\}]|\w*\w|\$\{[^\$\(\)\+\-\*\/)\$,]*\w\}|\$\{[^\$\(\)\+\-\*\/]*\w\}|\$\{[^\$\(\)\+\-\*\/]*[\u4e00-\u9fa5]\}|\w|(.)/g;
        var result = formulaValue.match(regx);
        BI.each(result, function (i, item) {
            var fieldRegx = /\$[\{][^\}]*[\}]/;
            var str = item.match(fieldRegx);
            if (BI.isNotEmptyArray(str)) {
                var id = str[0].substring(2, item.length - 1);
                var item = BI.find(fieldItems, function (i, item) {
                    return id === item.value;
                });
                formulaString = formulaString + (BI.isNull(item) ? id : item.text);
            } else {
                formulaString = formulaString + item;
            }
        });
        return formulaString;
    },

    getFieldsFromFormulaValue: function (formulaValue) {
        var fields = [];
        if (BI.isNull(formulaValue)){
            return [];
        }
        var regx = /\$[\{][^\}]*[\}]|\w*\w|\$\{[^\$\(\)\+\-\*\/)\$,]*\w\}|\$\{[^\$\(\)\+\-\*\/]*\w\}|\$\{[^\$\(\)\+\-\*\/]*[\u4e00-\u9fa5]\}|\w|(.)/g;
        var result = formulaValue.match(regx);
        BI.each(result, function (i, item) {
            var fieldRegx = /\$[\{][^\}]*[\}]/;
            var str = item.match(fieldRegx);
            if (BI.isNotEmptyArray(str)) {
                fields.push(str[0].substring(2, item.length - 1));
            } 
        });
        return fields;
    },

    createDateFieldType: function (group) {
        switch (group) {
            case BICst.GROUP.Y :
                return BICst.COLUMN.NUMBER;
            case BICst.GROUP.S :
                return BICst.COLUMN.NUMBER;
            case BICst.GROUP.M :
                return BICst.COLUMN.NUMBER;
            case BICst.GROUP.W :
                return BICst.COLUMN.NUMBER;
            case BICst.GROUP.YMD :
                return BICst.COLUMN.DATE;
        }
    },


    buildData : function(model, widget, callback, filterValueGetter) {
        //测试数据
        var header = [];
        var table = {};
        table[ETLCst.ITEMS] = [model];
        BI.ETLReq.reqPreviewTable(table, function (data) {
            BI.each(model[ETLCst.FIELDS], function(idx, item){
                var head = {
                    text:item.field_name,
                    field_type:item.field_type,
                    field_id:item.field_id,
                    filterValueGetter : filterValueGetter
                };
                head[ETLCst.FIELDS] = model[ETLCst.FIELDS];
                if(model[ETLCst.TYPE] === ETLCst.ETL_TYPE.GROUP_SUMMARY){
                    var dimensions = model[ETLCst.OPERATOR].dimensions;
                    var keys = BI.keys(dimensions);
                    BI.each(head[ETLCst.FIELDS], function(idx, field){
                        var group = dimensions[keys[idx]].group;
                        if(BI.isNotNull(group)){
                            field.group = group.type;
                        }
                    });
                }
                header.push(head);
            });
            callback([data.value, header])
        });

    },

    triggerPreview : function() {
        return function (widget, previewModel, operatorType, type) {
            if (this.innerTrigger == null) {
                this.innerTrigger = new BI.Utils.ThreadRunTrigger();
            }
            var callBack = function (data) {
                widget.setPreviewOperator(operatorType);
                widget.populatePreview.apply(widget, data)
            };
            var ajaxObject =  {
                work : function (callBack) {
                    BI.Utils.buildData(previewModel.update(), widget.previewTable, callBack, widget.controller.getFilterValue);
                }
            };
            var maskElement = widget.previewTable;
            switch (type) {
                case ETLCst.PREVIEW.SELECT : {
                    ajaxObject = {
                        work: function (callBack) {
                            BI.Utils.buildData(previewModel.update4Preview(), widget.previewTable, callBack, widget.controller.getFilterValue)
                        }
                    }

                    break;
                }
                case  ETLCst.PREVIEW.MERGE : {
                    ajaxObject = {
                        work: function (callBack) {
                            BI.concat(BI.Utils.buildData(previewModel, widget, callBack), operatorType);
                        }
                    }
                    callBack = function (data) {
                        widget.populate.apply(widget, data);
                    };
                    maskElement = widget;
                    break;
                }
                default : {
                    break;
                }

            }
            this.innerTrigger(ajaxObject, callBack, function () {
                return BI.createWidget({
                    type: "bi.etl_loading_mask",
                    masker: maskElement.element,
                    text: BI.i18nText("BI-Loading")
                });
            });
        }
    }

})

BI.ThreadRun = BI.inherit(FR.OB, {
    _init : function () {
        BI.ThreadRun.superclass._init.apply(this, arguments);
        this.triggerIndex = this.options.triggerIndex;
    },

    getTriggerIndex: function(){
        return this.triggerIndex;
    },

    submit : function (runner) {
        runner.apply(runner, this.options.args)
    },

})


BI.Utils.ThreadRunTrigger = function () {
    return BI.throttle(function (ajaxObject, callback, mask) {
        if(this.triggerIndex == null){
            this.triggerIndex = 0;
        }
        this.triggerIndex++;
        var runner = new BI.ThreadRun({
            args:arguments,
            triggerIndex:this.triggerIndex
        })
        var self = this;
        if(self.currentMask == null && mask != null) {
            self.currentMask = mask();
        }
        runner.submit(function () {
            ajaxObject.work(function () {
                if(runner.getTriggerIndex() == self.triggerIndex){
                    if(self.currentMask != null) {
                        self.currentMask.destroy()
                        self.currentMask = null;
                    }
                    callback.apply(this, arguments);
                }

            })
        })
    },300)
}
