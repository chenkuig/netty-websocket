package com.shsc.basic.messagecenter.websocket.handler;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.shsc.basic.messagecenter.websocket.config.NettyConfiguration;
import com.shsc.basic.messagecenter.websocket.constant.HttpConstant;
import com.shsc.basic.messagecenter.websocket.info.MessageInfo;
import com.shsc.basic.messagecenter.websocket.manager.IChannelManageService;
import com.shsc.basic.messagecenter.websocket.utils.HttpRequestUtils;
import com.shsc.basic.messagecenter.websocket.utils.IpUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ChannelHandlerImpl extends AbstractChannelHandler{
	private WebSocketServerHandshaker handshaker;
	private static String REQUEST_CONNECT_PREFIX ="";
	public ChannelHandlerImpl(IChannelManageService channelManageService, NettyConfiguration config) {
		super(channelManageService, config);
		ChannelHandlerImpl.REQUEST_CONNECT_PREFIX= config.getWebSocketPath()+"?"+HttpConstant.IDENTIFY_PRIMARY;
	}
	@Override
	public void textdoMessage(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
		//ctx.channel().writeAndFlush(new TextWebSocketFrame("========")); 
		return ;
	}

	@Override
	public void httpdoMessage(ChannelHandlerContext ctx, FullHttpRequest msg) {
		String uri = msg.uri();
		try {
			if (StringUtils.isBlank(uri)) {
				channelManageService.close(ctx.channel());
				return ;
			}
			log.info("remoteAddress{} do http request uri : {}:,{}",ctx.channel().remoteAddress().toString(), uri);
			if (uri.startsWith(ChannelHandlerImpl.REQUEST_CONNECT_PREFIX)) {
				log.info("ip : {}, will build a connection", ctx.channel().remoteAddress());
				buildShakerHttpRequest(ctx, msg);
				Map<String,String> requestParamterMap =  HttpRequestUtils.getUrlParamsByGetMethod(uri);
				super.channelManageService.register(ctx.channel(),  requestParamterMap.get(HttpConstant.IDENTIFY_PRIMARY) );
			}else if (uri.endsWith(HttpConstant.SEND_MESSAGE)) {
		         String content = msg.content().toString(CharsetUtil.UTF_8); 
		         if (StringUtils.isBlank(content)) {
		        	  HttpRequestUtils. httpRequestResponse(ctx.channel(), false);
		        	  return ;
		         }
		         boolean isWriteClientSuccessFlag = false;
		         MessageInfo messageInfo = JSON.parseObject(content, MessageInfo.class);
		         Channel channel = channelManageService.getLocalChannel(messageInfo.getPrimary());
		         if (channel != null) {
		        	 if (StringUtils.isNotBlank(messageInfo.getMessage())) {
		        		 channel.writeAndFlush(new TextWebSocketFrame(messageInfo.getMessage()));
		        		 isWriteClientSuccessFlag =  true;
		        	 }
		         }
		         if (!isWriteClientSuccessFlag) {
		        	 HttpRequestUtils. httpRequestResponse(ctx.channel(), false);
		         }else {
		        	 HttpRequestUtils. httpRequestResponse(ctx.channel(), true);
		         }
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	 
	@Override
	public void binarydoMessage(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) {
		return ;
	}
	@Override
	public void closedoMessage(ChannelHandlerContext ctx, CloseWebSocketFrame msg) {
		log.info("connection ip : {}, will be closed", ctx.channel().remoteAddress());
		channelManageService.close(ctx.channel());
		handshaker.close(ctx.channel(), (CloseWebSocketFrame) msg.retain());
	}
	@Override
	public void pingdoMessage(ChannelHandlerContext ctx, PingWebSocketFrame msg) {
		ctx.channel().write(new PongWebSocketFrame(msg.content().retain()));
	}
	@Override
	public void webSocketotherdoMessage(ChannelHandlerContext ctx, WebSocketFrame msg) {
		if (msg instanceof PongWebSocketFrame) {
	        return;
	    }
		throw new RuntimeException("【"+msg.getClass().getName()+"】 message type is unSupported!");
	}
	@Override
	public void unSupportdoMessage(ChannelHandlerContext ctx, Object msg) {
		throw new RuntimeException("【"+msg.getClass().getName()+"】 message type is unSupported!");
	}
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("", cause);
        ctx.close();
    }
    
	@SuppressWarnings("deprecation")
	private void buildShakerHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req){
		if (!req.getDecoderResult().isSuccess() 
				|| ! ("websocket".equals(req.headers().get("Upgrade")))) {
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
			return;
		}
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				getWebSocketLocation() , null, false);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
		}else{
			handshaker.handshake(ctx.channel(), req);
		}
	}
	
	@SuppressWarnings("deprecation")
	private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res){
		if (res.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
		}
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
    private  String getWebSocketLocation() {
        String location = IpUtils.getHost() + ":" + config.getWebport()+config.getWebSocketPath();
        return "ws://" + location;
    }
}
