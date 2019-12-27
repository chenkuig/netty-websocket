package com.shsc.basic.messagecenter.websocket.rocketmq;

import javax.annotation.Resource;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.spring.autoconfigure.RocketMQProperties;
import org.springframework.stereotype.Component;

import com.shsc.basic.messagecenter.websocket.constant.HttpConstant;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class BootStrapMQConsumer {
	private static final String CONSUMER_INSTALL_NAME = "message-center-websocket";
	private static final String CONSUMER_GROUP = "message-center-websocket";
	@Resource
	private  RocketMQProperties rocketMQProperties;
	@Resource
	private WebSocketRocketMqConsumer webSocketRocketMqConsumer;
	public void start() {
		try {
			DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(CONSUMER_GROUP);
			consumer.setNamesrvAddr(rocketMQProperties.getNameServer());
			consumer.setInstanceName(CONSUMER_INSTALL_NAME);
			consumer.subscribe(HttpConstant.ROCKET_MQ_MESSAGE_CENTER_WEBSOCKET_TOPIC, "");
			consumer.registerMessageListener(webSocketRocketMqConsumer);
			consumer.start();
			log.info("websocket message Consumer start successfully !");
		} catch (Exception e) {
			log.error("websocket message Consumer start Failed !");
			log.error("", e);
		}
	}
}
