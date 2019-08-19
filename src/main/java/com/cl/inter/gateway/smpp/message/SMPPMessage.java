package com.cl.inter.gateway.smpp.message;

import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * SMPP
 * 
 * @author zhu_tek
 *
 */
public class SMPPMessage extends SMPPHeader implements Message, Cloneable {

	public final static Logger logger = LogManager.getLogger(SMPPMessage.class);

	public static final int SMPP_NACK = 0x80000000; // 消息头错误响应

	public static final int SMPP_BIND_RECEIVER = 0x00000001; // 接受者身份登录

	public static final int SMPP_BIND_RECEIVER_RESP = 0x80000001; // 接受者身份登录resp

	public static final int SMPP_BIND_TRANSMITTER = 0x00000002; // 发送者身份登录

	public static final int SMPP_BIND_TRANSMITTER_RESP = 0x80000002; // 发送者身份登录resp

	public static final int SMPP_SUBMIT = 0x00000004;

	public static final int SMPP_SUBMIT_RESP = 0x80000004;

	public static final int SMPP_DELIVER = 0x00000005;

	public static final int SMPP_DELIVER_RESP = 0x80000005;

	public static final int SMPP_UNBIND = 0x00000006; // 断开连接

	public static final int SMPP_UNBIND_RESP = 0x80000006; // 断开连接resp

	public static final int SMPP_BIND_TRANSCEIVER = 0x00000009; // 发送接受者身份登录

	public static final int SMPP_BIND_TRANSCEIVER_RESP = 0x80000009; // 发送接受者身份登录resp

	public static final int SMPP_ENQUIRE_LINK = 0x00000015; // 心跳包

	public static final int SMPP_ENQUIRE_LINK_RESP = 0x80000015; // 心跳包响应

	//
	protected SMPPBody body = SMPPBody.NOOP_BODY;

	/**
	 * @return 消息体
	 */
	public SMPPBody getBody() {
		return body;
	}

	/**
	 * @param body 消息体
	 */
	public void setBody(SMPPBody body) {
		this.body = body;
	}

	/***
	 * 设置消息头
	 * 
	 * @param header
	 */
	public void setHeader(SMPPHeader header) {
		this.totalLength = header.getTotalLength();
		this.commandID = header.getCommandID();
		this.commandStatus = header.getCommandStatus();
		this.sequenceId = header.getSequenceId();
	}

	/***
	 * 消息编码
	 */
	@Override
	public int encode(ByteBuf buffer) {

		int index = buffer.writerIndex();

		// 消息头编码
		int size = super.encode(buffer);

		// 消息体编码
		size += getBody().encode(buffer);

		// 消息总长度
		setTotalLength(size);

		buffer.setInt(index, size);

		return size;

	}

	/**
	 * 消息解码
	 */
	@Override
	public SMPPMessage decode(ByteBuf buffer) {

		// 消息头
		super.decode(buffer);

		// 消息体
		SMPPBody body = SMPPBody.createBody(super.getCommandID());
		body.decode(buffer);
		this.body = body;

		return this;
	}

	/**
	 * clone
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {

		}

		SMPPMessage message = new SMPPMessage();
		message.setHeader(this);
		message.setBody(getBody());
		return message;
	}

	/**
	 * 
	 */
	@Override
	public String toString() {
		return getBody() + "COMMANID:" + Integer.toHexString(commandID) + " SEQ:" + sequenceId;
	}
}
