package cn.wulin.netty.multi.port.client;

import cn.wulin.netty.multi.port.domain.CompleteData;
import io.netty.channel.ChannelHandlerContext;

public interface ClientDataHandler {

	public int dataType();
	
	public void execute(ChannelHandlerContext ctx, CompleteData data) throws Exception ;
}
