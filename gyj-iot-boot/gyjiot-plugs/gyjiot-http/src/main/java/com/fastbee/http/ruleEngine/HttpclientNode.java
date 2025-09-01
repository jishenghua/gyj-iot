package com.gyjiot.http.ruleEngine;

import com.dtflys.forest.http.ForestResponse;
import com.gyjiot.http.client.Amap;
import com.gyjiot.http.client.Cn12306;
import com.gyjiot.http.client.Gitee;
import com.gyjiot.http.model.Location;
import com.gyjiot.http.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@Component
public class HttpclientNode {
    @Resource
    private Amap amap;

    @Resource
    private Gitee gitee;

    @Resource
    private Cn12306 cn12306;

    public Result<Location> amapLocation(BigDecimal longitude, BigDecimal latitude) {
        Result<Location> result = amap.getLocation(longitude.toEngineeringString(), latitude.toEngineeringString());
        return result;
    }

    public String gitee() {
        String result = gitee.index();
        return result;
    }

    public String cn12306() {
        ForestResponse<String> response = cn12306.index();
        return response.getResult();
    }

}
