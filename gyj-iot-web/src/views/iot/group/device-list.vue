<template>
    <el-dialog title="选择设备" v-model="openDeviceList" width="800px" append-to-body>
        <el-form :model="queryParams" ref="queryRef" :inline="true">
            <el-form-item label="设备名称" prop="deviceName">
                <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            </el-form-item>
        </el-form>

        <el-table v-loading="loading" :data="deviceList" @select="handleSelectionChange" @select-all="handleSelectionAll" ref="multipleTable" border>
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="设备名称" align="center" prop="deviceName" />
            <el-table-column label="设备编号" align="center" prop="serialNumber" />
            <el-table-column label="产品名称" align="center" prop="productName" />
            <el-table-column label="设备类型" align="center">
                <template #default="scope">
                    <el-tag type="success" v-if="scope.row.isOwner==0">分享</el-tag>
                    <el-tag v-else>拥有</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="设备状态" align="center" prop="status">
                <template #default="scope">
                    <dict-tag :options="iot_device_status" :value="scope.row.status" />
                </template>
            </el-table-column>
        </el-table>
        <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

        <template #footer>
            <div class="dialog-footer">
                <el-button type="primary" @click="handleDeviceSelected">确 定</el-button>
                <el-button @click="closeSelectDeviceList">取 消</el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script setup name="DeviceList">
import { getDeviceIds, updateDeviceGroups } from "@/api/iot/group"
import { listDeviceByGroup } from "@/api/iot/device";

const { proxy } = getCurrentInstance();
const { iot_device_status } = proxy.useDict("iot_device_status");

// 设备分组
const deviceGroup = ref({});
// 遮罩层
const loading = ref(true);
// 选中数组
const ids = ref([]);
// 是否显示设备列表
const openDeviceList = ref(false);
// 总条数
const total = ref(0);
// 设备表格数据
const deviceList = ref([]);
const daterangeActiveTime = ref([]);

const data = reactive({
    // 查询参数
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceName: null,
        productId: null,
        productName: null,
        userId: null,
        userName: null,
        tenantId: null,
        tenantName: null,
        serialNumber: null,
        status: null,
        networkAddress: null,
        activeTime: null,
    }
})

const { queryParams } = toRefs(data);

// 获取分组下关联的设备ID数组
function getDeviceIdsByGroupId(groupId) {
    getDeviceIds(groupId).then(response => {
        ids.value = response.data;
        getList();
    });
}
/** 查询设备列表 */
function getList() {
    loading.value = true;
    queryParams.value.params = {};
    if (null != daterangeActiveTime.value && '' != daterangeActiveTime.value) {
        queryParams.value.params["beginActiveTime"] = daterangeActiveTime.value[0];
        queryParams.value.params["endActiveTime"] = daterangeActiveTime.value[1];
    }
    listDeviceByGroup(queryParams.value).then(response => {
        deviceList.value = response.rows;
        total.value = response.total;
        loading.value = false;
        // 设置分组关联的设备选中
        deviceList.value.forEach(row => {
            proxy.$nextTick(() => {
                if (ids.value.some(x => x === row.deviceId)) {
                    proxy.$refs.multipleTable.toggleRowSelection(row, true);
                }
            })
        });
    });
}
/** 搜索按钮操作 */
function handleQuery() {
    queryParams.value.pageNum = 1;
    getList();
}
/** 重置按钮操作 */
function resetQuery() {
    daterangeActiveTime.value = [];
    proxy.resetForm("queryForm");
    handleQuery();
}
// 多选框选中数据
function handleSelectionChange(selection, row) {
    // 设备ID是否存在于原始设备ID数组中
    let index = ids.value.indexOf(row.deviceId);
    // 是否选中
    let value = selection.indexOf(row);
    if (index == -1 && value != -1) {
        // 不存在且选中
        ids.value.push(row.deviceId);
    } else if (index != -1 && value == -1) {
        // 存在且取消选中
        ids.value.splice(index, 1);
    }
}
// 全选事件处理
function handleSelectionAll(selection) {
    for (let i = 0; i < deviceList.value.length; i++) {
        // 设备ID是否存在于原始设备ID数组中
        let index = ids.value.indexOf(deviceList.value[i].deviceId);
        // 是否选中
        let value = selection.indexOf(deviceList.value[i]);
        if (index == -1 && value != -1) {
            // 不存在且选中
            ids.value.push(deviceList.value[i].deviceId);
        } else if (index != -1 && value == -1) {
            // 存在且取消选中
            ids.value.splice(index, 1);
        }
    }
}
// 关闭选择设备列表
function closeSelectDeviceList() {
    openDeviceList.value = false;
}
// 更新分组下的设备
function handleDeviceSelected() {
    let param = {
        groupId: deviceGroup.value.groupId,
        deviceIds: ids.value
    }
    updateDeviceGroups(param).then(response => {
        proxy.$modal.msgSuccess("更新分组下的设备成功");
        openDeviceList.value = false;
    })
}

// 显示弹框
function show(row) {
    if(row.groupId) {
        deviceGroup.value = row
        openDeviceList.value = true;
        queryParams.value.userId = row.userId;
        queryParams.value.pageNum = 1;
        getDeviceIdsByGroupId(row.groupId);
    }
}

defineExpose({
    show,
});
</script>
