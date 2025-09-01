package com.gyjiot.common.utils.gateway.mq;

import lombok.Data;

/**
 * @author gyj.iot
 * @date 2023/2/27 13:41
 */
@Data
public class TopicsPost {

    private String[] topics;
    private int[] qos;
}
