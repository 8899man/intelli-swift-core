package com.fr.bi.cal.analyze.report.report.widget.chart.export.format.utils;

import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.utils.program.BIStringUtils;
import com.fr.general.ComparatorUtils;
import com.fr.general.Inter;
import com.fr.json.JSONArray;
import com.fr.json.JSONException;
import com.fr.json.JSONObject;
import com.fr.stable.StableUtils;
import com.fr.stable.StringUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kary on 2017/5/11.
 */
public class BITableCellFormatHelper {
    static final String PERCENT_SYMBOL = "%";
    static final int DEFAULT_SCALE = 1;
    static final String NONE_VALUE = "--";

    public static String targetValueFormat(JSONObject settings, String text) throws JSONException {
        if (BIStringUtils.isEmptyString(text)) {
            return text;
        }
        try {
            float value = Float.valueOf(text);
            value = parseNumByLevel(settings, value);
            text = parseNumByFormat(decimalFormat(settings), value);
            String tail = createTailUnit(settings);
            return text + tail;
        } catch (NumberFormatException e) {
            return text;
        }
    }

    private static String parseNumByFormat(String format, float value) {
        DecimalFormat decimalFormat = new DecimalFormat(format);
        return decimalFormat.format(value);
    }

    private static float parseNumByLevel(JSONObject setting, float value) {
        try {
            if (value == 0) {
                return value;
            }
            int numLevel = setting.optInt("numLevel", DEFAULT_SCALE);
            switch (numLevel) {
                case BIReportConstant.TARGET_STYLE.NUM_LEVEL.TEN_THOUSAND:
                    value /= Math.pow(10, 4);
                    break;
                case BIReportConstant.TARGET_STYLE.NUM_LEVEL.MILLION:
                    value /= Math.pow(10, 6);
                    break;
                case BIReportConstant.TARGET_STYLE.NUM_LEVEL.YI:
                    value /= Math.pow(10, 8);
                    break;
                case BIReportConstant.TARGET_STYLE.NUM_LEVEL.PERCENT:
                    value *= Math.pow(10, 2);
                    break;
            }
        } catch (Exception e) {
            BILoggerFactory.getLogger(BITableCellFormatHelper.class).error(e.getMessage(), e);
        }
        return value;
    }

    public static String createTailUnit(JSONObject settings) {
        String scaleUnit = scaleUnit(settings.optInt("numLevel"));
        String unit = settings.optString("unit", StringUtils.EMPTY);
        return scaleUnit + unit;
    }

    public static String dateFormat(JSONObject format, int groupType, String text) throws JSONException {
        if (StringUtils.isBlank(text)||ComparatorUtils.equals(text, NONE_VALUE)) {
            return text;
        }
        JSONObject dateFormat = format.optJSONObject("dateFormat");
        int dateFormatType = dateFormat == null ? BIReportConstant.DATE_FORMAT.SPLIT : dateFormat.optInt("type", BIReportConstant.DATE_FORMAT.SPLIT);
        Number dateCategory = StableUtils.string2Number(text);
        long dateValue = dateCategory == null ? 0L : dateCategory.longValue();
        switch (groupType) {
            case BIReportConstant.GROUP.S:
                text = FULL_QUARTER_NAMES[(int) dateValue];
                break;
            case BIReportConstant.GROUP.M:
                text = FULL_MONTH_NAMES[(int) dateValue];
                break;
            case BIReportConstant.GROUP.W:
                text = FULL_WEEK_NAMES[(int) dateValue];
                break;
            case BIReportConstant.GROUP.YMD:
                text = formatYMDByDateFormat(dateValue, dateFormatType);
                break;
            case BIReportConstant.GROUP.YMDHMS:
                text = formatYMDHMSByDateFormat(dateValue, dateFormatType);
                break;
            case BIReportConstant.GROUP.YMDH:
                text = formatYMDHByDateFormat(dateValue, dateFormatType);
                break;
            case BIReportConstant.GROUP.YMDHM:
                text = formatYMDHMByDateFormat(dateValue, dateFormatType);
                break;
            case BIReportConstant.GROUP.YS:
                text = formatCombineDateByDateFormat(text, dateFormatType, new String[]{getLocText("BI-Basic_Year"), getLocText("BI-Basic_Quarter")});
                break;
            case BIReportConstant.GROUP.YM:
                text = formatCombineDateByDateFormat(text, dateFormatType, new String[]{getLocText("BI-Basic_Year"), getLocText("BI-Basic_Month")});
                break;
            case BIReportConstant.GROUP.YW:
                text = formatCombineDateByDateFormat(text, dateFormatType, new String[]{getLocText("BI-Basic_Year"), getLocText("BI-Week_Simple")});
                break;
        }
        return text;
    }

