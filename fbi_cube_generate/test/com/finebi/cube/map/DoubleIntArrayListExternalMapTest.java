package com.finebi.cube.map;

import com.finebi.cube.map.map2.DoubleIntArrayListExternalMap;
import com.fr.bi.stable.operation.sort.comp.ComparatorFacotry;
import com.fr.stable.collections.array.IntArray;
import junit.framework.TestCase;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by wang on 2016/9/2.
 */
public class DoubleIntArrayListExternalMapTest extends TestCase {
    public void testDoubleIntArrayListExternalMap() {
        DoubleIntArrayListExternalMap map = new DoubleIntArrayListExternalMap(ComparatorFacotry.DOUBLE_DESC, "test/Double");
        for (int c = 1; c < 40000; c++) {
            IntArray list = new IntArray();
            for (int i = 3; i > 0; i--) {
                list.add(i * c);
                list.add(i * c + 4);
            }
            map.put(c * 1.1, list);
        }

        Iterator<ExternalMap.Entry<Double, IntArray>> it = map.getIterator();
        while (it.hasNext()) {
            Map.Entry<Double, IntArray> entry = it.next();
            System.out.println(entry.getKey());
        }
        System.err.println(map.size());

        map.clear();
    }
}
