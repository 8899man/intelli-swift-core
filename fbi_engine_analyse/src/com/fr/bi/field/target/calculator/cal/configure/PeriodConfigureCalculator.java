package com.fr.bi.field.target.calculator.cal.configure;

import com.fr.base.FRContext;
import com.fr.bi.field.target.key.cal.configuration.BIPeriodCalTargetKey;
import com.fr.bi.field.target.key.sum.AvgKey;
import com.fr.bi.field.target.target.cal.target.configure.BIConfiguredCalculateTarget;
import com.fr.bi.stable.constant.BIReportConstant;
import com.fr.bi.stable.report.key.TargetGettingKey;
import com.fr.bi.stable.report.result.BICrossNode;
import com.fr.bi.stable.report.result.BINode;
import com.fr.bi.stable.report.result.BITargetKey;
import com.fr.bi.stable.utils.CubeBaseUtils;
import com.fr.general.ComparatorUtils;

import java.util.*;

/**
 * Created by 小灰灰 on 2015/7/2.
 */
public class PeriodConfigureCalculator extends AbstractConfigureCalulator {
    private static final long serialVersionUID = 550160604753618347L;
    private int type = BIReportConstant.TARGET_TYPE.CAL_VALUE.PERIOD_TYPE.VALUE;

    public PeriodConfigureCalculator(BIConfiguredCalculateTarget target, String target_id, int start_group, int period_type) {
        super(target, target_id, start_group);
        this.type = period_type;
    }
    //TODO 待测试

    /**
     * 计算target值
     *
     * @param node node节点
     */
    @Override
    public void calCalculateTarget(BINode node) {
        Object key = getCalKey();
        int deep = getCalDeep(node);
        if (key == null) {
            return;
        }
        BINode tempNode = node;
        //从第几个纬度开始计算
        int calDeep = start_group == 0 ? deep - 1 : deep;
        for (int i = 0; i < calDeep; i++) {
            if (tempNode.getFirstChild() == null) {
                break;
            }
            tempNode = tempNode.getFirstChild();
        }
        List<RankDealWith> nodeList = new ArrayList<RankDealWith>();
        BINode cursor_node = tempNode;
        BINode last_node = null;
        while (cursor_node != null) {
            nodeList.add(new RankDealWith(last_node, cursor_node));
            last_node = cursor_node;
            cursor_node = cursor_node.getSibling();
            if (cursor_node != null && start_group == 0 && !ComparatorUtils.equals(last_node.getParent(), cursor_node.getParent())) {
                last_node = null;
            }
            try {
                CubeBaseUtils.invokeCalculatorThreads(nodeList);
            } catch (InterruptedException e) {
                FRContext.getLogger().error(e.getMessage(), e);
            }
        }

    }

    @Override
    public BITargetKey createTargetKey() {
        return new BIPeriodCalTargetKey(targetName, target_id, targetMap, start_group);
    }

    /**
     * 计算target值
     *
     * @param node 节点
     * @param key1 target的key
     */
    @Override
    public void calCalculateTarget(BICrossNode node, TargetGettingKey key1) {
        Object key = getCalKey();
        if (key == null) {
            return;
        }
        BICrossNode tempNode = node;
        for (int i = 0; i < start_group + 1; i++) {
            if (tempNode.getLeftFirstChild() == null) {
                break;
            }
            tempNode = tempNode.getLeftFirstChild();
        }
        List nodeList = new ArrayList();
        BICrossNode cursor_node = tempNode;
        BICrossNode last_node = null;
        while (cursor_node != null) {
            nodeList.add(new RankDealWithCrossNode(last_node, cursor_node));
            last_node = cursor_node;
            cursor_node = cursor_node.getBottomSibling();
        }
        try {
            CubeBaseUtils.invokeCalculatorThreads(nodeList);
        } catch (InterruptedException e) {
            FRContext.getLogger().error(e.getMessage(), e);
        }
    }

    private class RankDealWith implements java.util.concurrent.Callable {
        private BINode last_node;
        private BINode current_node;

        private RankDealWith(BINode last_node, BINode current_node) {
            this.last_node = last_node;
            this.current_node = current_node;
        }


