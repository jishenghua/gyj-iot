package com.gyjiot.common.core.mq.message;

import lombok.Data;

/**
 * 指令下发组将的model
 * @author shenghua.ji
 */
@Data
public class InstructionsMessage {

    /*下发的数据*/
    private byte[] message;

    /*MQTt-下发的topic*/
    private String topicName;

    /*设备编号*/
    private String serialNumber;
}