    private static String formatYMDByDateFormat(long dateValue, int dateFormatType) {
        Date date = new Date(dateValue);
        SimpleDateFormat formatter;
        if (dateFormatType == BIReportConstant.DATE_FORMAT.CHINESE) {
            formatter = new SimpleDateFormat(String.format("yyyy%sMM%sdd%s", getLocText("BI-Basic_Year"), getLocText("BI-Basic_Month"), getLocText("BI-Date_Day")));
        } else {
            formatter = new SimpleDateFormat("yyyy-MM-dd");
        }
        return formatter.format(date);
    }

    private static String formatYMDHMSByDateFormat(long dateValue, int dateFormatType) {
        Date date = new Date(dateValue);
        SimpleDateFormat formatter;

        if (dateFormatType == BIReportConstant.DATE_FORMAT.CHINESE) {
            formatter = new SimpleDateFormat(String.format("yyyy%sMM%sdd%s H%sm%ss%s", getLocText("BI-Basic_Year"), getLocText("BI-Basic_Month"), getLocText("BI-Date_Day"),
                    getLocText("BI-Hour_Sin"), getLocText("BI-Basic_Minute"), getLocText("BI-Basic_Seconds")));
        } else {
            formatter = new SimpleDateFormat("yyyy-MM-dd H:m:s");
        }
        return formatter.format(date);
    }

    private static String formatYMDHByDateFormat(long dateValue, int dateFormatType) {
        Date date = new Date(dateValue);
        SimpleDateFormat formatter;

        if (dateFormatType == BIReportConstant.DATE_FORMAT.CHINESE) {
            formatter = new SimpleDateFormat(String.format("yyyy%sMM%sdd%s H%s", getLocText("BI-Basic_Year"), getLocText("BI-Basic_Month"),
                    getLocText("BI-Date_Day"), getLocText("BI-Hour_Sin")));
        } else {
            formatter = new SimpleDateFormat("yyyy-MM-dd H");
        }
        return formatter.format(date);
    }

    private static String formatYMDHMByDateFormat(long dateValue, int dateFormatType) {
        Date date = new Date(dateValue);
        SimpleDateFormat formatter;

        if (dateFormatType == BIReportConstant.DATE_FORMAT.CHINESE) {
            formatter = new SimpleDateFormat(String.format("yyyy%sMM%sdd%s H%sm%s", getLocText("BI-Basic_Year"), getLocText("BI-Basic_Month"), getLocText("BI-Date_Day"),
                    getLocText("BI-Hour_Sin"), getLocText("BI-Basic_Minute")));
        } else {
            formatter = new SimpleDateFormat("yyyy-MM-dd H:m");
        }
        return formatter.format(date);
    }

    private static String getLocText(String key) {
        return Inter.getLocText(key);
    }

    private static String formatCombineDateByDateFormat(String category, int dateFormatType, String[] format) {

        if (dateFormatType == BIReportConstant.DATE_FORMAT.CHINESE) {
            String[] text = category.split("-");

            if (text.length == format.length) {
                String resultText = new String();
                for (int i = 0, len = text.length; i < len; i++) {
                    resultText += (text[i] + format[i]);
                }
                return resultText;
            }
        }

        return category;
    }

