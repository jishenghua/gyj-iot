<template>
    <el-card style="margin:6px;padding-bottom:100px;">
        <el-tabs v-model="activeName" tab-position="left" style="padding:10px;min-height:400px;" @tab-click="tabChange">
            <el-tab-pane label="" name="basic">
                <template #label><span style="color:red; padding-right:4px">* </span>基本信息</template>
                <el-form ref="productRef" :model="form" :rules="rules" label-width="100px">
                    <el-row :gutter="100">
                        <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="8">
                            <el-form-item label="产品名称" prop="productName">
                                <el-input v-model="form.productName" placeholder="请输入产品名称" :readonly="form.status == 2" />
                            </el-form-item>
                            <el-form-item label="产品分类" prop="categoryId">
                                <el-select v-model="form.categoryId" placeholder="请选择分类" @change="selectCategory"
                                    style="width:100%" :disabled="form.status == 2">
                                    <el-option v-for="category in categoryShortList" :key="category.id"
                                        :label="category.name" :value="category.id"></el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="设备类型" prop="deviceType">
                                <el-select v-model="form.deviceType" placeholder="请选择设备类型" @change="deviceTypeChange" :disabled="form.status == 2"
                                    style="width:100%">
                                    <el-option v-for="dict in iot_device_type" :key="dict.value"
                                        :label="dict.label" :value="parseInt(dict.value)"></el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="传输协议" prop="transport">
                                <el-select v-model="form.transport" placeholder="请选择传输协议" style="width: 100%"
                                    :disabled="true">
                                    <el-option v-for="dict in iot_transport_type" :key="dict.value"
                                        :label="dict.label" :value="dict.value" />
                                </el-select>
                            </el-form-item>
                            <el-form-item v-if="form.deviceType !== 3" label="编码协议" prop="protocolCode">
                              <el-select v-model="form.protocolCode" placeholder="请选择编码协议" style="width: 100%"
                                         :disabled="form.status == 2" @change="changeProductCode">
                                <el-option v-for="p in protocolList" :key="p.protocolCode" :label="p.protocolName"
                                           :value="p.protocolCode" />
                              </el-select>
                            </el-form-item>
                            <el-form-item label="联网方式" prop="networkMethod">
                                <el-select v-model="form.networkMethod" placeholder="请选择联网方式" style="width:100%;"
                                    :disabled="form.status == 2">
                                    <el-option v-for="dict in iot_network_method" :key="dict.value"
                                        :label="dict.label" :value="parseInt(dict.value)"></el-option>
                                </el-select>
                            </el-form-item>

                            <el-form-item label="备注信息" prop="remark">
                                <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" rows="3"
                                    :readonly="form.status == 2" />
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="8">
                            <el-form-item label="启用授权" prop="networkMethod">
                                <el-switch v-model="form.isAuthorize" @change="changeIsAuthorize(form.isAuthorize)"
                                    :active-value="1" :inactive-value="0"
                                    :disabled="form.status == 2 || form.deviceType == 3" />
                            </el-form-item>
                            <el-form-item label="认证方式" prop="vertificateMethod">
                                <el-select v-model="form.vertificateMethod" placeholder="请选择认证方式" style="width:100%"
                                    :disabled="form.status == 2 || form.deviceType == 3">
                                    <el-option v-for="dict in iot_vertificate_method" :key="dict.value"
                                        :label="dict.label" :value="parseInt(dict.value)"></el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="产品编号" prop="productId">
                                <el-input v-model="form.productId" placeholder="自动生成"
                                    :disabled="!form.mqttAccount || form.deviceType == 3" readonly />
                            </el-form-item>
                            <el-form-item label="Mqtt账号" prop="mqttAccount">
                                <el-input v-model="form.mqttAccount" placeholder="不填自动生成" :disabled="form.deviceType == 3"
                                    :readonly="accountInputType == 'password'" :type="accountInputType">
                                    <template #append>
                                        <el-button icon="View" style="font-size:18px;" @click="changeInputType('account')"></el-button>
                                    </template>
                                </el-input>
                            </el-form-item>
                            <el-form-item label="Mqtt密码" prop="mqttPassword">
                                <el-input v-model="form.mqttPassword" placeholder="不填则自动生成" :disabled="form.deviceType == 3"
                                    :readonly="passwordInputType == 'password'" :type="passwordInputType">
                                    <template #append>
                                        <el-button icon="View" style="font-size:18px;" @click="changeInputType('password')"></el-button>
                                    </template>
                                </el-input>
                            </el-form-item>
                            <el-form-item label="产品秘钥" prop="mqttSecret">
                                <el-input v-model="form.mqttSecret" placeholder="自动生成"
                                    :disabled="!form.mqttAccount || form.deviceType == 3" readonly :type="keyInputType">
                                    <template #append>
                                        <el-button icon="View" style="font-size:18px;" @click="changeInputType('key')"></el-button>
                                    </template>
                                </el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="8">
                            <el-form-item label="产品图片">
                                <div v-if="form.status == 2 && form.imgUrl == null">
                                    <el-image style="height:145px;height:145px;border-radius:10px;"
                                        :preview-src-list="[gatewayImg]" :src="gatewayImg" fit="cover"
                                        v-if="form.deviceType == 2"></el-image>
                                    <el-image style="height:145px;height:145px;border-radius:10px;"
                                        :preview-src-list="[videoImg]" :src="videoImg" fit="cover"
                                        v-else-if="form.deviceType == 3"></el-image>
                                    <el-image style="height:145px;height:145px;border-radius:10px;"
                                        :preview-src-list="[productImg]" :src="productImg" fit="cover" v-else></el-image>
                                </div>
                                <div v-else>
                                    <imageUpload ref="image-upload" :disabled="true" :value="form.imgUrl"
                                        :limit="form.status == 2 ? 0 : 1" :fileSize="1" @input="getImagePath($event)">
                                    </imageUpload>
                                </div>
                                <div class="el-upload__tip" style="color:#f56c6c"
                                    v-if="form.productId == null || form.productId == 0">提示：上传后需要提交保存</div>
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-col :span="20">
                        <el-form-item style="text-align: center;margin:10px 0px 40px 450px;">
                            <el-button type="primary" @click="submitForm" v-hasPermi="['iot:product:edit']"
                                v-show="form.productId != 0 && form.status != 2">修 改</el-button>
                            <el-button type="primary" @click="submitForm" v-hasPermi="['iot:product:add']"
                                v-show="form.productId == 0 && form.status != 2">新 增</el-button>
                        </el-form-item>
                    </el-col>
                </el-form>
            </el-tab-pane>

            <el-tab-pane label="" name="things" :disabled="form.productId == 0">
                <template #label><span style="color:red; padding-right:4px">* </span>产品模型</template>
                <product-things-model ref="productThingsModelRef" :product="form" />
            </el-tab-pane>

            <el-tab-pane label="设备授权" name="productAuthorize" :disabled="form.productId == 0" v-if="form.deviceType !== 3">
                <product-authorize ref="productAuthorizeRef" :product="form" />
            </el-tab-pane>

            <el-tab-pane label="SIP配置" name="sipConfig" :disabled="form.productId == 0" v-if="form.deviceType === 3">
                <config-sip ref="configSipRef" :product="form" />
            </el-tab-pane>

            <div style="margin-top: 200px"></div>

            <div style="margin-top:200px;"></div>

            <!-- 用于设置间距 -->
            <el-tab-pane v-for="(item, index) in 3" disabled>
                <template #label>
                    <div style="margin-top:100px;"></div>
                </template>
            </el-tab-pane>

            <el-tab-pane v-if="form.status == 1" name="product04" disabled>
                <template #label>
                    <el-button type="success" size="small" @click="updateProductStatus(2)"
                        v-hasPermi="['iot:product:add']">发布产品</el-button>
                </template>
            </el-tab-pane>
            <el-tab-pane v-if="form.status == 2" name="product05" disabled>
                <template #label>
                    <el-button type="danger" size="small" @click="updateProductStatus(1)"
                        v-hasPermi="['iot:product:edit']">取消发布</el-button>
                </template>
            </el-tab-pane>
            <el-tab-pane name="product06" disabled>
                <template #label>
                    <el-button type="info" size="small" @click="goBack()">返回列表</el-button>
                </template>
            </el-tab-pane>
        </el-tabs>

    </el-card>
