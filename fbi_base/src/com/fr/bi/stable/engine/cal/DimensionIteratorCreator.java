package com.fr.bi.stable.engine.cal;

import com.finebi.cube.api.ICubeValueEntryGetter;
import com.fr.bi.base.FinalInt;
import com.fr.bi.stable.constant.BIBaseConstant;
import com.fr.bi.stable.gvi.GVIFactory;
import com.fr.bi.stable.gvi.GVIUtils;
import com.fr.bi.stable.gvi.GroupValueIndex;
import com.fr.bi.stable.gvi.traversal.SingleRowTraversalAction;
import com.fr.bi.stable.io.newio.NIOConstant;
import com.fr.stable.collections.array.IntArray;

import java.util.*;

/**
 * Created by 小灰灰 on 2016/11/28.
 */
public class DimensionIteratorCreator {
    /**
     * 根据父节点的过滤条件算出子节点的结构
     * @param getter 子节点所在维度的ICubeValueEntryGetter
     * @param filterGVI 父节点的索引
     * @param asc 升序为true
     * @return 迭代器
     */
    public static DimensionIterator createValueMapIterator(ICubeValueEntryGetter getter, GroupValueIndex filterGVI, boolean asc){
        return createValueMapIterator(getter, filterGVI, 0, asc);
    }

    public static DimensionIterator createValueMapIterator(ICubeValueEntryGetter getter, GroupValueIndex filterGVI, Object start, boolean asc){
        int index = asc ? getter.getPositionOfGroupByValue(start) : getter.getGroupSize() - getter.getPositionOfGroupByValue(start) - 1;
        return createValueMapIterator(getter, filterGVI, index, asc);
    }
    /**
     * 根据父节点的过滤条件算出子节点的结构
     * @param getter 子节点所在维度的ICubeValueEntryGetter
     * @param filterGVI 父节点的索引
     * @param asc 升序为true
     * @param startIndex 偏移量
     * @return 迭代器
     */
    public static DimensionIterator createValueMapIterator(ICubeValueEntryGetter getter, GroupValueIndex filterGVI, int startIndex, boolean asc){
        if (GVIUtils.isAllShowRoaringGroupValueIndex(filterGVI)){
            return getAllShowIterator(getter, startIndex,  asc);
        }
        SortTool tool = SortToolUtils.getSortTool(getter.getGroupSize(), filterGVI.getRowsCountWithData());
        switch (tool) {
            case INT_ARRAY:
                return getArraySortIterator(getter, filterGVI, startIndex, asc);
            case TREE_MAP_RE_SORT:
                return getTreeMapReSortIterator(getter, filterGVI, startIndex, asc);
            case INT_ARRAY_RE_SORT:
                return getArrayReSortIterator(getter, filterGVI, startIndex, asc);
            case DIRECT:
                return getOneKeyIterator(getter, filterGVI);
            case EMPTY:
                return EMPTY;
            case TREE_MAP:
                return getTreeMapSortIterator(getter, filterGVI, startIndex, asc);
            default:
                return getArraySortIterator(getter, filterGVI, startIndex, asc);
        }
    }

    private static final DimensionIterator EMPTY = new DimensionIterator(){

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Map.Entry<Object, GroupValueIndex> next() {
            return null;
        }

        @Override
        public void remove() {

        }

        @Override
        public int getCurrentGroup() {
            return 0;
        }

        @Override
        public boolean canReGainGroupValueIndex() {
            return false;
        }

        @Override
        public GroupValueIndex getGroupValueIndexByGroupIndex(int groupIndex) {
            return null;
        }

    };


    private static DimensionIterator getAllShowIterator(final ICubeValueEntryGetter getter, int startIndex, boolean asc) {
        return asc ? getAllShowASCIterator(getter, startIndex) : getAllShowDESCIterator(getter, startIndex);
    }

