package cn.wulin.netty.multi.port.server.data.handler;

import com.alibaba.fastjson.JSON;

import cn.wulin.netty.multi.port.biz.ClientConnect;
import cn.wulin.netty.multi.port.biz.vo.RegisterChannelVO;
import cn.wulin.netty.multi.port.codec.DataType;
import cn.wulin.netty.multi.port.domain.CompleteData;
import cn.wulin.netty.multi.port.handler.ServerHandler;
import cn.wulin.netty.multi.port.server.ServerDataHandler;
import cn.wulin.netty.multi.port.utils.CommonUtil;
import io.netty.channel.ChannelHandlerContext;

public class RegisterChannelHandler implements ServerDataHandler{

	@Override
	public int dataType() {
		return DataType.REGISTER_CHANNEL;
	}

	@Override
	public void execute(ChannelHandlerContext ctx, CompleteData data) throws Exception {
		
		String json = new String(data.getData(), CommonUtil.getCharset());
		RegisterChannelVO rc = JSON.parseObject(json, RegisterChannelVO.class);
		
		System.out.println(data.getClientId()+" :"+rc.getChannelType()+" 注册成功 "+ctx.channel().remoteAddress());
		if("cmd".equals(rc.getChannelType())) {
			regiterCmdChannel(ctx,rc);
		}else if("screen".equals(rc.getChannelType())) {
			regiterScreenChannel(ctx,rc);
		}else if("file".equals(rc.getChannelType())) {
			regiterFileChannel(ctx,rc);
		}
		
		System.out.println(ServerHandler.getClientConnect(rc.getClientId()).toString());
	}

	private void regiterFileChannel(ChannelHandlerContext ctx, RegisterChannelVO rc) {
		ClientConnect clientConnect = ServerHandler.getClientConnect(rc.getClientId());
		clientConnect.getFileChannel().add(ctx.channel());
	}

	private void regiterScreenChannel(ChannelHandlerContext ctx, RegisterChannelVO rc) {
		ClientConnect clientConnect = ServerHandler.getClientConnect(rc.getClientId());
		clientConnect.getScreenChannel().add(ctx.channel());
	}

	private void regiterCmdChannel(ChannelHandlerContext ctx, RegisterChannelVO rc) {
		ClientConnect clientConnect = ServerHandler.getClientConnect(rc.getClientId());
		
		clientConnect.setClientId(rc.getClientId());
		clientConnect.setCmdChannel(ctx.channel());
	}
}
