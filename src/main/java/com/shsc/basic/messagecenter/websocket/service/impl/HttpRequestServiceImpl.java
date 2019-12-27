package com.shsc.basic.messagecenter.websocket.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.rocketmq.common.message.MessageConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.shsc.basic.messagecenter.websocket.constant.HttpConstant;
import com.shsc.basic.messagecenter.websocket.feign.IMessageCenterFeign;
import com.shsc.basic.messagecenter.websocket.info.ChannelDistributeRegisterInfo;
import com.shsc.basic.messagecenter.websocket.info.MessageInfo;
import com.shsc.basic.messagecenter.websocket.manager.IChannelManageService;
import com.shsc.basic.messagecenter.websocket.service.IHttpRequestService;
import com.shsc.basic.messagecenter.websocket.start.IBootstrapServer;
import com.shsc.basic.messagecenter.websocket.utils.HttpRequestUtils;
import com.shsc.framework.plugin.rocketmq.utils.MessageBuildUtil;
import com.shsc.framework.plugin.rocketmq.utils.RocketMQTemplate;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class HttpRequestServiceImpl implements IHttpRequestService{
	@Value("${mq_delay_level:3}")
	private Integer delay_level ;//default delay 10s
	@Value("${send_max_times:3}")
	private Integer send_max_times;
	@Resource
	private IChannelManageService channelManageService;
	@Resource
	private IMessageCenterFeign messageCenterFeign;
	@Autowired
	@Qualifier("shscRocketMQTemplate")
	private RocketMQTemplate rocketMQTemplate;
	@Resource
	private IBootstrapServer bootstrapServer;
	@Override
	public boolean sendMessage(MessageInfo messageInfo) {
		boolean sendSuccess =  false;
		Channel channel = channelManageService.getLocalChannel(messageInfo.getPrimary());
		try {
			if (channel != null) {
				channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageInfo)));
				sendSuccess = true;
			}else {
				ChannelDistributeRegisterInfo info = channelManageService.getDistributedChannelInfo(messageInfo.getPrimary());
				if (info !=null) {
					// send to websocket server node
					String url = HttpConstant.HTTP_PREFIX+info.getFromServiceIp()+":"+info.getFromServicPort()+HttpConstant.SEND_MESSAGE;
					HttpRequestUtils.sendPostRequest(url,  JSON.toJSONString(messageInfo));
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}finally {
			if (!sendSuccess && messageInfo.getTimes()<send_max_times) {
				  // when failing , store to rocketmq
				  messageInfo.setTimes(messageInfo.getTimes()+1);
				  Map<String, Object> properties = new HashMap<>();
		          properties.put(MessageConst.PROPERTY_DELAY_TIME_LEVEL, delay_level);
				  rocketMQTemplate.syncSend(MessageBuildUtil.createMessage(HttpConstant.ROCKET_MQ_MESSAGE_CENTER_WEBSOCKET_TOPIC, "", messageInfo, properties));
			}
			if (!sendSuccess && messageInfo.getTimes()==send_max_times) {
				String[]  messageIds = new String[1];
				messageIds[0] = JSON.parseObject(messageInfo.getMessage()).getString("id");
				messageCenterFeign.updateStatusInfo(messageIds, 2, true);
			}
		}
		return sendSuccess;
	}
	@Override
	public int getConnectionSize() {
		int connectionSize  = 0;
		try {
			connectionSize = channelManageService.getConnectionSize();
		} catch (Exception e) {
			log.error("", e);
		}
		return connectionSize;
	}
	@Override
	public Set<String> getWebSocketServerUrlCollection() {
		Set<String> webSocketServer = null;
		try {
			webSocketServer = bootstrapServer.getRegisteredServerUrlList();
		} catch (Exception e) {
			log.error("", e);
		}
		return webSocketServer ;
	}
}
