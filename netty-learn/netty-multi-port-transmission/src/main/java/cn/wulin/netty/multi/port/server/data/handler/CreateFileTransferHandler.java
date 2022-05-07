package cn.wulin.netty.multi.port.server.data.handler;

import java.io.File;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import cn.wulin.netty.multi.port.biz.ClientConnect;
import cn.wulin.netty.multi.port.biz.vo.ClientCreateFileTransferVO;
import cn.wulin.netty.multi.port.codec.DataType;
import cn.wulin.netty.multi.port.domain.CompleteData;
import cn.wulin.netty.multi.port.handler.ServerHandler;
import cn.wulin.netty.multi.port.server.ServerDataHandler;
import cn.wulin.netty.multi.port.utils.CmdDownloadProgressBar;
import cn.wulin.netty.multi.port.utils.CmdDownloadProgressBar.ProgressInfo;
import cn.wulin.netty.multi.port.utils.CommonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * 创建文件传输处理器
 * @author wulin
 *
 */
public class CreateFileTransferHandler implements ServerDataHandler{
	
	public static CmdDownloadProgressBar progressBar;
	public static ClientCreateFileTransferVO currentTransferFile = new ClientCreateFileTransferVO();

	@Override
	public int dataType() {
		return DataType.CREATE_FILE_TRANSFER;
	}

	@Override
	public void execute(ChannelHandlerContext ctx, CompleteData data) throws Exception {
		String clientId = data.getClientId();
		String sendClientId = getSendClientId(clientId);
		if(StringUtils.isBlank(sendClientId)) {
			throw new RuntimeException(clientId+" 没有找到发送客户端");
		}
		
		//这里将数据转发到发送客户端
		ClientConnect sendClientConnect = ServerHandler.getClientConnect(sendClientId);
		Channel sendCmdChannel = sendClientConnect.getCmdChannel();
		sendCmdChannel.writeAndFlush(data);
		String filePaht = new String(data.getData(),CommonUtil.getCharset());
		currentTransferFile.setFileName(new File(filePaht).getName());
		currentTransferFile.setRemoteFilePath(filePaht);
		currentTransferFile.setRequestId(data.getRequestId());
		
		progressBar = new CmdDownloadProgressBar(new ProgressInfo() {
			
			@Override
			public long totalLength() {
				return currentTransferFile.getTotalLength();
			}
			
			@Override
			public String fileName() {
				return currentTransferFile.getFileName();
			}
			
			@Override
			public long currentLength() {
				return currentTransferFile.getCurrentLength();
			}
		});
		
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
