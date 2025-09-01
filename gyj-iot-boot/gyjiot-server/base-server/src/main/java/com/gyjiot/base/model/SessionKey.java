package com.gyjiot.base.model;

import com.gyjiot.base.session.Session;

/**
 * @author gyj.iot
 * @date 2023/3/9 10:03
 */
public enum SessionKey {

    DeviceMsg;

    public static DeviceMsg getDeviceMsg(Session session){
       return (DeviceMsg)session.getAttribute(SessionKey.DeviceMsg);
    }
}
