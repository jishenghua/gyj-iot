package com.gyjiot.base.core;

import com.gyjiot.base.core.hanler.BaseHandler;

/**
 * 消息处理接口
 * @author shenghua.ji
 */
public interface HandlerMapping {

    BaseHandler getHandler(int messageId);
}
