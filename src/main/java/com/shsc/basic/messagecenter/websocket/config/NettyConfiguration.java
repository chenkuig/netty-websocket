package com.shsc.basic.messagecenter.websocket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix="netty.config")
public class NettyConfiguration {
    private int webport = 8090;

    private int bossThread = 1;

    private int workerThread = 2;

    private boolean keepalive = true;

    private int backlog = 1024;

    private boolean nodelay = true;

    private boolean reuseaddr = true;

    private  int  sndbuf = 10485760;

    private int revbuf = 10485760;

    private int heart = 180;

    private int period = 10;

    private int initalDelay = 10;

    private int maxContext = 65536;

    private String webSocketPath = "/ws";

    private Boolean isDistributed = false;
}
