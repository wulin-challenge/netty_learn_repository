package cn.wulin.netty.file.demo.codec;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.wulin.brace.core.utils.SerializeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 用于文件传输和普通对象的解码器
 * @author wulin
 *
 */
public class FileDemoDecoder extends ByteToMessageDecoder{
	
	private Class<?> genericClass;
	
	

	public FileDemoDecoder(Class<?> genericClass) {
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
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < 8) {
            return;
        }
		in.markReaderIndex();
		int codecType = in.readInt();
		
		int dataLength = in.readInt();
		if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
		
		if(codecType == CodecType.CODEC_OBJECT) {
			byte[] data = new byte[dataLength];
	        in.readBytes(data);
	        out.add(SerializeUtil.deserialize(data, genericClass));
	        
		}else if(codecType == CodecType.CODEC_FILE) {
			
			ByteBuf buffer = Unpooled.buffer(dataLength);
			byte[] data = new byte[dataLength];
			in.readBytes(data);
			buffer.writeBytes(data);
			
			out.add(buffer);
			System.out.println("client:decoder: "+count2.incrementAndGet());
		}
	}
	
	AtomicInteger count2 = new AtomicInteger();
}
