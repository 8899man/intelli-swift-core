package com.fr.bi.cal.analyze.report.report.widget.chart.style;

import com.fr.base.CoreDecimalFormat;
import com.fr.bi.stable.constant.BIChartSettingConstant;
import com.fr.bi.stable.constant.BIReportConstant;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.general.Inter;
import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;
import com.fr.stable.StringUtils;

import java.text.DecimalFormat;

/**
 * Created by windy on 2016/8/31.
 * 所有图的抽象基类，提供数据格式化方法，样式格式化方法
 */
public abstract class BIAbstractChartSetting implements BIChartSetting {

    private DecimalFormat TWOFIEXEDFORMAT = new CoreDecimalFormat(new DecimalFormat("##.##;-##.##"), "");
    private DecimalFormat FOURFIEXEDFORMAT = new CoreDecimalFormat(new DecimalFormat("##.####;-##.####"), "");

    public BIAbstractChartSetting() {
        TWOFIEXEDFORMAT.setGroupingUsed(false);
        FOURFIEXEDFORMAT.setGroupingUsed(false);
    }

    @Override
    public JSONObject formatItems(JSONArray data, JSONArray types, JSONObject options) throws JSONException{
        JSONArray result = new JSONArray();
        int yAxisIndex = 0;
        for(int i = 0; i < data.length(); i++){
            JSONArray belongAxisItems = data.getJSONArray(i);
            JSONArray combineItems = combineChildItems(types.getJSONArray(i), belongAxisItems);
            for(int j = 0; j < combineItems.length(); j++){
                JSONObject axisItems = combineItems.getJSONObject(j);
                result.put(axisItems.put("yAxis", yAxisIndex));
            }
            if(combineItems.length() > 0){
                yAxisIndex ++;
            }
        }
        JSONObject config = combineConfig();
        return new JSONObject().put("result", result).put("config", config);
    }

    @Override
    public JSONArray formatTypes(JSONArray data, JSONArray types) throws JSONException {
        return types;
    }

    private JSONArray combineChildItems(JSONArray types, JSONArray items) throws JSONException{
        JSONArray result = new JSONArray();
        for(int i = 0; i < items.length(); i++){
            JSONObject item = items.getJSONObject(i);
            result.put(formatChildItem(types.getInt(i), item));
        }
        return result;
    }

    private JSONObject formatChildItem(int type, JSONObject items) throws JSONException{
        switch (type) {
            case BIReportConstant.WIDGET.BAR:
            case BIReportConstant.WIDGET.ACCUMULATE_BAR:
            case BIReportConstant.WIDGET.COMPARE_BAR:
                items.put("type", "bar");
                break;
            case BIReportConstant.WIDGET.BUBBLE:
            case BIReportConstant.WIDGET.FORCE_BUBBLE:
                items.put("type",  "bubble");
                break;
            case BIReportConstant.WIDGET.SCATTER:
                items.put("type", "scatter");
                break;
            case BIReportConstant.WIDGET.COLUMN:
            case BIReportConstant.WIDGET.ACCUMULATE_COLUMN:
            case BIReportConstant.WIDGET.PERCENT_ACCUMULATE_COLUMN:
            case BIReportConstant.WIDGET.COMPARE_COLUMN:
            case BIReportConstant.WIDGET.FALL_COLUMN:
                items.put("type", "column");
                break;
            case BIReportConstant.WIDGET.LINE:
                items.put("type", "line");
                break;
            case BIReportConstant.WIDGET.AREA:
            case BIReportConstant.WIDGET.ACCUMULATE_AREA:
            case BIReportConstant.WIDGET.COMPARE_AREA:
            case BIReportConstant.WIDGET.RANGE_AREA:
            case BIReportConstant.WIDGET.PERCENT_ACCUMULATE_AREA:
                items.put("type", "area");
                break;
            case BIReportConstant.WIDGET.DONUT:
                items.put("type", "pie");
                break;
            case BIReportConstant.WIDGET.RADAR:
            case BIReportConstant.WIDGET.ACCUMULATE_RADAR:
                items.put("type", "radar");
                break;
            case BIReportConstant.WIDGET.PIE:
                items.put("type", "pie");
                break;
            case BIReportConstant.WIDGET.DASHBOARD:
                items.put("type", "gauge");
                break;
            case BIReportConstant.WIDGET.MAP:
                items.put("type", "areaMap");
                break;
            case BIReportConstant.WIDGET.GIS_MAP:
                items.put("type", "pointMap");
                break;
            default:
                items.put("type", "column");
                break;
        }
        return items;
    }

