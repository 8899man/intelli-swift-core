package com.finebi.integration.cube.data.reader;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.data.ICubePrimitiveResourceDiscovery;
import com.finebi.cube.data.disk.BICubeDiskPrimitiveDiscovery;
import com.finebi.cube.data.disk.reader.primitive.BIByteNIOReader;
import com.finebi.cube.data.disk.writer.primitive.BIByteNIOWriter;
import com.finebi.cube.location.ICubeResourceLocation;
import com.finebi.cube.provider.BICubeLocationProvider;
import com.finebi.cube.provider.BIProjectPathProvider;
import junit.framework.TestCase;

/**
 * This class created on 2016/6/3.
 *
 * @author Connery
 * @since 4.0
 */
public class BIPrimitiveDataCacheTest extends TestCase {
    private ICubePrimitiveResourceDiscovery discovery;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        discovery = BICubeDiskPrimitiveDiscovery.getInstance();
    }

    public void testWriteReaderRelease() {
        try {
            ICubeResourceLocation location = BICubeLocationProvider.buildWrite(BIProjectPathProvider.projectPath, "writer");
            location.setByteType();
            BIByteNIOWriter writer = (BIByteNIOWriter) discovery.getCubeWriter(location);
            writer.recordSpecificPositionValue(0l, Byte.valueOf("35"));
            writer.recordSpecificPositionValue(1l, Byte.valueOf("35"));

            location.setReaderSourceLocation();
            BIByteNIOReader reader = (BIByteNIOReader) discovery.getCubeReader(location);
            assertEquals(reader.getSpecificValue(0l), Byte.valueOf("35").byteValue());
            assertEquals(reader.getSpecificValue(1l), Byte.valueOf("35").byteValue());

            location.setWriterSourceLocation();
            BIByteNIOWriter writer_copy = (BIByteNIOWriter) discovery.getCubeWriter(location);
            location.setReaderSourceLocation();

            BIByteNIOReader reader_copy = (BIByteNIOReader) discovery.getCubeReader(location);
            assertEquals(writer.getWriterHandler(), writer_copy.getWriterHandler());
            assertEquals(reader.getReaderHandler(), reader_copy.getReaderHandler());
            reader.releaseHandler("handlerKeyR");
            assertTrue(reader_copy.canReader());
            reader.forceRelease();
            assertFalse(reader_copy.canReader());
            writer.releaseHandler("handlerKeyW");
            assertTrue(writer_copy.canWriter());
            writer.forceRelease();
            assertFalse(writer_copy.canWriter());

        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
    }


    public void testCacheRelease() {
        try {
            ICubeResourceLocation location = BICubeLocationProvider.buildWrite(BIProjectPathProvider.projectPath, "writer");
            location.setByteType();
            BIByteNIOWriter writer = (BIByteNIOWriter) discovery.getCubeWriter(location);
            writer.recordSpecificPositionValue(0l, Byte.valueOf("35"));

            location.setReaderSourceLocation();
            BIByteNIOReader reader = (BIByteNIOReader) discovery.getCubeReader(location);
            assertEquals(reader.getSpecificValue(0l), Byte.valueOf("35").byteValue());
            assertTrue(writer.canWriter());
            assertTrue(reader.canReader());
            BICubeDiskPrimitiveDiscovery.getInstance().forceRelease();
            assertFalse(writer.canWriter());
            assertFalse(reader.canReader());

        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
    }
}
