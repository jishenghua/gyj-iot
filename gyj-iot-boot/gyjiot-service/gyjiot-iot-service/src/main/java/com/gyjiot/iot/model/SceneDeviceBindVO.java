package com.gyjiot.iot.model;

import lombok.Data;

/**
 * @author gyj.iot
 * @version 1.0
 * @description: 场景关联设备、产品VO类
 * @date 2024-02-06 15:10
 */
@Data
public class SceneDeviceBindVO {

    /**
     * 场景联动id
     */
    private Long sceneId;

    /**
     * 场景联动名称
     */
    private String sceneName;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 设备编号
     */
    private String serialNumber;
}
