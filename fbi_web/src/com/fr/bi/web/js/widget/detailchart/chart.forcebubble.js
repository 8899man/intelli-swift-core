/**
 * 图表控件
 * @class BI.ForceBubbleChart
 * @extends BI.Widget
 */
BI.ForceBubbleChart = BI.inherit(BI.AbstractChart, {

    _defaultConfig: function () {
        return BI.extend(BI.ForceBubbleChart.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-force-bubble-chart"
        })
    },

    _init: function () {
        BI.ForceBubbleChart.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.combineChart = BI.createWidget({
            type: "bi.combine_chart",
            popupItemsGetter: o.popupItemsGetter,
            formatConfig: BI.bind(this._formatConfig, this),
            element: this.element
        });
        this.combineChart.on(BI.CombineChart.EVENT_CHANGE, function (obj) {
            self.fireEvent(BI.ForceBubbleChart.EVENT_CHANGE, obj);
        });
        this.combineChart.on(BI.CombineChart.EVENT_ITEM_CLICK, function (obj) {
            self.fireEvent(BI.AbstractChart.EVENT_ITEM_CLICK, obj)
        });
    },

    _formatConfig: function (config, items) {
        var self = this, o = this.options;
        config.chartType = 'forceBubble';
        config.colors = this.config.chart_color;
        this.formatChartLegend(config, this.config.chart_legend);

        config.plotOptions.force = true;
        config.plotOptions.shadow = this.config.bubble_style !== this.constants.NO_PROJECT;
        config.plotOptions.dataLabels.enabled = true;
        config.plotOptions.dataLabels.align = "inside";
        config.plotOptions.dataLabels.style = this.config.chart_font;
        config.plotOptions.dataLabels.formatter.identifier = "${CATEGORY}${VALUE}";
        delete config.xAxis;
        delete config.yAxis;
        delete config.zoom;
        BI.each(items, function (idx, item) {
            BI.each(item.data, function (id, da) {
                da.y = self.formatXYDataWithMagnify(da.y, 1);
            })
        });
        config.legend.style = this.config.chart_font;
        return [items, config];
    },

    populate: function (items, options) {
        options || (options = {});
        var self = this, c = this.constants;
        this.config = {
            chart_color: options.chart_color || [],
            chart_legend: options.chart_legend || c.LEGEND_BOTTOM,
            bubble_style: options.bubble_style || c.NO_PROJECT,
            chart_font: options.chart_font || c.FONT_STYLE
        };
        this.options.items = items;

        var types = [];
        BI.each(items, function (idx, axisItems) {
            var type = [];
            BI.each(axisItems, function (id, item) {
                type.push(BICst.WIDGET.FORCE_BUBBLE);
            });
            types.push(type);
        });

        this.combineChart.populate(items, types);
    },

    resize: function () {
        this.combineChart.resize();
    },

    magnify: function () {
        this.combineChart.magnify();
    }
});
BI.ForceBubbleChart.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut('bi.force_bubble_chart', BI.ForceBubbleChart);