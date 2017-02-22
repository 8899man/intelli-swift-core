package com.finebi.cube.structure.table.property;

import com.finebi.cube.ICubeConfiguration;
import com.finebi.cube.data.ICubeResourceDiscovery;
import com.finebi.cube.exception.BICubeResourceAbsentException;
import com.finebi.cube.exception.BIResourceInvalidException;
import com.finebi.cube.location.BICubeConfigurationTest;
import com.finebi.cube.location.BICubeResourceRetrieval;
import com.finebi.cube.location.ICubeResourceLocation;
import com.finebi.cube.location.ICubeResourceRetrievalService;
import com.finebi.cube.structure.BITableKey;
import com.finebi.cube.structure.ITableKey;
import com.finebi.cube.tools.BITableSourceTestTool;
import com.finebi.cube.tools.DBFieldTestTool;
import com.fr.bi.common.factory.BIFactoryHelper;
import com.fr.bi.stable.data.db.BICubeFieldSource;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.stable.utils.file.BIFileUtils;
import com.fr.bi.stable.utils.program.BINonValueUtils;
import com.fr.json.JSONObject;
import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class created on 2016/5/1.
 *
 * @author Connery
 * @since 4.0
 */
public class BICubeTablePropertyTest extends TestCase {
    private BICubeTableProperty property;
    private ICubeResourceRetrievalService retrievalService;
    private ICubeConfiguration cubeConfiguration;
    private ICubeResourceLocation location;


