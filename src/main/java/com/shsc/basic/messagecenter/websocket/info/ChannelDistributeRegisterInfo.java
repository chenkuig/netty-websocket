package com.shsc.basic.messagecenter.websocket.info;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ChannelDistributeRegisterInfo {
	private String fromServiceIp;
	private Integer fromServicPort;
	private String primary;
}
