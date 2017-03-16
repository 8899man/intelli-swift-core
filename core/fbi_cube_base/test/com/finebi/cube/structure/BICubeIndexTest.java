package com.finebi.cube.structure;

import com.finebi.cube.ICubeConfiguration;
import com.finebi.cube.data.ICubeResourceDiscovery;
import com.finebi.cube.exception.BICubeIndexException;
import com.finebi.cube.exception.BICubeResourceAbsentException;
import com.finebi.cube.tools.BICubeConfigurationTool;
import com.finebi.cube.location.BICubeResourceRetrieval;
import com.finebi.cube.location.ICubeResourceLocation;
import com.finebi.cube.location.ICubeResourceRetrievalService;
import com.finebi.cube.tools.BICubeResourceLocationTestTool;
import com.finebi.cube.tools.BITableSourceTestTool;
import com.finebi.cube.tools.GroupValueIndexTestTool;
import com.fr.bi.common.factory.BIFactoryHelper;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.stable.utils.file.BIFileUtils;
import junit.framework.TestCase;

import java.io.File;

/**
 * This class created on 2016/3/29.
 *
 * @author Connery
 * @since 4.0
 */
public class BICubeIndexTest extends TestCase {
    private BICubeIndexData indexData;
    private ICubeResourceRetrievalService retrievalService;
    private ICubeConfiguration cubeConfiguration;
    private ICubeResourceLocation location;

    @Override
    protected void setUp() throws Exception {

        try {
            cubeConfiguration = new BICubeConfigurationTool();
            retrievalService = new BICubeResourceRetrieval(cubeConfiguration);
            location = retrievalService.retrieveResource(new BITableKey(BITableSourceTestTool.getDBTableSourceD()));
            indexData = new BICubeIndexData(BIFactoryHelper.getObject(ICubeResourceDiscovery.class), location);
        } catch (BICubeResourceAbsentException e) {
            assertFalse(true);
        }
        super.setUp();
        ICubeResourceLocation location = retrievalService.retrieveResource(new BITableKey(BITableSourceTestTool.getDBTableSourceD()));
        File file = new File(location.getAbsolutePath());
        if (file.exists()) {
            BIFileUtils.delete(file);
        }
    }

    public void testIndex() {
        try {
            BICubeIndexData column = new BICubeIndexData(BIFactoryHelper.getObject(ICubeResourceDiscovery.class),BICubeResourceLocationTestTool.getBasic("testIndex"));
            column.addIndex(0, GroupValueIndexTestTool.generateSampleIndex());
            column.forceReleaseWriter();
            assertEquals(GroupValueIndexTestTool.generateSampleIndex(), column.getBitmapIndex(0));
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            assertFalse(true);
        }
    }

    public void testNullIndex() {
        try {
            BICubeIndexData column = new BICubeIndexData(BIFactoryHelper.getObject(ICubeResourceDiscovery.class),BICubeResourceLocationTestTool.getBasic("testNullIndex"));
            column.addNULLIndex(0, GroupValueIndexTestTool.generateSampleIndex());
            column.forceReleaseWriter();
            assertEquals(GroupValueIndexTestTool.generateSampleIndex(), column.getNULLIndex(0));
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            assertFalse(true);
        }
    }

    public void testAvailable() {
        try {
            assertFalse(indexData.isIndexReaderAvailable());
            assertFalse(indexData.isIndexWriterAvailable());
            assertFalse(indexData.isNullReaderAvailable());
            assertFalse(indexData.isNullWriterAvailable());
            indexData.addIndex(0, GroupValueIndexTestTool.generateSampleIndex());
            assertFalse(indexData.isIndexReaderAvailable());
            assertTrue(indexData.isIndexWriterAvailable());
            assertFalse(indexData.isNullReaderAvailable());
            assertFalse(indexData.isNullWriterAvailable());
            indexData.forceReleaseWriter();
            assertEquals(GroupValueIndexTestTool.generateSampleIndex(), indexData.getBitmapIndex(0));
            assertTrue(indexData.isIndexReaderAvailable());
            assertFalse(indexData.isIndexWriterAvailable());
            assertFalse(indexData.isNullReaderAvailable());
            assertFalse(indexData.isNullWriterAvailable());
            indexData.addNULLIndex(0, GroupValueIndexTestTool.generateSampleIndex());
            assertTrue(indexData.isIndexReaderAvailable());
            assertFalse(indexData.isIndexWriterAvailable());
            assertFalse(indexData.isNullReaderAvailable());
            assertTrue(indexData.isNullWriterAvailable());
            indexData.forceReleaseWriter();
            assertEquals(GroupValueIndexTestTool.generateSampleIndex(), indexData.getNULLIndex(0));
            assertTrue(indexData.isIndexReaderAvailable());
            assertFalse(indexData.isIndexWriterAvailable());
            assertTrue(indexData.isNullReaderAvailable());
            assertFalse(indexData.isNullWriterAvailable());
        } catch (BICubeIndexException e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            assertFalse(true);
        }

    }
}
