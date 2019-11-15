package cn.wulin.netty.file.demo;

import java.net.InetSocketAddress;

import cn.wulin.netty.file.demo.codec.FileDemoDecoder;
import cn.wulin.netty.file.demo.codec.FileDemoEncoder;
import cn.wulin.netty.file.demo.domain.RemotingCommand;
import cn.wulin.netty.file.demo.handler.FileDemoServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 参考资料: 
 * <p> https://github.com/wulin-challenge/itstack-demo-netty
 * <p> github.com/wulin-challenge/fileserver
 * <p> 未来可能参考的资料: https://github.com/wulin-challenge/netty_fileserver
 * <p> 权威指南2的源码: https://github.com/wulin-challenge/nettybook2
 * @author wulin
 *
 */
public class FileDemoServer {
	
	int port = 8765;
	
	public static void main(String[] args) {
		Integer port = new FileDemoServer().server();
		System.out.println("服务已经启动,启动端口为: "+port);
	}
	
	public Integer server(){
		ServerBootstrap b= new ServerBootstrap();
		EventLoopGroup bossGroup = new NioEventLoopGroup(16);
        EventLoopGroup workerGroup = new NioEventLoopGroup(16);
		b.group(bossGroup,workerGroup)
		 .channel(NioServerSocketChannel.class)
	     .option(ChannelOption.SO_BACKLOG,1024)
	     .localAddress(new InetSocketAddress(port))
		 .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new FileDemoEncoder(RemotingCommand.class));
                socketChannel.pipeline().addLast(new FileDemoDecoder(RemotingCommand.class));
                socketChannel.pipeline().addLast(new ChunkedWriteHandler());
                socketChannel.pipeline().addLast(new FileDemoServerHandler());
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
