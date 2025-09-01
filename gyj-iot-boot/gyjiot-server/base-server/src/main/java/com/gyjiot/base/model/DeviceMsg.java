package com.gyjiot.base.model;

import lombok.Data;

/**
 * @author gyj.iot
 * @date 2023/3/9 10:07
 */
@Data
public class DeviceMsg {

    protected String clientId;

    protected Long deviceId;

    private int protocolVersion;

    private Long productId;
}