</template>

<script setup name="ProductEdit">
import productThingsModel from "./product-things-model";
import productApp from "./product-app";
import productAuthorize from "./product-authorize";
import imageUpload from "../../../components/ImageUpload/index";
import configSip from '../sip/sipconfig.vue';
import gatewayImg from '@/assets/images/gateway.png'
import videoImg from '@/assets/images/video.png'
import productImg from '@/assets/images/product.png'

import {
    listProtocol
} from "@/api/iot/protocol";

import {
    listShortCategory,
} from "@/api/iot/category";
import {
    getProduct,
    addProduct,
    updateProduct,
    changeProductStatus,
    deviceCount,
} from "@/api/iot/product";

import {
    getAllPoints
} from "@/api/iot/template";

const { proxy } = getCurrentInstance();
const { iot_device_type, iot_network_method, iot_vertificate_method, iot_transport_type, data_collect_type }
= proxy.useDict('iot_device_type', 'iot_network_method', 'iot_vertificate_method', 'iot_transport_type', 'data_collect_type');

// 输入框类型
const keyInputType = ref("password");
const accountInputType = ref("password");
const passwordInputType = ref("password");
// 选中选项卡
const activeName = ref('basic');
// 分类短列表
const categoryShortList = ref([]);
//协议列表
const protocolList = ref([]);
const pointList = ref([]);
const open = ref(false);
// 弹出层标题
const title = ref("");
const loading = ref(true);
const tempList = ref([]);
// 总条数
const total = ref(0);
const tempTotal = ref(0);
// 查询参数
const pointsParams = ref({
    pageNum: 1,
    pageSize: 8,
    templateId: 0,
});
const tempParams = ref({
    pageNum: 1,
    pageSize: 10,
});
const currentRow = ref({});
const selectRowData = ref({});
const isModbus = ref(false);

