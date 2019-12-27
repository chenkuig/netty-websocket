package com.shsc.basic.messagecenter.websocket.service;

import java.util.Set;

import com.shsc.basic.messagecenter.websocket.info.MessageInfo;

public interface IHttpRequestService {
	public boolean sendMessage (MessageInfo messageInfo) ;
	/**
	 * get  the number of builded connection 
	 * @return
	 */
	public int getConnectionSize();
	/**
	 * get the url data of registered websocket server
	 * @return
	 */
	Set<String> getWebSocketServerUrlCollection();
}
