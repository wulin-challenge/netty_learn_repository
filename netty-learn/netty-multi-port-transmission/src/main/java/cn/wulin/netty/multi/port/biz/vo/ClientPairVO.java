package cn.wulin.netty.multi.port.biz.vo;

/**
 * 客户端配对vo
 * @author wulin
 *
 */
public class ClientPairVO {
	
	/**
	 * 接受端的客户端Id,这里的接受端只的屏幕数据接收端
	 */
	private String clientId;
	
	/**
	 * 发送端的客户端Id,这里的发送端指的屏幕数据发送端
	 */
	private String sendClientId;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSendClientId() {
		return sendClientId;
	}

	public void setSendClientId(String sendClientId) {
		this.sendClientId = sendClientId;
	}
}
