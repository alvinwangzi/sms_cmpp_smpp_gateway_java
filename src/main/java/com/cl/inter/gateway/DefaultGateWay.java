package com.cl.inter.gateway;

import com.cl.inter.util.CommUtil;
import com.google.common.util.concurrent.RateLimiter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

//import com.cl.inter.task.ProductSwitchTask;
//import com.cl.inter.task.UndelivSwitchTask;

/**
 * 默认网关处理程序
 *
 * @author zhu_tek
 */
public class DefaultGateWay implements GateWay {

    private final static ExecutorService executor = Executors.newFixedThreadPool(5);

    // 令牌桶算法 限流
    protected RateLimiter limiter = RateLimiter.create(100);

    // 通道ID
    private int gateWayId;

    // 上下文环境
    private ChannelHandlerContext context;

    // 通道名称
    private String gateWayName;

    // 通道协议类型
    private int protocol;

    // 服务器地址
    private String serverAddr;

    // 服务器端口
    private int serverPort;

    // 接入号
    private String spNumber;

    // 用户名
    private String spAccount;

    // 密码
    private String spPassword;

    //smpp接口类型
    private String spType;

    // 发送流速
    private int flowSize;

    // 虚拟号码流速，仅针对SMGP
    private int vitualFlowSize;

    // SP节点编号,仅针对SGIP
    private String spNodeNumber;

    // HTTP 请求头
    private String httpRequestHeader = "";

    // HTTP 请求体
    private String httpRequestBody = "";

    // HTTP请求方式
    private String httpRequestMethod = "1";

    // HTTP请求版本
    private String httpRequestVersion = "";

    // 企业代码/业务代码
    private String spId;

    // 监听端口，仅针对SGIP
    private int listenerPort;

    // 本地地址
    private String localAddr;

    // 本地地址
    private String httpCallbackUrisuffix;

    // 连接数
    private int connectNum;

    // 是否自主退订 0否 1是
    private boolean unsubscribeType;

    // 普通短信单条长度
    private int messageItemLength;

    // 长短信单条长度
    private int longMessageItemLength;

    // 长连接空闲时间
    private int idleNotifyTime = 5;

    // 窗口大小
    private int windowSize = 16;

    // 是否支持长短信
    private boolean enableLongMessage = true;

    // 是否需要监控
    private boolean monitorRequired = true;

    // 成功率多少提示
    public float successRatioThreshold;

    // 是否登录
    private boolean loginFlag = false;
    private boolean loginFailFlag = false;

    // 是否建立连接
    private boolean connected = false;

    // 日志工具
    private final static Logger logger = LogManager.getLogger(DefaultGateWay.class);

    //短信序号
    private final AtomicInteger seqNo = new AtomicInteger(1);

    /**
     * ========================= 接口实现
     * ============================================
     */

