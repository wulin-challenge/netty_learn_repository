package cn.wulin.netty.thread.scheduled.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class TestZipFileCopy {
	
	@Test
	public void testFileCopy(){
		File x1 = new File("F:/resources/temp/temp4/clientData/x1.zip");
		File x2 = new File("F:/resources/temp/temp4/clientData/x2.zip");
		
		try {
			FileUtils.copyFile(x1, x2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFileCopy2(){
		File x1 = new File("F:/resources/temp/temp4/clientData/x1.zip");
		File x2 = new File("F:/resources/temp/temp4/clientData/x2.zip");
		
		
		
		try {
			FileInputStream fis = new FileInputStream(x1);
			FileOutputStream fos = new FileOutputStream(x2);
			
			byte[] b = new byte[2];
			int len = 0;
		
			long start = System.currentTimeMillis();
			
			int i =0;
			while((len = fis.read(b))>0) {
				fos.write(b,0,len);
				i++;
			}
			
			long end = System.currentTimeMillis();
			System.out.println("总共时间:"+((end-start)/1000));
			
			fos.close();
			fis.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFileCopy3(){
		File x1 = new File("F:/resources/temp/temp4/clientData/x1.zip");
		File x2 = new File("F:/resources/temp/temp4/clientData/x2.zip");
		
		long length = x1.length();
		long offset = 0;
		byte[] buffer = new byte[1024*8];
		
		
		
		try {
			RandomAccessFile randomFile1 = new RandomAccessFile(x1, "rw");
			RandomAccessFile randomFile2 = new RandomAccessFile(x2, "rw");
			while(length - offset>0) {
				int chunkSize = (int) Math.min(buffer.length, length - offset);
				
				randomFile1.readFully(buffer, 0, chunkSize);
				
				randomFile2.write(buffer, 0, chunkSize);
				offset += chunkSize;
			}
			
			randomFile1.close();
			randomFile2.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
