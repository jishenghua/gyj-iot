package com.gyjiot.web.controller.system;

import com.gyjiot.common.core.domain.AjaxResult;
import com.gyjiot.system.service.ISysConfigService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.gyjiot.common.config.RuoYiConfig;
import com.gyjiot.common.utils.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 首页
 *
 * @author jishenghua
 */
@RestController
public class SysIndexController
{
    @Autowired
    private ISysConfigService configService;

    /** 系统基础配置 */
    @Autowired
    private RuoYiConfig ruoyiConfig;

    /**
     * 访问首页，提示语
     */
    @RequestMapping("/")
    public String index()
    {
        return StringUtils.format("欢迎使用{}后台管理框架，当前版本：v{}，请通过前端地址访问。", ruoyiConfig.getName(), ruoyiConfig.getVersion());
    }

    /**
     * 获取平台名称
     */
    @ApiOperation("获取平台名称")
    @GetMapping("/platformName")
    public AjaxResult platformName(HttpServletResponse response) throws IOException {
        AjaxResult ajax = AjaxResult.success();
        String platformName = configService.selectConfigByKey("sys.index.platformName");
        ajax.put("platformName", platformName);
        return ajax;
    }
}
