package com.gyjiot.iot.service.impl;

import com.gyjiot.common.utils.DateUtils;
import com.gyjiot.iot.domain.DeviceLog;
import com.gyjiot.iot.model.HistoryModel;
import com.gyjiot.iot.tdengine.service.ILogService;
import com.gyjiot.iot.mapper.DeviceLogMapper;
import com.gyjiot.iot.model.MonitorModel;
import com.gyjiot.iot.service.IDeviceLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 设备日志Service业务层处理
 *
 * @author ji-sheng-hua
 * @date 2022-01-13
 */
@Service
public class DeviceLogServiceImpl implements IDeviceLogService
{

    @Autowired
    private ILogService logService;

    /**
     * 查询设备监测数据
     *
     * @param deviceLog 设备日志
     * @return 设备日志
     */
    @Override
    public List<MonitorModel> selectMonitorList(DeviceLog deviceLog)
    {
        return logService.selectMonitorList(deviceLog);
    }

    /**
     * 查询设备日志列表
     *
     * @param deviceLog 设备日志
     * @return 设备日志
     */
    @Override
    public List<DeviceLog> selectDeviceLogList(DeviceLog deviceLog)
    {
        if(deviceLog.getIsMonitor()==null){
            deviceLog.setIsMonitor(0);
        }
        return logService.selectDeviceLogList(deviceLog);
    }
}
