package com.gyjiot.mqtt.handler;

import com.gyjiot.mqtt.annotation.Process;
import com.gyjiot.mqtt.handler.adapter.MqttHandler;
import com.gyjiot.mqtt.manager.ClientManager;
import com.gyjiot.mqtt.manager.ResponseManager;
import com.gyjiot.mqtt.service.IMessageStore;
import com.gyjiot.base.session.Session;
import com.gyjiot.base.util.AttributeUtils;
import com.gyjiot.mqtt.utils.MqttMessageUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 消息等级=Qos2 发布消息释放 PUBREL
 * @author shenghua.ji
 */
@Slf4j
@Process(type = MqttMessageType.PUBREL)
public class MqttPubRel implements MqttHandler {

    @Autowired
    private IMessageStore messageStore;

    @Override
    public void handler(ChannelHandlerContext ctx, MqttMessage message){
        MqttMessageIdVariableHeader variableHeader = MqttMessageUtils.getIdVariableHeader(message);
        Session session = AttributeUtils.getSession(ctx.channel());
        //获取packetId
        int messageId = variableHeader.messageId();
        messageStore.removeRelInMsg(messageId);
        MqttMessage mqttMessage = MqttMessageUtils.buildPubCompMessage(message);
        ResponseManager.responseMessage(session,mqttMessage,true);
        /*更新本地ping时间*/
        ClientManager.updatePing(session.getClientId());
    }
}