    private JSONObject combineConfig() throws JSONException{
        return new JSONObject("{\"title\":'',\"plotOptions\":{\"rotatable\":false,\"startAngle\":0,\"borderRadius\":0,\"endAngle\":360,\"innerRadius\":\"0.0%\",\"layout\":\"horizontal\",\"hinge\":\"rgb(101,107,109)\",\"dataLabels\":{\"style\":{\"fontFamily\":\"inherit\",\"color\":\"#808080\",\"fontSize\":\"12px\"},\"formatter\":{\"identifier\":\"${VALUE}\",\"valueFormat\": \""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT2DECIMAL +"\",\"seriesFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT +"\",\"percentFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMATPERCENTAGE +"\",\"categoryFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT + "\",\"XFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT2DECIMAL +"\",\"YFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT2DECIMAL +"\",\"sizeFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT2DECIMAL +"\"},\"align\":\"outside\",\"enabled\":false},\"percentageLabel\":{\"formatter\":{\"identifier\":\"${PERCENT}\",\"valueFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT2DECIMAL +"\",\"seriesFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT +"\",\"percentFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMATPERCENTAGE +"\",\"categoryFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT +"\"},\"style\":{\"fontFamily\":\"Microsoft YaHei, Hiragino Sans GB W3\",\"color\":\"#808080\",\"fontSize\":\"12px\"},\"align\":\"bottom\",\"enabled\":true},\"valueLabel\":{\"formatter\":{\"identifier\":\"${SERIES}${VALUE}\",\"valueFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT2DECIMAL +"\",\"seriesFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT +"\",\"percentFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMATPERCENTAGE +"\",\"categoryFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT +"\"},\"backgroundColor\":\"rgb(255,255,0)\",\"style\":{\"fontFamily\":\"Microsoft YaHei, Hiragino Sans GB W3\",\"color\":\"#808080\",\"fontSize\":\"12px\"},\"align\":\"inside\",\"enabled\":true},\"hingeBackgroundColor\":\"rgb(220,242,249)\",\"seriesLabel\":{\"formatter\":{\"identifier\":\"${CATEGORY}\",\"valueFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT2DECIMAL +"\",\"seriesFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT +"\",\"percentFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMATPERCENTAGE +"\",\"categoryFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT +"\"},\"style\":{\"fontFamily\":\"Microsoft YaHei, Hiragino Sans GB W3\",\"color\":\"#808080\",\"fontSize\":\"12px\"},\"align\":\"bottom\",\"enabled\":true},\"style\":\"pointer\",\"paneBackgroundColor\":\"rgb(252,252,252)\",\"needle\":\"rgb(229,113,90)\",\"large\":false,\"connectNulls\":false,\"shadow\":true,\"curve\":false,\"sizeBy\":\"area\",\"tooltip\":{\"formatter\":{\"identifier\":\"${SERIES}${X}${Y}${SIZE}{CATEGORY}${VALUE}\",\"valueFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT +"\",\"seriesFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT +"\",\"percentFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMATPERCENTAGE +"\",\"categoryFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT +"\",\"XFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT2DECIMAL +"\",\"sizeFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT2DECIMAL +"\",\"YFormat\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT2DECIMAL +"\"},\"shared\":false,\"padding\":5,\"backgroundColor\":\"rgba(0,0,0,0.4980392156862745)\",\"borderColor\":\"rgb(0,0,0)\",\"shadow\":false,\"borderRadius\":2,\"borderWidth\":0,\"follow\":false,\"enabled\":true,\"animation\":true,\"style\":{\"fontFamily\":\"Microsoft YaHei, Hiragino Sans GB W3\",\"color\":\"#c4c6c6\",\"fontSize\":\"12px\",\"fontWeight\":\"\"}},\"maxSize\":80,\"fillColorOpacity\":1,\"step\":false,\"force\":false,\"minSize\":15,\"displayNegative\":true,\"categoryGap\":\"16.0%\",\"borderColor\":\"rgb(255,255,255)\",\"borderWidth\":1,\"gap\":\"22.0%\",\"animation\":true,\"lineWidth\":2,\"bubble\":{\"large\":false,\"connectNulls\":false,\"shadow\":true,\"curve\":false,\"sizeBy\":\"area\",\"maxSize\":80,\"minSize\":15,\"lineWidth\":0,\"animation\":true,\"fillColorOpacity\":0.699999988079071,\"marker\":{\"symbol\":\"circle\",\"radius\":28.39695010101295,\"enabled\":true}}},\"dTools\":{\"enabled\":false,\"style\":{\"fontFamily\":\"Microsoft YaHei, Hiragino Sans GB W3\",\"color\":\"#1a1a1a\",\"fontSize\":\"12px\"},\"backgroundColor\":\"white\"},\"dataSheet\":{\"enabled\":false,\"borderColor\":\"rgb(0,0,0)\",\"borderWidth\":1,\"formatter\":\""+ BIChartSettingConstant.DEFAULT_FORMAT_FUNCTIONS.CONTENTFORMAT2DECIMAL +"\",\"style\":{\"fontFamily\":\"Microsoft YaHei, Hiragino Sans GB W3\",\"color\":\"#808080\",\"fontSize\":\"12px\"}},\"borderColor\":\"rgb(238,238,238)\",\"shadow\":false,\"legend\":{\"borderColor\":\"rgb(204,204,204)\",\"borderRadius\":0,\"shadow\":false,\"borderWidth\":0,\"visible\":true,\"style\":{\"fontFamily\":\"Microsoft YaHei, Hiragino Sans GB W3\",\"color\":\"#1a1a1a\",\"fontSize\":\"12px\"},\"position\":\"right\",\"enabled\":false},\"rangeLegend\":{\"range\":{\"min\":0,\"color\":[[0,\"rgb(182,226,255)\"],[0.5,\"rgb(109,196,255)\"],[1,\"rgb(36,167,255)\"]],\"max\":266393},\"enabled\":false},\"zoom\":{\"zoomType\":\"xy\",\"zoomTool\":{\"visible\":false,\"resize\":true,\"from\":\"\",\"to\":\"\"}},\"plotBorderColor\":\"rgba(255,255,255,0)\",\"tools\":{\"hidden\":true,\"toImage\":{\"enabled\":true},\"sort\":{\"enabled\":true},\"enabled\":false,\"fullScreen\":{\"enabled\":true}},\"plotBorderWidth\":0,\"colors\":[\"rgb(99,178,238)\",\"rgb(118,218,145)\"],\"borderRadius\":0,\"borderWidth\":0,\"style\":\"normal\",\"plotShadow\":false,\"plotBorderRadius\":0}");
    }

