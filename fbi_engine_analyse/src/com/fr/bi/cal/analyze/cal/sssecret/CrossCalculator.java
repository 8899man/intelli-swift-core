/**
 *
 */
package com.fr.bi.cal.analyze.cal.sssecret;

import com.finebi.cube.api.ICubeDataLoader;
import com.finebi.cube.common.log.BILoggerFactory;
import com.fr.bi.cal.analyze.cal.result.*;
import com.fr.bi.report.key.TargetGettingKey;
import com.fr.bi.report.result.TargetCalculator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Daniel
 *         计算交叉表用的
 */
public class CrossCalculator {

    private ICubeDataLoader loader;

//    private Map<Integer, NewCrossRootKeyMap> pageMap = new ConcurrentHashMap<Integer, NewCrossRootKeyMap>(2);

    public CrossCalculator(ICubeDataLoader loader) {
        this.loader = loader;
    }


    public void execute(NewCrossRoot root, List calculators, CrossExpander expander) {


        ExecutorThread t = new ExecutorThread();
        t.calculators = calculators;
        t.root = root;
        t.expander = expander;
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
    }


    private class ExecutorThread extends Thread {


        private List<TargetCalculator> calculators;

        private NewCrossRoot root;

        private CrossExpander expander;


        @Override
        public void run() {

            if (calculators != null) {
                TargetCalculator[] array = calculators.toArray(new TargetCalculator[calculators.size()]);
                CrossNode baseNode = new CrossNode4Calculate(this.top(), this.left(), array);
                this.left().setValue(baseNode);
                this.top().setValue(baseNode);
                top().dealWithChildTop4Calculate(left(), baseNode, array);
                left().dealWithChildLeft4Calculate(top(), baseNode, array);
                left().buildLeftRelation(top());
            } else {
                CrossNode baseNode = new CrossNode(this.top(), this.left());
                this.left().setValue(baseNode);
                this.top().setValue(baseNode);
                top().dealWithChildTop(left(), baseNode);
                left().dealWithChildLeft(top(), baseNode);
                left().buildLeftRelation(top());
            }
            if (calculators == null) {
                return;
            }
            createSumIndex(left(), expander.getYExpander(), expander.getYExpander(), expander.getXExpander());
        }

        private void createSumIndex(CrossHeader left, NodeExpander yp, NodeExpander y, NodeExpander x) {
            if (yp != null) {
                dealWithCrossNode(left.getValue(), x, x);
                for (int i = 0; i < left.getChildLength(); i++) {
                    createSumIndex((CrossHeader) left.getChild(i), y, y == null ? null : y.getChildExpander((left.getChild(i)).getShowValue()), x);
                }
            }
        }

        private void dealWithCrossNode(CrossNode node, NodeExpander xp, NodeExpander x) {
            if (xp != null) {
                for (int i = 0; i < calculators.size(); i++) {
                    TargetCalculator calculator = calculators.get(i);
                    calculator.calculateFilterIndex(loader);
                    //TODO 改成多线程
                    TargetGettingKey key = calculator.createTargetGettingKey();
                    calculator.doCalculator(loader.getTableIndex(calculator.createTableKey().getTableSource()), node, node.getIndex4CalByTargetKey(key), key);
                }
                for (int i = 0; i < node.getTopChildLength(); i++) {
                    dealWithCrossNode(node.getTopChild(i), x, x == null ? null : x.getChildExpander(node.getTopChild(i).getHead().getShowValue()));
                }
            }
        }

        private CrossHeader left() {
            return root.getLeft();
        }

        private CrossHeader top() {
            return root.getTop();
        }

    }

    private class NewCrossRootKeyMap {
        private NewCrossRoot root;
        private Map<TargetCalculator, Integer> calculatedKeys = new ConcurrentHashMap<TargetCalculator, Integer>(2);

        private NewCrossRootKeyMap(NewCrossRoot root) {
            this.root = root;
        }
    }

}