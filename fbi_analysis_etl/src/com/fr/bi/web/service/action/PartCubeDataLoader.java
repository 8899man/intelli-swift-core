package com.fr.bi.web.service.action;

import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.api.ICubeTableService;
import com.finebi.cube.conf.field.BusinessField;
import com.fr.bi.base.BICore;
import com.fr.bi.base.key.BIKey;
import com.fr.bi.cal.stable.cube.memory.MemoryCubeFile;
import com.fr.bi.cal.stable.tableindex.index.BITableIndex;
import com.fr.bi.common.inter.Traversal;
import com.fr.bi.etl.analysis.data.AnalysisETLTableSource;
import com.fr.bi.etl.analysis.data.UserBaseTableSource;
import com.fr.bi.etl.analysis.data.UserCubeTableSource;
import com.fr.bi.etl.analysis.data.UserETLTableSource;
import com.fr.bi.stable.constant.BIBaseConstant;
import com.fr.bi.stable.data.db.BIDataValue;
import com.fr.bi.stable.data.source.CubeTableSource;
import com.fr.bi.stable.engine.index.key.IndexKey;
import com.fr.bi.stable.io.newio.SingleUserNIOReadManager;
import com.fr.bi.stable.utils.program.BIConstructorUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 小灰灰 on 2016/5/16.
 */
public class PartCubeDataLoader implements ICubeDataLoader {
    private static final String EXCEPTION = "ONLY PARAMETER TYPE ITableSource SUPPORT HERE";

    private long userId;

    private static final Map<Long, PartCubeDataLoader> userMap = new ConcurrentHashMap<Long, PartCubeDataLoader>();


    private transient Map<BICore, CubeTableSource> sourceMap = new ConcurrentHashMap<BICore, CubeTableSource>();

    public PartCubeDataLoader(long userId) {
        this.userId = userId;
    }

    public static PartCubeDataLoader getInstance(long userId, UserCubeTableSource source) {
        PartCubeDataLoader loader = BIConstructorUtils.constructObject(userId, PartCubeDataLoader.class, userMap, false);
        loader.registSource(source);
        return loader;
    }


    private void registSource(CubeTableSource source) {
        BICore core = source.fetchObjectCore();
        if (!sourceMap.containsKey(core)) {
            sourceMap.put(source.fetchObjectCore(), source);
            if (source.getType() == BIBaseConstant.TABLE_TYPE.USER_ETL) {
                for (CubeTableSource s : ((UserETLTableSource) source).getParents()) {
                    registSource(s);
                }
            }
        }
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public boolean needReadTempValue() {
        return false;
    }

    @Override
    public boolean needReadCurrentValue() {
        return false;
    }

    @Override
    public SingleUserNIOReadManager getNIOReaderManager() {
        throw new RuntimeException(EXCEPTION);
    }

    @Override
    public void releaseCurrentThread() {

    }

    @Override
    public ICubeTableService getTableIndex(CubeTableSource tableSource) {
        CubeTableSource source = sourceMap.get(tableSource.fetchObjectCore());
        if (isParentTableIndex(source)) {
            return getTableIndex(((UserETLTableSource) source).getParents().get(0));
        }
        final MemoryCubeFile cube = new MemoryCubeFile(source.getFieldsArray(new HashSet<CubeTableSource>()));
        cube.writeRowCount(source.read(new Traversal<BIDataValue>() {
            @Override
            public void actionPerformed(BIDataValue data) {
                cube.addDataValue(data);
            }
        }, source.getFieldsArray(new HashSet<CubeTableSource>()), this));
        return new BITableIndex(cube);
    }

    @Override
    public BIKey getFieldIndex(BusinessField column) {
        return new IndexKey(column.getFieldName());
    }

    @Override
    public ICubeTableService getTableIndex(CubeTableSource tableSource, int start, int end) {
        CubeTableSource source = sourceMap.get(tableSource.fetchObjectCore());
        if (isParentTableIndex(source)) {
            return getTableIndex(((UserETLTableSource) source).getParents().get(0), start, end);
        }
        final MemoryCubeFile cube = new MemoryCubeFile(source.getFieldsArray(new HashSet<CubeTableSource>()));
        cube.writeRowCount(source.read4Part(new Traversal<BIDataValue>() {
            @Override
            public void actionPerformed(BIDataValue data) {
                cube.addDataValue(data);
            }
        }, source.getFieldsArray(new HashSet<CubeTableSource>()), this, start, end));
        return new BITableIndex(cube);
    }

    private boolean isParentTableIndex(CubeTableSource source) {
        return source.getType() == BIBaseConstant.TABLE_TYPE.USER_ETL && (((UserETLTableSource) source).hasTableFilterOperator() || ((UserETLTableSource) source).getETLOperators().isEmpty());
    }

    @Override
    public void clear() {
        synchronized (this) {
            if (userMap != null) {
                userMap.clear();
            }
            if (sourceMap != null) {
                Iterator<Map.Entry<BICore, CubeTableSource>> it = sourceMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<BICore, CubeTableSource> entry = it.next();
                    if (entry.getValue().getType() == BIBaseConstant.TABLE_TYPE.USER_ETL) {
                        ((AnalysisETLTableSource) entry.getValue()).clearUserBaseTableMap();
                    } else {
                        ((UserBaseTableSource) entry.getValue()).clearUserBaseTableMap();
                    }
                }
                sourceMap.clear();
            }

        }
    }

    public static void clearAll() {
        synchronized (userMap) {
            Iterator<Map.Entry<Long, PartCubeDataLoader>> it = userMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, PartCubeDataLoader> entry = it.next();
                entry.getValue().clear();
            }
        }
    }

    @Override
    public long getVersion() {
        return 0;
    }
}