    public JSONObject getFontStyle() throws JSONException{
        return new JSONObject("{" +
                "fontFamily:\"" + BIChartSettingConstant.FONT_STYLE.FONTFAMILY + "\"," +
                "color:\"" + BIChartSettingConstant.FONT_STYLE.COLOR + "\"," +
                "fontSize:\"" + BIChartSettingConstant.FONT_STYLE.FONTSIZE + "\"}"
        );
    }

    public void formatNumberLevelInYaxis(JSONObject config, JSONArray items, int type, int position, String formatter) throws JSONException{
        int magnify = this.calcMagnify(type);
        for(int i = 0; i < items.length(); i++){
            JSONObject item = items.getJSONObject(i);
            JSONArray data = item.getJSONArray("data");
            for(int j = 0; j < data.length(); j++){
                JSONObject da = data.getJSONObject(j);
                if(position == item.optInt("yAxis")){
                    double y = da.optDouble("y", 0);
                    da.put("y", this.FOURFIEXEDFORMAT.format(y / magnify));
                }
            }
            if(position == item.optInt("yAxis")){
                JSONObject tooltip = new JSONObject(config.getJSONObject("plotOptions").getJSONObject("tooltip").toString());
                if(tooltip.optJSONObject("formatter") != null) {
                    tooltip.getJSONObject("formatter").put("valueFormat", formatter);
                    item.put("tooltip", tooltip);
                }
            }
        }
    }

