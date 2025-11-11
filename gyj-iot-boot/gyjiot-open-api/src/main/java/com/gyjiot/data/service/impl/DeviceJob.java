package com.gyjiot.data.service.impl;

import com.gyjiot.base.session.Session;
import com.gyjiot.common.enums.DeviceStatus;
import com.gyjiot.framework.mybatis.helper.DataBaseHelper;
import com.gyjiot.iot.domain.Device;
import com.gyjiot.iot.model.DeviceStatusVO;
import com.gyjiot.iot.service.IDeviceService;
import com.gyjiot.mq.service.IMqttMessagePublish;
import com.gyjiot.mqtt.manager.SessionManger;
import com.gyjiot.sip.domain.SipDevice;
import com.gyjiot.sip.domain.SipDeviceChannel;
import com.gyjiot.sip.enums.DeviceChannelStatus;
import com.gyjiot.sip.mapper.SipDeviceChannelMapper;
import com.gyjiot.sip.mapper.SipDeviceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class DeviceJob {

    @Resource
    private IDeviceService deviceService;

    @Autowired
    private SipDeviceMapper sipDeviceMapper;

    @Autowired
    private SipDeviceChannelMapper sipDeviceChannelMapper;

    @Resource
    private IMqttMessagePublish mqttMessagePublish;

    private Boolean enabled = true;

    public void updateSipDeviceOnlineStatus(Integer timeout) {
        String checkTimeCondition = DataBaseHelper.checkTime(timeout);
        List<SipDevice> devs = sipDeviceMapper.selectOfflineSipDevice(checkTimeCondition);
        devs.forEach(item -> {
            if (!Objects.equals(item.getDeviceSipId(), "")) {
                //更新iot设备状态
                Device dev = deviceService.selectDeviceBySerialNumber(item.getDeviceSipId());
                if (dev != null && dev.getStatus() == 3) {
                    log.warn("定时任务：=>设备:{} 已经下线，设备超过{}秒没上线！", item.getDeviceSipId(), timeout);
                    dev.setStatus(4);
                    deviceService.updateDeviceStatusAndLocation(dev, "");
                }
                //更新通道状态
                List<SipDeviceChannel> channels = sipDeviceChannelMapper.selectSipDeviceChannelByDeviceSipId(item.getDeviceSipId());
                channels.forEach(citem -> {
                    citem.setStatus(DeviceChannelStatus.offline.getValue());
                    sipDeviceChannelMapper.updateSipDeviceChannel(citem);
                });
            }
        });
    }

    /**
     * 定期同步设备状态
     *  1.将异常在线设备变更为离线状态
     *  2.将离线设备但实际在线设备变更为在线
     */
    public void syncDeviceStatus() {
        if (enabled) {
            //获取所有已激活并不是禁用的设备
            List<DeviceStatusVO> deviceStatusVOList = deviceService.selectDeviceActive();
            if (!CollectionUtils.isEmpty(deviceStatusVOList)) {
                for (DeviceStatusVO statusVO : deviceStatusVOList) {
                    Session session = SessionManger.getSession(statusVO.getSerialNumber());
                    Device device = new Device();
                    device.setSerialNumber(statusVO.getSerialNumber());
                    device.setRssi(statusVO.getRssi());
                    device.setProductId(statusVO.getProductId());
                    device.setIsShadow(statusVO.getIsShadow());
                    // 如果session中设备在线，数据库状态离线 ,则更新设备的状态为在线
                    if (!Objects.isNull(session) && statusVO.getStatus() == DeviceStatus.OFFLINE.getType()) {
                        device.setStatus(DeviceStatus.ONLINE.getType());
                        deviceService.updateDeviceStatus(device);
                        mqttMessagePublish.pushDeviceStatus(device, DeviceStatus.ONLINE);
                    }
                    if (Objects.isNull(session) && statusVO.getStatus() == DeviceStatus.ONLINE.getType()) {
                        device.setStatus(DeviceStatus.OFFLINE.getType());
                        deviceService.updateDeviceStatus(device);
                        mqttMessagePublish.pushDeviceStatus(device, DeviceStatus.OFFLINE);
                    }
                }
            }
        }
    }
}
