package cn.wulin.netty.multi.port.domain;

import java.io.Serializable;

import cn.wulin.netty.multi.port.codec.CodecType;

/**
 * 端口端数据结构
 * <p> 注意字段的顺序,这里的字段顺序就代表了数据传输的顺序
 * @author wulin
 *
 */
public class PortSegmentData implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private int type = CodecType.CODEC_PORT_SEGMENT_DATA;
	
	/**
	 * 数据长度,指的是当前段数据的长度,(包括type和length)
	 */
	private long length = 0L;
	
	/**
	 * 客户端Id
	 */
	private String clientId;
	
	/**
	 * 数据请求Id,这里是32位的uuid
	 */
	private String requestId;
	
	/**
	 * 一共有多少个数据段
	 */
	private int segmentCount = 0;
	
	/**
	 * 当前是那个数据段
	 */
	private int currentSegment = -1;
	
	/**
	 * 表示要传输数据的总长度
	 */
	private long dataTotalLength = 0L;
	
	private long dataStartPosition = 0L;
	
	/**
	 * 表示当前传输数据的长度
	 */
	private long dataLength = 0L;
	
	/**
	 * 真正的数据
	 */
	private byte[] data;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public int getSegmentCount() {
		return segmentCount;
	}

	public void setSegmentCount(int segmentCount) {
		this.segmentCount = segmentCount;
	}

	public int getCurrentSegment() {
		return currentSegment;
	}

	public void setCurrentSegment(int currentSegment) {
		this.currentSegment = currentSegment;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public long getDataTotalLength() {
		return dataTotalLength;
	}

	public void setDataTotalLength(long dataTotalLength) {
		this.dataTotalLength = dataTotalLength;
	}

	public long getDataStartPosition() {
		return dataStartPosition;
	}

	public void setDataStartPosition(long dataStartPosition) {
		this.dataStartPosition = dataStartPosition;
	}

	public long getDataLength() {
		return dataLength;
	}

	public void setDataLength(long dataLength) {
		this.dataLength = dataLength;
	}
}