    public void formatNumberLevelInXaxis(JSONArray items, int type) throws JSONException{
        int magnify = this.calcMagnify(type);
        for(int i = 0; i < items.length(); i++){
            JSONObject item = items.getJSONObject(i);
            JSONArray data = item.getJSONArray("data");
            for(int j = 0; j < data.length(); j++){
                JSONObject da = data.getJSONObject(j);
                    double x = da.optDouble("x", 0);
                    da.put("x", this.FOURFIEXEDFORMAT.format(x / magnify));
            }
        }
    }

    public String formatXYDataWithMagnify(double number, int magnify){
        return this.FOURFIEXEDFORMAT.format(number / magnify);
    }

    public void formatChartLegend (JSONObject config, int chart_legend) throws JSONException{
        JSONObject legend = config.getJSONObject("legend");
        switch (chart_legend) {
            case BIChartSettingConstant.CHART_LEGENDS.BOTTOM:
                legend.put("enabled", true)
                        .put("position", "bottom")
                        .put("maxHeight", BIChartSettingConstant.LEGEND_HEIGHT);
                break;
            case BIChartSettingConstant.CHART_LEGENDS.RIGHT:
                legend.put("enabled", true)
                        .put("position", "right")
                        .put("maxHeight", BIChartSettingConstant.LEGEND_WIDTH);
                break;
            case BIChartSettingConstant.CHART_LEGENDS.NOT_SHOW:
            default:
                legend.put("enabled", false);
                break;
        }
    }

    public String formatTickInXYaxis(int type, int number_level, boolean separators){
        String formatter = "#.##";
        switch (type) {
            case BIChartSettingConstant.NORMAL:
                formatter = "#.##";
                break;
            case BIChartSettingConstant.ZERO2POINT:
                formatter = "#0";
                break;
            case BIChartSettingConstant.ONE2POINT:
                formatter = "#0.0";
                break;
            case BIChartSettingConstant.TWO2POINT:
                formatter = "#0.00";
                break;
        }
        if (separators) {
            formatter = "#,##0";
        }
        if (number_level == BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.PERCENT) {
            if (separators) {
                formatter = "#,##0%";
            } else if (type == BIChartSettingConstant.NORMAL) {
                formatter = "#0.##%";
            } else {
                formatter += '%';
            }
        }
        formatter += ";-" + formatter;
        return "function () { return window.BH ? BH.contentFormat(arguments[0], '" + formatter + "') : arguments[0]}";
    }

    public int calcMagnify(int type){
        int magnify = 1;
        switch (type) {
            case BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.NORMAL:
            case BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.PERCENT:
                magnify = 1;
                break;
            case BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.TEN_THOUSAND:
                magnify = 10000;
                break;
            case BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.MILLION:
                magnify = 1000000;
                break;
            case BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.YI:
                magnify = 100000000;
                break;
        }
        return magnify;
    }

    public String formatChartStyle(int style){
        switch (style) {
            case BIChartSettingConstant.CHART_STYLE.STYLE_GRADUAL:
                return "gradual";
            case BIChartSettingConstant.CHART_STYLE.STYLE_NORMAL:
            default:
                return "normal";
        }
    }

