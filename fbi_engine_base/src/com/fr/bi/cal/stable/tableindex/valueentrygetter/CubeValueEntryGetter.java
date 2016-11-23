package com.fr.bi.cal.stable.tableindex.valueentrygetter;

import com.finebi.cube.api.ICubeColumnIndexReader;
import com.finebi.cube.api.ICubeValueEntryGetter;
import com.finebi.cube.relation.BITableSourceRelation;
import com.fr.bi.base.key.BIKey;
import com.fr.bi.stable.file.ColumnFile;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.io.newio.SingleUserNIOReadManager;
import com.fr.bi.stable.structure.object.CubeValueEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 小灰灰 on 2016/7/5.
 */
public class CubeValueEntryGetter implements ICubeValueEntryGetter {
    private ColumnFile cf;
    private SingleUserNIOReadManager manager;
    private BIKey key;
    private List<BITableSourceRelation> relationList;
    public CubeValueEntryGetter(ColumnFile cf, SingleUserNIOReadManager manager, BIKey key, List<BITableSourceRelation> relationList) {
        this.cf = cf;
        this.manager = manager;
        this.key = key;
        this.relationList = relationList;
    }

    @Override
    public GroupValueIndex getIndexByRow(int row) {
        return cf.getIndexByRow(row, manager);
    }

    @Override
    public CubeValueEntry getEntryByRow(int row) {
        return new CubeValueEntry(cf.createDetailGetter(manager).getValue(row), cf.getIndexByRow(row, manager), cf.getPositionOfGroup(row, manager));
    }

    @Override
    public CubeValueEntry getEntryByGroupRow(int row) {
        ICubeColumnIndexReader reader = cf.createGroupByType(key, new ArrayList<BITableSourceRelation>(), manager);
        return new CubeValueEntry(reader.getGroupValue(row), reader.getIndex(reader.getGroupValue(row)), row);
    }

    @Override
    public Object getGroupValue(int groupRow) {
        ICubeColumnIndexReader reader = cf.createGroupByType(key, new ArrayList<BITableSourceRelation>(), manager);
        return reader.getGroupValue(groupRow);
    }

    @Override
    public int getPositionOfGroupByRow(int row) {
        return cf.getPositionOfGroup(row, manager);
    }

    @Override
    public int getGroupSize() {
        return (int) cf.getGroupCount(key);
    }
}
