package com.cl.inter.gateway.smpp.message;

import com.cl.inter.gateway.LongMsgHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 短信发送消息
 * 
 * @author
 *
 */
public class SMPPSubmit extends SMPPBody implements Cloneable {
	
	private final Logger logger = LogManager.getLogger("submit_sm_pdu");

	private final static byte REPORT_MASK = 0X04;
	// private final static byte[] TAG_MESSAGE_PAYLOAD = {0x04,0x24};

	private String serviceType = ""; // 保留字段，为将来扩展用。必须设为NULL。
	private byte sourceAddrTon = (byte) 0; // 源地址编码类型，如不需要，可设为NULL
	private byte sourceAddrNpi = (byte) 0; // 源地址编码方案，如不需要，可设为NULL
	private String sourceAddr = ""; // 提交该短消息的SME的地址。是提交的短消息的源地址。
	private byte destAddrTon = (byte) 0; // 目的地址编码类型。
	private byte destAddrNpi = (byte) 0; // 目的地址编码方案。
	private String destinationAddr = ""; // 短消息的目的地址。对于移动终止的短消息来说，它就是目的手机的MSISDN。
	// 该字短为短消息类型。对submit_sm消息来说，该字段必须为NULL;对deliver_sm消息来说，该字段表示这条消息是状态报告。
	private byte esmClass = (byte) 0;
	private byte protocolID = (byte) 0; // GSM协议类型
	private byte priorityFlag = (byte) 1; // 短消息的优先级。 0:普通 1:高级
	private String scheduleDeliveryTime = ""; // 该字段表示计划下发该短消息的时间。
	private String validityPeriod = ""; // 短消息的最后生存期限。
	private byte registeredDeliveryFlag = (byte) 1; // 是否需要状态报告。 0:不需要 1:需要
	// 替换短消息标志。即当提交的短消息的源地址和目的地址相同时，是否替换存在的短消息。0：不替换 1：替换
	private byte replaceIfPresentFlag = (byte) 0;
	private CharsetInfo dataCoding = CharsetInfo.UCS2; // 信息格式
	private byte smDefaultMsgId = (byte) 0; // 预定义短消息ID。该ID是短消息中心管理者建立的预定义短消息表的索引。
	private String shortMessage = ""; // 短消息数据内容。
	private LongMsgHeader msgHeader = null;

	@Override
	public int encode(ByteBuf buffer) {
		int size = 0;

		byte[] temp = serviceType.getBytes();
		buffer.writeBytes(temp);
		buffer.writeByte(0x0);
		size += temp.length + 1;

		buffer.writeByte(sourceAddrTon);
		size += 1;

		buffer.writeByte(sourceAddrNpi);
		size += 1;

		temp = sourceAddr.getBytes();
		buffer.writeBytes(temp);
		buffer.writeByte(0x0);
		size += temp.length + 1;

		buffer.writeByte(destAddrTon);
		size += 1;

		buffer.writeByte(destAddrNpi);
		size += 1;

		temp = destinationAddr.getBytes();
		buffer.writeBytes(temp);
		buffer.writeByte(0x0);
		size += temp.length + 1;

		buffer.writeByte(esmClass);
		size += 1;

		buffer.writeByte(protocolID);
		size += 1;

		buffer.writeByte(priorityFlag);
		size += 1;

		temp = scheduleDeliveryTime.getBytes();
		buffer.writeBytes(temp);
		buffer.writeByte(0x0);
		size += temp.length + 1;

		temp = validityPeriod.getBytes();
		buffer.writeBytes(temp);
		buffer.writeByte(0x0);
		size += temp.length + 1;

		buffer.writeByte(registeredDeliveryFlag);
		size += 1;

		buffer.writeByte(replaceIfPresentFlag);
		size += 1;

		buffer.writeByte(dataCoding.getCode());
		size += 1;

		buffer.writeByte(smDefaultMsgId);
		size += 1;

		byte[] byte_content = dataCoding.encode(shortMessage);
		// 如果设置了短信内容头信息，则要拼入短信信息
		if (msgHeader != null) {
			byte[] headerBytes = msgHeader.getBytes();
			buffer.writeByte(byte_content.length + headerBytes.length);
			size += 1;
			buffer.writeBytes(headerBytes);
			size += headerBytes.length;
		} else {
			buffer.writeByte(byte_content.length);
			size += 1;
		}
		buffer.writeBytes(byte_content);
		size += byte_content.length;
		logger.info("smpp encode :\r\n" + ByteBufUtil.prettyHexDump(buffer));
		return size;
	}

