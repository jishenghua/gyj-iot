package com.gyjiot.iot.model.login;

import lombok.Data;

/**
 * @author gyj.iot
 * @date 2023-09-04 11:03
 */
@Data
public class WeChatLoginQrRes {

    private String appid;

    private String scope;

    private String redirectUri;

    private String state;
}
