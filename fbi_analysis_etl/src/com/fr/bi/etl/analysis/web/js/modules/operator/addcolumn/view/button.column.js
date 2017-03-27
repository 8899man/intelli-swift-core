

BI.ColumnButton = FR.extend(BI.BasicButton, {
    _defaultConfig : function () {
        return BI.extend(BI.ColumnButton.superclass._defaultConfig.apply(this, arguments), {
            baseCls:"bi-analysis-etl-operator-button-column",
            height:25,
            fieldType:BICst.COLUMN.STRING,
            fieldName:"aaa",
        })
    },

    _initOpts : function () {
        var o = this.options;
        o.title = o.fieldName
    },
    
    _init : function () {
        BI.ColumnButton.superclass._init.apply(this, arguments);
        var o = this.options;
        var self = this;
        BI.createWidget({
            type:"bi.htape",
            element:this.element,
            items:[{
                type:"bi.icon_button",
                cls: BI.Utils.getFieldClass(o.fieldType),
                width:o.height,
                height:o.height,
                forceNotSelected :true
            }, {
                el:{
                    // type:"bi.center_adapt",
                    height:o.height,
                    // items:[{
                        type:"bi.label",
                        textAlign:"left",
                        text:o.fieldName
                    // }]
                }
            }, {
                type:"bi.icon_button",
                cls: "detail-dimension-set-font set",
                width:o.height,
                height:o.height,
                handler : function () {
                    self.fireEvent(BI.ColumnButton.EVENT_EDIT, o.fieldName)
                }
            }, {
                type:"bi.icon_button",
                cls: "close-font delete",
                width:o.height,
                height:o.height,
                handler : function () {
                    self.fireEvent(BI.ColumnButton.EVENT_DELETE, o.fieldName)
                }
            }]
        })
    }

})
BI.ColumnButton.EVENT_DELETE="event_delete";
BI.ColumnButton.EVENT_EDIT="event_edit";
$.shortcut("bi.etl_button_column", BI.ColumnButton);