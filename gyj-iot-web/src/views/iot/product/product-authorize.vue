<template>
<div style="padding-left:20px;">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
        <el-form-item label="设备编号" prop="serialNumber">
            <el-input v-model="queryParams.serialNumber" placeholder="请输入设备编号" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="授权码" prop="authorizeCode">
            <el-input v-model="queryParams.authorizeCode" placeholder="请输入授权码" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
                <el-option v-for="dict in iot_auth_status" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
        </el-form-item>
        <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['iot:authorize:add']">生成授权码</el-button>
        </el-col>
        <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['iot:authorize:remove']">批量删除</el-button>
        </el-col>
        <el-col :span="1.5">
            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['iot:authorize:export']">导出</el-button>
        </el-col>
        <el-col :span="1.5">
            <el-link type="info" style="padding-top:5px" :underline="false">Tips：双击可以复制授权码。</el-link>
        </el-col>
    </el-row>

    <el-table v-loading="loading" :data="authorizeList" @selection-change="handleSelectionChange" @cell-dblclick="celldblclick" size="small" border>
        <el-table-column type="selection" :selectable="selectable" width="55" align="center" />
        <el-table-column label="授权码" width="320" align="center" prop="authorizeCode" />
        <el-table-column label="状态" align="center" prop="active" width="100">
            <template #default="scope">
                <dict-tag :options="iot_auth_status" :value="scope.row.status" />
            </template>
        </el-table-column>
        <el-table-column label="设备编号" width="150" align="center" prop="serialNumber">
            <template #default="scope">
                <el-link type="primary" @click="getDeviceBySerialNumberInfo(scope.row.serialNumber)" :underline="false">{{scope.row.serialNumber}}</el-link>
            </template>
        </el-table-column>
        <el-table-column label="授权时间" align="center" prop="updateTime" width="180">
            <template #default="scope">
                <span>{{ parseTime(scope.row.updateTime, '{y}-{m}-{d} {h}:{m}:{s}') }}</span>
            </template>
        </el-table-column>
        <el-table-column label="备注" align="center" prop="remark" />
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
            <template #default="scope">
                <el-button type="text" icon="Coordinate" size="small" @click="handleUpdate(scope.row,'auth')" v-hasPermi="['iot:authorize:edit']" v-if="scope.row.status==1 && !scope.row.deviceId">设备授权</el-button>
                <el-button type="text" icon="Notebook" size="small" @click="handleUpdate(scope.row,'remark')" v-hasPermi="['iot:authorize:edit']">备注</el-button>
                <el-button type="text" icon="Delete" size="small" @click="handleDelete(scope.row)" v-hasPermi="['iot:authorize:remove']" v-if="!scope.row.deviceId">删除</el-button>
            </template>
        </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 设备授权和授权备注对话框 -->
    <el-dialog :title="title" v-model="open" :width="editWidth" append-to-body>
        <div v-if="editType=='auth'">
            <div class="el-divider el-divider--horizontal" style="margin-top: -25px;"></div>
            <el-form :model="deviceParams" ref="queryDeviceForm" :inline="true" label-width="68px">
                <el-form-item label="设备名称" prop="deviceName">
                    <el-input v-model="deviceParams.deviceName" placeholder="请输入设备名称" clearable size="small" @keyup.enter.native="handleQuery" style="width:150px;" />
                </el-form-item>
                <el-form-item label="设备编号" prop="serialNumber" style="margin:0 30px;">
                    <el-input v-model="deviceParams.serialNumber" placeholder="请输入设备编号" clearable size="small" @keyup.enter.native="handleQuery" style="width:150px;" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" icon="el-icon-search" @click="handleDeviceQuery">搜索</el-button>
                    <el-button icon="el-icon-refresh" @click="resetDeviceQuery">重置</el-button>
                </el-form-item>
            </el-form>
            <el-table v-loading="deviceLoading" :data="deviceList" ref="singleTable" @row-click="rowClick" highlight-current-row>
                <el-table-column label="选择" width="50" align="center">
                    <template #default="scope">
                        <input type="radio" :checked="scope.row.isSelect" name="device" />
                    </template>
                </el-table-column>
                <el-table-column label="设备名称" align="center" prop="deviceName" />
                <el-table-column label="设备ID" align="center" prop="deviceId" />
                <el-table-column label="设备编号" align="center" prop="serialNumber" />
                <el-table-column label="用户名称" align="center" prop="userName" />
                <el-table-column label="设备状态" align="center" prop="status">
                    <template #default="scope">
                        <dict-tag :options="iot_device_status" :value="scope.row.status" />
                    </template>
                </el-table-column>
            </el-table>
            <pagination v-show="deviceTotal>0" :total="deviceTotal" :page.sync="deviceParams.pageNum" :limit.sync="deviceParams.pageSize" @pagination="getDeviceList" />
        </div>
        <div v-if="editType=='remark'">
            <el-input v-model="form.remark" type="textarea" rows="4" placeholder="请输入内容" />
        </div>
        <template #footer>
            <div class="dialog-footer">
                <el-button type="primary" @click="submitForm">确 定</el-button>
                <el-button @click="cancel">取 消</el-button>
            </div>
        </template>
    </el-dialog>

    <!-- 设备详情对话框 -->
    <el-dialog title="设备详情" v-model="openDevice" width="600px" append-to-body>
        <div v-if="device==null" style="text-align:center;"><i class="el-icon-warning" style="color:#E6A23C;"></i> 提示：查找不到设备，可能已经被删除</div>
        <el-descriptions border :column="2" size="medium" v-if="device!=null">
            <el-descriptions-item label="设备ID">{{device.deviceId}}</el-descriptions-item>
            <el-descriptions-item label="设备名称">{{device.deviceName}}</el-descriptions-item>
            <el-descriptions-item label="设备编号">{{device.serialNumber}}</el-descriptions-item>
            <el-descriptions-item label="设备状态">
                <!-- （1-未激活，2-禁用，3-在线，4-离线） -->
                <el-tag v-if="device.status==1" type="warning">未激活</el-tag>
                <el-tag v-else-if="device.status==2" type="danger">禁用</el-tag>
                <el-tag v-else-if="device.status==3" type="success">在线</el-tag>
                <el-tag v-else-if="device.status==4" type="info">离线</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="设备影子">
                <el-tag v-if="device.isShadow==1" type="success">启用</el-tag>
                <el-tag v-else type="info">未启用</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="定位方式">
                <!-- (1=ip自动定位，2=设备定位，3=自定义) -->
                <el-tag v-if="device.locationWay==1" type="success">自动定位</el-tag>
                <el-tag v-else-if="device.locationWay==2" type="warning">设备定位</el-tag>
                <el-tag v-else-if="device.locationWay==3" type="primary">自定义位置</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="产品名称">{{device.productName}}</el-descriptions-item>
            <el-descriptions-item label="租户名称">{{device.userName}}</el-descriptions-item>
            <el-descriptions-item label="固件版本">Version {{device.firmwareVersion}}</el-descriptions-item>
            <el-descriptions-item label="所在地址">{{device.networkAddress}}</el-descriptions-item>
            <el-descriptions-item label="设备经度">{{device.longitude}}</el-descriptions-item>
            <el-descriptions-item label="设备纬度">{{device.latitude}}</el-descriptions-item>
            <el-descriptions-item label="入网IP">{{device.networkIp}}</el-descriptions-item>
            <el-descriptions-item label="设备信号">{{device.rssi}}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{device.createTime}}</el-descriptions-item>
            <el-descriptions-item label="激活时间">{{device.activeTime}}</el-descriptions-item>
            <el-descriptions-item label="备注信息">{{device.remark}}</el-descriptions-item>
        </el-descriptions>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="goToEditDevice(device.deviceId)" type="primary">查看设备</el-button>
                <el-button @click="closeDevice">关 闭</el-button>
            </div>
        </template>
    </el-dialog>
