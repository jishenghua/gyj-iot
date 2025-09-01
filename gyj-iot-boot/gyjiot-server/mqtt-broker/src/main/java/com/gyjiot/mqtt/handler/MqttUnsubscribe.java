package com.gyjiot.mqtt.handler;

import com.gyjiot.mqtt.annotation.Process;
import com.gyjiot.mqtt.handler.adapter.MqttHandler;
import com.gyjiot.mqtt.manager.ClientManager;
import com.gyjiot.mqtt.manager.ResponseManager;
import com.gyjiot.base.session.Session;
import com.gyjiot.base.util.AttributeUtils;
import com.gyjiot.mqtt.utils.MqttMessageUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.codec.mqtt.MqttUnsubscribeMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
@Process(type = MqttMessageType.UNSUBSCRIBE)
public class MqttUnsubscribe implements MqttHandler {

    @Override
    public void handler(ChannelHandlerContext ctx, MqttMessage message) {
        MqttUnsubscribeMessage unsubscribeMessage = (MqttUnsubscribeMessage) message;
        List<String> topics = unsubscribeMessage.payload().topics();
        log.debug("=>收到取消订阅请求,topics[{}]", topics);
        Session session = AttributeUtils.getSession(ctx.channel());
        topics.forEach(topic -> {
            ClientManager.unsubscribe(topic, session);
        });
        MqttUnsubAckMessage unsubAckMessage = MqttMessageUtils.buildUnsubAckMessage(unsubscribeMessage);
        ResponseManager.responseMessage(session, unsubAckMessage, true);
        /*更新客户端平台时间*/
        ClientManager.updatePing(session.getClientId());
    }
}
