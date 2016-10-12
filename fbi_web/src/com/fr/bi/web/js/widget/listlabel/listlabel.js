/**
 * 文本标签
 *
 * Created by fay on 2016/9/11.
 */
BI.ListLabel = BI.inherit(BI.Widget, {

    _constant: {
        MAX_COLUMN_SIZE: 40,
        DEFAULT_LABEL_GAP: 25,
        DEFAULT_LEFT_GAP: 20
    },

    _defaultConfig: function () {
        return BI.extend(BI.ListLabel.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-list-label",
            title: BI.i18nText("BI-List_Label_Con"),
            showTitle: true,
            items: [],
            height: 40
        })
    },

    _init: function () {
        BI.ListLabel.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.items = BI.clone(o.items);
        this.items.unshift({
            text: BI.i18nText("BI-Nolimited"),
            value: "*"
        });
        this.title = BI.createWidget({
            type: "bi.label",
            text: o.title + ":",
            height: o.height
        });
        this.container = BI.createWidget({
            type: "bi.list_label_item_group",
            items: BI.createItems(this.items.slice(0, this._constant.MAX_COLUMN_SIZE), {
                type: "bi.text_button"
            }),
            layouts: [{
                type: "bi.inline_vertical_adapt",
                rgap: this._constant.DEFAULT_LABEL_GAP,
                height: o.height
            }]
        });
        this.container.on(BI.ButtonGroup.EVENT_CHANGE, function (value, obj) {
            self.fireEvent(BI.ListLabel.EVENT_CHANGE, value, obj.options.id);
        });
        this.minTip = BI.createWidget({
            type: "bi.label",
            text: BI.i18nText("BI-No_Selected_Value"),
            disabled: true,
            height: o.height
        });
        this.maxTip = BI.createWidget({
            type: "bi.label",
            text: BI.i18nText("BI-Max_Show_40_Labels"),
            disabled: true,
            height: o.height
        });

        this.checkTipsState(o.items);
        this.right = BI.createWidget({
            type: "bi.horizontal",
            items: [this.container, this.minTip, this.maxTip],
            height: o.height
        });

        o.showTitle ? BI.createWidget({
            type: "bi.absolute",
            items: [{
                el: this.title,
                left:0,
                right:0,
                top:0,
                bottom:0,
                width: 55
            }, {
                el: this.right,
                left: 60,
                right:0,
                top:0,
                bottom:0
            }],
            element: this.element
        }) : BI.createWidget({
            type: "bi.horizontal",
            items: [this.right],
            element: this.element
        });
    },

    addItems: function (v) {
        v = v || [];
        this.checkTipsState(v);
        this.container.addItems(v.slice(0, this._constant.MAX_COLUMN_SIZE - 1));
    },

    checkTipsState: function (v) {
        if (BI.isEmptyArray(v)) {
            this.minTip.setVisible(true);
            this.container.setVisible(false);
        } else {
            this.minTip.setVisible(false);
            this.container.setVisible(true);
        }
        if (v.length >= this._constant.MAX_COLUMN_SIZE) {
            this.maxTip.setVisible(true);
        } else {
            this.maxTip.setVisible(false);
        }
    },

    removeAllItems: function () {
        this.container.removeAllItems();
    },

    getSelectedButtons: function () {
        return this.container.isVisible() ? this.container.getSelectedButtons() : [];
    },

    getAllButtons: function () {
        return this.container.getAllButtons();
    },

    populate: function (v) {
        if(v.title) {
            this.title.setText(v.title);
        }
        this.removeAllItems();
        this.addItems(v.items);
    },

    setValue: function (v) {
        this.container.setValue(v);
    },

    getValue: function () {
        return this.container.getValue();
    }
});

BI.ListLabel.EVENT_CHANGE = 'BI.ListLabel.EVENT_CHANGE';
$.shortcut('bi.list_label', BI.ListLabel);