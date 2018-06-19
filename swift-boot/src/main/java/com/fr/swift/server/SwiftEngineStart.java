package com.fr.swift.server;

import com.fr.swift.boot.ClusterListener;
import com.fr.swift.context.SwiftContext;
import com.fr.swift.cube.queue.ProviderTaskManager;
import com.fr.swift.event.ClusterEvent;
import com.fr.swift.event.ClusterEventType;
import com.fr.swift.event.ClusterListenerHandler;
import com.fr.swift.event.ClusterType;
import com.fr.swift.http.SwiftHttpServer;
import com.fr.swift.log.SwiftLogger;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.property.SwiftProperty;
import com.fr.swift.service.register.LocalSwiftRegister;
import com.fr.workspace.simple.SimpleWork;

/**
 * This class created on 2018/6/12
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI 5.0
 */
public class SwiftEngineStart {

    private static final SwiftLogger LOGGER = SwiftLoggers.getLogger(SwiftEngineStart.class);

    public static void main(String[] args) {
        try {
            SimpleWork.checkIn(System.getProperty("user.dir"));

            SwiftContext.init();
            SwiftContext.getInstance().getBean(SwiftHttpServer.class).start();
            LOGGER.info("http server starting!");

            new LocalSwiftRegister().serviceRegister();
            ClusterListenerHandler.addListener(new ClusterListener());
            ProviderTaskManager.start();
            if (SwiftContext.getInstance().getBean("swiftProperty", SwiftProperty.class).isCluster()) {
                ClusterListenerHandler.handlerEvent(new ClusterEvent(ClusterEventType.JOIN_CLUSTER, ClusterType.CONFIGURE));
            }
        } catch (Throwable e) {
            LOGGER.error(e);
            System.exit(1);
        }
    }
}
