package cn.wulin.netty.multi.port.biz;

public class ClientProperties {
	private static ClientProperties clientProperties;
	

	/**
	 * 屏幕传输管道数量
	 */
	private int screenChannel = 3;
	
	/**
	 * 文件传输管道数量
	 */
	private int fileChannel = 3;
	
	private int serverPort = 8686;
	
//	private String serverIp = "wulinThinkPad";
	private String serverIp = "121.5.149.59";
	
	private ClientProperties() {}

	public int getScreenChannel() {
		return screenChannel;
	}

	public void setScreenChannel(int screenChannel) {
		this.screenChannel = screenChannel;
	}

	public int getFileChannel() {
		return fileChannel;
	}

	public void setFileChannel(int fileChannel) {
		this.fileChannel = fileChannel;
	}
	
	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public static ClientProperties getClientProperties() {
		if(clientProperties == null) {
			synchronized (ClientProperties.class) {
				if(clientProperties == null) {
					clientProperties = new ClientProperties();
				}
			}
		}
		return clientProperties;
	}
	
}
