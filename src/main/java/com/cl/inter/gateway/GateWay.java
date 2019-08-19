package com.cl.inter.gateway;

import java.util.Map;

import io.netty.channel.ChannelHandlerContext;

/**
 * 短信网关
 * 
 * @author XS-021
 *
 */
public interface GateWay {

	public static final int MAX_CONNECT_TIMES = 1000;

	// 网关启动
	public void start() throws InterruptedException;

	// 获得网关ID
	public int getGateWayId();

	// 初始化通道信息
	public void init(Map<String, String> gatewayInfo);

	// 判断是否登录
	public boolean isLogin();

	// 通道连接成功
	public void connectSuccess(ChannelHandlerContext context);

	// 通道连接超时
	public void connectTimeout();

	// 登录成功后操作
	public void loginSuccess();

	// 登录失败后操作
	public void loginFail(int status, String message);

	// 发送短信
	public void sendMessage(String[] messageInfo);

	// 收到submit回应
	public void onSubmitResponse(String result, String seqno, String messageId);

	// 收到上行短信
	public void receiveMessage(Map<String, String> deliver);

	// 收到短信状态报告
	public void receiveReport(Map<String, String> deliver);

	// 获取流速大小
	public int flowSize();

	// 获取通道类型
	public int getGateWayProtocol();

	// 通道正在关闭
	public void terminating();

	// 通道已经关闭
	public void terminated();

	// 通道重连
	public void doConnect();

	// 关闭通道
	public void close();

}
