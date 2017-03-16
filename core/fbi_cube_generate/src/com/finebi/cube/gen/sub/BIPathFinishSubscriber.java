package com.finebi.cube.gen.sub;

import com.finebi.cube.impl.pubsub.BISubscribe;
import com.finebi.cube.pubsub.IProcessor;
import com.finebi.cube.pubsub.ISubscribeID;

/**
 * This class created on 2016/4/12.
 *
 * @author Connery
 * @since 4.0
 */
public class BIPathFinishSubscriber extends BISubscribe {
    public BIPathFinishSubscriber(ISubscribeID subscribeID, IProcessor processor) {
        super(subscribeID, processor);
    }
}
