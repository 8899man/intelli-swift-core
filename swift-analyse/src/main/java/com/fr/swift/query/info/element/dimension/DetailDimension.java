package com.fr.swift.query.info.element.dimension;

import com.fr.swift.query.group.Group;
import com.fr.swift.query.group.info.IndexInfo;
import com.fr.swift.query.sort.Sort;
import com.fr.swift.segment.column.ColumnKey;

/**
 * Created by pony on 2017/12/22.
 */
public class DetailDimension extends AbstractDimension {
    public DetailDimension(int index, ColumnKey columnKey, Group group, Sort sort, IndexInfo indexInfo) {
        super(index, columnKey, group, sort, indexInfo);
    }

    @Override
    public DimensionType getDimensionType() {
        return DimensionType.DETAIL;
    }
}
