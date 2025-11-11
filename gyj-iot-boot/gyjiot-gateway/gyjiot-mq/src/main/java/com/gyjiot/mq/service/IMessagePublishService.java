package com.gyjiot.mq.service;

import com.gyjiot.common.core.mq.message.DeviceMessage;

/**
 * 设备消息推送mq
 * @author shenghua.ji
 */
public interface IMessagePublishService {


    /**
     * 发布消息到mq
     * @param message 设备消息
     * @param channel 推送channel
     */
    public void publish(Object message,String channel);

}