</div>
</template>

<style>
.createNum {
    width: 300px;
}

.createNum input {
    width: 260px;
}
</style>

<script setup name="ProductAuthorize">
import {
    getDeviceBySerialNumber,
} from "@/api/iot/device";
import {
    listUnAuthDevice,
} from "@/api/iot/device";
import {
    listAuthorize,
    getAuthorize,
    delAuthorize,
    addProductAuthorizeByNum,
    updateAuthorize
} from "@/api/iot/authorize";

const { proxy } = getCurrentInstance();
const { iot_auth_status, iot_device_status } = proxy.useDict('iot_auth_status', 'iot_device_status');

// 设备信息
const device = ref({});
// 设备信息对话框
const openDevice = ref(false);
// 设备遮罩层
const deviceLoading = ref(true);
// 总条数
const deviceTotal = ref(0);
// 设备表格数据
const deviceList = ref([]);
// 编辑类型，remark=备注、auth=设备授权
const editType = ref('');
// 编辑界面宽度
const editWidth = ref('500px');
// 遮罩层
const loading = ref(true);
// 选中数组
const ids = ref([]);
// 非多个禁用
const multiple = ref(true);
// 显示搜索条件
const showSearch = ref(true);
// 总条数
const total = ref(0);
// 产品授权码表格数据
const authorizeList = ref([]);
// 弹出层标题
const title = ref("");
// 是否显示弹出层
const open = ref(false);
// 新增授权码个数
const createNum = ref(10);
// 产品
const productInfo = ref({});

