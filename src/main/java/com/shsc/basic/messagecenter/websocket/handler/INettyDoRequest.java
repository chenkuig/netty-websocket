package com.shsc.basic.messagecenter.websocket.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public interface INettyDoRequest {
	public void textdoMessage(ChannelHandlerContext ctx, TextWebSocketFrame msg);
	
	public  void binarydoMessage(ChannelHandlerContext ctx, BinaryWebSocketFrame msg);
	
	public void httpdoMessage(ChannelHandlerContext ctx, FullHttpRequest msg);
	
	public void closedoMessage(ChannelHandlerContext ctx, CloseWebSocketFrame msg);
	
	public void pingdoMessage(ChannelHandlerContext ctx, PingWebSocketFrame msg);
	
	public void webSocketotherdoMessage(ChannelHandlerContext ctx, WebSocketFrame msg);
	
	public void unSupportdoMessage(ChannelHandlerContext ctx,  Object msg);
	
}