	@Override
	public SMPPBody decode(ByteBuf buffer) {
		
		logger.info("smpp decode :\r\n" + ByteBufUtil.prettyHexDump(buffer));

		byte[] temp = readByteBufString(buffer, 6);
		this.serviceType = new String(temp);

		this.sourceAddrTon = buffer.readByte();

		this.sourceAddrNpi = buffer.readByte();

		temp = readByteBufString(buffer, 21);
		this.sourceAddr = new String(temp);

		this.destAddrTon = buffer.readByte();

		this.destAddrNpi = buffer.readByte();

		temp = readByteBufString(buffer, 21);
		this.destinationAddr = new String(temp);

		this.esmClass = buffer.readByte();


		this.protocolID = buffer.readByte();

		this.priorityFlag = buffer.readByte();

		temp = readByteBufString(buffer, 17);
		this.scheduleDeliveryTime = new String(temp);

		temp = readByteBufString(buffer, 17);
		this.validityPeriod = new String(temp);

		this.registeredDeliveryFlag = buffer.readByte();

		this.replaceIfPresentFlag = buffer.readByte();

		if (isDelivery()) {
			logger.info("delivery encode :\r\n" + ByteBufUtil.prettyHexDump(buffer));
		}
		
		this.dataCoding = CharsetInfo.fromCMPPFormat(buffer.readByte());

		this.smDefaultMsgId = buffer.readByte();

		int length = buffer.readByte() & 0xff;
		byte[] content = new byte[length];
		buffer.readBytes(content);
		this.shortMessage = dataCoding.decode(content);
		
		return this;
	}

	@Override
	public Object clone() {

		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {

		}
		return null;
	}

	@Override
	public String toString() {
		return "SMPPSubmit [serviceType=" + serviceType + ", sourceAddr=" + sourceAddr + ", destinationAddr="
				+ destinationAddr + ", esmClass=" + Integer.toHexString(esmClass) + ", protocolID=" + protocolID
				+ ", scheduleDeliveryTime=" + scheduleDeliveryTime + ", validityPeriod=" + validityPeriod
				+ ", dataCoding=" + dataCoding.getCharsetName() + ", smDefaultMsgId=" + smDefaultMsgId
				+ ", shortMessage=" + shortMessage + "]";
	}

	public boolean isDelivery() {
		if ((this.esmClass & REPORT_MASK) == REPORT_MASK) {
			// 状态报告
			return false;
		}
		// 上行消息
		return true;
	}

	public String getReceiveAccount() {
		return "";
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public byte getSourceAddrTon() {
		return sourceAddrTon;
	}

	public void setSourceAddrTon(byte sourceAddrTon) {
		this.sourceAddrTon = sourceAddrTon;
	}

	public byte getSourceAddrNpi() {
		return sourceAddrNpi;
	}

	public void setSourceAddrNpi(byte sourceAddrNpi) {
		this.sourceAddrNpi = sourceAddrNpi;
	}

	public String getSourceAddr() {
		return sourceAddr;
	}

	public void setSourceAddr(String sourceAddr) {
		this.sourceAddr = sourceAddr;
	}

	public byte getDestAddrTon() {
		return destAddrTon;
	}

	public void setDestAddrTon(byte destAddrTon) {
		this.destAddrTon = destAddrTon;
	}

	public byte getDestAddrNpi() {
		return destAddrNpi;
	}

	public void setDestAddrNpi(byte destAddrNpi) {
		this.destAddrNpi = destAddrNpi;
	}

	public String getDestinationAddr() {
		return destinationAddr;
	}

	public void setDestinationAddr(String destinationAddr) {
		this.destinationAddr = destinationAddr;
	}

	public byte getEsmClass() {
		return esmClass;
	}

	public void setEsmClass(byte esmClass) {
		this.esmClass = esmClass;
	}

	public byte getProtocolID() {
		return protocolID;
	}

	public void setProtocolID(byte protocolID) {
		this.protocolID = protocolID;
	}

	public byte getPriorityFlag() {
		return priorityFlag;
	}

	public void setPriorityFlag(byte priorityFlag) {
		this.priorityFlag = priorityFlag;
	}

	public String getScheduleDeliveryTime() {
		return scheduleDeliveryTime;
	}

	public void setScheduleDeliveryTime(String scheduleDeliveryTime) {
		this.scheduleDeliveryTime = scheduleDeliveryTime;
	}

	public String getValidityPeriod() {
		return validityPeriod;
	}

	public void setValidityPeriod(String validityPeriod) {
		this.validityPeriod = validityPeriod;
	}

	public byte getRegisteredDeliveryFlag() {
		return registeredDeliveryFlag;
	}

	public void setRegisteredDeliveryFlag(byte registeredDeliveryFlag) {
		this.registeredDeliveryFlag = registeredDeliveryFlag;
	}

	public byte getReplaceIfPresentFlag() {
		return replaceIfPresentFlag;
	}

	public void setReplaceIfPresentFlag(byte replaceIfPresentFlag) {
		this.replaceIfPresentFlag = replaceIfPresentFlag;
	}

	public CharsetInfo getDataCoding() {
		return dataCoding;
	}

	public void setDataCoding(CharsetInfo dataCoding) {
		this.dataCoding = dataCoding;
	}

	public byte getSmDefaultMsgId() {
		return smDefaultMsgId;
	}

	public void setSmDefaultMsgId(byte smDefaultMsgId) {
		this.smDefaultMsgId = smDefaultMsgId;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public LongMsgHeader getMsgHeader() {
		return msgHeader;
	}

	public void setMsgHeader(LongMsgHeader msgHeader) {
		this.msgHeader = msgHeader;
	}

}
