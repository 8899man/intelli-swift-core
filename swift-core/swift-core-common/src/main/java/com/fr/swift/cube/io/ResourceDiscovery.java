package com.fr.swift.cube.io;

import com.fr.swift.cube.io.impl.mem.MemIo;
import com.fr.swift.cube.io.impl.mem.MemIoBuilder;
import com.fr.swift.cube.io.input.Reader;
import com.fr.swift.cube.io.location.IResourceLocation;
import com.fr.swift.cube.io.location.ResourceLocation;
import com.fr.swift.cube.io.output.Writer;
import com.fr.swift.db.SwiftDatabase;
import com.fr.swift.util.IoUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author anchore
 * @date 2017/11/16
 */
public class ResourceDiscovery implements IResourceDiscovery {

    /**
     * schema/table/seg/column/...
     */
    private static final Pattern PATTERN = Pattern.compile(".+/seg\\d+?(/).+");

    private static final ResourceDiscovery INSTANCE = new ResourceDiscovery();

    /**
     * 预览mem io
     * <p>
     * schema/table/seg -> (column/... -> mem io)
     */
    private final Map<String, Map<String, MemIo>> minorMemIos = new ConcurrentHashMap<String, Map<String, MemIo>>();

    /**
     * 增量realtime mem io
     * <p>
     * schema/table/seg -> (column/... -> mem io)
     */
    private final Map<String, Map<String, MemIo>> cubeMemIos = new ConcurrentHashMap<String, Map<String, MemIo>>();

    private static MemIo getMemIo(Map<String, Map<String, MemIo>> segMemIos, IResourceLocation location, BuildConf conf) {
        String path = location.getPath();
        String segPath = getSegPath(path);
        String ioPath = path.substring(segPath.length());

        if (!segMemIos.containsKey(segPath)) {
            Map<String, MemIo> baseMemIos = new HashMap<String, MemIo>();
            MemIo memIo = MemIoBuilder.build(conf);
            baseMemIos.put(ioPath, memIo);
            segMemIos.put(segPath, baseMemIos);
            return memIo;
        }

        Map<String, MemIo> baseMemIos = segMemIos.get(segPath);
        if (!baseMemIos.containsKey(ioPath)) {
            MemIo memIo = MemIoBuilder.build(conf);
            baseMemIos.put(ioPath, memIo);
            return memIo;
        }

        return segMemIos.get(segPath).get(ioPath);
    }

    private static boolean isMemory(IResourceLocation location) {
        return location.getStoreType().isTransient();
    }

    private static boolean isMinor(String path) {
        return path.contains(SwiftDatabase.MINOR_CUBE.getDir());
    }

    private static String getSegPath(String path) {
        //todo 路径需要单独配置，后续需要对此进行改正，现在先简单处理
        Matcher matcher = PATTERN.matcher(path);
        matcher.find();
        return path.substring(0, matcher.start(1));
    }

    public static IResourceDiscovery getInstance() {
        return INSTANCE;
    }

    @Override
    public <R extends Reader> R getReader(IResourceLocation location, BuildConf conf) {
        String path = location.getPath();
        if (!isMemory(location)) {
            return (R) Readers.build(location, conf);
        }
        if (isMinor(path)) {
            synchronized (minorMemIos) {
                return (R) getMemIo(minorMemIos, location, conf);
            }
        }
        synchronized (cubeMemIos) {
            return (R) getMemIo(cubeMemIos, location, conf);
        }
    }

    @Override
    public <W extends Writer> W getWriter(IResourceLocation location, BuildConf conf) {
        String path = location.getPath();
        if (!isMemory(location)) {
            return (W) Writers.build(location, conf);
        }
        if (isMinor(path)) {
            return (W) getMemIo(minorMemIos, location, conf);
        }
        return (W) getMemIo(cubeMemIos, location, conf);
    }

    @Override
    public boolean exists(IResourceLocation location, BuildConf conf) {
        boolean readable = getReader(location, conf).isReadable();
        if (!readable && location.getStoreType().isTransient()) {
            release(location);
        }
        return readable;
    }

    @Override
    public void clear() {
        //todo 增量的memio慎重clear!!!
        for (Map.Entry<String, Map<String, MemIo>> mapEntry : minorMemIos.entrySet()) {
            for (Map.Entry<String, MemIo> entry : mapEntry.getValue().entrySet()) {
                entry.getValue().release();
            }
        }
        minorMemIos.clear();
    }

    @Override
    public Map<String, MemIo> removeCubeResource(String basePath) {
        return cubeMemIos.remove(new ResourceLocation(basePath).getPath());
    }

    @Override
    public void release(IResourceLocation location) {
        if (location.getStoreType().isPersistent()) {
            return;
        }

        String path = location.getPath();
        for (Iterator<Entry<String, Map<String, MemIo>>> segItr = cubeMemIos.entrySet().iterator(); segItr.hasNext(); ) {
            Entry<String, Map<String, MemIo>> segEntry = segItr.next();
            String segPath = segEntry.getKey();
            if (path.equals(segPath)) {
                // 匹配到seg，则整个release
                for (MemIo memIo : segEntry.getValue().values()) {
                    IoUtil.release(memIo);
                }
                segItr.remove();
                continue;
            }

            for (Iterator<Entry<String, MemIo>> memIoItr = segEntry.getValue().entrySet().iterator(); memIoItr.hasNext(); ) {
                Entry<String, MemIo> memIoEntry = memIoItr.next();
                String memIoPath = String.format("%s%s/", segPath, memIoEntry.getKey());
                // 匹配前缀，一般为release单个column
                if (memIoPath.startsWith(path + "/")) {
                    IoUtil.release(memIoEntry.getValue());
                    memIoItr.remove();
                }
            }
        }
    }
}
