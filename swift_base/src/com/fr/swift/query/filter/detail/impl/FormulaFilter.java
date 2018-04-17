package com.fr.swift.query.filter.detail.impl;

import com.fr.script.Calculator;
import com.fr.swift.bitmap.BitMaps;
import com.fr.swift.bitmap.ImmutableBitMap;
import com.fr.swift.bitmap.MutableBitMap;
import com.fr.swift.query.filter.detail.DetailFilter;
import com.fr.swift.result.SwiftNode;
import com.fr.swift.segment.Segment;
import com.fr.swift.segment.column.ColumnKey;
import com.fr.swift.source.etl.utils.FormulaUtils;

import java.util.Map;

/**
 * Created by Lyon on 2018/3/22.
 */
public class FormulaFilter implements DetailFilter {

    private String expression;
    private Segment segment;

    public FormulaFilter(String expression, Segment segment) {
        this.expression = expression;
        this.segment = segment;
    }

    @Override
    public ImmutableBitMap createFilterIndex() {
        Calculator calculator = Calculator.createCalculator();
        String formula = FormulaUtils.getParameterIndexEncodedFormula(expression);
        Map<String, ColumnKey> columnKeyMap = FormulaUtils.createColumnIndexMap(expression, segment);
        MutableBitMap bitMap = BitMaps.newRoaringMutable();
        for (int row = 0, rowCount = segment.getRowCount(); row < rowCount; row++) {
            Object value = FormulaUtils.getCalculatorValue(calculator, formula, segment, columnKeyMap, row);
            if (value instanceof Boolean && (Boolean) value) {
                bitMap.add(row);
            }
        }
        return bitMap.toBitMap();
    }

    @Override
    public boolean matches(SwiftNode node, int targetIndex) {
        return false;
    }
}
