package cn.wulin.netty.multi.port;

import cn.wulin.netty.multi.port.biz.ClientBizService;
import cn.wulin.netty.multi.port.handler.ClientHandler;
import cn.wulin.netty.multi.port.utils.ExtensionLoader;

/**
 * 数据发送客户端,这里的数据指的是屏幕数据
 * @author wulin
 *
 */
public class SendClient {
	private static Client CLIENT = new Client(new ClientHandler());
	
	public static void main(String[] args) {
		CLIENT.client();
		ClientBizService clientBizService = new ClientBizService(CLIENT);
		ExtensionLoader.setInstance(ClientBizService.class, clientBizService);
	}

}
