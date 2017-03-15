package com.finebi.cube.structure;

import com.finebi.cube.BICubeTestBase;
import com.finebi.cube.structure.column.CubeColumnReaderService;
import com.finebi.cube.structure.column.date.BIDateColumnTool;
import com.finebi.cube.tools.DBFieldTestTool;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.finebi.cube.common.log.BILoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class created on 2016/4/6.
 *
 * @author Connery
 * @since 4.0
 */
public class BICubeTest extends BICubeTestBase {

    public void testMainData() {

        List<ICubeFieldSource> fields = new ArrayList<ICubeFieldSource>();
        fields.add(DBFieldTestTool.generateDATE());
        tableEntity.recordTableStructure(fields);
        assertFalse(tableEntity.tableDataAvailable());
    }

    public void testFieldData() {
        try {
            List<ICubeFieldSource> fields = new ArrayList<ICubeFieldSource>();

            fields.clear();
            fields.add(DBFieldTestTool.generateDATE());
            fields.add(DBFieldTestTool.generateSTRING());
            fields.add(DBFieldTestTool.generateBOOLEAN());
            fields.add(DBFieldTestTool.generateDECIMAL());
            fields.add(DBFieldTestTool.generateDOUBLE());
            fields.add(DBFieldTestTool.generateFLOAT());
            fields.add(DBFieldTestTool.generateINTEGER());
            fields.add(DBFieldTestTool.generateLONG());
            fields.add(DBFieldTestTool.generateTIME());

            tableEntity.recordTableStructure(fields);
            CubeColumnReaderService readerService = tableEntity.getColumnDataGetter(BIDateColumnTool.generateYearMonthDay(DBFieldTestTool.generateTIME()));

            assertFalse(tableEntity.tableDataAvailable());
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            assertTrue(false);
        }
    }

    public void testCubeVersion() {
        long time = System.currentTimeMillis();
        cube.addVersion(time);
        assertEquals(time, cube.getCubeVersion());
    }
}
