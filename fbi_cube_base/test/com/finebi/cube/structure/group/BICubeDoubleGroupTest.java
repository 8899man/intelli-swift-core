package com.finebi.cube.structure.group;

import com.finebi.cube.ICubeConfiguration;
import com.finebi.cube.data.ICubeResourceDiscovery;
import com.finebi.cube.exception.BICubeResourceAbsentException;
import com.finebi.cube.location.BICubeConfigurationTest;
import com.finebi.cube.location.BICubeResourceRetrieval;
import com.finebi.cube.location.ICubeResourceLocation;
import com.finebi.cube.location.ICubeResourceRetrievalService;
import com.finebi.cube.structure.BITableKey;
import com.finebi.cube.tools.BITableSourceTestTool;
import com.fr.bi.common.factory.BIFactoryHelper;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.stable.utils.file.BIFileUtils;
import junit.framework.TestCase;

import java.io.File;

/**
 * This class created on 2016/5/2.
 *
 * @author Connery
 * @since 4.0
 */
public class BICubeDoubleGroupTest extends TestCase {
    private BICubeDoubleGroupData groupData;
    private ICubeResourceRetrievalService retrievalService;
    private ICubeConfiguration cubeConfiguration;
    private ICubeResourceLocation location;


    public BICubeDoubleGroupTest() {
        try {
            cubeConfiguration = new BICubeConfigurationTest();
            retrievalService = new BICubeResourceRetrieval(cubeConfiguration);
            location = retrievalService.retrieveResource(new BITableKey(BITableSourceTestTool.getDBTableSourceD()));
            groupData = new BICubeDoubleGroupData(BIFactoryHelper.getObject(ICubeResourceDiscovery.class),location);
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


    public void testAvailable() {
        available(1, Double.valueOf("123"));
    }

    public void available(int position, Double value) {
        try {
            assertFalse(groupData.isGroupReaderAvailable());
            assertFalse(groupData.isGroupWriterAvailable());
            assertFalse(groupData.isLengthReaderAvailable());
            assertFalse(groupData.isLengthWriterAvailable());
            groupData.addGroupDataValue(position, value);
            assertFalse(groupData.isGroupReaderAvailable());
            assertTrue(groupData.isGroupWriterAvailable());
            assertFalse(groupData.isLengthReaderAvailable());
            assertFalse(groupData.isLengthWriterAvailable());
            assertEquals(value, groupData.getGroupValueByPosition(position));
            assertTrue(groupData.isGroupReaderAvailable());
            assertTrue(groupData.isGroupWriterAvailable());
            assertFalse(groupData.isLengthReaderAvailable());
            assertFalse(groupData.isLengthWriterAvailable());
            groupData.writeSizeOfGroup(10);
            assertTrue(groupData.isGroupReaderAvailable());
            assertTrue(groupData.isGroupWriterAvailable());
            assertFalse(groupData.isLengthReaderAvailable());
            assertTrue(groupData.isLengthWriterAvailable());
            assertEquals(10, groupData.sizeOfGroup());
            assertTrue(groupData.isGroupReaderAvailable());
            assertTrue(groupData.isGroupWriterAvailable());
            assertTrue(groupData.isLengthReaderAvailable());
            assertTrue(groupData.isLengthWriterAvailable());
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            assertTrue(false);
        }
    }

    public void testReset() {
        try {
            testAvailable();
            groupData.resetGroupWriter();
            assertTrue(groupData.isGroupReaderAvailable());
            assertFalse(groupData.isGroupWriterAvailable());
            assertTrue(groupData.isLengthReaderAvailable());
            assertTrue(groupData.isLengthWriterAvailable());
            groupData.resetGroupReader();
            assertFalse(groupData.isGroupReaderAvailable());
            assertFalse(groupData.isGroupWriterAvailable());
            assertTrue(groupData.isLengthReaderAvailable());
            assertTrue(groupData.isLengthWriterAvailable());
            groupData.resetLengthWriter();
            assertFalse(groupData.isGroupReaderAvailable());
            assertFalse(groupData.isGroupWriterAvailable());
            assertTrue(groupData.isLengthReaderAvailable());
            assertFalse(groupData.isLengthWriterAvailable());
            groupData.resetLengthReader();
            assertFalse(groupData.isGroupReaderAvailable());
            assertFalse(groupData.isGroupWriterAvailable());
            assertFalse(groupData.isLengthReaderAvailable());
            assertFalse(groupData.isLengthWriterAvailable());
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            assertTrue(false);
        }
    }

    public void testResetInitial() {
        try {
            testReset();
            available(1, Double.valueOf("32"));
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            assertTrue(false);
        }
    }
}
