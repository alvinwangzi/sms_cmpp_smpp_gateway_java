package com.cl.inter.app;

import com.cl.inter.gateway.smpp.SMPPClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


public class Main {

    // 日志
    private final static Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        SMPPClient smppClient = new SMPPClient();
        smppClient.setServerAddr("164..157.232");//服务器ip
        smppClient.setServerPort(9000);//服务器端口
        smppClient.setSpAccount("");//账号
        smppClient.setSpPassword("");//密码
//        smppClient.setSpNodeNumber("106901");//接入号
        String phone = "008618269731502";
      String content = "【小米金融】重磅免30天息！领取您的最高30万专享额度，抽最长免30天息！点击领取 s.mi.cn/T6Z8D4 回复TD退订";//短信内容
        try {
            smppClient.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        smppClient.doConnect();
        while (!smppClient.isConnected()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (!smppClient.isLogin()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        logger.info("========输入任意字符开始发送======");
        System.in.read();
        long start = System.currentTimeMillis();
//        smppClient.sendMessage(phone, content);
        //smppClient.sendMessage("8616621075157", "[Test short message]This is a long SMPP test message 005.");
        //Thread.sleep(200);
        //long end = System.currentTimeMillis();
        //logger.info("first message cost {} ms", (end - start));
        //smppClient.sendMessage(phone, "ong message.长长长短信");
        Thread.sleep(Integer.MAX_VALUE);
    }

}
