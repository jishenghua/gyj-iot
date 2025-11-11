package com.gyjiot.mq.redischannel.producer;

import com.gyjiot.common.core.mq.DeviceReportBo;
import com.gyjiot.mqttclient.IEmqxMessageProducer;
import org.springframework.stereotype.Component;

/**
 * @author shenghua.ji
 */
@Component
public class EmqxMessageProducer implements IEmqxMessageProducer {
    @Override
    public void sendEmqxMessage(String topicName, DeviceReportBo deviceReportBo) {
        MessageProducer.sendOtherMsg(deviceReportBo);
    }
}
