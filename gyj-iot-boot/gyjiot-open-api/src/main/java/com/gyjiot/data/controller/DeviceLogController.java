package com.gyjiot.data.controller;

import com.gyjiot.common.annotation.Log;
import com.gyjiot.common.core.controller.BaseController;
import com.gyjiot.common.core.domain.AjaxResult;
import com.gyjiot.common.core.page.TableDataInfo;
import com.gyjiot.common.enums.BusinessType;
import com.gyjiot.common.utils.poi.ExcelUtil;
import com.gyjiot.iot.domain.DeviceLog;
import com.gyjiot.iot.model.HistoryModel;
import com.gyjiot.iot.model.MonitorModel;
import com.gyjiot.iot.service.IDeviceLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 设备日志Controller
 *
 * @author jishenghua
 * @date 2022-01-13
 */
@Api(tags = "设备日志模块")
@RestController
@RequestMapping("/iot/deviceLog")
public class DeviceLogController extends BaseController
{
    @Autowired
    private IDeviceLogService deviceLogService;

    /**
     * 查询设备的监测数据
     */
    @ApiOperation("查询设备的监测数据")
    @PreAuthorize("@ss.hasPermi('iot:device:list')")
    @GetMapping("/monitor")
    public TableDataInfo monitorList(DeviceLog deviceLog)
    {
        List<MonitorModel> list = deviceLogService.selectMonitorList(deviceLog);
        return getDataTable(list);
    }

    /**
     * 查询设备日志列表
     */
    @ApiOperation("查询设备日志列表")
    @PreAuthorize("@ss.hasPermi('iot:device:list')")
    @GetMapping("/list")
    public TableDataInfo list(DeviceLog deviceLog)
    {
        startPage();
        List<DeviceLog> list = deviceLogService.selectDeviceLogList(deviceLog);
        return getDataTable(list);
    }

}
