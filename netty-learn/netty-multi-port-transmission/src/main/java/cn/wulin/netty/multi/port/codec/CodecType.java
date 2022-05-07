package cn.wulin.netty.multi.port.codec;

/**
 * 编解码类型
 * @author wulin
 *
 */
public class CodecType {
	
	/**
	 * 表示使用端口分段数据
	 * <p> 表示一次传输的数据采用多端口分段传输
	 */
	public static final int CODEC_PORT_SEGMENT_DATA = 1;
	
	/**
	 * 表示采用多端口流式传输
	 * <p> 表示数据采用多端口传输且要连续传输多次
	 */
	public static final int CODEC_PORT_SEGMENT_STREAM_DATA = 2;
	
	/**
	 * 表示完整数据
	 * <p> 表示一次传输的数据是完整的数据,没有使用多端口
	 */
	public static final int CODEC_COMPLETE_DATA = 3;
	
}
