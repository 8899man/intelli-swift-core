package com.finebi.cube.structure.column;

import com.finebi.cube.data.ICubeResourceDiscovery;
import com.finebi.cube.exception.BIResourceInvalidException;
import com.finebi.cube.tools.BICubeResourceLocationTestTool;
import com.fr.bi.common.factory.BIFactoryHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * This class created on 2016/4/8.
 *
 * @author Connery
 * @since 4.0
 */
public class BICubeStringColumnTest extends BICubeColumnBasicTest<String> {
    @Override
    public ICubeColumnEntityService<String> getTestTarget() {
        return new BICubeStringColumn(BIFactoryHelper.getObject(ICubeResourceDiscovery.class),BICubeResourceLocationTestTool.getBasic("testStringCubeColumn"));
    }

    @Override
    public List<String> getListData() {
        List<String> lists = new ArrayList<String>();
        lists.add("b");
        lists.add("e");
        lists.add("d");
        lists.add("a");
        lists.add("f");
        lists.add("c");

        return lists;
    }

    @Override
    public void checkCubeColumnGroupSort(ICubeColumnEntityService<String> column) {
        assertEquals(column.getGroupObjectValue(0), "a");
        assertEquals(column.getGroupObjectValue(1), "b");
        assertEquals(column.getGroupObjectValue(2), "c");
        column.forceReleaseReader();
        column.forceReleaseWriter();
    }


    public void testGetGroupByPosition() {
        try {
            column.forceReleaseWriter();
            assertEquals(column.getPositionOfGroupByGroupValue("a"), 0);
            assertEquals(column.getPositionOfGroupByGroupValue("b"), 1);
            assertEquals(column.getPositionOfGroupByGroupValue("c"), 2);
            column.forceReleaseReader();
            column.forceReleaseWriter();
        } catch (BIResourceInvalidException e) {
            assertTrue(false);
        }

    }
}
