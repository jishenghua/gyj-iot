package com.gyjiot.base.codec;

import com.gyjiot.common.core.protocol.Message;
import com.gyjiot.base.session.Session;
import io.netty.buffer.ByteBuf;

/**
 * 基础消息编码类
 *
 * @author shenghua.ji
 */
public interface MessageEncoder{

    ByteBuf encode(Message message, String clientId);
}
