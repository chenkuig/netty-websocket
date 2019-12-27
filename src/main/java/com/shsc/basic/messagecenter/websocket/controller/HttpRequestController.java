package com.shsc.basic.messagecenter.websocket.controller;

import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shsc.basic.messagecenter.websocket.info.MessageInfo;
import com.shsc.basic.messagecenter.websocket.service.IHttpRequestService;
import com.shsc.framework.common.model.ResponseEntity;
import com.shsc.framework.common.model.ResponseHelper;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/websocket")
@Slf4j
public class HttpRequestController {
	@Resource
	private IHttpRequestService httpRequestService;
	/**
	 * http send message
	 * @param primary
	 * @param message
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@PostMapping("/sendMessage")
	public ResponseEntity<String> sendMessage (@RequestBody MessageInfo messageInfo) {
		boolean isSendSuccess =   false;
		try {
			if (StringUtils.isBlank(messageInfo.getPrimary()) || StringUtils.isBlank(messageInfo.getMessage())) {
				return ResponseHelper.fail(String.valueOf(isSendSuccess));
			}
			isSendSuccess =  httpRequestService.sendMessage(messageInfo);
			return ResponseHelper.success(String.valueOf(isSendSuccess));
		} catch (Exception e) {
			log.error("" , e);
		}
		return ResponseHelper.fail(String.valueOf(isSendSuccess));
	}
	/**
	 * 
	 * @return
	 */
	@PostMapping("/getConnectionSize")
	public ResponseEntity<Integer>  getConnectionSize(){
		return ResponseHelper.success(httpRequestService.getConnectionSize());
	}
	
	@PostMapping("/getWebSocketServerUrlCollection")
	public ResponseEntity<Set<String>> getWebSocketServerUrlCollection(){
		return ResponseHelper.success(httpRequestService.getWebSocketServerUrlCollection());
	}
}
