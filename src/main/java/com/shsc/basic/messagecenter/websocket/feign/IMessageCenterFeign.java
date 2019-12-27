package com.shsc.basic.messagecenter.websocket.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shsc.framework.common.model.ResponseEntity;

@FeignClient("shsc-basic-message-center")
public interface IMessageCenterFeign {
	@PostMapping("/message/updateStatus")
	ResponseEntity<String> updateStatusInfo (@RequestParam("messageIds") String[] messageIds 
			, @RequestParam("status") int status
			,@RequestParam("isCallBack") boolean isCallBack);
}
