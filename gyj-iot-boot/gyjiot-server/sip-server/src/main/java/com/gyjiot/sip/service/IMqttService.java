package com.gyjiot.sip.service;

import com.gyjiot.common.core.thingsModel.ThingsModelSimpleItem;
import com.gyjiot.sip.domain.SipDevice;
import com.gyjiot.sip.domain.SipDeviceChannel;
import com.gyjiot.sip.model.RecordList;
import com.gyjiot.sip.server.msg.Alarm;

import java.util.List;

public interface IMqttService {
    void publishInfo(SipDevice device);
    void publishStatus(SipDevice device, int deviceStatus);
    void publishEvent(Alarm alarm);
    void publishProperty(Long productId, String deviceNum, List<ThingsModelSimpleItem> thingsList, int delay);
    void publishChannelsProperty(String DeviceSipId, List<SipDeviceChannel> channels);
    void publishRecordsProperty(String DeviceSipId, RecordList recordList);
}
