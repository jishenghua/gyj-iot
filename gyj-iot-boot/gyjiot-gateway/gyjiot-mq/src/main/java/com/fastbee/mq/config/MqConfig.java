package com.gyjiot.mq.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;

/**
 * mq集群配置
 * @author gyj.iot
 * @date 2022/10/10 8:27
 */
@Configuration
@ConditionalOnExpression("${cluster.enable:false}")
public class MqConfig {



}