    private static final String[] FULL_QUARTER_NAMES = new String[]{
            Inter.getLocText("BI-Quarter_1"),
            Inter.getLocText("BI-Quarter_1"),
            Inter.getLocText("BI-Quarter_2"),
            Inter.getLocText("BI-Quarter_3"),
            Inter.getLocText("BI-Quarter_4")
    };
    private static final String[] FULL_MONTH_NAMES = new String[]{
            Inter.getLocText("BI-Basic_January"),
            Inter.getLocText("BI-Basic_January"),
            Inter.getLocText("BI-Basic_February"),
            Inter.getLocText("BI-Basic_March"),
            Inter.getLocText("BI-Basic_April"),
            Inter.getLocText("BI-Basic_May"),
            Inter.getLocText("BI-Basic_June"),
            Inter.getLocText("BI-Basic_July"),
            Inter.getLocText("BI-Basic_August"),
            Inter.getLocText("BI-Basic_September"),
            Inter.getLocText("BI-Basic_October"),
            Inter.getLocText("BI-Basic_November"),
            Inter.getLocText("BI-Basic_December")
    };
    private static final String[] FULL_WEEK_NAMES = new String[]{
            Inter.getLocText("BI-Basic_Sunday"),
            Inter.getLocText("BI-Basic_Monday"),
            Inter.getLocText("BI-Basic_Tuesday"),
            Inter.getLocText("BI-Basic_Wednesday"),
            Inter.getLocText("BI-Basic_Thursday"),
            Inter.getLocText("BI-Basic_Friday"),
            Inter.getLocText("BI-Basic_Saturday"),
            Inter.getLocText("BI-Basic_Sunday")
    };

    private static String decimalFormat(JSONObject setting) {
        boolean hasSeparator = setting.optBoolean("numSeparators", true);
        int type = setting.optInt("format", BIReportConstant.TARGET_STYLE.FORMAT.NORMAL);//默认为自动
        String format;
        switch (type) {
            case BIReportConstant.TARGET_STYLE.FORMAT.NORMAL:
                format = hasSeparator ? "#,###.##" : "#.##";
                break;
            case BIReportConstant.TARGET_STYLE.FORMAT.ZERO2POINT:
                format = hasSeparator ? "#,###" : "#0";
                break;
            default:
                format = hasSeparator ? "#,###." : "#0.";
                for (int i = 0; i < type; i++) {
                    format += "0";
                }
        }

        return format;
    }

    private static String scaleUnit(int level) {
        String unit = StringUtils.EMPTY;

        if (level == BIReportConstant.TARGET_STYLE.NUM_LEVEL.TEN_THOUSAND) {

            unit = getLocText("BI-Basic_Wan");

        } else if (level == BIReportConstant.TARGET_STYLE.NUM_LEVEL.MILLION) {

            unit = getLocText("BI-Basic_Million");

        } else if (level == BIReportConstant.TARGET_STYLE.NUM_LEVEL.YI) {

            unit = getLocText("BI-Basic_Yi");

        } else if (level == BIReportConstant.TARGET_STYLE.NUM_LEVEL.PERCENT) {

            unit = PERCENT_SYMBOL;

        }

        return unit;
    }

    public static JSONObject createTextStyle(JSONObject settings, String text) throws JSONException {
        Float num;
        try {
            num = Float.valueOf(text);
        } catch (NumberFormatException e) {
            return JSONObject.create();
        }
        int markResult = getTextCompareResult(settings, num);
        int iconStyle = settings.getInt("iconStyle");
        String textColor = getTextColor(settings, num);
        return JSONObject.create().put("markResult", markResult).put("iconStyle", iconStyle).put("color", textColor);
    }

    private static int getTextCompareResult(JSONObject settings, Float num) throws JSONException {
        if (!settings.has("mark") || num < settings.getInt("mark")) {
            return BIReportConstant.TARGET_COMPARE_RES.LESS;
        } else {
            return num == settings.getLong("mark") ? BIReportConstant.TARGET_COMPARE_RES.EQUAL : BIReportConstant.TARGET_COMPARE_RES.MORE;
        }
    }

    private static String getTextColor(JSONObject settings, Float num) throws JSONException {
        JSONArray conditions = settings.optJSONArray("conditions");
        if (null!=conditions) {
            for (int i = 0; i < conditions.length(); i++) {
                JSONObject range = conditions.getJSONObject(i).getJSONObject("range");
                long min = range.getLong("min");
                long max = range.getLong("max");
                boolean minBoolean = range.optBoolean("closemin", false) ? num >= min : num > min;
                boolean maxBoolean = range.optBoolean("closemax", false) ? num <= max : num < max;
                if (minBoolean && maxBoolean) {
                    return conditions.getJSONObject(i).getString("color");
                }
            }
        }
        return StringUtils.EMPTY;
    }

}
