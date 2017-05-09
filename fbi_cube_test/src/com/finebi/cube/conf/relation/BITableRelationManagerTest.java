package com.finebi.cube.conf.relation;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.conf.BISystemPackageConfigurationProvider;
import com.finebi.cube.conf.BITableRelationConfigurationProvider;
import com.finebi.cube.conf.pack.imp.BISystemPackageConfigurationManager;
import com.finebi.cube.relation.BITableRelation;
import com.finebi.cube.relation.BITableRelationPath;
import com.finebi.cube.relation.BITableSourceRelation;
import com.finebi.cube.tools.BISystemTableRelationManagerTestTool;
import com.finebi.cube.tools.BITableRelationTestTool;
import com.finebi.cube.tools.BITableTestTool;
import com.fr.bi.base.BIUser;
import com.fr.bi.stable.exception.BIRelationDuplicateException;
import com.fr.stable.bridge.StableFactory;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Connery on 2016/1/28.
 */
public class BITableRelationManagerTest extends TestCase {

    private BISystemTableRelationManager manager;
    private BIUser user = new BIUser(999);

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        manager = new BISystemTableRelationManagerTestTool();
    }

    public void testRemoveRelationship() {
        try {
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getBaCa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getBaEa());

            assertTrue(manager.containTableRelationship(user.getUserId(), BITableRelationTestTool.getAaBa()));
            assertTrue(manager.containTableRelationship(user.getUserId(), BITableRelationTestTool.getBaCa()));
            assertTrue(manager.containTableRelationship(user.getUserId(), BITableRelationTestTool.getBaEa()));

            manager.removeTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());

            assertFalse(manager.containTableRelationship(user.getUserId(), BITableRelationTestTool.getAaBa()));
            assertTrue(manager.containTableRelationship(user.getUserId(), BITableRelationTestTool.getBaCa()));
            assertTrue(manager.containTableRelationship(user.getUserId(), BITableRelationTestTool.getBaEa()));
            Set<BITableRelation> relations = manager.getAllTableRelation(user.getUserId());
            assertEquals(relations.size(), 2);
            boolean caught = false;
            try {
                manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getBaEa());

            } catch (BIRelationDuplicateException ignore) {
                caught = true;
            }
            assertTrue(caught);
        } catch (Exception ignore) {
            assertTrue(false);
        }
    }

    public void testAddRelation() {
        try {
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            assertTrue(manager.containTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa()));
            boolean caught = false;
            try {
                manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());

            } catch (BIRelationDuplicateException ignore) {
                caught = true;
            }
            assertTrue(caught);
        } catch (Exception ignore) {
            assertTrue(false);
        }
    }

    public void testAddTwoRelation() {
        try {
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getBaCa());

            assertTrue(manager.containTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa()));
            Set<BITableRelation> relations = manager.getAllTableRelation(user.getUserId());
            assertEquals(relations.size(), 2);
            boolean caught = false;
            try {
                manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());

            } catch (BIRelationDuplicateException ignore) {
                caught = true;
            }
            assertTrue(caught);
        } catch (Exception ignore) {
            assertTrue(false);
        }
    }

    public void testRemoveRelation() {
        try {
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getDaAa());
            assertTrue(manager.containTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa()));
            assertTrue(manager.containTableRelation(user.getUserId(), BITableRelationTestTool.getDaAa()));

            manager.removeTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            assertFalse(manager.containTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa()));
            assertTrue(manager.containTableRelation(user.getUserId(), BITableRelationTestTool.getDaAa()));


        } catch (Exception ignore) {
            assertTrue(false);
        }
    }

    public void testGetAllRelation() {
        try {
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getDaAa());
            Set<BITableRelation> relationSet = manager.getAllTableRelation(user.getUserId());

            assertTrue(relationSet.contains(BITableRelationTestTool.getAaBa()));
            assertTrue(relationSet.contains(BITableRelationTestTool.getDaAa()));

        } catch (Exception ignore) {
            assertTrue(false);
        }
    }

    public void testRelationTableLostName() {
        try {
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            Set<BITableRelation> relationSet = manager.getAllTableRelation(user.getUserId());
            assertTrue(relationSet.contains(BITableRelationTestTool.getAaBa()));
            BITableRelation AaBa = (BITableRelation) relationSet.toArray()[0];
            assertEquals(AaBa.getPrimaryTable().getTableName(), BITableRelationTestTool.getAaBa().getPrimaryTable().getTableName());
            assertEquals(AaBa.getPrimaryTable().getTableName(), "A_table_Name");
        } catch (Exception ignore) {
            assertTrue(false);
        }
    }

    public void testGetBasicAllAvailablePath() {
        try {
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getDaAa());
            Set<BITableRelationPath> relationSet = manager.getAllAvailablePath(user.getUserId()
                    , BITableRelationTestTool.getAaBa().getForeignTable()
                    , BITableRelationTestTool.getAaBa().getPrimaryTable()
            );
            Iterator<BITableRelationPath> pathIterator = relationSet.iterator();
            assertTrue(pathIterator.hasNext());
            while (pathIterator.hasNext()) {
                BITableRelationPath path = pathIterator.next();
                assertEquals(path.getFirstRelation(), BITableRelationTestTool.getAaBa());
                assertEquals(path.getLastRelation(), BITableRelationTestTool.getAaBa());
            }
            Set<BITableRelationPath> relationSet2 = manager.getAllAvailablePath(user.getUserId()
                    , BITableTestTool.getB()
                    , BITableTestTool.getD()
            );
            Iterator<BITableRelationPath> pathIterator2 = relationSet2.iterator();
            assertTrue(pathIterator2.hasNext());
            while (pathIterator2.hasNext()) {
                BITableRelationPath path = pathIterator2.next();
                assertEquals(path.getFirstRelation(), BITableRelationTestTool.getDaAa());
                assertEquals(path.getLastRelation(), BITableRelationTestTool.getAaBa());
            }
        } catch (Exception ignore) {
            BILoggerFactory.getLogger().error(ignore.getMessage(), ignore);
            assertTrue(false);
        }
    }

    public void testGetAllPath() {
        try {
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getDaAa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getCaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getDaCa());

            Set<BITableRelationPath> relationSet = manager.getAllPath(user.getUserId()
                    , BITableTestTool.getB()
                    , BITableTestTool.getD()
            );
            assertEquals(relationSet.size(), 2);
            BITableRelationPath D_A_B = new BITableRelationPath();
            D_A_B.addRelationAtHead(BITableRelationTestTool.getAaBa());
            D_A_B.addRelationAtHead(BITableRelationTestTool.getDaAa());
            assertTrue(relationSet.contains(D_A_B));

            BITableRelationPath D_C_B = new BITableRelationPath();
            D_C_B.addRelationAtHead(BITableRelationTestTool.getCaBa());
            D_C_B.addRelationAtHead(BITableRelationTestTool.getDaCa());
            assertTrue(relationSet.contains(D_C_B));

        } catch (Exception ignore) {
            BILoggerFactory.getLogger().error(ignore.getMessage(), ignore);
            assertTrue(false);
        }
    }

    public void testAddDisablePath() {
        try {
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getDaAa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getCaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getDaCa());
            Set<BITableRelationPath> relationSet = manager.getAllPath(user.getUserId()
                    , BITableTestTool.getB()
                    , BITableTestTool.getD()
            );
            assertEquals(relationSet.size(), 2);
            BITableRelationPath D_A_B = new BITableRelationPath();
            D_A_B.addRelationAtHead(BITableRelationTestTool.getAaBa());
            D_A_B.addRelationAtHead(BITableRelationTestTool.getDaAa());
            assertTrue(relationSet.contains(D_A_B));

            BITableRelationPath D_C_B = new BITableRelationPath();
            D_C_B.addRelationAtHead(BITableRelationTestTool.getCaBa());
            D_C_B.addRelationAtHead(BITableRelationTestTool.getDaCa());
            assertTrue(relationSet.contains(D_C_B));
            /**
             * 禁用路径DAB
             */
            manager.addDisableRelations(user.getUserId(), D_A_B);
            relationSet = manager.getAllAvailablePath(user.getUserId()
                    , BITableTestTool.getB()
                    , BITableTestTool.getD()
            );
            /**
             * 可用路径不包含DAB
             */
            assertEquals(relationSet.size(), 1);
            assertFalse(relationSet.contains(D_A_B));
            assertTrue(relationSet.contains(D_C_B));
            /**
             * 全部路径仍然应该包含DAB
             */
            relationSet = manager.getAllPath(user.getUserId()
                    , BITableTestTool.getB()
                    , BITableTestTool.getD()
            );
            assertEquals(relationSet.size(), 2);
            assertTrue(relationSet.contains(D_A_B));
            assertTrue(relationSet.contains(D_C_B));
        } catch (Exception ignore) {
            BILoggerFactory.getLogger().error(ignore.getMessage(), ignore);
            assertTrue(false);
        }
    }

    /*设置ABC，disable掉AB，查看AC是否可用*/
    public void testAddDisablePathByPart() {
        try {
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getBaCa());
            Set<BITableRelationPath> relationSet = manager.getAllPath(user.getUserId()
                    , BITableTestTool.getC()
                    , BITableTestTool.getA()
            );
            assertEquals(relationSet.size(), 1);
            BITableRelationPath A_B = new BITableRelationPath();
            A_B.addRelationAtHead(BITableRelationTestTool.getAaBa());
            /**
             * 禁用路径AB
             */
            manager.addDisableRelations(user.getUserId(), A_B);
            relationSet = manager.getAllAvailablePath(user.getUserId()
                    , BITableTestTool.getB()
                    , BITableTestTool.getA()
            );
            /**
             * 可用路径不包含AB
             */
            assertEquals(relationSet.size(), 0);
            /**
             * 全部路径应该包含AC
             */
            relationSet = manager.getAllPath(user.getUserId()
                    , BITableTestTool.getC()
                    , BITableTestTool.getA()
            );
            assertEquals(relationSet.size(), 1);
            relationSet = manager.getAllAvailablePath(user.getUserId()
                    , BITableTestTool.getC()
                    , BITableTestTool.getA()
            );
            /**
             * 可用路径也不应该包含AC
             */
            assertEquals(relationSet.size(), 0);
        } catch (Exception ignore) {
            BILoggerFactory.getLogger().error(ignore.getMessage(), ignore);
            assertTrue(false);
        }
    }

    //设置ABCD,禁用ABC,AB可用， ABC和ABCD不可用
    public void testAddDisablePathByPart2() {
        try {
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getBaCa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getCaDa());

            BITableRelationPath A_B_C = new BITableRelationPath();
            A_B_C.addRelationAtHead(BITableRelationTestTool.getAaBa());
            A_B_C.addRelationAtTail(BITableRelationTestTool.getBaCa());
            manager.addDisableRelations(user.getUserId(), A_B_C);

            Set<BITableRelationPath> relationSet = manager.getAllAvailablePath(user.getUserId()
                    , BITableTestTool.getB()
                    , BITableTestTool.getA()
            );
            assertEquals(relationSet.size(), 1);

            relationSet = manager.getAllAvailablePath(user.getUserId()
                    , BITableTestTool.getC()
                    , BITableTestTool.getA()
            );
            assertEquals(relationSet.size(), 0);

            relationSet = manager.getAllAvailablePath(user.getUserId()
                    , BITableTestTool.getD()
                    , BITableTestTool.getA()
            );
            assertEquals(relationSet.size(), 0);

        } catch (Exception ignore) {
            BILoggerFactory.getLogger().error(ignore.getMessage(), ignore);
            assertTrue(false);
        }
    }

    public void testGetAllTablesPath() {
        try {
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getAaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getDaAa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getCaBa());
            manager.registerTableRelation(user.getUserId(), BITableRelationTestTool.getDaCa());
            Set<BITableRelationPath> relationSet = manager.getAllTablePath(user.getUserId());
            assertEquals(relationSet.size(), 6);


        } catch (Exception ignore) {
            BILoggerFactory.getLogger().error(ignore.getMessage(), ignore);
            assertTrue(false);
        }
    }

    /**
     * relation是否已经生成过cube
     */
    public void testIsRelationGenerated() {
        try {
            StableFactory.registerMarkedObject(BISystemPackageConfigurationProvider.XML_TAG, new BISystemPackageConfigurationManager());
            StableFactory.registerMarkedObject(BITableRelationConfigurationProvider.XML_TAG, new BISystemTableRelationManager());
            BITableRelation aaBa = BITableRelationTestTool.getAaBa();
            BITableRelation aaCa = BITableRelationTestTool.getAaCa();
            manager.registerTableRelation(user.getUserId(), aaBa);
            manager.registerTableRelation(user.getUserId(), aaCa);
            assertFalse(manager.isRelationGenerated(user.getUserId(), aaBa));
            assertFalse(manager.isRelationGenerated(user.getUserId(), aaCa));
            manager.finishGenerateCubes(user.getUserId(), new HashSet<BITableSourceRelation>());
            assertTrue(manager.isRelationGenerated(user.getUserId(), aaBa));
            assertTrue(manager.isRelationGenerated(user.getUserId(), aaCa));
        } catch (Exception ignore) {
            assertTrue(false);
        }
    }
}