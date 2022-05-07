package cn.wulin.netty.multi.port.client.data.handler;

import java.io.File;
import java.io.RandomAccessFile;

import cn.wulin.netty.multi.port.biz.vo.ClientCreateFileTransferVO;
import cn.wulin.netty.multi.port.client.ClientDataHandler;
import cn.wulin.netty.multi.port.codec.DataType;
import cn.wulin.netty.multi.port.domain.CompleteData;
import cn.wulin.netty.multi.port.handler.ClientHandler;
import cn.wulin.netty.multi.port.utils.CmdDownloadProgressBar;
import cn.wulin.netty.multi.port.utils.CommonUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 * 客户端文件传输处理器
 * @author wulin
 *
 */
public class ClientCreateFileTransferHandler implements ClientDataHandler{

	@Override
	public int dataType() {
		return DataType.CREATE_FILE_TRANSFER;
	}

	@Override
	public void execute(ChannelHandlerContext ctx, CompleteData msg) throws Exception {
		String filePath = new String(msg.getData(),CommonUtil.getCharset());
		File remoteFile = new File(filePath);
		
		String localFilePath = "F:/resources/temp/temp4/sendData/"+remoteFile.getName();
		File localFile = new File(localFilePath);
		
		ClientCreateFileTransferVO vo = new ClientCreateFileTransferVO();
		vo.setRemoteFilePath(filePath);
		vo.setFileName(remoteFile.getName());
		vo.setLocalFilePath(localFilePath);
		vo.setRequestId(msg.getRequestId());
		
		if(!localFile.getParentFile().exists()) {
			localFile.getParentFile().mkdirs();
		}
		
		localFile.createNewFile();
		
		RandomAccessFile raf = new RandomAccessFile(localFile, "rwd");
		vo.setLocalFile(raf);

		CmdDownloadProgressBar progressBar = new CmdDownloadProgressBar(new CmdDownloadProgressBar.ProgressInfo(){

			@Override
			public String fileName() {
				return vo.getFileName();
			}

			@Override
			public long totalLength() {
				return vo.getTotalLength();
			}

			@Override
			public long currentLength() {
				return vo.getCurrentLength();
			}
		});

		vo.setProgressBar(progressBar);
		ClientHandler.getFileTransferRequest().put(msg.getRequestId(), vo);
	}

}
