package com.cl.inter.gateway.smpp.handler;

import com.cl.inter.gateway.smpp.SMPPClient;
import com.cl.inter.gateway.smpp.message.*;
import com.cl.inter.gateway.smpp.message.SMPPBody.NOOPBody;
import com.cl.inter.util.CommUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTimeZone;

import java.util.HashMap;
import java.util.Map;

/**
 * SMPP接收消息控制器
 * 
 * @author zhu_tek
 */
public class SMPPReceiverHandler extends SimpleChannelInboundHandler<SMPPMessage> {

	// 日志
	private final Logger logger = LogManager.getLogger(SMPPReceiverHandler.class);
	private SMPPClient client;

	public SMPPReceiverHandler(SMPPClient client) {
		this.client = client;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SMPPMessage msg) throws Exception {
		int commandId = msg.getCommandID();
		switch (commandId) {

		// 如果收到心跳包，不处理
		case SMPPMessage.SMPP_ENQUIRE_LINK_RESP:
			logger.info(" 收到心跳响应标志");
			break;
        case SMPPMessage.SMPP_UNBIND_RESP:
            logger.info("sldkfkdkljfewj");
            ctx.close();
			break;
		// 如果收到心跳包，回复网关
		case SMPPMessage.SMPP_ENQUIRE_LINK:
			SMPPMessage messageTest = new SMPPMessage();
			messageTest.setSequenceId(msg.getSequenceId());
			messageTest.setCommandID(SMPPMessage.SMPP_ENQUIRE_LINK_RESP);
			NOOPBody activeTestResp = new NOOPBody();
			messageTest.setBody(activeTestResp);
			ctx.writeAndFlush(messageTest);
			break;
		// 连接回应
		case SMPPMessage.SMPP_BIND_RECEIVER_RESP:
		case SMPPMessage.SMPP_BIND_TRANSMITTER_RESP:
		case SMPPMessage.SMPP_BIND_TRANSCEIVER_RESP:
			handleSmppConnected(ctx, msg);
			break;

		// 获得上行短信
		case SMPPMessage.SMPP_DELIVER:
			SMPPSubmit deliver = (SMPPSubmit) msg.getBody();
			logger.info("receive delivry from {}, msg {}", client.getServerAddr(), deliver.getShortMessage());
			if (deliver.isDelivery()) {
				Map<String, String> message = new HashMap<String, String>();
				message.put("messageId", CommUtil.generateMessageId());
				message.put("remoteMessageId", String.valueOf(msg.getSequenceId()));
				message.put("messageContent", deliver.getShortMessage());
				message.put("receiveTime", System.currentTimeMillis() + "");
				message.put("fmt", deliver.getDataCoding().getCode()+"");
				String sendPhone = deliver.getSourceAddr();
				System.out.printf("-----------");
				System.out.printf(sendPhone);
				if (!sendPhone.substring(0, 2).equals("00")) {
					sendPhone = "00" + sendPhone;
				}
				message.put("sendPhone", sendPhone);
				message.put("accessNumber", deliver.getDestinationAddr());
				logger.info("收到上行短信message: " + message);
				client.receiveMessage(message);
			} else {
				DeliveryReceipt d = DeliveryReceipt.parseShortMessage(deliver.getShortMessage(), DateTimeZone.UTC,
						false);
				Map<String, String> message = new HashMap<>();
				message.put("reportTime", System.currentTimeMillis() + "");
				message.put("result", DeliveryReceipt.toStateText(d.getState()));
				message.put("sendPhone", deliver.getSourceAddr());
				message.put("SMSCsequence", "");
				message.put("account", deliver.getReceiveAccount());
				message.put("accessNumber", deliver.getDestinationAddr());
				message.put("remoteMessageId", d.getMessageId());
				if(null!=d.getDoneDate())
					message.put("receiveTime", d.getDoneDate().toString());
				if(null!=d.getSubmitDate())
				message.put("submitTime", d.getSubmitDate().toString());
				logger.info("收到SMPP状态报告：{}", message);
				client.receiveReport(message);
			}
			responseDeliveryMessage(ctx, msg);
			client.close();
			break;

		// 获得消息发送返回值
		case SMPPMessage.SMPP_SUBMIT_RESP:
			handleSummitResponse(msg);
			break;
		default:
			break;
		}
	}

	/**
	 * 发送回应处理
	 */
	private void handleSummitResponse(SMPPMessage msg) {
		String status = String.valueOf(msg.getCommandStatus());
		String seqId = String.valueOf(msg.getSequenceId());
		SMPPSubmitResp resp = (SMPPSubmitResp) msg.getBody();
		logger.info("提交相应状态码："+status);
		client.onSubmitResponse(status, seqId, resp.getMessageId());
	}

	/**
	 * 处理登录回应
	 */
	private void handleSmppConnected(ChannelHandlerContext ctx, SMPPMessage response) {
		int status = response.getCommandStatus();
		if (status == 0) {
			client.loginSuccess();
			//CatUtil.logMetricForCount(client.getGateWayId()+"：号通道登录成功");
			return;
		}
		//CatUtil.logMetricForCount(client.getGateWayId()+"：号通道登录失败");
		client.loginFail(status, Integer.toHexString(status));
        ctx.close();
	}

	/**
	 * 处理上行短信
	 */
	private void responseDeliveryMessage(ChannelHandlerContext context, SMPPMessage message) {

		SMPPMessage msg = new SMPPMessage();
		SMPPHeader header = new SMPPHeader();
		header.setCommandID(SMPPMessage.SMPP_DELIVER_RESP);
		header.setSequenceId(message.getSequenceId());
		msg.setHeader(header);

		SMPPSubmitResp resp = new SMPPSubmitResp();
		resp.setMessageId("");
		msg.setBody(resp);

		context.writeAndFlush(msg);

	}
}
