/**
 * Created by 小灰灰 on 2016/4/18.
 */
BI.AnalysisETLOperatorUsePartPaneController = BI.inherit(BI.MVCController, {
    populate : function (widget, model) {
        this._check(widget, model);
        var parent = model.get(SQLCst.PARENTS)[0];
        widget.fieldList.populate(parent[SQLCst.FIELDS]);
        var value = model.get('operator');
        if (!BI.isNull(value)){
            widget.fieldList.setValue(value);
        }
        this.doCheck(widget, model)
        widget.fireEvent(BI.AnalysisETLOperatorAbstractController.PREVIEW_CHANGE, widget.controller, widget.options.value.operatorType)
    },
    
    doCheck  : function (widget, model) {
        widget.fireEvent(BI.TopPointerSavePane.EVENT_CHECK_SAVE_STATUS, this.isValid(widget, model), BI.i18nText("BI-Please_Select_Needed_Field"))
    },

    _check : function (widget, model) {
        model.check();
        widget.fireEvent(BI.TopPointerSavePane.EVENT_FIELD_VALID, model.get(SQLCst.FIELDS))

    },

    isValid : function (widget, model) {
        var parent = model.get(SQLCst.PARENTS)[0];
        return parent[SQLCst.FIELDS].length !== widget.fieldList.getValue().assist.length;
    },
    
    update : function (widget, model) {
        var newFields = [];
        var value = widget.fieldList.getValue();

        var isAll = value.type === BI.ButtonGroup.CHOOSE_TYPE_ALL;
        var contains = function (name) {
            var index = BI.indexOf(value.value, name);
            return isAll ? index < 0 :index > -1
        }
        BI.each(model.get(SQLCst.PARENTS)[0][SQLCst.FIELDS], function (i, item) {
            if (contains(item.field_name)){
                newFields.push(item);
            }
        })
        var table = model.update();
        table.etlType = SQLCst.ETL_TYPE.USE_PART_FIELDS;
        table[SQLCst.FIELDS] = newFields;
        table.operator = value;
        return table;
    },

    isDefaultValue : function (widget, model) {
        return !this.isValid(widget, model)
    }
})