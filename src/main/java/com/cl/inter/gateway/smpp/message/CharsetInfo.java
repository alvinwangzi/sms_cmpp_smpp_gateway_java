package com.cl.inter.gateway.smpp.message;


import com.cl.inter.util.CommUtil;

import java.io.UnsupportedEncodingException;

/**
 * 消息编码
 * 
 * @author zhu_tek
 */
public enum CharsetInfo {

	GBK("GBK", 15),

	UCS2("UnicodeBigUnmarked", 8),

	ASCII("iso-8859-1", 3),
	
	INDONESIA("iso-8859-1", 0);

	private CharsetInfo(String charsetName, int code) {
		this.charsetName = charsetName;
		this.code = code;
	}

	private String charsetName;
	private int code;

	public byte[] encode(String content) {
		try {
			return content.getBytes(charsetName);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public String decode(byte[] content) {
		try {
			return new String(content, charsetName);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public String getCharsetName() {
		return charsetName;
	}

	public int getCode() {
		return code;
	}

	public static CharsetInfo fromCMPPFormat(int format) {
		switch (format) {
		case 8:
			return UCS2;
		case 15:
			return GBK;
		case 0:
			return INDONESIA;
		}
		return ASCII;
	}

	/**
	 * SMPP短信内容字符集判断
	 * 
	 * @param content
	 * @return
	 */
	public static CharsetInfo getSmppCharsetInfo(String content) {
		CharsetInfo ret = CharsetInfo.UCS2;
		if (CommUtil.checkLatinCharset(content)) {
			ret = CharsetInfo.ASCII;
		}
		// try {
		// int byteLen = content.getBytes("UTF-8").length;
		// if(byteLen == content.length()){
		// ret = CharsetInfo.ASCII;
		// }
		// } catch (UnsupportedEncodingException e) {
		// }
		return ret;
	}

}
