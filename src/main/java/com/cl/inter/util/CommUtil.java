package com.cl.inter.util;


import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CommUtil {

	private final static Logger logger = LogManager.getLogger(CommUtil.class);

	// 短信序号
	private final static AtomicInteger seqNo = new AtomicInteger(1);
	// 批量序号
	private final static AtomicInteger batchNo = new AtomicInteger(1);
	// 消息ID
	private final static AtomicInteger messageIdSeed = new AtomicInteger(1000000000);

	// 时间格式
	private final static ThreadLocal<SimpleDateFormat> df_yyMMddHHmm = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyMMddHHmm");
		}
	};



	/**
	 * HEX字符串转换成byte数组
	 */
	public static byte[] transHexString2bytes(String content) throws NumberFormatException {
		int length = content.length();
		if (length == 0) {
			return new byte[0];
		}
		byte[] bytes = new byte[length / 2];
		for (int i = 0; i < bytes.length; i++) {
			String temp = content.substring(i * 2, i * 2 + 2);
			int hex = 0;
			try {
				hex = Integer.parseInt(temp, 16);
			} catch (NumberFormatException e) {
				throw e;
			}
			bytes[i] = (byte) hex;
		}
		return bytes;
	}

	/**
	 * 获取发送序号
	 */
	public final static int generateMessageSeqNumber() {
		if (seqNo.intValue() > Integer.MAX_VALUE) {
			seqNo.set(1);
		}
		return seqNo.incrementAndGet();
	}

	/**
	 * 获取批量序号
	 */
	public final static String generateBatchSeqNumber(String account) {
		if (batchNo.intValue() > Integer.MAX_VALUE) {
			batchNo.set(1);
		}
		SimpleDateFormat df = df_yyMMddHHmm.get();
		return account + "_" + df.format(new Date()) + "_" + batchNo.incrementAndGet();
	}

	/**
	 * 生产消息ID
	 */
	public final static String generateMessageId() {
		if (messageIdSeed.intValue() > Integer.MAX_VALUE) {
			messageIdSeed.set(1000000000);
		}
		SimpleDateFormat df = df_yyMMddHHmm.get();
		return df.format(new Date()) + messageIdSeed.incrementAndGet();
	}

	/**
	 * 验证手机号码格式
	 * 
	 * @param phone
	 * @return
	 */
	public static boolean checkPhone(String phone) {
		if (phone.length() < 5) {
			return true;
		}
		if (phone.length() > 20) {
			return true;
		}
		if (!phone.matches("^\\d+$")) {
			return true;
		}
		return false;
	}

	/**
	 * 验证客户端IP
	 * 
	 * @param ip
	 * @return
	 */
	public static boolean validClientIp(String ip, String clientIp) {
		String[] ips = ip.split(",");
		for (String iip : ips) {
			if (iip.equals(clientIp)) {
				return true;
			}
		}
		return false;
	}

	// 计算短信条数：
	// 1国际非ASCII字符：最大长度70字符，长短信按67字符分隔
	// 2国际纯ASCII字符：最大长度140字符，长短信按134字符分隔
	public static int getContentNum(String content, String countryNo) {
		int maxSize = Constant.SMPP_MAX_SIZE;
		int splitSize = Constant.SMPP_SPLIT_SIZE;
		if (!"00".equals(countryNo.substring(0, 2))) {
			countryNo = "00" + countryNo;
		}
		// 如果是国内短信
		if ("0086".equals(countryNo)) {
			maxSize = Constant.SMPP_MAX_SIZE;
			splitSize = Constant.SMPP_SPLIT_SIZE;
		} else {
			// 国际短信
			// 如果是 纯Latin字符
			if (checkLatinCharset(content)) {
				maxSize = Constant.SMPP_MAX_SIZE_ASC;
				splitSize = Constant.SMPP_SPLIT_SIZE_ASC;
			} else {
				maxSize = Constant.SMPP_MAX_SIZE;
				splitSize = Constant.SMPP_SPLIT_SIZE;
			}
		}

		int smsNum = 1;
		if (content.length() > maxSize) {
			smsNum = (int) Math.ceil((double) content.length() / splitSize);
		}
		return smsNum;
	}

	/*
	 * 判断短信内容是否是纯英文拉丁字符
	 */
	public static boolean checkLatinCharset(String content) {
		try {
			char[] chars = content.toCharArray();
			byte[] bytes = content.getBytes("iso-8859-1");
			for (int index = 0; index < bytes.length; index++) {
				if (bytes[index] == 63) {
					if (chars[index] != 63) {
						return false;
					}
				}
			}
			return true;
		} catch (Exception e) {
		}
		return false;
	}


	/**
	 * 获取应该在哪个字段操作
	 */
	public static String getChargeField(int smsType, String chargeMode) {
		String result = "";
		if (smsType == Constant.SMS_TYPE[3]) {
			result = chargeMode.equals("1") ? "balance_international" : "advance_international";
		} else if (smsType == Constant.SMS_TYPE[0]) {
			result = chargeMode.equals("1") ? "balance_sms_normal" : "advance_sms_normal";
		} else if (smsType == Constant.SMS_TYPE[1]) {
			result = chargeMode.equals("1") ? "balance_sms_market" : "advance_sms_market";
		} else if (smsType == Constant.SMS_TYPE[2]) {
			result = chargeMode.equals("1") ? "balance_voice" : "advance_voice";
		}
		return result;
	}

	/**
	 * 判断数字是不是0
	 */
	public final static boolean isZero(String value) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		if (value.equals("0")) {
			return true;
		}
		try {
			Double.parseDouble(value);
		} catch (Exception ex) {
			return true;
		}
		return false;
	}


	/**
	 * 获取十进制的日期时间 YYmmddhhmi
	 * 
	 * @param time
	 * @return
	 */
	public static int getYYMMDDHHMM(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(time));
		int timestamp = (calendar.get(Calendar.YEAR) % 100) * 0x5f5e100 + calendar.get(Calendar.MONTH) * 0xf4240
				+ calendar.get(Calendar.DAY_OF_MONTH) * 10000 + calendar.get(Calendar.HOUR) * 100
				+ calendar.get(Calendar.MINUTE);
		return timestamp;
	}



	public static int getMMDDHHMMSS() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int timestamp = (calendar.get(Calendar.MONTH) + 1) * 0x5f5e100 + calendar.get(Calendar.DAY_OF_MONTH) * 0xf4240
				+ calendar.get(Calendar.HOUR) * 10000 + calendar.get(Calendar.MINUTE) * 100
				+ calendar.get(Calendar.SECOND);
		return timestamp;
	}

	public static byte[] Md5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(str.getBytes());
		return md5.digest();
	}

	/***
	 * 去除buffer末尾的0x0
	 */
	public static byte[] trimStringBufferRightZeros(byte[] buffer) {
		int offset = buffer.length - 1;
		while (offset > 0) {
			if (buffer[offset] != 0x0) {
				break;
			}
			offset--;
		}
		return Arrays.copyOf(buffer, offset + 1);
	}

	/***
	 * 解码
	 * 
	 * @param content
	 * @return
	 */
	public static String decodeUCS2String(byte[] content) {
		try {
			return new String(content, "UnicodeBigUnmarked");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}



	/***
	 * 长短信内容拆分,CMPP使用UCS2编码
	 * 
	 * @param content
	 * @return
	 */
	public static List<String> splitLongContenteForCMPP(String messageContent) {
		List<String> strLst = new ArrayList<String>();
		StringBuffer strBuf = new StringBuffer(messageContent);
		int maxSize = Constant.SMPP_MAX_SIZE;
		int splitSize = Constant.SMPP_SPLIT_SIZE;
		if (strBuf.length() > maxSize) {
			while (strBuf.length() > splitSize) {
				String str = strBuf.substring(0, splitSize);
				strBuf.delete(0, splitSize);
				strLst.add(str);
			}
		}
		if (strBuf.length() > 0) {
			strLst.add(strBuf.toString());
		}

		return strLst;
	}

	/***
	 * 长短信内容拆分,SMPP默认使用UCS2编码，比较简单
	 * 
	 * @param content
	 * @return
	 */
	public static List<String> splitLongContenteForSMPP(String messageContent) {
		List<String> strLst = new ArrayList<String>();
		StringBuffer strBuf = new StringBuffer(messageContent);
		int maxSize = Constant.SMPP_MAX_SIZE;
		int splitSize = Constant.SMPP_SPLIT_SIZE;
		if (checkLatinCharset(messageContent)) {
			maxSize = Constant.SMPP_MAX_SIZE_ASC;
			splitSize = Constant.SMPP_SPLIT_SIZE_ASC;
		}
		if (strBuf.length() > maxSize) {
			while (strBuf.length() > splitSize) {
				String str = strBuf.substring(0, splitSize);
				strBuf.delete(0, splitSize);
				strLst.add(str);
			}
		}
		if (strBuf.length() > 0) {
			strLst.add(strBuf.toString());
		}

		return strLst;
	}

	/**
	 * 获取昨日日期
	 */
	public final static String lastDate() {
		SimpleDateFormat dFormatter = new SimpleDateFormat("yyMMdd");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, -1);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return dFormatter.format(new Date(cal.getTimeInMillis()));
	}



}
