package cn.wulin.netty.demo.first;

import java.io.IOException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 客户端处理类
 * @author wulin
 *
 */
public class FirstClientHandler extends ChannelInboundHandlerAdapter{
	
	FirstClient firstClient;
	
	public FirstClientHandler(FirstClient firstClient) {
		super();
		this.firstClient = firstClient;
	}
	

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("server 对client说: "+msg);
		super.channelRead(ctx, msg);
	}




	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if(cause instanceof IOException){
			firstClient.getChannel();
		}else{
			super.exceptionCaught(ctx, cause);
		}
	}
	
	
	
	
}
