package com.gyjiot.mq.service;

import com.gyjiot.mq.model.ReportDataBo;

/**
 * 客户端上报数据处理方法集合
 * @author shenghua.ji
 */
public interface IDataHandler {

    /**
     * 上报属性或功能处理
     *
     * @param bo 上报数据模型
     */
    public void reportData(ReportDataBo bo);


    /**
     * 上报事件
     *
     * @param bo 上报数据模型
     */
    public void reportEvent(ReportDataBo bo);

    /**
     * 上报设备信息
     * @param bo 上报数据模型
     */
    public void reportDevice(ReportDataBo bo);

}
