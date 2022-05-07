package cn.wulin.netty.multi.port;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wulin.netty.multi.port.biz.ClientProperties;
import cn.wulin.netty.multi.port.codec.DataDecoder;
import cn.wulin.netty.multi.port.codec.DataEncoder;
import cn.wulin.netty.multi.port.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 数据中转服务端
 * @author wulin
 *
 */
public class Server {
	private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
	private static int port = ClientProperties.getClientProperties().getServerPort();
	public static void main(String[] args) {
		new Server().server();
		
		System.out.println("服务端启动成功!");
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
                socketChannel.pipeline().addLast(new DataEncoder());
                socketChannel.pipeline().addLast(new DataDecoder());
                socketChannel.pipeline().addLast(new ServerHandler());
            }
         });
		 try {
			ChannelFuture sync = b.bind().sync();
			 InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();
	         return addr.getPort();
		} catch (Exception e) {
			LOGGER.error("服务端启动失败!",e);
			return null;
		}
	}
	

}
