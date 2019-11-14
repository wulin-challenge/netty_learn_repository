package cn.wulin.netty.file.demo.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 合成的多个Bytebuf
 * <p> 没有使用netty Unpooled.compositeBuffer() ,是因为当多次写入数据,性能很低
 * @author wulin
 */
public class MultiByteBufComposite {
	
	//使用阻塞队列来防止读取线程消费缓慢而出现内存溢出的情况
	private BlockingQueue<ByteBuf> buffers = new LinkedBlockingQueue<>(50);
	
	private AtomicLong readIndex = new AtomicLong(0);
	private AtomicLong writeIndex = new AtomicLong(0);
	
	private ByteBuf currentBuffer;
	private long totalLength = -1L;
	
	/**
	 * 此处在无锁情况下如何保证多线读写数据的安全与正确
	 * <p> 1. 对于写入状态必须要在写入数据之后更新
	 * <p> 2. 对于读出数据,必须要保证先判断写入状态与读取状态在相应的条件下成立,在进数据的读出,然后更新读状态
	 * @param buffer
	 * @param length
	 */
	public void put(ByteBuf buffer,long length) {
		try {
			if(this.totalLength == -1L) {
				this.totalLength = length;
			}
			int tembLen = buffer.readableBytes()-8;
			buffers.put(buffer);
			writeIndex.addAndGet(tembLen);
			
			if(writeIndex.get() == totalLength) {
				System.out.println("服务端传输完成: 总共数据为: "+totalLength);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public long readableBytes() {
		return writeIndex.get()-readIndex.get();
	}
	
	public boolean isReadable() {
		return readableBytes()>0;
	}
	
	public byte readByte() {
		checkBuffer();
		byte readByte = currentBuffer.readByte();
		readIndex.incrementAndGet();
		checkBuffer();
		return readByte;
	}
	
	public int readBytes(byte[] dst, int dstIndex, int length){
		
		checkBuffer();
		if(currentBuffer == null) {
			checkBuffer();
			//TODO 此处逻辑有待考证..........
			System.out.println("此处逻辑有待考证..........");
			return -1;
		}
		if(currentBuffer.readableBytes()>=length) {
			currentBuffer.readBytes(dst, 0, length);
			readIndex.addAndGet(length);
			checkBuffer();
			return length;
		}
		
		ByteBuf tempBuffer = Unpooled.buffer(length);
		while(true) {
			if(tempBuffer.writableBytes()-currentBuffer.readableBytes()>=0) {
				int tempLen = currentBuffer.readableBytes();
				byte[] temp = new byte[tempLen];
				
				currentBuffer.readBytes(temp, 0, tempLen);
				readIndex.addAndGet(tempLen);
				
				tempBuffer.writeBytes(temp);
			}else {
				int tempLen = tempBuffer.writableBytes();
				byte[] temp = new byte[tempLen];
				
				currentBuffer.readBytes(temp, 0, tempLen);
				readIndex.addAndGet(tempLen);
				
				tempBuffer.writeBytes(temp);
			}
			checkBuffer();
			if(tempBuffer.writableBytes()==0 || currentBuffer == null) {
				length = tempBuffer.readableBytes();
				break;
			}
		}
		tempBuffer.readBytes(dst, 0, length);
		checkBuffer();
		return length;
	}
	
	private void checkBuffer() {
		if(currentBuffer == null) {
			try {
				if(totalLength > readIndex.get()) {
					currentBuffer = buffers.take();
					long readLong = currentBuffer.readLong();
					System.out.println("sort: "+readLong);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(currentBuffer != null && !currentBuffer.isReadable()) {
			currentBuffer.release();
			currentBuffer = null;
			checkBuffer();
		}
	}
	
	public long writerIndex() {
		return writeIndex.get();
	}

	public long readerIndex() {
		return readIndex.get();
	}
	
	public long capacity() {
		return totalLength;
	}
}
