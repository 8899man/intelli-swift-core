package com.finebi.cube.tools.operate;

import com.finebi.cube.common.log.BILoggerFactory;
import com.finebi.cube.message.IMessage;
import com.finebi.cube.pubsub.IProcessor;
import com.finebi.cube.pubsub.IPublish;

/**
 * This class created on 2016/3/26.
 *
 * @author Connery
 * @since 4.0
 */
public class BIPrcessorStepOne implements IProcessor {
    private IPublish publish;

    @Override
    public void process(IMessage lastReceiveMessage) {
        try {
            publish.publicRunningMessage(null);
//            System.out.println("Start First Step,");
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                BILoggerFactory.getLogger().error(e.getMessage(), e);
            }
//            System.out.println("Stop First Step,");
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
    public void handleMessage(IMessage receiveMessage) {

    }

    @Override
    public void setPublish(IPublish publish) {
        this.publish = publish;
    }
}
