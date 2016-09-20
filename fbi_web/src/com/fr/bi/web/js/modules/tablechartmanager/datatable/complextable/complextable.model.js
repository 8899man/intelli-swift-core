/**
 * Created by Young's on 2016/9/14.
 */
BI.ComplexTableModel = BI.inherit(FR.OB, {

    constant: {
        OUTER_SUM: "__outer_sum__"
    },

    _init: function () {
        BI.ComplexTableModel.superclass._init.apply(this, arguments);
        var self = this;
        this.wId = this.options.wId;
        this.status = this.options.status;      //一个恶心的属性，来自于详细设置，查看真实数据
        this.EMPTY_VALUE = BI.UUID();
        this._refreshDimsInfo();

        //展开的节点的树结构，需要保存
        this.tree = new BI.Tree();
        this.crossTree = new BI.Tree();

        this.page = [0, 0, 0, 0, 0];
        this.eTree = new BI.Tree();         //展开节点——维度
        this.crossETree = new BI.Tree();    //展开节点——系列，用于交叉表

        this.clickValue = "";               //点击的值
        this.pageOperator = BICst.TABLE_PAGE_OPERATOR.REFRESH;  //翻页操作

        //当当前组件删除的时候删除存储的区域columnSize缓存
        BI.Broadcasts.on(BICst.BROADCAST.WIDGETS_PREFIX + this.wId, function () {
            self.deleteStoredRegionColumnSize();
        });
    },

    getStoredRegionColumnSize: function () {
        var columnSize = BI.Cache.getItem(BICst.CACHE.REGION_COLUMN_SIZE_PREFIX + this.wId);
        if (BI.isKey(columnSize)) {
            return [BI.parseInt(columnSize), ""];
        }
        return false;
    },

    setStoredRegionColumnSize: function (columnSize) {
        if (BI.isKey(columnSize)) {
            BI.Cache.setItem(BICst.CACHE.REGION_COLUMN_SIZE_PREFIX + this.wId, columnSize);
        }
    },

    deleteStoredRegionColumnSize: function () {
        BI.Cache.removeItem(BICst.CACHE.REGION_COLUMN_SIZE_PREFIX + this.wId);
    },

    getWidgetId: function () {
        return this.wId;
    },

    getStatus: function () {
        return this.status;
    },

    isNeed2Freeze: function () {
        if (this.targetIds.length === 0 || (this.dimIds.length + this.crossDimIds.length) === 0) {
            return false;
        }
        return this.freezeDim;
    },

    getFreezeCols: function () {
        return this.isNeed2Freeze() ? this.freezeCols : [];
    },

    getMergeCols: function () {
        return this.mergeCols;
    },

    getColumnSize: function () {
        var columnSize = [];
        BI.each(this.columnSize, function (i, size) {
            if (size < 80) {
                size = 80;
            }
            columnSize.push(size);
        });
        return columnSize;
    },

    getHeader: function () {
        return this.header;
    },

    getCrossHeader: function () {
        return this.crossHeader;
    },

    getItems: function () {
        return this.items;
    },

    getCrossItems: function () {
        return this.crossItems;
    },

    getPage: function () {
        return this.page;
    },

    getData: function () {
        return this.data;
    },

    getPageOperator: function () {
        return this.pageOperator;
    },

    isShowNumber: function () {
        return this.showNumber;
    },

    getThemeColor: function () {
        return this.themeColor;
    },

    getTableForm: function () {
        return this.tableForm;
    },

    getTableStyle: function () {
        return this.tableStyle;
    },

    setPageOperator: function (pageOperator) {
        this.pageOperator = pageOperator;
    },

    getExtraInfo: function () {
        var op = {};
        op.expander = {
            x: {
                type: BI.Utils.getWSOpenColNodeByID(this.wId),
                value: [this._formatExpanderTree(this.crossETree.toJSONWithNode())]
            },
            y: {
                type: BI.Utils.getWSOpenRowNodeByID(this.wId),
                value: [this._formatExpanderTree(this.eTree.toJSONWithNode())]
            }
        };
        op.clickvalue = this.clickValue;
        op.page = this.pageOperator;
        op.status = this.status;
        op.real_data = true;
        if (this.status === BICst.WIDGET_STATUS.DETAIL) {
            op.real_data = BI.Utils.isShowWidgetRealDataByID(this.wId) || false;
        }
        return op;
    },

    setDataAndPage: function (data) {
        this.data = data.data;
        this.page = data.page;
    },

    _refreshDimsInfo: function () {
        var self = this;
        this.dimIds = [];
        this.crossDimIds = [];
        var view = BI.Utils.getWidgetViewByID(this.wId);
        var regionIds = BI.keys(view);
        var sortedRegions = BI.sortBy(regionIds);
        //行表头、列表头都只需要第一个分组中使用中的维度
        BI.some(sortedRegions, function (i, sRegion) {
            if (BI.parseInt(sRegion) < BI.parseInt(BICst.REGION.DIMENSION2)) {
                BI.each(view[sRegion], function (j, dId) {
                    BI.Utils.isDimensionUsable(dId) && (self.dimIds.push(dId));
                });
                return true;
            }
        });
        BI.some(sortedRegions, function (i, sRegion) {
            if (BI.parseInt(BICst.REGION.DIMENSION2) <= BI.parseInt(sRegion) &&
                BI.parseInt(sRegion) < BI.parseInt(BICst.REGION.TARGET1)) {
                BI.each(view[sRegion], function (j, dId) {
                    BI.Utils.isDimensionUsable(dId) && (self.crossDimIds.push(dId));
                });
                return true;
            }
        });
        //使用中的指标
        this.targetIds = [];
        BI.each(view[BICst.REGION.TARGET1], function (i, dId) {
            BI.Utils.isDimensionUsable(dId) && (self.targetIds.push(dId));
        });
    },

    /**
     * 重置部分数据，用于无后台请求
     */
    _resetPartAttrs: function () {
        var wId = this.options.wId;
        this.showNumber = BI.Utils.getWSShowNumberByID(wId);         //显示行号
        this.showRowTotal = BI.Utils.getWSShowRowTotalByID(wId);    //显示行汇总
        this.showColTotal = BI.Utils.getWSShowColTotalByID(wId);    //显示列汇总
        this.openRowNode = BI.Utils.getWSOpenRowNodeByID(wId);      //展开所有行表头节点
        this.openColNode = BI.Utils.getWSOpenColNodeByID(wId);      //展开所有列表头节点
        this.freezeDim = BI.Utils.getWSFreezeDimByID(wId);           //冻结维度
        this.themeColor = BI.Utils.getWSThemeColorByID(wId);         //主题色
        this.tableForm = BI.Utils.getWSTableFormByID(wId);           //表格类型
        this.tableStyle = BI.Utils.getWSTableStyleByID(wId);         //表格风格

        this.header = [];
        this.items = [];
        this.crossHeader = [];
        this.crossItems = [];
        this.mergeCols = [];
        this.columnSize = BI.Utils.getWSColumnSizeByID(wId);

        this.tree = new BI.Tree();
        this.crossTree = new BI.Tree();

    },

    resetETree: function () {
        this.eTree = new BI.Tree();
        this.crossETree = new BI.Tree();
    },

    /**
     * format展开节点树
     */
    _formatExpanderTree: function (eTree) {
        var self = this, result = [];
        BI.each(eTree, function (i, t) {
            var item = {};
            item.name = t.node.name;
            if (BI.isNotNull(t.children)) {
                item.children = self._formatExpanderTree(t.children);
            }
            result.push(item);
        });
        return result;
    },

    //clicked 中的值，如果是分组名使用分组对应的id
    _parseClickedValue4Group: function (v, dId) {
        var group = BI.Utils.getDimensionGroupByID(dId);
        var fieldType = BI.Utils.getFieldTypeByDimensionID(dId);
        var clicked = v;

        if (BI.isNotNull(group)) {
            if (fieldType === BICst.COLUMN.STRING) {
                var details = group.details,
                    ungroup2Other = group.ungroup2Other,
                    ungroup2OtherName = group.ungroup2OtherName;
                if (ungroup2Other === BICst.CUSTOM_GROUP.UNGROUP2OTHER.SELECTED &&
                    ungroup2OtherName === v) {
                    clicked = BICst.UNGROUP_TO_OTHER;
                }
                BI.some(details, function (i, detail) {
                    if (detail.value === v) {
                        clicked = detail.id;
                        return true;
                    }
                });
            } else if (fieldType === BICst.COLUMN.NUMBER) {
                var groupValue = group.group_value, groupType = group.type;
                if (groupType === BICst.GROUP.CUSTOM_NUMBER_GROUP) {
                    var groupNodes = groupValue.group_nodes, useOther = groupValue.use_other;
                    if (useOther === v) {
                        clicked = BICst.UNGROUP_TO_OTHER;
                    }
                    BI.some(groupNodes, function (i, node) {
                        if (node.group_name === v) {
                            clicked = node.id;
                            return true;
                        }
                    });
                }
            }
        }
        return clicked;
    },

    /**
     * 基本的复杂表结构
     * 有几个维度的分组表示就有几个表
     * view: {10000: [a, b], 10001: [c, d]}, 20000: [e, f], 20001: [g, h], 20002: [i, j], 30000: [k]}
     * 表示横向（类似与交叉表）会有三个表，纵向会有两个表
     */
    _createComplexTableItems: function () {
        var self = this;
        var tempItems = [];
        BI.each(this.data, function (i, rowTables) {
            self.rowValues = {};
            BI.each(rowTables, function (j, tableData) {
                //parse一个表结构
                var singleTable = self._createSingleCrossTableItems(tableData);
                self._parseRowTableItems(singleTable.item);
                if (j === 0) {
                    tempItems.push(singleTable.item);
                }
            });
        });
        self._parseColTableItems(tempItems);
    },

    //对于首层，可以以行号作为key，其他层以dId + text作为key
    //一般只处理有values（同时有children的表示合计、否则表示相应的值）
    _parseRowTableItems: function (data) {
        var self = this;
        //最外层的合计 可以通过data中是否包含dId来确定
        if (BI.isNotNull(data.children) && BI.isNotNull(data.values) && BI.isNull(data.dId)) {
            if (BI.isNull(this.rowValues[this.constant.OUTER_SUM])) {
                this.rowValues[this.constant.OUTER_SUM] = data.values;
            } else {
                BI.each(data.values, function (i, v) {
                    self.rowValues[self.constant.OUTER_SUM].push(v);
                });
            }
        }
        if (BI.isNotNull(data.values) && BI.isNotNull(data.dId)) {
            var itemId = data.dId + data.text;
            if (BI.isNull(this.rowValues[itemId])) {
                this.rowValues[itemId] = data.values;
            } else {
                BI.each(data.values, function (i, v) {
                    self.rowValues[itemId].push(v);
                });
            }
        }
        if (BI.isNotNull(data.children) && data.children.length > 0) {
            BI.each(data.children, function (i, child) {
                self._parseRowTableItems(child);
            });
        }
    },

    // 处理列 针对于children
    // 要将所有的最外层的values处理成children
    _parseColTableItems: function (tempItems) {
        this.items = [];
        var children = [];
        BI.each(tempItems, function (i, tItem) {
            children = children.concat(tItem.children);
            children.push({
                text: BI.i18nText("BI-Summary_Values"),
                values: tItem.values
            });
        });
        this.items = [{
            children: children
        }];
    },

    /**
     * 交叉表 items and crossItems
     */
    _createSingleCrossTableItems: function (data) {
        var self = this;
        var top = data.t, left = data.l;

        //根据所在的层，汇总情况——是否含有汇总
        var singleCrossItemsSums = [];
        singleCrossItemsSums[0] = [];
        if (BI.isNotNull(left.s)) {
            singleCrossItemsSums[0].push(true);
        }
        this._initCrossItemsSum(0, left.c, singleCrossItemsSums);

        //交叉表items
        var crossItem = {
            children: this._createCrossPartItems(top.c, 0)
        };
        if (this.showColTotal === true) {
            BI.each(this.targetIds, function (i, tId) {
                crossItem.children.push({
                    type: "bi.normal_header_cell",
                    dId: tId,
                    text: BI.i18nText("BI-Summary_Values"),
                    tag: BI.UUID(),
                    sortFilterChange: function (v) {
                        self.resetETree();
                        self.pageOperator = BICst.TABLE_PAGE_OPERATOR.REFRESH;
                        self.headerOperatorCallback(v, tId);
                    },
                    isSum: true
                });
            });
        }
        var crossItems = [crossItem];

        //用cross parent value来对应到联动的时候的列表头值
        var crossPV = [];

        function parseCrossItem2Array(crossItems, pValues, pv) {
            BI.each(crossItems, function (i, crossItem) {
                if (BI.isNotNull(crossItem.children)) {
                    var tempPV = [];
                    if (BI.isNotNull(crossItem.dId)) {
                        if (BI.isNotEmptyArray(crossItem.values)) {
                            BI.each(crossItem.values, function (j, v) {
                                tempPV = pv.concat([{
                                    dId: crossItem.dId,
                                    value: [self._parseClickedValue4Group(crossItem.text, crossItem.dId)]
                                }]);
                            });
                            //显示列汇总的时候需要构造汇总
                        } else {
                            tempPV = pv.concat([{
                                dId: crossItem.dId,
                                value: [self._parseClickedValue4Group(crossItem.text, crossItem.dId)]
                            }]);
                        }
                    }
                    parseCrossItem2Array(crossItem.children, pValues, tempPV);
                    //汇总
                    if (BI.isNotEmptyArray(crossItem.values)) {
                        BI.each(crossItem.values, function (j, v) {
                            pValues.push([{
                                dId: crossItem.dId,
                                value: [self._parseClickedValue4Group(crossItem.text, crossItem.dId)]
                            }]);
                        });
                    }
                } else if (BI.isNotNull(crossItem.dId)) {
                    if (BI.isNotEmptyArray(crossItem.values)) {
                        BI.each(crossItem.values, function (j, v) {
                            pValues.push(pv.concat([{
                                dId: crossItem.dId,
                                value: [self._parseClickedValue4Group(crossItem.text, crossItem.dId)]
                            }]));
                        });
                    } else {
                        pValues.push([]);
                    }
                } else if (BI.isNotNull(crossItem.isSum)) {
                    pValues.push(pv);
                }
            });
        }

        parseCrossItem2Array(crossItems, crossPV, []);

        var item = {
            children: this._createTableItems(left.c, 0)
        };
        if (this.showRowTotal === true) {
            //汇总值
            var sums = [], ob = {index: 0};
            if (BI.isNotNull(left.s.c) && BI.isNotNull(left.s.s)) {
                this._createTableSumItems(left.s.c, sums, [], ob, true);
            } else {
                BI.isArray(left.s) && this._createTableSumItems(left.s, sums, [], ob, true);
            }
            if (this.showColTotal === true) {
                var outerValues = [];
                BI.each(left.s.s, function (i, v) {
                    if (self.targetIds.length > 0) {
                        var tId = self.targetIds[i];
                        outerValues.push({
                            type: "bi.target_body_normal_cell",
                            text: v,
                            dId: tId,
                            cls: "summary-cell last",
                            clicked: [{}]
                        });
                    }
                });
                BI.each(sums, function (i, sum) {
                    sums[i].cls = "summary-cell last"
                });
                sums = sums.concat(outerValues);
            }
            item.values = sums;
        }
        return {
            crossItem: crossItem,
            item: item,
            crossPV: crossPV
        }
    },

    /**
     * 交叉表的(指标)汇总值
     */
    _createTableSumItems: function (s, sum, pValues, ob, isLast) {
        var self = this;
        BI.each(s, function (i, v) {
            if (BI.isObject(v)) {
                var sums = v.s, child = v.c;
                if (BI.isNotNull(sums) && BI.isNotNull(child)) {
                    self._createTableSumItems(child, sum, pValues, ob, isLast);
                    self.showColTotal === true && self._createTableSumItems(sums, sum, pValues, ob, isLast);
                } else if (BI.isNotNull(sums)) {
                    self._createTableSumItems(sums, sum, pValues, ob, isLast);
                }

            } else {
                var tId = self.targetIds[i];
                if (self.targetIds.length === 0) {
                    tId = self.crossDimIds[i];
                }

                sum.push({
                    type: "bi.target_body_normal_cell",
                    text: v,
                    dId: tId,
                    // clicked: pValues.concat(self.crossPV[ob.index]),
                    cls: isLast ? "last summary-cell" : ""
                });
                ob.index++;
            }
        });
    },

    /**
     * 表items
     */
    _createTableItems: function (c, currentLayer, parent) {
        var self = this, items = [];
        currentLayer++;
        BI.each(c, function (i, child) {
            //可以直接使用每一层中的树节点的parent.id + child.n作为id，第一层无需考虑，因为第一层不可能有相同值
            //考虑到空字符串问题
            var cId = BI.isEmptyString(child.n) ? self.EMPTY_VALUE : child.n;
            var nodeId = BI.isNotNull(parent) ? parent.get("id") + cId : cId;
            var node = new BI.Node(nodeId);
            var currDid = self.dimIds[currentLayer - 1], currValue = child.n;
            node.set("name", currValue);
            self.tree.addNode(parent, node);
            var pValues = [];
            var tempLayer = currentLayer, tempNodeId = nodeId;
            while (tempLayer > 0) {
                var pv = self.tree.search(tempNodeId).get("name"), dId = self.dimIds[tempLayer - 1];
                pValues.push({
                    value: [self._parseClickedValue4Group(pv, dId)],
                    dId: dId
                });
                tempNodeId = self.tree.search(tempNodeId).getParent().get("id");
                tempLayer--;
            }
            var item = {
                type: "bi.normal_expander_cell",
                text: child.n,
                dId: currDid,
                expandCallback: function () {
                    //全部展开再收起——横向
                    var clickNode = self.eTree.search(nodeId);
                    if (self.openRowNode === true) {
                        self._addNode2eTree4OpenRowNode(nodeId);
                    } else {
                        if (BI.isNull(clickNode)) {
                            self.eTree.addNode(self.eTree.search(BI.isNull(parent) ? self.tree.getRoot().get("id") : parent.get("id")), BI.deepClone(node));
                        } else {
                            clickNode.getParent().removeChild(nodeId);
                        }
                    }

                    self.pageOperator = BICst.TABLE_PAGE_OPERATOR.EXPAND;
                    self.clickValue = child.n;
                    self.expanderCallback();
                }
            };
            //展开情况——最后一层没有这个展开按钮
            if (currentLayer < self.dimIds.length) {
                item.needExpand = true;
                item.isExpanded = false;
            }
            //有c->说明有children，构造children，并且需要在children中加入汇总情况（如果有并且需要）
            if (BI.isNotNull(child.c)) {
                item.children = self._createTableItems(child.c, currentLayer, node) || [];
                //在tableForm为 行展开模式 的时候 如果不显示汇总行 只是最后一行不显示汇总
                if (self.showRowTotal === true || self.getTableForm() === BICst.TABLE_FORM.OPEN_COL) {
                    var vs = [];
                    var summary = self._getOneRowSummary(child.s);
                    var tarSize = self.targetIds.length;
                    BI.each(summary, function (i, sum) {
                        vs.push({
                            type: "bi.target_body_normal_cell",
                            text: sum,
                            dId: self.targetIds[i % tarSize],
                            clicked: pValues,
                            cls: "summary-cell"
                        });
                    });
                    item.values = vs;
                }

                item.isExpanded = true;
            } else if (BI.isNotNull(child.s)) {
                var values = [];
                if (BI.isNotNull(child.s.c) || BI.isArray(child.s.s)) {
                    //交叉表，pValue来自于行列表头的结合
                    var ob = {index: 0};
                    self._createTableSumItems(child.s.c, values, pValues, ob);
                    //显示列汇总 有指标
                    if (self.showColTotal === true && self.targetIds.length > 0) {
                        self._createTableSumItems(child.s.s, values, pValues, ob);
                    }
                } else {
                    BI.each(child.s, function (j, sum) {
                        var tId = self.targetIds[j];
                        values.push({
                            type: "bi.target_body_normal_cell",
                            text: sum,
                            dId: tId,
                            clicked: pValues
                        })
                    });
                }
                item.values = values;
            }
            items.push(item);
        });
        return items;
    },

    /**
     * 展开所有节点的情况下的收起    横向
     */
    _addNode2eTree4OpenRowNode: function (nodeId) {
        var self = this;
        var clickNode = self.eTree.search(nodeId);
        if (BI.isNull(clickNode)) {
            //找到原始tree的这个节点的所有父节点，遍历一遍是否存在于eTree中
            //a、存在，向eTree直接添加；b、不存在，把这些父级节点都添加进去
            var pNodes = [];
            while (true) {
                if (BI.isNull(this.eTree.search(nodeId))) {
                    var node = this.tree.search(nodeId);
                    pNodes.push(node);
                    if (node.getParent().get("id") === this.tree.getRoot().get("id")) {
                        break;
                    }
                } else {
                    break;
                }
                nodeId = this.tree.search(nodeId).getParent().get("id");
            }
            pNodes.reverse();
            BI.each(pNodes, function (i, pNode) {
                var epNode = self.eTree.search(pNode.getParent().get("id"));
                pNode.removeAllChilds();
                self.eTree.addNode(BI.isNotNull(epNode) ? epNode : self.eTree.getRoot(), BI.deepClone(pNode));
            });
        } else {
            //如果已经在这个eTree中，看其是否存在兄弟节点，如果没有应该删除当前节点所在的树，有的话， 只删除自身
            function getFinalParent(nodeId) {
                var node = self.eTree.search(nodeId);
                if (node.getParent().get("id") === self.eTree.getRoot().get("id")) {
                    return nodeId;
                } else {
                    return getFinalParent(node.getParent().get("id"));
                }
            }

            if (this.eTree.search(nodeId).getParent().getChildrenLength() > 1) {
                this.eTree.search(nodeId).getParent().removeChild(nodeId);
            } else if (this.eTree.search(nodeId).getChildrenLength() > 0) {
                //此时应该是做收起，把所有的children都remove掉
                this.eTree.search(nodeId).removeAllChilds();
            } else {
                this.eTree.getRoot().removeChild(getFinalParent(nodeId));
            }
        }
    },

    /**
     * 初始化 crossItemsSum
     */
    _initCrossItemsSum: function (currentLayer, sums, singleCrossItemsSums) {
        var self = this;
        currentLayer++;
        BI.each(sums, function (i, v) {
            if (BI.isNotNull(v) && BI.isNotNull(v.c)) {
                self._initCrossItemsSum(currentLayer, v.c, singleCrossItemsSums);
            }
            BI.isNull(singleCrossItemsSums[currentLayer]) && (singleCrossItemsSums[currentLayer] = []);
            singleCrossItemsSums[currentLayer].push(BI.isNotNull(v.s) ? true : false);
        });
    },

    /**
     * 交叉表——crossItems
     */
    _createCrossPartItems: function (c, currentLayer, parent) {
        var self = this, crossHeaderItems = [];
        currentLayer++;
        BI.each(c, function (i, child) {
            if (BI.isNull(child.c) && (self.targetIds.contains(child.n) || self.crossDimIds.contains(child.n))) {
                return;
            }
            var cId = BI.isEmptyString(child.n) ? self.EMPTY_VALUE : child.n;
            var currDid = self.crossDimIds[currentLayer - 1], currValue = child.n;
            var nodeId = BI.isNotNull(parent) ? parent.get("id") + cId : cId;
            var node = new BI.Node(nodeId);
            node.set("name", child.n);
            self.crossTree.addNode(parent, node);
            var pValues = [];
            var tempLayer = currentLayer, tempNodeId = nodeId;
            while (tempLayer > 0) {
                var dId = self.crossDimIds[tempLayer - 1];
                pValues.push({
                    value: [self._parseClickedValue4Group(self.crossTree.search(tempNodeId).get("name"), dId)],
                    dId: self.crossDimIds[tempLayer - 1]
                });
                tempNodeId = self.crossTree.search(tempNodeId).getParent().get("id");
                tempLayer--;
            }
            var item = {
                type: "bi.normal_expander_cell",
                text: currValue,
                dId: currDid,
                isCross: true,
                expandCallback: function () {
                    var clickNode = self.crossETree.search(nodeId);
                    //全部展开再收起——纵向
                    if (self.openColNode === true) {
                        self._addNode2crossETree4OpenColNode(nodeId);
                    } else {
                        if (BI.isNull(clickNode)) {
                            self.crossETree.addNode(self.crossETree.search(BI.isNull(parent) ? self.crossTree.getRoot().get("id") : parent.get("id")), BI.deepClone(node));
                        } else {
                            clickNode.getParent().removeChild(nodeId);
                        }
                    }
                    self.pageOperator = BICst.TABLE_PAGE_OPERATOR.EXPAND;
                    self.clickValue = child.n;
                    self.expanderCallback();
                }
            };
            if (currentLayer < self.crossDimIds.length) {
                item.needExpand = true;
                item.isExpanded = false;
            }
            if (BI.isNotNull(child.c)) {
                var children = self._createCrossPartItems(child.c, currentLayer, node);
                if (BI.isNotEmptyArray(children)) {
                    item.children = self._createCrossPartItems(child.c, currentLayer, node);
                    item.isExpanded = true;
                }
            }
            var hasSum = false;
            if (BI.isNotNull(self.crossItemsSums) &&
                BI.isNotNull(self.crossItemsSums[currentLayer]) &&
                self.crossItemsSums[currentLayer][i] === true) {
                hasSum = true;
            }
            if (hasSum === true && self.showColTotal === true && BI.isNotEmptyArray(item.children)) {
                BI.each(self.targetIds, function (k, tId) {
                    item.values = [];
                    BI.each(self.targetIds, function (k, tarId) {
                        item.values.push("");
                    });
                });
            }
            if (self.showColTotal === true || BI.isNull(item.children)) {
                item.values = BI.makeArray(self.targetIds.length, "");
            }
            crossHeaderItems.push(item);
        });
        return crossHeaderItems;
    },

    /**
     * 交叉表——header and crossHeader
     */
    _createCrossTableHeader: function () {
        var self = this;
        BI.each(this.dimIds, function (i, dId) {
            if (BI.isNotNull(dId)) {
                self.header.push({
                    type: "bi.normal_header_cell",
                    dId: dId,
                    text: BI.Utils.getDimensionNameByID(dId),
                    sortFilterChange: function (v) {
                        self.resetETree();
                        self.pageOperator = BICst.TABLE_PAGE_OPERATOR.REFRESH;
                        self.headerOperatorCallback(v, dId);
                    }
                });
            }
        });
        BI.each(this.crossDimIds, function (i, dId) {
            if (BI.isNotNull(dId)) {
                self.crossHeader.push({
                    type: "bi.normal_header_cell",
                    dId: dId,
                    text: BI.Utils.getDimensionNameByID(dId),
                    sortFilterChange: function (v) {
                        self.resetETree();
                        self.pageOperator = BICst.TABLE_PAGE_OPERATOR.REFRESH;
                        self.headerOperatorCallback(v, dId);
                    }
                });
            }
        });

        var targetsArray = [];
        BI.each(this.targetIds, function (i, tId) {
            if (BI.isNotNull(tId)) {
                targetsArray.push({
                    type: "bi.page_table_cell",
                    cls: "cross-table-target-header",
                    text: BI.Utils.getDimensionNameByID(tId),
                    title: BI.Utils.getDimensionNameByID(tId)
                });
            }
        });
        //根据crossItems创建部分header
        this._createCrossPartHeader();
    },

    /**
     * 交叉表——crossHeader
     */
    _createCrossPartHeader: function () {
        var self = this;
        var dId = null;
        //可以直接根据crossItems确定header的后半部分
        function parseHeader(items) {
            BI.each(items, function (i, item) {
                var dName = BI.Utils.getDimensionNameByID(self.targetIds[i % (self.targetIds.length)]) || "--";
                if (BI.isNotNull(item.children)) {
                    parseHeader(item.children);
                    if (BI.isNotNull(item.values) && self.showColTotal === true) {
                        //合计
                        BI.each(self.targetIds, function (j, tarId) {
                            self.header.push({
                                type: "bi.page_table_cell",
                                cls: "cross-table-target-header",
                                text: BI.i18nText("BI-Summary_Values") + ":" + BI.Utils.getDimensionNameByID(tarId),
                                title: BI.i18nText("BI-Summary_Values") + ":" + BI.Utils.getDimensionNameByID(tarId),
                                tag: BI.UUID()
                            });
                        });
                    }
                } else if (BI.isNotNull(item.isSum)) {
                    //合计
                    item.text = BI.i18nText("BI-Summary_Values") + ":" + BI.Utils.getDimensionNameByID(item.dId);
                    item.cls = "cross-table-target-header";
                    self.header.push(item);
                } else if (BI.isNotEmptyArray(item.values)) {
                    BI.each(item.values, function (k, v) {
                        self.header.push({
                            type: "bi.page_table_cell",
                            cls: "cross-table-target-header",
                            text: BI.Utils.getDimensionNameByID(self.targetIds[k]),
                            title: BI.Utils.getDimensionNameByID(self.targetIds[k]),
                            tag: BI.UUID()
                        })
                    });
                } else {
                    self.header.push({
                        type: "bi.page_table_cell",
                        cls: "cross-table-target-header",
                        text: dName,
                        title: dName,
                        tag: BI.UUID()
                    })
                }
            });
        }

        parseHeader(this.crossItems);
    },

    _setOtherCrossAttrs: function () {
        var self = this;
        //冻结列
        this.freezeCols = [];
        //合并列，列大小
        var cSize = [];
        BI.each(this.dimIds, function (i, id) {
            self.mergeCols.push(i);
            self.freezeCols.push(i);
        });
        // this.showNumber === true && this.freezeCols.push(this.freezeCols.length);
        BI.each(this.header, function (i, id) {
            cSize.push("");
        });
        if (this.columnSize.length !== cSize.length) {
            //重置列宽
            this.columnSize = [];
            BI.each(cSize, function (i, id) {
                self.columnSize.push("");
            });
        }
    },

    createTableAttrs: function () {
        this.headerOperatorCallback = arguments[0];
        this.expanderCallback = arguments[1];
        this.clickedCallback = arguments[2];

        this._resetPartAttrs();
        this._refreshDimsInfo();

        //items
        this._createComplexTableItems();

        //header
        this._createCrossTableHeader();

        this._setOtherCrossAttrs();
    }
});