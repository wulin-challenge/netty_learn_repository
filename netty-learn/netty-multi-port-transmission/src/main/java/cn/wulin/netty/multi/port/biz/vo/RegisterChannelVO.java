package cn.wulin.netty.multi.port.biz.vo;

import java.io.Serializable;

/**
 * 注册管道
 * @author wulin
 *
 */
public class RegisterChannelVO implements Serializable{
	private static final long serialVersionUID = 1L;

	private String clientId;
	
	/**
	 * 管道类型:
	 * cmd,screen,file
	 */
	private String channelType;
	
	public RegisterChannelVO() {
		super();
	}

	public RegisterChannelVO(String clientId, String channelType) {
		super();
		this.clientId = clientId;
		this.channelType = channelType;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}
}
