/**
 * Created by GUY on 2015/6/29.
 * @class BI.DetailSelectDataLevel2Node
 * @extends BI.Widget
 */
BI.DetailSelectDataLevel2Node = BI.inherit(BI.Widget, {
    _defaultConfig: function () {
        return BI.extend(BI.DetailSelectDataLevel2Node.superclass._defaultConfig.apply(this, arguments), {
            extraCls: "bi-detail-select-data-level2-node",
            id: "",
            pId: "",
            layer: 2,
            open: false,
            height: 25
        })
    },
    _init: function () {
        BI.DetailSelectDataLevel2Node.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.node = BI.createWidget({
            type: "bi.select_data_level2_node",
            element: this.element,
            layer: o.layer,
            id: o.id,
            pId: o.pId,
            open: o.open,
            text: o.text,
            value: o.value,
            keyword: o.keyword,
            title: o.title
        });
        this.node.on(BI.Controller.EVENT_CHANGE, function () {
            self.fireEvent(BI.Controller.EVENT_CHANGE, arguments);
        });
        BI.Broadcasts.on(BICst.BROADCAST.DIMENSIONS_PREFIX + o.wId, function () {
            self.setValue([]);
        });
    },

    doRedMark: function () {
        this.node.doRedMark.apply(this.node, arguments);
    },

    unRedMark: function () {
        this.node.unRedMark.apply(this.node, arguments);
    },

    setSelected: function (b) {
        return this.node.setSelected(b);
    },

    isSelected: function () {
        return this.node.isSelected();
    },

    isOpened: function () {
        return this.node.isOpened();
    },

    setOpened: function (v) {
        this.node.setOpened(v);
    },

    setValue: function (items) {
        var vals = [];
        BI.each(items, function (i, item) {
            if (BI.isString(item)) {
                vals.push(BI.Utils.getFieldNameByID(item));
            } else {
                if (BI.isNotNull(item.field_id)) {
                    var name = BI.Utils.getFieldNameByID(item.field_id);

                    if (BI.isNotNull(item.group)) {
                        switch (item.group.type) {
                            case BICst.GROUP.Y:
                                name += "(" + BI.i18nText("BI-Year") + ")";
                                break;
                            case BICst.GROUP.M:
                                name += "(" + BI.i18nText("BI-Basic_Month") + ")";
                                break;
                            case BICst.GROUP.YMD:
                                name += "(" + BI.i18nText("BI-Basic_Date") + ")";
                                break;
                            case BICst.GROUP.S:
                                name += "(" + BI.i18nText("BI-Quarter") + ")";
                                break;
                        }
                    }
                    vals.push(name);
                }
            }
        });
        this.node.setValue(vals);
    },

    setEnable: function (b) {
        BI.DetailSelectDataLevel2Node.superclass.setEnable.apply(this, arguments);
        !b && this.node.isOpened() && this.node.triggerCollapse();
    }
});

$.shortcut("bi.detail_select_data_level2_node", BI.DetailSelectDataLevel2Node);