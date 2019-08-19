package com.cl.inter.gateway.smpp;


import com.cl.inter.gateway.KeepConnectGateWay;
import com.cl.inter.gateway.LongMsgHeader;
import com.cl.inter.gateway.SubmitMessage;
import com.cl.inter.gateway.smpp.message.*;
import com.cl.inter.util.CommUtil;
import com.cl.inter.util.Constant;
import com.cl.inter.util.DefaultSequenceNumberUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SMPP客户端入口类
 *
 * @author zhu_tek
 */
public class SMPPClient extends KeepConnectGateWay {

    private final static Logger logger = LogManager.getLogger(SMPPClient.class);
    private AtomicInteger sendCount = new AtomicInteger();
    private long start = System.currentTimeMillis();

    /**
     * 客户端构造函数
     *
     * @throws InterruptedException
     */
    public SMPPClient() {
        super();
    }

    // 开启SMPP客户端
    @Override
    public void start() throws InterruptedException {
        super.start();
        bootstrap.handler(new SMPPClientInitializer(this));
    }

    // 发送短信
    public void sendMessage(String phone,String messageContent) {
        SMPPMessage message = new SMPPMessage();
        // 设置消息头
        SMPPHeader header = new SMPPHeader();
        header.setCommandID(SMPPMessage.SMPP_SUBMIT);
        header.setSequenceId(0x125);
        message.setHeader(header);
        // 设置消息体
        SMPPSubmit submit = new SMPPSubmit();
        // 电话号码00开头，去掉00
        if ("00".equals(phone.substring(0, 2))) {
            phone = phone.substring(2);
        }
        submit.setSourceAddr(getSpNodeNumber());
        submit.setDestinationAddr(phone);
        submit.setDestAddrNpi((byte) 1);
        submit.setDestAddrTon((byte) 1);
        submit.setSourceAddrTon((byte) 1);
        submit.setSourceAddrNpi((byte) 0);
        // 长短信内容分割及设置
        List<String> contentChips = CommUtil.splitLongContenteForSMPP(messageContent);
        int sendNum = contentChips.size() > 1 ? contentChips.size() : 1;//add by zhangct
        // 判断字符集
        submit.setDataCoding(CharsetInfo.getSmppCharsetInfo(messageContent));
        // 流速控制
        limiter.acquire(contentChips.size());
        // 流速监控
        sendCount.addAndGet(contentChips.size());

        String messageId = "0111122";//add by zhangct
        if (sendNum > 1) {
            byte total = (byte) sendNum;
            byte serial = DefaultSequenceNumberUtil.getOne();

            for (int index = 0; index < contentChips.size(); index++) {
                header.setSequenceId(index + 1);
                SMPPMessage message2 = (SMPPMessage) message.clone();
                SMPPSubmit submit2 = (SMPPSubmit) submit.clone();
                LongMsgHeader msgHeader = new LongMsgHeader();

                // 设置sequenceId对应的发送信息 add by zhangct
                int sequenceId = getSeqNo();
                message2.setSequenceId(sequenceId);
                String seqStr = String.valueOf(sequenceId);
                String str = messageId + Constant.TOKEN + String.valueOf(sendNum);
                logger.info("sequence:{}  to message:{}", seqStr, str);
                msgHeader.setSerial(serial);
                msgHeader.setTotal(total);
                msgHeader.setNumber((byte) (index + 1));
                submit2.setShortMessage(contentChips.get(index));
                submit2.setMsgHeader(msgHeader);
                submit2.setEsmClass((byte) 0x40);
                message2.setBody(submit2);
                getContext().writeAndFlush(message2);
            }
        } else {
            // 设置sequenceId对应的发送信息 add by zhangct
            int sequenceId = getSeqNo();
            message.setSequenceId(sequenceId);
            String seqStr = String.valueOf(sequenceId);

            submit.setShortMessage(messageContent);
            message.setBody(submit);
            getContext().writeAndFlush(message);
        }
        if (System.currentTimeMillis() - start >= 1000) {
            logger.info("发送条数 :"+ sendCount.get());
            sendCount.set(0);
            start = System.currentTimeMillis();
        }
    }

    @Override
    public void close() {
        logger.info("即将关闭...");
        SMPPHeader header = new SMPPHeader();
        header.setCommandID(SMPPMessage.SMPP_UNBIND);
        header.setSequenceId(0x1);
        SMPPMessage closeMessage = new SMPPMessage();
        closeMessage.setHeader(header);
        closeMessage.setBody(new SMPPUnbind());
        getContext().writeAndFlush(closeMessage);
    }

    @Override
    public int getGateWayProtocol() {
        return 0;
    }

}