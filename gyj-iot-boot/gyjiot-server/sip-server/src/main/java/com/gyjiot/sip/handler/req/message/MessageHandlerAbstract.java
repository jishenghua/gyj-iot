package com.gyjiot.sip.handler.req.message;

import com.gyjiot.sip.domain.SipDevice;
import com.gyjiot.sip.util.XmlUtil;
import org.dom4j.Element;

import javax.sip.RequestEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class MessageHandlerAbstract implements IMessageHandler {
    public Map<String, IMessageHandler> messageHandlerMap = new ConcurrentHashMap<>();

    public void addHandler(String cmdType, IMessageHandler messageHandler) {
        messageHandlerMap.put(cmdType, messageHandler);
    }

    @Override
    public void handlerCmdType(RequestEvent evt, SipDevice device, Element element) {
        String cmd = XmlUtil.getText(element, "CmdType");
        IMessageHandler messageHandler = messageHandlerMap.get(cmd);
        if (messageHandler != null) {
            messageHandler.handlerCmdType(evt, device, element);
        }
    }
}
