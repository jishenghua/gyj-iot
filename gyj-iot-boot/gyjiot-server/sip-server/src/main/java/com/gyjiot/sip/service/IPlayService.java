package com.gyjiot.sip.service;

import com.gyjiot.sip.model.Stream;

public interface IPlayService {
    Stream play(String deviceId, String channelId, boolean record);
    String closeStream(String deviceId, String channelId, String streamId);
}
