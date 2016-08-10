/**
 * Created by GUY on 2015/9/15.
 * @class BI.SelectDataLevel1DateNode
 * @extends BI.NodeButton
 */
BI.SelectDataLevel1DateNode = BI.inherit(BI.NodeButton, {
    _defaultConfig: function () {
        return BI.extend(BI.SelectDataLevel1DateNode.superclass._defaultConfig.apply(this, arguments), {
            extraCls: "bi-select-data-level1-date-node bi-list-item",
            id: "",
            pId: "",
            layer: 1,
            open: false,
            height: 25
        })
    },

    _getFieldClass: function (type) {
        switch (type) {
            case BICst.COLUMN.STRING:
                return "select-data-field-string-group-font";
            case BICst.COLUMN.NUMBER:
                return "select-data-field-number-group-font";
            case BICst.COLUMN.DATE:
                return "select-data-field-date-group-font";
            case BICst.COLUMN.COUNTER:
                return "select-data-field-number-group-font";
            default:
                return "select-data-field-date-group-font";
        }
    },

    _init: function () {
        BI.SelectDataLevel1DateNode.superclass._init.apply(this, arguments);
        var self = this, o = this.options;

        this.button = BI.createWidget({
            type: "bi.icon_text_item",
            cls: this._getFieldClass(o.fieldType),
            text: o.text,
            value: o.value,
            keyword: o.keyword,
            height: o.height,
            textLgap: 10,
            textRgap: 5
        });

        this.checkbox = BI.createWidget({
            type: "bi.tree_group_node_checkbox"
        });
        this.checkbox.on(BI.Controller.EVENT_CHANGE, function (type) {
            if (type === BI.Events.CLICK) {
                self.setSelected(self.isSelected());
            }
            self.fireEvent(BI.Controller.EVENT_CHANGE, arguments);
        });
        this.button.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.Controller.EVENT_CHANGE, arguments);
        });
        BI.createWidget({
            type: "bi.htape",
            element: this.element,
            items: [{
                el: {
                    type: "bi.layout"
                },
                width: o.layer * 20
            }, {
                el: this.button
            }, {
                el: this.checkbox,
                width: 25
            }]
        })
    },

    doRedMark: function () {
        this.button.doRedMark.apply(this.button, arguments);
    },

    unRedMark: function () {
        this.button.unRedMark.apply(this.button, arguments);
    },

    doClick: function () {
        BI.SelectDataLevel1DateNode.superclass.doClick.apply(this, arguments);
        this.checkbox.setSelected(this.isOpened());
    },

    setOpened: function (v) {
        BI.SelectDataLevel1DateNode.superclass.setOpened.apply(this, arguments);
        this.checkbox.setSelected(v);
    },

    setValue: function (items) {
        BI.SelectDataLevel1DateNode.superclass.setValue.apply(this, arguments);
    },

    setEnable: function (b) {
        BI.SelectDataLevel1DateNode.superclass.setEnable.apply(this, arguments);
        this.checkbox.setEnable(b);
    }
});

$.shortcut("bi.select_data_level1_date_node", BI.SelectDataLevel1DateNode);