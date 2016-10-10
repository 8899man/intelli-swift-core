package com.finebi.cube.impl.operate;

import com.finebi.cube.message.IMessage;
import com.finebi.cube.pubsub.IProcessor;
import com.finebi.cube.pubsub.IPublish;
import com.finebi.cube.common.log.BILoggerFactory;

/**
 * This class created on 2016/3/26.
 *
 * @author Connery
 * @since 4.0
 */
public class BIPrcessorStepTwo implements IProcessor {
    private IPublish publish;

    @Override
    public void process(IMessage lastReceiveMessage) {
        try {
            publish.publicRunningMessage(null);
            System.out.println("Start Second Step");
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                BILoggerFactory.getLogger().error(e.getMessage(), e);
            }
            System.out.println("Stop  Second Step");
            publish.publicStopMessage(null);
        } catch (Exception e) {
            BILoggerFactory.getLogger().error(e.getMessage(), e);
        }
    }
    @Override
    public Object getResult() {
        return null;
    }
    @Override
    public void setPublish(IPublish publish) {
        this.publish = publish;
    }
}
