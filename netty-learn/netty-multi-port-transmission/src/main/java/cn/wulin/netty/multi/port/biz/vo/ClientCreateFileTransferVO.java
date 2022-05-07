package cn.wulin.netty.multi.port.biz.vo;

import cn.wulin.netty.multi.port.utils.CmdDownloadProgressBar;

import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicLong;

public class ClientCreateFileTransferVO {
	private String requestId;
	
	/**
	 * 文件名称
	 */
	private String fileName;
	
	/**
	 * 远端的文件路径
	 */
	private String remoteFilePath;
	
	/**
	 * 当前的本地文件路径
	 */
	private String localFilePath;
	
	private RandomAccessFile localFile;
	
	/**
	 * 文件总大小
	 */
	private Long totalLength = 0L;

	private CmdDownloadProgressBar progressBar;
	
	/**
	 * 当前的文件大小
	 */
	private volatile AtomicLong currentLength = new AtomicLong(0L);
	
	public void addCurrentLength(long length) {
		currentLength.addAndGet(length);
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Long getTotalLength() {
		return totalLength;
	}

	public void setTotalLength(Long totalLength) {
		this.totalLength = totalLength;
	}

	public long getCurrentLength() {
		return currentLength.get();
	}

	public String getRemoteFilePath() {
		return remoteFilePath;
	}

	public void setRemoteFilePath(String remoteFilePath) {
		this.remoteFilePath = remoteFilePath;
	}

	public String getLocalFilePath() {
		return localFilePath;
	}

	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}

	public RandomAccessFile getLocalFile() {
		return localFile;
	}

	public void setLocalFile(RandomAccessFile localFile) {
		this.localFile = localFile;
	}

	public CmdDownloadProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(CmdDownloadProgressBar progressBar) {
		this.progressBar = progressBar;
	}
}
