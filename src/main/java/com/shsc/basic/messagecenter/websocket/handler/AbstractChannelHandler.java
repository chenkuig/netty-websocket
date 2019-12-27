package com.shsc.basic.messagecenter.websocket.handler;


import com.shsc.basic.messagecenter.websocket.config.NettyConfiguration;
import com.shsc.basic.messagecenter.websocket.manager.IChannelManageService;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public abstract class AbstractChannelHandler extends SimpleChannelInboundHandler<Object> implements INettyDoRequest {
	protected IChannelManageService channelManageService;
	protected NettyConfiguration config;
	AbstractChannelHandler(IChannelManageService channelManageService, NettyConfiguration config){
		this.channelManageService = channelManageService;
		this.config = config;
	};
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof WebSocketFrame) {
			if (msg instanceof TextWebSocketFrame){
				textdoMessage(ctx, (TextWebSocketFrame) msg);
	        }else if (msg instanceof BinaryWebSocketFrame) {
            	binarydoMessage(ctx, (BinaryWebSocketFrame) msg);
            }else if (msg instanceof CloseWebSocketFrame) {
            	closedoMessage(ctx, (CloseWebSocketFrame) msg);
            }else if (msg instanceof PingWebSocketFrame) {
            	pingdoMessage(ctx, (PingWebSocketFrame) msg);
            }else {
            	webSocketotherdoMessage(ctx, (WebSocketFrame) msg);
            }
        }else if (msg instanceof FullHttpRequest){
        	httpdoMessage(ctx, (FullHttpRequest) msg);
        }else {
        	unSupportdoMessage(ctx, msg);
        }
	}
}
