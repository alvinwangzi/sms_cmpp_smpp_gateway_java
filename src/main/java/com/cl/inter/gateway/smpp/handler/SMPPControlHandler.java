package com.cl.inter.gateway.smpp.handler;

import com.cl.inter.gateway.DefaultGateWay;
import com.cl.inter.gateway.smpp.message.SMPPBind;
import com.cl.inter.gateway.smpp.message.SMPPBody.NOOPBody;
import com.cl.inter.gateway.smpp.message.SMPPMessage;
import com.cl.inter.util.CommUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * SMPP协议总控制器
 *
 * @author zhu_tek
 */
public class SMPPControlHandler extends ChannelDuplexHandler {
	private static Map<String, Integer> COUNT_MAP = new HashMap<>();
	private final Logger logger = LogManager.getLogger(SMPPControlHandler.class);
	private DefaultGateWay gateWayInfo;

	public SMPPControlHandler(DefaultGateWay gateWayInfo) {
		this.gateWayInfo = gateWayInfo;
	}


	@Override
	public void close(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
		logger.info(gateWayInfo.getGateWayName() + " 正在关闭");
		gateWayInfo.terminating();
		super.close(ctx, future);
	}

	/**
	 * 如果write()方法超过writerIdleTime时间未被调用则会触发超时事件调用userEventTrigger()方法
	 */
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		super.write(ctx, msg, promise);
	}

	/**
	 * 如果channelRead()方法超过readerIdleTime时间未被调用则会触发超时事件调用userEventTrigger()方法
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead(ctx, msg);
	}

	/**
	 * channelActive()方法在socket通道建立时被触发
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		gateWayInfo.connectSuccess(ctx);
		doLogin(ctx);
		super.channelActive(ctx);
	}

	/**
	 * Socket连接关闭时触发
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.info(gateWayInfo.getGateWayName() + " to " + gateWayInfo.getServerAddr() + " 连接断开。");
		//CatUtil.logMetricForCount(gateWayInfo.getGateWayId()+"：号通道断开");
		gateWayInfo.setLoginFlag(false);
		gateWayInfo.setConnected(false);
		super.channelInactive(ctx);
	}

	// 登录
	public void doLogin(ChannelHandlerContext ctx) {
		logger.info(" 正在登录。。。");
		SMPPMessage loginMessage = new SMPPMessage();
		SMPPBind SMPPConnection = new SMPPBind(gateWayInfo.getSpAccount(), gateWayInfo.getSpPassword(),gateWayInfo.getSpType());
		loginMessage.setCommandID(SMPPMessage.SMPP_BIND_TRANSCEIVER);
		loginMessage.setSequenceId(CommUtil.generateMessageSeqNumber());
		loginMessage.setBody(SMPPConnection);
        ctx.writeAndFlush(loginMessage);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (ctx.channel().isActive()){
			ctx.close();
		}
		logger.error("SMPP请求错误：" + cause.getMessage(), cause);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent){
			IdleStateEvent idleStateEvent=(IdleStateEvent)evt;
			IdleState idleState = idleStateEvent.state();
			if (idleState == IdleState.READER_IDLE){
				ctx.close();
			}else {
                logger.info(" 触发心跳测试。");
                //CatUtil.logMetricForCount(gateWayInfo.getGateWayId()+"：号通道空闲");
                SMPPMessage message = new SMPPMessage();
                message.setCommandID(SMPPMessage.SMPP_ENQUIRE_LINK);
                message.setSequenceId(CommUtil.generateMessageSeqNumber());
                NOOPBody activeTest = new NOOPBody();
                message.setBody(activeTest);
                ctx.writeAndFlush(message);
			}
		}
	}
}
