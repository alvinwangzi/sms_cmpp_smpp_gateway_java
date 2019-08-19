/**
 * 
 */
package com.cl.inter.gateway.smpp.message;

import io.netty.buffer.ByteBuf;

/**
 * submit_resp消息
 * 
 * @author
 *
 */
public class SMPPSubmitResp extends SMPPBody {

	private String messageId;

	@Override
	public int encode(ByteBuf buffer) {
		int length = 0;
		byte[] temp = messageId.getBytes();
		buffer.writeBytes(temp);
		buffer.writeByte(0x0);
		length += temp.length + 1;

		return length;
	}

	@Override
	public SMPPBody decode(ByteBuf buffer) {

		byte[] temp = readByteBufString(buffer, 35);
		this.messageId = new String(temp);

		return this;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

}
