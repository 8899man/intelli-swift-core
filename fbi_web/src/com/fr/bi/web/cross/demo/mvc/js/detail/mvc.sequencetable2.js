SequenceTable2View = BI.inherit(BI.View, {
    _defaultConfig: function () {
        return BI.extend(SequenceTable2View.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-mvc-sequence-table bi-mvc-layout"
        })
    },

    _init: function () {
        SequenceTable2View.superclass._init.apply(this, arguments);
    },

    _render: function (vessel) {
        var items = [{
            children: [{
                text: "节点1",
                children: [{
                    text: "子节点1",
                    children: [{
                        text: "叶节点1",
                        values: [{text: 11}, {text: 12}, {text: 11}, {text: 12}, {text: 11}, {text: 12}, {text: 112}]
                    }, {
                        text: "叶节点2",
                        values: [{text: 21}, {text: 22}, {text: 21}, {text: 22}, {text: 21}, {text: 22}, {text: 122}]
                    }],
                    values: [{text: 101}, {text: 102}, {text: 101}, {text: 102}, {text: 101}, {text: 102}, {text: 1102}]
                }, {
                    text: "子节点2",
                    children: [{
                        text: "叶节点3",
                        values: [{text: 31}, {text: 32}, {text: 31}, {text: 32}, {text: 31}, {text: 32}, {text: 132}]
                    }, {
                        text: "叶节点4",
                        values: [{text: 41}, {text: 42}, {text: 41}, {text: 42}, {text: 41}, {text: 42}, {text: 142}]
                    }],
                    values: [{text: 201}, {text: 202}, {text: 201}, {text: 202}, {text: 201}, {text: 202}, {text: 1202}]
                }, {
                    text: "子节点3",
                    children: [{
                        text: "叶节点5",
                        values: [{text: 51}, {text: 52}, {text: 51}, {text: 52}, {text: 51}, {text: 52}, {text: 152}]
                    }],
                    values: [{text: 301}, {text: 302}, {text: 301}, {text: 302}, {text: 301}, {text: 302}, {text: 1302}]
                }],
                values: [{text: 1001}, {text: 1002}, {text: 1001}, {text: 1002}, {text: 1001}, {text: 1002}, {text: 11002}]
            }, {
                text: "节点2",
                values: [{text: 2001}, {text: 2002}, {text: 2001}, {text: 2002}, {text: 2001}, {text: 2002}, {text: 12002}]
            }],
            values: [{text: 12001}, {text: 12002}, {text: 12001}, {text: 12002}, {text: 12001}, {text: 12002}, {text: 112002}]
        }];

        var header = [{
            text: "header1"
        }, {
            text: "header2"
        }, {
            text: "header3"
        }, {
            text: "金额",
            tag: 1
        }, {
            text: "金额",
            tag: 2
        }, {
            text: "金额",
            tag: 3
        }, {
            text: "金额",
            tag: 4
        }, {
            text: "金额",
            tag: 5
        }, {
            text: "金额",
            tag: 6
        }, {
            text: "金额",
            tag: 7
        }];

        var crossHeader = [{
            text: "cross1"
        }, {
            text: "cross2"
        }];

        var crossItems = [{
            children: [{
                text: "节点1",
                children: [{
                    text: "子节点1"
                }, {
                    text: "子节点2"
                }],
                values: [0]
            }, {
                text: "节点2",
                children: [{
                    text: "子节点3"
                }, {
                    text: "子节点4"
                }],
                values: [0]
            }],
            values: [0]
        }];
        var table = BI.createWidget({
            type: "bi.sequence_table",
            el: {
                el: {
                    el: {
                        type: "bi.table_tree"
                    }
                }
            },
            sequence: {
                type: "bi.sequence_table_tree_number"
            },
            isNeedResize: true,
            isNeedMerge: true,
            isNeedFreeze: true,
            freezeCols: [0, 1, 2],
            mergeCols: [0, 1, 2],
            columnSize: [100, 100, 100, 100, 100, 100, 100, 100, 100, 100],
            header: header,
            items: items,
            crossHeader: crossHeader,
            crossItems: crossItems
        });

        BI.createWidget({
            type: "bi.absolute",
            element: vessel,
            items: [{
                el: {
                    type: "bi.grid",
                    columns: 1,
                    rows: 1,
                    items: [{
                        column: 0,
                        row: 0,
                        el: table
                    }]
                },
                left: 10,
                right: 10,
                top: 10,
                bottom: 50
            }, {
                el: {
                    type: "bi.button",
                    height: 25,
                    text: "showSequence",
                    handler: function () {
                        table.showSequence();
                    }
                },
                left: 10,
                bottom: 10
            }, {
                el: {
                    type: "bi.button",
                    height: 25,
                    text: "hideSequence",
                    handler: function () {
                        table.hideSequence();
                    }
                },
                right: 10,
                bottom: 10
            }]
        })
    }
});

SequenceTable2Model = BI.inherit(BI.Model, {});