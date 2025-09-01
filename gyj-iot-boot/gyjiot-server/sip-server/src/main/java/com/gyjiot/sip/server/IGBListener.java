package com.gyjiot.sip.server;

import com.gyjiot.sip.handler.IReqHandler;
import com.gyjiot.sip.handler.IResHandler;

import javax.sip.SipListener;

public interface IGBListener extends SipListener {
    public void addRequestProcessor(String method, IReqHandler processor);
    public void addResponseProcessor(String method, IResHandler processor);
}
