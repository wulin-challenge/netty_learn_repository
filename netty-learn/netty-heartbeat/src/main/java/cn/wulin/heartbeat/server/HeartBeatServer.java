package cn.wulin.heartbeat.server;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;

public class HeartBeatServer {
	
	public static void main(String[] args) {
		Integer port = new HeartBeatServer().server();
		System.out.println("服务已经启动,启动端口为: "+port);
	}
	
	public Integer server(){
		ServerBootstrap b= new ServerBootstrap();
		EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
		b.group(bossGroup,workerGroup)
		 .channel(NioServerSocketChannel.class)
	     .option(ChannelOption.SO_BACKLOG,1024)
	     .localAddress(new InetSocketAddress(12345))
		 .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
                socketChannel.pipeline().addLast(new StringDecoder());
                socketChannel.pipeline().addLast(new HeartBeatServerHandler());
            }
         });
		 try {
			ChannelFuture sync = b.bind().sync();
			 InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();
	         return addr.getPort();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
