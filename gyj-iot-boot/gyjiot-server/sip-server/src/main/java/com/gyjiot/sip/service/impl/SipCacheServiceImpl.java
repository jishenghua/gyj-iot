package com.gyjiot.sip.service.impl;

import com.gyjiot.common.constant.GyjConstant;
import com.gyjiot.common.core.redis.RedisCache;
import com.gyjiot.common.core.redis.RedisKeyBuilder;
import com.gyjiot.sip.model.ZlmMediaServer;
import com.gyjiot.sip.service.ISipCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SipCacheServiceImpl implements ISipCacheService {

    @Autowired
    private RedisCache redisCache;

    @Override
    public Long getCSEQ(String serverSipId) {
        String key = RedisKeyBuilder.buildSipCSEQCacheKey(serverSipId);
        long result = redisCache.incr(key, 1L);
        if (result > Integer.MAX_VALUE) {
            redisCache.setCacheObject(key, 1);
            result = 1;
        }
        return result;
    }

    @Override
    public void updateMediaInfo(ZlmMediaServer mediaServerConfig) {
        redisCache.setCacheObject(GyjConstant.REDIS.DEFAULT_MEDIA_CONFIG, mediaServerConfig);
    }
}
