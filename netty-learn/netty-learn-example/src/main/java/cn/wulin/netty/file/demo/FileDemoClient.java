package cn.wulin.netty.file.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import cn.wulin.netty.file.demo.codec.FileDemoDecoder;
import cn.wulin.netty.file.demo.codec.FileDemoEncoder;
import cn.wulin.netty.file.demo.common.FileDemoInputStream;
import cn.wulin.netty.file.demo.domain.RemotingCommand;
import cn.wulin.netty.file.demo.handler.FileDemoClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class FileDemoClient {

	private Bootstrap b;
	public FileDemoClientHandler clinetHandler;
	
	public static void main(String[] args) {
		FileDemoClient fileDemoClient = new FileDemoClient();
		fileDemoClient.client();
		fileDemoClient.readFile();
	}
	
	public void client(){
		clinetHandler = new FileDemoClientHandler();
		
		b = new Bootstrap();
		EventLoopGroup workerGroup = new NioEventLoopGroup(16);
		b.group(workerGroup)
		 .channel(NioSocketChannel.class)
		 .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
            	 socketChannel.pipeline().addLast(new FileDemoEncoder(RemotingCommand.class));
                 socketChannel.pipeline().addLast(new FileDemoDecoder(RemotingCommand.class));
                socketChannel.pipeline().addLast(clinetHandler);
            }
         });
	}
	
	public void readFile() {
		Channel channel = getChannel();
		try {
//			RemotingCommand command = new RemotingCommand(1,1,0,"F:/resources/temp/temp4/clientData/1.doc");
//			RemotingCommand command = new RemotingCommand(1,1,0,"F:/resources/temp/temp4/clientData/2.txt");
//			RemotingCommand command = new RemotingCommand(1,1,0,"F:/resources/temp/temp4/clientData/2.zip");
//			RemotingCommand command = new RemotingCommand(1,1,0,"F:/resources/temp/temp4/clientData/m1.zip");
//			RemotingCommand command = new RemotingCommand(1,1,0,"F:/resources/temp/temp4/clientData/x1.zip");
//			RemotingCommand command = new RemotingCommand(1,1,0,"F:/resources/temp/temp4/clientData/2.zip");
			RemotingCommand command = new RemotingCommand(1,1,0,"F:/resources/temp/temp4/clientData/11.zip");
			
			
			
			FileDemoInputStream is = new FileDemoInputStream(channel,command);
//			File file = new File("F:/resources/temp/temp4/clientData","2.doc");
//			File file = new File("F:/resources/temp/temp4/clientData","3.txt");
//			File file = new File("F:/resources/temp/temp4/clientData","3.zip");
//			File file = new File("F:/resources/temp/temp4/clientData","m2.zip");
//			File file = new File("F:/resources/temp/temp4/clientData","x2.zip");
			File file = new File("F:/resources/temp/temp4/clientData","11.zip");
//			File file = new File("F:/resources/temp/temp4/clientData","3.zip");
			
			FileDemoClientHandler.fileDemoMap.put(1,is);
			if(!file.exists()) {
				file.createNewFile();
			}
			
			//采用apache的工具包输出
//			FileUtils.copyInputStreamToFile(is, file);
			
			//自己写的输出
			outputFile(is, file);
			
			System.out.println("--------------------ok 已经完成了--------------------------------------");
			//退出系统
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 输出文件
	 * @param is
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void outputFile(FileDemoInputStream is, File file) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(file);
		
		
		byte[] b = new byte[1024*4];
//			byte[] b = new byte[12];
		int len = 0;

		long start = System.currentTimeMillis();
		
		while((len = is.read(b))>0) {
			fos.write(b,0,len);
		}
		
		long end = System.currentTimeMillis();
		System.out.println("总共时间:"+((end-start)/1000));
		
		fos.close();
		is.close();
	}
	
	public Channel getChannel() {
		return getChannel("wulinThinkPad", 8765);
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
