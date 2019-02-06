package cn.wulin.heartbeat.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class HeartBeatClient {
	
	public static void main(String[] args) {
		new HeartBeatClient().client("wulinThinkPad", 12345, 5L);
	}
	
	public void client(String host, int port,Long idleTime){
		CountDownLatch count = new CountDownLatch(1);
		Bootstrap b = new Bootstrap();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		b.group(workerGroup)
		 .channel(NioSocketChannel.class)
		 .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new IdleStateHandler(0,idleTime,0, TimeUnit.SECONDS));
                socketChannel.pipeline().addLast(new StringEncoder());
                socketChannel.pipeline().addLast(new HeartBeatClientHandler(idleTime,host,port,b));
            }
         });
		
		ChannelFuture channelFuture = b.connect(host, port);
		channelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture channelFuture) throws Exception {
				if (channelFuture.isSuccess()) {
					HeartBeatClientHandler handler = channelFuture.channel().pipeline().get(HeartBeatClientHandler.class);
//					cacheConnect.put(host + "_" + port, handler);
					System.out.println("连接成功");
				}else{
					System.out.println("连接失败");
					System.exit(1);
				}
				count.countDown();
			}
		});
		try {
			count.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
