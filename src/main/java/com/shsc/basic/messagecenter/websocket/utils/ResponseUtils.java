package com.shsc.basic.messagecenter.websocket.utils;

import org.apache.commons.lang3.StringUtils;

import com.shsc.basic.messagecenter.websocket.constant.HttpConstant;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP响应
 * @author chenkui
 */
@Slf4j
public class ResponseUtils {
	public static boolean  response (Channel channel, String message) {
		boolean flag = true;
		try {
			if (channel!=null && StringUtils.isBlank(message)) {
				 FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
			     response.headers().set(HttpConstant.CONTENT_TYPE,HttpConstant.APPLICATION_JSON);
			     ByteBuf buf = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
			     response.content().writeBytes(buf);
			     channel.writeAndFlush(response);
			}else {
				flag = false;
			}
		} catch (Exception e) {
			log.error("", e);
			flag = false;
		}finally {
			if (channel!=null) {
				 channel.close();
			}
		}
		return flag;
    }
}
