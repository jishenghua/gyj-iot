package com.gyjiot.mq.service;

import com.gyjiot.common.core.mq.DeviceReport;
import com.gyjiot.common.core.mq.DeviceReportBo;
import com.gyjiot.iot.domain.Device;

/**
 * 处理设备上报数据解析
 * @author gyj.iot
 * @date 2022/10/10 13:48
 */
public interface IDeviceReportMessageService {

    /**
     * 处理设备主动上报数据
     * @param bo
     */
    public void parseReportMsg(DeviceReportBo bo);

    /**
     * 构建消息
     * @param bo
     */
    public Device buildReport(DeviceReportBo bo);


    /**
     * 处理设备主动上报属性
     *
     * @param topicName
     * @param message
     */
    public void handlerReportMessage(DeviceReport message, String topicName);

}
