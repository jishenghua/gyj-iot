package com.gyjiot.sip.handler.res;

import com.gyjiot.sip.handler.IResHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.ResponseEvent;
import java.text.ParseException;

@Slf4j
@Component
public class UnknowResHandler implements IResHandler {
    @Override
    public void processMsg(ResponseEvent evt) throws ParseException {
        log.warn("Unknow Response! ReasonPhrase:" + evt.getResponse().getReasonPhrase());
    }
}