        @Override
        public Object call() throws Exception {
            int deep = 0;
            BINode temp_node = current_node;
            while (temp_node.getFirstChild() != null) {
                temp_node = temp_node.getFirstChild();
                deep++;
            }
            BINode cursor_node = temp_node;
            while (isNotEnd(cursor_node, deep)) {
                BINode n = cursor_node;
                Object[] way = new Object[deep];
                for (int i = way.length; i > 0; i--) {
                    way[i - 1] = n.getData();
                    n = n.getParent();
                }
                Object value = getValueFromLast(way);
                if (value != null) {
                    if (type == BIReportConstant.TARGET_TYPE.CAL_VALUE.PERIOD_TYPE.RATE) {
                        Object key = getCalKey();
                        String targetName = ((TargetGettingKey) key).getTargetName();
                        BITargetKey targetKey = ((TargetGettingKey) key).getTargetKey();
                        double currentValue;
                        if (targetKey instanceof AvgKey) {
                            currentValue = getAvgValue(targetName, (AvgKey) targetKey, cursor_node);
                        } else {
                            currentValue = cursor_node.getSummaryValue(key).doubleValue();
                        }
                        cursor_node.setSummaryValue(createTargetGettingKey(), (currentValue - (Double) value) / (Double) value);
                    } else {
                        cursor_node.setSummaryValue(createTargetGettingKey(), value);
                    }

                }
                cursor_node = cursor_node.getSibling();
            }
            return null;
        }

        private Object getValueFromLast(Object[] way) {
            if (last_node == null) {
                return null;
            }
            int i = 0;
            BINode n = last_node;
            while (i < way.length) {
                n = n.getChild(way[i++]);
                if (n == null) {
                    break;
                }
            }
            if (n == null) {
                return null;
            } else {
                Object key = getCalKey();
                String targetName = ((TargetGettingKey) key).getTargetName();
                BITargetKey targetKey = ((TargetGettingKey) key).getTargetKey();
                Number value;
                if (targetKey instanceof AvgKey) {
                    value = getAvgValue(targetName, (AvgKey) targetKey, n);
                } else {
                    value = n.getSummaryValue(key);
                }
                return value;
            }
        }


        private boolean isNotEnd(BINode node, int deep) {
            if (node == null) {
                return false;
            }
            BINode temp = node;
            for (int i = 0; i < deep; i++) {
                temp = temp.getParent();
            }
            return temp == current_node;
        }

    }

    private class RankDealWithCrossNode implements java.util.concurrent.Callable {
        private BICrossNode last_node;
        private BICrossNode current_node;

        private RankDealWithCrossNode(BICrossNode last_node, BICrossNode current_node) {
            this.last_node = last_node;
            this.current_node = current_node;
        }


        @Override
        public Object call() throws Exception {
            int deep = 0;
            BICrossNode temp_node = current_node;
            while (temp_node.getLeftFirstChild() != null) {
                temp_node = temp_node.getLeftFirstChild();
                deep++;
            }
            BICrossNode cursor_node = temp_node;
            while (isNotEnd(cursor_node, deep)) {
                BICrossNode n = cursor_node;
                Object[] way = new Object[deep];
                for (int i = way.length; i > 0; i--) {
                    way[i - 1] = n.getLeft().getData();
                    n = n.getLeftParent();
                }
                Object value = getValueFromLast(way);
                if (value != null) {
                    if (type == BIReportConstant.TARGET_TYPE.CAL_VALUE.PERIOD_TYPE.RATE) {
                        Object key = getCalKey();
                        String targetName = ((TargetGettingKey) key).getTargetName();
                        BITargetKey targetKey = ((TargetGettingKey) key).getTargetKey();
                        double currentValue;
                        if (targetKey instanceof AvgKey) {
                            currentValue = getAvgValue(targetName, (AvgKey) targetKey, cursor_node);
                        } else {
                            currentValue = cursor_node.getSummaryValue(key).doubleValue();
                        }
                        cursor_node.setSummaryValue(createTargetGettingKey(), (currentValue - (Double) value) / (Double) value);
                    } else {
                        cursor_node.setSummaryValue(createTargetGettingKey(), value);
                    }
                }
                cursor_node = cursor_node.getBottomSibling();
            }
            return null;
        }

        private Object getValueFromLast(Object[] way) {
            if (last_node == null) {
                return null;
            }
            int i = 0;
            BICrossNode n = last_node;
            while (i < way.length) {
                n = n.getLeftChildByKey(way[i++]);
                if (n == null) {
                    break;
                }
            }
            if (n == null) {
                return null;
            } else {
                Object key = getCalKey();
                return n.getSummaryValue(key);
            }
        }


        private boolean isNotEnd(BICrossNode node, int deep) {
            if (node == null) {
                return false;
            }
            BICrossNode temp = node;
            for (int i = 0; i < deep; i++) {
                temp = temp.getLeftParent();
            }
            return temp == current_node;
        }


    }
}