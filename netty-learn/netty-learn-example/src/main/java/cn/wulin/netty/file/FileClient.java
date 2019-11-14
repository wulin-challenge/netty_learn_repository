package cn.wulin.netty.file;

import java.util.concurrent.CountDownLatch;

import cn.wulin.netty.demo.first.FirstClient;
import cn.wulin.netty.demo.first.FirstClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.file.FileServerHandler;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

public class FileClient {
	private Bootstrap b;
	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	
	public static void main(String[] args) {
		FileClient client = new FileClient();
		client.client();
		client.getFile();
	}
	
	
	public void client(){
		 b = new Bootstrap();
         b.group(workerGroup)
          .channel(NioSocketChannel.class)
          .option(ChannelOption.SO_BACKLOG, 100)
          .handler(new LoggingHandler(LogLevel.INFO))
          .handler(new ChannelInitializer<SocketChannel>() {
              @Override
              protected void initChannel(SocketChannel ch) throws Exception {
             	 ChannelPipeline p = ch.pipeline();
                  p.addLast(
                          new FileClientHandler()
                          );
              }
           });
		
		
	}
	
	public void getFile() {
		Channel channel = getChannel();
	}
	
	public Channel getChannel() {
		return getChannel("wulinThinkPad", FileServer.PORT);
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