    public void formatCordon(JSONArray cordon, JSONArray xAxis, JSONArray yAxis, int xAxisNumberLevel, int leftYAxisNumberLevel, int rightYAxisNumberLevel, int rightYAxisSecondNumberLevel) throws JSONException {
        for (int i = 0; i < cordon.length(); i++) {
            JSONArray cor = cordon.getJSONArray(i);
            if (i == 0 && xAxis.length() > 0) {
                int magnify = this.calcMagnify(xAxisNumberLevel);
                JSONArray plotLines = new JSONArray();
                for (int j = 0; j < cor.length(); j++) {
                    JSONObject t = new JSONObject();
                    t.put("value", t.optDouble("value") / magnify)
                            .put("width", 1)
                            .put("label", new JSONObject()
                                    .put("style", this.getFontStyle())
                                    .put("text", t.optString("text"))
                                    .put("align", "top"));
                    plotLines.put(t);
                }
                xAxis.getJSONObject(0).put("plotLines", plotLines);
            }
            if (i > 0 && yAxis.length() >= i) {
                int magnify = 1;
                switch (i - 1) {
                    case BIChartSettingConstant.LEFT_AXIS:
                        magnify = this.calcMagnify(leftYAxisNumberLevel);
                        break;
                    case BIChartSettingConstant.RIGHT_AXIS:
                        magnify = this.calcMagnify(rightYAxisNumberLevel);
                        break;
                    case BIChartSettingConstant.RIGHT_AXIS_SECOND:
                        magnify = this.calcMagnify(rightYAxisSecondNumberLevel);
                        break;
                }
                JSONArray plotLines = new JSONArray();
                for (int j = 0; j < cor.length(); j++) {
                    JSONObject t = new JSONObject();
                    t.put("value", t.optDouble("value") / magnify)
                            .put("width", 1)
                            .put("label", new JSONObject()
                                    .put("style", this.getFontStyle())
                                    .put("text", t.optString("text"))
                                    .put("align", "top"));
                    plotLines.put(t);
                }
                yAxis.getJSONObject(i - 1).put("plotLines", plotLines);
            }
        }
    }

    public String getXYAxisTitle(int numberLevelType, int position, boolean show, String units, String title){
        String unit = "";
        switch (numberLevelType) {
            case BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.NORMAL:
                unit = "";
                break;
            case BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.TEN_THOUSAND:
                unit = Inter.getLocText("BI-Wan");
                break;
            case BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.MILLION:
                unit = Inter.getLocText("BI-Million");
                break;
            case BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.YI:
                unit = Inter.getLocText("BI-Yi");
                break;
        }
        unit = unit + units;
        unit = StringUtils.isEmpty(unit) ? unit : "(" + unit + ")";

        return show ? title + unit : unit;
    }

