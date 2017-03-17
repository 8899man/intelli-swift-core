package com.fr.bi.stable.engine.index;

import com.finebi.cube.api.ICubeColumnIndexReader;
import com.finebi.cube.api.ICubeColumnDetailGetter;
import com.finebi.cube.api.ICubeValueEntryGetter;
import com.finebi.cube.relation.BITableSourceRelation;
import com.fr.bi.base.key.BIKey;
import com.fr.bi.common.inter.Delete;
import com.fr.bi.stable.data.db.BIDataValue;
import com.fr.bi.stable.data.db.ICubeFieldSource;
import com.fr.bi.stable.file.IndexFile;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.gvi.array.ICubeTableIndexReader;
import com.fr.bi.stable.io.newio.SingleUserNIOReadManager;
import com.fr.bi.stable.structure.collection.list.IntList;
import com.fr.stable.collections.array.IntArray;

import java.util.Date;
import java.util.List;
import java.util.TreeSet;

public interface BITableCubeFile extends Delete {

    void mkDir();

    void writeMain(List<String> columnList);

    void writeVersionCheck();

    void writeTableGenerateVersion(int version);

    int getTableVersion();

    void writeRowCount(long rowCount);

    void writeLastTime();

    void writeRemovedLine(TreeSet<Integer> removedLine);

    ICubeFieldSource[] getBIField();

    void createDetailDataWriter();

    void releaseDetailDataWriter();

    void addDataValue(BIDataValue v);

    boolean checkRelationVersion(List<BITableSourceRelation> relations,
                                        int relation_version);

    boolean checkRelationVersion(BIKey key, List<BITableSourceRelation> relations,
                                        int relation_version);

    IndexFile getLinkIndexFile(List<BITableSourceRelation> relations);

    boolean checkCubeVersion();

    IndexFile getLinkIndexFile(BIKey key, List<BITableSourceRelation> relations);

    Date getCubeLastTime();

    IntArray getRemoveList(SingleUserNIOReadManager manager);

    Long getGroupCount(BIKey key);

    int getRowCount();

    void copyDetailValue(BITableCubeFile cube, SingleUserNIOReadManager manager, long rowCount);

    /**
     * 获取字符串类型的按行的index
     * @param key
     * @param manager
     * @return
     */
    GroupValueIndex getNullGroupValueIndex(BIKey key, SingleUserNIOReadManager manager);

    ICubeTableIndexReader getGroupValueIndexArrayReader(BIKey key, SingleUserNIOReadManager manager);

    ICubeColumnDetailGetter createDetailGetter(BIKey key, SingleUserNIOReadManager manager);

    ICubeColumnIndexReader createGroupByType(BIKey key, List<BITableSourceRelation> relationList, SingleUserNIOReadManager manager);

    ICubeTableIndexReader getBasicGroupValueIndexArrayReader(List<BITableSourceRelation> relationList, SingleUserNIOReadManager manager);

    GroupValueIndex getIndexByRow(BIKey key, int row, SingleUserNIOReadManager manager);

    ICubeValueEntryGetter getValueEntryGetter(BIKey key, List<BITableSourceRelation> relationList, SingleUserNIOReadManager manager);
}