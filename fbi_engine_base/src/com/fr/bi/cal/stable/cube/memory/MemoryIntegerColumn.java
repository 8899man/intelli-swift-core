package com.fr.bi.cal.stable.cube.memory;

import com.fr.bi.cal.stable.tableindex.detailgetter.MemoryDetailGetter;
import com.fr.bi.stable.engine.index.getter.DetailGetter;
import com.fr.bi.stable.io.newio.SingleUserNIOReadManager;

import java.util.ArrayList;

/**
 * Created by 小灰灰 on 2016/1/14.
 */
public class MemoryIntegerColumn extends AbstractSingleMemoryColumn<Integer> {
    @Override
    public DetailGetter<Integer> createDetailGetter(SingleUserNIOReadManager manager) {
        return new MemoryDetailGetter<Integer>(detail);
    }

    @Override
    protected void initDetail() {
        detail = new ArrayList<Integer>();
    }
}