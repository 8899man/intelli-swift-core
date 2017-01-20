/**
 * Created by fay on 2016/9/18.
 */
BI.TreeLabelView = BI.inherit(BI.Widget, {
    _constant: {
        LIST_LABEL_HEIGHT: 40,
        DEFAULT_LEFT_GAP: 5
    },

    _defaultConfig: function () {
        return BI.extend(BI.TreeLabelView.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-tree-label-view",
            titleWidth: 60,
            titles: [],
            items: []
        })
    },

    _init: function () {
        BI.TreeLabelView.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.container = BI.createWidget();
        this.items = [];
        this._initView();
    },

    _initView: function () {
        var self = this, o = this.options;
        this.title = BI.createWidget({
            type: "bi.button_group",
            items: BI.createItems(o.titles, {
                type: "bi.label",
                height: this._constant.LIST_LABEL_HEIGHT,
                width: o.titleWidth
            }),
            height: this._constant.LIST_LABEL_HEIGHT * o.titles.length,
            layouts: [{
                type: "bi.vertical"
            }]
        });
        BI.createWidget({
            type: "bi.default",
            element: this.container,
            items: this.items
        });
        this.right = BI.createWidget({
            type: "bi.button_group",
            cls: "list-label-group",
            items: [this.container],
            height: this._constant.LIST_LABEL_HEIGHT * this.items.length,
            layouts: [{
                type: "bi.horizontal"
            }]
        });
        BI.createWidget({
            type: "bi.absolute",
            items: [{
                el: this.title,
                left: 0,
                right: 0,
                top: 0,
                bottom: 0,
                width: 60
            }, {
                el: this.right,
                left: 65,
                right: 0,
                top: 0,
                bottom: 0
            }],
            element: this.element
        });
    },

    _changeView: function (op) {
        var options = {};
        options.id = op.id;
        options.type = op.type;
        options.floor = op.floor;
        options.value = op.value;
        options.selectedValues = this.getValue();
        options.selectedIds = this.getSelectedIds();
        this.fireEvent(BI.TreeLabelView.EVENT_CHANGE, options);
    },

    _setItems: function (items) {
        var self = this;
        var length = this.right.getAllButtons().length;
        var deletes = [];
        for (var i = 0; i < length; i++) {
            deletes.push(i);
        }
        this.right.removeItemAt(deletes);
        self.items = [];
        BI.each(items, function (idx, values) {
            var temp = BI.createWidget({
                type: "bi.list_label",
                items: values,
                showTitle: false
            });
            temp.on(BI.ListLabel.EVENT_CHANGE, function (value, id) {
                self._changeView({
                    floor: idx,
                    value: value,
                    id: id,
                    type: 1
                });
            });
            self.items.push(temp);
        });
        var temp = BI.createWidget({
            type: "bi.default",
            items: self.items
        });
        this.right.addItems([temp]);
        this.right.setHeight(self.items.length * this._constant.LIST_LABEL_HEIGHT);
    },

    _setTitles: function (titles) {
        var length = this.title.getAllButtons().length;
        var deletes = [];
        for (var i = 0; i < length; i++) {
            deletes.push(i);
        }
        this.title.removeItemAt(deletes);
        this.title.addItems(BI.createItems(titles, {
            type: "bi.label",
            height: this._constant.LIST_LABEL_HEIGHT,
            width: this.options.titleWidth
        }));
        this.title.setHeight(titles.length * this._constant.LIST_LABEL_HEIGHT);
    },

    updateView: function (items, floor) {
        var self = this,
            updateList = this.items.slice(floor + 1),
            values = items.slice(floor + 1);
        for (var i = 0; i < updateList.length; i++) {
            if (BI.isNull(values[i])) {
                return;
            }
            if (BI.isEmptyArray(values[i])) {
                for (var j = i; j < updateList.length; j++) {
                    updateList[j].populate({items: []});
                }
                return;
            }
            var originalValue = updateList[i].getValue();
            updateList[i].populate({
                items: values[i]
            });
            updateList[i].setValue(originalValue);

            var currentValue = updateList[i].getValue();
            if (!arraysEqual(originalValue, currentValue)) {     //接着刷新剩余行
                return;
            }
        }

        function arraysEqual(a1, a2) {     //仅考虑数值字符串等简单数据
            if (a1.length !== a2.length) {
                return false;
            }
            BI.each(a2, function (idx, data) {
                if (a1.indexOf(data) === -1) {
                    return false;
                }
            });
            return true;
        }
    },

    refreshView: function (data) {
        data.titles && this._setTitles(BI.isEmpty(data.titles) ? [{
            text: BI.i18nText("BI-Tree_Label_Con") + BI.i18nText("BI-Colon"),
            title: BI.i18nText("BI-Tree_Label_Con")
        }] : data.titles);
        data.items && this._setItems(BI.isEmpty(data.items) ? [[]] : data.items);
    },

    setValue: function (v) {
        var value = [];
        BI.each(this.items, function (idx, item) {
            if (BI.isNotEmptyArray(v[idx])) {
                item.setValue(v[idx] || []);
            }
            if (BI.isEmptyArray(v[idx]) || BI.isNull(v[idx])) {
                value.push([BICst.LIST_LABEL_TYPE.ALL]);
            } else {
                var temp = [];
                // 排除错误的设置的值
                BI.each(item.items, function (idx, itemValue) {
                    temp.push(itemValue.value)
                });
                var valueTemp = BI.intersection(v[idx], temp);
                if (BI.isEmptyArray(valueTemp)) {
                    valueTemp = [BICst.LIST_LABEL_TYPE.ALL];
                }
                value.push(valueTemp);
            }
        });
    },

    getSelectedButtons: function () {
        var result = [];
        BI.each(this.items, function (idx, item) {
            result.push(item.getSelectedButtons());
        });
        return result;
    },

    getSelectedIds: function () {
        var result = [];
        BI.each(this.items, function (idx, item) {
            result.push(item.getSelectedIds());
        });
        return result;
    },

    getValue: function () {
        var result = [];
        BI.each(this.items, function (idx, item) {
            result.push(item.getValue());
        });
        return result;
    }
});
BI.TreeLabelView.EVENT_CHANGE = "BI.TreeLabelView.EVENT_CHANGE";
$.shortcut('bi.tree_label_view', BI.TreeLabelView);