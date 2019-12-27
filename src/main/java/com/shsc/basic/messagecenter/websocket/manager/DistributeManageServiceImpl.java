package com.shsc.basic.messagecenter.websocket.manager;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Component;

import com.shsc.basic.messagecenter.websocket.config.NettyConfiguration;
import com.shsc.basic.messagecenter.websocket.info.ChannelDistributeRegisterInfo;
import com.shsc.basic.messagecenter.websocket.utils.IpUtils;
import com.shsc.framework.plugin.redis.util.RedisOperateUtil;

import io.netty.channel.Channel;
@Component
public class DistributeManageServiceImpl extends ApplicationObjectSupport  implements IDistributeManageService, SmartInitializingSingleton{
	 private int port = 8090;
	 @Resource
	 private RedisOperateUtil  redisOperateUtil ;
	@Override
	public void register(Channel channel, String primary) {
		redisOperateUtil.addMap(REDIS_WEBSOCKET_CHANNLE_COLLECTION, primary, new ChannelDistributeRegisterInfo()
				.setFromServiceIp(IpUtils.getHost()).setFromServicPort(port).setPrimary(primary));
	}

	@Override
	public boolean hasPrimary(String primary) {
		Object object = redisOperateUtil.getMap(REDIS_WEBSOCKET_CHANNLE_COLLECTION, primary, ChannelDistributeRegisterInfo.class);
		if (object!=null) {
			return true;
		}
		return false;
	}

	@Override
	public ChannelDistributeRegisterInfo getDistributedChannelInfo(String primary) {
		return   redisOperateUtil.getMap(REDIS_WEBSOCKET_CHANNLE_COLLECTION, primary, ChannelDistributeRegisterInfo.class);
	}

	@Override
	public void remove(String primary) {
		redisOperateUtil.deleteMap(REDIS_WEBSOCKET_CHANNLE_COLLECTION, primary);
	}

	@Override
	public void afterSingletonsInstantiated() {
		ApplicationContext context = getApplicationContext();
	     if (context != null) {
	    	 NettyConfiguration config =  context.getBean(NettyConfiguration.class);
	    	 port = config.getWebport();
	     }
	}

	@SuppressWarnings("unchecked")
	@Override
	public int getConnectionSize() {
		Map<String, ChannelDistributeRegisterInfo> map = redisOperateUtil.getMap(REDIS_WEBSOCKET_CHANNLE_COLLECTION, Map.class);
		if (map!=null) {
			return map.size();
		}
		return 0;
	}

}
