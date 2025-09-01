package com.gyjiot.iot.service.impl;

import com.gyjiot.common.core.domain.AjaxResult;
import com.gyjiot.common.core.domain.entity.SysRole;
import com.gyjiot.common.core.domain.entity.SysUser;
import com.gyjiot.common.utils.DateUtils;
import com.gyjiot.iot.domain.Category;
import com.gyjiot.iot.mapper.CategoryMapper;
import com.gyjiot.iot.model.IdAndName;
import com.gyjiot.iot.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.gyjiot.common.utils.SecurityUtils.getLoginUser;

/**
 * 产品分类Service业务层处理
 * 
 * @author ji-sheng-hua
 * @date 2021-12-16
 */
@Service
public class CategoryServiceImpl implements ICategoryService 
{
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 查询产品分类
     * 
     * @param categoryId 产品分类主键
     * @return 产品分类
     */
    @Override
    public Category selectCategoryByCategoryId(Long categoryId)
    {
        return categoryMapper.selectCategoryByCategoryId(categoryId);
    }

    /**
     * 查询产品分类列表
     * 
     * @param category 产品分类
     * @return 产品分类
     */
    @Override
    public List<Category> selectCategoryList(Category category)
    {
        SysUser user = getLoginUser().getUser();
        List<SysRole> roles=user.getRoles();
        // 租户
        if(roles.stream().anyMatch(a->a.getRoleKey().equals("tenant"))){
            category.setTenantId(user.getUserId());
        }
        return categoryMapper.selectCategoryList(category);
    }

    /**
     * 查询产品分简短类列表
     *
     * @return 产品分类
     */
    @Override
    public List<IdAndName> selectCategoryShortList()
    {
        Category category=new Category();
        SysUser user = getLoginUser().getUser();
        List<SysRole> roles=user.getRoles();
        // 租户
        if(roles.stream().anyMatch(a->a.getRoleKey().equals("tenant"))){
            category.setTenantId(user.getUserId());
        }
        return categoryMapper.selectCategoryShortList(category);
    }

    /**
     * 新增产品分类
     * 
     * @param category 产品分类
     * @return 结果
     */
    @Override
    public int insertCategory(Category category)
    {
        // 判断是否为管理员
        category.setIsSys(1);
        SysUser user = getLoginUser().getUser();
        List<SysRole> roles=user.getRoles();
        for(int i=0;i<roles.size();i++){
            if(roles.get(i).getRoleKey().equals("tenant") || roles.get(i).getRoleKey().equals("general")){
                category.setIsSys(0);
                break;
            }
        }
        category.setTenantId(user.getUserId());
        category.setTenantName(user.getUserName());
        category.setCreateTime(DateUtils.getNowDate());
        return categoryMapper.insertCategory(category);
    }

    /**
     * 修改产品分类
     * 
     * @param category 产品分类
     * @return 结果
     */
    @Override
    public int updateCategory(Category category)
    {
        category.setUpdateTime(DateUtils.getNowDate());
        return categoryMapper.updateCategory(category);
    }

    /**
     * 批量删除产品分类
     * 
     * @param categoryIds 需要删除的产品分类主键
     * @return 结果
     */
    @Override
    public AjaxResult deleteCategoryByCategoryIds(Long[] categoryIds)
    {
        int productCount=categoryMapper.productCountInCategorys(categoryIds);
        if(productCount>0){
            return AjaxResult.error("删除失败，请先删除对应分类下的产品");
        }
        if(categoryMapper.deleteCategoryByCategoryIds(categoryIds)>0){
            return AjaxResult.success("删除成功");
        }
        return AjaxResult.error("删除失败");
    }

    /**
     * 删除产品分类信息
     * 
     * @param categoryId 产品分类主键
     * @return 结果
     */
    @Override
    public int deleteCategoryByCategoryId(Long categoryId)
    {
        return categoryMapper.deleteCategoryByCategoryId(categoryId);
    }
}
