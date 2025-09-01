package com.gyjiot.iot.tdengine.service.impl;

import com.gyjiot.common.utils.DateUtils;
import com.gyjiot.iot.domain.Device;
import com.gyjiot.iot.domain.DeviceLog;
import com.gyjiot.iot.model.DeviceStatistic;
import com.gyjiot.iot.model.HistoryModel;
import com.gyjiot.iot.tdengine.service.ILogService;
import com.gyjiot.iot.mapper.DeviceLogMapper;
import com.gyjiot.iot.model.MonitorModel;
import com.gyjiot.iot.tdengine.service.model.TdLogDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MySqlLogServiceImpl implements ILogService {

    private DeviceLogMapper deviceLogMapper;

    public MySqlLogServiceImpl(DeviceLogMapper _deviceLogMapper){
        this.deviceLogMapper=_deviceLogMapper;
    }

    /***
     * 根据设备ID删除设备日志
     * @return
     */
    @Override
    public int deleteDeviceLogByDeviceNumber(String deviceNumber) {
        return deviceLogMapper.deleteDeviceLogByDeviceNumber(deviceNumber);
    }

    /***
     * 设备属性、功能、事件和监测数据总数
     * @return
     */
    @Override
    public DeviceStatistic selectCategoryLogCount(Device device){
        return deviceLogMapper.selectCategoryLogCount(device);
    }


    /***
     * 监测数据列表
     * @return
     */
    @Override
    public List<MonitorModel> selectMonitorList(DeviceLog deviceLog) {
        return deviceLogMapper.selectMonitorList(deviceLog);
    }

    /***
     * 日志列表
     * @return
     */
    @Override
    public List<DeviceLog> selectDeviceLogList(DeviceLog deviceLog) {
        return deviceLogMapper.selectDeviceLogList(deviceLog);
    }
}
