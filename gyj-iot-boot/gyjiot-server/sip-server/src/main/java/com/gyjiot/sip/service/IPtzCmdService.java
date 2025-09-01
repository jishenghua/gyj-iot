package com.gyjiot.sip.service;

import com.gyjiot.sip.enums.Direct;

public interface IPtzCmdService {
    public boolean directPtzCmd(String deviceId, String channelId, Direct direct, Integer speed);
}
