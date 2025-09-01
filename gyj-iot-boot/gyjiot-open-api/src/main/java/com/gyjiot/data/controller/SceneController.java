package com.gyjiot.data.controller;

import com.gyjiot.common.annotation.Log;
import com.gyjiot.common.core.controller.BaseController;
import com.gyjiot.common.core.domain.AjaxResult;
import com.gyjiot.common.core.domain.model.LoginUser;
import com.gyjiot.common.core.page.TableDataInfo;
import com.gyjiot.common.enums.BusinessType;
import com.gyjiot.common.utils.poi.ExcelUtil;
import com.gyjiot.iot.domain.Scene;
import com.gyjiot.iot.service.ISceneService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 场景联动Controller
 *
 * @author jishenghua
 * @date 2022-01-13
 */
@Api(tags = "场景联动")
@RestController
@RequestMapping("/iot/scene")
public class SceneController extends BaseController
{
    @Autowired
    private ISceneService sceneService;

    /**
     * 查询场景联动列表
     */
    @ApiOperation("查询场景联动列表")
    @PreAuthorize("@ss.hasPermi('iot:scene:list')")
    @GetMapping("/list")
    public TableDataInfo list(Scene scene)
    {
        startPage();
        List<Scene> list = sceneService.selectSceneList(scene);
        return getDataTable(list);
    }

    /**
     * 导出场景联动列表
     */
    @ApiOperation("导出场景联动列表")
    @PreAuthorize("@ss.hasPermi('iot:scene:export')")
    @Log(title = "场景联动", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Scene scene)
    {
        List<Scene> list = sceneService.selectSceneList(scene);
        ExcelUtil<Scene> util = new ExcelUtil<Scene>(Scene.class);
        util.exportExcel(response, list, "场景联动数据");
    }

    /**
     * 获取场景联动详细信息
     */
    @ApiOperation("获取场景联动详细信息")
    @PreAuthorize("@ss.hasPermi('iot:scene:query')")
    @GetMapping(value = "/{sceneId}")
    public AjaxResult getInfo(@PathVariable("sceneId") Long sceneId)
    {
        return AjaxResult.success(sceneService.selectSceneBySceneId(sceneId));
    }

    /**
     * 新增场景联动
     */
    @ApiOperation("新增场景联动")
    @PreAuthorize("@ss.hasPermi('iot:scene:add')")
    @Log(title = "场景联动", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Scene scene)
    {
        LoginUser loginUser = getLoginUser();
        if (loginUser == null || loginUser.getUser() == null) {
            return error("请登录后重试！");
        }
        scene.setUserId(loginUser.getUser().getUserId());
        scene.setUserName(loginUser.getUser().getUserName());
        return toAjax(sceneService.insertScene(scene));
    }

    /**
     * 修改场景联动
     */
    @ApiOperation("修改场景联动")
    @PreAuthorize("@ss.hasPermi('iot:scene:edit')")
    @Log(title = "场景联动", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Scene scene)
    {
        return toAjax(sceneService.updateScene(scene));
    }

    /**
     * 删除场景联动
     */
    @ApiOperation("删除场景联动")
    @PreAuthorize("@ss.hasPermi('iot:scene:remove')")
    @Log(title = "场景联动", businessType = BusinessType.DELETE)
	@DeleteMapping("/{sceneIds}")
    public AjaxResult remove(@PathVariable Long[] sceneIds)
    {
        return toAjax(sceneService.deleteSceneBySceneIds(sceneIds));
    }

    /**
     * 修改场景联动状态
     */
    @ApiOperation("修改场景联动状态")
    @PreAuthorize("@ss.hasPermi('iot:scene:edit')")
    @Log(title = "场景联动", businessType = BusinessType.UPDATE)
    @PutMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestBody Scene scene)
    {
        return toAjax(sceneService.updateStatus(scene));
    }
}
