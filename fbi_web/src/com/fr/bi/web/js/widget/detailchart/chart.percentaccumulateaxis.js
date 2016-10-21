/**
 * 图表控件 百分比堆积柱状
 * @class BI.PercentAccumulateAxisChart
 * @extends BI.Widget
 */
BI.PercentAccumulateAxisChart = BI.inherit(BI.AbstractChart, {

    _defaultConfig: function () {
        return BI.extend(BI.PercentAccumulateAxisChart.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-percent-accumulate-axis-chart"
        })
    },

    _init: function () {
        BI.PercentAccumulateAxisChart.superclass._init.apply(this, arguments);
        var self = this, o = this.options;
        this.xAxis = [{
            type: "category",
            title: {
                style: this.constants.FONT_STYLE
            },
            labelStyle: this.constants.FONT_STYLE,
            position: "bottom",
            gridLineWidth: 0
        }];
        this.yAxis = [{
            type: "value",
            title: {
                style: self.constants.FONT_STYLE
            },
            labelStyle: self.constants.FONT_STYLE,
            position: "left",
            gridLineWidth: 0
        }];
        this.combineChart = BI.createWidget({
            type: "bi.combine_chart",
            xAxis: this.xAxis,
            formatConfig: BI.bind(this._formatConfig, this),
            element: this.element
        });
        this.combineChart.on(BI.CombineChart.EVENT_CHANGE, function (obj) {
            self.fireEvent(BI.PercentAccumulateAxisChart.EVENT_CHANGE, obj);
        });
    },

    _formatConfig: function (config, items) {
        var self = this, o = this.options;
        var yTitle = getXYAxisUnit(this.config.left_y_axis_number_level, this.constants.LEFT_AXIS);
        config.colors = this.config.chart_color;
        config.plotOptions.style = formatChartStyle();
        formatCordon();
        this.formatChartLegend(config, this.config.chart_legend);
        config.plotOptions.dataLabels.enabled = this.config.show_data_label;
        config.dataSheet.enabled = this.config.show_data_table;
        config.xAxis[0].showLabel = !config.dataSheet.enabled;
        config.zoom.zoomTool.enabled = this.config.show_zoom;
        if (this.config.show_zoom === true) {
            delete config.dataSheet;
            delete config.zoom.zoomType;
        }

        config.yAxis = this.yAxis;
        config.yAxis[0].title.rotation = this.constants.ROTATION;
        config.yAxis[0].title.text = this.config.show_left_y_axis_title === true ? this.config.left_y_axis_title + yTitle : yTitle;
        BI.extend(config.yAxis[0], {
            lineWidth: this.config.line_width,
            showLabel: this.config.show_label,
            enableTick: this.config.enable_tick,
            reversed: this.config.left_y_axis_reversed,
            enableMinorTick: this.config.enable_minor_tick,
            gridLineWidth: this.config.show_grid_line === true ? 1 : 0,
            formatter: self.formatTickInXYaxis(this.config.left_y_axis_style, this.config.left_y_axis_number_level, this.config.num_separators)
        });
        self.formatNumberLevelInYaxis(config, items, this.config.left_y_axis_number_level, this.constants.LEFT_AXIS, config.yAxis[0].formatter);

        config.xAxis[0].title.align = "center";
        config.xAxis[0].title.text = this.config.show_x_axis_title === true ? this.config.x_axis_title : "";
        BI.extend(config.xAxis[0], {
            lineWidth: this.config.line_width,
            enableTick: this.config.enable_tick,
            labelRotation: this.config.text_direction,
            enableMinorTick: this.config.enable_minor_tick,
            gridLineWidth: this.config.show_grid_line === true ? 1 : 0
        });

        config.plotOptions.tooltip.formatter.identifier = "${CATEGORY}${SERIES}${PERCENT}";

        //为了给数据标签加个%,还要遍历所有的系列，唉
        this.formatDataLabelForAxis(config.plotOptions.dataLabels.enabled, items, config.yAxis[0].formatter, this.config.chart_font);

        //全局样式的图表文字
        this.setFontStyle(this.config.chart_font, config);

        return [items, config];

        function formatChartStyle() {
            switch (self.config.chart_style) {
                case BICst.CHART_STYLE.STYLE_GRADUAL:
                    return "gradual";
                case BICst.CHART_STYLE.STYLE_NORMAL:
                default:
                    return "normal";
            }
        }

        function formatCordon() {
            BI.each(self.config.cordon, function (idx, cor) {
                if (idx === 0 && self.xAxis.length > 0) {
                    var magnify = self.calcMagnify(self.config.x_axis_number_level);
                    self.xAxis[0].plotLines = BI.map(cor, function (i, t) {
                        return BI.extend(t, {
                            value: t.value.div(magnify),
                            width: 1,
                            label: {
                                "style" : self.config.chart_font,
                                "text": t.text,
                                "align": "top"
                            }
                        });
                    });
                }
                if (idx > 0 && self.yAxis.length >= idx) {
                    var magnify = 1;
                    switch (idx - 1) {
                        case self.constants.LEFT_AXIS:
                            magnify = self.calcMagnify(self.config.left_y_axis_number_level);
                            break;
                        case self.constants.RIGHT_AXIS:
                            magnify = self.calcMagnify(self.config.right_y_axis_number_level);
                            break;
                        case self.constants.RIGHT_AXIS_SECOND:
                            magnify = self.calcMagnify(self.config.right_y_axis_second_number_level);
                            break;
                    }
                    self.yAxis[idx - 1].plotLines = BI.map(cor, function (i, t) {
                        return BI.extend(t, {
                            value: t.value.div(magnify),
                            width: 1,
                            label: {
                                "style" : self.config.chart_font,
                                "text": t.text,
                                "align": "left"
                            }
                        });
                    });
                }
            })
        }

        function getXYAxisUnit(numberLevelType, position) {
            var unit = "";
            switch (numberLevelType) {
                case BICst.TARGET_STYLE.NUM_LEVEL.NORMAL:
                    unit = "";
                    break;
                case BICst.TARGET_STYLE.NUM_LEVEL.TEN_THOUSAND:
                    unit = BI.i18nText("BI-Wan");
                    break;
                case BICst.TARGET_STYLE.NUM_LEVEL.MILLION:
                    unit = BI.i18nText("BI-Million");
                    break;
                case BICst.TARGET_STYLE.NUM_LEVEL.YI:
                    unit = BI.i18nText("BI-Yi");
                    break;
            }
            if (position === self.constants.X_AXIS) {
                self.config.x_axis_unit !== "" && (unit = unit + self.config.x_axis_unit)
            }
            if (position === self.constants.LEFT_AXIS) {
                self.config.left_y_axis_unit !== "" && (unit = unit + self.config.left_y_axis_unit)
            }
            return unit === "" ? unit : "(" + unit + ")";
        }
    },

    _formatItems: function (items) {
        return BI.map(items, function (idx, item) {
            var i = BI.UUID();
            return BI.map(item, function (id, it) {
                return BI.extend({}, it, {stack: i, stackByPercent: true});
            });
        });
    },

    populate: function (items, options) {
        options || (options = {});
        var self = this, c = this.constants;
        this.config = {
            left_y_axis_title: options.left_y_axis_title || "",
            chart_color: options.chart_color || [],
            chart_style: options.chart_style || c.NORMAL,
            left_y_axis_style: options.left_y_axis_style || c.NORMAL,
            show_x_axis_title: options.show_x_axis_title || false,
            show_left_y_axis_title: options.show_left_y_axis_title || false,
            left_y_axis_reversed: options.left_y_axis_reversed || false,
            left_y_axis_number_level: options.left_y_axis_number_level || c.NORMAL,
            x_axis_unit: options.x_axis_unit || "",
            left_y_axis_unit: options.left_y_axis_unit || "",
            x_axis_title: options.x_axis_title || "",
            chart_legend: options.chart_legend || c.LEGEND_BOTTOM,
            show_data_label: options.show_data_label || false,
            show_data_table: options.show_data_table || false,
            show_grid_line: BI.isNull(options.show_grid_line) ? true : options.show_grid_line,
            show_zoom: options.show_zoom || false,
            text_direction: options.text_direction || 0,
            cordon: options.cordon || [],
            line_width: BI.isNull(options.line_width) ? 1 : options.line_width,
            show_label: BI.isNull(options.show_label) ? true : options.show_label,
            enable_tick: BI.isNull(options.enable_tick) ? true : options.enable_tick,
            enable_minor_tick: BI.isNull(options.enable_minor_tick) ? true : options.enable_minor_tick,
            num_separators: options.num_separators || false,
            chart_font: options.chart_font || c.FONT_STYLE
        };
        this.options.items = items;

        var types = [];
        BI.each(items, function (idx, axisItems) {
            var type = [];
            BI.each(axisItems, function (id, item) {
                type.push(BICst.WIDGET.AXIS);
            });
            types.push(type);
        });

        this.combineChart.populate(this._formatItems(items), types);
    },

    resize: function () {
        this.combineChart.resize();
    },

    magnify: function () {
        this.combineChart.magnify();
    }
});
BI.PercentAccumulateAxisChart.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut('bi.percent_accumulate_axis_chart', BI.PercentAccumulateAxisChart);