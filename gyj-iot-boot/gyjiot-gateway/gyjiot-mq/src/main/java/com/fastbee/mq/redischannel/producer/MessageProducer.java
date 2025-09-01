package com.gyjiot.mq.redischannel.producer;

import com.gyjiot.common.core.mq.DeviceReportBo;
import com.gyjiot.mq.redischannel.queue.*;

/**
 *设备消息生产者 ,设备的消息发送通道
 * @author shenghua.ji
 */
public class MessageProducer {
    public static void sendOtherMsg(DeviceReportBo bo){
        DeviceOtherQueue.offer(bo);
    }

}
