package cn.wulin.netty.multi.port.server.data.handler;

import java.util.Set;
import java.util.Map.Entry;

import cn.wulin.netty.multi.port.biz.ClientConnect;
import cn.wulin.netty.multi.port.codec.DataType;
import cn.wulin.netty.multi.port.domain.CompleteData;
import cn.wulin.netty.multi.port.handler.ServerHandler;
import cn.wulin.netty.multi.port.server.ServerDataHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class ServerNotifySendScreenHandler implements ServerDataHandler{

	@Override
	public int dataType() {
		return DataType.NOTIFY_SEND_SCREEN;
	}

	@Override
	public void execute(ChannelHandlerContext ctx, CompleteData data) throws Exception {
		String sendClientId = getSendClientId(data.getClientId());
		//这里将数据转发到发送客户端
		ClientConnect sendClientConnect = ServerHandler.getClientConnect(sendClientId);
		Channel sendCmdChannel = sendClientConnect.getCmdChannel();
		sendCmdChannel.writeAndFlush(data);
	}
	
	/**
	 * 得到发送client Id
	 * @param receiveClientId
	 * @return
	 */
	private String getSendClientId(String receiveClientId) {
		Set<Entry<String, String>> entrySet = ServerHandler.getRemoteConnectPair().entrySet();
		for (Entry<String, String> entry : entrySet) {
			if(entry.getKey().equals(receiveClientId)) {
				return entry.getValue();
			}
		}
		return "";
	}

}
