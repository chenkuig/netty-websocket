package com.shsc.basic.messagecenter.websocket.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.shsc.basic.messagecenter.websocket.info.ChannelDistributeRegisterInfo;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public interface IChannelManageService {
	public Map<String, Channel> HANNEL_STORE = new ConcurrentHashMap<String, Channel>();
	public Map<String, String> ADDRESS_STORE = new ConcurrentHashMap<String, String>();
	public static ChannelGroup GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    void register(Channel channel, String primary);

    boolean hasPrimary(String primary);

    Channel getLocalChannel(String primary);

    void close(Channel channel);
    
    ChannelDistributeRegisterInfo getDistributedChannelInfo (String primary);
    
    int getConnectionSize();
}