const data = reactive({
    // 表单参数
    form: {
        networkMethod: 1,
        deviceType: 1,
        vertificateMethod: 3,
        transport: 'MQTT',
        imgUrl: "",
        protocolCode: "JSON",
        isAuthorize: 0,
    },
    // 表单校验
    rules: {
        productName: [{
            required: true,
            message: "产品名称不能为空",
            trigger: "blur",
        },
            {
                min: 1,
                max: 64,
                message: '产品名称不能少于1个字符和超过64字符',
                trigger: 'blur',
            },
        ],
        categoryId: [{
            required: true,
            message: "产品分类ID不能为空",
            trigger: "blur"
        }],
        deviceType: [{
            required: true,
            message: "请选择设备类型",
            trigger: "blur"
        }],
        protocolCode: [{
            required: true,
            message: "设备协议不能为空",
            trigger: "blur"
        }],
        transport: [{
            required: true,
            message: "传输协议不能为空",
            trigger: 'blur'
        }]
    },
    // 查询参数
    queryParams: {
        tenantName: null,
    }
})

const { form, rules, queryParams } = toRefs(data);

// 获取简短分类列表
function getShortCategory() {
    listShortCategory().then(response => {
        categoryShortList.value = response.data;
    })
}
/** 返回按钮 */
function goBack() {
    const obj = {
        path: "/iot/product",
        query: {
            t: Date.now(),
            pageNum: proxy.$route.query.pageNum
        }
    };
    proxy.$tab.closeOpenPage(obj);
    reset();
}
/** 获取产品信息 */
function getProductInfo() {
    getProduct(form.value.productId).then(response => {
        form.value = response.data;
        changeProductCode(form.value.protocolCode);
    });
}
// 表单重置
function reset() {
    form.value = {
        productId: 0,
        productName: null,
        categoryId: null,
        categoryName: null,
        status: 0,
        tslJson: null,
        isAuthorize: 0,
        deviceType: 1,
        networkMethod: 1,
        vertificateMethod: 3,
        mqttAccount: null,
        mqttPassword: null,
        mqttSecret: null,
        remark: null,
        imgUrl: "",
    };
    proxy.resetForm("productRef");
}
/** 提交按钮 */
function submitForm() {
    proxy.$refs["productRef"].validate(valid => {
        if (valid) {
            if (form.value.productId != null && form.value.productId != 0) {
                updateProduct(form.value).then(response => {
                    changeProductCode(form.value.protocolCode);
                    proxy.$modal.alertSuccess("修改成功");
                });
            } else {
                addProduct(form.value).then(response => {
                    if (!form.value.isModbus) {
                        proxy.$modal.alertSuccess("添加成功,可以开始定义物模型或配置");
                    } else {
                        proxy.$modal.alertSuccess("物模型已经从采集点模板同步至产品")
                    }
                    form.value = response.data;
                    changeProductCode(form.value.protocolCode);
                });
            }
        }
    });
}
/**同步获取产品下的设备数量**/
function getDeviceCountByProductId(productId) {
    return new Promise((resolve, reject) => {
        deviceCount(productId).then(res => {
            resolve(res);
        }).catch(error => {
            reject(error);
        })
    })
}
/** 更新产品状态 */
async function updateProductStatus(status) {
    let message = "确定取消发布？";
    if (status == 2) {
        message = "产品发布后，可以创建对应的设备";
    } else if (status == 1) {
        let result = await getDeviceCountByProductId(form.value.productId);
        if (result.data > 0) {
            message = "重要提示：产品下已有 " + result.data + " 个设备，取消发布可以修改产品信息和模型，重新发布后对应设备状态将会被重置！"
        }
    }
    proxy.$confirm(message, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        let data = {};
        data.productId = form.value.productId;
        data.status = status;
        data.deviceType = form.value.deviceType;
        changeProductStatus(data).then(response => {
            proxy.$modal.alertSuccess(response.msg);
            activeName.value = "basic";
            getProductInfo();
        }).catch(() => {
            if (status == 2) {
                activeName.value = "basic";
            } else {
                goBack();
            }
        });
    }).catch(() => {
        activeName.value = "basic";
    });
}
/** 选择分类 */
function selectCategory(val) {
    for (var i = 0; i < categoryShortList.value.length; i++) {
        if (categoryShortList.value[i].id == val) {
            form.value.categoryName = categoryShortList.value[i].name;
            return;
        }
    }
}
/**获取上传图片的路径 */
function getImagePath(data) {
    form.value.imgUrl = data;
}
/**改变输入框类型**/
function changeInputType(name) {
    if (name == "key") {
        keyInputType.value = keyInputType.value == "password" ? "text" : "password";
    } else if (name == "account") {
        accountInputType.value = accountInputType.value == "password" ? "text" : "password";
    } else if (name == "password") {
        passwordInputType.value = passwordInputType.value == "password" ? "text" : "password";
    }
}
// 授权码状态修改
function changeIsAuthorize() {
    let text = form.value.isAuthorize == "1" ? "启用" : "停用";
    proxy.$modal.confirm('确认要' + text + '授权码吗？').then(() => {
        if (form.value.productId != null && form.value.productId != 0) {
            updateProduct(form.value).then(response => {
                proxy.$modal.alertSuccess("授权码已" + text);
            });
        }
    }).catch(() => {
        form.value.isAuthorize = 0;
    });
}
//获取设备协议
function getProtocol() {
    const data = {
        protocolStatus: 1
    };
    listProtocol(data).then(res => {
        protocolList.value = res.rows;
    })
}
// 设备类型改变事件
function deviceTypeChange(type){
    if(type===3){
        form.value.transport="GB28181";
    }else{
        form.value.transport="MQTT";
    }
}
// 取消按钮
function cancel() {
    open.value = false;
}
function getList() {
    getAllPoints(pointsParams.value).then(response => {
        pointList.value = response.rows;
        total.value = response.total;
    });
}
function changeProductCode(val) {
    if (val && val.startsWith("MODBUS")) {
        form.value.deviceType = 2;
        form.value.isModbus = true;
        if (form.value.productId != 0 && form.value.productId != null) {
        }
    } else {
        form.value.isModbus = false;
    }
}
/**选项卡切换事件**/
function tabChange(tabItem) {
    // 切换到告警配置，获取物模型
    if (tabItem.paneName == "alert") {
        //this.$refs.productAlert.getCacheThingsModel(form.value.productId);
    }
}

