package com.gyjiot.sip.service;

import com.gyjiot.sip.model.ZlmMediaServer;

public interface ISipCacheService {
    Long getCSEQ(String serverSipId);
    void updateMediaInfo(ZlmMediaServer mediaServerConfig);
}
