package cn.wulin.netty.multi.port.utils;

import java.util.concurrent.atomic.AtomicLong;

import cn.wulin.netty.multi.port.utils.CmdDownloadProgressBar.ProgressInfo;

/**
 * 命令行下载进度条
 * @author wubo
 *
 */
public class CmdDownloadProgressBar {
	private volatile boolean start = false;
	private Object lock = new Object();
	private ProgressInfo progressInfo;
	private String lastPrintInfo = "";
	private long lastPrintLength = 0L;
	private long startTime;
	private long endTime;
	
	public CmdDownloadProgressBar(String fileName,Long totalLength) {
		progressInfo = new InnerProgressInfo(fileName, totalLength);
	}
	
	/**
	 * 自定义进度信息
	 * @param progressInfo
	 */
	public CmdDownloadProgressBar(ProgressInfo progressInfo) {
		super();
		this.progressInfo = progressInfo;
	}
	
	public long addCurrentLength(long length) {
		return progressInfo.addCurrentLength(length);
	}

	public void setTotalLength(long totalLength) {
		progressInfo.setTotalLength(totalLength);
	}
	
	/**
	 * 命令行下载开启方法,该方法可以执行多次
	 */
	public void start() {
		if(!start) {
			synchronized (lock) {
				if(!start) {
					start = true;
					startTime = System.currentTimeMillis();
					
					Thread thread = new Thread(()->{
						while(true){
							try {
								Thread.sleep(1000);

							} catch (Exception e) {
								e.printStackTrace();
							}
							printProgressInfo();

						}

					});
					thread.start();
				}
			}
		}
	}
	
	private void printProgressInfo() {
		deletePrint(lastPrintInfo);

		long totalLength = progressInfo.totalLength()/1024;
		long v = progressInfo.currentLength()-lastPrintLength;
		v = v/1024;

		endTime = System.currentTimeMillis();
		long totalTime = (endTime-startTime)/1000;
		String p = "--"+progressInfo.fileName()+"("+totalLength+" kb)---- "+v+" kb/s ----总时间: "+totalTime+" s";
		System.out.print(p);
		lastPrintLength = progressInfo.currentLength();
		lastPrintInfo = p;
	}

	private void deletePrint(String p) {
		for (int i = 0; i < p.length(); i++) {
			System.out.print("\b");
		}
	}

	/**
	 * 进度信息接口
	 * @author wulin
	 *
	 */
	public static interface ProgressInfo{
		
		/**
		 * 文件名
		 * @return
		 */
		String fileName();
		
		/**
		 * 总长度
		 * @return
		 */
		long totalLength();
		
		default void setTotalLength(long length) {
			
		}
		
		/**
		 * 当前已经下载的长度
		 * @return
		 */
		long currentLength();
		
		default long addCurrentLength(long length) {
			return 0;
		}

	}
	
}

class InnerProgressInfo implements ProgressInfo{
	
	private AtomicLong currentLength = new AtomicLong();
	private String fileName;
	private long totalLength = 0L;
	
	public InnerProgressInfo(String fileName, long totalLength) {
		super();
		this.fileName = fileName;
		this.totalLength = totalLength;
	}
	
	public long addCurrentLength(long length) {
		return currentLength.addAndGet(length);
	}
	
	@Override
	public String fileName() {
		return fileName;
	}

	@Override
	public long totalLength() {
		return totalLength;
	}

	@Override
	public long currentLength() {
		return currentLength.get();
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setTotalLength(long totalLength) {
		this.totalLength = totalLength;
	}
	
}
