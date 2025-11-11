package com.gyjiot.mqttclient;

import com.gyjiot.common.core.mq.DeviceReportBo;

/**
 * @author shenghua.ji
 */
public interface IEmqxMessageProducer {

    public void sendEmqxMessage(String topicName, DeviceReportBo deviceReportBo);
}
