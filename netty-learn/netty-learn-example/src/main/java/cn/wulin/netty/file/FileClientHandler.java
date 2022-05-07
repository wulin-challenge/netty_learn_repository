/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package cn.wulin.netty.file;

import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class FileClientHandler extends ChannelDuplexHandler {
	RandomAccessFile raf = null;
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
         try {
             raf = new RandomAccessFile("F:/resources/temp/temp4/clientData/111.txt", "rw");
         } catch (Exception e) {
         	e.printStackTrace();
         }
    }
    

    @Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(msg instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf)msg;
			int len = buf.readableBytes();
			byte[] tempBuf = new byte[len];
			buf.readBytes(tempBuf);
			raf.write(tempBuf);
		}
	}



	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

}

