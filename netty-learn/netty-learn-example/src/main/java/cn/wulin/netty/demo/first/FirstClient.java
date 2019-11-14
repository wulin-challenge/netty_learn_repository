package cn.wulin.netty.demo.first;

import java.util.concurrent.CountDownLatch;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class FirstClient {
	Bootstrap b;
	
	public static void main(String[] args) {
		FirstClient firstClient = new FirstClient();
		firstClient.client();
		firstClient.send();
	}
	
	public void client(){
		b = new Bootstrap();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		b.group(workerGroup)
		 .channel(NioSocketChannel.class)
		 .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new StringEncoder());
                socketChannel.pipeline().addLast(new StringDecoder());
                socketChannel.pipeline().addLast(new FirstClientHandler(FirstClient.this));
            }
         });
	}
	
	public void send() {
		Channel channel = getChannel();
		
		channel.writeAndFlush("你好!");
	}
	
	public Channel getChannel() {
		return getChannel("wulinThinkPad", 22345);
	}
	
	public Channel getChannel(String host,int port) {
		ChannelFuture channelFuture = b.connect(host, port);
		
		CountDownLatch count = new CountDownLatch(1);
		channelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture channelFuture) throws Exception {
				if (channelFuture.isSuccess()) {
					System.out.println("连接成功");
					count.countDown();
				}else{
					System.out.println("连接失败");
					System.exit(1);
				}
			}
		});
		try {
			count.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Channel channel = channelFuture.channel();
		return channel;
	}

}
