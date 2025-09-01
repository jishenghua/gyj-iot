package com.gyjiot.common.exception.iot;

import com.gyjiot.common.exception.GlobalException;
import lombok.NoArgsConstructor;

/**
 * mqtt客户端权限校验异常
 * @author gyj.iot
 * @date 2022/10/8 14:11
 */
@NoArgsConstructor
public class MqttAuthorizationException extends GlobalException {

    public MqttAuthorizationException(String messageId){
        super(messageId);
    }
}
