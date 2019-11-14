package cn.wulin.netty.thread.scheduled.test;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TestByteBuf {
	
	@Test
	public void testBuf() {
		
		long start = System.currentTimeMillis();
		ByteBuf buffer = Unpooled.buffer();
		for (int i = 0; i < 1000; i++) {
			
			byte[] s = new byte[1024*1024];
//			buffer.readBytes(s);
			buffer.writeBytes(s);
			System.out.println(i);
		}
		long end = System.currentTimeMillis();
		System.out.println("总共时间:"+(end-start));
	}

}
