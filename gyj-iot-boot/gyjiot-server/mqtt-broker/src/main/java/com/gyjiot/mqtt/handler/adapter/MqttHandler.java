package com.gyjiot.mqtt.handler.adapter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

/**
 * @author shenghua.ji
 */
public interface MqttHandler {

    public void handler(ChannelHandlerContext ctx, MqttMessage message);
}
