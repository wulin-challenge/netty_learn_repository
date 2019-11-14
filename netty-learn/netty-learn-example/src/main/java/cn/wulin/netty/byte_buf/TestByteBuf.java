package cn.wulin.netty.byte_buf;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

public class TestByteBuf {
	
	@Test
	public void testSetInt() {
		ByteBuf directBuffer = Unpooled.directBuffer();
		ByteBuf buffer = Unpooled.buffer(5);
		
		PooledByteBufAllocator.DEFAULT.buffer();
		
		buffer.setByte(0, 0);
		buffer.setByte(1, 1);
		buffer.setByte(2, 2);
		buffer.setByte(3, 3);
		buffer.setByte(4, 4);
		buffer.setByte(5, 5);
		
		System.out.println(directBuffer);
	}
	
	@Test
	public void testWriteInt() {
		ByteBuf buffer = Unpooled.buffer(5);
		buffer.writeByte(0);
//		buffer.setByte(0, 1);
		
		byte readByte = buffer.readByte();
		
		
//		buffer.writeByte(1);
//		buffer.writeByte(2);
//		buffer.writeByte(3);
//		buffer.writeByte(4);
//		buffer.writeByte(5);
		
		System.out.println();
		
		int capacity = buffer.capacity();
		
	}
	
	@Test
	public void testSetWrintInt() {
		ByteBuf buffer = Unpooled.buffer(5);
		
		buffer.setInt(0, 5);
		buffer.writeInt(7);
		buffer.writeInt(8);
		buffer.writeInt(9);
		buffer.writeLong(5l);
		buffer.setInt(4, 15);
		buffer.writeByte(1);
		
		int int1 = buffer.getInt(0);
		int readInt = buffer.readInt();
		int readInt2 = buffer.readInt();
		long readLong = buffer.readLong();
		
		System.out.println();
	}
	
	@Test
	public void testMark() {
		ByteBuf buffer = Unpooled.buffer(5);
		
		buffer.writeByte(0);
		buffer.writeInt(7);
		buffer.writeInt(8);
		buffer.writeInt(9);
		
		buffer.writerIndex(5);
		
		buffer.markWriterIndex();
		buffer.resetWriterIndex();
		ByteBufProcessor processor = ByteBufProcessor.FIND_NUL;
		buffer.forEachByte(processor);
		
		System.out.println();
	}
	
	@Test
	public void xx() {
		long start = System.currentTimeMillis();
		
//		Set<Xxx> xxSet = new HashSet<Xxx>();
		
		List<Xxx> xxSet = new LinkedList<>();
		
		Xxx[] x  = new Xxx[10000000];
		
		for (int i = 0; i < 10000000; i++) {
//			
//			List<Xxx> xxSet = new LinkedList<>();
			xxSet.add( new Xxx());
//			x[i] = new Xxx();
//			xxSet.add();
		}
		
		long end = System.currentTimeMillis();
		
		System.out.println((end-start));
		
	}
	
	private static class  Xxx{
		private String x = "1";


		public String getX() {
			return x;
		}

		public void setX(String x) {
			this.x = x;
		}
		
	}
	
	@Test
	public void testByteBufImpl() {
		ByteBuf buffer = Unpooled.buffer(5);
		
		ByteBuf directBuffer = Unpooled.directBuffer(5);
		
		directBuffer.writeByte(1);
		directBuffer.writeByte(2);
		directBuffer.writeByte(3);
		directBuffer.writeByte(4);
		directBuffer.writeByte(5);
		
		ByteBuf copy = directBuffer.copy();
		
		
		System.out.println(buffer);
		System.out.println(directBuffer);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
