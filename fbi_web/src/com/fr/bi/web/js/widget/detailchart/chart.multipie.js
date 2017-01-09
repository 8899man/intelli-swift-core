/**
 * 图表控件
 * @class BI.MultiPieChart
 * @extends BI.Widget
 */
BI.MultiPieChart = BI.inherit(BI.AbstractChart, {

    _defaultConfig: function () {
        return BI.extend(BI.MultiPieChart.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-multi-pie-chart"
        })
    },

    _init: function () {
        BI.MultiPieChart.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.combineChart = BI.createWidget({
            type: "bi.combine_chart",
            popupItemsGetter: o.popupItemsGetter,
            formatConfig: BI.bind(this._formatConfig, this),
            element: this.element
        });
        this.combineChart.on(BI.CombineChart.EVENT_CHANGE, function (obj) {
            self.fireEvent(BI.MultiPieChart.EVENT_CHANGE, obj);
        });
        this.combineChart.on(BI.CombineChart.EVENT_ITEM_CLICK, function (obj) {
            self.fireEvent(BI.AbstractChart.EVENT_ITEM_CLICK, obj)
        });
    },

    _formatConfig: function (config, items) {
        var self = this, o = this.options;
        delete config.zoom;
        config.colors = this.config.chartColor;
        config.plotOptions.style = formatChartStyle();
        config.plotOptions.gradual = formatGradientType();
        formatChartPieStyle();

        this.formatChartLegend(config, this.config.legend);

        config.plotOptions.tooltip.formatter.identifier = "${NAME}${VALUE}${PERCENT}";
        config.plotOptions.tooltip.shared = true;
        config.chartType = "multiPie";
        delete config.xAxis;
        delete config.yAxis;

        BI.extend(config.plotOptions.dataLabels, {
            enabled: this.config.showDataLabel,
            align: "top",
        });
        config.plotOptions.dataLabels.formatter.identifier = "${NAME}${VALUE}";
        BI.each(items, function (idx, item) {
            BI.each(item.data, function (id, da) {
                da.y = self.formatXYDataWithMagnify(da.y, 1);
            })
        });

        config.legend.style = BI.extend({}, this.config.legendStyle, {
            fontSize: this.config.legendStyle && this.config.legendStyle.fontSize + "px"
        });

        return [items, config];

        function formatChartStyle() {
            switch (self.config.chartStyle) {
                case BICst.CHART_STYLE.STYLE_GRADUAL:
                    return "gradual";
                case BICst.CHART_STYLE.STYLE_NORMAL:
                default:
                    return "normal";
            }
        }

        function formatGradientType() {
            switch (self.config.gradientType) {
                case BICst.MULTI_PIE_GRADIENT_STYLE.DARKER:
                    return "darker";
                case BICst.MULTI_PIE_GRADIENT_STYLE.LIGHTER:
                default:
                    return "lighter";
            }
        }

        function formatChartPieStyle() {
            config.plotOptions.innerRadius = self.config.innerRadius + "%";
            config.plotOptions.startAngle = 270;
            config.plotOptions.endAngle = (270 + self.config.totalAngle) % 360;
        }

    },

    _formatDrillItems: function (items) {
        var self = this;
        BI.each(items, function (idx, data) {
            data.y = self.formatXYDataWithMagnify(data.y, 1);
            data.name = data.x;
            data.value = data.y;
            if (BI.has(data, "children")) {
                self._formatDrillItems(data.children);
            }
        });
        return items;
    },

    _formatItems: function (items, options) {
        var self = this;
        BI.each(items, function (idx, item) {
            BI.each(item, function (id, it) {
                it.drilldown = options.clickZoom;
                BI.each(it.data, function (i, da) {
                    da.y = self.formatXYDataWithMagnify(da.y, 1);
                    da.name = da.x;
                    da.value = da.y;
                    if (BI.has(da, "children")) {
                        self._formatDrillItems(da.children);
                    }
                });
            })
        });
        return items;
    },

    populate: function (items, options) {
        options || (options = {});
        var self = this, c = this.constants;
        this.config = self.getChartConfig(options);
        this.options.items = items;

        var types = [];
        BI.each(items, function (idx, axisItems) {
            var type = [];
            BI.each(axisItems, function (id, item) {
                type.push(BICst.WIDGET.MULTI_PIE);
            });
            types.push(type);
        });

        this.combineChart.populate(this._formatItems(items, options), types);
    },

    resize: function () {
        this.combineChart.resize();
    },

    magnify: function () {
        this.combineChart.magnify();
    }
});
BI.MultiPieChart.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut('bi.multi_pie_chart', BI.MultiPieChart);