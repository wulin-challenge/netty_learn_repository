package cn.wulin.netty.file.demo.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import cn.wulin.netty.file.demo.domain.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.stream.ChunkedFile;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理文件传输和对象的server handler
 * @author wulin
 *
 */
@Sharable
public class FileDemoServerHandler  extends ChannelDuplexHandler {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof RemotingCommand) {
			RemotingCommand command = (RemotingCommand)msg;
			dealWithCommand(ctx, command.getRequestId(), (String)command.getData());
		}
		
		//demo中暂时不会出现这个类型,当前demo只实现从client端发起一个文件地址,server端就通过这个地址获取文件流,然后发送给client端
		if(msg instanceof ByteBuf) {
			
		}
	}
	
	/**
	 * 文件传输的buffer数据结构: 
	 * <p> int : 请求Id
	 * <p> int : 是否结束的标记, 1: 表示开始传输,0:表示正在传输, -1 表示传输结束
	 * <p> long : 文件数据的总长度
	 * <p> int : 当前传输数据的长度
	 * <p> bytes: 传输的数据
	 * @param ctx
	 * @param msg
	 */
	private void dealWithCommand(ChannelHandlerContext ctx, int requestId,String path) {
		try {
			ctx.write(new FileDemoChunkedFile(new File(path),1024*8,requestId));
//			ctx.write(new FileDemoChunkedFile(new File(path),512,requestId));
			ctx.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	AtomicInteger count2 = new AtomicInteger();

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("server出现了异常!");
		cause.printStackTrace();
	}
	
	

}
