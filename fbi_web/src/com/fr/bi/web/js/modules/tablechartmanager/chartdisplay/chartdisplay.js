/**
 * 图表控件
 * @class BI.ChartDisplay
 * @extends BI.Pane
 */
BI.ChartDisplay = BI.inherit(BI.Pane, {

    constants: {
        SCATTER_REGION_COUNT: 3,
        BUBBLE_REGION_COUNT: 4
    },

    _defaultConfig: function () {
        return BI.extend(BI.ChartDisplay.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-chart-display",
            overlap: false,
            wId: ""
        })
    },

    _init: function () {
        BI.ChartDisplay.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.model = new BI.ChartDisplayModel({
            wId: o.wId,
            status: o.status
        });

        this.tab = BI.createWidget({
            type: "bi.tab",
            element: this.element,
            cardCreator: BI.bind(this._createTabs, this)
        });

        this.tab.element.css("z-index", 1);

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

    _doChartItemClick: function (obj) {
        var self = this, o = this.options;
        var linkageInfo = this.model.getLinkageInfo(obj);
        var dId = linkageInfo.dId, clicked = linkageInfo.clicked;

        BI.each(BI.Utils.getWidgetLinkageByID(o.wId), function (i, link) {
            if (BI.contains(dId, link.from) && BI.isEmpty(link.cids)) {
                BI.Broadcasts.send(BICst.BROADCAST.LINKAGE_PREFIX + link.to, link.from, clicked);
                self._send2AllChildLinkWidget(link.to, link.from, clicked);
            }
        });

        BI.Broadcasts.send(BICst.BROADCAST.CHART_CLICK_PREFIX + o.wId, obj);
    },

    _onClickDrill: function (dId, value, drillId) {
        var wId = this.options.wId;
        var drillMap = BI.Utils.getDrillByID(wId);
        if (BI.isNull(dId)) {
            this.fireEvent(BI.ChartDisplay.EVENT_CHANGE, {clicked: BI.extend(BI.Utils.getLinkageValuesByID(wId), {})});
            return;
        }
        //value 存当前的过滤条件——因为每一次钻取都要带上所有父节点的值
        //当前钻取的根节点
        var rootId = dId;
        BI.each(drillMap, function (drId, ds) {
            if (dId === drId || (ds.length > 0 && ds[ds.length - 1].dId === dId)) {
                rootId = drId;
            }
        });

        var drillOperators = drillMap[rootId] || [];
        //上钻
        if (BI.isNull(drillId)) {
            if (drillOperators.length !== 0) {
                var val = drillOperators[drillOperators.length - 1].values[0].dId;
                while (val !== dId) {
                    if (drillOperators.length === 0) {
                        break;
                    }
                    var obj = drillOperators.pop();
                    val = obj.values[0].dId;
                }
                if (val === dId && drillOperators.length !== 0) {
                    drillOperators.pop();
                }
            }
        } else {
            drillOperators.push({
                dId: drillId,
                values: [{
                    dId: dId,
                    value: [value]
                }]
            });
        }
        drillMap[rootId] = drillOperators;
        this.fireEvent(BI.ChartDisplay.EVENT_CHANGE, {clicked: BI.extend(BI.Utils.getLinkageValuesByID(wId), drillMap)});
    },

    _send2AllChildLinkWidget: function (wid, dId, clicked) {
        var self = this;
        var linkage = BI.Utils.getWidgetLinkageByID(wid);
        BI.each(linkage, function (i, link) {
            BI.Broadcasts.send(BICst.BROADCAST.LINKAGE_PREFIX + link.to, dId, clicked);
            self._send2AllChildLinkWidget(link.to, dId, clicked);
        });
    },

    _createTabs: function (v) {
        var self = this, o = this.options;
        var popupItemsGetter = function (obj) {
            var linkages = [];
            var linkInfo = self.model.getLinkageInfo(obj);
            BI.each(linkInfo.dId, function (idx, dId) {
                if (BI.Utils.isCalculateTargetByDimensionID(dId)) {
                    BI.each(BI.Utils.getWidgetLinkageByID(o.wId), function (i, link) {
                        if (link.cids && dId === link.cids[0]) {
                            var name = BI.i18nText("BI-An");
                            BI.each(link.cids, function (idx, cId) {
                                name += BI.Utils.getDimensionNameByID(cId) + "-";
                            });
                            name += BI.Utils.getDimensionNameByID(link.from) + BI.i18nText("BI-Link");
                            var temp = {
                                text: name,
                                title: name,
                                to: link.to,
                                from: link.from,
                                cids: link.cids
                            };
                            var containsItem = containsLinkage(linkages, temp);
                            if (BI.isEmptyObject(containsItem)) {
                                linkages.push(temp);
                            } else {
                                BI.isArray(containsItem.to) ? containsItem.to.push(temp.to) : containsItem.to = [containsItem.to, temp.to];
                            }
                        }
                    });
                }
            });
            return linkages;
        };

        function containsLinkage(list, item) {
            for (var i = 0; i < list.length; i++) {
                if (list[i].from === item.from && BI.isEqual(list[i].cids, item.cids)) {
                    return list[i];
                }
            }
            return {};
        }

        var chart;
        if (BI.has(BICst.INIT_CHART_MAP, v)) {
            chart = BI.createWidget({type: BICst.INIT_CHART_MAP[v].type, popupItemsGetter: popupItemsGetter});
            if (v === BICst.WIDGET.MAP) {
                chart.on(BI.MapChart.EVENT_CHANGE, function (obj) {
                    self._doChartItemClick(obj);
                    BI.isNotNull(obj.drillDid) && self._onClickDrill(obj.dId, obj.x, obj.drillDid);
                });
                chart.on(BI.MapChart.EVENT_CLICK_DTOOL, function (obj) {
                    self._onClickDrill(obj.dId, obj.x);
                });
            } else {
                BI.each(BICst.INIT_CHART_MAP[v].events, function (idx, v) {
                    chart.on(v, function (obj) {
                        self._doChartItemClick(obj);
                    })
                })
            }
            chart.on(BI.AbstractChart.EVENT_ITEM_CLICK, function (obj) {
                self._sendLinkWidget(obj);
            });
        }
        return chart;
    },

    _sendLinkWidget: function (obj) {
        var self = this, linkageInfo = this.model.getLinkageInfo(obj);
        BI.each(BI.isArray(obj.to) ? obj.to : [obj.to], function (idx, to) {
            BI.Broadcasts.send(BICst.BROADCAST.LINKAGE_PREFIX + to, obj.from, linkageInfo.clicked);
            self._send2AllChildLinkWidget(to, obj.from, linkageInfo.clicked);
        });
    },


    populate: function () {
        var self = this, o = this.options;
        var type = BI.Utils.getWidgetTypeByID(o.wId);
        this.errorPane.setVisible(false);
        this.loading();
        this.model.getWidgetData(type, function (types, data, options) {
            self.loaded();
            if (BI.isNotNull(types.error)) {
                self.errorPane.setErrorInfo(types.error);
                self.errorPane.setVisible(true);
                return;
            }
            try {
                var dimensionIds = BI.Utils.getAllDimDimensionIDs(o.wId);
                var lnglat = BI.Utils.getDimensionPositionByID(dimensionIds[0]);
                var op;
                if (BI.Utils.getWSMinimalistByID(o.wId) && BI.ChartDisplay.MINIMALIST_WIDGET.contains(type)) {
                    op = BI.extend(options, {
                        chartColor: BI.Utils.getWSChartColorByID(o.wId),
                        chartStyle: BI.Utils.getWSChartStyleByID(o.wId),
                        chartType: BI.Utils.getWSLineAreaChartTypeByID(o.wId),
                        transferFilter: BI.Utils.getWSTransferFilterByID(o.wId),
                        leftYReverse: BI.Utils.getWSChartLeftYReverseByID(o.wId),
                        rightYReverse: BI.Utils.getWSChartRightYReverseByID(o.wId),
                        chartFont: BI.extend(BI.Utils.getGSChartFont(o.wId), {
                            fontSize: BI.Utils.getGSChartFont(o.wId).fontSize + "px"
                        }),
                        nullContinuity: BI.Utils.getWSNullContinuityByID(o.wId),
                        leftYShowLabel: false,
                        rightYShowLabel: false,
                        rightY2ShowLabel: false,
                        catLabelStyle: BI.Utils.getWSChartCatLabelStyleByID(o.wId),
                        clickZoom: BI.Utils.getWSChartClickZoomByID(o.wId),
                        lineWidth: BICst.DEFAULT_CHART_SETTING.miniLineWidth,
                        enableTick: BICst.DEFAULT_CHART_SETTING.miniEnableTick,
                        enableMinorTick: BICst.DEFAULT_CHART_SETTING.miniEnableMinorTick,
                        leftYUnit: BICst.DEFAULT_CHART_SETTING.leftYUnit,
                        catShowTitle: BICst.DEFAULT_CHART_SETTING.catShowTitle,
                        leftYShowTitle: BICst.DEFAULT_CHART_SETTING.leftYShowTitle,
                        legend: BICst.DEFAULT_CHART_SETTING.miniChartLegend,
                        showDataLabel: BICst.DEFAULT_CHART_SETTING.miniShowDataLabel,
                        showGridLine: BICst.DEFAULT_CHART_SETTING.miniShowGridLine
                    }, {
                        seriesAccumulation: self.model.getSeriesAccumulation(o.wId),
                        tooltip: self.model.getToolTip(type),
                        lnglat: BI.isNotNull(lnglat) ? lnglat.type : lnglat
                    })
                } else {
                    op = BI.extend(options, {
                        chartColor: BI.Utils.getWSChartColorByID(o.wId),
                        chartStyle: BI.Utils.getWSChartStyleByID(o.wId),
                        lienAreaChartType: BI.Utils.getWSLineAreaChartTypeByID(o.wId),
                        pieChartType: BI.Utils.getWSPieChartTypeByID(o.wId),
                        radarChartType: BI.Utils.getWSRadarChartTypeByID(o.wId),
                        dashboardChartType: BI.Utils.getWSDashboardChartTypeByID(o.wId),
                        innerRadius: BI.Utils.getWSChartInnerRadiusByID(o.wId),
                        totalAngle: BI.Utils.getWSChartTotalAngleByID(o.wId),
                        dashboardPointer: BI.Utils.getWSChartDashboardPointerByID(o.wId),
                        dashboardStyles: BI.Utils.getWSChartDashboardStylesByID(o.wId),
                        //左值轴y
                        leftYNumberFormat: BI.Utils.getWSChartLeftYNumberFormatByID(o.wId),
                        leftYUnit: BI.Utils.getWSLeftYAxisUnitByID(o.wId),
                        leftYNumberLevel: BI.Utils.getWSChartLeftYNumberLevelByID(o.wId),
                        leftYShowTitle: BI.Utils.getWSChartLeftYShowTitleByID(o.wId),
                        leftYTitle: BI.Utils.getWSChartLeftYTitleByID(o.wId),
                        leftYReverse: BI.Utils.getWSChartLeftYReverseByID(o.wId),
                        leftYShowLabel: BI.Utils.getWSChartLeftYShowLabelByID(o.wId),
                        leftYLabelStyle: BI.Utils.getWSChartLeftYLabelStyleByID(o.wId),
                        leftYLineColor: BI.Utils.getWSChartLeftYLineColorByID(o.wId),
                        leftYSeparator: BI.Utils.getWSLeftYNumberSeparatorByID(o.wId),
                        leftYTitleStyle: BI.Utils.getWSChartLeftYTitleStyleByID(o.wId),
                        leftYCustomScale: BI.Utils.getWSChartLeftYCustomScaleByID(o.wId),
                        //右值轴y
                        rightYNumberFormat: BI.Utils.getWSChartRightYNumberFormatByID(o.wId),
                        rightYNumberLevel: BI.Utils.getWSChartRightYNumberLevelByID(o.wId),
                        rightYUnit: BI.Utils.getWSChartRightYUnitByID(o.wId),
                        rightYReverse: BI.Utils.getWSChartRightYReverseByID(o.wId),
                        rightYShowTitle: BI.Utils.getWSChartRightYShowTitleByID(o.wId),
                        rightYTitleStyle: BI.Utils.getWSChartRightYTitleStyleByID(o.wId),
                        rightYTitle: BI.Utils.getWSChartRightYTitleByID(o.wId),
                        rightYSeparator: BI.Utils.getWSRightYNumberSeparatorByID(o.wId),
                        rightYCustomScale: BI.Utils.getWSChartRightYCustomScaleByID(o.wId),
                        rightYShowLabel: BI.Utils.getWSRightYShowLabelByID(o.wId),
                        rightYLabelStyle: BI.Utils.getWSRightYLabelStyleByID(o.wId),
                        rightYLineColor: BI.Utils.getWSRightYLineColorByID(o.wId),
                        //右值轴y2
                        rightY2NumberFormat: BI.Utils.getWSChartRightY2NumberFormatByID(o.wId),
                        rightY2NumberLevel: BI.Utils.getWSChartRightY2NumberLevelByID(o.wId),
                        rightY2Unit: BI.Utils.getWSChartRightYAxis2UnitByID(o.wId),
                        rightY2ShowTitle: BI.Utils.getWSChartRightY2ShowTitleByID(o.wId),
                        rightY2Title: BI.Utils.getWSChartRightY2TitleByID(o.wId),
                        rightY2Reverse: BI.Utils.getWSChartRightY2ReverseByID(o.wId),
                        rightY2Separator: BI.Utils.getWSRightY2NumberSeparatorByID(o.wId),
                        rightY2ShowLabel: BI.Utils.getWSRightY2ShowLabelByID(o.wId),
                        rightY2LabelStyle: BI.Utils.getWSRightY2LabelStyleByID(o.wId),
                        rightY2LineColor: BI.Utils.getWSRightY2LineColorByID(o.wId),
                        rightY2TitleStyle: BI.Utils.getWSChartRightY2TitleStyleByID(o.wId),
                        rightY2CustomScale: BI.Utils.getWSChartRightY2CustomScaleByID(o.wId),
                        rightY2ShowCustomScale: BI.Utils.getWSChartRightY2ShowCustomScaleByID(o.wId),
                        //分类轴
                        catShowTitle: BI.Utils.getWSChartCatShowTitleByID(o.wId),
                        catTitle: BI.Utils.getWSChartCatTitleByID(o.wId),
                        catTitleStyle: BI.Utils.getWSChartCatTitleStyleByID(o.wId),
                        catShowLabel: BI.Utils.getWSChartCatShowLabelByID(o.wId),
                        catLabelStyle: BI.Utils.getWSChartCatLabelStyleByID(o.wId),
                        catLineColor: BI.Utils.getWSChartCatLineColorByID(o.wId),
                        //其他元素
                        legend: BI.Utils.getWSChartLegendByID(o.wId),
                        legendStyle: BI.Utils.getWSChartLegendStyleByID(o.wId),
                        showDataLabel: BI.Utils.getWSChartShowDataLabelByID(o.wId),
                        showDataTable: BI.Utils.getWSChartShowDataTableByID(o.wId),
                        showZoom: BI.Utils.getWSChartShowZoomByID(o.wId),
                        styleRadio: BI.Utils.getWSChartStyleRadioByID(o.wId),
                        themeColor: BI.Utils.getWSThemeColorByID(o.wId),
                        mapStyles: BI.Utils.getWSChartMapStylesByID(o.wId),
                        displayRules: BI.Utils.getWSChartDisplayRulesByID(o.wId),
                        bubbleStyle: BI.Utils.getWSChartBubbleStyleByID(o.wId),
                        maxScale: BI.Utils.getWSChartMaxScaleByID(o.wId),
                        minScale: BI.Utils.getWSChartMinScaleByID(o.wId),
                        showPercentage: BI.Utils.getWSChartShowPercentageByID(o.wId),
                        bubbleSizeFrom: BI.Utils.getWSChartBubbleSizeFromByID(o.wId),
                        bubbleSizeTo: BI.Utils.getWSChartBubbleSizeToByID(o.wId),
                        gradientStyle: BI.Utils.getWSChartBubbleGradientStyleByID(o.wId),
                        fixedStyle: BI.Utils.getWSChartBubbleFixedStyleByID(o.wId),
                        isShowBackgroundLayer: BI.Utils.getWSShowBackgroundByID(o.wId),
                        hShowGridLine: BI.Utils.getWSChartHShowGridLineByID(o.wId),
                        hGridLineColor: BI.Utils.getWSChartHGridLineColorByID(o.wId),
                        vShowGridLine: BI.Utils.getWSChartVShowGridLineByID(o.wId),
                        vGridLineColor: BI.Utils.getWSChartVGridLineColorByID(o.wId),
                        tooltipStyle: BI.Utils.getWSChartToolTipStyleByID(o.wId),
                        chartFont: BI.extend(BI.Utils.getGSChartFont(o.wId), {
                            fontSize: BI.Utils.getGSChartFont(o.wId).fontSize + "px"
                        }),
                        nullContinuity: BI.Utils.getWSNullContinuityByID(o.wId),
                        backgroundLayerInfo: MapConst.WMS_INFO[BI.Utils.getWSChartMapBackgroundLayerInfoByID(o.wId)],
                        transferFilter: BI.Utils.getWSTransferFilterByID(o.wId),
                        bigDataMode: BI.Utils.getWSChartBigDataModeByID(o.wId),
                        dataLabelSetting: BI.Utils.getWSChartDataLabelSettingByID(o.wId),
                        clickZoom: BI.Utils.getWSChartClickZoomByID(o.wId)
                    }, {
                        seriesAccumulation: self.model.getSeriesAccumulation(o.wId),
                        cordon: self.model.getCordon(),
                        tooltip: self.model.getToolTip(type),
                        lnglat: BI.isNotNull(lnglat) ? lnglat.type : lnglat
                    });
                }
                self.tab.setSelect(type);
                var selectedTab = self.tab.getSelectedTab();
                selectedTab.populate(data, op, types);
            } catch (e) {
                self.errorPane.setErrorInfo("error happens during populate chart: " + e);
                console.error(e);
                self.errorPane.setVisible(true);
            }
        });
    },

    resize: function () {
        this.tab.getSelectedTab().resize();
    },

    magnify: function () {
        this.tab.getSelectedTab().magnify();
    }
});
BI.extend(BI.ChartDisplay, {
    MINIMALIST_WIDGET: [
        BICst.WIDGET.AXIS,
        BICst.WIDGET.ACCUMULATE_AXIS,
        BICst.WIDGET.PERCENT_ACCUMULATE_AXIS,
        BICst.WIDGET.COMPARE_AXIS,
        BICst.WIDGET.FALL_AXIS,
        BICst.WIDGET.BAR,
        BICst.WIDGET.ACCUMULATE_BAR,
        BICst.WIDGET.COMPARE_BAR,
        BICst.WIDGET.LINE,
        BICst.WIDGET.AREA,
        BICst.WIDGET.ACCUMULATE_AREA,
        BICst.WIDGET.PERCENT_ACCUMULATE_AREA,
        BICst.WIDGET.COMPARE_AREA,
        BICst.WIDGET.RANGE_AREA,
        BICst.WIDGET.COMBINE_CHART,
        BICst.WIDGET.MULTI_AXIS_COMBINE_CHART
    ]
});
BI.ChartDisplay.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut('bi.chart_display', BI.ChartDisplay);
