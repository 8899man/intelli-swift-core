package com.finebi.cube.map;

import com.finebi.cube.map.map2.LongIntArrayListExternalMap;
import com.fr.bi.stable.operation.sort.comp.ComparatorFacotry;
import com.fr.stable.collections.array.IntArray;
import junit.framework.TestCase;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by wang on 2016/9/2.
 */
public class LongIntArrayListExternalMapTestaa extends TestCase {
    public void testLongIntArrayListExternalMap() {
        LongIntArrayListExternalMap map = new LongIntArrayListExternalMap(ComparatorFacotry.LONG_ASC, "test/Long");
        for (long c = 1; c < 40000; c++) {
            IntArray list = new IntArray();
            for (int i = 3; i > 0; i--) {
                list.add((int) (i * c));
                list.add((int) (i * c + 4));
            }
            map.put(c, list);
        }

        Iterator<ExternalMap.Entry<Long, IntArray>> it = map.getIterator();
        while (it.hasNext()) {
            Map.Entry<Long, IntArray> entry = it.next();
            System.out.println(entry.getKey());
        }
        System.err.println(map.size());
    }
}