    // 初始化
    @Override
    public void init(Map<String, String> gatewayInfo) {
        setGateWayId(Integer.parseInt(gatewayInfo.get("gateway_id")));
        setGateWayName(gatewayInfo.get("gateway_name"));
        setProtocol(CommUtil.isZero(gatewayInfo.get("gateway_protocol")) ? 0
                : Integer.parseInt(gatewayInfo.get("gateway_protocol")));
        setServerAddr(gatewayInfo.get("gateway_server_addr"));
        setServerPort(CommUtil.isZero(gatewayInfo.get("gateway_server_port")) ? 0
                : Integer.parseInt(gatewayInfo.get("gateway_server_port")));
        setSpNumber(gatewayInfo.get("gateway_access_number"));
        setSpAccount(gatewayInfo.get("gateway_username"));
        setSpPassword(gatewayInfo.get("gateway_password"));
        setFlowSize(CommUtil.isZero(gatewayInfo.get("send_flow_speed")) ? 100
                : Integer.parseInt(gatewayInfo.get("send_flow_speed")));
        setVitualFlowSize(CommUtil.isZero(gatewayInfo.get("virtual_number_flow")) ? 0
                : Integer.parseInt(gatewayInfo.get("virtual_number_flow")));
        setSpNodeNumber(gatewayInfo.get("sp_node_code"));
        setSpId(gatewayInfo.get("business_code"));
        setListenerPort(
                CommUtil.isZero(gatewayInfo.get("listen_port")) ? 0 : Integer.parseInt(gatewayInfo.get("listen_port")));
        setLocalAddr(gatewayInfo.get("localtion_addr"));
        setConnectNum(
                CommUtil.isZero(gatewayInfo.get("link_nums")) ? 1 : Integer.parseInt(gatewayInfo.get("link_nums")));
        setMessageItemLength(CommUtil.isZero(gatewayInfo.get("normal_sms_length")) ? 70
                : Integer.parseInt(gatewayInfo.get("normal_sms_length")));
        setLongMessageItemLength(CommUtil.isZero(gatewayInfo.get("long_sms_length")) ? 67
                : Integer.parseInt(gatewayInfo.get("long_sms_length")));
        setMonitorRequired(gatewayInfo.get("monitor_required").equals("1") ? true : false);
        setSuccessRatioThreshold(StringUtils.isBlank(gatewayInfo.get("success_ratio_threshold"))
                || gatewayInfo.get("success_ratio_threshold").equals("null") ? 0
                : Float.parseFloat(gatewayInfo.get("success_ratio_threshold")));
        if (gatewayInfo.get("idle_time") != null && !gatewayInfo.get("idle_time").isEmpty()) {
            setIdleNotifyTime(Integer.parseInt(gatewayInfo.get("idle_time")));
        }
        if (StringUtils.isNotBlank(gatewayInfo.get("gateway_system_type"))) {
            setSpType(gatewayInfo.get("gateway_system_type"));
        }
        setHttpRequestMethod(gatewayInfo.get("http_request_method"));
        setHttpCallbackUrisuffix(gatewayInfo.get("http_callback_urisuffix"));
        setHttpRequestBody(gatewayInfo.get("http_request_body").replace(":", "&"));
        setHttpRequestHeader(gatewayInfo.get("http_request_header").replace(":", "&"));
        setHttpRequestVersion(gatewayInfo.get("http_request_version"));
        // 设置流速
        limiter.setRate(getFlowSize());
        logger.info("网关ID：" + gateWayId + ",流速：" + getFlowSize());
    }


    //http网关处理第三方回调入口
    public FullHttpResponse handleHttpCallback(HttpRequest request) {
        return null;
    }

    ;

    @Override
    public void start() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void doConnect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLogin() {
        return loginFlag;
    }

    public boolean isLoginFail() {
        return loginFailFlag;
    }

    @Override
    public void connectSuccess(ChannelHandlerContext context) {
        logger.info("连接成功");
        this.context = context;
        setConnected(true);
    }

    @Override
    public void connectTimeout() {
        logger.info("连接超时");
    }

    @Override
    public void loginSuccess() {
        logger.info("登录成功");
        setLoginFlag(true);

    }

    private static Map<String, Integer> COUNT_MAP = new HashMap<>();

    @Override
    public void loginFail(int status, String message) {
        logger.info("登录失败，失败代码：" + message);
        setLoginFlag(false);
    }

    @Override
    public void sendMessage(String[] messageInfo) {
    }

    @Override
    public void onSubmitResponse(String result, String seqno, String messageId) {

    }

    @Override
    public void terminating() {
        logger.info("正在关闭");
    }

    @Override
    public void terminated() {
        logger.info("已经关闭");
        setLoginFlag(false);
        close();
    }


    // 收到上行短信
    @Override
    public void receiveMessage(Map<String, String> deliver) {
        logger.info("收到上行短信：" + deliver);
    }

    @Override
    public void receiveReport(Map<String, String> deliver) {

    }

