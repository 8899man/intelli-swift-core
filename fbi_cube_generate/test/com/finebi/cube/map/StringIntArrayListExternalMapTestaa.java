package com.finebi.cube.map;

import com.finebi.cube.map.map2.StringIntArrayListExternalMap;
import com.fr.bi.stable.operation.sort.comp.ComparatorFacotry;
import com.fr.stable.collections.array.IntArray;
import com.sap.conn.jco.support.Test;
import junit.framework.TestCase;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by wang on 2016/9/2.
 */
public class StringIntArrayListExternalMapTest extends TestCase {
    public void testStringIntArrayListExternalMap() {
        StringIntArrayListExternalMap map = new StringIntArrayListExternalMap(ComparatorFacotry.CHINESE_ASC, "test/String");
        for (int c = 1; c < 40000; c++) {
            IntArray list = new IntArray();
            for (int i = 3; i > 0; i--) {
                list.add(i * c);
                list.add(i * c + 4);
            }
            map.put(c + "haha", list);
        }

        Iterator<ExternalMap.Entry<String, IntArray>> it = map.getIterator();
        while (it.hasNext()) {
            Map.Entry<String, IntArray> entry = it.next();
            System.out.println(entry.getKey());
        }
        System.err.println(map.size());
    }
}
