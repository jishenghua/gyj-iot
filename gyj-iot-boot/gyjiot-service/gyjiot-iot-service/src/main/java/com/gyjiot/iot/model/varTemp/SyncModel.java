package com.gyjiot.iot.model.varTemp;

import lombok.Data;

import java.util.List;

/**
 * @author shenghua.ji
 */
@Data
public class SyncModel {

    private List<Long> productIds;
    private Long templateId;
}
