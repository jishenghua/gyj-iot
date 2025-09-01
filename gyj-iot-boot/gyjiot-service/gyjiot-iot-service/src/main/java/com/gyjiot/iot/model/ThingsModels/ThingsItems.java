package com.gyjiot.iot.model.ThingsModels;

import lombok.Data;

import java.util.List;

/**
 * @author gyj.iot
 * @date 2022/10/24 15:45
 */
@Data
public class ThingsItems {

    private List<String> ids;

    private Long productId;

    private Integer slaveId;
}
