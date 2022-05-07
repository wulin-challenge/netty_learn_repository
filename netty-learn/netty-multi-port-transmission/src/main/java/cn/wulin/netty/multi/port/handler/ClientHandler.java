package cn.wulin.netty.multi.port.handler;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import cn.wulin.brace.utils.ThreadFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.wulin.netty.multi.port.biz.vo.ClientCreateFileTransferVO;
import cn.wulin.netty.multi.port.client.ClientDataHandler;
import cn.wulin.netty.multi.port.client.data.handler.ClientCreateFileTransferHandler;
import cn.wulin.netty.multi.port.domain.CompleteData;
import cn.wulin.netty.multi.port.domain.PortSegmentData;
import cn.wulin.netty.multi.port.domain.PortSegmentStreamData;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

@Sharable
public class ClientHandler extends ChannelDuplexHandler{
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
	
	/**
	 * 数据处理器
	 */
	private static final List<ClientDataHandler> DATA_HANDLER = new ArrayList<>();
	
	/**
	 * 文件传输请求
	 */
	private static final ConcurrentHashMap<String, ClientCreateFileTransferVO> FILE_TRANSFER_REQUEST = new ConcurrentHashMap<>();

    private static final ExecutorService executor = new ThreadPoolExecutor(8, 8,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(500000),
            new ThreadFactoryImpl("ClientHandler"));
	
	public ClientHandler() {
		super();
		initDataHandler();
	}

	private void initDataHandler() {
		DATA_HANDLER.add(new ClientCreateFileTransferHandler());
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("ClientHandler exceptionCaught",cause);
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		if(msg instanceof CompleteData) {
			CompleteData data = (CompleteData)msg;
			int dataType = data.getDataType();
			
			for (ClientDataHandler clientDataHandler : DATA_HANDLER) {
				if(dataType == clientDataHandler.dataType()) {
					clientDataHandler.execute(ctx, data);
					break;
				}
			}
		}else if(msg instanceof PortSegmentData) {
			PortSegmentData data = (PortSegmentData)msg;
			
		}else if(msg instanceof PortSegmentStreamData) {
			PortSegmentStreamData data = (PortSegmentStreamData)msg;
            executor.submit(()->{
                portSegmentStreamHandler(ctx, data);
            });

		}
	}
	
	private void portSegmentStreamHandler(ChannelHandlerContext ctx,PortSegmentStreamData data) {
		String requestId = data.getRequestId();
		ClientCreateFileTransferVO ft = ClientHandler.getFileTransferRequest().get(requestId);
		RandomAccessFile localFile = ft.getLocalFile();
		try {
			localFile.seek(data.getDataStartPosition());
			localFile.write(data.getData(), 0, (int)data.getDataLength());
			
			ft.setTotalLength(data.getDataTotalLength());
			ft.addCurrentLength(data.getDataLength());

            System.out.println("当前传输大小: "+ft.getCurrentLength()+" -- 当前管道: "+ctx.channel().localAddress());
			ft.getProgressBar().start();
			if(ft.getTotalLength().equals(ft.getCurrentLength())) {
				System.out.println("文件传输完成");
				localFile.close();
				//文件写入完成
			}
		} catch (Exception e) {
			LOGGER.error("文件写入失败!",e);
		}
		
	}

	public static List<ClientDataHandler> getDataHandler() {
		return DATA_HANDLER;
	}
	
	public static ConcurrentHashMap<String, ClientCreateFileTransferVO> getFileTransferRequest() {
		return FILE_TRANSFER_REQUEST;
	}

}
