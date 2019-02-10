package cn.wulin.nio.socket.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

public class NClient {
	
	//定义检测SocketChannel 的Selector对象
	private Selector selector = null;
	
	//定义处理编码和解码的字符集
	private Charset charset = Charset.forName("UTF-8");
	
	//客户端socketChannel
	private SocketChannel sc = null;
	
	public void init()throws IOException{
		selector = Selector.open();
		InetSocketAddress isa = new InetSocketAddress("127.0.0.1", 30000);
		//调用open静态方法创建连接到指定主机的socketChannel
		sc = SocketChannel.open(isa);
		//设置该sc以非阻塞方式工作
		sc.configureBlocking(false);
		//将SocketChannel对象注册到指定Selector
		sc.register(selector, SelectionKey.OP_READ);
		//启动读取服务器大端数据的线程
		new ClientThread().start();
		
		//创建键盘输入流
		Scanner scan = new Scanner(System.in);
		while(scan.hasNextLine()){
			//读取键盘输入
			String line = scan.nextLine();
			//将键盘输入的内容输出到SocketChannel中
			sc.write(charset.encode(line));
		}
	}
	
	private class ClientThread extends Thread{

		@Override
		public void run() {
			
			try {
				while(selector.select()>0){
					//遍历每个有可用IO操作Channel对应的SelectionKey
					for (SelectionKey sk : selector.selectedKeys()) {
						//删除正在处理的SelectionKey
						selector.selectedKeys().remove(sk);
						//如果该selectionKey对应的Channel中有可读的数据
						if(sk.isReadable()){
							//使用NIO读取channel中的数据
							SocketChannel sc = (SocketChannel) sk.channel();
							ByteBuffer buff = ByteBuffer.allocate(1024);
							String content = "";
							//开始读取数据
							while(sc.read(buff)>0){
//									sc.read(buff); //应该是多余的
								buff.flip();
								content += charset.decode(buff);
							}
							//打印输出读取的内容
							System.out.println("====="+content);
							//为下一次读取做准备
							sk.interestOps(SelectionKey.OP_READ);
							
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		new NClient().init();
	}

}
