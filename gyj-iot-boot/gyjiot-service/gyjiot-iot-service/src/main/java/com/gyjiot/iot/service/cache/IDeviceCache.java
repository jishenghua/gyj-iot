package com.gyjiot.iot.service.cache;

import com.gyjiot.common.core.mq.DeviceStatusBo;
import com.gyjiot.common.enums.DeviceStatus;
import com.gyjiot.iot.domain.Device;

import java.util.List;

/**
 * 设备缓存
 * @author shenghua.ji
 */
public interface IDeviceCache {

    /**
     * 更新设备状态
     * @param dto dto
     */
    public Device updateDeviceStatusCache(DeviceStatusBo dto);

    /**
     * 批量更新redis缓存设备状态
     * @param serialNumbers 设备列表
     * @param status 状态
     */
    void updateBatchDeviceStatusCache(List<String> serialNumbers, DeviceStatus status);


}
