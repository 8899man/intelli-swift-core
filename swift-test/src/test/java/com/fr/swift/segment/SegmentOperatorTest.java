package com.fr.swift.segment;

import com.fr.swift.context.SwiftContext;
import com.fr.swift.segment.operator.Inserter;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.SourceKey;
import com.fr.swift.source.SwiftMetaData;
import com.fr.swift.source.SwiftResultSet;
import com.fr.swift.source.core.Core;
import com.fr.swift.test.Preparer;
import com.fr.swift.test.TestResource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * @author yee
 * @date 2018/1/9
 */
public class SegmentOperatorTest {
    private SourceKey intKey;
    private SourceKey longKey;
    private SourceKey doubleKey;
    private SourceKey stringKey;
    private SourceKey dateKey;


    @BeforeClass
    public static void boot() throws Exception {
        Preparer.prepareCubeBuild();
    }

    @Before
    public void setUp() throws Exception {
        File file = new File(TestResource.getRunPath(getClass()), "resources");
        file.deleteOnExit();
        intKey = new SourceKey("int_table");
        longKey = new SourceKey("long_table");
        doubleKey = new SourceKey("double_table");
        stringKey = new SourceKey("string_table");
        dateKey = new SourceKey("date_table");
    }

    private void transport(SourceKey key, SwiftResultSet set) {
        boolean success = true;
        try {
            DataSource dataSource = new DataSource() {
                @Override
                public SwiftMetaData getMetadata() {
                    try {
                        return set.getMetaData();
                    } catch (Exception e) {
                        return null;
                    }
                }

                @Override
                public SourceKey getSourceKey() {
                    return key;
                }

                @Override
                public Core fetchObjectCore() {
                    return null;
                }
            };

            Inserter inserter = (Inserter) SwiftContext.get().getBean("historyBlockInserter", dataSource);
            inserter.insertData(set);
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        }
        assertTrue(success);
    }

    @Test
    public void testIntTransport() {
        SwiftResultSet set = new IntResultSet();
        transport(intKey, set);
    }

    @Test
    public void testLongTransport() {
        SwiftResultSet set = new LongResultSet();
        transport(longKey, set);
    }

    @Test
    public void testDoubleTransport() {
        SwiftResultSet set = new DoubleResultSet();
        transport(doubleKey, set);
    }

    @Test
    public void testStringTransport() {
        SwiftResultSet set = new StringResultSet();
        transport(stringKey, set);
    }

    @Test
    public void testDateTransport() {
        SwiftResultSet set = new DateResultSet();
        transport(dateKey, set);
    }
}