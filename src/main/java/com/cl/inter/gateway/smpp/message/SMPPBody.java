package com.cl.inter.gateway.smpp.message;

import io.netty.buffer.ByteBuf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * SMPP
 * 
 * @author zhu_tek
 * 
 */
public abstract class SMPPBody implements Message {

	private static final Logger logger = LogManager.getLogger(SMPPBody.class);

	private static Map<Integer, Class<?>> BODY_TYPE = new HashMap<Integer, Class<?>>();

	static {
		registerBodyType(SMPPMessage.SMPP_NACK, NOOPBody.class);
		registerBodyType(SMPPMessage.SMPP_BIND_RECEIVER, SMPPBind.class);
		registerBodyType(SMPPMessage.SMPP_BIND_RECEIVER_RESP, SMPPBindResp.class);
		registerBodyType(SMPPMessage.SMPP_BIND_TRANSMITTER, SMPPBind.class);
		registerBodyType(SMPPMessage.SMPP_BIND_TRANSMITTER_RESP, SMPPBindResp.class);
		registerBodyType(SMPPMessage.SMPP_BIND_TRANSCEIVER, SMPPBind.class);
		registerBodyType(SMPPMessage.SMPP_BIND_TRANSCEIVER_RESP, SMPPBindResp.class);
		registerBodyType(SMPPMessage.SMPP_SUBMIT, SMPPSubmit.class);
		registerBodyType(SMPPMessage.SMPP_SUBMIT_RESP, SMPPSubmitResp.class);
		registerBodyType(SMPPMessage.SMPP_DELIVER, SMPPSubmit.class);
		registerBodyType(SMPPMessage.SMPP_DELIVER_RESP, SMPPSubmitResp.class);
		registerBodyType(SMPPMessage.SMPP_UNBIND, NOOPBody.class);
		registerBodyType(SMPPMessage.SMPP_UNBIND_RESP, NOOPBody.class);
		registerBodyType(SMPPMessage.SMPP_ENQUIRE_LINK, NOOPBody.class);
		registerBodyType(SMPPMessage.SMPP_ENQUIRE_LINK_RESP, NOOPBody.class);
	}

	protected final static void registerBodyType(int commandId, Class<?> bodyType) {
		BODY_TYPE.put(commandId, bodyType);
	}

	public static final SMPPBody NOOP_BODY = new NOOPBody();

	public abstract int encode(ByteBuf buffer);

	public abstract SMPPBody decode(ByteBuf buffer);

	public static SMPPBody decodeBody(int commandId, ByteBuf buffer) {
		SMPPBody body = createBody(commandId);
		body.decode(buffer);
		return body;
	}

	public static SMPPBody createBody(int commandId) {
		SMPPBody body = null;
		try {
			body = SMPPBody.newInstance(commandId);
		} catch (InstantiationException e) {
			logger.error(e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error(e.getMessage());
		}
		return body;
	}

	public final static Class<?> getBodyType(int commandId) {
		return BODY_TYPE.get(commandId);
	}

	/***
	 * 
	 * @param commandId
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static SMPPBody newInstance(int commandId) throws InstantiationException, IllegalAccessException {
		Class<?> type = getBodyType(commandId);
		if (type == null) {
			return new NOOPBody();
		}
		return (SMPPBody) type.newInstance();
	}
	
	public static byte[] readByteBufString(ByteBuf buffer, int max) {
		if (max <= 0 || buffer.readableBytes() <= 0) {
			return new byte[0];
		}
		byte[] retByte = new byte[max];
		int offset = 0;
		for (; offset < max; offset++) {
			byte temp = buffer.readByte();
			if (temp == 0x0) {
				break;
			}
			retByte[offset] = temp;
		}
		return Arrays.copyOf(retByte, offset);
	}

	/**
	 * 空消息体
	 * 
	 * @author zhu_tek
	 */
	public static class NOOPBody extends SMPPBody {
		@Override
		public int encode(ByteBuf buffer) {
			return 0;
		}

		@Override
		public SMPPBody decode(ByteBuf buffer) {
			return this;
		}
	}
}
