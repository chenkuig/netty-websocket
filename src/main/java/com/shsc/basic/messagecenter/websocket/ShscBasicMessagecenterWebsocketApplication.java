package com.shsc.basic.messagecenter.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients({"com.shsc.basic.messagecenter.websocket.feign"})
public class ShscBasicMessagecenterWebsocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShscBasicMessagecenterWebsocketApplication.class, args);
		//websocket.start
		InitWebSocketServer.start();
	}
}
