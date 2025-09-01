package com.gyjiot.iot.model.varTemp;

import lombok.Data;

import java.util.List;

/**
 * @author gyj.iot
 * @date 2022/12/14 15:38
 */
@Data
public class DeviceTemp {

    private Long productId;

    private String serialNumber;

    private List<DeviceSlavePoint> pointList;
}
