package com.cl.inter.gateway.smpp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cl.inter.gateway.smpp.message.SMPPMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * SMPP协议 数据流编码
 * 
 * @author zhu_tek
 */
public class SMPPStreamEncoder extends MessageToByteEncoder<SMPPMessage> {

	// 日志
	private final static Logger logger = LogManager.getLogger(SMPPStreamEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext context, SMPPMessage message, ByteBuf buff) throws Exception {
		try {
			// 消息长度
			int size = 0;
			size = message.encode(buff);
			// 校正消息体长度
			message.setTotalLength(size);
			buff.setInt(0, size);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			logger.error("SMPP编码消息:" + message.toString());
//			logger.debug("SMPP ENCODE: " + ByteBufUtil.prettyHexDump(buff));
		}

	}
}
