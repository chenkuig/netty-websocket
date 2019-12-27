package com.shsc.basic.messagecenter.websocket;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.util.Assert;

import com.shsc.basic.messagecenter.websocket.rocketmq.BootStrapMQConsumer;
import com.shsc.basic.messagecenter.websocket.start.IBootstrapServer;

import lombok.extern.slf4j.Slf4j;
@Configuration
@Slf4j
@ConditionalOnBean(IBootstrapServer.class)
public class InitWebSocketServer extends ApplicationObjectSupport  implements SmartInitializingSingleton{
	private static IBootstrapServer BOOTSTRAPSERVER;
	private static BootStrapMQConsumer BOOTSTRAPMQCONSUMER;
	public static void start()  {
		if (BOOTSTRAPSERVER == null ) {
			Assert.isTrue(false, "BOOTSTRAPSERVER class is null ,please check!");
		}else {
			try {
				BOOTSTRAPSERVER.start();
				BOOTSTRAPMQCONSUMER.start();
			} catch (InterruptedException e) {
				log.error("", e);
			}
		}
	}
	@Override
	public void afterSingletonsInstantiated() {
	     ApplicationContext context = getApplicationContext();
	     if (context != null) {
	    	 InitWebSocketServer. BOOTSTRAPSERVER = context.getBean(IBootstrapServer.class);
	    	 InitWebSocketServer.BOOTSTRAPMQCONSUMER = context.getBean(BootStrapMQConsumer.class);
	     }
	}
}