    public BICubeTablePropertyTest() {
        try {
            cubeConfiguration = new BICubeConfigurationTest();
            retrievalService = new BICubeResourceRetrieval(cubeConfiguration);
            location = retrievalService.retrieveResource(new BITableKey(BITableSourceTestTool.getDBTableSourceD()));
            property = new BICubeTableProperty(location, BIFactoryHelper.getObject(ICubeResourceDiscovery.class));
        } catch (BICubeResourceAbsentException e) {
            assertFalse(true);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ICubeResourceLocation location = retrievalService.retrieveResource(new BITableKey(BITableSourceTestTool.getDBTableSourceD()));
        File file = new File(location.getAbsolutePath());
        if (file.exists()) {
            BIFileUtils.delete(file);
        }

    }

    public void testFieldReadAndWrite() {
        List<ICubeFieldSource> tableFields = new ArrayList<ICubeFieldSource>();
        try {


            JSONObject jo1 = new JSONObject("{\"class_type\":5,\"is_enable\":true,\"field_size\":200,\"id\":\"客户ID\",\"table_id\":\"af33cab4\",\"is_usable\":true,\"field_type\":16,\"field_name\":\"客户ID\"}");
            JSONObject jo2 = new JSONObject("{\"class_type\":5,\"is_enable\":true,\"field_size\":200,\"id\":\"销售机会ID\",\"table_id\":\"af33cab4\",\"is_usable\":true,\"field_type\":16,\"field_name\":\"销售机会ID\"}");
            JSONObject jo3 = new JSONObject("{\"class_type\":5,\"is_enable\":true,\"field_size\":20,\"id\":\"合同类型\",\"table_id\":\"af33cab4\",\"is_usable\":true,\"field_type\":16,\"field_name\":\"合同类型\"}");

            BICubeFieldSource field1 = new BICubeFieldSource(null, null, 0, 0);
            BICubeFieldSource field2 = new BICubeFieldSource(null, null, 0, 0);
            BICubeFieldSource field3 = new BICubeFieldSource(null, null, 0, 0);
            field1.parseJSON(jo1);
            field2.parseJSON(jo2);
            field3.parseJSON(jo3);
            tableFields.add(field1);
            tableFields.add(field2);
            tableFields.add(field3);
            tableFields.add(field1);
            tableFields.add(field2);
            tableFields.add(field3);
        } catch (BIResourceInvalidException e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            BINonValueUtils.beyondControl(e.getMessage(), e);
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            BINonValueUtils.beyondControl(e.getMessage(), e);
        }

        property.recordTableStructure(tableFields);
        assertTrue(property.getFieldInfo().size()==6);
    }
    public void testRowCountWriteAvailable() {
        synchronized (this.getClass()) {
            try {
                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
                property.recordRowCount(10L);
                assertFalse(property.isRowCountReaderAvailable());
                assertTrue(property.isRowCountWriterAvailable());
            } catch (Exception e) {
                assertTrue(false);
            } finally {
                property.forceRelease();

                File file = new File(location.getAbsolutePath());
                if (file.exists()) {
                    BIFileUtils.delete(file);
                }
            }
        }

    }

    public void rowCountReadAvailable() {
        synchronized (this.getClass()) {
            try {
                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
                int count = 10;
                property.recordRowCount(count);
                assertFalse(property.isRowCountReaderAvailable());
                assertTrue(property.isRowCountWriterAvailable());
                property.forceReleaseWriter();
                assertEquals(property.getRowCount(), count);
                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
            } catch (Exception e) {
                assertFalse(true);
            } finally {
                property.forceRelease();

                File file = new File(location.getAbsolutePath());
                if (file.exists()) {
                    BIFileUtils.delete(file);
                }
            }
        }

    }

    public void testVersionAvailable() {
        synchronized (this.getClass()) {

            try {

                long version = 10;
                property.addVersion(version);
                assertEquals(version,property.getCubeVersion());

                version = 100;
                property.addVersion(version);
                assertEquals(version,property.getCubeVersion());

                version = 1;
                property.addVersion(version);
                assertEquals(version,property.getCubeVersion());
            } catch (Exception e) {
                assertFalse(true);
            } finally {
                property.forceRelease();

                File file = new File(location.getAbsolutePath());
                if (file.exists()) {
                    BIFileUtils.delete(file);
                }
            }
        }
    }

    public void testPropertyAvailable() {
        try {
            propertyAvailable();
            setUp();
            fieldInfoAvailable();
            setUp();

            lastTimeAvailable();
            setUp();


            rowCountReadAvailable();
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            assertTrue(false);
        }
    }

    public void lastTimeAvailable() {
        try {
            synchronized (this.getClass()) {
                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
                assertFalse(property.isLastExecuteTimeReaderAvailable());
                assertFalse(property.isLastExecuteTimeWriterAvailable());
                assertFalse(property.isCurrentExecuteTimeReaderAvailable());
                assertFalse(property.isCurrentExecuteTimeWriterAvailable());
                long time = System.currentTimeMillis();
                property.recordLastExecuteTime(time);
                property.recordCurrentExecuteTime();

                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
                assertFalse(property.isLastExecuteTimeReaderAvailable());
                assertTrue(property.isLastExecuteTimeWriterAvailable());
                assertFalse(property.isCurrentExecuteTimeReaderAvailable());
                assertTrue(property.isCurrentExecuteTimeWriterAvailable());

                property.forceReleaseWriter();
                assertEquals(property.getLastExecuteTime().getTime(), time);

                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
                assertFalse(property.isLastExecuteTimeWriterAvailable());
                assertTrue(property.isLastExecuteTimeReaderAvailable());
            }
        } catch (Exception e) {
            assertTrue(false);
        } finally {
            property.forceRelease();

            File file = new File(location.getAbsolutePath());
            if (file.exists()) {
                BIFileUtils.delete(file);
            }
        }
    }

    public void fieldInfoAvailable() {
        try {
            synchronized (this.getClass()) {

                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
                assertFalse(property.isLastExecuteTimeReaderAvailable());
                assertFalse(property.isLastExecuteTimeWriterAvailable());
                assertFalse(property.isFieldReaderAvailable());
                assertFalse(property.isFieldWriterAvailable());
                assertFalse(property.isFieldReaderAvailable());

                List<ICubeFieldSource> fields = new ArrayList<ICubeFieldSource>();
                fields.add(DBFieldTestTool.generateSTRING());
                property.recordTableStructure(fields);


                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
                assertFalse(property.isLastExecuteTimeReaderAvailable());
                assertFalse(property.isLastExecuteTimeWriterAvailable());
                assertFalse(property.isFieldReaderAvailable());
                assertTrue(property.isFieldWriterAvailable());
                List<ICubeFieldSource> fields1 = property.getFieldInfo();

                assertEquals(fields1.size(), 1);
                assertEquals(fields1.get(0), fields.get(0));

                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
                assertFalse(property.isLastExecuteTimeReaderAvailable());
                assertFalse(property.isLastExecuteTimeWriterAvailable());
                assertFalse(property.isFieldReaderAvailable());
                assertTrue(property.isFieldWriterAvailable());
            }
        } catch (Exception e) {
            assertTrue(false);
        } finally {
            property.forceRelease();

            File file = new File(location.getAbsolutePath());
            if (file.exists()) {
                BIFileUtils.delete(file);
            }
        }
        propertyAvailable();
    }

    public void testAddNullFieldsPropertyAvailable() {
        try {
            synchronized (this.getClass()) {
                setUp();
                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
                assertFalse(property.isLastExecuteTimeReaderAvailable());
                assertFalse(property.isLastExecuteTimeWriterAvailable());
                assertFalse(property.isFieldWriterAvailable());
                assertFalse(property.isFieldReaderAvailable());

                assertFalse(property.isPropertyExist());
                assertFalse(property.isFieldReaderAvailable());

                property.recordTableStructure(null);

                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
                assertFalse(property.isLastExecuteTimeReaderAvailable());
                assertFalse(property.isLastExecuteTimeWriterAvailable());
                assertFalse(property.isFieldReaderAvailable());
                assertTrue(property.isFieldWriterAvailable());
                property.recordCurrentExecuteTime();
                assertTrue(property.isPropertyExist());
            }

        } catch (Exception e) {
            assertTrue(false);
        } finally {
            property.forceRelease();
            File file = new File(location.getAbsolutePath());
            if (file.exists()) {
                BIFileUtils.delete(file);
            }
        }
    }

    public void propertyAvailable() {
        try {
            synchronized (this.getClass()) {
                setUp();
                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
                assertFalse(property.isLastExecuteTimeReaderAvailable());
                assertFalse(property.isLastExecuteTimeWriterAvailable());
                assertFalse(property.isFieldWriterAvailable());
                assertFalse(property.isFieldReaderAvailable());

                assertFalse(property.isPropertyExist());
                assertFalse(property.isFieldReaderAvailable());
                List<ICubeFieldSource> fields = new ArrayList<ICubeFieldSource>();
                fields.add(DBFieldTestTool.generateSTRING());
                property.recordTableStructure(fields);

                assertFalse(property.isRowCountReaderAvailable());
                assertFalse(property.isRowCountWriterAvailable());
                assertFalse(property.isLastExecuteTimeReaderAvailable());
                assertFalse(property.isLastExecuteTimeWriterAvailable());
                assertFalse(property.isFieldReaderAvailable());
                assertTrue(property.isFieldWriterAvailable());
                property.recordCurrentExecuteTime();
                assertTrue(property.isPropertyExist());
            }

        } catch (Exception e) {
            assertTrue(false);
        } finally {
            property.forceRelease();
            File file = new File(location.getAbsolutePath());
            if (file.exists()) {
                BIFileUtils.delete(file);
            }
        }
    }

    public void testPropertyParentTables() {
        try {
            synchronized (this.getClass()) {
                assertFalse(property.isParentReaderAvailable());
                assertFalse(property.isParentWriterAvailable());
                ITableKey tableKey = new BITableKey("abc");
                ITableKey tableKey2 = new BITableKey("dfg");
                List<ITableKey> parents = new ArrayList<ITableKey>();
                parents.add(tableKey);
                parents.add(tableKey2);
                property.recordParentsTable(parents);
                assertFalse(property.isParentReaderAvailable());
                assertTrue(property.isParentWriterAvailable());
                property.forceReleaseWriter();
                List<ITableKey> parentsTable = property.getParentsTable();
                assertEquals(tableKey, parentsTable.get(0));
                assertEquals(tableKey2, parentsTable.get(1));
            }

        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            assertTrue(false);
        } finally {
//            property.forceRelease();
            File file = new File(location.getAbsolutePath());
            if (file.exists()) {
                BIFileUtils.delete(file);
            }
        }
    }


}
