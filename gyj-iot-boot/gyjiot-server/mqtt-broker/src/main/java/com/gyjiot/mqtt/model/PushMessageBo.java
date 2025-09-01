package com.gyjiot.mqtt.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author shenghua.ji
 */
@Data
public class PushMessageBo implements Serializable {

    /*主题*/
    private String topic;
    /*数据*/
    private String message;
    /*消息质量*/
    private int qos;

    private Integer value;
    private Integer address;
    private Integer slaveId;
}