    /**
     * 根据内容或去扩展码
     *
     * @param content
     * @return
     */
    public String getextNo(String content) {
        String extNo = null;
        if (StringUtils.isNotBlank(content)) {
            if (content.substring(0, 2).equalsIgnoreCase("cl")) {
                //先将CL两个字过滤掉
                content = content.substring(2);
                //找出第一个不是空格的下标
                int index = -1;
                char[] c = content.toCharArray();
                for (int i = 0; i < c.length; i++) {
                    if (' ' != c[i]) {
                        index = i;
                        break;
                    }
                }
                if (-1 != index) {
                    //截取6个长度 就是扩展码
                    extNo = content.substring(index, content.length() < index + 6 ? content.length() : index + 6);
                }
                logger.info("本次infobip 纯上行拓展码：" + extNo);
            }
        }
        return extNo;
    }

    @Override
    public int flowSize() {
        return 0;
    }

    @Override
    public int getGateWayProtocol() {
        return 0;
    }

    /**
     * @return 是否启用长短信支持
     */
    public boolean isEnableLongMessage() {
        return enableLongMessage;
    }

    /**
     * @param enableLongMessage 是否启用长短信支持,启用后发送超长短信终端将收到一条短信,否个将收到多条分割的短信
     */
    public void setEnableLongMessage(boolean enableLongMessage) {
        this.enableLongMessage = enableLongMessage;
    }

    /**
     * @param windowSize 设置窗口大小,默认16
     */
    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    /**
     * @return 窗口大小
     */
    public int getWindowSize() {
        return windowSize;
    }

    public int getIdleNotifyTime() {
        return idleNotifyTime;
    }

    public void setIdleNotifyTime(int idleNotifyTime) {
        this.idleNotifyTime = idleNotifyTime;
    }

    // 获取通道唯一ID,通道唯一ID也作为REDIS缓存的键值，我们通过该键值获取需要发送的短信
    public int getGateWayId() {
        return gateWayId;
    }

    public void setGateWayId(int gateWayId) {
        this.gateWayId = gateWayId;
    }

    public String getGateWayName() {
        return gateWayName;
    }

