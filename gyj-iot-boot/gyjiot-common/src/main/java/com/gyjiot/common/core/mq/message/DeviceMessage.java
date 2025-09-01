package com.gyjiot.common.core.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 集群消息
 * @author shenghua.ji
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceMessage<T> {

    /*数据*/
    private T data;

    private int nodeId;
}
