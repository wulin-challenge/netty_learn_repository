package cn.wulin.netty.multi.port.domain;

import java.io.Serializable;

import cn.wulin.netty.multi.port.codec.CodecType;
import cn.wulin.netty.multi.port.codec.DataType;

/**
 * 表示一次传输的数据的完整数据
 * @author wulin
 *
 */
public class CompleteData implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private int type = CodecType.CODEC_COMPLETE_DATA;
	
	/**
	 * 数据长度,(包括type和length)
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
	 * 数据类型
	 */
	private int dataType = DataType.STRING;
	
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

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
}
