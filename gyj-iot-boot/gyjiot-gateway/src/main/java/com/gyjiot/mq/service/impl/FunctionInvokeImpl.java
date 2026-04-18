package com.gyjiot.mq.service.impl;

import com.gyjiot.common.core.mq.InvokeReqDto;
import com.gyjiot.common.core.mq.MQSendMessageBo;
import com.gyjiot.common.enums.ThingsModelType;
import com.gyjiot.common.utils.bean.BeanUtils;
import com.gyjiot.iot.util.SnowflakeIdWorker;
import com.gyjiot.mq.service.IFunctionInvoke;
import com.gyjiot.mq.service.IMqttMessagePublish;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author gyj.iot
 * @date 2022/12/5 11:34
 *
 *
 *
 */
@Slf4j
@Service
public class FunctionInvokeImpl implements IFunctionInvoke {


    @Resource
    private IMqttMessagePublish mqttMessagePublish;
    private SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(2);


    /**
     * 服务调用,设备不响应
     * @param reqDto 服务下发对象
     * @return 消息id messageId
     */
    @Override
    public String invokeNoReply(InvokeReqDto reqDto){
        log.debug("=>下发指令请求：[{}]",reqDto);
        MQSendMessageBo bo = new MQSendMessageBo();
        BeanUtils.copyBeanProp(bo,reqDto);
        long id = snowflakeIdWorker.nextId();
        String messageId = id+"";
        bo.setMessageId(messageId+"");
        bo.setType(ThingsModelType.getType(reqDto.getType()));
        mqttMessagePublish.funcSend(bo);
        return messageId;
    }
}
