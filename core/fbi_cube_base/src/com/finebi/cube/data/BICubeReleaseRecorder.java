package com.finebi.cube.data;

import com.finebi.cube.data.input.primitive.ICubePrimitiveReader;
import com.finebi.cube.data.output.primitive.ICubePrimitiveWriter;
import com.fr.bi.common.factory.BIMateFactory;
import com.fr.bi.common.factory.IModuleFactory;
import com.fr.bi.common.factory.annotation.BIMandatedObject;
import com.fr.bi.common.factory.annotation.BISingletonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This class created on 2016/5/3.
 *
 * @author Connery
 * @since 4.0
 */
@BIMandatedObject(module = IModuleFactory.CUBE_BASE_MODULE, factory = BIMateFactory.CUBE_BASE
        , implement = BICubeReleaseRecorder.class)
@BISingletonObject
public class BICubeReleaseRecorder implements ICubeSourceReleaseManager {
    //    StackTraceElement[] elements = ;
    private Map<Object, StackTraceElement[]> content;

    public BICubeReleaseRecorder() {
        content = new HashMap<Object, StackTraceElement[]>();
    }

    public Map<Object, StackTraceElement[]> getContent() {
        return content;
    }

    public void record(Object obj) {
        synchronized (content) {
            if (!content.containsKey(obj)) {
                content.put(obj, Thread.currentThread().getStackTrace());
            } else {
//            throw BINonValueUtils.beyondControl();
            }
        }
    }

    public void clear() {
        content.clear();
    }

    public void remove(Object obj) {
        synchronized (content) {
            if (content.containsKey(obj)) {
                content.remove(obj);
            } else {
//            throw BINonValueUtils.beyondControl();
            }
        }
    }

    @Override
    public void release(ICubePrimitiveWriter writer) {
//        writer.releaseSource();
        remove(writer);
    }

    @Override
    public void release(ICubePrimitiveReader reader) {
//        reader.releaseSource();
        remove(reader);
    }
}
