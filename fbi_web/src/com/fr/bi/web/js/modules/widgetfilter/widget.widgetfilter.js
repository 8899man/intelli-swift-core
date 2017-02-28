/**
 * Created by Young's on 2016/4/6.
 */
BI.WidgetFilter = BI.inherit(BI.Widget, {

    _constants: {
        SHOW_FILTER: 1,
        SHOW_NONE_FILTER: 2
    },

    _defaultConfig: function () {
        return BI.extend(BI.WidgetFilter.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-widget-filter"
        })
    },

    _init: function () {
        BI.WidgetFilter.superclass._init.apply(this, arguments);
        var self = this;
        this.model = new BI.WidgetFilterModel();
        this.tab = BI.createWidget({
            type: "bi.tab",
            cls: "main-filter-pane",
            cardCreator: function (v) {
                switch (v) {
                    case self._constants.SHOW_FILTER:
                        self.filterPane = BI.createWidget({
                            type: "bi.filter_pane",
                            cls: "filter-pane"
                        });
                        self.drills = BI.createWidget({
                            type: "bi.drill_filter_item",
                            wId: self.options.wId
                        });
                        return {
                            type: "bi.vertical",
                            items: [self.filterPane, self.drills],
                            hgap: 10,
                            vgap: 10
                        };
                        break;
                    case self._constants.SHOW_NONE_FILTER:
                        return {
                            type: "bi.vertical",
                            items: [{
                                type: "bi.label",
                                cls: "none-filter-tip",
                                text: BI.i18nText("BI-None_Filter"),
                                height: 50
                            }]
                        }
                }
            }
        });
        this.populate();
        BI.createWidget({
            type: "bi.absolute",
            element: this.element,
            items: [{
                el: this.tab,
                top: 0,
                left: 0,
                bottom: 40,
                right: 0
            }, {
                el: {
                    type: "bi.button",
                    text: BI.i18nText("BI-Close_Filter"),
                    height: 26,
                    handler: function () {
                        self.setVisible(false);
                    }
                },
                right: 0,
                bottom: 2
            }]
        });
    },

    populate: function () {
        var self = this, o = this.options;
        var wId = o.wId;
        var items = [];

        var allWidgetIds = BI.Utils.getAllWidgetIDs();
        //找到所有控件的过滤条件
        BI.each(allWidgetIds, function (i, cwid) {
            if (BI.Utils.isControlWidgetByWidgetId(cwid)) {
                //通用查询
                if (BI.Utils.getWidgetTypeByID(cwid) === BICst.WIDGET.GENERAL_QUERY) {
                    var value = BI.Utils.getWidgetValueByID(cwid);
                    var item = self.model.parseGeneralQueryFilter(value[0]);
                    if (BI.isNotNull(item)) {
                        items.push(item);
                    }
                } else {
                    var text = self.model.getControlWidgetValueTextByID(cwid);
                    if (BI.isNotNull(text) && text !== "") {
                        items.push({
                            type: "bi.control_filter_item",
                            wId: cwid,
                            text: text,
                            id: BI.UUID()
                        });
                    }
                }
            }
        });

        //组件的联动条件
        var linkageFilters = BI.Utils.getLinkageValuesByID(wId);
        BI.each(linkageFilters, function (tId, linkFilter) {
            if (BI.isEmptyObject(linkFilter[0])) {
                return;
            }
            items.push({
                type: "bi.linkage_filter_item",
                id: BI.UUID(),
                tId: tId,
                filter: linkFilter,
                onRemoveFilter: function (tId, dId) {
                    //这个地方就处理好clicked
                    var clicked = BI.Utils.getClickedByID(wId);
                    var values = clicked[tId];
                    BI.some(values, function (i, value) {
                        if (value.dId === dId) {
                            values.splice(i, 1);
                            return true;
                        }
                    });
                    BI.isEmptyArray(values) && delete clicked[tId];
                    self.fireEvent(BI.WidgetFilter.EVENT_REMOVE_FILTER, {clicked: clicked});
                }
            });
        });

        //表头上设置的指标过滤条件
        var targetFilter = BI.Utils.getWidgetFilterValueByID(wId);
        BI.each(targetFilter, function (tId, filter) {
            items.push(self.model.parseTargetFilter(tId, filter));
        });

        //表头上设置的过滤条件，还要加上所有dimension的过滤条件
        var wType = BI.Utils.getWidgetTypeByID(wId);
        var dimIds = BI.Utils.getAllDimDimensionIDs(wId);
        BI.each(dimIds, function (i, dimId) {
            if (BI.Utils.isDimensionUsable(dimId)) {
                var fValue = BI.Utils.getDimensionFilterValueByID(dimId);
                if (BI.isNotEmptyObject(fValue)) {
                    items.push(wType === BICst.WIDGET.DETAIL ?
                        self.model.parseTargetFilter(dimId, fValue) :
                        self.model.parseDimensionFilter(dimId, fValue));
                }
            }
        });

        if (BI.isEmptyArray(items) && this.model.isEmptyDrillById(wId)) {
            this.tab.setSelect(this._constants.SHOW_NONE_FILTER);
            return;
        }
        this.tab.setSelect(this._constants.SHOW_FILTER);
        var filterValues = [];
        if (items.length > 1) {
            filterValues.push({
                value: BICst.FILTER_TYPE.AND,
                children: items
            });
        } else {
            filterValues = items;
        }
        this.filterPane.populate(filterValues);
        this.drills.populate();
    }
});
BI.WidgetFilter.EVENT_REMOVE_FILTER = "EVENT_REMOVE_FILTER";
BI.WidgetFilter.EVENT_CLOSE_FILTER = "EVENT_CLOSE_FILTER";
$.shortcut("bi.widget_filter", BI.WidgetFilter);