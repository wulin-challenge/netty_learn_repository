package cn.wulin.netty.file.demo.domain;

import java.io.Serializable;

/**
 * 远程命令
 * @author wulin
 *
 */
public class RemotingCommand implements Serializable{

	private static final long serialVersionUID = 1L;

	/**
	 * 请求类型,1:String
	 */
	private int code;
	
	/**
	 * 请求Id
	 */
	private int requestId;
	
	/**
	 * 0: request,1: response
	 */
	private int commandType = 0;
	
	/**
	 * 请求数据
	 */
	private Object data;
	
	public RemotingCommand() {
	}

	public RemotingCommand(int code, int requestId, int commandType, Object data) {
		super();
		this.code = code;
		this.requestId = requestId;
		this.commandType = commandType;
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getCommandType() {
		return commandType;
	}

	public void setCommandType(int commandType) {
		this.commandType = commandType;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
