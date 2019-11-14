package cn.wulin.netty.file.demo.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import cn.wulin.netty.file.demo.domain.RemotingCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * 用于接收远程文件流程的FileDemo输入流
 * @author wulin
 *
 */
public class FileDemoInputStream extends InputStream{
	private Channel channel;
	private RemotingCommand command;
	
	 /**
     * 已累积的 ByteBuf 对象,默认初始化20M
     */
	MultiByteBufComposite cumulation = new MultiByteBufComposite();
	
	private ReentrantLock lock = new ReentrantLock();
	private Condition condion = lock.newCondition();
	
	/**
	 * 临时计数用
	 */
	private AtomicInteger count = new AtomicInteger();
	
	/**
	 * 传输状态
	 * <p> 0:表示正在传输, -1 表示传输结束,-2: 表示还没有开始传输,-3: 表示已经发起传输请求
	 */
	private volatile int transferState = -2;
	
	/**
	 * 总的长度
	 */
	private volatile long length;
	
	/**
	 * 当前传输的长度
	 */
	private volatile int transferLength;
	
	public FileDemoInputStream(Channel channel, RemotingCommand command) {
		super();
		this.channel = channel;
		this.command = command;
	}
	
	/**
	 * 添加byte
	 * 文件传输的buffer数据结构: 
	 * <p> int : 请求Id -- 该数据已经被获取
	 * <p> int : 是否结束的标记, 1: 表示开始传输,0:表示正在传输, -1 表示传输结束
	 * <p> long : 文件数据的总长度
	 * <p> int : 当前传输数据的长度
	 * <p> bytes: 传输的数据
	 * 
	 */
	public void addByteBuf(ByteBuf buffer) {
		transferState = buffer.readInt();
		length = buffer.readLong();
		transferLength = buffer.readInt();
		
		buffer = buffer.retainedSlice(buffer.readerIndex(), transferLength);
		
		System.out.println("--------------------------- "+transferLength);
		
		cumulation.put(buffer,length);
		System.out.println("client: "+count.incrementAndGet());
		signal();
	}
	
	private void check(int len) {
		if(transferState == -2) {
			//发起传输
			channel.writeAndFlush(command);
			transferState = -3;
			await();
			check(len);
		}
		
		if(cumulation.readableBytes()<len && transferState != -1) {
			await();
			check(len);
		}
	}
	
	private void await() {
		try {
			lock.lock();
			condion.await(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if(lock.isLocked()) {
				lock.unlock();
			}
		}
	}
	
	private void signal() {
		try {
			lock.lock();
			condion.signal();
		} finally {
			if(lock.isLocked()) {
				lock.unlock();
			}
		}
		
	}

	@Override
	public int read() throws IOException {
		check(1);
		if (!cumulation.isReadable()) {
            return -1;
        }
        return cumulation.readByte() & 0xff;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b,0,b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if(transferState == -1 && cumulation.readableBytes()==0 && cumulation.readerIndex() == cumulation.capacity()) {
			return -1;
		}
		
		if(transferState == -1 && cumulation.readableBytes()<(long)len && cumulation.writerIndex() == cumulation.capacity()) {
			len = (int) cumulation.readableBytes();
		}
		check(len);
		try {
			len = cumulation.readBytes(b, off, len);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return len;
	}

	@Override
	public long skip(long n) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int available() throws IOException {
		return (int) length;
	}

	@Override
	public void close() throws IOException {
		super.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean markSupported() {
		return false;
	}
}
