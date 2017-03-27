BI.SheetButton = FR.extend(BI.BasicButton, {

    _constant : {
      popupItemHeight:25
    },

    _defaultConfig: function () {
        return BI.extend(BI.SheetButton.superclass._defaultConfig.apply(this, arguments), {
            extraCls: "bi-tab-sheet-button bi-list-item",
            height: 28,
            width:null,
            iconWidth: 13,
            iconHeight: 13,
            text : "sheet1",
            value : 1
        })
    },

    _init : function (){
        BI.SheetButton.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.text = BI.createWidget({
            type: "bi.label",
            cls:"bi-inline-vertical",
            textAlign: "left",
            whiteSpace: "nowrap",
            textHeight: o.height,
            height: o.height,
            hgap: o.hgap,
            text: o.text,
            title: o.text,
            value: o.value,
            py: o.py
        });
        this.combo = BI.createWidget({
            type: "bi.combo",
            isNeedAdjustWidth: false,
            el: {
                type: "bi.icon_trigger",
                extraCls: "icon-analysis-table-set",
                width: o.width,
                height: o.height
            },
            popup: {
                el: {
                    type: "bi.button_group",
                    chooseType:BI.Selection.None,
                    items: BI.createItems(this._createItemList(), {
                        type: "bi.icon_text_item",
                        cls: "bi-list-item-hover",
                        height: this._constant.popupItemHeight
                    }),
                    layouts: [{
                        type: "bi.vertical"
                    }]
                }
            }
        });
        this.combo.on(BI.Combo.EVENT_CHANGE, function (v) {
            self.fireEvent(BI.WidgetCombo.EVENT_CHANGE, v);
            this.hideView();
        })

        //this.combo.element.click(function(e){
        //    e.stopPropagation();
        //})
        BI.createWidget({
            element:this.element,
            type:"bi.inline",
            scrollable : false,
            items: [this.text, {
                type:"bi.center_adapt",
                cls:"bi-inline-vertical",
                height: o.height,
                width : o.height,
                items:[this.combo]
            }]
        })
    },

    setValid : function (isValid) {
        this.noError = isValid
        this._refreshRedMark();
    },

    _refreshRedMark : function () {
        this.noError === true ? this.text.unRedMark("") : this.text.doRedMark(this.options.text);
    },

    setText : function(text) {
        BI.SheetButton.superclass.setText.apply(this, arguments);
        this.text.setText(text);
        this._refreshRedMark();
    },

    _createItemList : function (){
        return [{
            text: BI.i18nText("BI-Basic_Rename"),
            value:ETLCst.ANALYSIS_TABLE_SET.RENAME,
            title: BI.i18nText("BI-Basic_Rename"),
            extraCls:"rename-font"
        },{
            text: BI.i18nText("BI-Basic_Copy"),
            title: BI.i18nText("BI-Basic_Copy"),
            value:ETLCst.ANALYSIS_TABLE_SET.COPY,
            extraCls:"widget-copy-h-font"
        },{
            text: BI.i18nText("BI-Basic_Remove"),
            title: BI.i18nText("BI-Basic_Remove"),
            value:ETLCst.ANALYSIS_TABLE_SET.DELETE,
            extraCls:"widget-delete-h-font"
        }];
    }
})

BI.shortcut("bi.sheet_button", BI.SheetButton);