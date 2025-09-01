package com.gyjiot.mq.redischannel.config;

import com.gyjiot.common.constant.GyjConstant;
import com.gyjiot.mq.redischannel.consumer.RedisChannelConsume;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * redisChannel配置
 * @author gyj.iot
 * @date 2022/10/10 8:57
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisConsumeConfig {

    @Bean
    @ConditionalOnProperty(prefix ="cluster", name = "type" ,havingValue = GyjConstant.MQTT.REDIS_CHANNEL,matchIfMissing = true)
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 可以添加多个 messageListener，配置不同的交换机
        container.addMessageListener(listenerAdapter, new PatternTopic(GyjConstant.CHANNEL.DEVICE_STATUS));
        container.addMessageListener(listenerAdapter, new PatternTopic(GyjConstant.CHANNEL.PROP_READ));
        container.addMessageListener(listenerAdapter, new PatternTopic(GyjConstant.CHANNEL.FUNCTION_INVOKE));
        container.addMessageListener(listenerAdapter,new PatternTopic(GyjConstant.CHANNEL.UPGRADE));
        return container;
    }

    /**配置消息监听类 默认监听方法onMessage*/
    @Bean
    @ConditionalOnProperty(prefix ="cluster", name = "type" ,havingValue = GyjConstant.MQTT.REDIS_CHANNEL,matchIfMissing = true)
    MessageListenerAdapter listenerAdapter(RedisChannelConsume consume){
        return new MessageListenerAdapter(consume,"onMessage");
    }

    @Bean
    @ConditionalOnProperty(prefix ="cluster", name = "type" ,havingValue = GyjConstant.MQTT.REDIS_CHANNEL,matchIfMissing = true)
    StringRedisTemplate template(RedisConnectionFactory connectionFactory){
        return new StringRedisTemplate(connectionFactory);
    }




}
