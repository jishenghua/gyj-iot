package com.gyjiot.mq.redischannel.listen;

import com.gyjiot.common.constant.GyjConstant;
import com.gyjiot.common.core.mq.DeviceReportBo;
import com.gyjiot.mq.redischannel.consumer.DeviceOtherMsgConsumer;
import com.gyjiot.mq.redischannel.queue.DeviceOtherQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author gyj.iot
 * @date 2023/2/28 10:02
 */
@Slf4j
@Component
public class DeviceOtherListen {

    @Resource
    private DeviceOtherMsgConsumer otherMsgConsumer;

    @Async(GyjConstant.TASK.DEVICE_OTHER_TASK)
    public void listen(){
        while (true){
            try {
                DeviceReportBo reportBo = DeviceOtherQueue.take();
                otherMsgConsumer.consume(reportBo);
            }catch (Exception e){
                log.error("=>emq数据转发异常");
            }
        }
    }
}
