package cn.wulin.nio.test.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import org.junit.Test;

public class TestSelector {
	Selector selector = null;

	@Test
	public void selectorWakeUpTest() throws IOException {
        selector = Selector.open();
		
		//通过open方法来打开一个未绑定的ServerSocketChannel实例
		ServerSocketChannel server = ServerSocketChannel.open();
		
		InetSocketAddress isa = new InetSocketAddress("127.0.0.1",30000);
		
		//将该ServerSocketChannel绑定到指定ip地址
		server.socket().bind(isa);
		
		//设置ServerSocket以非阻塞方式工作
		server.configureBlocking(false);
		
		//将server注册到指定Selector对象
		server.register(selector, SelectionKey.OP_ACCEPT);
		
		wakeUp();
		selectNow();
		
		for (int i = 0; i < 1000; i++) {
			int select = selector.select();
			System.out.println(select);
		}
	}
	
	/**
	 * 执行唤醒
	 */
	private void wakeUp() {
		
		Thread thread = new Thread(()-> {
			for (int i = 0; i < 1000; i++) {
				System.out.println();
				selector.wakeup();
				System.out.println();
			}
		});
		thread.start();
		
	}

	private void selectNow() {
		Thread thread = new Thread(()-> {
			for (int i = 0; i < 1000; i++) {
				System.out.println();
				try {
					selector.selectNow();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println();
			}
		});
		thread.start();
	}
}
