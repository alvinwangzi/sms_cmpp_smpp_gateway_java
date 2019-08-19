package com.cl.inter.gateway.smpp;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cl.inter.gateway.smpp.message.SMPPBody;
import com.cl.inter.gateway.smpp.message.SMPPHeader;
import com.cl.inter.gateway.smpp.message.SMPPMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * SMPP消息解码
 * 
 * @author zhu_tek
 */
public class SMPPStreamDecoder extends ByteToMessageDecoder {
	private static Logger logger = LogManager.getLogger(SMPPStreamDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext context, ByteBuf buff, List<Object> out) throws Exception {
		if (buff.readableBytes() <= 0) {
			return;
		}
		try {
			SMPPMessage message = new SMPPMessage();

			SMPPHeader header = new SMPPHeader();
			header.decode(buff);
			message.setHeader(header);
			SMPPBody body = SMPPBody.decodeBody(message.getCommandID(), buff);
			message.setBody(body);
			out.add(message);
		} catch (Exception e) {
			logger.error("SMPP decode error!", e);
		}
		if (buff.readableBytes() > 0) {
			// logger.error("SMPP decode have remain
			// bytes:"+ByteBufUtil.prettyHexDump(buff));
			byte[] temp = new byte[buff.readableBytes()];
			buff.readBytes(temp);
		}
	}
}