    public void formatPercentForItems(JSONArray items, JSONArray yAxis, int leftYAxisNumberLevel, int rightYAxisNumberLevel, int rightYAxisSecondNumberLevel, boolean numSeparator, boolean numSeparatorRight, boolean right2NumSeparators) throws JSONException {
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            boolean isNeedFormatDataLabel = false;
            switch (yAxis.getJSONObject(item.getInt("yAxis")).getInt("axisIndex")) {
                case BIChartSettingConstant.LEFT_AXIS:
                    if (leftYAxisNumberLevel == BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.PERCENT || numSeparator) {
                        isNeedFormatDataLabel = true;
                    }
                    break;
                case BIChartSettingConstant.RIGHT_AXIS:
                    if (rightYAxisNumberLevel == BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.PERCENT || numSeparatorRight) {
                        isNeedFormatDataLabel = true;
                    }
                    break;
                case BIChartSettingConstant.RIGHT_AXIS_SECOND:
                    if(rightYAxisSecondNumberLevel == BIChartSettingConstant.CHART_TARGET_STYLE.NUM_LEVEL.PERCENT || right2NumSeparators){
                        isNeedFormatDataLabel = true;
                    }
                    break;
            }
            if (isNeedFormatDataLabel) {
                item.put("dataLabels", new JSONObject()
                        .put("style", this.getFontStyle())
                        .put("align", "outside")
                        .put("enable", true)
                        .put("formatter", new JSONObject("{identifier:\"${VALUE}\", valueFormat:" +
                                yAxis.getJSONObject(item.getInt("yAxis")).getString("formatter") + "}")));

            }
        }
    }

    public void formatXYAxis(JSONObject options, JSONObject config, JSONArray items) throws JSONException{
        JSONArray yAxis = config.getJSONArray("yAxis");
        for(int i = 0; i < yAxis.length(); i++){
            JSONObject axis = yAxis.getJSONObject(i);
            int axisIndex = axis.optInt("axisIndex");
            switch (axisIndex) {
                case BIChartSettingConstant.LEFT_AXIS:
                    axis.getJSONObject("title").put("text", this.getXYAxisTitle(options.optInt("left_y_axis_number_level"), BIChartSettingConstant.LEFT_AXIS, options.optBoolean("show_left_y_axis_title"), options.optString("left_y_axis_unit"), options.optString("left_y_axis_title")));
                    axis.getJSONObject("title").put("rotation", BIChartSettingConstant.ROTATION);
                    axis.put("lineWidth", options.optInt("line_width"))
                            .put("showLabel", options.optBoolean("show_label"))
                            .put("enableTick", options.optBoolean("enable_tick"))
                            .put("reversed", options.optBoolean("left_y_axis_reversed"))
                            .put("enableMinorTick", options.optBoolean("enable_minor_tick"))
                            .put("gridLineWidth", options.optBoolean("show_grid_line") ? 1 : 0)
                            .put("formatter", this.formatTickInXYaxis(options.optInt("left_y_axis_style"), options.optInt("left_y_axis_number_level"), options.optBoolean("num_separators")));
                    this.formatNumberLevelInYaxis(config, items, options.optInt("left_y_axis_number_level"), i, axis.optString("formatter"));
                    break;
                case BIChartSettingConstant.RIGHT_AXIS:
                    axis.getJSONObject("title").put("text", this.getXYAxisTitle(options.optInt("right_y_axis_number_level"), BIChartSettingConstant.RIGHT_AXIS, options.optBoolean("show_right_y_axis_title"), options.optString("right_y_axis_unit"), options.optString("right_y_axis_title")));
                    axis.getJSONObject("title").put("rotation", BIChartSettingConstant.ROTATION);
                    axis.put("lineWidth", options.optInt("line_width"))
                            .put("showLabel", options.optBoolean("show_label"))
                            .put("enableTick", options.optBoolean("enable_tick"))
                            .put("reversed", options.optBoolean("right_y_axis_reversed"))
                            .put("enableMinorTick", options.optBoolean("enable_minor_tick"))
                            .put("gridLineWidth", options.optBoolean("show_grid_line") ? 1 : 0)
                            .put("formatter", this.formatTickInXYaxis(options.optInt("right_y_axis_style"), options.optInt("right_y_axis_number_level"), options.optBoolean("right_num_separators")));
                    this.formatNumberLevelInYaxis(config, items, options.optInt("right_y_axis_number_level"), i, axis.optString("formatter"));
                    break;
                case BIChartSettingConstant.RIGHT_AXIS_SECOND:
                    axis.getJSONObject("title").put("text", this.getXYAxisTitle(options.optInt("right_y_axis_second_number_level"), BIChartSettingConstant.RIGHT_AXIS_SECOND, options.optBoolean("show_right_y_axis_second_title"), options.optString("right_y_axis_second_unit"), options.optString("right_y_axis_second_title")));
                    axis.getJSONObject("title").put("rotation", BIChartSettingConstant.ROTATION);
                    axis.put("lineWidth", options.optInt("line_width"))
                            .put("showLabel", options.optBoolean("show_label"))
                            .put("enableTick", options.optBoolean("enable_tick"))
                            .put("reversed", options.optBoolean("right_y_axis_second_reversed"))
                            .put("enableMinorTick", options.optBoolean("enable_minor_tick"))
                            .put("gridLineWidth", options.optBoolean("show_grid_line") ? 1 : 0)
                            .put("formatter", this.formatTickInXYaxis(options.optInt("right_y_axis_second_style"), options.optInt("right_y_axis_second_number_level"), options.optBoolean("right2_num_separators")));
                    this.formatNumberLevelInYaxis(config, items, options.optInt("right_y_axis_second_number_level"), i, axis.optString("formatter"));
                    break;

            }
        }
        JSONArray xAxis = config.getJSONArray("xAxis");
        xAxis.getJSONObject(0).getJSONObject("title")
                .put("text", options.optBoolean("show_x_axis_title") ? options.getString("x_axis_title") : "")
                .put("align", "center");
        xAxis.getJSONObject(0)
                .put("lineWidth", options.optInt("line_width"))
                .put("enableTick", options.optBoolean("enable_tick"))
                .put("enableMinorTick", options.optBoolean("enable_minor_tick"))
                .put("labelRotation", options.optString("text_direction"))
                .put("gridLineWidth", options.optBoolean("show_grid_line") ? 1 : 0);
    }

    public void formatChartLineStyle(JSONObject config, int type) throws JSONException{
        JSONObject plotOptions = config.optJSONObject("plotOptions");
        switch (type) {
            case BIChartSettingConstant.CHART_SHAPE.RIGHT_ANGLE:
                plotOptions.put("curve", false);
                plotOptions.put("step", true);
                break;
            case BIChartSettingConstant.CHART_SHAPE.CURVE:
                plotOptions.put("curve", true);
                plotOptions.put("step", false);
                break;
            case BIChartSettingConstant.CHART_SHAPE.NORMAL:
            default:
                plotOptions.put("curve", false);
                plotOptions.put("step", false);
                break;
        }
    }

    public void formatChartPieStyle(JSONObject config, int type, int innerRadius, int endAngle) throws JSONException{
        JSONObject plotOptions = config.optJSONObject("plotOptions");
        switch (type){
            case BIChartSettingConstant.CHART_SHAPE.EQUAL_ARC_ROSE:
                plotOptions.put("roseType", "sameArc");
                break;
            case BIChartSettingConstant.CHART_SHAPE.NOT_EQUAL_ARC_ROSE:
                plotOptions.put("roseType", "differentArc");
                break;
            case BIChartSettingConstant.CHART_SHAPE.NORMAL:
            default:
                plotOptions.remove("roseType");
                break;
        }
        plotOptions.put("innerRadius", String.valueOf(innerRadius) + "%");
        plotOptions.put("endAngle", endAngle);
    }

    public double[] calculateValueNiceDomain(double minValue, double maxValue){
        minValue = Math.min(0, minValue);
        double tickInterval = linearTickInterval(minValue, maxValue);
        return linearNiceDomain(minValue, maxValue, tickInterval);
    }

    private double linearTickInterval(double minValue, double maxValue){
        int m = 5;
        double span = maxValue - minValue;
        double step = Math.pow(10, Math.floor(Math.log(span / m) / Math.log(10)));
        double err = m / span * step;
        if(err <= .15){
            step *= 10;
        }else{
            if(err <= .35){
                step *= 5;
            }else{
                if(err <= .75){
                    step *= 2;
                }
            }
        }
        return step;
    }

    private double[] linearNiceDomain(double minValue, double maxValue, double tickInterval){
        minValue = accMul(Math.floor(minValue / tickInterval), tickInterval);

        maxValue = accMul(Math.ceil(maxValue / tickInterval), tickInterval);

        return new double[]{
                minValue, maxValue
        };
    }

    private double accMul(double arg1, double arg2){
        int m = 0;
        String s1 = this.getTwoFiexedFormat().format(arg1);
        String s2 = this.getTwoFiexedFormat().format(arg2);
        try{
            m += s1.split(".")[1].length();
        }
        catch(Exception e){
            BILoggerFactory.getLogger().error(e.getMessage());
        }
        try{
            m += s2.split(".")[1].length();
        }
        catch(Exception e){
            BILoggerFactory.getLogger().error(e.getMessage());
        }
        return Double.parseDouble(s1.replace(".","")) * Double.parseDouble(s2.replace(".","")) / Math.pow(10,m);
    }

    public DecimalFormat getFourFiexedFormat(){
        return this.FOURFIEXEDFORMAT;
    }

    public DecimalFormat getTwoFiexedFormat(){
        return this.TWOFIEXEDFORMAT;
    }
}
