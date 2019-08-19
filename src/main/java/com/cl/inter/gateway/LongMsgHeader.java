package com.cl.inter.gateway;

import io.netty.buffer.ByteBuf;

/*
 * SMPP协议长短信分割后，每条子短信内容的头部信息
 * @auth ydj
 */
public class LongMsgHeader {

	private byte[] bytes = {0x05,0x00,0x03,0x00,0x00,0x00};
	private byte total;
	private byte number;
	private byte serial;
	
	public byte[] getBytes(){
		bytes[3] = serial;
		bytes[4] = total;
		bytes[5] = number;
		return bytes;
	}
	
	
	public void setSerial(byte serial){
		this.serial = serial;
	}
	
	//设置总条数
	public void setTotal(byte total){
		this.total = total;
	}
	
	//设置当前是第几条
	public void setNumber(byte number){
		this.number = number;
	}
	
	public static LongMsgHeader decode(ByteBuf buffer){
		LongMsgHeader msgHeader = new LongMsgHeader();
		buffer.readByte();
		buffer.readByte();
		buffer.readByte();
		byte serial = buffer.readByte();
		byte total = buffer.readByte();
		byte number = buffer.readByte();
		msgHeader.setSerial(serial);
		msgHeader.setTotal(total);
		msgHeader.setNumber(number);
		return msgHeader;
	}

	public byte getTotal() {
		return total;
	}

	public byte getNumber() {
		return number;
	}

	public byte getSerial() {
		return serial;
	}

	@Override
	public String toString() {
		return "[" + serial + "," + total + "," + number + "]";
	}
	
}
