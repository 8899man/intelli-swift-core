package com.finebi.cube.conf.relation.path;


import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.relation.BITableRelationTestTool;
import com.finebi.cube.conf.relation.BITableTestTool;
import com.finebi.cube.conf.relation.BIUserTableRelationManager;
import com.finebi.cube.conf.relation.BIUserTableRelationManagerTestTool;
import com.finebi.cube.relation.BITablePair;
import junit.framework.TestCase;

/**
 * Created by Connery on 2016/1/14.
 */
public class BICommonSeniorTableTest extends TestCase {
    private BIUserTableRelationManager tableRelationManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tableRelationManager = BIUserTableRelationManagerTestTool.generateUserTableRelationManager();
    }

    public void testOneCommonSenior() {
        try {
            tableRelationManager.registerTableRelation(BITableRelationTestTool.getAaBa());
            tableRelationManager.registerTableRelation(BITableRelationTestTool.getAaCa());
            BITableContainer tableContainer = tableRelationManager.getCommonSeniorTables(new BITablePair(BITableTestTool.getB(), BITableTestTool.getC()));
            assertTrue(tableContainer.contain(BITableTestTool.getA()));
            assertFalse(tableContainer.contain(BITableTestTool.getB()));
            assertFalse(tableContainer.contain(BITableTestTool.getC()));

        } catch (Exception e) {
            BILoggerFactory.getLogger().error("", e);
            assertTrue(false);
        }
    }

    public void testTwoCommonSenior() {
        try {
            tableRelationManager.registerTableRelation(BITableRelationTestTool.getDaAa());
            tableRelationManager.registerTableRelation(BITableRelationTestTool.getAaBa());
            tableRelationManager.registerTableRelation(BITableRelationTestTool.getAaCa());
            BITableContainer tableContainer = tableRelationManager.getCommonSeniorTables(new BITablePair(BITableTestTool.getB(), BITableTestTool.getC()));
            assertTrue(tableContainer.contain(BITableTestTool.getA()));
            assertFalse(tableContainer.contain(BITableTestTool.getB()));
            assertFalse(tableContainer.contain(BITableTestTool.getC()));
            assertTrue(tableContainer.contain(BITableTestTool.getD()));


        } catch (Exception e) {
            BILoggerFactory.getLogger().error("", e);
            assertTrue(false);
        }
    }
}