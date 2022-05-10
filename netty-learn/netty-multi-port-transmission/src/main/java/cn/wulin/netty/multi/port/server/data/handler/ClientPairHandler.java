package cn.wulin.netty.multi.port.server.data.handler;

import com.alibaba.fastjson.JSON;

import cn.wulin.netty.multi.port.biz.vo.ClientPairVO;
import cn.wulin.netty.multi.port.codec.DataType;
import cn.wulin.netty.multi.port.domain.CompleteData;
import cn.wulin.netty.multi.port.handler.ServerHandler;
import cn.wulin.netty.multi.port.server.ServerDataHandler;
import cn.wulin.netty.multi.port.utils.CommonUtil;
import io.netty.channel.ChannelHandlerContext;

/**
 * 客户端配对处理器
 * @author wulin
 *
 */
public class ClientPairHandler implements ServerDataHandler{

	@Override
	public int dataType() {
		return DataType.CLIENT_PAIR;
	}

	@Override
	public void execute(ChannelHandlerContext ctx, CompleteData data) throws Exception {

		String json = new String(data.getData(), CommonUtil.getCharset());
		ClientPairVO cp = JSON.parseObject(json, ClientPairVO.class);
		//相互配对
		ServerHandler.getRemoteConnectPair().put(cp.getClientId(), cp.getSendClientId());
		ServerHandler.getRemoteConnectPair().put(cp.getSendClientId(), cp.getClientId());
		
		System.out.println("成功配对: 接收端:"+cp.getClientId()+" , 发送端: "+cp.getSendClientId());
	}

}