    public void setGateWayName(String gateWayName) {
        this.gateWayName = gateWayName;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getSpNumber() {
        return spNumber;
    }

    public void setSpNumber(String spNumber) {
        this.spNumber = spNumber;
    }

    public String getSpAccount() {
        return spAccount;
    }

    public void setSpAccount(String spAccount) {
        this.spAccount = spAccount;
    }

    public String getSpPassword() {
        return spPassword;
    }

    public void setSpPassword(String spPassword) {
        this.spPassword = spPassword;
    }

    public int getFlowSize() {
        return flowSize;
    }

    public void setFlowSize(int flowSize) {
        this.flowSize = flowSize;
    }

    public int getVitualFlowSize() {
        return vitualFlowSize;
    }

    public void setVitualFlowSize(int vitualFlowSize) {
        this.vitualFlowSize = vitualFlowSize;
    }

    public String getSpNodeNumber() {
        return spNodeNumber;
    }

    public void setSpNodeNumber(String spNodeNumber) {
        this.spNodeNumber = spNodeNumber;
    }

    public String getSpId() {
        return spId;
    }

    public void setSpId(String spId) {
        this.spId = spId;
    }

    public int getListenerPort() {
        return listenerPort;
    }

    public void setListenerPort(int listenerPort) {
        this.listenerPort = listenerPort;
    }

    public String getLocalAddr() {
        return localAddr;
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    public int getConnectNum() {
        return connectNum;
    }

    public void setConnectNum(int connectNum) {
        this.connectNum = connectNum;
    }

    public boolean isUnsubscribeType() {
        return unsubscribeType;
    }

    public void setUnsubscribeType(boolean unsubscribeType) {
        this.unsubscribeType = unsubscribeType;
    }

    public int getMessageItemLength() {
        return messageItemLength;
    }

    public void setMessageItemLength(int messageItemLength) {
        this.messageItemLength = messageItemLength;
    }

    public int getLongMessageItemLength() {
        return longMessageItemLength;
    }

    public void setLongMessageItemLength(int longMessageItemLength) {
        this.longMessageItemLength = longMessageItemLength;
    }

    public boolean isMonitorRequired() {
        return monitorRequired;
    }

    public void setMonitorRequired(boolean monitorRequired) {
        this.monitorRequired = monitorRequired;
    }

    public float getSuccessRatioThreshold() {
        return successRatioThreshold;
    }

    public void setSuccessRatioThreshold(float successRatioThreshold) {
        this.successRatioThreshold = successRatioThreshold;
    }

    public void setLoginFlag(boolean loginFlag) {
        this.loginFlag = loginFlag;
    }

    public String getHttpRequestHeader() {
        return httpRequestHeader;
    }

    public void setHttpRequestHeader(String httpRequestHeader) {
        this.httpRequestHeader = httpRequestHeader;
    }

    public String getHttpRequestBody() {
        return httpRequestBody;
    }

    public void setHttpRequestBody(String httpRequestBody) {
        this.httpRequestBody = httpRequestBody;
    }

    public String getHttpRequestMethod() {
        return httpRequestMethod;
    }

    public void setHttpRequestMethod(String httpRequestMethod) {
        this.httpRequestMethod = httpRequestMethod;
    }

    public String getHttpRequestVersion() {
        return httpRequestVersion;
    }

    public void setHttpRequestVersion(String httpRequestVersion) {
        this.httpRequestVersion = httpRequestVersion;
    }

    public void setLoginFailFlag(boolean loginFailFlag) {
        this.loginFailFlag = loginFailFlag;
    }

    public int getSeqNo() {
        return seqNo.incrementAndGet();
    }

    public String getSpType() {
        return spType;
    }

    public void setSpType(String spType) {
        this.spType = spType;
    }

    @Override
    public String toString() {
        return "DefaultGateWay [gateWayId=" + gateWayId + ", context=" + context + ", gateWayName=" + gateWayName
                + ", protocol=" + protocol + ", serverAddr=" + serverAddr + ", serverPort=" + serverPort + ", spNumber="
                + spNumber + ", spAccount=" + spAccount + ", spPassword=" + spPassword + ", spType=" + spType + ", flowSize=" + flowSize
                + ", vitualFlowSize=" + vitualFlowSize + ", spNodeNumber=" + spNodeNumber + ", httpRequestHeader="
                + httpRequestHeader + ", httpRequestBody=" + httpRequestBody + ", httpRequestMethod="
                + httpRequestMethod + ", httpRequestVersion=" + httpRequestVersion + ", spId=" + spId
                + ", listenerPort=" + listenerPort + ", localAddr=" + localAddr + ", connectNum=" + connectNum
                + ", unsubscribeType=" + unsubscribeType + ", messageItemLength=" + messageItemLength
                + ", longMessageItemLength=" + longMessageItemLength + ", idleNotifyTime=" + idleNotifyTime
                + ", windowSize=" + windowSize + ", enableLongMessage=" + enableLongMessage + ", monitorRequired="
                + monitorRequired + ", successRatioThreshold=" + successRatioThreshold + ", loginFlag=" + loginFlag
                + "]";
    }

    public ChannelHandlerContext getContext() {
        return context;
    }

    public void setContext(ChannelHandlerContext context) {
        this.context = context;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean needConnect() {
        return false;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public void close() {
    }


    public String getHttpCallbackUrisuffix() {
        return httpCallbackUrisuffix;
    }


    public void setHttpCallbackUrisuffix(String httpCallbackUrisuffix) {
        this.httpCallbackUrisuffix = httpCallbackUrisuffix;
    }
}
