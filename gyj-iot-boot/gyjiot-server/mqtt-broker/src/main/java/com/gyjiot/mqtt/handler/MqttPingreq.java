package com.gyjiot.mqtt.handler;

import com.gyjiot.mqtt.annotation.Process;
import com.gyjiot.mqtt.handler.adapter.MqttHandler;
import com.gyjiot.mqtt.manager.ClientManager;
import com.gyjiot.mqtt.manager.ResponseManager;
import com.gyjiot.base.util.AttributeUtils;
import com.gyjiot.mqtt.utils.MqttMessageUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端Ping消息应答
 *
 * @author shenghua.ji
 */
@Slf4j
@Process(type = MqttMessageType.PINGREQ)
public class MqttPingreq implements MqttHandler {

    @Override
    public void handler(ChannelHandlerContext ctx, MqttMessage message) {
        /*获取客户端id*/
        String clientId = AttributeUtils.getClientId(ctx.channel());
        try {
            // log.debug("=>客户端:{},心跳信息", clientId);
            /*更新客户端ping时间*/
            ClientManager.updatePing(clientId);
            /*响应设备的ping消息*/
            MqttMessage pingResp = MqttMessageUtils.buildPingResp();
            ResponseManager.sendMessage(pingResp, clientId, true);
        } catch (Exception e) {
            log.error("=>客户端:{},ping异常:{}", clientId, e);
        }
    }
}
