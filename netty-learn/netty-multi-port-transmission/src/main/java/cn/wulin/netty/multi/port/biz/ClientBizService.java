package cn.wulin.netty.multi.port.biz;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.wulin.netty.multi.port.Client;
import cn.wulin.netty.multi.port.biz.vo.ClientCreateFileTransferVO;
import cn.wulin.netty.multi.port.biz.vo.ClientPairVO;
import cn.wulin.netty.multi.port.biz.vo.RegisterChannelVO;
import cn.wulin.netty.multi.port.codec.DataType;
import cn.wulin.netty.multi.port.domain.CompleteData;
import cn.wulin.netty.multi.port.domain.PortSegmentData;
import cn.wulin.netty.multi.port.domain.PortSegmentStreamData;
import cn.wulin.netty.multi.port.utils.CmdDownloadProgressBar;
import cn.wulin.netty.multi.port.utils.CmdDownloadProgressBar.ProgressInfo;
import cn.wulin.netty.multi.port.utils.CommonUtil;
import cn.wulin.netty.multi.port.utils.ExtensionLoader;
import cn.wulin.netty.multi.port.utils.GzipUtil;
import cn.wulin.netty.multi.port.utils.ScreenRobotUtil;
import io.netty.channel.Channel;

public class ClientBizService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientBizService.class);
	
	private Client client;
	private CmdDownloadProgressBar progressBar;
	private CommandLineService cmdLine = new CommandLineService();
	private ClientProperties clientProperties = ClientProperties.getClientProperties();
	private ClientConnect clientConnect = new ClientConnect();
	
	/**
	 * 当前传输文件
	 */
	private ClientCreateFileTransferVO currentTransferFile = new ClientCreateFileTransferVO();

	public ClientBizService(Client client) {
		this.client = client;
		
		cmdLine.clientTipList();
		cmdLine.cmd((stringLine)->{
			
			String[] params = stringLine.split("::");
			String cmd = params[0];
			String param = params.length >1?params[1]:"";
			param = param.trim();
			
			switch (cmd) {
			case "c1":
				connectServer();
				break;
			case "c2":
				clientPair(param);
				break;
			case "c3":
				createFileTransfer(param);
				break;
			case "c4":
				fileTransfer();
				break;
			case "c5":
//				Thread thread = new Thread(()->{
//					while(true) {
//						long start = System.currentTimeMillis();
//						screenTransfer();
//						long end = System.currentTimeMillis();
//						System.out.println("截取时间--"+(end-start));
//					}
//				});
//				
//				thread.start();
				
				screenTransfer();
				
				break;
			default:
				break;
			}
		});
	}
	

	public void screenTransfer() {
		
		byte[] screen = ScreenRobotUtil.getScreenSnapshot();
		screen = GzipUtil.compress(screen);
		
		long dataTotalLength = screen.length;
		long perLength = 1024*50; //50kb
		int segmentCount = getSegmentCount(dataTotalLength,perLength);
		String requestId = CommonUtil.getUUID();
		
		for(int i=0;i<segmentCount;i++) {
			PortSegmentData data = new PortSegmentData();
			data.setDataTotalLength(dataTotalLength);
			data.setClientId(clientConnect.getClientId());
			data.setRequestId(requestId);
			data.setSegmentCount(segmentCount);
			data.setCurrentSegment(i);
			data.setDataStartPosition(getDataStartPosition(i, perLength));
			data.setDataLength(getDataLength(dataTotalLength, segmentCount, i, perLength));
			data.setData(getData(data, screen));
			
			Channel fileChannel = getChannel(i,clientConnect.getScreenChannel());
			
			fileChannel.writeAndFlush(data);
		}
		
//		try {
//			Thread.sleep(300);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}

	private void createFileTransfer(String filePath) {
		Channel channel = clientConnect.getCmdChannel();
		String requestId = CommonUtil.getUUID();
		
		try {
			
			byte[] data = filePath.getBytes(CommonUtil.getCharset());
			CompleteData cd = new CompleteData();
			cd.setClientId(clientConnect.getClientId());
			cd.setDataType(DataType.CREATE_FILE_TRANSFER);
			cd.setData(data);
			cd.setRequestId(requestId);
			
			currentTransferFile.setLocalFilePath(filePath);
			currentTransferFile.setRequestId(requestId);
			
			File localFile = new File(filePath);
			currentTransferFile.setFileName(localFile.getName());
			currentTransferFile.setTotalLength(localFile.length());
			
			
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

			channel.writeAndFlush(cd);
		} catch (Exception e) {
			LOGGER.error("配对失败",e);
		}
	}
	
	/**
	 * 文件传输
	 * @param filePath
	 */
	private void fileTransfer() {
		String localFilePath = currentTransferFile.getLocalFilePath();
		String requestId = currentTransferFile.getRequestId();
		File localFile = new File(localFilePath);
		try {
			RandomAccessFile randomFile = new RandomAccessFile(localFile, "r");
			long dataTotalLength = randomFile.length();
			long perLength = 1024*50; //50kb
//			long perLength = 1024*100; //100kb
			//long perLength = 1024*1024; //100kb
			int segmentCount = getSegmentCount(dataTotalLength,perLength);
			
			for(int i=0;i<segmentCount;i++) {
				PortSegmentStreamData data = new PortSegmentStreamData();
				data.setDataTotalLength(dataTotalLength);
				data.setClientId(clientConnect.getClientId());
				data.setRequestId(requestId);
				data.setSegmentCount(segmentCount);
				data.setCurrentSegment(i);
				data.setDataStartPosition(getDataStartPosition(i, perLength));
				data.setDataLength(getDataLength(dataTotalLength, segmentCount, i, perLength));
				data.setData(getData(data, randomFile));
				
				Channel fileChannel = getChannel(i,clientConnect.getFileChannel());
				
				currentTransferFile.addCurrentLength(data.getDataLength());
				currentTransferFile.setTotalLength(dataTotalLength);
				
				fileChannel.writeAndFlush(data);
				
				System.out.println("---当前段---"+i);
				progressBar.start();
			}
		} catch (Exception e) {
			LOGGER.error("文件传输失败",e);
		}
		
	}
	
	private Channel getChannel(int currentSegment,List<Channel> channelList) {
		int channelSize = channelList.size();
		if(channelSize == 1) {
			return channelList.get(0);
		}
		
		int index = currentSegment%channelSize;
		return channelList.get(index);
	}
	
	private byte[] getData(PortSegmentData data,byte[] screenData) {
		byte[] dest = new byte[(int)data.getDataLength()];
		
		System.arraycopy(screenData, (int)data.getDataStartPosition(), dest, 0, (int)data.getDataLength());
		return dest;
	}
	
	private byte[] getData(PortSegmentStreamData data,RandomAccessFile randomFile) {
		byte[] b = new byte[(int)data.getDataLength()];
		try {
			randomFile.seek((int)data.getDataStartPosition());
			randomFile.read(b, 0, (int)data.getDataLength());
		} catch (Exception e) {
			LOGGER.error("读取数据失败",e);
		}
		return b;
	}
	
	private long getDataLength(long dataTotalLength,int segmentCount,int currentSegment,long perLength) {
		if((currentSegment+1) < segmentCount) {
			return perLength;
		}
		return dataTotalLength-(currentSegment*perLength);
	}
	
	private long getDataStartPosition(int currentSegment,long perLength) {
		return currentSegment*perLength;
	}
	
	private int getSegmentCount(long total,long perLength) {
		
		if((total%perLength==0)) {
			return (int)(total/perLength);
		}
		return (int)((total/perLength)+1);
	}
	
	/**
	 * 客户端配对
	 */
	private void clientPair(String sendClientId) {
		Channel channel = clientConnect.getCmdChannel();
		
		try {
			ClientPairVO cp = new ClientPairVO();
			cp.setClientId(clientConnect.getClientId());
			cp.setSendClientId(sendClientId);
			byte[] data = JSON.toJSONString(cp).getBytes(CommonUtil.getCharset());
			
			CompleteData cd = new CompleteData();
			cd.setClientId(clientConnect.getClientId());
			cd.setDataType(DataType.CLIENT_PAIR);
			cd.setData(data);
			cd.setRequestId(CommonUtil.getUUID());

			channel.writeAndFlush(cd);
		} catch (Exception e) {
			LOGGER.error("配对失败",e);
		}
	}
	
	private void connectServer() {
		
		//初始化管道并向服务端注册管道
		Channel channel = client.getChannel();
		clientConnect.setClientId(CommonUtil.getUUID());
		clientConnect.setCmdChannel(channel);
		
		System.out.println("客户端Id: "+clientConnect.getClientId());
		
		registerChannel(channel, new RegisterChannelVO(clientConnect.getClientId(), "cmd"));
		
		int screenChannel = clientProperties.getScreenChannel();
		for (int i = 0; i < screenChannel; i++) {
			Channel channel2 = client.getChannel();

			clientConnect.getScreenChannel().add(channel2);
			registerChannel(channel2, new RegisterChannelVO(clientConnect.getClientId(), "screen"));
		}
		
		int fileChannel = clientProperties.getFileChannel();
		for (int i = 0; i < fileChannel; i++) {
			Channel channel2 = client.getChannel();
			clientConnect.getFileChannel().add(channel2);
			registerChannel(channel2, new RegisterChannelVO(clientConnect.getClientId(), "file"));
		}
		
		ExtensionLoader.setInstance(ClientConnect.class, clientConnect);
	}
	
	private void registerChannel(Channel channel,RegisterChannelVO registerChannel) {
		try {
			byte[] data = JSON.toJSONString(registerChannel).getBytes(CommonUtil.getCharset());
			
			CompleteData cd = new CompleteData();
			cd.setClientId(clientConnect.getClientId());
			cd.setDataType(DataType.REGISTER_CHANNEL);
			cd.setData(data);
			cd.setRequestId(CommonUtil.getUUID());

			channel.writeAndFlush(cd);
		} catch (Exception e) {
			LOGGER.error("注册失败",e);
		}
	}
	
	

}