/*按照模板名查询*/
function queryTemp() {
    getTempList();
}

/** 搜索按钮操作 */
function handleQuery() {
    tempParams.value.pageNum = 1
    getTempList()
}

/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm('tempParams')
    handleQuery()
}

onActivated(() => {
    const time = proxy.$route.query.t;
    if (time != null && time != uniqueId.value) {
        uniqueId.value = time;
    }
    // 获取产品信息
    let productId = proxy.$route.query.productId
    if (productId != null && productId != 0) {
        form.value.productId = Number(productId);
        getProductInfo();
        getShortCategory();
    }
    // 切换选项卡
    const tabPanelName = proxy.$route.query && proxy.$route.query.tabPanelName;
    if (tabPanelName != null && tabPanelName != '') {
        activeName.value = tabPanelName;
    }
})

// 获取产品信息
const productId = proxy.$route.query && proxy.$route.query.productId;
form.value.productId = productId;
if (form.value.productId != 0 && form.value.productId != null) {
    getProductInfo();
}
// 切换选项卡
const tabPanelName = proxy.$route.query && proxy.$route.query.tabPanelName;
if (tabPanelName != null && tabPanelName != '') {
    activeName.value = tabPanelName;
}
// 获取分类信息
getShortCategory();
// 设置账号密码输入框类型,新增时为text，查看时为password
if (!form.value.productId || form.value.productId == 0) {
    accountInputType.value = "text";
    passwordInputType.value = "text";
}
getProtocol();
</script>

<style>
.el-aside {
    margin: 0;
    padding: 0;
    background-color: #fff;
    color: #333;
}

.el-main {
    margin: 0;
    padding: 0 10px;
    background-color: #fff;
    color: #333;
}
</style>
