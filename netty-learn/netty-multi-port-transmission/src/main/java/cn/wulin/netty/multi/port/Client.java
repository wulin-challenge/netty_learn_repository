package cn.wulin.netty.multi.port;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wulin.netty.multi.port.biz.ClientProperties;
import cn.wulin.netty.multi.port.codec.DataDecoder;
import cn.wulin.netty.multi.port.codec.DataEncoder;
import cn.wulin.netty.multi.port.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 数据发送客户端
 * @author wulin
 *
 */
public class Client {
	private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
	Bootstrap b;
	
	private ChannelDuplexHandler handler;
	
	public Client(ChannelDuplexHandler handler) {
		super();
		this.handler = handler;
	}

	public void client(){
		b = new Bootstrap();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		b.group(workerGroup)
		 .channel(NioSocketChannel.class)
		 .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new DataEncoder());
                socketChannel.pipeline().addLast(new DataDecoder());
                socketChannel.pipeline().addLast(handler);
            }
         });
	}
	
	public Channel getChannel() {
		ClientProperties clientProperties = ClientProperties.getClientProperties();
		Channel channel = getChannel(clientProperties.getServerIp(), clientProperties.getServerPort());
		while(true){
			if(channel.isActive() && channel.isOpen() && channel.isWritable()){
				return channel;
			}
			channel = getChannel(clientProperties.getServerIp(), clientProperties.getServerPort());
		}
	}
	
	public String getRequestId() {
		return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
	}
	
	public Channel getChannel(String host,int port) {
		ChannelFuture channelFuture = b.connect(host, port);
		
		CountDownLatch count = new CountDownLatch(1);
		channelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture channelFuture) throws Exception {
				if (channelFuture.isSuccess()) {
					LOGGER.info("连接成功");
					count.countDown();
				}else{
					LOGGER.info("连接失败");
					System.exit(1);
				}
			}
		});
		try {
			count.await();
		} catch (Exception e) {
			LOGGER.error("连接失败了",e);
		}
		Channel channel = channelFuture.channel();
		return channel;
	}
	
	
	

}
