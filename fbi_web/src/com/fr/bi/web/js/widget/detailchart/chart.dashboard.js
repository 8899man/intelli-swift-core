/**
 * 图表控件
 * @class BI.DashboardChart
 * @extends BI.Widget
 */
BI.DashboardChart = BI.inherit(BI.AbstractChart, {


    _defaultConfig: function () {
        return BI.extend(BI.DashboardChart.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-dashboard-chart"
        })
    },

    _init: function () {
        BI.DashboardChart.superclass._init.apply(this, arguments);
        var self = this;
        this.gaugeAxis = [{
            "minorTickColor": "rgb(226,226,226)",
            "tickColor": "rgb(186,186,186)",
            labelStyle: this.constants.FONT_STYLE,
            "step": 0,
            "showLabel": true
        }];
        this.combineChart = BI.createWidget({
            type: "bi.combine_chart",
            formatConfig: BI.bind(this._formatConfig, this),
            element: this.element
        });
        this.combineChart.on(BI.CombineChart.EVENT_CHANGE, function (obj) {
            self.fireEvent(BI.DashboardChart.EVENT_CHANGE, obj);
        });
    },

    _formatConfig: function (config, items) {
        var self = this, o = this.options;
        formatChartDashboardStyle();
        config.chartType = "gauge";
        delete config.xAxis;
        delete config.yAxis;
        if (BI.contains([self.constants.NORMAL, self.constants.HALF_DASHBOARD], self.config.chart_dashboard_type)) {
            config.plotOptions.seriesLabel.enabled = false
        }
        config.gaugeAxis[0].labelStyle = this.config.chart_font;
        return [items, config];

        function formatChartDashboardStyle() {
            var bands = getBandsStyles(self.config.bands_styles, self.config.auto_custom_style);
            var percentageLabel = BI.extend(config.plotOptions.percentageLabel, {
                enabled: self.config.show_percentage === BICst.PERCENTAGE.SHOW
            });

            config.gaugeAxis = self.gaugeAxis;
            var slotValueLAbel = {
                formatter: function () {
                    var value = this.value;
                    if (self.config.dashboard_number_level === BICst.TARGET_STYLE.NUM_LEVEL.PERCENT && self.config.num_separators) {
                        value = BI.contentFormat(this.value, "#,##0%;-#,##0%")
                    } else if (self.config.dashboard_number_level === BICst.TARGET_STYLE.NUM_LEVEL.PERCENT && !self.config.num_separators) {
                        value = BI.contentFormat(this.value, "#0.00%");
                    } else if (!(self.config.dashboard_number_level === BICst.TARGET_STYLE.NUM_LEVEL.PERCENT) && self.config.num_separators) {
                        value = BI.contentFormat(this.value, "#,###.##;-#,###.##")
                    } else {
                        value = BI.contentFormat(this.value, "#.##;-#.##");
                    }

                    if (BI.contains([self.constants.NORMAL, self.constants.HALF_DASHBOARD], self.config.chart_dashboard_type) && BI.isNull(items[0].data[0].seriesName)) {
                        return'<div style="text-align: center">' + this.seriesName + '</div>' + '<div style="text-align: center">' + value +
                            getXYAxisUnit(self.config.dashboard_number_level, self.constants.DASHBOARD_AXIS) + '</div>';
                    }

                    return '<div style="text-align: center">' + this.category + '</div>' + '<div style="text-align: center">' + this.seriesName + '</div>' + '<div style="text-align: center">' + value +
                        getXYAxisUnit(self.config.dashboard_number_level, self.constants.DASHBOARD_AXIS) + '</div>';
                },
                style: self.config.chart_font,
                useHtml: true
            };
            switch (self.config.chart_dashboard_type) {
                case BICst.CHART_SHAPE.HALF_DASHBOARD:
                    setPlotOptions("pointer_semi", bands, slotValueLAbel, percentageLabel);
                    break;
                case BICst.CHART_SHAPE.PERCENT_DASHBOARD:
                    setPlotOptions("ring", bands, slotValueLAbel, percentageLabel);
                    break;
                case BICst.CHART_SHAPE.PERCENT_SCALE_SLOT:
                    setPlotOptions("slot", bands, slotValueLAbel, percentageLabel);
                    break;
                case BICst.CHART_SHAPE.HORIZONTAL_TUBE:
                    BI.extend(slotValueLAbel, {
                        align: "bottom"
                    });
                    BI.extend(percentageLabel, {
                        align: "bottom"
                    });
                    setPlotOptions("thermometer", bands, slotValueLAbel, percentageLabel, "horizontal", "vertical");
                    break;
                case BICst.CHART_SHAPE.VERTICAL_TUBE:
                    BI.extend(slotValueLAbel, {
                        align: "left"
                    });
                    BI.extend(percentageLabel, {
                        align: "left"
                    });
                    setPlotOptions("thermometer", bands, slotValueLAbel, percentageLabel, "vertical", "horizontal");
                    break;
                case BICst.CHART_SHAPE.NORMAL:
                default:
                    setPlotOptions("pointer", bands, slotValueLAbel, percentageLabel);
                    break;
            }
            changeMaxMinScale();
            formatNumberLevelInYaxis(self.config.dashboard_number_level, self.constants.LEFT_AXIS);
            if (self.config.dashboard_number_level === BICst.TARGET_STYLE.NUM_LEVEL.PERCENT) {
                config.gaugeAxis[0].formatter = function () {
                    var scaleValue = this;
                    if (self.config.num_separators) {
                        scaleValue = BI.contentFormat(scaleValue, '#,##0%;-#,##0%')
                    } else {
                        scaleValue = BI.contentFormat(scaleValue, '#0.00%')
                    }
                    return scaleValue + getXYAxisUnit(self.config.dashboard_number_level, self.constants.DASHBOARD_AXIS);
                };
            } else {
                config.gaugeAxis[0].formatter = function () {
                    var value = this;
                    if (self.config.num_separators) {
                        value = BI.contentFormat(value, "#,###;-#,###")
                    }
                    return value + getXYAxisUnit(self.config.dashboard_number_level, self.constants.DASHBOARD_AXIS);
                };
            }
        }

        function setPlotOptions(style, bands, valueLabel, percentageLabel, thermometerLayout, layout) {
            config.plotOptions.style = style;
            config.plotOptions.bands = bands;
            config.plotOptions.valueLabel = valueLabel;
            config.plotOptions.percentageLabel = percentageLabel;
            config.plotOptions.thermometerLayout = thermometerLayout;
            config.plotOptions.layout = layout;
        }

        function changeMaxMinScale() {
            self.gaugeAxis[0].min = self.config.min_scale || null;
            self.gaugeAxis[0].max = self.config.max_scale || null;
        }

        function formatNumberLevelInYaxis(type, position) {
            var magnify = self.calcMagnify(type);
            BI.each(items, function (idx, item) {
                BI.each(item.data, function (id, da) {
                    if (position === item.yAxis) {
                        da.y = self.formatXYDataWithMagnify(da.y, magnify);
                    }
                })
            });

            config.plotOptions.tooltip.formatter.valueFormat = function () {
                return BI.contentFormat(this, '#.##;-#.##') + getXYAxisUnit(type, position)
            };

            if (self.config.num_separators) {
                config.plotOptions.tooltip.formatter.valueFormat = function () {
                    return BI.contentFormat(arguments[0], '#,###.##;-#,###.##')
                };
            }

            if (type === BICst.TARGET_STYLE.NUM_LEVEL.PERCENT) {
                config.plotOptions.tooltip.formatter.valueFormat = function () {
                    return BI.contentFormat(arguments[0], '#0.00%')
                };
                if (self.config.num_separators) {
                    config.plotOptions.tooltip.formatter.valueFormat = function () {
                        return BI.contentFormat(arguments[0], '#,##0%;-#,##0%')
                    };
                }
            }
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
            if (position === self.constants.DASHBOARD_AXIS) {
                self.config.dashboard_unit !== "" && (unit = unit + self.config.dashboard_unit)
            }
            return unit;
        }

        function getBandsStyles(styles, change) {
            var min = 0, bands = [], color = null, max = null, conditionMax = null;

            BI.each(items, function (idx, item) {
                var data = item.data[0];
                if ((BI.isNull(max) || data.y > max)) {
                    max = data.y
                }
            });

            switch (change) {

                case BICst.SCALE_SETTING.AUTO:
                    break;
                case BICst.SCALE_SETTING.CUSTOM:
                    if (styles.length === 0) {
                        return bands
                    } else {
                        BI.each(styles, function (idx, style) {
                            bands.push({
                                color: style.color,
                                from: style.range.min,
                                to: style.range.max
                            });
                            color = style.color;
                            conditionMax = style.range.max
                        });
                        min = BI.parseInt(styles[0].range.min);
                        bands.push({
                            color: "#808080",
                            from: 0,
                            to: min
                        });

                        var maxScale = _calculateValueNiceDomain(0, max)[1];

                        bands.push({
                            color: color,
                            from: conditionMax,
                            to: maxScale
                        });

                        return bands;
                    }
            }
        }

        function _calculateValueNiceDomain(minValue, maxValue) {
            minValue = Math.min(0, minValue);
            var tickInterval = _linearTickInterval(minValue, maxValue);

            return _linearNiceDomain(minValue, maxValue, tickInterval);
        }

        function _linearTickInterval(minValue, maxValue, m) {
            m = m || 5;
            var span = maxValue - minValue;
            var step = Math.pow(10, Math.floor(Math.log(span / m) / Math.LN10));
            var err = m / span * step;
            if (err <= .15) step *= 10; else if (err <= .35) step *= 5; else if (err <= .75) step *= 2;

            return step;
        }

        function _linearNiceDomain(minValue, maxValue, tickInterval) {
            minValue = VanUtils.accMul(Math.floor(minValue / tickInterval), tickInterval);
            maxValue = VanUtils.accMul(Math.ceil(maxValue / tickInterval), tickInterval);

            return [minValue, maxValue];
        }
    },

    _formatItems: function (items) {
        if (items.length === 0) {
            return [];
        }
        var c = this.constants;
        if (this.config.chart_dashboard_type === c.NORMAL || this.config.chart_dashboard_type === c.HALF_DASHBOARD) {
            var result = [];
            if (this.config.number_of_pointer === c.ONE_POINTER && items[0].length === 1) {//单个系列
                BI.each(items[0][0].data, function (idx, da) {
                    result.push({
                        data: [BI.extend({}, da, {
                            x: items[0][0].name
                        })],
                        name: da.x
                    })
                });
                return [result];
            } else if(this.config.number_of_pointer === c.ONE_POINTER && items[0].length > 1) {
                BI.each(items[0], function (idx, item) {
                    result.push({
                        data: [BI.extend(item.data[0], {
                            x: item.name
                        })],
                        name: BI.UUID()
                    })
                });
                return [result]
            }
            if (this.config.number_of_pointer === c.MULTI_POINTER && items[0].length > 1) {//多个系列
                BI.each(items, function (idx, item) {
                    BI.each(item, function (id, it) {
                        var data = it.data[0];
                        data.x = it.name;
                        result.push(data);
                    })
                });
                return [[{
                    data: result,
                    name: ""
                }]];
            }
        } else {
            var others = [];
            if (BI.isNotNull(items[0][0].data[0].seriesName)) {
                BI.each(items[0], function (idx, item) {
                    BI.each(item.data, function (id, da) {
                        others.push({
                            data: [BI.extend({}, da, {
                                x: item.name,
                                y: da.y
                            })],
                            name: da.x
                        })
                    })
                });
                return [others];
            }
        }
        return items;
    },

    populate: function (items, options) {
        options || (options = {});
        var self = this, c = this.constants, o = this.options;
        this.config = {
            dashboard_number_level: options.dashboard_number_level || c.NORMAL,
            dashboard_unit: options.dashboard_unit || "",
            chart_dashboard_type: options.chart_dashboard_type || c.NORMAL,
            number_of_pointer: options.number_of_pointer || c.ONE_POINTER,
            bands_styles: options.style_conditions || [],
            auto_custom_style: options.auto_custom || c.AUTO,
            min_scale: options.min_scale,
            max_scale: options.max_scale,
            show_percentage: options.show_percentage || c.NOT_SHOW,
            num_separators: options.num_separators || false,
            chart_font: options.chart_font || c.FONT_STYLE
        };
        o.items = this._formatItems(items);
        var types = [];
        BI.each(o.items, function (idx, axisItems) {
            var type = [];
            BI.each(axisItems, function (id, item) {
                type.push(BICst.WIDGET.DASHBOARD);
            });
            types.push(type);
        });

        this.combineChart.populate(o.items, types);
    },

    resize: function () {
        this.combineChart.resize();
    },

    magnify: function () {
        this.combineChart.magnify();
    }
});
BI.DashboardChart.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut('bi.dashboard_chart', BI.DashboardChart);