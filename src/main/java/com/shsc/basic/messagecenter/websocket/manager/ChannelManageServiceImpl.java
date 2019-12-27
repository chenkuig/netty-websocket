package com.shsc.basic.messagecenter.websocket.manager;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Component;

import com.shsc.basic.messagecenter.websocket.config.NettyConfiguration;
import com.shsc.basic.messagecenter.websocket.info.ChannelDistributeRegisterInfo;

import io.netty.channel.Channel;
@Component
public class ChannelManageServiceImpl extends ApplicationObjectSupport  implements IChannelManageService , SmartInitializingSingleton{
	private boolean isDistributed = false;
	@SuppressWarnings("unused")
	private int port = 8090;
	@Resource
	private IDistributeManageService distributeManageService;
	@Override
	public void register(Channel channel, String primary) {
			GROUP.add(channel);
			HANNEL_STORE.put(primary, channel);
			ADDRESS_STORE.put(channel.remoteAddress().toString(), primary);
			if (isDistributed) {
				distributeManageService.register(channel, primary);
			}
	}

	@Override
	public boolean hasPrimary(String primary) {
		if (!isDistributed) {
			return HANNEL_STORE.containsKey(primary);
		}else {
			boolean isLocal = HANNEL_STORE.containsKey(primary);
			if (isLocal) {
				return isLocal;
			}
			distributeManageService.hasPrimary(primary);
		}
		return false;
	}

	@Override
	public Channel getLocalChannel(String primary) {
		return HANNEL_STORE.get(primary);
	}
	
	@Override
	public void close(Channel channel) {
		String remoteAddress = channel.remoteAddress().toString();
		String primary = ADDRESS_STORE.get(remoteAddress);
		if (StringUtils.isBlank(primary)) {
			channel.close();
			return ;
		}
		HANNEL_STORE.remove(primary);
		ADDRESS_STORE.remove(remoteAddress);
		if (isDistributed) {
			distributeManageService.remove(primary);
		}
		channel.close();
	}
	
	@Override
	public ChannelDistributeRegisterInfo getDistributedChannelInfo(String primary) {
		return distributeManageService.getDistributedChannelInfo(primary);
	}
	
	@Override
	public void afterSingletonsInstantiated() {
		ApplicationContext context = getApplicationContext();
	     if (context != null) {
	    	 NettyConfiguration config =  context.getBean(NettyConfiguration.class);
	    	 isDistributed = config.getIsDistributed();
	    	 port = config.getWebport();
	     }
	}

	@Override
	public int getConnectionSize() {
		if (!isDistributed) {
			return HANNEL_STORE.size();
		}else {
			return distributeManageService.getConnectionSize();
		}
	}
}
