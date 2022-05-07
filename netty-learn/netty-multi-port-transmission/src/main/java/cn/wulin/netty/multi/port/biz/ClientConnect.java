package cn.wulin.netty.multi.port.biz;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.Channel;

/**
 * 客户端连接
 * @author wulin
 *
 */
public class ClientConnect {

	/**
	 * 客户端Id
	 */
	private String clientId;
	
	/**
	 * 命令行管道
	 */
	private Channel cmdChannel;
	
	/**
	 * 屏幕传输管道列表
	 */
	List<Channel> screenChannel = new ArrayList<>();
	
	/**
	 * 文件管道列表
	 */
	List<Channel> fileChannel = new ArrayList<>();

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Channel getCmdChannel() {
		return cmdChannel;
	}

	public void setCmdChannel(Channel cmdChannel) {
		this.cmdChannel = cmdChannel;
	}

	public List<Channel> getScreenChannel() {
		return screenChannel;
	}

	public void setScreenChannel(List<Channel> screenChannel) {
		this.screenChannel = screenChannel;
	}

	public List<Channel> getFileChannel() {
		return fileChannel;
	}

	public void setFileChannel(List<Channel> fileChannel) {
		this.fileChannel = fileChannel;
	}

	@Override
	public String toString() {
		return "ClientConnect [clientId=" + clientId + ", cmdChannel=" + cmdChannel + ", screenChannel=" + screenChannel
				+ ", fileChannel=" + fileChannel + "]";
	}
	
	
}
