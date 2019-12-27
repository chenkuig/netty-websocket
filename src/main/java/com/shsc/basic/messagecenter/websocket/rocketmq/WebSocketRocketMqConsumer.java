package com.shsc.basic.messagecenter.websocket.rocketmq;

import java.util.List;

import javax.annotation.Resource;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.shsc.basic.messagecenter.websocket.info.MessageInfo;
import com.shsc.basic.messagecenter.websocket.service.IHttpRequestService;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class WebSocketRocketMqConsumer implements MessageListenerConcurrently{
	@Resource
	private IHttpRequestService httpRequestService;
	@Override
	public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
			 if (CollectionUtils.isEmpty(msgs)) {
				 return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			 }else {
				 msgs.stream().forEach(messageExt ->{
					 try {
							 MessageInfo messageInfo =  JSON.parseObject(new String(messageExt.getBody(),"UTF-8"), MessageInfo.class);
							 if (messageInfo !=null ) {
								 httpRequestService.sendMessage(messageInfo) ;
							 }
					   } catch (Exception e) {
						   	log.error("", e);
					 }});
			 }
			 return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
	}
}
