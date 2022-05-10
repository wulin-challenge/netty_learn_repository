package cn.wulin.netty.multi.port.codec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wulin.netty.multi.port.domain.CompleteData;
import cn.wulin.netty.multi.port.domain.PortSegmentData;
import cn.wulin.netty.multi.port.domain.PortSegmentStreamData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 数据解码器
 * @author wulin
 *
 */
public class DataDecoder extends ByteToMessageDecoder{
	private static final Logger LOGGER = LoggerFactory.getLogger(DataDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < 12) {
            return;
        }
		in.markReaderIndex();
		int codecType = in.readInt();
		
		long dataLength = in.readLong()-4-8;
		if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
		
		//是因为我需要读取完整数据
		in.resetReaderIndex();
		try {
			if(codecType == CodecType.CODEC_COMPLETE_DATA) {
				decodeCompleteData(ctx, in, out);
				
			}else if(codecType == CodecType.CODEC_PORT_SEGMENT_DATA) {
				decodePortSegmentData(ctx, in, out);
			
			}else if(codecType == CodecType.CODEC_PORT_SEGMENT_STREAM_DATA) {
				decodePortSegmentStreamData(ctx, in, out);
			}
			
		} catch (Exception e) {
			LOGGER.error("解码失败!",e);
		}
	}

	private void decodePortSegmentStreamData(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		PortSegmentStreamData data = new PortSegmentStreamData();
		data.setType(in.readInt());
		data.setLength(in.readLong());
		
		byte[] clientId = new byte[32];
        in.readBytes(clientId);
		data.setClientId(new String(clientId));
		
		byte[] requestId = new byte[32];
        in.readBytes(requestId);
		data.setRequestId(new String(requestId));
		
		data.setSegmentCount(in.readInt());
		data.setCurrentSegment(in.readInt());
		
		data.setDataTotalLength(in.readLong());
		data.setDataStartPosition(in.readLong());
		data.setDataLength(in.readLong());
		
		long realLength = data.getLength()-4-8-32-32-4-4-8-8-8;
		byte[] realData = new byte[(int)realLength];
		in.readBytes(realData);
		data.setData(realData);
		
		out.add(data);
	}

	private void decodePortSegmentData(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		PortSegmentData data = new PortSegmentData();
		data.setType(in.readInt());
		data.setLength(in.readLong());
		
		byte[] clientId = new byte[32];
        in.readBytes(clientId);
		data.setClientId(new String(clientId));
		
		byte[] requestId = new byte[32];
        in.readBytes(requestId);
		data.setRequestId(new String(requestId));
		
		data.setSegmentCount(in.readInt());
		data.setCurrentSegment(in.readInt());
		
		data.setDataTotalLength(in.readLong());
		data.setDataStartPosition(in.readLong());
		data.setDataLength(in.readLong());
		
		long realLength = data.getLength()-4-8-32-32-4-4-8-8-8;
		byte[] realData = new byte[(int)realLength];
		in.readBytes(realData);
		data.setData(realData);
		
		out.add(data);
	}

	private void decodeCompleteData(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		CompleteData data = new CompleteData();
		data.setType(in.readInt());
		data.setLength(in.readLong());
		
		byte[] clientId = new byte[32];
        in.readBytes(clientId);
		data.setClientId(new String(clientId));
		
		byte[] requestId = new byte[32];
        in.readBytes(requestId);
		data.setRequestId(new String(requestId));
		
		data.setDataType(in.readInt());
		
		long realLength = data.getLength()-4-8-32-32-4;
		byte[] realData = new byte[(int)realLength];
		in.readBytes(realData);
		data.setData(realData);
		
		out.add(data);
	}

}
