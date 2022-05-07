package cn.wulin.netty.multi.port.server;

import cn.wulin.netty.multi.port.domain.CompleteData;
import io.netty.channel.ChannelHandlerContext;

/**
 * 服务端数据处理接口
 * @author wulin
 *
 */
public interface ServerDataHandler {

	public int dataType();
	
	public void execute(ChannelHandlerContext ctx, CompleteData data) throws Exception ;
}
