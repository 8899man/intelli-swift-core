package com.fr.bi.stable.operation.group.group;

import com.finebi.cube.api.ICubeColumnIndexReader;
import com.fr.bi.base.annotation.BICoreField;
import com.fr.bi.stable.gvi.GVIFactory;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.operation.group.AbstractGroup;
import com.fr.bi.stable.structure.collection.map.CubeLinkedHashMap;
import com.fr.bi.stable.utils.BICollectionUtils;
import com.fr.general.ComparatorUtils;
import com.fr.general.GeneralUtils;
import com.fr.json.JSONObject;
import com.fr.stable.StableUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by GUY on 2015/4/9.
 */
public class AutoGroup extends AbstractGroup {

    private static DecimalFormat nFormat = new DecimalFormat("#.##");
    private double start;
    @BICoreField
    private double interval;
    private transient double max;
    private transient double min;
    @BICoreField
    private boolean hasInterval;

    @Override
    public ICubeColumnIndexReader createGroupedMap(ICubeColumnIndexReader baseMap) {
        Number lastKey = (Number)BICollectionUtils.lastUnNullKey(baseMap);
        double tiMax = 0d;
        if(lastKey != null){
            tiMax = lastKey.doubleValue();
        }
        Number firstKey = ((Number) baseMap.firstKey());
        double tiMin = 0d;
        if(firstKey != null) {
            tiMin = firstKey.doubleValue();
        }
        double interval = this.interval;
        if (!hasInterval) {
            interval = initGroup(tiMin, tiMax);
        }
        int groupSize = (int) Math.ceil((tiMax - start) / interval);
        CubeLinkedHashMap resultMap = new CubeLinkedHashMap();
        CubeLinkedHashMap indexMap = new CubeLinkedHashMap();
        Iterator<Map.Entry<Number, GroupValueIndex>> it = baseMap.iterator();
        while (it.hasNext()) {
            Map.Entry<Number, GroupValueIndex> entry = it.next();
            Number k = entry.getKey();
            if(k == null) {
                continue;
            }
            double key = k.doubleValue();
            GroupValueIndex gvi = entry.getValue();
            int index = getAutoGroupIndex(key, interval, groupSize);
            if (indexMap.containsKey(index)){
                ((GroupValueIndex)indexMap.get(index)).or(gvi);
            } else {
                String groupName = getAutoGroupName(index, interval);
                GroupValueIndex g = GVIFactory.createAllEmptyIndexGVI();
                g.or(gvi);
                resultMap.put(groupName, g);
                indexMap.put(index, g);
            }
        }
        return resultMap;
    }

    private int getAutoGroupIndex(double value, double interval, int groupSize) {
        int index = (int) ((value - start) / interval);
        if(value == start + interval * groupSize){
            return index - 1;
        }
        return index;
    }

    private String getAutoGroupName(int index, double interval) {
        return nFormat.format(start + interval * index) + "-" + nFormat.format(start + interval * (index + 1));
    }

    @Override
    public boolean isNullGroup() {
        return false;
    }

    @Override
    public void parseJSON(JSONObject jo) throws Exception {
        super.parseJSON(jo);
        if(jo.has("group_value")){
            JSONObject valueJson = jo.optJSONObject("group_value");
            if (valueJson.has("max")) {
                max = valueJson.getDouble("max");
            }
            if (valueJson.has("min")) {
                min = valueJson.getDouble("min");
            }
            if (valueJson.has("group_interval")) {
                hasInterval = true;
                interval = valueJson.getDouble("group_interval");
            } else {
                hasInterval = false;
                interval = (max - min) / 5;
            }
            this.interval = initGroup(min, max);
        }
    }

    private double cutSmall(String val, int cutPosition) {
        val = val.substring(0, cutPosition);
        return Double.parseDouble(val);
    }

    private double cutBig(String val, int cutPosition){
        if(val.charAt(cutPosition) == '0'){
            return Double.parseDouble(val);
        }
        val = val.substring(0, cutPosition);
        int length = val.length() - 2;
        StringBuilder add = new StringBuilder("0.");
        while (--length > 0) {
            add.append("0");
        }
        add.append("1");
        if( ComparatorUtils.equals(val.charAt(cutPosition - 1), '.')){
            return new BigDecimal(val.substring(0, cutPosition - 1)).add(new BigDecimal("1")).doubleValue();
        }
        BigDecimal b1 = new BigDecimal(val);
        BigDecimal b2 = new BigDecimal(add.toString());
        return b1.add(b2).doubleValue();
    }

    private int checkMagnifyCount(double number) {
        String numText = GeneralUtils.objectToString(number);
        String dotText = numText.split("\\.")[0];
        return dotText.length();
    }

    private double initGroup(double minValue, double maxValue) {
        int magnify = 1;
        double minV = Math.abs(minValue);
        double maxV = Math.abs(maxValue);
        int minCount = this.checkMagnifyCount(minV);
        int maxCount = this.checkMagnifyCount(maxV);
        //缩小补零
        int count = minCount > maxCount ? minCount : maxCount;
        StringBuilder minBuilder = new StringBuilder();
        minBuilder.append("0.");
        while (count - minCount > 0) {
            minBuilder.append("0");
            minCount++;
        }
        String min = GeneralUtils.objectToString(minV);
        int minIndex = min.indexOf(".");
        min = minIndex == -1 ? min : (min.substring(minIndex).matches("\\.0+$") ? min.substring(0, minIndex) : min.replace(".", ""));
        minBuilder.append(min);

        StringBuilder maxBuilder = new StringBuilder("0.");
        while (count - maxCount > 0) {
            maxBuilder.append("0");
            maxCount++;
        }
        String max = GeneralUtils.objectToString(maxV);
        int maxIndex = max.indexOf(".");
        max = maxIndex == -1 ? max : (max.substring(maxIndex).matches("\\.0+$") ? max.substring(0, maxIndex) : max.replace(".", ""));
        maxBuilder.append(max);

        //后面补零对齐
        int zeros = maxBuilder.length() - minBuilder.length();
        if(zeros > 0){
            while (zeros-- > 0) {
                minBuilder.append("0");
            }
        }else{
            while (zeros++ < 0) {
                maxBuilder.append("0");
            }
        }
        min = minBuilder.toString();
        max = maxBuilder.toString();

        //截零
        int i = max.length() - 1;
        while (min.charAt(i) == '0' && max.charAt(i) == '0' && maxValue != 0 && minValue != 0) {
            i--;
        }

        //截位/截位+1
        while (count-- > 0) {
            magnify *= 10;
        }
        minV = minValue < 0 ? -(cutBig(min, i)) : cutSmall(min, i);
        maxV = maxValue < 0 ? -(cutSmall(max, i)) : cutBig(max, i);
        double genMin = minV * magnify;
        double genMax = maxV * magnify;
        this.start = genMin;
        if(!hasInterval){
            return Double.parseDouble(StableUtils.convertNumberStringToString((genMax - genMin) / 5));
        }else{
            return this.interval;
        }
    }
}