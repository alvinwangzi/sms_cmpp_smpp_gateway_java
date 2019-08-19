package com.cl.inter.gateway.smpp;

import java.util.concurrent.TimeUnit;

import com.cl.inter.gateway.smpp.handler.SMPPControlHandler;
import com.cl.inter.gateway.smpp.handler.SMPPReceiverHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * 客户端pipeline初始化
 * 
 * @author zhu_tek
 *
 */
public class SMPPClientInitializer extends ChannelInitializer<SocketChannel> {

	// 客户端
	private SMPPClient client;

	// 构造
	public SMPPClientInitializer(SMPPClient client) {
		this.client = client;
	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {

		ChannelPipeline pipeline = channel.pipeline();

		// 长度解码器
		pipeline.addLast("length field decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, -4, 0));

		// 解码器
		pipeline.addLast("decoder", new SMPPStreamDecoder());

		// 编码器
		pipeline.addLast("encoder", new SMPPStreamEncoder());

		// 空闲事件将发送UP事件
		pipeline.addLast("idleCheck", new IdleStateHandler(client.getIdleNotifyTime() * 6, client.getIdleNotifyTime(), 0, TimeUnit.SECONDS));

		// 接收控制器
		pipeline.addLast("receiver handler", new SMPPReceiverHandler(client));

		// 总控制器
		pipeline.addLast("controler", new SMPPControlHandler(client));

	}

}
