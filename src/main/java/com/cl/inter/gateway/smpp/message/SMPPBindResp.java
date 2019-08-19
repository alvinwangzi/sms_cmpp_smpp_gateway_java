package com.cl.inter.gateway.smpp.message;

import io.netty.buffer.ByteBuf;

/**
 * 连接回复消息
 * 
 * @author zhu_tek
 */
public class SMPPBindResp extends SMPPBody {

	// 登录状态
	private String systemId = "";

	@Override
	public int encode(ByteBuf buffer) {
		int length = 0;
		byte[] temp = systemId.getBytes();
		buffer.writeBytes(temp);
		buffer.writeByte(0x0);
		length += temp.length + 1;

		return length;
	}

	/**
	 * 消息注册解码
	 */
	@Override
	public SMPPBody decode(ByteBuf buffer) {
		byte[] temp = readByteBufString(buffer, 16);
		systemId = new String(temp);

		return this;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

}
