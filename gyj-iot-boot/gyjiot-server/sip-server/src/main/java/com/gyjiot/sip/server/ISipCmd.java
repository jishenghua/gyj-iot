package com.gyjiot.sip.server;

import com.gyjiot.sip.domain.SipDevice;
import com.gyjiot.sip.model.VideoSessionInfo;

public interface ISipCmd {
    VideoSessionInfo playStreamCmd(SipDevice device, String channelId, boolean record);
    void streamByeCmd(SipDevice device, String channelId, String stream, String ssrc);
    void streamByeCmd(String deviceId, String channelId, String stream, String ssrc);
}
