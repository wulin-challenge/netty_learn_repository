package cn.wulin.netty.multi.port.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wulin.netty.multi.port.biz.ClientConnect;
import cn.wulin.netty.multi.port.domain.CompleteData;
import cn.wulin.netty.multi.port.domain.PortSegmentData;
import cn.wulin.netty.multi.port.domain.PortSegmentStreamData;
import cn.wulin.netty.multi.port.server.ServerDataHandler;
import cn.wulin.netty.multi.port.server.data.handler.ClientPairHandler;
import cn.wulin.netty.multi.port.server.data.handler.CreateFileTransferHandler;
import cn.wulin.netty.multi.port.server.data.handler.RegisterChannelHandler;
import cn.wulin.netty.multi.port.server.data.handler.ServerNotifySendScreenHandler;
import cn.wulin.netty.multi.port.utils.CmdDownloadProgressBar;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

@Sharable
public class ServerHandler extends ChannelDuplexHandler{
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);
	
	/**
	 * 数据处理器
	 */
	private static final List<ServerDataHandler> DATA_HANDLER = new ArrayList<>();
	
	/**
	 * 客户端连接
	 * <p> key: clientId
	 * <p> value 客户端连接配置信息
	 */
	private static final ConcurrentHashMap<String, ClientConnect> CLIENT_CONNECT = new ConcurrentHashMap<>();
	
	/**
	 * 远程连接配对
	 * <p> key: 接受端的 clientId
	 * <p> value: 发送端的 clientId
	 */
	private static final ConcurrentHashMap<String, String> REMOTE_CONNECT_PAIR = new ConcurrentHashMap<>();
	
	private static Object lock = new Object();
	
	private CmdDownloadProgressBar downloadSpeed = new CmdDownloadProgressBar("", 0L);
	
	public ServerHandler() {
		super();
		initDataHandler();
	}
	
	private void initDataHandler() {
		DATA_HANDLER.add(new RegisterChannelHandler());
		DATA_HANDLER.add(new ClientPairHandler());
		DATA_HANDLER.add(new CreateFileTransferHandler());
		DATA_HANDLER.add(new ServerNotifySendScreenHandler());
	}

	@Override
    public void channelActive(ChannelHandlerContext ctx) {
		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("ServerHandler exceptionCaught",cause);
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		if(msg instanceof CompleteData) {
			CompleteData data = (CompleteData)msg;
			int dataType = data.getDataType();
			
			for (ServerDataHandler serverDataHandler : DATA_HANDLER) {
				if(dataType == serverDataHandler.dataType()) {
					serverDataHandler.execute(ctx, data);
					break;
				}
			}
		}else if(msg instanceof PortSegmentData) {
			PortSegmentData data = (PortSegmentData)msg;
			
			portSegmentHandler(ctx, data);
			
		}else if(msg instanceof PortSegmentStreamData) {
			PortSegmentStreamData data = (PortSegmentStreamData)msg;
			portSegmentStreamHandler(ctx, data);
			
		}
	}
	
	private void portSegmentHandler(ChannelHandlerContext ctx, PortSegmentData data) {
		String receiveClientId = data.getClientId();
		String sendClientId = getSendClientId(receiveClientId);
		int currentSegment = data.getCurrentSegment();
		
		ClientConnect clientConnect = ServerHandler.getClientConnect(sendClientId);
		Channel fileChannel = getChannel(currentSegment, clientConnect.getScreenChannel());
		fileChannel.writeAndFlush(data);
		
		downloadSpeed.start();
		downloadSpeed.setTotalLength(data.getDataTotalLength());
		downloadSpeed.addCurrentLength(data.getLength());
	}

	private void portSegmentStreamHandler(ChannelHandlerContext ctx,PortSegmentStreamData data) {
		String receiveClientId = data.getClientId();
		String sendClientId = getSendClientId(receiveClientId);
		int currentSegment = data.getCurrentSegment();
		
		ClientConnect clientConnect = ServerHandler.getClientConnect(sendClientId);
		Channel fileChannel = getChannel(currentSegment, clientConnect.getFileChannel());
		fileChannel.writeAndFlush(data);
		
		CreateFileTransferHandler.currentTransferFile.setTotalLength(data.getDataTotalLength());
		CreateFileTransferHandler.currentTransferFile.addCurrentLength(data.getDataLength());
		CreateFileTransferHandler.progressBar.start();
	}
	
	private Channel getChannel(int currentSegment,List<Channel> channelList) {
		int channelSize = channelList.size();
		if(channelSize == 1) {
			return channelList.get(0);
		}
		
		int index = currentSegment%channelSize;
		return channelList.get(index);
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

	public static List<ServerDataHandler> getDataHandler() {
		return DATA_HANDLER;
	}

	public static ConcurrentHashMap<String, ClientConnect> getClientConnect() {
		return CLIENT_CONNECT;
	}
	
	public static ConcurrentHashMap<String, String> getRemoteConnectPair() {
		return REMOTE_CONNECT_PAIR;
	}
	
	public static ClientConnect getClientConnect(String clientId) {
		ClientConnect clientConnect = CLIENT_CONNECT.get(clientId);
		if(clientConnect == null) {
			synchronized (lock) {
				clientConnect = CLIENT_CONNECT.get(clientId);
				if(clientConnect == null) {
					clientConnect = new ClientConnect();
					CLIENT_CONNECT.put(clientId, clientConnect);
				}
			}
		}
		return clientConnect;
	}
	
}
