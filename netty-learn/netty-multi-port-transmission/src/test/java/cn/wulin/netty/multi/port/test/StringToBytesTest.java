package cn.wulin.netty.multi.port.test;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.junit.Test;

public class StringToBytesTest {
	
	@Test
	public void stringToBytes() throws UnsupportedEncodingException {
		String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
		
		byte[] b = uuid.getBytes("UTF-8");
		
		String x = new String(b);
		System.out.println(b.length);
		System.out.println(x);
	}

}