    private static DimensionIterator getAllShowASCIterator(final ICubeValueEntryGetter getter, final int startIndex) {
        return new DimensionIterator() {

            private int index = startIndex;
            private int groupSize = getter.getGroupSize();
            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
            @Override
            public boolean hasNext() {
                return index < groupSize;
            }
            @Override
            public Map.Entry<Object, GroupValueIndex> next() {
                final int groupIndex = index;
                Map.Entry<Object, GroupValueIndex> entry = new Map.Entry<Object, GroupValueIndex>() {
                    @Override
                    public Object getKey() {
                        return getter.getGroupValue(groupIndex);
                    }

                    @Override
                    public GroupValueIndex getValue() {
                        return getter.getIndexByGroupRow(groupIndex);
                    }

                    @Override
                    public GroupValueIndex setValue(GroupValueIndex value) {
                        return null;
                    }

                    @Override
                    public boolean equals(Object o) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }
                };
                index++;
                return entry;
            }

            @Override
            public int getCurrentGroup() {
                return index - 1;
            }

            @Override
            public boolean canReGainGroupValueIndex(){
                return true;
            }

            @Override
            public GroupValueIndex getGroupValueIndexByGroupIndex(int groupIndex) {
                return getter.getIndexByGroupRow(groupIndex);
            }
        };
    }

    private static DimensionIterator getAllShowDESCIterator(final ICubeValueEntryGetter getter, final int startIndex) {
        return new DimensionIterator() {
            private int index = getter.getGroupSize() - 1 - startIndex;
            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }

            @Override
            public boolean hasNext() {
                return index >= 0;
            }

            @Override
            public Map.Entry<Object, GroupValueIndex> next() {
                final int groupIndex = index;
                Map.Entry<Object, GroupValueIndex> entry = new Map.Entry<Object, GroupValueIndex>() {
                    @Override
                    public Object getKey() {
                        return getter.getGroupValue(groupIndex);
                    }

                    @Override
                    public GroupValueIndex getValue() {
                        return getter.getIndexByGroupRow(groupIndex);
                    }

                    @Override
                    public GroupValueIndex setValue(GroupValueIndex value) {
                        return null;
                    }

                    @Override
                    public boolean equals(Object o) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }
                };
                index--;
                return entry;
            }
            @Override
            public int getCurrentGroup() {
                return index + 1;
            }

            @Override
            public boolean canReGainGroupValueIndex(){
                return true;
            }

            @Override
            public GroupValueIndex getGroupValueIndexByGroupIndex(int groupIndex) {
                return getter.getIndexByGroupRow(groupIndex);
            }
        };
    }


    private static DimensionIterator getArraySortIterator(final ICubeValueEntryGetter getter, GroupValueIndex filterGVI, int startIndex, boolean asc) {
        final int[] groupIndex = new int[getter.getGroupSize()];
        Arrays.fill(groupIndex, NIOConstant.INTEGER.NULL_VALUE);
        filterGVI.Traversal(new SingleRowTraversalAction() {
            @Override
            public void actionPerformed(int row) {
                int groupRow = getter.getPositionOfGroupByRow(row);
                if (groupRow != NIOConstant.INTEGER.NULL_VALUE) {
                    groupIndex[groupRow] = groupRow;
                }
            }
        });
        return asc ? getArraySortASCIterator(getter, groupIndex, startIndex) : getArraySortDESCIterator(getter, groupIndex, startIndex);
    }

    private static DimensionIterator getArraySortDESCIterator(final ICubeValueEntryGetter getter, final int[] groupIndex, final int startIndex) {
        return new DimensionIterator() {

            private int index = groupIndex.length - 1 - startIndex;

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }

            @Override
            public boolean hasNext() {
                while (index >= 0 && groupIndex[index] == NIOConstant.INTEGER.NULL_VALUE) {
                    index--;
                }
                return index >= 0;
            }

            @Override
            public Map.Entry<Object, GroupValueIndex> next() {
                final int groupRow = index;
                Map.Entry<Object, GroupValueIndex> entry = new Map.Entry<Object, GroupValueIndex>() {
                    @Override
                    public Object getKey() {
                        return getter.getGroupValue(groupRow);
                    }

                    @Override
                    public GroupValueIndex getValue() {
                        return getter.getIndexByGroupRow(groupRow);
                    }

                    @Override
                    public GroupValueIndex setValue(GroupValueIndex value) {
                        return null;
                    }

                    @Override
                    public boolean equals(Object o) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }
                };
                index--;
                return entry;
            }
            @Override
            public int getCurrentGroup() {
                return index + 1;
            }

            @Override
            public boolean canReGainGroupValueIndex(){
                return true;
            }

            @Override
            public GroupValueIndex getGroupValueIndexByGroupIndex(int groupIndex) {
                return getter.getIndexByGroupRow(groupIndex);
            }
        };
    }

    private static DimensionIterator getArraySortASCIterator(final ICubeValueEntryGetter getter, final int[] groupIndex, final int startIndex) {
        return new DimensionIterator() {

            private int index = startIndex;

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }

            @Override
            public boolean hasNext() {
                while (index < groupIndex.length && groupIndex[index] == NIOConstant.INTEGER.NULL_VALUE) {
                    index++;
                }
                return index < groupIndex.length;
            }

            @Override
            public Map.Entry<Object, GroupValueIndex> next() {
                final int groupRow = index;
                Map.Entry<Object, GroupValueIndex> entry = new Map.Entry<Object, GroupValueIndex>() {
                    @Override
                    public Object getKey() {
                        return getter.getGroupValue(groupRow);
                    }

                    @Override
                    public GroupValueIndex getValue() {
                        return getter.getIndexByGroupRow(groupRow);
                    }

                    @Override
                    public GroupValueIndex setValue(GroupValueIndex value) {
                        return null;
                    }

                    @Override
                    public boolean equals(Object o) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }
                };
                index++;
                return entry;
            }
            @Override
            public int getCurrentGroup() {
                return index - 1;
            }

            @Override
            public boolean canReGainGroupValueIndex(){
                return true;
            }

            @Override
            public GroupValueIndex getGroupValueIndexByGroupIndex(int groupIndex) {
                return getter.getIndexByGroupRow(groupIndex);
            }
        };
    }

    private static DimensionIterator getOneKeyIterator(final ICubeValueEntryGetter getter, GroupValueIndex filterGVI) {
        final FinalInt i = new FinalInt();
        i.value = NIOConstant.INTEGER.NULL_VALUE;
        filterGVI.Traversal(new SingleRowTraversalAction() {
            @Override
            public void actionPerformed(int row) {
                i.value = row;
            }
        });
        return getOneKeyIterator(getter, i.value);
    }

    private static DimensionIterator getOneKeyIterator(final ICubeValueEntryGetter getter, final Integer row) {
        return new DimensionIterator() {
            int groupRow = getter.getPositionOfGroupByRow(row);
            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }

            @Override
            public boolean hasNext() {
                return groupRow != NIOConstant.INTEGER.NULL_VALUE;
            }

            @Override
            public Map.Entry<Object, GroupValueIndex> next() {
                Map.Entry<Object, GroupValueIndex> entry =  new Map.Entry<Object, GroupValueIndex>() {
                    int index = groupRow;
                    @Override
                    public Object getKey() {
                        return getter.getGroupValue(index);
                    }

                    @Override
                    public GroupValueIndex getValue() {
                        return GVIFactory.createGroupValueIndexBySimpleIndex(row);
                    }

                    @Override
                    public GroupValueIndex setValue(GroupValueIndex value) {
                        return null;
                    }

                    @Override
                    public boolean equals(Object o) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }
                };
                groupRow = NIOConstant.INTEGER.NULL_VALUE;
                return entry;
            }
            @Override
            public int getCurrentGroup() {
                return groupRow;
            }

            //这个能regain，但是这种情况一般几乎不占内存，就不要释放了，返回false吧
            @Override
            public boolean canReGainGroupValueIndex(){
                return false;
            }

            @Override
            public GroupValueIndex getGroupValueIndexByGroupIndex(int groupIndex) {
                return GVIFactory.createGroupValueIndexBySimpleIndex(row);
            }
        };
    }

    private static DimensionIterator getTreeMapSortIterator(final ICubeValueEntryGetter getter, GroupValueIndex filterGVI, final int startIndex, final boolean asc) {
        final TreeSet<Integer> set = asc ? new TreeSet<Integer>(BIBaseConstant.COMPARATOR.COMPARABLE.ASC) : new TreeSet<Integer>(BIBaseConstant.COMPARATOR.COMPARABLE.DESC);
        filterGVI.Traversal(new SingleRowTraversalAction() {
            @Override
            public void actionPerformed(int row) {
                int groupRow = getter.getPositionOfGroupByRow(row);
                if (match(startIndex, asc, groupRow)) {
                    set.add(groupRow);
                }
            }
        });
        final Iterator<Integer> it = set.iterator();
        return getTreeMapSortIterator(getter, it);
    }

    private static DimensionIterator getTreeMapSortIterator(final ICubeValueEntryGetter getter, final Iterator<Integer> it) {
        return new DimensionIterator() {
            private int lastGroup;
            @Override
            public void remove() {
                it.remove();
            }

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Map.Entry<Object, GroupValueIndex> next() {
                final int groupRow = it.next();
                lastGroup = groupRow;
                return new Map.Entry<Object, GroupValueIndex>() {
                    @Override
                    public Object getKey() {
                        return getter.getGroupValue(groupRow);
                    }

                    @Override
                    public GroupValueIndex getValue() {
                        return getter.getIndexByGroupRow(groupRow);
                    }

                    @Override
                    public GroupValueIndex setValue(GroupValueIndex value) {
                        return null;
                    }

                    @Override
                    public boolean equals(Object o) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }
                };
            }
            @Override
            public int getCurrentGroup() {
                return lastGroup;
            }

            @Override
            public boolean canReGainGroupValueIndex(){
                return true;
            }
            @Override
            public GroupValueIndex getGroupValueIndexByGroupIndex(int groupIndex) {
                return getter.getIndexByGroupRow(groupIndex);
            }
        };
    }

    private static DimensionIterator getTreeMapReSortIterator(final ICubeValueEntryGetter getter, GroupValueIndex filterGVI, final int startIndex, final boolean asc) {
        final TreeMap<Integer, IntArray> map = asc ? new TreeMap<Integer, IntArray>(BIBaseConstant.COMPARATOR.COMPARABLE.ASC) : new TreeMap<Integer, IntArray>(BIBaseConstant.COMPARATOR.COMPARABLE.DESC);
        filterGVI.Traversal(new SingleRowTraversalAction() {
            @Override
            public void actionPerformed(int row) {
                int groupRow = getter.getPositionOfGroupByRow(row);
                if (match(startIndex, asc, groupRow)) {
                    IntArray array = map.get(groupRow);
                    if (array == null){
                        array = new IntArray();
                        map.put(groupRow, array);
                    }
                    array.add(row);
                }
            }
        });
        return getTreeMapReSortIterator(getter, map);
    }

    private static boolean match(int startIndex, boolean asc, int groupRow) {
        if (groupRow == NIOConstant.INTEGER.NULL_VALUE){
            return false;
        }
        if (startIndex == 0){
            return true;
        }
        return asc ? groupRow >= startIndex : groupRow <= startIndex;
    }

    private static DimensionIterator getTreeMapReSortIterator(ICubeValueEntryGetter getter, Map<Integer, IntArray> map) {
        return new TreeMapResortIterator(getter, map);
    }

    private static DimensionIterator getArrayReSortIterator(final ICubeValueEntryGetter getter, GroupValueIndex filterGVI, int startIndex, boolean asc) {
        final IntArray[] groupArray = new IntArray[getter.getGroupSize()];
        filterGVI.Traversal(new SingleRowTraversalAction() {
            @Override
            public void actionPerformed(int row) {
                int groupRow = getter.getPositionOfGroupByRow(row);
                if (groupRow != NIOConstant.INTEGER.NULL_VALUE) {
                    if (groupArray[groupRow] == null){
                        groupArray[groupRow] = new IntArray();
                    }
                    groupArray[groupRow].add(row);
                }
            }
        });
        return asc ? getArrayReSortASCIterator(getter, groupArray, startIndex) : getArrayReSortDESCIterator(getter, groupArray, startIndex);
    }

    private static DimensionIterator getArrayReSortDESCIterator(final ICubeValueEntryGetter getter, final IntArray[] groupArray, final int startIndex) {
        return new DimensionIterator() {
            private int index = groupArray.length - 1 -startIndex;

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }

            @Override
            public boolean hasNext() {
                while (index >= 0 && groupArray[index] == null) {
                    index--;
                }
                return index >= 0;
            }

            @Override
            public Map.Entry<Object, GroupValueIndex> next() {
                Map.Entry<Object, GroupValueIndex> entry = new Map.Entry<Object, GroupValueIndex>() {
                    int group = index;
                    @Override
                    public Object getKey() {
                        return getter.getGroupValue(group);
                    }

                    @Override
                    public GroupValueIndex getValue() {
                        return GVIFactory.createGroupValueIndexBySimpleIndex(groupArray[group]);
                    }

                    @Override
                    public GroupValueIndex setValue(GroupValueIndex value) {
                        return null;
                    }

                    @Override
                    public boolean equals(Object o) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }
                };
                index--;
                return entry;
            }
            @Override
            public int getCurrentGroup() {
                return index + 1;
            }

            //这个能regain，但是这种情况一般几乎不占内存，就不要释放了，返回false吧
            @Override
            public boolean canReGainGroupValueIndex(){
                return false;
            }

            @Override
            public GroupValueIndex getGroupValueIndexByGroupIndex(int groupIndex) {
                return GVIFactory.createGroupValueIndexBySimpleIndex(groupArray[groupIndex]);
            }
        };
    }

    private static DimensionIterator getArrayReSortASCIterator(final ICubeValueEntryGetter getter, final IntArray[] groupArray, final int startIndex) {
        return new DimensionIterator() {

            private int index = startIndex;

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }

            @Override
            public boolean hasNext() {
                while (index < groupArray.length && groupArray[index] == null) {
                    index++;
                }
                return index < groupArray.length;
            }

            @Override
            public Map.Entry<Object, GroupValueIndex> next() {
                Map.Entry<Object, GroupValueIndex> entry = new Map.Entry<Object, GroupValueIndex>() {
                    int group = index;
                    @Override
                    public Object getKey() {
                        return getter.getGroupValue(group);
                    }

                    @Override
                    public GroupValueIndex getValue() {
                        return GVIFactory.createGroupValueIndexBySimpleIndex(groupArray[group]);
                    }

                    @Override
                    public GroupValueIndex setValue(GroupValueIndex value) {
                        return null;
                    }

                    @Override
                    public boolean equals(Object o) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }
                };
                index++;
                return entry;
            }

            @Override
            public int getCurrentGroup() {
                return index - 1;
            }

            //这个能regain，但是这种情况一般几乎不占内存，就不要释放了，返回false吧
            @Override
            public boolean canReGainGroupValueIndex(){
                return false;
            }

            @Override
            public GroupValueIndex getGroupValueIndexByGroupIndex(int groupIndex) {
                return GVIFactory.createGroupValueIndexBySimpleIndex(groupArray[groupIndex]);
            }
        };
    }
}
