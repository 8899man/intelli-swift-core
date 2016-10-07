/**
 * 汇总表（分组表、交叉表）
 * @class BI.SummaryTable
 * @extends BI.Pane
 */
BI.SummaryTable = BI.inherit(BI.Pane, {
    _defaultConfig: function () {
        return BI.extend(BI.SummaryTable.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-summary-table",
            overlap: false
        })
    },

    _init: function () {
        BI.SummaryTable.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.model = new BI.SummaryTableModel({
            wId: o.wId,
            status: o.status
        });
        this._createTable();
        this.errorPane = BI.createWidget({
            type: "bi.table_chart_error_pane",
            invisible: true
        });
        this.errorPane.element.css("z-index", 1);
        BI.createWidget({
            type: "bi.absolute",
            element: this.element,
            items: [{
                el: this.errorPane,
                top: 0,
                left: 0,
                bottom: 0,
                right: 0
            }]
        })
    },

    _createTable: function () {
        var self = this, o = this.options;
        this.table && this.table.destroy();
        this.vPage = 1;
        this.hPage = 1;
        var tableStyle = this.model.getTableForm();
        switch (tableStyle) {
            case BICst.TABLE_FORM.OPEN_COL:
                this.tableForm = BICst.TABLE_FORM.OPEN_COL;
                this.table = BI.createWidget({
                    type: "bi.style_table",
                    el: {
                        type: "bi.page_table",
                        isNeedFreeze: null,
                        mergeRule: function (col1, col2) {
                            if (col1.tag && col2.tag) {
                                return col1.tag === col2.tag;
                            }
                            return col1 === col2;
                        },
                        el: {
                            el: {
                                el: {
                                    el: {
                                        type: "bi.dynamic_summary_layer_tree_table"
                                    }
                                }
                            },
                            sequence: {
                                type: "bi.sequence_table_dynamic_number"
                            }
                        },
                        itemsCreator: function (op, populate) {
                            var vPage = op.vpage, hPage = op.hpage;
                            var pageOperator = BICst.TABLE_PAGE_OPERATOR.COLUMN_NEXT;
                            if (vPage > self.vPage) {
                                pageOperator = BICst.TABLE_PAGE_OPERATOR.ROW_NEXT;
                            }
                            if (vPage < self.vPage) {
                                pageOperator = BICst.TABLE_PAGE_OPERATOR.ROW_PRE;
                            }
                            if (hPage > self.hPage) {
                                pageOperator = BICst.TABLE_PAGE_OPERATOR.COLUMN_NEXT;
                            }
                            if (hPage < self.hPage) {
                                pageOperator = BICst.TABLE_PAGE_OPERATOR.COLUMN_PRE;
                            }
                            self.hPage = hPage;
                            self.vPage = vPage;
                            self.model.setPageOperator(pageOperator);
                            self._onPageChange(function (items, header, crossItems, crossHeader) {
                                populate.apply(self.table, arguments);
                                self._afterTablePopulate();
                            })
                        },
                        pager: {
                            pages: false,
                            curr: 1,
                            firstPage: 1,
                            horizontal: {
                                pages: false, //总页数
                                curr: 1, //初始化当前页， pages为数字时可用
                                hasPrev: function () {
                                    return self.model.getPage()[2] === 1;
                                },
                                hasNext: function () {
                                    return self.model.getPage()[3] === 1;
                                },
                                firstPage: 1,
                                lastPage: BI.emptyFn
                            },
                            vertical: {
                                pages: false, //总页数
                                curr: 1, //初始化当前页， pages为数字时可用
                                hasPrev: function () {
                                    return self.model.getPage()[0] === 1;
                                },
                                hasNext: function () {
                                    return self.model.getPage()[1] === 1;
                                },
                                firstPage: 1,
                                lastPage: BI.emptyFn
                            }
                        },
                        isNeedMerge: true,
                        regionColumnSize: this.model.getStoredRegionColumnSize()
                    }
                });
                break;
            default :
                this.tableForm = BICst.TABLE_FORM.OPEN_ROW;
                this.table = BI.createWidget({
                    type: "bi.style_table",
                    el: {
                        type: "bi.page_table",
                        isNeedFreeze: null,
                        mergeRule: function (col1, col2) {
                            if (col1.tag && col2.tag) {
                                return col1.tag === col2.tag;
                            }
                            return col1 === col2;
                        },
                        el: {
                            el: {
                                el: {
                                    el: {
                                        type: "bi.dynamic_summary_tree_table"
                                    }
                                }
                            },
                            sequence: {
                                type: "bi.sequence_table_dynamic_number"
                            }
                        },
                        itemsCreator: function (op, populate) {
                            var vPage = op.vpage, hPage = op.hpage;
                            var pageOperator = BICst.TABLE_PAGE_OPERATOR.COLUMN_NEXT;
                            if (vPage > self.vPage) {
                                pageOperator = BICst.TABLE_PAGE_OPERATOR.ROW_NEXT;
                            }
                            if (vPage < self.vPage) {
                                pageOperator = BICst.TABLE_PAGE_OPERATOR.ROW_PRE;
                            }
                            if (hPage > self.hPage) {
                                pageOperator = BICst.TABLE_PAGE_OPERATOR.COLUMN_NEXT;
                            }
                            if (hPage < self.hPage) {
                                pageOperator = BICst.TABLE_PAGE_OPERATOR.COLUMN_PRE;
                            }
                            self.hPage = hPage;
                            self.vPage = vPage;
                            self.model.setPageOperator(pageOperator);
                            self._onPageChange(function (items, header, crossItems, crossHeader) {
                                populate.apply(self.table, arguments);
                                self._afterTablePopulate();
                            });
                        },
                        pager: {
                            pages: false,
                            curr: 1,
                            firstPage: 1,
                            horizontal: {
                                pages: false, //总页数
                                curr: 1, //初始化当前页， pages为数字时可用
                                hasPrev: function () {
                                    return self.model.getPage()[2] === 1;
                                },
                                hasNext: function () {
                                    return self.model.getPage()[3] === 1;
                                },
                                firstPage: 1,
                                lastPage: BI.emptyFn
                            },
                            vertical: {
                                pages: false, //总页数
                                curr: 1, //初始化当前页， pages为数字时可用
                                hasPrev: function () {
                                    return self.model.getPage()[0] === 1;
                                },
                                hasNext: function () {
                                    return self.model.getPage()[1] === 1;
                                },
                                firstPage: 1,
                                lastPage: BI.emptyFn
                            }
                        },
                        isNeedMerge: true,
                        regionColumnSize: this.model.getStoredRegionColumnSize()
                    }
                });
                break;
        }
        this.table.on(BI.StyleTable.EVENT_TABLE_AFTER_REGION_RESIZE, function () {
            var columnSize = this.getCalculateRegionColumnSize();
            self.model.setStoredRegionColumnSize(columnSize[0]);
        });
        this.table.on(BI.StyleTable.EVENT_TABLE_AFTER_COLUMN_RESIZE, function () {
            var columnSize = BI.clone(this.getColumnSize());
            self.fireEvent(BI.SummaryTable.EVENT_CHANGE, {settings: BI.extend(BI.Utils.getWidgetSettingsByID(self.model.getWidgetId()), {column_size: columnSize})});
        });
        this.table.on(BI.StyleTable.EVENT_TABLE_AFTER_INIT, function () {
            self._resizeTableColumnSize();
        });
        if (this.model.getPageOperator() === BICst.TABLE_PAGE_OPERATOR.ROW_NEXT || this.model.getPageOperator() === BICst.TABLE_PAGE_OPERATOR.ROW_PRE) {
            this.table.setVPage(this.model.getPage()[4]);
        }

        BI.createWidget({
            type: "bi.absolute",
            element: this.element,
            items: [{
                el: this.table,
                left: 0,
                right: 0,
                top: 0,
                bottom: 0
            }]
        })
    },

    _resizeTableColumnSize: function () {
        var cs = this.table.getColumnSize();
        var isValid = true;
        BI.some(cs, function (i, size) {
            if (!BI.isNumeric(size)) {
                isValid = false;
                return true;
            }
        });
        if (!isValid) {
            var columnSize = this.table.getCalculateColumnSize();
            if (this.model.isNeed2Freeze()) {
                var regionColumnSize = this.table.getCalculateRegionColumnSize();
                this.model.setStoredRegionColumnSize(regionColumnSize[0]);
                var freezeCols = this.model.getFreezeCols();
                var freezeColumnSize = columnSize.slice(0, freezeCols.length);
                var otherSize = columnSize.slice(freezeCols.length);
                var fl = freezeColumnSize.length, ol = otherSize.length;
                BI.each(freezeColumnSize, function (i, size) {
                    if (size > 200 && i < fl - 1) {
                        freezeColumnSize[fl - 1] = freezeColumnSize[fl - 1] + freezeColumnSize[i] - 200;
                        freezeColumnSize[i] = 200;
                    }
                    if (size < 80 && i < fl - 1) {
                        var tempSize = freezeColumnSize[fl - 1] - (80 - freezeColumnSize[i]);
                        freezeColumnSize[fl - 1] = tempSize < 80 ? 80 : tempSize;
                        freezeColumnSize [i] = 80;
                    }
                });
                BI.each(otherSize, function (i, size) {
                    if (size > 200 && i < ol - 1) {
                        otherSize[ol - 1] = otherSize[ol - 1] + otherSize[i] - 200;
                        otherSize[i] = 200;
                    }
                    if (size < 80 && i < ol - 1) {
                        var tempSize = otherSize[ol - 1] - (80 - otherSize[i]);
                        otherSize[ol - 1] = tempSize < 80 ? 80 : tempSize;
                        otherSize [i] = 80;
                    }
                });
                columnSize = freezeColumnSize.concat(otherSize);
            } else {
                var cl = columnSize.length;
                BI.each(columnSize, function (i, size) {
                    if (size > 200 && i < cl - 1) {
                        columnSize[cl - 1] = columnSize[cl - 1] + columnSize[i] - 200;
                        columnSize[i] = 200;
                    }
                    if (size < 80 && i < cl - 1) {
                        var tempSize = columnSize[cl - 1] - (80 - columnSize[i]);
                        columnSize[cl - 1] = tempSize < 80 ? 80 : tempSize;
                        columnSize [i] = 80;
                    }
                })
            }
            this.table.setColumnSize(columnSize);
            this.fireEvent(BI.SummaryTable.EVENT_CHANGE, {settings: BI.extend(BI.Utils.getWidgetSettingsByID(this.model.getWidgetId()), {column_size: columnSize})});
        }
    },

    /**
     * 无维度或指标发生变化时（如：展开节点）
     * @private
     */
    _populateNoDimsChange: function () {
        var self = this, wId = this.options.wId;
        this.loading();
        BI.Utils.getWidgetDataByID(wId, function (jsonData) {
            self.loaded();
            if (BI.isNotNull(jsonData.error)) {
                self.errorPane.setErrorInfo(jsonData.error);
                self.errorPane.setVisible(true);
                return;
            }
            if (BI.isNull(jsonData.data) || BI.isNull(jsonData.page)) {
                self.errorPane.setErrorInfo("invalid json data!");
                self.errorPane.setVisible(true);
                return;
            }
            self.model.setDataAndPage(jsonData);
            var widgetType = BI.Utils.getWidgetTypeByID(wId);
            try {
                switch (widgetType) {
                    case BICst.WIDGET.TABLE:
                        self._prepareData4GroupTable();
                        break;
                    case BICst.WIDGET.CROSS_TABLE:
                        //如果没有列表头，还是以分组表展示——后台传这样的数据
                        if (BI.isNotNull(self.model.getData().t)) {
                            self._prepareData4CrossTable();
                        } else {
                            self._prepareData4GroupTable();
                        }
                        break;
                    case BICst.WIDGET.COMPLEX_TABLE:
                        self._populateComplexTable();
                        break;
                }
                self._refreshTable();
            } catch (e) {
                self.errorPane.setErrorInfo("error happens during populate table: " + e);
                self.errorPane.setVisible(true);
            }
        }, this.model.getExtraInfo());
    },

    _onPageChange: function (callback) {
        var self = this, wId = this.options.wId;
        this.loading();
        BI.Utils.getWidgetDataByID(wId, function (jsonData) {
            self.loaded();
            if (BI.isNotNull(jsonData.error)) {
                self.errorPane.setErrorInfo(jsonData.error);
                self.errorPane.setVisible(true);
                return;
            }
            if (BI.isNull(jsonData.data) || BI.isNull(jsonData.page)) {
                self.errorPane.setErrorInfo("invalid json data!");
                self.errorPane.setVisible(true);
                return;
            }
            self.model.setDataAndPage(jsonData);
            var widgetType = BI.Utils.getWidgetTypeByID(wId);
            try {
                switch (widgetType) {
                    case BICst.WIDGET.TABLE:
                        self.model.createGroupTableAttrs(BI.bind(self._onClickHeaderOperator, self), BI.bind(self._populateNoDimsChange, self), BI.bind(self._onClickBodyCellOperator, self));
                        break;
                    case BICst.WIDGET.CROSS_TABLE:
                        if (BI.isNotNull(self.model.getData().t)) {
                            self.model.createCrossTableAttrs(BI.bind(self._onClickHeaderOperator, self), BI.bind(self._populateNoDimsChange, self), BI.bind(self._onClickBodyCellOperator, self));
                        } else {
                            self.model.createGroupTableAttrs(BI.bind(self._onClickHeaderOperator, self), BI.bind(self._populateNoDimsChange, self), BI.bind(self._onClickBodyCellOperator, self));
                        }
                        break;
                }
                callback(self.model.getItems(), self.model.getHeader(), self.model.getCrossItems(), self.model.getCrossHeader());
            } catch (e) {
                self.errorPane.setErrorInfo("error happens during populate for table: " + e);
                self.errorPane.setVisible(true);
            }
            self.fireEvent(BI.SummaryTable.EVENT_CHANGE, {
                _page_: {
                    h: self.table.getHPage(),
                    v: self.table.getVPage()
                }
            });
        }, this.model.getExtraInfo());
    },

    /**
     * 分组表
     * @private
     */
    _prepareData4GroupTable: function () {
        //创建表格的各种属性——回调各种点击事件
        this.model.createGroupTableAttrs(BI.bind(this._onClickHeaderOperator, this), BI.bind(this._populateNoDimsChange, this), BI.bind(this._onClickBodyCellOperator, this));
    },

    /**
     * 交叉表
     * @private
     */
    _prepareData4CrossTable: function () {
        this.model.createCrossTableAttrs(BI.bind(this._onClickHeaderOperator, this), BI.bind(this._populateNoDimsChange, this), BI.bind(this._onClickBodyCellOperator, this));
    },

    /**
     * 表头上的一系列操作（排序、过滤）
     */
    _onClickHeaderOperator: function (v, dId) {
        switch (v) {
            case BICst.SORT.ASC:
            case BICst.SORT.DESC:
            case BICst.SORT.NONE:
                this._onClickHeaderSort(dId, v);
                break;
            default :
                this._onClickHeaderCellFilter(dId);
                break;
        }
    },

    /**
     * 表上的操作（上钻、下钻）
     * @private
     */
    _onClickBodyCellOperator: function (clicked) {
        this.fireEvent(BI.SummaryTable.EVENT_CHANGE, {clicked: clicked});
    },

    _onClickHeaderCellFilter: function (dId) {
        var self = this;

        function formatTargetFilter(filter, tId) {
            if (filter.filter_type === BICst.FILTER_TYPE.AND ||
                filter.filter_type === BICst.FILTER_TYPE.OR) {
                BI.each(filter.filter_value, function (i, filter) {
                    formatTargetFilter(filter, tId);
                });
                return;
            }
            filter.target_id = tId;
        }

        BI.Popovers.remove(dId);
        if (BI.Utils.isDimensionByDimensionID(dId)) {
            var popup = BI.createWidget({
                type: "bi.dimension_filter_popup",
                dId: dId
            });
            popup.on(BI.DimensionFilterPopup.EVENT_CHANGE, function (v) {
                var dimensions = BI.Utils.getWidgetDimensionsByID(self.options.wId);
                dimensions[dId].filter_value = v;
                self.fireEvent(BI.SummaryTable.EVENT_CHANGE, {dimensions: dimensions});
            });
        } else {
            var popup = BI.createWidget({
                type: "bi.target_summary_filter_popup",
                dId: dId
            });
            popup.on(BI.TargetSummaryFilterPopup.EVENT_CHANGE, function (v) {
                var targetFilter = BI.Utils.getWidgetFilterValueByID(self.options.wId);
                if (BI.isNotNull(v)) {
                    formatTargetFilter(v, dId);
                    targetFilter[dId] = v;
                } else {
                    delete targetFilter[dId];
                }

                self.fireEvent(BI.SummaryTable.EVENT_CHANGE, {filter_value: targetFilter});
            });
        }
        BI.Popovers.create(dId, popup).open(dId);
        popup.populate();
    },

    _onClickHeaderSort: function (dId, v) {
        var ob = {};
        if (BI.Utils.isDimensionByDimensionID(dId)) {
            var dimensions = BI.Utils.getWidgetDimensionsByID(this.options.wId);
            dimensions[dId].sort = {sort_target: dId, type: v};
            ob.dimensions = dimensions;
        } else {
            ob.sort = {sort_target: dId, type: v};
        }
        this.fireEvent(BI.SummaryTable.EVENT_CHANGE, ob);
    },

    _populateComplexTable: function () {

    },

    _populateTable: function () {
        this.errorPane.setVisible(false);
        this.table.attr("isNeedFreeze", true);
        this.table.attr("freezeCols", this.model.getFreezeCols());
        this.table.attr("mergeCols", this.model.getMergeCols());
        this.table.attr("columnSize", this.model.getColumnSize());
        this.table.populate(this.model.getItems(), this.model.getHeader(), this.model.getCrossItems(), this.model.getCrossHeader());
        this._afterTablePopulate();
    },

    _refreshTable: function () {
        this.errorPane.setVisible(false);
        this.table.attr("isNeedFreeze", true);
        this.table.attr("freezeCols", this.model.getFreezeCols());
        this.table.attr("mergeCols", this.model.getMergeCols());
        this.table.attr("columnSize", this.model.getColumnSize());
        this.table.refresh(this.model.getItems(), this.model.getHeader(), this.model.getCrossItems(), this.model.getCrossHeader());
        this._afterTablePopulate();
    },

    _afterTablePopulate: function () {
        if (this.model.isShowNumber()) {
            this.table.showSequence();
        } else {
            this.table.hideSequence();
        }
        //css
        this.table.setStyleAndColor(this.model.getTableStyle(), this.model.getThemeColor());
    },

    populate: function () {
        var self = this;
        var widgetId = this.options.wId;
        this.vPage = 1;
        this.hPage = 1;
        this.model.setPageOperator(BICst.TABLE_PAGE_OPERATOR.REFRESH);
        this.model.resetETree();
        this.loading();
        BI.Utils.getWidgetDataByID(widgetId, function (jsonData) {
            self.loaded();
            if (BI.isNotNull(jsonData.error)) {
                self.errorPane.setErrorInfo(jsonData.error);
                self.errorPane.setVisible(true);
                return;
            }
            if (BI.isNull(jsonData.data) || BI.isNull(jsonData.page)) {
                self.errorPane.setErrorInfo("invalid json data!");
                self.errorPane.setVisible(true);
                return;
            }
            self.model.setDataAndPage(jsonData);
            var widgetType = BI.Utils.getWidgetTypeByID(widgetId);
            // try {
            switch (widgetType) {
                case BICst.WIDGET.TABLE:
                    self._prepareData4GroupTable();
                    break;
                case BICst.WIDGET.CROSS_TABLE:
                    //如果没有列表头，还是以分组表展示——后台传这样的数据
                    if (BI.isNotNull(self.model.getData().t)) {
                        self._prepareData4CrossTable();
                    } else {
                        self._prepareData4GroupTable();
                    }
                    break;
                case BICst.WIDGET.COMPLEX_TABLE:
                    self._populateComplexTable();
                    break;
            }
            if (self.model.getTableForm() !== self.tableForm) {
                self._createTable();
            }
            self.table.setVPage(1);
            self._populateTable();
            // } catch (e) {
            //     self.errorPane.setErrorInfo("error happens during populate table: " + e);
            //     console.error(e);
            //     self.errorPane.setVisible(true);
            // }
        }, this.model.getExtraInfo());
    },

    resize: function () {
        this.table.resize();
    },

    magnify: function () {

    },

    empty: function () {
        BI.SummaryTable.superclass.empty.apply(this, arguments);
        if (BI.isNotNull(this.table)) {
            this.table.empty();
        }
    }
});
BI.SummaryTable.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut("bi.summary_table", BI.SummaryTable);
