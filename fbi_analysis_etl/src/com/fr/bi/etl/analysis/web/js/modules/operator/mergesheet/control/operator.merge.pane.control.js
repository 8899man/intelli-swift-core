BI.AnalysisETLOperatorMergeSheetPaneController = BI.inherit(BI.MVCController, {
    
    
    populate : function (widget, model) {
        var tables = model.get(ETLCst.PARENTS);
        widget.table.empty();
        widget.table.populate(widget.createTable(tables));
        widget.mergeFields.populate(widget.createCell(model.getMergeFieldsName(), "cell"), widget.createCell([[tables[0].tableName,tables[1].tableName]], "header"))
        widget.fireEvent(BI.AnalysisETLOperatorAbstractController.PREVIEW_CHANGE, model, ETLCst.ANALYSIS_TABLE_OPERATOR_KEY.NULL)
        widget.fireEvent(BI.TopPointerSavePane.EVENT_FIELD_VALID, model.getValue("columns"))
    },


    editMerge : function (widget, model) {
        var currentSheets = BI.isFunction(this.options.currentTables)? this.options.currentTables(): {};
        var currentTable = [];
        BI.each(model.get(ETLCst.PARENTS), function (idx, item) {
            currentTable.push(item)
        })
        var sheetName = BI.isFunction(this.options.getSheetName) ? this.options.getSheetName(): null;
        var sheetName = sheetName || currentSheets.currentTable["tableName"];
        currentTable = BI.concat(currentTable, currentSheets.others)
        var self = this;
        BI.createWidget({
            type : "bi.analysis_etl_merge_sheet",
            element:BI.Layers.create(BICst.ANALYSIS_MERGE_LAYER, "body"),
            model : BI.extend(model.update(), {
                "tables":currentTable,
                "name":sheetName
            }),
            controller : {
                saveHandler : function(v) {
                    var oldSheets = model.getSheets();
                    if(BI.isFunction(self.options.setSheetName)){
                        self.options.setSheetName(v["name"])
                    };
                    widget.fireEvent(BI.AnalysisETLOperatorCenter.DATA_CHANGE, v);
                    widget.fireEvent(BI.AnalysisETLOperatorMergeSheetPane.STOP_EDITING)
                    if(!BI.isEqual(v["sheets"], oldSheets)) {
                        widget.fireEvent(BI.AnalysisETLOperatorMergeSheetPane.MERGE_SHEET_CHANGE, v, currentTable)
                    }
                },
                cancelHandler : function () {
                    widget.fireEvent(BI.AnalysisETLOperatorMergeSheetPane.STOP_EDITING)
                }
            }
        })
        BI.Layers.show(BICst.ANALYSIS_MERGE_LAYER)
    }
})