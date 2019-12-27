package com.shsc.basic.messagecenter.websocket.info;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MessageInfo {
	private String primary;
	private String message;
	private int times = 0;
}
