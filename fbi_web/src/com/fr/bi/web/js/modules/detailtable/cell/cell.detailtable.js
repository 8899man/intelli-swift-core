/**
 * Created by roy on 16/5/23.
 */
BI.DetailTableCell = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return {
            baseCls: "bi-detail-table-cell",
            dId: "",
            text: ""
        }
    },

    _init: function () {
        BI.DetailTableCell.superclass._init.apply(this, arguments);
        this._createItem();
    },

    _createItem: function () {
        var o = this.options;
        var dId = this.options.dId;
        var type = BI.Utils.getDimensionTypeByID(dId);
        if (this._checkHyperLinkDimension()) {
            var hyperlink = BI.Utils.getDimensionHyperLinkByID(dId);
            var item = BI.createWidget({
                type: "bi.a",
                cls: "hyper-link-item",
                lgap: 5,
                textAlign: "left",
                text: this.options.text,
                href: hyperlink.expression.replaceAll("\\$\\{.*\\}", this.options.text)
            });

            this._createItemWithStyle(item);

        } else if(type === BICst.TARGET_TYPE.NUMBER || type === BICst.TARGET_TYPE.FORMULA ){
            var item = BI.createWidget({
                type: "bi.label",
                cls: "detail-table-cell-text",
                textAlign: "right",
                whiteSpace: "nowrap",
                height: this.options.height,
                text: this.options.text,
                value: this.options.value,
                lgap: 5,
                rgap: 5
            });
            this._createItemWithStyle(item);
        } else{
            var item = BI.createWidget({
                type: "bi.label",
                cls: "detail-table-cell-text",
                textAlign: "left",
                whiteSpace: "nowrap",
                height: this.options.height,
                text: this.options.text,
                value: this.options.value,
                lgap: 5,
                rgap: 5
            });
            this._createItemWithStyle(item);
        }
    },

    _checkHyperLinkDimension: function () {
        var hyperlink = BI.Utils.getDimensionHyperLinkByID(this.options.dId);
        return hyperlink.used || false
    },

    _parseFloatByDot: function (text, dot, separators) {
        if (text === Infinity || text !== text) {
            return text;
        }
        if (!BI.isNumeric(text)) {
            return text;
        }
        var num = BI.parseFloat(text);
        switch (dot) {
            case BICst.TARGET_STYLE.FORMAT.NORMAL:
                if(separators){
                    num = BI.contentFormat(num, '#,###.##')
                } else {
                    num = BI.contentFormat(num, '#.##')
                }
                return num;
                break;
            case BICst.TARGET_STYLE.FORMAT.ZERO2POINT:
                if(separators){
                    num = BI.contentFormat(num, '#,###')
                } else {
                    num = BI.parseInt(num)
                }
                return num;
                break;
            case BICst.TARGET_STYLE.FORMAT.ONE2POINT:
                if(separators){
                    num = BI.contentFormat(num, '#,###.0')
                } else {
                    num = BI.contentFormat(num, '#.0')
                }
                return num;
            case BICst.TARGET_STYLE.FORMAT.TWO2POINT:
                if(separators){
                    num = BI.contentFormat(num, '#,###.00')
                } else {
                    num = BI.contentFormat(num, '#.00')
                }
                return num;
        }
        return text;
    },

    _getIconByStyleAndMark: function (text, style, mark) {
        var num = BI.parseFloat(text), nMark = BI.parseFloat(mark);
        switch (style) {
            case BICst.TARGET_STYLE.ICON_STYLE.NONE:
                return "";
            case BICst.TARGET_STYLE.ICON_STYLE.POINT:
                if (num > nMark) {
                    return "target-style-more-dot-font";
                } else if (num === nMark) {
                    return "target-style-equal-dot-font"
                } else {
                    return "target-style-less-dot-font";
                }
            case BICst.TARGET_STYLE.ICON_STYLE.ARROW:
                if (num > nMark) {
                    return "target-style-more-arrow-font";
                } else if (num === nMark) {
                    return "target-style-equal-arrow-font";
                } else {
                    return "target-style-less-arrow-font";
                }
        }
        return "";
    },

    _createItemWithStyle: function (item) {
        var o = this.options;
        var iconCls = "", color = "";
        var text = o.text;
        var dId = this.options.dId;
        var styleSettings = BI.Utils.getDimensionSettingsByID(dId);

        var format = styleSettings.format, numLevel = styleSettings.num_level,
            iconStyle = styleSettings.icon_style, mark = styleSettings.mark,
            num_separators = styleSettings.num_separators;
        text = BI.TargetBodyNormalCell.parseNumByLevel(text, numLevel);
        text = this._parseFloatByDot(text, format, num_separators);
        if (text === Infinity) {
            text = "N/0";
        } else if(BI.Utils.getDimensionSettingsByID(dId).num_level === BICst.TARGET_STYLE.NUM_LEVEL.PERCENT) {
            text += "%";
        }
        item.setText(text);

        iconCls = this._getIconByStyleAndMark(text, iconStyle, mark);
        var conditions = styleSettings.conditions;
        BI.some(conditions, function (i, co) {
            var range = co.range;
            var min = BI.parseFloat(range.min), max = BI.parseFloat(range.max);
            var minBoolean = true;
            var maxBoolean = true;
            if (BI.isNumeric(min)) {
                minBoolean = (range.closemin === true ? text >= min : text > min);
            }
            if (BI.isNumeric(max)) {
                maxBoolean = (range.closemax === true ? text <= max : text < max);
            }
            if (minBoolean && maxBoolean) {
                color = co.color;
            }
        });

        if (BI.isNotEmptyString(color)) {
            item.element.css("color", color);
        }
        if (BI.isNotEmptyString(iconCls)) {
            BI.createWidget({
                type: "bi.horizontal_adapt",
                element: this.element,
                items: [item, {
                    type: "bi.default",
                    cls: iconCls,
                    items: [{
                        type: "bi.icon",
                        width: 16,
                        height: 16
                    }],
                    width: 16,
                    height: 16
                }],
                columnSize: ["", 30]
            });
        } else {
            BI.createWidget({
                type: "bi.vertical",
                element: this.element,
                items: [item]
            })
        }

    }

});
$.shortcut("bi.detail_table_cell", BI.DetailTableCell);