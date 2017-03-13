package com.finebi.cube.tools.subset;

import com.finebi.cube.gen.oper.BIFieldPathIndexBuilder;
import com.finebi.cube.message.IMessage;
import com.finebi.cube.structure.BICubeTablePath;
import com.finebi.cube.structure.Cube;
import com.fr.bi.stable.data.db.ICubeFieldSource;

/**
 * This class created on 2016/4/13.
 *
 * @author Connery
 * @since 4.0
 */
public class BIFieldPathIndexBuilderTestTool extends BIFieldPathIndexBuilder {
    public BIFieldPathIndexBuilderTestTool(Cube cube, ICubeFieldSource field, BICubeTablePath relationPath) {
        super(cube, field, relationPath);
    }

    @Override
    public Object mainTask(IMessage lastReceiveMessage) {
        System.out.println("Path Path Index!");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return null;
    }    @Override
    public void release() {

    }
}