const data = reactive({
    // 查询参数
    deviceParams: {
        pageNum: 1,
        pageSize: 10,
        userId: null,
        deviceName: null,
        productId: 0,
        productName: null,
        userName: null,
        tenantId: null,
        tenantName: null,
        serialNumber: null,
        status: null,
        networkAddress: null,
        activeTime: null,
    },
    // 查询参数
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        authorizeCode: null,
        productId: null,
        deviceId: null,
        serialNumber: null,
        userId: null,
        userName: null,
        status: null,
    },
    // 表单参数
    form: {},
})

const { deviceParams, queryParams, form } = toRefs(data);

const props = defineProps({
    product: {
        type: Object,
        default: null
    }
});

// 获取到父组件传递的productId后，刷新列表
watch(() => props.product, (newVal, oldVal) => {
    productInfo.value = newVal;
    if (productInfo.value && productInfo.value.productId != 0) {
        queryParams.value.productId = productInfo.value.productId;
        deviceParams.value.productId = productInfo.value.productId;
        getList();
        getDeviceList();
    }
});

/**获取设备详情*/
function getDeviceBySerialNumberInfo(serialNumber) {
    openDevice.value = true;
    getDeviceBySerialNumber(serialNumber).then(response => {
        device.value = response.data;
    });
}
/** 修改按钮操作 */
function goToEditDevice(deviceId) {
    openDevice.value = false;
    proxy.$router.push({
        path: '/iot/device-edit',
        query: {
            deviceId: deviceId,
        }
    });
}
/** 查询设备列表 */
function getDeviceList() {
    deviceLoading.value = true;
    deviceParams.value.params = {};
    listUnAuthDevice(deviceParams.value).then(response => {
        //设备列表初始化isSelect值，用于单选
        for (let i = 0; i < response.rows.length; i++) {
            response.rows[i].isSelect = false;
        }
        deviceList.value = response.rows;
        deviceTotal.value = response.total;
        deviceLoading.value = false;
    });
}
/** 搜索按钮操作 */
function handleDeviceQuery() {
    deviceParams.value.pageNum = 1;
    getDeviceList();
}
/** 重置按钮操作 */
function resetDeviceQuery() {
    proxy.resetForm("queryDeviceForm");
    handleDeviceQuery();
}
/** 单选数据 */
function rowClick(device) {
    if (device != null) {
        setRadioSelected(device.deviceId);
        // 赋值
        form.value.userId = device.userId;
        form.value.userName = device.userName;
        form.value.deviceId = device.deviceId;
        form.value.serialNumber = device.serialNumber;
    }
}
/** 设置单选按钮选中 */
function setRadioSelected(deviceId) {
    for (let i = 0; i < deviceList.value.length; i++) {
        let device = deviceList.value[i];
        if (deviceList.value[i].deviceId == deviceId) {
            device.isSelect = true;
            proxy.$set(deviceList.value, i, device);
        } else {
            device.isSelect = false;
            proxy.$set(deviceList.value, i, device);
        }
    }
}
/** 查询产品授权码列表 */
function getList() {
    loading.value = true;
    listAuthorize(queryParams.value).then(response => {
        authorizeList.value = response.rows;
        total.value = response.total;
        loading.value = false;
    });
}
// 取消按钮
function cancel() {
    open.value = false;
    reset();
}
// 关闭设备详情
function closeDevice() {
    openDevice.value = false;
}
// 表单重置
function reset() {
    form.value = {
        authorizeId: null,
        authorizeCode: null,
        productId: "",
        userId: "",
        deviceId: null,
        serialNumber: null,
        userName: null,
        delFlag: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null
    };
    device.value = {};
    proxy.resetForm("form");
}
/** 搜索按钮操作 */
function handleQuery() {
    queryParams.value.pageNum = 1;
    getList();
}
/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm("queryForm");
    handleQuery();
}
// 多选框选中数据
function handleSelectionChange(selection) {
    ids.value = selection.map(item => item.authorizeId)
    multiple.value = !selection.length
}
/** 批量新增按钮操作 */
function handleAdd() {
    proxy.$prompt('', '输入授权码数量', {
        customClass: 'createNum',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /[0-9\-]/,
        inputErrorMessage: '数量内容不正确',
        inputType: 'number',
        inputValue: createNum.value
    }).then(({
        value
    }) => {
        createNum.value = value
        if (queryParams.value.productId != null) {
            let _addData = {
                productId: queryParams.value.productId,
                createNum: createNum.value
            }
            addProductAuthorizeByNum(_addData).then(response => {
                proxy.$modal.msgSuccess("新增授权码成功");
                getList();
                createNum.value = 10;
            });
        }
    }).catch(() => {
        proxy.$message({
            type: 'info',
            message: '取消新增'
        });
    });
}
/** 修改按钮操作 */
function handleUpdate(row, editType) {
    reset();
    editType.value = editType;
    const authorizeId = row.authorizeId || ids.value
    getAuthorize(authorizeId).then(response => {
        form.value = response.data;
        open.value = true;
        if (editType.value == 'auth') {
            title.value = "选择设备";
            editWidth.value = "800px";
        } else {
            title.value = "备注信息";
            editWidth.value = "500px";
        }
        // 取消选中

        for (let i = 0; i < deviceList.value.length; i++) {
            // deviceList.value[i].isSelect=false;
            let device = deviceList.value[i];
            device.isSelect = false;
            // deviceList.value.splice(i,1,device )
            proxy.$set(deviceList.value, i, device);
        }
    });
}
/** 提交按钮 */
function submitForm() {
    if (editType.value == 'auth') {
        if (form.value.deviceId != null && form.value.deviceId != 0) {
            updateAuthorize(form.value).then(response => {
                proxy.$modal.msgSuccess("设备授权成功");
                open.value = false;
                getList();
            });
        } else {
            proxy.$modal.msg("请选择要授权的设备");
        }
    } else if (form.value.authorizeId != null) {
        updateAuthorize(form.value).then(response => {
            proxy.$modal.msgSuccess("备注成功");
            open.value = false;
            getList();
        });
    }
}
/** 删除按钮操作 */
function handleDelete(row) {
    const authorizeIds = row.authorizeId || ids.value;
    proxy.$modal.confirm('是否确认删除产品授权码编号为"' + authorizeIds + '"的数据项？').then(function () {
        return delAuthorize(authorizeIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {});
}
/** 导出按钮操作 */
function handleExport() {
    proxy.download('iot/authorize/export', {
        ...queryParams.value
    }, `authorize_${new Date().getTime()}.xlsx`)
}
//禁用有绑定设备的复选框
function selectable(row) {
    return row.deviceId != null ? false : true;
}
//表格增加复制功能
async function celldblclick(row, column, cell, event) {
    try {
        await navigator.clipboard.writeText(row[column.property])
        onCopy()
    } catch (err) {
        onError()
    }
}
function onCopy() {
    proxy.$notify({
        title: '成功',
        message: '复制成功！',
        type: 'success',
        offset: 50,
        duration: 2000
    })
}
function onError() {
    proxy.$notify({
        title: '失败',
        message: '复制失败！',
        type: 'error',
        offset: 50,
        duration: 2000
    })
}
</script>
