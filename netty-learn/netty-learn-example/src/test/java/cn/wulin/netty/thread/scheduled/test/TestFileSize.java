package cn.wulin.netty.thread.scheduled.test;

import java.io.File;

import org.junit.Test;

public class TestFileSize {
	
//	RemotingCommand command = new RemotingCommand(1,1,0,"F:/resources/temp/temp4/clientData/2.txt");
////	RemotingCommand command = new RemotingCommand(1,1,0,"F:/resources/temp/temp4/clientData/2.zip");
////	RemotingCommand command = new RemotingCommand(1,1,0,"F:/resources/temp/temp4/clientData/m1.zip");
////	RemotingCommand command = new RemotingCommand(1,1,0,"F:/resources/temp/temp4/clientData/x1.zip");
////	RemotingCommand command = new RemotingCommand(1,1,0,"F:/resources/temp/temp4/clientData/e1.rar");
//	FileDemoInputStream is = new FileDemoInputStream(channel,command);
//	File file = new File("F:/resources/temp/temp4/clientData","3.txt");
////	File file = new File("F:/resources/temp/temp4/clientData","3.zip");
////	File file = new File("F:/resources/temp/temp4/clientData","m2.zip");
////	File file = new File("F:/resources/temp/temp4/clientData","x2.zip");
////	File file = new File("F:/resources/temp/temp4/clientData","e2.rar");
	
	@Test
	public void testBothFileSize() {
		
//		File txt2 = new File("F:/resources/temp/temp4/clientData/2.txt");
//		File txt3 = new File("F:/resources/temp/temp4/clientData/3.txt");
//		
//		System.out.println(txt2.length());
//		System.out.println(txt3.length());
		
		File x1 = new File("F:/resources/temp/temp4/clientData/x1.zip");
		File x2 = new File("F:/resources/temp/temp4/clientData/x2.zip");
		
		System.out.println(x1.length());
		System.out.println(x2.length());
		
		
		
	}

}
