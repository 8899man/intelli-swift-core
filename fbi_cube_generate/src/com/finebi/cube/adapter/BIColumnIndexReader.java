package com.finebi.cube.adapter;

import com.finebi.cube.api.ICubeColumnIndexReader;
import com.finebi.cube.exception.BICubeIndexException;
import com.finebi.cube.exception.BIResourceInvalidException;
import com.finebi.cube.structure.BICubeTablePath;
import com.finebi.cube.structure.ICubeIndexDataGetterService;
import com.finebi.cube.structure.column.CubeColumnReaderService;
import com.finebi.cube.utils.BICubePathUtils;
import com.fr.bi.stable.gvi.GVIFactory;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.finebi.cube.relation.BITableSourceRelation;
import com.fr.bi.stable.utils.program.BINonValueUtils;

import java.io.Serializable;
import java.util.*;

/**
 * This class created on 2016/4/16.
 *
 * @author Connery
 * @since 4.0
 */
public class BIColumnIndexReader<T> implements ICubeColumnIndexReader<T> {
    protected CubeColumnReaderService<T> columnReaderService;
    private BICubeTablePath path;
    private ICubeIndexDataGetterService indexDataGetterService;
    private CSet<Map.Entry<T, GroupValueIndex>> ascSet = new CSet<Map.Entry<T, GroupValueIndex>>(true);
    private CSet<Map.Entry<T, GroupValueIndex>> descSet = new CSet<Map.Entry<T, GroupValueIndex>>(false);
    private int size;

    public BIColumnIndexReader(CubeColumnReaderService<T> columnReaderService, List<BITableSourceRelation> relationList) {
        this.columnReaderService = columnReaderService;
        this.size = columnReaderService.sizeOfGroup();
        if (isRelationIndex(relationList)) {
            path = BICubePathUtils.convert(relationList);
            try {
                indexDataGetterService = columnReaderService.getRelationIndexGetter(path);
            } catch (Exception e) {
                throw BINonValueUtils.beyondControl(e);
            }
        } else {
            indexDataGetterService = columnReaderService;
        }
    }

    private boolean isRelationIndex(List<BITableSourceRelation> relationList) {
        return relationList != null && !relationList.isEmpty();
    }

    @Override
    public GroupValueIndex[] getGroupIndex(T[] groupValues) {

        GroupValueIndex[] res = new GroupValueIndex[groupValues.length];
        for (int i = 0; i < groupValues.length; i++) {
            res[i] = getIndex(groupValues[i]);
        }
        return res;
    }

    public GroupValueIndex getIndex(T groupValue) {
        if (groupValue == null) {
            return getNullValueIndex(groupValue);
        } else {
            return getNormalValueIndex(groupValue);
        }
    }

