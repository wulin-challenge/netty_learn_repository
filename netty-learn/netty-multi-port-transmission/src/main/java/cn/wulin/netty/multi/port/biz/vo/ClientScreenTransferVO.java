package cn.wulin.netty.multi.port.biz.vo;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 客户端屏幕传输vo
 * @author wulin
 *
 */
public class ClientScreenTransferVO {
	
	private String requestId;
	
	/**
	 * 文件总大小
	 */
	private Long totalLength = 0L;
	
	/**
	 * 数据段
	 */
	byte[][] dataSegment;
	
	/**
	 * 当前的文件大小
	 */
	private volatile AtomicLong currentLength = new AtomicLong(0L);
	
	public void addCurrentLength(long length) {
		currentLength.addAndGet(length);
	}
	
	public Long getCurrentLength() {
		return currentLength.get();
	}
	
	/**
	 * 合并数据
	 * @return
	 */
	public byte[] mergeData() {
		int length = totalLength.intValue();
		byte[] dest = new byte[length];
		
		int destPos = 0;
		for (int i = 0; i < dataSegment.length; i++) {
			byte[] temp = dataSegment[i];
			System.arraycopy(temp, 0, dest, destPos, temp.length);
			destPos+=temp.length;
		}
		return dest;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Long getTotalLength() {
		return totalLength;
	}

	public void setTotalLength(Long totalLength) {
		this.totalLength = totalLength;
	}

	public byte[][] getDataSegment() {
		return dataSegment;
	}

	public void setDataSegment(byte[][] dataSegment) {
		this.dataSegment = dataSegment;
	}
	
}
