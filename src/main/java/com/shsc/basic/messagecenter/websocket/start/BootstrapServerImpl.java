package com.shsc.basic.messagecenter.websocket.start;

import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.shsc.basic.messagecenter.websocket.config.NettyConfiguration;
import com.shsc.basic.messagecenter.websocket.constant.BootstrapConstant;
import com.shsc.basic.messagecenter.websocket.handler.ChannelHandlerImpl;
import com.shsc.basic.messagecenter.websocket.manager.IChannelManageService;
import com.shsc.basic.messagecenter.websocket.utils.IpUtils;
import com.shsc.basic.messagecenter.websocket.utils.RemotingUtil;
import com.shsc.framework.plugin.redis.util.RedisOperateUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@EnableConfigurationProperties(NettyConfiguration.class)
@Component
public class BootstrapServerImpl implements IBootstrapServer{
	
	@Resource
    private NettyConfiguration config;
	@Resource
	private IChannelManageService channelManageService;
    
    private EventLoopGroup bossGroup;

    private EventLoopGroup workGroup;

    ServerBootstrap bootstrap = null;

    Object waitLock = new Object(); //lock 
    
    @Resource
	 private RedisOperateUtil  redisOperateUtil ;
    
	@Override
    public void start() throws InterruptedException {
    	synchronized (waitLock) {
            initEventPool();
            bootstrap.group(bossGroup, workGroup)
                    .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .option(ChannelOption.SO_REUSEADDR, config.isReuseaddr())
                    .option(ChannelOption.SO_BACKLOG, config.getBacklog())
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_RCVBUF, config.getRevbuf())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) throws Exception {
                        	  ChannelPipeline pipeline = ch.pipeline();
                              intProtocolHandler(pipeline);
                              pipeline.addLast(new ChannelHandlerImpl(channelManageService, config));
                        }
                    })
                    .childOption(ChannelOption.TCP_NODELAY, config.isNodelay())
                    .childOption(ChannelOption.SO_KEEPALIVE, config.isKeepalive())
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            		bootstrap.bind(IpUtils.getHost(), config.getWebport()).addListener((ChannelFutureListener) channelFuture -> {
                if (channelFuture.isSuccess()) {
                	//register server
                	registerServerUrlList();
                    log.info("websocket服务端启动成功【" + IpUtils.getHost() + ":" + config.getWebport() + "】");
                } else {
                    log.info("websocket服务端启动失败【" + IpUtils.getHost() + ":" + config.getWebport() + "】");
                }
            });
        }
    }
    
    private  void intProtocolHandler(ChannelPipeline channelPipeline){
        channelPipeline.addLast(BootstrapConstant.HTTPCODE,new HttpServerCodec());
        channelPipeline.addLast(BootstrapConstant.AGGREGATOR, new HttpObjectAggregator(config.getMaxContext()));
        channelPipeline.addLast(BootstrapConstant.CHUNKEDWRITE,new ChunkedWriteHandler());
        channelPipeline.addLast(new IdleStateHandler(config.getHeart(),0,0));
    }
    
    private void initEventPool() {
        bootstrap = new ServerBootstrap();
        if (useEpoll()) {
            bossGroup = new EpollEventLoopGroup(config.getBossThread(), new ThreadFactory() {
                private AtomicInteger index = new AtomicInteger(0);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "LINUX_BOSS_" + index.incrementAndGet());
                }
            });
            workGroup = new EpollEventLoopGroup(config.getWorkerThread(), new ThreadFactory() {
                private AtomicInteger index = new AtomicInteger(0);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "LINUX_WORK_" + index.incrementAndGet());
                }
            });

        } else {
            bossGroup = new NioEventLoopGroup(config.getBossThread(), new ThreadFactory() {
                private AtomicInteger index = new AtomicInteger(0);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "BOSS_" + index.incrementAndGet());
                }
            });
            workGroup = new NioEventLoopGroup(config.getWorkerThread(), new ThreadFactory() {
                private AtomicInteger index = new AtomicInteger(0);
                public Thread newThread(Runnable r) {
                    return new Thread(r, "WORK_" + index.incrementAndGet());
                }
            });
        }
    }
	@Override
    public void shutdown() {
        synchronized (waitLock) {
            if (workGroup != null && bossGroup != null) {
                try {
                    bossGroup.shutdownGracefully().sync();
                    workGroup.shutdownGracefully().sync();
                    // remove register url
            		destory();
                } catch (InterruptedException e) {
                    log.error("服务端关闭资源失败【" + IpUtils.getHost() + ":" + config.getWebport() + "】");
                }
            }
        }
    }
    private boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

	@Override
	public void registerServerUrlList() {
		if (config.getIsDistributed()) {
			  redisOperateUtil.addSet(REDIS_WEBSOCKET_SERVER_COLLECTION, getWebSocketUrl());
		}else {
			WEBSOCKET_SERVER_SET.add(getWebSocketUrl());
		}
	}

	@Override
	public Set<String> getRegisteredServerUrlList() {
		if (config.getIsDistributed()) {
			return redisOperateUtil.getAllSet(REDIS_WEBSOCKET_SERVER_COLLECTION);
		}else {
			return WEBSOCKET_SERVER_SET;
		}
	}
	
	@PreDestroy
    public void destory() {
		try {
			if (config.getIsDistributed()) {
				String url = getWebSocketUrl();
				log.info("websocket of url {} is stoped ", url);
				redisOperateUtil.deleteSet(REDIS_WEBSOCKET_SERVER_COLLECTION, url);
			}
		} catch (Exception e) {
			log.error("", e);
		}
    }
	/**
	 * websocket url 
	 * @return
	 */
	private String getWebSocketUrl () {
		String location = IpUtils.getHost() + ":" + config.getWebport()+config.getWebSocketPath(); 
		return "ws://" + location;
	}
}
