package cn.wulin.netty.file.demo.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import cn.wulin.netty.file.demo.common.FileDemoInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理文件传输和对象的client handler
 * @author wulin
 *
 */
public class FileDemoClientHandler extends ChannelDuplexHandler {
	public static final ConcurrentHashMap<Integer,FileDemoInputStream> fileDemoMap = new ConcurrentHashMap<>();

	/**
	 * 添加byte
	 * 文件传输的buffer数据结构: 
	 * <p> int : 请求Id
	 * <p> int : 是否结束的标记, 0:表示正在传输, -1 表示传输结束
	 * <p> long : 文件数据的总长度
	 * <p> int : 当前传输数据的长度(包含当前传输的数据块的传输顺序序号)
	 * <p> long : 当前传输的数据块的传输顺序序号
	 * <p> bytes: 传输的数据
	 * 
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		if(msg instanceof ByteBuf) {
			ByteBuf buffer = (ByteBuf)msg;
			
			int requestId = buffer.readInt();
			FileDemoInputStream fileDemoInputStream = fileDemoMap.get(requestId);
			if(fileDemoInputStream != null) {
				fileDemoInputStream.addByteBuf(buffer);
			}else {
				System.out.println("没哟要传输的文件流!");
			}
		}
	}
	
	List<ByteBuf> tem = new ArrayList<>();
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		System.out.println("出错了client");
	}

	
}
