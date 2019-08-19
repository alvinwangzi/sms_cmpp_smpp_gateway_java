package com.cl.inter.gateway.smpp.message;

import io.netty.buffer.ByteBuf;

/**
 * message接口
 * @author zhu_tek
 */
public interface Message {
	
	/***
	 * 消息对象编码至buffer
	 * @param buffer
	 * @return 编码长度
	 */
	public abstract int encode(ByteBuf buffer);
	
	/***
	 * buffer解码为消息对象
	 * @param buffer
	 * @return
	 */
	public abstract Message decode(ByteBuf buffer);
	

}
