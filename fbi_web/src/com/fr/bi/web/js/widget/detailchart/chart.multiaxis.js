/**
 * 图表控件
 * @class BI.MultiAxisChart
 * @extends BI.Widget
 * leftYxis 左值轴属性
 * rightYxis 右值轴属性
 * xAxis    分类轴属性
 */
BI.MultiAxisChart = BI.inherit(BI.AbstractChart, {

    _defaultConfig: function () {
        return BI.extend(BI.MultiAxisChart.superclass._defaultConfig.apply(this, arguments), {
            baseCls: "bi-axis-chart"
        })
    },

    _init: function () {
        BI.MultiAxisChart.superclass._init.apply(this, arguments);
        var self = this;
        this.xAxis = [{
            type: "category",
            title: {
                style: {"fontFamily":"inherit","color":"#808080","fontSize":"12px","fontWeight":""}
            },
            labelStyle: {
                "fontFamily":"inherit","color":"#808080","fontSize":"12px"
            },
            position: "bottom",
            gridLineWidth: 0
        }];
        this.combineChart = BI.createWidget({
            type: "bi.combine_chart",
            xAxis: this.xAxis,
            formatConfig: BI.bind(this._formatConfig, this),
            element: this.element
        });
        this.combineChart.on(BI.CombineChart.EVENT_CHANGE, function (obj) {
            self.fireEvent(BI.MultiAxisChart.EVENT_CHANGE, obj);
        });
    },

    _formatConfig: function(config, items){
        var self = this, o = this.options;
        config.colors = this.config.chart_color;
        config.style = this.formatChartStyle();
        this.formatCordon();
        switch (this.config.chart_legend){
            case BICst.CHART_LEGENDS.BOTTOM:
                config.legend.enabled = true;
                config.legend.position = "bottom";
                config.legend.maxHeight = self.constants.LEGEND_HEIGHT;
                break;
            case BICst.CHART_LEGENDS.RIGHT:
                config.legend.enabled = true;
                config.legend.position = "right";
                break;
            case BICst.CHART_LEGENDS.NOT_SHOW:
            default:
                config.legend.enabled = false;
                break;
        }
        config.plotOptions.dataLabels.enabled = this.config.show_data_label;
        config.dataSheet.enabled = this.config.show_data_table;
        config.xAxis[0].showLabel = !config.dataSheet.enabled;
        config.zoom.zoomTool.visible = this.config.show_zoom;
        if(this.config.show_zoom === true){
            delete config.dataSheet;
            delete config.zoom.zoomType;
        }

        config.yAxis = this.yAxis;
        BI.each(config.yAxis, function(idx, axis){
            var title = "";
            switch (axis.axisIndex){
                case self.constants.LEFT_AXIS:
                    title = self.getXYAxisUnit(self.config.left_y_axis_number_level, self.constants.LEFT_AXIS);
                    axis.title.text = self.config.show_left_y_axis_title === true ? self.config.left_y_axis_title + title : title;
                    axis.title.rotation = self.constants.ROTATION;
                    axis.labelStyle.color = axis.lineColor = axis.tickColor = config.colors[0];
                    BI.extend(axis, {
                        lineWidth: self.config.line_width,
                        showLabel: self.config.show_label,
                        enableTick: self.config.enable_tick,
                        reversed: self.config.left_y_axis_reversed,
                        enableMinorTick: self.config.enable_minor_tick,
                        gridLineWidth: self.config.show_grid_line === true ? 1 : 0,
                        formatter: self.formatTickInXYaxis(self.config.left_y_axis_style, self.config.left_y_axis_number_level)
                    });
                    self.formatNumberLevelInYaxis(config, items, self.config.left_y_axis_number_level, idx, axis.formatter);
                    break;
                case self.constants.RIGHT_AXIS:
                    title = self.getXYAxisUnit(self.config.right_y_axis_number_level, self.constants.RIGHT_AXIS);
                    axis.title.text = self.config.show_right_y_axis_title === true ? self.config.right_y_axis_title + title : title;
                    axis.title.rotation = self.constants.ROTATION;
                    axis.labelStyle.color = axis.lineColor = axis.tickColor = config.colors[1];
                    BI.extend(axis, {
                        lineWidth: self.config.line_width,
                        showLabel: self.config.show_label,
                        enableTick: self.config.enable_tick,
                        reversed: self.config.right_y_axis_reversed,
                        enableMinorTick: self.config.enable_minor_tick,
                        gridLineWidth: self.config.show_grid_line === true ? 1 : 0,
                        formatter: self.formatTickInXYaxis(self.config.right_y_axis_style, self.config.right_y_axis_number_level)
                    });
                    self.formatNumberLevelInYaxis(config, items, self.config.right_y_axis_number_level, idx, axis.formatter);
                    break;
                case self.constants.RIGHT_AXIS_SECOND:
                    title = self.getXYAxisUnit(self.config.right_y_axis_second_number_level, self.constants.RIGHT_AXIS_SECOND);
                    axis.title.text = self.config.show_right_y_axis_second_title === true ? self.config.right_y_axis_second_title + title : title;
                    axis.title.rotation = self.constants.ROTATION;
                    axis.labelStyle.color = axis.lineColor = axis.tickColor = config.colors[2];
                    BI.extend(axis, {
                        lineWidth: self.config.line_width,
                        showLabel: self.config.show_label,
                        enableTick: self.config.enable_tick,
                        reversed: self.config.right_y_axis_second_reversed,
                        enableMinorTick: self.config.enable_minor_tick,
                        gridLineWidth: self.config.show_grid_line === true ? 1 : 0,
                        formatter: self.formatTickInXYaxis(self.config.right_y_axis_second_style, self.config.right_y_axis_second_number_level)
                    });
                    self.formatNumberLevelInYaxis(config, items, self.config.right_y_axis_second_number_level, idx, axis.formatter);
                    break;
                default:
                    break;
            }
        });
        config.xAxis[0].title.align = "center";
        config.xAxis[0].title.text = this.config.show_x_axis_title === true ? this.config.x_axis_title : "";
        BI.extend(config.xAxis[0], {
            lineWidth: this.config.line_width,
            enableTick: this.config.enable_tick,
            labelRotation: this.config.text_direction,
            enableMinorTick: this.config.enable_minor_tick,
            gridLineWidth: this.config.show_grid_line === true ? 1 : 0
        });

        var lineItem = [];
        var otherItem = [];
        BI.each(items, function(idx, item){
            item.color = [config.yAxis[idx].labelStyle.color];
            if(item.type === "line"){
                lineItem.push(item);
            }else{
                otherItem.push(item);
            }
        });

        //为了给数据标签加个%,还要遍历所有的系列，唉
        if(config.plotOptions.dataLabels.enabled === true){
            BI.each(items, function(idx, item){
                var isNeedFormatDataLabel = false;
                switch (config.yAxis[item.yAxis].axisIndex) {
                    case self.constants.LEFT_AXIS:
                        if(self.config.left_y_axis_number_level === BICst.TARGET_STYLE.NUM_LEVEL.PERCENT){
                            isNeedFormatDataLabel = true;
                        }
                        break;
                    case self.constants.RIGHT_AXIS:
                        if(self.config.right_y_axis_number_level === BICst.TARGET_STYLE.NUM_LEVEL.PERCENT){
                            isNeedFormatDataLabel = true;
                        }
                        break;
                    case self.constants.RIGHT_AXIS_SECOND:
                        if(self.config.right_y_axis_second_number_level === BICst.TARGET_STYLE.NUM_LEVEL.PERCENT){
                            isNeedFormatDataLabel = true;
                        }
                        break;
                }
                if(isNeedFormatDataLabel === true){
                    item.dataLabels = {
                        "style": "{fontFamily:inherit, color: #808080, fontSize: 12px}",
                        "align": "outside",
                        enabled: true,
                        formatter: {
                            identifier: "${VALUE}",
                            valueFormat: config.yAxis[item.yAxis].formatter
                        }
                    };
                }
            });
        }

        return [BI.concat(otherItem, lineItem), config];
    },

    formatChartStyle: function () {
        switch (this.config.chart_style) {
            case BICst.CHART_STYLE.STYLE_GRADUAL:
                return "gradual";
            case BICst.CHART_STYLE.STYLE_NORMAL:
            default:
                return "normal";
        }
    },

    formatCordon: function () {
        var self = this;
        var magnify = 1;
        BI.each(this.config.cordon, function (idx, cor) {
            if (idx === 0 && self.xAxis.length > 0) {
                magnify = self.calcMagnify(self.config.x_axis_number_level);
                self.xAxis[0].plotLines = BI.map(cor, function (i, t) {
                    return BI.extend(t, {
                        value: t.value.div(magnify),
                        width: 1,
                        label: {
                            "style": {
                                "fontFamily": "inherit",
                                "color": "#808080",
                                "fontSize": "12px",
                                "fontWeight": ""
                            },
                            "text": t.text,
                            "align": "top"
                        }
                    });
                });
            }
            if (idx > 0 && self.yAxis.length >= idx) {
                magnify = 1;
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
                    default:
                        break;
                }
                self.yAxis[idx - 1].plotLines = BI.map(cor, function (i, t) {
                    return BI.extend(t, {
                        value: t.value.div(magnify),
                        width: 1,
                        label: {
                            "style": {
                                "fontFamily": "inherit",
                                "color": "#808080",
                                "fontSize": "12px",
                                "fontWeight": ""
                            },
                            "text": t.text,
                            "align": "left"
                        }
                    });
                });
            }
        })
    },

    getXYAxisUnit: function (numberLevelType, position) {
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
            default:
                break;
        }
        if (position === this.constants.X_AXIS) {
            this.config.x_axis_unit !== "" && (unit = unit + this.config.x_axis_unit)
        }
        if (position === this.constants.LEFT_AXIS) {
            this.config.left_y_axis_unit !== "" && (unit = unit + this.config.left_y_axis_unit)
        }
        if (position === this.constants.RIGHT_AXIS) {
            this.config.right_y_axis_unit !== "" && (unit = unit + this.config.right_y_axis_unit)
        }
        if (position === this.constants.RIGHT_AXIS_SECOND) {
            this.config.right_y_axis_second_unit !== "" && (unit = unit + this.config.right_y_axis_second_unit)
        }
        return unit === "" ? unit : "(" + unit + ")";
    },

    populate: function (items, options, types) {
        var self = this, c = this.constants;
        this.config = {
            left_y_axis_title: options.left_y_axis_title || "",
            right_y_axis_title: options.right_y_axis_title || "",
            right_y_axis_second_title: options.right_y_axis_second_title || "",
            chart_color: options.chart_color || ["#5caae4", "#70cc7f", "#ebbb67", "#e97e7b", "#6ed3c9"],
            chart_style: options.chart_style || c.NORMAL,
            left_y_axis_style: options.left_y_axis_style || c.NORMAL,
            right_y_axis_style: options.right_y_axis_style || c.NORMAL,
            right_y_axis_second_style: options.right_y_axis_second_style || c.NORMAL,
            show_x_axis_title: options.show_x_axis_title || false,
            show_left_y_axis_title: options.show_left_y_axis_title || false,
            show_right_y_axis_title: options.show_right_y_axis_title || false,
            show_right_y_axis_second_title: options.show_right_y_axis_second_title || false,
            left_y_axis_reversed: options.left_y_axis_reversed || false,
            right_y_axis_reversed: options.right_y_axis_reversed || false,
            right_y_axis_second_reversed: options.right_y_axis_second_reversed || false,
            left_y_axis_number_level: options.left_y_axis_number_level || c.NORMAL,
            right_y_axis_number_level:  options.right_y_axis_number_level || c.NORMAL,
            right_y_axis_second_number_level: options.right_y_axis_second_number_level || c.NORMAL,
            x_axis_unit: options.x_axis_unit || "",
            left_y_axis_unit: options.left_y_axis_unit || "",
            right_y_axis_unit: options.right_y_axis_unit || "",
            right_y_axis_second_unit: options.right_y_axis_second_unit || "",
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
            enable_minor_tick: BI.isNull(options.enable_minor_tick) ? true : options.enable_minor_tick
        };
        this.options.items = items;

        this.yAxis = [];
        BI.each(types, function(idx, type){
            if(BI.isEmptyArray(type)){
                return;
            }
            var newYAxis = {
                type: "value",
                title: {
                    style: {"fontFamily":"inherit","color":"#808080","fontSize":"12px","fontWeight":""}
                },
                labelStyle: {
                    "fontFamily":"inherit","color":"#808080","fontSize":"12px"
                },
                position: idx > 0 ? "right" : "left",
                lineWidth: 1,
                axisIndex: idx,
                gridLineWidth: 0
            };
            self.yAxis.push(newYAxis);
        });

        this.combineChart.populate(items, types);
    },

    resize: function () {
        this.combineChart.resize();
    },

    magnify: function(){
        this.combineChart.magnify();
    }
});
BI.MultiAxisChart.EVENT_CHANGE = "EVENT_CHANGE";
$.shortcut('bi.multi_axis_chart', BI.MultiAxisChart);