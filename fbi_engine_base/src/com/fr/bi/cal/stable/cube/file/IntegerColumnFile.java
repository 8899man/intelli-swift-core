package com.fr.bi.cal.stable.cube.file;

import com.finebi.cube.api.ICubeColumnDetailGetter;
import com.fr.bi.base.key.BIKey;
import com.fr.bi.cal.stable.index.file.field.AbstractNIOCubeFile;
import com.fr.bi.cal.stable.index.file.field.CubeIntegerFile;
import com.fr.bi.cal.stable.tableindex.detailgetter.NormalDetailGetter;
import com.fr.bi.stable.io.newio.SingleUserNIOReadManager;
import com.fr.bi.stable.io.sortlist.ISortNIOReadList;
import com.fr.bi.stable.io.sortlist.SortNIOReadIntList;
import com.fr.bi.stable.operation.sort.comp.ComparatorFacotry;
import com.fr.bi.stable.utils.file.BIFileUtils;
import com.fr.bi.util.BIConfigurePathUtils;

import java.util.Comparator;

public class IntegerColumnFile extends AbstractSingleColumnFile<Integer> {

    public IntegerColumnFile(String path) {
        super(path);
    }


    @Override
    protected AbstractNIOCubeFile<Integer> createDetailFile() {

        return BIFileUtils.createFile(this, getDetailFieldName(), CubeIntegerFile.class, BIConfigurePathUtils.createSingleFieldDetailPath(path));
    }


    @Override
    protected AbstractNIOCubeFile<Integer> createGroupFile() {

        return BIFileUtils.createFile(this, getGroupFieldName(), CubeIntegerFile.class, BIConfigurePathUtils.createSingleFieldGroupPath(path));

    }


    @Override
    protected Comparator<Integer> getComparator() {
        return ComparatorFacotry.INTEGER_ASC;
    }


    @Override
    public ISortNIOReadList<Integer> createSortGroupReader(BIKey key,
                                                           SingleUserNIOReadManager manager) {
        return new SortNIOReadIntList(createGroupReader(manager), getGroupCount(key));
    }


    @Override
    public ICubeColumnDetailGetter createDetailGetter(SingleUserNIOReadManager manager) {
        return new NormalDetailGetter(createDetailReader(manager));
    }


}