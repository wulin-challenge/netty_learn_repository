package cn.wulin.netty.multi.port.client.data.handler;

import cn.wulin.netty.multi.port.biz.ClientBizService;
import cn.wulin.netty.multi.port.client.ClientDataHandler;
import cn.wulin.netty.multi.port.codec.DataType;
import cn.wulin.netty.multi.port.domain.CompleteData;
import cn.wulin.netty.multi.port.utils.ExtensionLoader;
import io.netty.channel.ChannelHandlerContext;

public class ClientNotifySendScreenHandler  implements ClientDataHandler{

	@Override
	public int dataType() {
		return DataType.NOTIFY_SEND_SCREEN;
	}

	@Override
	public void execute(ChannelHandlerContext ctx, CompleteData data) throws Exception {
		ClientBizService service = ExtensionLoader.getInstance(ClientBizService.class);
		service.screenTransfer();
	}

}
