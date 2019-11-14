package cn.wulin.netty.demo.first;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class FirstServer {
	
	public static void main(String[] args) {
		Integer port = new FirstServer().server();
		System.out.println("服务已经启动,启动端口为: "+port);
	}
	
	public Integer server(){
		ServerBootstrap b= new ServerBootstrap();
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1);
		b.group(bossGroup,workerGroup)
		 .channel(NioServerSocketChannel.class)
	     .option(ChannelOption.SO_BACKLOG,1024)
	     .localAddress(new InetSocketAddress(22345))
		 .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new StringEncoder());
                socketChannel.pipeline().addLast(new StringDecoder());
                socketChannel.pipeline().addLast(new FirstServerHandler());
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
