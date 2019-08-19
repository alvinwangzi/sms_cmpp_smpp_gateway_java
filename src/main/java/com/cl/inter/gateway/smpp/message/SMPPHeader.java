package com.cl.inter.gateway.smpp.message;

import io.netty.buffer.ByteBuf;

/**
 * CMPP
 * 
 * @author zhu_tek
 */
public class SMPPHeader implements Message {

	protected int totalLength;
	protected int commandID;
	protected int commandStatus;
	protected int sequenceId;

	public final static int HEADER_LENGTH = 16;

	public int getTotalLength() {
		return totalLength;
	}

	public void setTotalLength(int totalLength) {
		this.totalLength = totalLength;
	}

	public int getCommandID() {
		return commandID;
	}

	public void setCommandID(int commandID) {
		this.commandID = commandID;
	}

	public int getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}

	public int getCommandStatus() {
		return commandStatus;
	}

	public void setCommandStatus(int commandStatus) {
		this.commandStatus = commandStatus;
	}

	/**
	 * 
	 */
	@Override
	public int encode(ByteBuf buffer) {
		int length = 0;
		buffer.writeInt(getTotalLength());
		length += 4;
		buffer.writeInt(getCommandID());
		length += 4;
		buffer.writeInt(getCommandStatus());
		length += 4;
		buffer.writeInt(getSequenceId());
		length += 4;
		return length;
	}

	/**
	 * 
	 */
	@Override
	public SMPPHeader decode(ByteBuf buffer) {
		setTotalLength(buffer.readInt());
		setCommandID(buffer.readInt());
		setCommandStatus(buffer.readInt());
		setSequenceId(buffer.readInt());
		return this;
	}
}
