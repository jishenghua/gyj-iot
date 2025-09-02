<template>
<div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px">
        <el-form-item label="产品名称" prop="productName">
            <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
            <el-input v-model="queryParams.categoryName" placeholder="请输入产品分类名称" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
                <el-option v-for="dict in iot_product_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
        </el-form-item>
        <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            <el-button type="primary" plain icon="Plus" @click="handleEditProduct(0)" v-hasPermi="['iot:product:add']">新增</el-button>
        </el-form-item>
    </el-form>
    <el-row :gutter="30" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="12" :lg="8" :xl="6" v-for="(item,index) in productList" :key="index" style="margin-bottom:30px;text-align:center;">
            <el-card :body-style="{ padding: '20px'}" shadow="always" class="card-item">
                <el-row type="flex" :gutter="10" justify="space-between">
                    <el-col :span="20" style="text-align:left;">
                        <el-link :underline="false" @click="handleEditProduct(item)" style="font-weight:bold;font-size:16px;line-height:32px;white-space:nowrap;">
                            <svg-icon icon-class="product" />
                            <span style="margin: 0 5px">{{ item.productName }}</span>
                            <el-tag type="info" size="small" style="margin-left:5px;font-weight:200" v-if="item.isSys==1">系统</el-tag>
                        </el-link>
                    </el-col>
                    <el-col :span="4">
                        <el-tooltip class="item" effect="dark" content="取消发布" placement="top-start" v-if="item.status==2">
                            <el-button type="success" size="small" style="padding:5px;" @click="updateProductStatus(item.productId,1,item.deviceType)">已发布</el-button>
                        </el-tooltip>
                        <el-tooltip class="item" effect="dark" content="现在发布" placement="top-start" v-if="item.status==1">
                            <el-button type="info" size="small" style="padding:5px;" @click="updateProductStatus(item.productId,2,item.deviceType)">未发布</el-button>
                        </el-tooltip>
                    </el-col>
                </el-row>
                <el-row :gutter="10">
                    <el-col :span="14">
                        <el-descriptions :column="1" size="small" style="margin-top:10px;white-space:nowrap;">
                            <el-descriptions-item label="所属分类">
                                <el-link type="primary" :underline="false">{{item.categoryName}}</el-link>
                            </el-descriptions-item>
                            <el-descriptions-item label="产品类型" label-class-name="desc-class">
                                <dict-tag :options="iot_device_type" :value="item.deviceType" />
                            </el-descriptions-item>
                            <el-descriptions-item label="联网方式" label-class-name="desc-class">
                                <dict-tag :options="iot_network_method" :value="item.networkMethod" />
                            </el-descriptions-item>
                            <el-descriptions-item label="设备授权">
                                <el-tag type="success" size="small" v-if="item.isAuthorize==1">已启用</el-tag>
                                <el-tag type="info" size="small" v-else>未启用</el-tag>
                            </el-descriptions-item>
                        </el-descriptions>
                    </el-col>
                    <el-col :span="10">
                        <div style="margin-top:10px;">
                            <el-image style="width:100%;height:100px;border-radius:10px;" lazy :preview-src-list="[baseUrl+item.imgUrl]" :src="baseUrl+item.imgUrl" fit="cover" v-if="item.imgUrl!=null && item.imgUrl!=''"></el-image>
                            <el-image style="width:100%;height:100px;border-radius:10px;" :preview-src-list="[gatewayImg]" :src="gatewayImg" fit="cover" v-else-if="item.deviceType==2"></el-image>
                            <el-image style="width:100%;height:100px;border-radius:10px;" :preview-src-list="[videoImg]" :src="videoImg" fit="cover" v-else-if="item.deviceType==3"></el-image>
                            <el-image style="width:100%;height:100px;border-radius:10px;" :preview-src-list="[productImg]" :src="productImg" fit="cover" v-else></el-image>
                        </div>
                    </el-col>
                </el-row>
                <el-button-group style="margin-top:15px;height:28px;">
                    <el-button size="small" type="primary" icon="View" @click="handleEditProduct(item)" v-hasPermi="['iot:product:query']">详情</el-button>
                    <el-button size="small" type="danger" icon="Delete" @click="handleDelete(item)" v-hasPermi="['iot:product:remove']" v-if="item.status==1">删除</el-button>
                    <el-button size="small" type="success" icon="Stamp" @click="handleDeviceAuthorize(item)" v-hasPermi="['iot:product:edit']" v-if="item.status==2" :disabled="item.isAuthorize!=1">设备授权</el-button>
                    <el-button size="small" type="warning" icon="Search" @click="handleViewDevice(item.productId)" v-hasPermi="['iot:device:query']">查看设备</el-button>
                </el-button-group>
            </el-card>
        </el-col>
    </el-row>

    <el-empty description="暂无数据，请添加产品" v-if="total==0"></el-empty>
    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :pageSizes="[12, 24, 36, 60]" @pagination="getList" />

    <!-- 下载SDK -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
        <el-link type="danger" style="padding-left:10px;" :underline="false">该功能暂未实现，参考教程和项目的SDK示例</el-link>
        <el-form label-width="80px">
            <el-form-item label="设备类型">
                <el-radio-group v-model="form.datatype">
                    <el-radio v-for="dict in iot_device_chip" :key="dict.value" :label="dict.value" style="margin-top:15px;width:160px;">{{dict.label}}</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-form>
        <template #footer>
            <div class="dialog-footer">
                <el-button type="primary" @click="downloadSdk" disabled>下 载</el-button>
                <el-button @click="cancel">取 消</el-button>
            </div>
        </template>
    </el-dialog>
