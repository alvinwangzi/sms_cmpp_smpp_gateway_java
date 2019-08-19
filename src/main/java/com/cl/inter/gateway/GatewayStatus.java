package com.cl.inter.gateway;

/**
 * 网关状态
 * @author zhu_tek
 */
public interface GatewayStatus {
    final String BLACKLIST   = "CL:BLACKLIST";          //在黑名单
    final String INTERVAL_ERR = "CL:INTERVAL_ERR";      //在发送间隔之内
    final String OVER_MAX  = "CL:OVER_MAX";             //超过发送上限
    final String DANGER_CONTENT = "CL:DANGER_CONTENT";  //危险内容
    final String SEND_TIMEOUT = "CL:TIMEOUT";           //发送超时
}
