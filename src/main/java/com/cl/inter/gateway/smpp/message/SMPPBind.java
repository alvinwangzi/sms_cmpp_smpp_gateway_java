package com.cl.inter.gateway.smpp.message;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.buffer.ByteBuf;

/**
 * 请求receiver连接消息体
 * 
 * @author ydj
 */
public class SMPPBind extends SMPPBody {

	// 接口ID
	private String systemId;

	// 接口密码
	private String password;

	// 接口类型
	private String systemType = "";

	// 接口版本号
	private byte version = 0x34;

	// 编码类型
	private byte addrTon = (byte) 0;

	// 编码方案
	private byte addrNpi = (byte) 0;

	// 路由
	private String addressRange = "";

	// 日志实例
	private static final Logger logger = LogManager.getLogger(SMPPBind.class);

	/**
	 * 构造函数
	 * 
	 * @param account
	 * @param password
	 * @Param spType
	 */
	public SMPPBind(String account, String password,String syType) {
		super();

		this.systemId = account;
		this.password = password;
		if(StringUtils.isNotBlank(syType)){
			this.systemType = syType;
		}
	}

	public SMPPBind() {
		super();
	}

	/**
	 * 注册消息体编码
	 */
	@Override
	public int encode(ByteBuf buffer) {

		int length = 0;
		byte[] temp = systemId.getBytes();
		buffer.writeBytes(temp);
		buffer.writeByte(0x0);
		length += temp.length + 1;

		temp = password.getBytes();
		buffer.writeBytes(temp);
		buffer.writeByte(0x0);
		length += temp.length + 1;

		temp = systemType.getBytes();
		buffer.writeBytes(temp);
		buffer.writeByte(0x0);
		length += temp.length + 1;

		buffer.writeByte(version);
		length += 1;

		buffer.writeByte(addrTon);
		length += 1;

		buffer.writeByte(addrNpi);
		length += 1;

		temp = addressRange.getBytes();
		buffer.writeBytes(temp);
		buffer.writeByte(0x0);
		length += temp.length + 1;

		return length;
	}

	@Override
	public SMPPBody decode(ByteBuf buffer) {
		try {
			byte[] temp = readByteBufString(buffer, 16);
			systemId = new String(temp);

			temp = readByteBufString(buffer, 9);
			password = new String(temp);

			temp = readByteBufString(buffer, 13);
			systemType = new String(temp);

			version = buffer.readByte();

			addrTon = buffer.readByte();

			addrNpi = buffer.readByte();

			temp = readByteBufString(buffer, 41);
			addressRange = new String(temp);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return this;
	}

	public String toString() {
		return new String(systemId) + "," + new String(password);
	}

	public String getSystemId() {
		return new String(systemId).trim();
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public byte getAddrTon() {
		return addrTon;
	}

	public void setAddrTon(byte addrTon) {
		this.addrTon = addrTon;
	}

	public byte getAddrNpi() {
		return addrNpi;
	}

	public void setAddrNpi(byte addrNpi) {
		this.addrNpi = addrNpi;
	}

	public String getAddressRange() {
		return addressRange;
	}

	public void setAddressRange(String addressRange) {
		this.addressRange = addressRange;
	}

}
