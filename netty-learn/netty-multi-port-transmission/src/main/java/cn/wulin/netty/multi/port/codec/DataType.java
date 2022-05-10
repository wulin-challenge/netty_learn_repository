package cn.wulin.netty.multi.port.codec;

public class DataType {
	
	/**
	 * 表示普通字符串数据
	 */
	public static final int STRING = 1;
	
	/**
	 * 表示json数据
	 */
	public static final int JSON = 2;
	
	/**
	 * 注册管道
	 */
	public static final int REGISTER_CHANNEL = 3;
	
	/**
	 * 客户端配对
	 */
	public static final int CLIENT_PAIR = 4;
	
	/**
	 * 创建文件传输
	 */
	public static final int CREATE_FILE_TRANSFER = 5;
	
	/**
	 * 创建屏幕传输
	 */
	public static final int CREATE_SCREEN_TRANSFER = 6;
	
	/**
	 * 通知发送屏幕数据
	 */
	public static final int NOTIFY_SEND_SCREEN = 7;
	
	
	

}
