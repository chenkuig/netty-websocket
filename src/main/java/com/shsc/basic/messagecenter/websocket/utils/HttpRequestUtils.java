package com.shsc.basic.messagecenter.websocket.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
@Slf4j
public class HttpRequestUtils {
	public static Map<String,String> getUrlParamsByGetMethod(String url){
        Map<String,String> map = new HashMap<>();
        url = url.replace("?",";");
        if (!url.contains(";")){
            return map;
        }
        if (url.split(";").length > 0){
            String[] arr = url.split(";")[1].split("&");
            for (String s : arr){
                String key = s.split("=")[0];
                String value = s.split("=")[1];
                map.put(key,value);
            }
            return  map;
        }else{
            return map;
        }
    }
	public static  boolean sendPostRequest(String url, String requestBody) {
		boolean sendSuccessFlag = false;
    	try {
    		 if (StringUtils.isEmpty(requestBody)) {
    			 return  false;
    		 } 
    		 RequestBody body=RequestBody.create(MediaType.parse("application/json;"), requestBody);
    	     OkHttpClient okHttpClient=new OkHttpClient().newBuilder()
    	    	        .connectTimeout(20, TimeUnit.SECONDS)//设置连接超时时间
    	    	        .readTimeout(50, TimeUnit.SECONDS)//设置读取超时时间
    	    	        .build();
    	     Request request=new Request.Builder()
    	        		.url(url)
    	        		.post(body)
    	        		.build();
    	     Response response=okHttpClient.newCall(request).execute();
    	      sendSuccessFlag = true;
		} catch (Exception e) {
			log.error("", e);
		}
    	return sendSuccessFlag;
    }
	
	public  static void  httpRequestResponse(Channel channel, boolean isSuccess) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpConstant.CONTENT_TYPE, HttpConstant.APPLICATION_JSON);
        ByteBuf buf = Unpooled.copiedBuffer(String.valueOf(isSuccess), CharsetUtil.UTF_8);
        response.content().writeBytes(buf);
        channel.writeAndFlush(response);
        channel.close();
    }
}