    private GroupValueIndex getNormalValueIndex(T groupValue) {
        try {
            if (groupValue != null) {
                int position = columnReaderService.getPositionOfGroupByGroupValue(groupValue);
                //todo lookup 抛出异常代替返回-1
                if (position == -1) {
                    return GVIFactory.createAllEmptyIndexGVI();
                }
                return indexDataGetterService.getBitmapIndex(position);
            } else {
                throw BINonValueUtils.beyondControl("Please invoke Null value method");

            }
        } catch (BIResourceInvalidException e) {
            throw BINonValueUtils.beyondControl(e);
        } catch (BICubeIndexException e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }

    private GroupValueIndex getNullValueIndex(T groupValue) {
        try {
            if (groupValue == null) {
                return columnReaderService.getNULLIndex(0);
            } else {
                throw BINonValueUtils.beyondControl("Please invoke Normal value method");
            }
        } catch (BICubeIndexException e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }

    @Override
    public T getGroupValue(int groupValuePosition) {
        return columnReaderService.getGroupObjectValue(groupValuePosition);
    }

    public GroupValueIndex getGroupValueIndex(int groupValuePosition) {
        try {
            return indexDataGetterService.getBitmapIndex(groupValuePosition);
        } catch (BICubeIndexException e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }

    @Override
    public T getOriginalValue(int rowNumber) {
        return columnReaderService.getOriginalObjectValueByRow(rowNumber);
    }

    @Override
    public Iterator<Map.Entry<T, GroupValueIndex>> iterator() {
        return ascSet.iterator();
    }

    @Override
    public Iterator<Map.Entry<T, GroupValueIndex>> previousIterator() {
        return descSet.iterator();
    }

    /**
     * 从某个值开始的entry的set
     *
     * @param start
     * @return entry的set
     */
    @Override
    public Iterator<Map.Entry<T, GroupValueIndex>> iterator(T start) {
        return ascSet.iterator(start);
    }

    /**
     * 从某个值开始的反向的entry的set
     *
     * @param start
     * @return entry的set
     */
    @Override
    public Iterator<Map.Entry<T, GroupValueIndex>> previousIterator(T start) {
        return descSet.iterator(start);
    }


    public int size() {
        return size;
    }

    @Override
    public int sizeOfGroup() {
        return size();
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public T[] createKey(int length) {
        return (T[]) new Object[length];
    }

    @Override
    public T createValue(Object v) {
        return (T) v;
    }

    private class CSet<V> implements Set<V>, Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 4997766422304383192L;
        private boolean asc;

        private CSet(boolean asc) {
            this.asc = asc;
        }

        @Override
        public int size() {
            return BIColumnIndexReader.this.size();
        }

        @Override
        public boolean isEmpty() {
            return BIColumnIndexReader.this.size() == 0;
        }

        @Override
        public boolean contains(Object o) {
            try {
                columnReaderService.getPositionOfGroupByGroupValue((T) o);
                return true;
            } catch (BIResourceInvalidException e) {
                return false;
            }

        }

        // august:这儿必须重新new啊 不然第二次用迭代器的时候 不久出错了吗
        @Override
        public Iterator<V> iterator() {
            if (asc) {
                return new CIterator();
            } else {
                return new DIterator();
            }
        }

        public Iterator<V> iterator(T start) {
            if (asc) {
                return new CIterator(start);
            } else {
                return new DIterator(start);
            }
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public Object[] toArray(Object[] a) {
            return new Object[0];
        }

        @Override
        public boolean add(Object o) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection c) {
            return false;
        }

        @Override
        public boolean addAll(Collection c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection c) {
            return false;
        }

        @Override
        public void clear() {

        }
    }

    private class DIterator implements Iterator {
        private int c_index;

        public DIterator() {
            c_index = BIColumnIndexReader.this.size();
        }

        public DIterator(T start) {
            try {
                c_index = columnReaderService.getPositionOfGroupByGroupValue(start) + 1;
            } catch (BIResourceInvalidException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean hasNext() {
            return c_index > 0;
        }

        @Override
        public Object next() {
            return new CEnty(--c_index);
        }

        @Override
        public void remove() {
        }


    }

    private class CIterator implements Iterator {
        private int c_index;

        public CIterator() {
            c_index = 0;
        }

        public CIterator(T start) {
            try {
                c_index = columnReaderService.getPositionOfGroupByGroupValue(start);
            } catch (BIResourceInvalidException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean hasNext() {
            return c_index < BIColumnIndexReader.this.size();
        }

        @Override
        public Object next() {
            return new CEnty(c_index++);
        }

        @Override
        public void remove() {
        }

    }

    private class CEnty implements Map.Entry {
        private int index;

        public CEnty(int index) {
            this.index = index;
        }

        @Override
        public Object getKey() {
            return BIColumnIndexReader.this.getGroupValue(index);
        }

        @Override
        public Object getValue() {
            return BIColumnIndexReader.this.getGroupValueIndex(index);
        }

        @Override
        public Object setValue(Object value) {
            return null;
        }

    }

    @Override
    public T firstKey() {
        return getGroupValue(0);
    }

    @Override
    public T lastKey() {
        if (size() - 1 < 0) {
            return null;
        }
        return getGroupValue(size() - 1);
    }

    @Override
    public long nonPrecisionSize() {
        return size();
    }

    public int getClassType() {
        return columnReaderService.getClassType();
    }

    @Override
    public GroupValueIndex getNULLIndex() {
        try {
            return indexDataGetterService.getNULLIndex(0);
        } catch (BICubeIndexException e) {
            throw BINonValueUtils.beyondControl(e);
        }
    }
}
