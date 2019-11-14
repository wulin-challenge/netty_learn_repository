package cn.wulin.netty.file.demo.codec;

import java.util.concurrent.atomic.AtomicInteger;

import cn.wulin.brace.core.utils.SerializeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 用于文件传输和普通消息对象的编码器
 * @author wulin
 *
 */
public class FileDemoEncoder extends MessageToByteEncoder<Object>{
	
	private Class<?> genericClass;
	
	public FileDemoEncoder(Class<?> genericClass) {
		super();
		this.genericClass = genericClass;
	}

	/**
	 * 文件传输的buffer数据结构: 
	 * <p> int : 请求Id
	 * <p> int : 是否结束的标记, 0:表示正在传输, -1 表示传输结束
	 * <p> long : 文件数据的总长度
	 * <p> int : 当前传输数据的长度(包含当前传输的数据块的传输顺序序号)
	 * <p> long : 当前传输的数据块的传输顺序序号
	 * <p> bytes: 传输的数据
	 * @param ctx
	 * @param msg
	 */
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		if(genericClass.isInstance(msg)) {
			
			byte[] data = SerializeUtil.serialize(msg);
			out.writeInt(CodecType.CODEC_OBJECT); // int -> 4 byte
            out.writeInt(data.length);
            out.writeBytes(data);
			
		}else if(msg instanceof ByteBuf) {
			try {
			ByteBuf buf = (ByteBuf)msg;
			int length = buf.readableBytes();
			byte[] data = new byte[length];
			buf.readBytes(data);
			
			out.writeInt(CodecType.CODEC_FILE); // int -> 4 byte
			out.writeInt(length);
			out.writeBytes(data);
			
			System.out.println("server:encoder: "+count2.incrementAndGet());
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	AtomicInteger count2 = new AtomicInteger();
	
}
