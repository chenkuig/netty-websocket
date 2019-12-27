package com.shsc.basic.messagecenter.websocket.manager;

import com.shsc.basic.messagecenter.websocket.info.ChannelDistributeRegisterInfo;

import io.netty.channel.Channel;

/**
 * 
 * @author chenkui
 */
public interface IDistributeManageService {
	
    public final static String REDIS_WEBSOCKET_CHANNLE_COLLECTION = "REDIS_WEBSOCKET_CHANNLE_COLLECTION";
	
    public void register(Channel channel, String primary) ;
	
	public boolean hasPrimary(String primary);
	
	public ChannelDistributeRegisterInfo getDistributedChannelInfo(String primary);
	
	public void remove(String primary) ;
	
	public int getConnectionSize();
}
