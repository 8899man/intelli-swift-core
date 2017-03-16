package com.fr.bi.stable.utils;

import com.finebi.cube.api.ICubeColumnIndexReader;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.report.result.TargetCalculator;
import com.fr.general.ComparatorUtils;
import com.fr.third.org.apache.poi.util.StringUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by 小灰灰 on 2015/10/16.
 */
public class BICollectionUtils {

    public static Map mergeMapByKeyMapValue(Map key, Map value) {
        Map merge = new HashMap();
        if (key == null) {
            return value;
        }
        if (value == null) {
            return new HashMap();
        }
        Iterator<Map.Entry> it = key.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = it.next();
            Object ob;
            if (entry.getValue() instanceof TargetCalculator) {
                ob = value.get(((TargetCalculator) entry.getValue()).createTargetGettingKey());
            } else {
                ob = value.get(entry.getValue());
            }

            if (ob != null) {
                merge.put(entry.getKey(), ob);
            }
        }
        return merge;
    }

    /**
     * 从src合并到dest map
     *
     * @param dest
     * @param src
     * @param <T>
     * @param <V>
     */
    public static <T extends Object, V extends Set> void mergeSetValueMap(Map<T, V> dest, Map<T, V> src) {
        Iterator<Map.Entry<T, V>> it = src.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<T, V> entry = it.next();
            V set = dest.get(entry.getKey());
            if (set != null) {
                set.addAll(entry.getValue());
            } else {
                dest.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public static <T> T firstUnNullKey(ICubeColumnIndexReader<T> baseMap) {
        T firstKey = baseMap.firstKey();
        if (firstKey == null || ComparatorUtils.equals(firstKey, "")) {
            Iterator<Map.Entry<T, GroupValueIndex>> iter = baseMap.iterator(firstKey);
            while (iter.hasNext()) {
                Map.Entry<T, GroupValueIndex> entry = iter.next();
                firstKey = entry.getKey();
                if (firstKey != null && firstKey != "") {
                    break;
                }
            }
        }
        return firstKey;
    }

    public static <T> T lastUnNullKey(ICubeColumnIndexReader<T> baseMap) {
        T lastKey = baseMap.lastKey();
        if (lastKey == null || ComparatorUtils.equals(lastKey, "")) {
            Iterator<Map.Entry<T, GroupValueIndex>> iter = baseMap.previousIterator(lastKey);
            while (iter.hasNext()) {
                Map.Entry<T, GroupValueIndex> entry = iter.next();
                lastKey = entry.getKey();
                if (lastKey != null && lastKey != "") {
                    break;
                }
            }
        }
        return lastKey;
    }

}