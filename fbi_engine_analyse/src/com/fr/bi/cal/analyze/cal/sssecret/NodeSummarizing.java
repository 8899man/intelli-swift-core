package com.fr.bi.cal.analyze.cal.sssecret;

import com.fr.bi.cal.analyze.cal.index.loader.TargetAndKey;
import com.fr.bi.field.target.calculator.XCalculator;
import com.fr.bi.field.target.calculator.sum.AbstractSummaryCalculator;
import com.fr.bi.report.result.CalculatorType;
import com.fr.bi.report.result.TargetCalculator;

/**
 * Created by Hiram on 2015/1/28.
 */
public class NodeSummarizing {
    private MetricMergeResult node;
    protected TargetAndKey[] targetAndKeys;

    public NodeSummarizing(MetricMergeResult node, TargetAndKey[] targetAndKeys) {
        this.node = node;
        this.targetAndKeys = targetAndKeys;
    }

    protected void sum(){
        //不管怎样，root的汇总值必须要清，root没有child的话汇总值就是空
        resetSummaryValue(node);
        sum(node);
    }

    protected void sum(MetricMergeResult node) {
        //没有child的就不用汇总了
        if (node.getChildLength() == 0) {
            return;
        }
        resetSummaryValue(node);
        for (int i = 0; i < node.getChildLength(); i++) {
            MetricMergeResult child = (MetricMergeResult) node.getChild(i);
            sum(child);
            for (TargetAndKey targetAndKey : targetAndKeys) {
                Number value = node.getSummaryValue(targetAndKey.getTargetGettingKey());
                if (value == null) {
                    node.setSummaryValue(targetAndKey.getTargetGettingKey(), child.getSummaryValue(targetAndKey.getTargetGettingKey()));
                } else {
                    Number childValue = child.getSummaryValue(targetAndKey.getTargetGettingKey());
                    if (childValue != null) {
                        TargetCalculator calculator = targetAndKey.getCalculator();
                        if (calculator.getCalculatorType() == CalculatorType.X_SUM){
                            calculator = ((XCalculator)calculator).getCalculator();
                        }
                        if (calculator.getCalculatorType() == CalculatorType.SUM_DETAIL){
                            node.setSummaryValue(targetAndKey.getTargetGettingKey(), ((AbstractSummaryCalculator)calculator).createSumValue(value.doubleValue(), childValue.doubleValue()));
                        }
                    }
                }

            }
        }
    }

    protected void resetSummaryValue(MetricMergeResult node) {
        node.setSummaryValue(new Number[node.getSummaryValue().length]);
    }

}