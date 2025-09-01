package com.gyjiot.sip.service;

import com.gyjiot.sip.enums.SessionType;
import com.gyjiot.sip.model.InviteInfo;
import com.gyjiot.sip.model.VideoSessionInfo;

import java.util.List;

public interface IInviteService {

    void updateInviteInfo(VideoSessionInfo sinfo, InviteInfo inviteInfo);

    InviteInfo getInviteInfo(SessionType type,
                             String deviceId,
                             String channelId,
                             String stream);

    List<InviteInfo> getInviteInfoAll(SessionType type, String deviceId, String channelId, String stream);

    InviteInfo getInviteInfoBySSRC(String ssrc);

    void removeInviteInfo(SessionType type,
                          String deviceId,
                          String channelId,
                          String stream);
}
