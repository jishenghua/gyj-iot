package com.gyjiot.common.utils.gateway.mq;

import lombok.Data;

/**
 * @author shenghua.ji
 */
@Data
public class Topics {


    private String topicName;
    private Integer qos =0;
    private String desc;

}
