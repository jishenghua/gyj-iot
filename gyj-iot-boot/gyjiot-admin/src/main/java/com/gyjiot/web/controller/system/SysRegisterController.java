package com.gyjiot.web.controller.system;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.gyjiot.common.core.controller.BaseController;
import com.gyjiot.common.core.domain.AjaxResult;
import com.gyjiot.common.core.domain.model.RegisterBody;
import com.gyjiot.common.utils.StringUtils;
import com.gyjiot.framework.web.service.SysRegisterService;
import com.gyjiot.system.service.ISysConfigService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 注册验证
 * 
 * @author ruoyi
 */
@Api(tags = "注册账号")
@RestController
public class SysRegisterController extends BaseController
{
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysConfigService configService;

    @ApiOperation("注册账号")
    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user)
    {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser"))))
        {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }

    /**
     * 获取是否开启注册功能
     */
    @ApiOperation("获取是否开启注册功能")
    @GetMapping("/registerFlag")
    public AjaxResult registerFlag(HttpServletResponse response) throws IOException {
        AjaxResult ajax = AjaxResult.success();
        String registerUser = configService.selectConfigByKey("sys.account.registerUser");
        ajax.put("registerUser", registerUser);
        return ajax;
    }
}
