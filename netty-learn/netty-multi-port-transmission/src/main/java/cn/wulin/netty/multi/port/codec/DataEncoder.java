package cn.wulin.netty.multi.port.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wulin.netty.multi.port.domain.CompleteData;
import cn.wulin.netty.multi.port.domain.PortSegmentData;
import cn.wulin.netty.multi.port.domain.PortSegmentStreamData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 数据编码器
 * @author wulin
 *
 */
public class DataEncoder extends MessageToByteEncoder<Object>{
	private static final Logger LOGGER = LoggerFactory.getLogger(DataEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		
		try {
			if(msg instanceof CompleteData) {
				encodeCompleteData(ctx, (CompleteData)msg, out);
				
			}else if(msg instanceof PortSegmentData) {
				encodePortSegmentData(ctx, (PortSegmentData)msg, out);
			
			}else if(msg instanceof PortSegmentStreamData) {
				encodePortSegmentStreamData(ctx, (PortSegmentStreamData)msg, out);
			}
			
		} catch (Exception e) {
			LOGGER.error("编码失败!",e);
		}
		
	}
	
	private void encodeCompleteData(ChannelHandlerContext ctx, CompleteData msg, ByteBuf out) throws Exception {
		
		long length = 4+8+32+32+4+msg.getData().length;
		out.writeInt(msg.getType());
		out.writeLong(length);
		out.writeBytes(msg.getClientId().getBytes("UTF-8")); //32 位的uuid
		out.writeBytes(msg.getRequestId().getBytes("UTF-8")); //32 位的uuid
		out.writeInt(msg.getDataType());
		out.writeBytes(msg.getData());
	}

	private void encodePortSegmentData(ChannelHandlerContext ctx, PortSegmentData msg, ByteBuf out) throws Exception {
		
		long length = 4+8+32+32+4+4+msg.getData().length;
		out.writeInt(msg.getType());
		out.writeLong(length);
		out.writeBytes(msg.getClientId().getBytes("UTF-8")); //32 位的uuid
		out.writeBytes(msg.getRequestId().getBytes("UTF-8")); //32 位的uuid
		out.writeInt(msg.getSegmentCount());
		out.writeInt(msg.getCurrentSegment());
		out.writeBytes(msg.getData());
	}
	
	private void encodePortSegmentStreamData(ChannelHandlerContext ctx, PortSegmentStreamData msg, ByteBuf out) throws Exception {
		
		long length = 4+8+32+32+4+4+8+8+8+msg.getData().length;
		out.writeInt(msg.getType());
		out.writeLong(length);
		out.writeBytes(msg.getClientId().getBytes("UTF-8")); //32 位的uuid
		out.writeBytes(msg.getRequestId().getBytes("UTF-8")); //32 位的uuid
		out.writeInt(msg.getSegmentCount());
		out.writeInt(msg.getCurrentSegment());
		out.writeLong(msg.getDataTotalLength());
		out.writeLong(msg.getDataStartPosition());
		out.writeLong(msg.getDataLength());
		out.writeBytes(msg.getData());
	}
}
