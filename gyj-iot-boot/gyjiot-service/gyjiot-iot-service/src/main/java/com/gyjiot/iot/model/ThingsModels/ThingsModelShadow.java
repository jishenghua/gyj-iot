package com.gyjiot.iot.model.ThingsModels;

import com.gyjiot.common.core.thingsModel.ThingsModelSimpleItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 产品分类的Id和名称输出
 *
 * @author ji-sheng-hua
 * @date 2021-12-16
 */
public class ThingsModelShadow
{
    public ThingsModelShadow(){
        this.properties=new ArrayList<>();
        this.functions=new ArrayList<>();
    }

    public ThingsModelShadow(List<ThingsModelSimpleItem> properties, List<ThingsModelSimpleItem> functions){
        this.properties=properties;
        this.functions=functions;
    }

    /** 属性 */
    List<ThingsModelSimpleItem> properties;

    /** 功能 */
    List<ThingsModelSimpleItem> functions;

    public List<ThingsModelSimpleItem> getProperties() {
        return properties;
    }

    public void setProperties(List<ThingsModelSimpleItem> properties) {
        this.properties = properties;
    }

    public List<ThingsModelSimpleItem> getFunctions() {
        return functions;
    }

    public void setFunctions(List<ThingsModelSimpleItem> functions) {
        this.functions = functions;
    }
}
