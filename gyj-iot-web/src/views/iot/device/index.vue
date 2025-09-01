<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryRef" :inline="true">
            <el-form-item label="设备名称" prop="deviceName">
                <el-input v-model="queryParams.deviceName" placeholder="请输入设备名称" clearable @keyup.enter.native="handleQuery" style="width: 150px" />
            </el-form-item>
            <el-form-item label="设备编号" prop="serialNumber">
                <el-input v-model="queryParams.serialNumber" placeholder="请输入设备编号" clearable @keyup.enter.native="handleQuery" style="width: 150px" />
            </el-form-item>
            <el-form-item label="设备状态" prop="status">
                <el-select v-model="queryParams.status" placeholder="请选择设备状态" clearable style="width: 150px">
                    <el-option v-for="dict in iot_device_status" :key="dict.value" :label="dict.label" :value="dict.value" />
                </el-select>
            </el-form-item>
            <el-form-item label="我的分组">
                <el-select v-model="queryParams.groupId" placeholder="请选择我的分组" clearable style="width: 150px">
                    <el-option v-for="group in myGroupList" :key="group.groupId" :label="group.groupName" :value="group.groupId" />
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
        </el-form>
        <el-row :gutter="10" class="mb8">
            <el-col :span="6">
                <el-button type="primary" plain icon="Plus" @click="handleEditDevice(0)" v-hasPermi="['iot:device:add']">新增</el-button>
                <el-button type="primary" plain icon="Grid" @click="handleChangeShowType" v-hasPermi="['iot:device:add']">切换</el-button>
            </el-col>
        </el-row>

        <div style="padding-top:10px; padding-bottom: 100px" v-if="showType == 'list'">
            <el-table v-loading="loading" :data="deviceList" border>
                <el-table-column label="编号" align="center" header-align="center" prop="deviceId" width="60" />
                <el-table-column label="设备名称" align="center" header-align="center" prop="deviceName" min-width="120" />
                <el-table-column label="设备编号" align="center" prop="serialNumber" min-width="130" />
                <el-table-column label="所属产品" align="center" prop="productName" min-width="120" />
                <el-table-column label="协议" align="center" prop="transport" min-width="70" />
                <el-table-column label="通讯协议" align="center" prop="protocolCode" min-width="100" />
                <el-table-column label="子设备数" align="center" prop="subDeviceCount" width="80">
                    <template #default="scope">
                        {{ scope.row.subDeviceCount }}
                    </template>
                </el-table-column>
                <el-table-column label="设备影子" align="center" prop="isShadow" width="80">
                    <template #default="scope">
                        <el-tag type="success" size="small" v-if="scope.row.isShadow == 1">启用</el-tag>
                        <el-tag type="info" size="small" v-else>禁用</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="状态" align="center" prop="status" width="80">
                    <template #default="scope">
                        <dict-tag :options="iot_device_status" :value="scope.row.status" size="small" />
                    </template>
                </el-table-column>
                <el-table-column label="信号" align="center" prop="rssi" width="60">
                    <template #default="scope">
                        <svg-icon v-if="scope.row.status == 3 && scope.row.rssi >= '-55'" icon-class="wifi_4" />
                        <svg-icon v-else-if="scope.row.status == 3 && scope.row.rssi >= '-70' && scope.row.rssi < '-55'" icon-class="wifi_3" />
                        <svg-icon v-else-if="scope.row.status == 3 && scope.row.rssi >= '-85' && scope.row.rssi < '-70'" icon-class="wifi_2" />
                        <svg-icon v-else-if="scope.row.status == 3 && scope.row.rssi >= '-100' && scope.row.rssi < '-85'" icon-class="wifi_1" />
                        <svg-icon v-else icon-class="wifi_0" />
                    </template>
                </el-table-column>
                <el-table-column label="定位方式" align="center" prop="locationWay">
                    <template #default="scope">
                        <dict-tag :options="iot_location_way" :value="scope.row.locationWay" size="small" />
                    </template>
                </el-table-column>
                <el-table-column label="固件版本" align="center" prop="firmwareVersion">
                    <template #default="scope">
                        <el-tag size="small" type="info">Ver {{ scope.row.firmwareVersion }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="激活时间" align="center" prop="activeTime">
                    <template #default="scope">
                        <span>{{ parseTime(scope.row.activeTime, '{y}-{m}-{d}') }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="创建时间" align="center" prop="createTime">
                    <template #default="scope">
                        <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d}') }}</span>
                    </template>
                </el-table-column>

                <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
                    <template #default="scope">
                        <el-button type="danger" size="small" style="padding: 5px" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['iot:device:remove']">删除</el-button>
                        <el-button type="primary" size="small" style="padding: 5px" icon="View" @click="handleEditDevice(scope.row)" v-hasPermi="['iot:device:add']">查看</el-button>
                        <el-button type="primary" size="small" style="padding: 5px" @click="openSummaryDialog(scope.row)" v-if="form.deviceId != 0">二维码</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :pageSizes="[12, 24, 36, 60]" @pagination="getList" />
        </div>

        <div style="padding-top:10px; padding-bottom: 100px" v-if="showType == 'card'">
            <el-row :gutter="30" v-loading="loading">
                <el-col :xs="24" :sm="12" :md="12" :lg="8" :xl="6" v-for="(item, index) in deviceList" :key="index" style="margin-bottom: 30px; text-align: center">
                    <el-card :body-style="{ padding: '20px' }" shadow="always" class="card-item">
                        <el-row type="flex" :gutter="10" justify="space-between">
                            <el-col :span="19" style="text-align: left">
                                <el-link type="default" :underline="false" @click="handleEditDevice(item)" style="font-weight: bold; font-size: 16px; line-height: 32px">
                                    <el-tooltip class="item" effect="dark" content="分享的设备" placement="top-start">
                                        <svg-icon icon-class="share" style="font-size: 20px" v-if="item.isOwner != 1" />
                                    </el-tooltip>
                                    <svg-icon icon-class="device" v-if="item.isOwner == 1" />
                                    <span style="margin: 0 5px">{{ item.deviceName }}</span>
                                </el-link>
                            </el-col>
                            <el-col :span="2" style="font-size: 20px; padding-top: 5px; cursor: pointer">
                                <svg-icon icon-class="qrcode" @click="openSummaryDialog(item)" />
                            </el-col>
                            <el-col :span="3">
                                <div style="font-size: 28px; color: #ccc">
                                    <svg-icon v-if="item.status == 3 && item.rssi >= '-55'" icon-class="wifi_4" />
                                    <svg-icon v-else-if="item.status == 3 && item.rssi >= '-70' && item.rssi < '-55'" icon-class="wifi_3" />
                                    <svg-icon v-else-if="item.status == 3 && item.rssi >= '-85' && item.rssi < '-70'" icon-class="wifi_2" />
                                    <svg-icon v-else-if="item.status == 3 && item.rssi >= '-100' && item.rssi < '-85'" icon-class="wifi_1" />
                                    <svg-icon v-else icon-class="wifi_0" />
                                </div>
                            </el-col>
                        </el-row>
                        <el-row :gutter="10">
                            <el-col :span="17">
                                <div style="text-align: left; line-height: 40px; white-space: nowrap">
                                    <dict-tag :options="iot_device_status" :value="item.status" size="small" style="display: inline-block" />
                                    <span style="display: inline-block; margin: 0 10px">
                                        <el-tag size="small" v-if="item.protocolCode">{{ item.protocolCode }}</el-tag>
                                    </span>
                                    <el-tag size="small" v-if="item.transport">{{ item.transport }}</el-tag>
                                </div>
                                <el-descriptions :column="1" size="small" style="white-space: nowrap">
                                    <el-descriptions-item label="编号">
                                        {{ item.serialNumber }}
                                    </el-descriptions-item>
                                    <el-descriptions-item label="产品">
                                        {{ item.productName }}
                                    </el-descriptions-item>
                                    <el-descriptions-item label="激活时间">
                                        {{ parseTime(item.activeTime, '{y}-{m}-{d}') }}
                                    </el-descriptions-item>
                                </el-descriptions>
                            </el-col>
                            <el-col :span="7">
                                <div style="margin-top: 10px">
                                    <el-image
                                        style="width: 100%; height: 100px; border-radius: 10px"
                                        lazy
                                        :preview-src-list="[baseUrl + item.imgUrl]"
                                        :src="baseUrl + item.imgUrl"
                                        fit="cover"
                                        v-if="item.imgUrl != null && item.imgUrl != ''"
                                    ></el-image>
                                    <el-image
                                        style="width: 100%; height: 100px; border-radius: 10px"
                                        :preview-src-list="[gatewayImg]"
                                        :src="gatewayImg"
                                        fit="cover"
                                        v-else-if="item.deviceType == 2"
                                    ></el-image>
                                    <el-image
                                        style="width: 100%; height: 100px; border-radius: 10px"
                                        :preview-src-list="[videoImg]"
                                        :src="videoImg"
                                        fit="cover"
                                        v-else-if="item.deviceType == 3"
                                    ></el-image>
                                    <el-image
                                        style="width: 100%; height: 100px; border-radius: 10px"
                                        :preview-src-list="[productImg]"
                                        :src="productImg"
                                        fit="cover"
                                        v-else
                                    ></el-image>
                                </div>
                            </el-col>
                        </el-row>
                        <el-button-group style="margin-top: 15px">
                            <el-button type="danger" size="small" style="padding: 5px 10px" icon="Delete" @click="handleDelete(item)" v-hasPermi="['iot:device:remove']">删除</el-button>
                            <el-button type="primary" size="small" style="padding: 5px 15px" icon="View" @click="handleEditDevice(item, 'basic')" v-hasPermi="['iot:device:add']">查看</el-button>
                            <el-button type="success" size="small" style="padding: 5px 15px" icon="Odometer" @click="handleRunDevice(item)" v-hasPermi="['iot:device:add']">运行状态</el-button>
                        </el-button-group>
                    </el-card>
                </el-col>
            </el-row>
            <el-empty description="暂无数据，请添加设备" v-if="total == 0"></el-empty>
            <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :pageSizes="[12, 24, 36, 60]" @pagination="getList" />
        </div>
        <!-- 二维码 -->
        <el-dialog v-model="openSummary" width="300px" append-to-body>
            <div style="border: 1px solid #ccc; width: 220px; text-align: center; margin: 0 auto; margin-top: -15px">
                <vue-qr :text="qrText" :size="200"></vue-qr>
                <div style="padding-bottom: 10px">设备二维码</div>
            </div>
        </el-dialog>
    </div>
</template>

<script setup name="Device">
import vueQr from 'vue-qr/src/packages/vue-qr.vue'
import { listDeviceShort, delDevice } from '@/api/iot/device';
import { listGroup } from '@/api/iot/group';
import { delSipDeviceBySipId } from '@/api/iot/sipdevice';
import mqttTool from '@/utils/mqttTool'
import gatewayImg from '@/assets/images/gateway.png'
import videoImg from '@/assets/images/video.png'
import productImg from '@/assets/images/product.png'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

const { proxy } = getCurrentInstance();
const { iot_device_status, iot_is_enable, iot_location_way, iot_transport_type }
    = proxy.useDict('iot_device_status', 'iot_is_enable', 'iot_location_way', 'iot_transport_type');

// 二维码内容
const qrText = ref('gyjiot');
// 打开设备配置对话框
const openSummary = ref(false);
// 展示方式
const showType = ref('card');
// 遮罩层
const loading = ref(true);
// 总条数
const total = ref(0);
// 设备列表数据
const deviceList = ref([]);
// 我的分组列表数据
const myGroupList = ref([]);
const isSubDev = ref(false);

const data = reactive({
    // 查询参数
    queryParams: {
        pageNum: 1,
        pageSize: 12,
        deviceName: null,
        productId: null,
        groupId: null,
        productName: null,
        userId: null,
        userName: null,
        tenantId: null,
        tenantName: null,
        serialNumber: null,
        status: null,
        networkAddress: null,
        activeTime: null,
    },
    // 表单参数
    form: {
        productId: 0,
        status: 1,
        locationWay: 1,
        firmwareVersion: 1.0,
        serialNumber: '',
        deviceType: 1,
        isSimulate: 0,
    },
    // 根路径
    baseUrl: import.meta.env.VITE_APP_BASE_API
})

const { queryParams, form, baseUrl } = toRefs(data);

onActivated(() => {
    const time = proxy.$route.query.t;
    if (time != null && time != proxy.uniqueId) {
        proxy.uniqueId = time;
        // 页码筛选
        let pageNum = proxy.$route.query.pageNum;
        if (pageNum != null) {
            queryParams.value.pageNum = Number(pageNum);
        }
        // 产品筛选
        let productId = proxy.$route.query.productId;
        if (productId != null) {
            queryParams.value.productId = Number(productId);
            queryParams.value.groupId = null;
            queryParams.value.serialNumber = null;
        }
        // 分组筛选
        let groupId = proxy.$route.query.groupId;
        if (groupId != null) {
            queryParams.value.groupId = Number(groupId);
            queryParams.value.productId = null;
            queryParams.value.serialNumber = null;
        }
        // 设备编号筛选
        let sn = proxy.$route.query.sn;
        if (sn != null) {
            queryParams.value.serialNumber = sn;
            queryParams.value.productId = null;
            queryParams.value.groupId = null;
        }
        getList();
    }
})


// 连接 MQTT 并处理回调
const connectMqtt = async () => {
    try {
        if (!mqttTool.client) {
            await mqttTool.connect();
        }
        mqttTool.client?.on('connect', () => {
            mqttCallback(); // 连接成功后再执行回调
            getList();
        });
    } catch (error) {
        console.error('MQTT 连接失败:', error);
        // 可选：显示错误提示（如 ElMessage）
    }
};

/* Mqtt回调处理  */
function mqttCallback() {
    mqttTool.client.on('message', (topic, message, buffer) => {
        let topics = topic.split('/');
        let productId = topics[1];
        let deviceNum = topics[2];
        message = JSON.parse(message.toString());
        if (!message) {
            return;
        }
        if (topics[3] == 'status') {
            console.log('接收到【设备状态】主题：', topic);
            console.log('接收到【设备状态】内容：', message);
            // 更新列表中设备的状态
            for (let i = 0; i < deviceList.value.length; i++) {
                if (deviceList.value[i].serialNumber == deviceNum) {
                    deviceList.value[i].status = message.status;
                    deviceList.value[i].isShadow = message.isShadow;
                    deviceList.value[i].rssi = message.rssi;
                    return;
                }
            }
        }
    });
}
function openSummaryDialog(row) {
    let json = {
        type: 1, // 1=扫码关联设备
        deviceNumber: row.serialNumber,
        productId: row.productId,
        productName: row.productName,
    };
    qrText.value = JSON.stringify(json);
    openSummary.value = true;
}
/* 订阅消息 */
function mqttSubscribe(list) {
    // 订阅当前页面设备状态和实时监测
    let topics = [];
    for (let i = 0; i < list.length; i++) {
        let topicStatus = '/' + '+' + '/' + list[i].serialNumber + '/status/post';
        topics.push(topicStatus);
    }
    mqttTool.subscribe(topics);
}
/** 查询设备分组列表 */
function getGroupList() {
    loading.value = true;
    let queryParams = {
        pageSize: 30,
        pageNum: 1,
        userId: userStore.userId,
    };
    listGroup(queryParams).then((response) => {
        myGroupList.value = response.rows;
    });
}
/** 查询所有简短设备列表 */
function getList() {
    loading.value = true;
    queryParams.value.params = {};
    getGroupList();
    listDeviceShort(queryParams.value).then((response) => {
        deviceList.value = response.rows;
        total.value = response.total - 1;
        // 订阅消息
        if (deviceList.value && deviceList.value.length > 0) {
            mqttSubscribe(deviceList.value);
        }
        deviceList.value = deviceList.value.filter((device) => device.deviceId !== 118);
        loading.value = false;
    });
}
/** 搜索按钮操作 */
function handleQuery() {
    queryParams.value.pageNum = 1;
    getList();
}
/** 重置按钮操作 */
function resetQuery() {
    queryParams.value.productId = null;
    queryParams.value.groupId = null;
    queryParams.value.serialNumber = null;
    proxy.resetForm('queryRef');
    handleQuery();
}
/** 切换显示方式 */
function handleChangeShowType() {
    showType.value = showType.value == 'card' ? 'list' : 'card';
}
/** 修改按钮操作 */
function handleEditDevice(row, activeName) {
    let deviceId = 0;
    let isSubDev = 0;
    if (row != 0) {
        deviceId = row.deviceId || this.ids;
        isSubDev = row.subDeviceCount > 0 ? 1 : 0;
    }
    proxy.$router.push({
        path: '/iot/device-edit',
        query: {
            deviceId: deviceId,
            isSubDev: isSubDev,
            pageNum: queryParams.value.pageNum,
            activeName: activeName,
        },
    });
}
/** 运行状态按钮操作 */
function handleRunDevice(row) {
    let deviceId = 0;
    let isSubDev = 0;
    if (row != 0) {
        deviceId = row.deviceId || ids.value;
        isSubDev = row.subDeviceCount > 0 ? 1 : 0;
    }
    if (row.deviceType === 3) {
        proxy.$router.push({
            path: '/iot/device-edit',
            query: {
                deviceId: deviceId,
                isSubDev: isSubDev,
                pageNum: queryParams.value.pageNum,
                activeName: 'videoLive',
            },
        });
    } else {
        proxy.$router.push({
            path: '/iot/device-edit',
            query: {
                deviceId: deviceId,
                isSubDev: isSubDev,
                pageNum: queryParams.value.pageNum,
                activeName: 'runningStatus',
            },
        });
    }
}
/** 删除按钮操作 */
function handleDelete(row) {
    const deviceIds = row.deviceId || this.ids;
    proxy.$modal.confirm('是否确认删除设备编号为"' + deviceIds + '"的数据项？').then(function () {
            if (row.deviceType === 3) {
                delSipDeviceBySipId(row.serialNumber);
            }
            return delDevice(deviceIds);
        })
        .then(() => {
            this.getList();
            proxy.$modal.msgSuccess('删除成功');
        })
        .catch(() => {});
}
/** 未启用设备影子*/
function shadowUnEnable(device, thingsModel) {
    // 1-未激活，2-禁用，3-在线，4-离线
    if (device.status != 3 && device.isShadow == 0) {
        return true;
    }
    if (thingsModel.isReadonly) {
        return true;
    }
    return false;
}

// 产品筛选
let productId = proxy.$route.query.productId;
if (productId != null) {
    queryParams.value.productId = Number(productId);
    queryParams.value.groupId = null;
    queryParams.value.serialNumber = null;
}
// 分组筛选
let groupId = proxy.$route.query.groupId;
if (groupId != null) {
    queryParams.value.groupId = Number(groupId);
    queryParams.value.productId = null;
    queryParams.value.serialNumber = null;
}
// 设备编号筛选
let sn = proxy.$route.query.sn;
if (sn != null) {
    queryParams.value.serialNumber = sn;
    queryParams.value.productId = null;
    queryParams.value.groupId = null;
}


// 组件挂载时连接
onMounted(() => {
    connectMqtt();
});

// 组件卸载时断开
onUnmounted(() => {
    mqttTool.end();
});
</script>

<style scoped>
.card-item {
    border-radius: 15px;
}
</style>
