package com.finebi.cube.structure.table;

import com.finebi.cube.BICubeTestBase;
import com.finebi.cube.tools.BIMemDataSourceTestToolCube;

/**
 * This class created on 2016/6/20.
 *
 * @author Connery
 * @since 4.0
 */
public class CompoundCubeTableReaderTest extends BICubeTestBase {
    private BIMemDataSourceTestToolCube tableSource;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        tableSource = new BIMemDataSourceTestToolCube();
    }

    @Override
    public void testVoid() {

    }
}
