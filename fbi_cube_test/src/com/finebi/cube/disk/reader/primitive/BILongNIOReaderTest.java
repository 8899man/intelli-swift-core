package com.finebi.cube.disk.reader.primitive;

import com.finebi.cube.data.disk.reader.primitive.BILongNIOReader;
import com.finebi.cube.data.disk.writer.primitive.BILongNIOWriter;
import com.finebi.cube.common.log.BILoggerFactory;
import junit.framework.TestCase;

import java.io.File;

/**
 * This class created on 2016/4/1.
 *
 * @author Connery
 * @since 4.0
 */
public class BILongNIOReaderTest extends TestCase {
    public void testSpeed() {
        try {
            BILongNIOWriter writer = new BILongNIOWriter(BIByteNIOReaderTest.NIO_PATH_TEST);
            BILongNIOReader reader = new BILongNIOReader(new File(BIByteNIOReaderTest.NIO_PATH_TEST));

            writer.recordSpecificPositionValue(0, Long.valueOf(1));
            long start = System.currentTimeMillis();
            int sum = 0;
            int count = 1000000;
            for (int i = 0; i < count; i++) {
                sum += reader.getSpecificValue(0);
            }
            System.out.println("time:" + (System.currentTimeMillis() - start));
            assertEquals(sum, count);
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            assertTrue(false);
        }
    }

    public void testBasic() {
        try {
            BILongNIOWriter writer = new BILongNIOWriter(BIByteNIOReaderTest.NIO_PATH_TEST);
            BILongNIOReader reader = new BILongNIOReader(new File(BIByteNIOReaderTest.NIO_PATH_TEST));

            writer.recordSpecificPositionValue(0, Long.valueOf(1));
            writer.recordSpecificPositionValue(2, Long.valueOf(1));
            writer.recordSpecificPositionValue(3, Long.valueOf(1));
            assertEquals(reader.getSpecificValue(0l), 1);
            assertEquals(reader.getSpecificValue(2l), 1);
            assertEquals(reader.getSpecificValue(3l), 1);

        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            assertTrue(false);
        }
    }   public void testLarge() {
        try {
            BILongNIOWriter writer = new BILongNIOWriter(BIByteNIOReaderTest.NIO_PATH_TEST);
            BILongNIOReader reader = new BILongNIOReader(new File(BIByteNIOReaderTest.NIO_PATH_TEST));

            writer.recordSpecificPositionValue(0, Long.valueOf(1));
            writer.recordSpecificPositionValue(2, Long.valueOf(1));
            writer.recordSpecificPositionValue(2000000000, Long.valueOf(1));
            assertEquals(reader.getSpecificValue(0l), 1);
            assertEquals(reader.getSpecificValue(2l), 1);
            assertEquals(reader.getSpecificValue(2000000000), 1);

        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
            assertTrue(false);
        }
    }

    public void testWriteNegativeValue() {
        try {
            BILongNIOWriter writer = new BILongNIOWriter(BIByteNIOReaderTest.NIO_PATH_TEST);
            writer.recordSpecificPositionValue(-1, Long.valueOf(1));
        } catch (Exception e) {
            return;
        }
        assertTrue(false);
    }

    public void testReadNegativeValue() {
        try {
            BILongNIOReader reader = new BILongNIOReader(new File(BIByteNIOReaderTest.NIO_PATH_TEST));
            reader.getSpecificValue(-1l);
        } catch (Exception e) {
            return;
        }
        assertTrue(false);
    }
}