</div>
</template>

<script setup name="Product">
import { listProduct, delProduct, changeProductStatus, deviceCount,} from "@/api/iot/product";

import {checkPermi} from "@/utils/permission"

import gatewayImg from '@/assets/images/gateway.png'
import videoImg from '@/assets/images/video.png'
import productImg from '@/assets/images/product.png'

const { proxy } = getCurrentInstance();
const { iot_yes_no, iot_product_status, iot_device_type, iot_network_method, iot_vertificate_method, iot_device_chip }
        = proxy.useDict('iot_yes_no', 'iot_product_status', 'iot_device_type', 'iot_network_method', 'iot_vertificate_method', 'iot_device_chip');

// 遮罩层
const loading = ref(true);
// 总条数
const total = ref(0);
// 产品表格数据
const productList = ref([]);
// 弹出层标题
const title = ref("");
// 是否显示弹出层
const open = ref(false);
const uniqueId = ref("");
const ids = ref([]);

const data = reactive({
    // 查询参数
    queryParams: {
        pageNum: 1,
        pageSize: 12,
        productName: null,
        categoryId: null,
        categoryName: null,
        tenantId: null,
        tenantName: null,
        isSys: null,
        status: null,
        deviceType: null,
        networkMethod: null,
    },
    // 表单参数
    form: {},
    baseUrl: import.meta.env.VITE_APP_BASE_API,
})
const { queryParams, form, baseUrl } = toRefs(data);

onActivated(() => {
    const time = proxy.$route.query.t;
    if (time != null && time != uniqueId.value) {
        uniqueId.value = time;
        queryParams.value.pageNum = Number(proxy.$route.query.pageNum);
        getList();
    }
});

/** 查询产品列表 */
function getList() {
    loading.value = true;
    listProduct(queryParams.value).then(response => {
        productList.value = response.rows;
        total.value = response.total;
        loading.value = false;
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
async function  updateProductStatus(productId, status, deviceType) {
    let message = "确定取消发布？";
    if (status == 2) {
        // 发布
        let hasPermission = checkPermi(['iot:product:add']);
        if (!hasPermission) {
            proxy.$modal.alertError("没有操作权限");
            return;
        }
        message = "产品发布后，可以创建对应的设备";
    } else if (status == 1) {
        // 取消发布
        let hasPermission = checkPermi(['iot:product:edit']);
        if (!hasPermission) {
            proxy.$modal.alertError("没有操作权限");
            return;
        }
        let result = await getDeviceCountByProductId(productId);
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
        data.productId = productId;
        data.status = status;
        data.deviceType = deviceType;
        changeProductStatus(data).then(response => {
            getList();
            proxy.$modal.alertSuccess(response.msg);
        }).catch(() => {});
    }).catch(() => {});
}
/** 查看设备按钮操作 */
function handleViewDevice(productId) {
    proxy.$router.push({
        path: '/iot/device',
        query: {
            t: Date.now(),
            productId: productId,
        }
    });
}
// 取消按钮
function cancel() {
    open.value = false;
    reset();
}
/** 搜索按钮操作 */
function handleQuery() {
    queryParams.value.pageNum = 1;
    getList();
}
/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm("queryRef");
    handleQuery();
}
/** 生成SDK */
function handleGeneratorSDK(row) {
    title.value = "生成SDK"
    open.value = true;
}
/** 下载SDK */
function downloadSdk() {
    proxy.$download.zip("/iot/tool/genSdk?deviceChip=" + 1, "gyjiot-sdk");
}
/** 删除按钮操作 */
function handleDelete(row) {
    const productIds = row.productId || ids.value;
    let msg = "";
    proxy.$modal.confirm('是否确认删除产品编号为"' + productIds + '"的数据项？').then(function () {
        // // 删除SIP配置
        // delSipconfigByProductId(productIds).then(response => {});
        return delProduct(productIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess(msg);
    }).catch(() => {});
}
/** 修改按钮操作 */
function handleEditProduct(row) {
    let productId = 0;
    if (row != 0) {
        productId = row.productId || ids.value
    }
    proxy.$router.push({
        path: '/prod/product-edit',
        query: {
            productId: productId,
            pageNum: queryParams.value.pageNum
        }
    });
}
/** 设备授权操作 */
function handleDeviceAuthorize(row) {
    let productId = row.productId
    proxy.$router.push({
        path: '/prod/product-edit',
        query: {
            productId: productId,
            tabPanelName: 'productAuthorize',
            pageNum: queryParams.value.pageNum
        }
    });
}

getList();
</script>

<style scoped>
.card-item {
    border-radius: 15px;
}
:deep(.desc-class) {
    float: left;
}
</style>
