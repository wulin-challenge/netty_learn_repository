package cn.wulin.heartbeat.client;

import java.io.IOException;
import java.util.Date;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 心跳检测处理类
 * @author wubo
 *
 */
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter{
	
	/**
	 * 上次心跳时间
	 */
	private Long lastBeatTime=0L;
	
	/**
	 * 空闲时间
	 */
	private Long idleTime;
	
	private String host;
	private Integer port;
	
	private Bootstrap bootstrap;
	
	public HeartBeatClientHandler(Long idleTime, String host, Integer port, Bootstrap bootstrap) {
		super();
		this.idleTime = idleTime;
		this.host = host;
		this.port = port;
		this.bootstrap = bootstrap;
	}

	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		long currentTime = System.currentTimeMillis();
		currentTime -= this.idleTime;
	    System.out.println("客户端循环心跳监测发送: "+new Date());
	    if (evt instanceof IdleStateEvent){
	        IdleStateEvent event = (IdleStateEvent)evt;
	        if (event.state()== IdleState.WRITER_IDLE){
	            if (currentTime>lastBeatTime){
	            	lastBeatTime = currentTime;
	                ctx.writeAndFlush("biubiu");
	            }
	        }
	    }
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if(cause instanceof IOException){
			//失败重新连接
			bootstrap.connect(host,port);
		}else{
			super.exceptionCaught(ctx, cause);
		}
	}
	
	
	
	
}
