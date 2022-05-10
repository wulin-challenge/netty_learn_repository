package cn.wulin.netty.multi.port.biz;

import java.util.Scanner;
import java.util.function.Consumer;

@SuppressWarnings("resource")
public class CommandLineService {


	public void cmd(Consumer<String> cmd) {
		Thread thread = new Thread(()->{
			Scanner scanner = new Scanner(System.in);
			String command = scanner.nextLine();
			while(command != null) {
				cmd.accept(command);;
				command = scanner.nextLine();
			}
		});
		thread.start();
	}
	
	/**
	 * 客户端提示列表
	 */
	public void clientTipList() {
		System.out.println("连接服务端并注册管道: c1");
		System.out.println("客户端配对: c2");
		System.out.println("创建传输文件: c3");
		System.out.println("开始传输文件: c4");
		System.out.println("在sendClient端执行发送截屏: c4");
		
	}
	
	/**
	 * 服务端提示列表
	 */
	public void serverTipList() {
		System.out.println("监听: s1");
		
	}
}
