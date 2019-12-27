package com.shsc.basic.messagecenter.websocket.start;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * @author chenkui
 */
public interface IBootstrapServer {
	public static String REDIS_WEBSOCKET_SERVER_COLLECTION = "REDIS_WEBSOCKET_SERVER_COLLECTION";
	public static Set<String> WEBSOCKET_SERVER_SET = Sets.newConcurrentHashSet();
    void shutdown();

    void start()throws InterruptedException;
    
    void registerServerUrlList();
    
    Set<String> getRegisteredServerUrlList();
}
