/**
 * Created by Young's on 2017/4/5.
 */
BI.AnalysisTabSheetButton = BI.inherit(BI.Widget, {
    props: {
        extraCls: "bi-tab-sheet-button bi-list-item",
        height: 28,
        width: 28,
        iconWidth: 13,
        iconHeight: 13,
        text: "sheet1",
        value: 1
    },

    render: function () {
        var self = this, o = this.options;
        this.text = BI.createWidget({
            type: "bi.label",
            cls: "bi-inline-vertical",
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
                    chooseType: BI.Selection.None,
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
        });

        BI.createWidget({
            element: this,
            type: "bi.inline",
            scrollable: false,
            items: [this.text, {
                type: "bi.center_adapt",
                cls: "bi-inline-vertical",
                height: o.height,
                width: o.width,
                items: [this.combo]
            }]
        })
    },

    setValid: function (isValid) {
        this.noError = isValid;
        this._refreshRedMark();
    },

    _refreshRedMark: function () {
        this.noError === true ? this.text.unRedMark("") : this.text.doRedMark(this.options.text);
    },

    setText: function (text) {
        BI.SheetButton.superclass.setText.apply(this, arguments);
        this.text.setText(text);
        this._refreshRedMark();
    },

    _createItemList: function () {
        return [{
            text: BI.i18nText("BI-Rename"),
            value: ETLCst.ANALYSIS_TABLE_SET.RENAME,
            title: BI.i18nText("BI-Rename"),
            extraCls: "rename-font"
        }, {
            text: BI.i18nText("BI-Copy"),
            title: BI.i18nText("BI-Copy"),
            value: ETLCst.ANALYSIS_TABLE_SET.COPY,
            extraCls: "widget-copy-h-font"
        }, {
            text: BI.i18nText("BI-Remove"),
            title: BI.i18nText("BI-Remove"),
            value: ETLCst.ANALYSIS_TABLE_SET.DELETE,
            extraCls: "widget-delete-h-font"
        }];
    }
});
BI.shortcut("bi.analysis_tab_sheet_button", BI.AnalysisTabSheetButton);