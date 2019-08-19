package com.cl.inter.util;

import java.util.HashMap;
import java.util.Map;

public class Constant {

	// 短信类型 NORMAL, MARKET, VOICE, INTERNATIONAL
	public static final int[] SMS_TYPE = { 1, 2, 3, 4 };
	public static String[] SMS_TYPE_FIELD = new String[] { "", "balance_sms_normal", "balance_sms_market",
			"balance_voice", "balance_international" };
	// 统一编码
	public static final String CHARSET = "UTF-8";
	// jedis key统一分隔符
	public final static String TOKEN = ";;";
	// 余额提醒模板
	public final static String REMIND_CONTENT = "【创蓝国际短信】尊敬的客户您好！您的当前余额为%s元，已不足%s元，请及时充值。";

	// 长短信分隔长度(中文字符)
	public static final int SMPP_SPLIT_SIZE = 67;
	public static final int SMPP_MAX_SIZE = 70;
	// 长短信分隔长度(英文字符)
	public static final int SMPP_SPLIT_SIZE_ASC = 143;
	public static final int SMPP_MAX_SIZE_ASC = 140;

	public final static int SUCCESS = 1;
	public final static int SESSION_NOT_FOUND = 2;
	public final static int FAIL = 3;
	public static final int DELIVERY = 0;
	public static final int REPORT = 1;
	//用户拉取状态报告最大长度
	public static final int PULL_REPORT_LIMIT=100000;
	// 中国号码开头
	public static final String CH_PHONE_BEGIN = "0086";
	// 国际号码开头
	public static final String PHONE_BEGIN = "00";
	// 允许某个操作
	public static final String ALLOW = "1";

	// 返回类型
	public final static String CONTENT_TYPE = "application/json;charset=utf-8";
	// 批量提交号码限制个数
	public final static int MAX_PHONE = 10000;
	// 报警统一手机号
	public final static String[] WARN_PHONES = {"8618242360795","8617321342252","8615821842837","8613399155770"};
	// 队列操作类型
	public final static String[] OPERATE_TYPE = { "0", "1", "2", "3" };
	// 消息ID和余额关系
	public final static Map<String, String> M2F = new HashMap<>();
	// 管理员类型
	public final static int ADMIN = 1;
	// 普通用户
	public final static int USER = 2;
}
