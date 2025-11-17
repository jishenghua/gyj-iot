<template>
    <el-card style="margin: 6px; padding-bottom: 100px">
        <el-tabs v-model="activeName" tab-position="left" @tab-click="tabChange" style="padding: 10px; min-height: 400px">
            <el-tab-pane name="basic" label="">
                <template #label><span style="color:red; padding-right:4px">* </span>基本信息</template>
                <el-form ref="deviceRef" :model="form" :rules="rules" label-width="100px">
                    <el-row :gutter="100">
                        <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="8">
                            <el-form-item label="设备名称" prop="deviceName">
                                <el-input v-model="form.deviceName" placeholder="请输入设备名称">
                                    <el-button slot="append" @click="openSummaryDialog" v-if="form.deviceId != 0">摘要</el-button>
                                </el-input>
                            </el-form-item>
                            <el-form-item label="" prop="productName">
                                <template #label>
                                    <span style="color: red; padding-right: 4px;">*</span>
                                    所属产品
                                </template>
                                <el-input readonly v-model="form.productName" placeholder="请选择产品" :disabled="form.status != 1">
                                    <template #append>
                                        <el-button @click="selectProduct()" :disabled="form.status != 1">选择</el-button>
                                    </template>
                                </el-input>
                            </el-form-item>
                            <el-form-item label="" prop="serialNumber">
                                <template #label>
                                    <span style="color: red; padding-right: 4px;">*</span>
                                    设备编号
                                </template>
                                <el-input v-model="form.serialNumber" placeholder="请输入设备编号" :disabled="form.status !== 1" maxlength="32">
                                    <template #append>
                                        <el-button v-if="form.deviceType !== 3" @click="generateNum" :loading="genDisabled" :disabled="form.status != 1" v-hasPermi="['iot:device:add']">生成</el-button>
                                        <el-button v-if="form.deviceType === 3" @click="genSipID()" :disabled="form.status != 1" v-hasPermi="['iot:device:add']">生成</el-button>
                                    </template>
                                </el-input>
                            </el-form-item>
                            <el-form-item v-if="openServerTip">
                                <template>
                                    <el-alert type="info" show-icon description="当前选择TCP协议,设备编号生成为HEX格式"></el-alert>
                                </template>
                            </el-form-item>
                            <el-form-item v-if="openTip">
                                <template>
                                    <el-alert type="success" show-icon description="当前选择的产品属于modbus协议,将在网关设备创建后根据采集点模板生成子设备"></el-alert>
                                </template>
                            </el-form-item>
                            <el-form-item label="固件版本" prop="firmwareVersion">
                                <el-input v-model="form.firmwareVersion" placeholder="请输入固件版本" type="number" step="0.1" :disabled="form.status != 1 || form.deviceType === 3">
                                    <template #prepend>Version</template>
                                </el-input>
                            </el-form-item>
                            <el-form-item label="设备影子" prop="isShadow">
                                <el-switch v-model="form.isShadow" active-text="" inactive-text="" :active-value="1" :inactive-value="0" :disabled="form.deviceType === 3"></el-switch>
                            </el-form-item>
                            <el-form-item label="禁用设备" prop="deviceStatus">
                                <el-switch
                                    v-model="deviceStatus"
                                    active-text=""
                                    inactive-text=""
                                    :disabled="form.status === 1 || form.deviceType === 3"
                                    :active-value="1"
                                    :inactive-value="0"
                                    active-color="#F56C6C"
                                ></el-switch>
                            </el-form-item>
                            <el-form-item label="备注信息" prop="remark">
                                <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" rows="3" />
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="8">
                            <el-form-item label="定位方式" prop="locationWay">
                                <el-select v-model="form.locationWay" placeholder="请选择设备状态" clearable style="width: 100%" :disabled="form.deviceType === 3">
                                    <el-option v-for="dict in iot_location_way" :key="dict.value" :label="dict.label" :value="Number(dict.value)" />
                                </el-select>
                            </el-form-item>
                            <el-form-item label="设备经度" prop="longitude">
                                <el-input v-model="form.longitude" placeholder="请输入设备经度" type="number" :disabled="form.locationWay != 3">
                                    <template #append>
                                        <el-link :underline="false" href="https://lbs.amap.com/tools/picker" target="_blank" :disabled="form.locationWay != 3">坐标拾取</el-link>
                                    </template>
                                </el-input>
                            </el-form-item>
                            <el-form-item label="设备纬度" prop="latitude">
                                <el-input v-model="form.latitude" placeholder="请输入设备纬度" type="number" :disabled="form.locationWay != 3">
                                    <template #append>
                                        <el-link :underline="false" href="https://lbs.amap.com/tools/picker" target="_blank" :disabled="form.locationWay != 3">坐标拾取</el-link>
                                    </template>
                                </el-input>
                            </el-form-item>
                            <el-form-item label="所在地址" prop="networkAddress">
                                <el-input v-model="form.networkAddress" placeholder="请输入设备所在地址" :disabled="form.locationWay != 3" />
                            </el-form-item>
                            <el-form-item label="入网地址" prop="networkIp">
                                <el-input v-model="form.networkIp" placeholder="设备入网IP" disabled />
                            </el-form-item>
                            <el-form-item label="激活时间" prop="activeTime">
                                <el-date-picker clearable v-model="form.activeTime" type="date" value-format="yyyy-MM-dd" placeholder="设备激活时间" disabled style="width: 100%"></el-date-picker>
                            </el-form-item>
                            <el-form-item label="设备信号" prop="rssi">
                                <el-input v-model="form.rssi" placeholder="设备信号强度" disabled />
                            </el-form-item>
                            <el-form-item label="其他信息" prop="remark" v-if="form.deviceId != 0">
                                <dict-tag :options="iot_device_status" :value="form.status" style="display: inline-block; margin-right: 8px" />
                                <el-button size="small" @click="handleViewMqtt()">认证信息</el-button>
                                <el-button size="small" @click="openCodeDialog()">二维码</el-button>
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :sm="24" :md="24" :lg="12" :xl="8" v-if="form.deviceId != 0">
                            <div style="border: 1px solid #dfe4ed; border-radius: 5px; padding: 5px; text-align: center; line-height: 400px">
                                <div id="map" style="height: 435px; width: 100%">地图展示区域，新增后显示</div>
                            </div>
                        </el-col>
                    </el-row>
                </el-form>

                <el-form label-width="100px" style="margin-top: 50px">
                    <el-form-item style="text-align: center; margin-left: 450px; margin-top: 10px">
                        <el-button type="primary" @click="submitForm" v-hasPermi="['iot:device:edit']" v-show="form.deviceId != 0">修 改</el-button>
                        <el-button type="primary" @click="submitForm" v-hasPermi="['iot:device:add']" v-show="form.deviceId == 0">新 增</el-button>
                    </el-form-item>
                </el-form>

                <!-- 选择产品 -->
                <product-list ref="productListRef" :productId="form.productId" @productEvent="getProductData($event)" />

                <sipid ref="sipidGenRef" :product="form" @addGenEvent="getSipIDData($event)" />
            </el-tab-pane>

            <el-tab-pane name="runningStatus" label="运行状态" key="1" v-if="form.deviceType !== 3 && form.deviceId !=0  && !isSubDev">
                <running-status ref="runningStatusRef" :device="form" @statusEvent="getDeviceStatusData($event)"/>
              </el-tab-pane>
            <el-tab-pane name="videoLive" label="设备直播" key="2" v-if="form.deviceType === 3 && form.deviceId !=0 ">
                <device-live-stream ref="deviceLiveStreamRef" :device="form" />
            </el-tab-pane>

            <el-tab-pane name="deviceTimer" label="设备定时" key="3" v-if="form.deviceType !== 3 && form.deviceId !=0  && hasShrarePerm('timer')">
                <device-timer ref="deviceTimerRef" :device="form" />
            </el-tab-pane>

            <el-tab-pane name="deviceUser" label="设备用户" key="4"  v-if="form.deviceId !=0">
                <device-user ref="deviceUserRef" :device="form" @userEvent="getUserData($event)" />
            </el-tab-pane>

            <el-tab-pane name="deviceLog" label="事件日志" key="5"  v-if="form.deviceType !== 3 && form.deviceId !=0 && hasShrarePerm('log')" lazy>
                <device-log ref="deviceLogRef" :device="form" />
            </el-tab-pane>

            <el-tab-pane name="deviceFuncLog" label="指令日志" key="6" v-if="form.deviceType !== 3 && form.deviceId !=0 && hasShrarePerm('log')" lazy>
                <device-func ref="deviceFuncLogRef" :device="form" />
            </el-tab-pane>

            <!-- 用于设置间距 -->
            <el-tab-pane v-for="(item, index) in 3" disabled>
                <template #label>
                    <div style="margin-top:100px;"></div>
                </template>
            </el-tab-pane>

            <el-tab-pane name="deviceReturn" disabled>
                <template #label>
                    <el-button type="info" size="small" @click="goBack()">返回列表</el-button>
                </template>
            </el-tab-pane>
        </el-tabs>

        <!-- 设备配置JSON -->
        <el-dialog title="摘要（设备上传的只读数据）" v-model="openSummary" width="700px" append-to-body>
            <el-row :gutter="20">
                <el-col :span="14">
                    <div style="border: 1px solid #ccc; margin-top: -15px; height: 350px; width: 360px; overflow: scroll">
                        <json-viewer :value="summary" :expand-depth="10" copyable>
                            <template v-slot:copy>复制</template>
                        </json-viewer>
                    </div>
                </el-col>
                <el-col :span="10">
                    <div style="border: 1px solid #ccc; width: 200px; text-align: center; margin-left: 20px; margin-top: -10px">
                        <vue-qr :text="qrText" :size="200"></vue-qr>
                        <div style="padding-bottom: 10px">设备二维码</div>
                    </div>
                </el-col>
            </el-row>
            <div slot="footer" class="dialog-footer">
                <el-button type="info" @click="closeSummaryDialog">关 闭</el-button>
            </div>
        </el-dialog>
        <!-- 二维码 -->
        <el-dialog v-model="openCode" width="300px" append-to-body>
            <div style="border: 1px solid #ccc; width: 220px; text-align: center; margin: 0 auto; margin-top: -15px">
                <vue-qr :text="qrText" :size="200"></vue-qr>
                <div style="padding-bottom: 10px">设备二维码</div>
            </div>
        </el-dialog>
        <el-dialog title="Mqtt连接参数" v-model="openViewMqtt" width="600px" :show-close="true" append-to-body :close-on-click-modal="false">
            <el-form ref="listQueryRef" :model="listQuery" :rules="rules" label-width="150px">
                <el-form-item label="客户端ID" prop="clientId">
                    <el-input v-model="listQuery.clientId" readonly style="width: 80%" />
                </el-form-item>
                <el-form-item label="MQ账号" prop="username">
                    <el-input v-model="listQuery.username" readonly style="width: 80%" />
                </el-form-item>
                <el-form-item label="MQ密码" prop="passwd">
                    <el-input clearable v-model="listQuery.passwd" readonly style="width: 80%"></el-input>
                </el-form-item>
                <el-form-item label="订阅主题" prop="subscribeTopic">
                    <el-input clearable v-model="listQuery.subscribeTopic" readonly style="width: 80%"></el-input>
                </el-form-item>
                <el-form-item label="发布主题" prop="reportTopic">
                    <el-input clearable v-model="listQuery.reportTopic" readonly style="width: 80%"></el-input>
                </el-form-item>
                <el-form-item label="连接端口" prop="port">
                    <el-input clearable v-model="listQuery.port" readonly style="width: 80%"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button class="btns" type="primary" @click="doCopy(2)">一键复制</el-button>
                    <el-button @click="closeSummaryDialog">关 闭</el-button>
                </div>
            </template>
        </el-dialog>
    </el-card>
</template>

<script setup name="DeviceEdit">
import JsonViewer from 'vue-json-viewer';
import 'vue-json-viewer/style.css';
import productList from './product-list';
import deviceLog from './device-log';
import deviceUser from './device-user';
import runningStatus from './running-status';

import deviceTimer from './device-timer';
import DeviceFunc from './device-functionlog';
import vueQr from 'vue-qr/src/packages/vue-qr.vue'
import { deviceSynchronization, getDevice, addDevice, updateDevice, generatorDeviceNum, getMqttConnect } from '@/api/iot/device';
import { getDeviceRunningStatus } from '@/api/iot/device';
import { cacheJsonThingsModel } from '@/api/iot/model';
import deviceLiveStream from '@/views/components/player/deviceLiveStream';
import sipid from '../sip/sipidGen.vue';
import player from '@/views/components/player/player.vue';
import channel from '../sip/channel';
import mqttTool from '@/utils/mqttTool'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

const { proxy } = getCurrentInstance();
const { iot_device_status, iot_location_way } = proxy.useDict('iot_device_status', 'iot_location_way');

// 二维码内容
const qrText = ref('gyjiot');
// 打开设备配置对话框
const openSummary = ref(false);
//二维码
const openCode = ref(false);
const openViewMqtt = ref(false);
// 生成设备编码是否禁用
const genDisabled = ref(false);
// 选中选项卡
const activeName = ref('basic');
//查看mqtt参数
const mqttList = ref([]);
// 遮罩层
const loading = ref(true);
// 设备开始状态
const oldDeviceStatus = ref(null);
const deviceId = ref('');
const channelId = ref('');
const openTip = ref(false);
const openServerTip = ref(false);
const serverType = ref(1);
//用于判断是否是设备组(modbus)
const isSubDev = ref(false);
// 设备摘要
const summary = ref([]);
// 地址
const baseUrl = ref(import.meta.env.VITE_APP_BASE_API);
// 地图相关
const map = ref(null);
const mk = ref(null);
const latitude = ref('');
const longitude = ref('');
const isMediaDevice = ref(false);

const data = reactive({
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
    //mqtt参数查看
    listQuery: {
        clientId: 0,
        username: '',
        passwd: '',
        subscribeTopic: '',
        reportTopic: '',
        port: '',
    },
    // 表单校验
    rules: {
        deviceName: [
            {
                required: true,
                message: '设备名称不能为空',
                trigger: 'blur',
            },
            {
                min: 2,
                max: 32,
                message: '设备名称长度在 2 到 32 个字符',
                trigger: 'blur',
            },
        ],
        firmwareVersion: [
            {
                required: true,
                message: '固件版本不能为空',
                trigger: 'blur',
            },
        ],
    }
})

const { form, listQuery, rules } = toRefs(data);

const deviceStatus = computed({
    set(val) {
        if (val == 1) {
            // 1-未激活，2-禁用，3-在线，4-离线
            form.value.status = 2;
        } else if (val == 0) {
            form.value.status = 4;
        } else {
            form.value.status = oldDeviceStatus.value;
        }
    },
    get() {
        if (form.value.status == 2) {
            return 1;
        }
        return 0;
    }
})

/* 连接Mqtt消息服务器 */
async function connectMqtt() {
    if (mqttTool.client == null) {
        await mqttTool.connect(proxy.vuex_token);
    }
    mqttCallback();
}
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
        if (topics[3] == 'status' || topics[2] == 'status') {
            console.log('接收到【设备状态-详情】主题：', topic);
            console.log('接收到【设备状态-详情】内容：', message);
            // 更新列表中设备的状态
            if (form.value.serialNumber == deviceNum) {
                oldDeviceStatus.value = message.status;
                form.value.status = message.status;
                form.value.isShadow = message.isShadow;
                form.value.rssid = message.rssid;
            }
        }
        //不是modbus不转发到子页面，其他设备的页面有回调方法
        if (isSubDev.value) {
            /*发送设备上报到子模块*/
            if (topic.endsWith('ws/service')) {
                proxy.$busEvent.$emit('updateData', {
                    serialNumber: topics[2],
                    productId: form.value.productId,
                    data: message,
                });
            }
        }
        /*发送设备上报到子模块*/
        if (topic.endsWith('ws/post/simulate')) {
            proxy.$busEvent.$emit('logData', {
                serialNumber: topics[1],
                productId: form.value.productId,
                data: message,
            });
        }
    });
}
/** Mqtt订阅主题 */
function mqttSubscribe(device) {
    // 订阅当前设备状态和实时监测
    let topicStatus = '/' + device.productId + '/' + device.serialNumber + '/status/post';
    let topicProperty = '/' + device.productId + '/' + device.serialNumber + '/property/post';
    let topicFunction = '/' + device.productId + '/' + device.serialNumber + '/function/post';
    let topicMonitor = '/' + device.productId + '/' + device.serialNumber + '/monitor/post';
    let topicReply = '/' + device.productId + '/' + device.serialNumber + '/service/reply';
    let topics = [];
    let serviceTop = '/' + device.productId + '/' + device.serialNumber + '/ws/service';
    topics.push(serviceTop);

    topics.push(topicStatus);
    topics.push(topicFunction);
    topics.push(topicMonitor);
    topics.push(topicReply);
    /*modbus设备不订阅此topic*/
    if (!isSubDev.value) {
        // topics.push(topicProperty);
    }
    mqttTool.subscribe(topics);
}
/** Mqtt取消订阅主题 */
function mqttUnSubscribe(device) {
    // 订阅当前设备状态和实时监测
    let topicStatus = '/' + device.productId + '/' + device.serialNumber + '/status/post';
    let topicProperty = '/' + device.productId + '/' + device.serialNumber + '/property/post';
    let topicFunction = '/' + device.productId + '/' + device.serialNumber + '/function/post';
    let topicMonitor = '/' + device.productId + '/' + device.serialNumber + '/monitor/post';
    let topicReply = '/' + device.productId + '/' + device.serialNumber + '/service/reply';
    let topics = [];
    let serviceTop = '/' + device.productId + '/' + device.serialNumber + '/ws/service';
    topics.push(serviceTop);

    topics.push(topicStatus);
    topics.push(topicFunction);
    topics.push(topicMonitor);
    topics.push(topicReply);
    /*modbus设备不订阅此topic*/
    if (!isSubDev.value) {
        /*通过网关再转发*/
        // topics.push(topicProperty);
    }
    mqttTool.unsubscribe(topics);
}

//获取子组件订阅的设备状态
function getDeviceStatusData(status) {
  form.value.status = status;
}
function getPlayerData(data) {
    activeName.value = data.tabName;
    channelId.value = data.channelId;
    if (channelId.value) {
        proxy.$refs['deviceLiveStreamRef'].channelId = channelId.value;
        proxy.$refs['deviceLiveStreamRef'].changeChannel();
    }
}
/** 选项卡改变事件*/
function tabChange(panel) {
    proxy.$nextTick(() => {
        if (form.value.deviceType == 3 && panel.name != 'deviceReturn') {
            if (panel.name === 'sipPlayer') {
                if (proxy.$refs.deviceVideo && proxy.$refs.deviceVideo.destroy) {
                    proxy.$refs.deviceVideo.destroy();
                }
                if (channelId.value) {
                    if (proxy.$refs['deviceLiveStreamRef'] && proxy.$refs['deviceLiveStreamRef'].channelId !== undefined) {
                        proxy.$refs['deviceLiveStreamRef'].channelId = channelId.value;
                    }
                    proxy.$refs['deviceLiveStreamRef'].changeChannel();
                }
                if (proxy.$refs['deviceLiveStreamRef'].channelId !== undefined) {
                    proxy.$refs['deviceLiveStreamRef'].changeChannel();
                }
            } else if (panel.name === 'sipVideo') {
                if (proxy.$refs['deviceLiveStreamRef'] && proxy.$refs['deviceLiveStreamRef'].destroy) {
                    proxy.$refs['deviceLiveStreamRef'].destroy();
                }
                if (proxy.$refs.deviceVideo && proxy.$refs.deviceVideo.channelId !== undefined && proxy.$refs.deviceVideo.queryDate) {
                    proxy.$refs.deviceVideo.loadDevRecord();
                }
            } else if (panel.name === 'sipChannel') {
                proxy.$nextTick(() => {
                    proxy.$refs.Channel.getList();
                });
            }
            //关闭直播流
            if (panel.name !== 'sipPlayer' && proxy.$refs['deviceLiveStreamRef'] && proxy.$refs['deviceLiveStreamRef'].playing) {
                proxy.$refs['deviceLiveStreamRef'].closeDestroy(false);
            }
            //关闭录像流
            if (panel.name !== 'sipVideo' && proxy.$refs['deviceVideo'] && proxy.$refs['deviceVideo'].playing) {
                proxy.$refs['deviceVideo'].closeDestroy();
            }
        }
    });

    proxy.$nextTick(() => {
        // 获取监测统计数据
        if (panel.name === 'deviceStastic') {
            proxy.$refs['deviceStatistic'].getListHistory();
        } else if (panel.name === 'deviceTimer') {
            proxy.$refs['deviceTimer'].getList();
        } else if (panel.name === 'deviceSub') {
            if (form.value.serialNumber) {
                proxy.$refs['deviceSub'].queryParams.gwDeviceId = form.value.deviceId;
                proxy.$refs['deviceSub'].gateway.gwDeviceId = form.value.deviceId;
                proxy.$refs['deviceSub'].getList();
            }
        }
    });
    if (form.value.deviceType !== 3) {
        // 用于关闭视频推流（页面切换时候需要关闭推流）
        if (panel.name !== 'inlineVideo') {
            proxy.$refs['deviceInlineVideo'] && proxy.$refs['deviceInlineVideo'].handleClose();
        }
        if (panel.name !== 'scada') {
            const scadaRef = proxy.$refs['deviceScada'] || {};
            if (scadaRef && scadaRef.$refs && scadaRef.$refs.deviceScada) {
                const copmRef = scadaRef.$refs.deviceScada;
                if (copmRef.$refs && copmRef.$refs.spirit) {
                    copmRef.$refs.spirit.forEach((item) => {
                        if (item.$vnode.tag.includes('ViewInlineVideo')) {
                            item.handleCloseJessibuca();
                        }
                    });
                }
            }
        }
    }
}
/** 数据同步*/
function deviceSync() {
    deviceSynchronization(form.value.serialNumber).then(async (response) => {
        // 获取缓存物模型
        response.data.cacheThingsModel = await getCacheThingsModdel(response.data.productId);
        // 获取设备运行状态
        response.data.thingsModels = await getDeviceStatus(form.value);
        // 格式化物模型，拆分出监测值,数组添加前缀
        formatThingsModel(response.data);
        form.value = response.data;
        // 选项卡切换
        activeName.value = 'runningStatus';
        oldDeviceStatus.value = form.value.status;
        loadMap();
    });
}
/**获取设备详情*/
function getDeviceInfo(deviceId) {
    getDevice(deviceId).then(async (response) => {
        // 分享设备获取用户权限
        response.data.userPerms = [];
        if (response.data.isOwner == 0) {
            getDeviceUser(deviceId,userStore.userId).then((permResponse) => {
                response.data.userPerms = permResponse.data.perms.split(',');
                // 获取设备状态和物模型
                getDeviceStatusWitchThingsModel(response);
            });
        } else {
            // 获取设备状态和物模型
            getDeviceStatusWitchThingsModel(response);
        }
    });
}
/**用户是否拥有分享设备权限*/
function hasShrarePerm(permission) {
    if (form.value.isOwner == 0) {
        // 分享设备权限
        if (form.value.userPerms.indexOf(permission) == -1) {
            return false;
        }
    }
    return true;
}
/** 获取缓存物模型*/
function getCacheThingsModdel(productId) {
    return new Promise((resolve, reject) => {
        cacheJsonThingsModel(productId)
            .then((response) => {
                resolve(JSON.parse(response.data));
            })
            .catch((error) => {
                reject(error);
            });
    });
}
/**获取设备运行状态*/
function getDeviceStatus(data) {
    const params = {
        deviceId: data.deviceId,
    };
    return new Promise((resolve, reject) => {
        getDeviceRunningStatus(params)
            .then((response) => {
                resolve(response.data.thingsModels);
            })
            .catch((error) => {
                reject(error);
            });
    });
}
function formatThingsModel(data) {
    data.chartList = [];
    data.monitorList = [];
    data.staticList = [];
    // 物模型格式化
    for (let i = 0; i < data.thingsModels.length; i++) {
        // 数字类型设置默认值并转换未数值
        if (data.thingsModels[i].datatype.type == 'integer' || data.thingsModels[i].datatype.type == 'decimal') {
            if (data.thingsModels[i].shadow == '') {
                data.thingsModels[i].shadow = Number(data.thingsModels[i].datatype.min);
            } else {
                data.thingsModels[i].shadow = Number(data.thingsModels[i].shadow);
            }
        }

        // 物模型分类放置
        if (data.thingsModels[i].datatype.type == 'array') {
            if (data.thingsModels[i].datatype.arrayType == 'object') {
                for (let k = 0; k < data.thingsModels[i].datatype.arrayParams.length; k++) {
                    for (let j = 0; j < data.thingsModels[i].datatype.arrayParams[k].length; j++) {
                        // 数组元素中参数ID添加前缀，例如：array_00_
                        let index = k > 9 ? String(k) : '0' + k;
                        let prefix = 'array_' + index + '_';
                        data.thingsModels[i].datatype.arrayParams[k][j].id = prefix + data.thingsModels[i].datatype.arrayParams[k][j].id;
                        // 图表、实时监测、监测统计分类放置
                        if (data.thingsModels[i].datatype.arrayParams[k][j].isChart == 1) {
                            // 图表
                            data.thingsModels[i].datatype.arrayParams[k][j].name = '[' + data.thingsModels[i].name + (k + 1) + '] ' + data.thingsModels[i].datatype.arrayParams[k][j].name;
                            data.thingsModels[i].datatype.arrayParams[k][j].datatype.arrayType = 'object';
                            data.chartList.push(data.thingsModels[i].datatype.arrayParams[k][j]);

                            if (data.thingsModels[i].datatype.arrayParams[k][j].isMonitor == 1) {
                                // 实时监测
                                data.monitorList.push(data.thingsModels[i].datatype.arrayParams[k][j]);
                            }
                            data.thingsModels[i].datatype.arrayParams[k].splice(j--, 1);
                        }
                    }
                }
            } else {
                // 字符串拆分为物模型数组 model=id/name/type/isReadonly/value/shadow
                let values = data.thingsModels[i].value != '' ? data.thingsModels[i].value.split(',') : [];
                let shadows = data.thingsModels[i].shadow != '' ? data.thingsModels[i].shadow.split(',') : [];
                for (let j = 0; j < data.thingsModels[i].datatype.arrayCount; j++) {
                    if (!data.thingsModels[i].datatype.arrayModel) {
                        data.thingsModels[i].datatype.arrayModel = [];
                    }
                    // 数组里面的ID需要添加前缀和索引，例如：array_00_temperature
                    let index = j > 9 ? String(j) : '0' + j;
                    let prefix = 'array_' + index + '_';
                    data.thingsModels[i].datatype.arrayModel[j] = {
                        id: prefix + data.thingsModels[i].id,
                        name: data.thingsModels[i].name,
                        type: data.thingsModels[i].type,
                        isReadonly: data.thingsModels[i].isReadonly,
                        value: values[j] ? values[j] : '',
                        shadow: shadows[j] ? shadows[j] : '',
                    };
                }
            }
        } else if (data.thingsModels[i].datatype.type == 'object') {
            for (let j = 0; j < data.thingsModels[i].datatype.params.length; j++) {
                // 图表、实时监测、监测统计分类放置
                if (data.thingsModels[i].datatype.params[j].isChart == 1) {
                    // 图表
                    data.thingsModels[i].datatype.params[j].name = '[' + data.thingsModels[i].name + '] ' + data.thingsModels[i].datatype.params[j].name;
                    data.chartList.push(data.thingsModels[i].datatype.params[j]);

                    if (data.thingsModels[i].datatype.params[j].isMonitor == 1) {
                        // 实时监测
                        data.monitorList.push(data.thingsModels[i].datatype.params[j]);
                    }
                    data.thingsModels[i].datatype.params.splice(j--, 1);
                }
            }
        } else if (data.thingsModels[i].isChart == 1) {
            // // 图表、实时监测、监测统计分类放置
            data.chartList.push(data.thingsModels[i]);

            if (data.thingsModels[i].isMonitor == 1) {
                // 实时监测
                data.monitorList.push(data.thingsModels[i]);
            }
            // 使用i--解决索引变更问题
            data.thingsModels.splice(i--, 1);
        }
    }
}
/**加载地图*/
function loadMap() {
    proxy.$nextTick(() => {
      getmap();
    });
}
/** 返回按钮 */
function goBack() {
    const obj = {
        path: '/iot/device',
        query: {
            t: Date.now(),
            pageNum: proxy.$route.query.pageNum,
        },
    };
    proxy.$tab.closeOpenPage(obj);
    reset();
}
// 表单重置
function reset() {
    form.value = {
        deviceId: 0,
        deviceName: null,
        productId: null,
        productName: null,
        userId: null,
        userName: null,
        tenantId: null,
        tenantName: null,
        serialNumber: '',
        firmwareVersion: 1.0,
        status: 1,
        rssi: null,
        networkAddress: null,
        networkIp: null,
        longitude: null,
        latitude: null,
        activeTime: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null,
        remark: null,
        locationWay: 1,
        clientId: 0,
    };
    deviceStatus.value = 0;
    proxy.resetForm('deviceRef');
}
/** 提交按钮 */
async function submitForm() {
    if (form.value.serialNumber == null || form.value.serialNumber == 0) {
        proxy.$modal.alertError('设备编号不能为空');
        return;
    }
    let reg = /^[0-9a-zA-Z]+$/;
    if (!reg.test(form.value.serialNumber)) {
        proxy.$modal.alertError('设备编号只能是字母和数字');
        return;
    }
    if (form.value.productId == null || form.value.productId == 0) {
        proxy.$modal.alertError('所属产品不能为空');
        return;
    }

    proxy.$refs['deviceRef'].validate((valid) => {
        if (valid) {
            if (form.value.deviceId != 0) {
                updateDevice(form.value).then((response) => {
                    if (response.data == 0) {
                        proxy.$modal.alertError(response.msg);
                    } else {
                        proxy.$modal.alertSuccess('修改成功');
                        form.value = JSON.parse(JSON.stringify(form.value));
                        loadMap();
                    }
                });
            } else {
                addDevice(form.value).then(async (response) => {
                    // 获取设备状态
                    await getDeviceStatusWitchThingsModel(response);
                    if (form.value.deviceId == null || form.value.deviceId == 0) {
                        proxy.$modal.alertError('设备编号已经存在，添加设备失败');
                    } else {
                        if (form.value.status == 2) {
                            deviceStatus.value = 1;
                        }

                        proxy.$modal.alertSuccess('添加设备成功');
                        loadMap();
                    }
                });
            }
        }
    });
}
/** 获取设备状态和物模型 **/
async function getDeviceStatusWitchThingsModel(response) {
    // 获取缓存物模型
    response.data.cacheThingsModel = await getCacheThingsModdel(response.data.productId);
    // 获取设备运行状态
    response.data.thingsModels = await getDeviceStatus(response.data);
    //分享设备过滤没有权限的物模型
    if (response.data.isOwner == 0) {
        for (let i = 0; i < response.data.thingsModels.length; i++) {
            if (response.data.userPerms.indexOf(response.data.thingsModels[i].id) == -1) {
                response.data.thingsModels.splice(i--, 1);
            }
        }
    }
    // 格式化物模型，拆分出监测值,数组添加前缀
    formatThingsModel(response.data);
    form.value = response.data;
    // 解析设备摘要
    if (form.value.summary != null && form.value.summary != '') {
        summary.value = JSON.parse(form.value.summary);
    }
    isSubDev.value = form.value.subDeviceList && form.value.subDeviceList.length > 0;
    oldDeviceStatus.value = form.value.status;
    loadMap();
    //Mqtt订阅
    connectMqtt();
    mqttSubscribe(form.value);
}
/**选择产品 */
function selectProduct() {
    proxy.$refs['productListRef'].show(form.value.productId);
}
function genSipID() {
    proxy.$refs['sipidGenRef'].show();
}
/**获取选中的产品 */
function getProductData(product) {
    form.value.productId = product.productId;
    form.value.productName = product.productName;
    form.value.deviceType = product.deviceType;
    form.value.tenantId = product.tenantId;
    form.value.tenantName = product.tenantName;
    if (product.transport === 'TCP') {
        openServerTip.value = true;
        serverType.value = 3;
    } else {
        openServerTip.value = false;
        serverType.value = 1;
    }
}
function getSipIDData(devsipid) {
    form.value.serialNumber = devsipid;
}
// 获取选中的用户
function getUserData(user) {}
/**关闭物模型 */
function openSummaryDialog() {
    let json = {
        type: 1, // 1=扫码关联设备
        deviceNumber: form.value.serialNumber,
        productId: form.value.productId,
        // productName: form.value.productName,
    };
    qrText.value = JSON.stringify(json);
    openSummary.value = true;
}
/**关闭物模型 */
function closeSummaryDialog() {
    openSummary.value = false;
    openViewMqtt.value = false;
}
function doCopy(type) {
    if (type == 2) {
        const input = document.createElement('input');
        input.value =
            '{"clientId":"' +
            listQuery.value.clientId +
            '","username":"' +
            listQuery.value.username +
            '","passwd":"' +
            listQuery.value.passwd +
            '","subscribeTopic":"' +
            listQuery.value.subscribeTopic +
            '","reportTopic":"' +
            listQuery.value.reportTopic +
            '","port":"' +
            listQuery.value.port +
            '"}';
        document.body.appendChild(input);
        input.select(); //选中输入框
        document.execCommand('Copy'); //复制当前选中文本到前切板
        document.body.removeChild(input);
        proxy.$message.success('复制成功');
    }
}
function openCodeDialog() {
    let json = {
        type: 1, // 1=扫码关联设备
        deviceNumber: form.value.serialNumber,
        productId: form.value.productId,
        productName: form.value.productName,
    };
    qrText.value = JSON.stringify(json);
    openCode.value = true;
}
// 地图定位
function getmap() {
  let center = [ form.value.longitude, form.value.latitude]
    let map = new AMap.Map('map', {
      zoom: 11,
      center: center
    })
    let circleMarker = new AMap.CircleMarker({
      center:center,
      radius: 10,
      strokeColor:'white',
      strokeWeight:2,
      strokeOpacity:0.5,
      fillColor:'rgba(0,0,255,1)',
      fillOpacity:0.5,
      zIndex:10,
      bubble:true,
      cursor:'pointer',
      clickable: true
    })
    circleMarker.setMap(map)
}
// 生成随机字母和数字
function generateNum() {
    if (!form.value.productId || form.value.productId == 0) {
        proxy.$modal.alertError('请先选择产品');
        return;
    }
    genDisabled.value = true;
    const params = { type: serverType.value };
    generatorDeviceNum(params).then((response) => {
        form.value.serialNumber = response.data;
        genDisabled.value = false;
    });
}
//mqtt参数查看
function handleViewMqtt() {
    openViewMqtt.value = true;
    loading.value = true;
    const params = {
        deviceId: form.value.deviceId,
    };
    getMqttConnect(params).then((response) => {
        if (response.code == 200) {
            listQuery.value = response.data;
            loading.value = false;
        }
    });
}

onActivated(() => {
    // 跳转选项卡
    let activeNameInfo = proxy.$route.query.activeName;
    if (activeNameInfo != null && activeNameInfo != '') {
        activeName.value = activeNameInfo;
    }
})

onUnmounted(() => {
    // 取消订阅主题
    mqttUnSubscribe(form.value);
})

let activeNameInfo = proxy.$route.query.activeName;
if (activeNameInfo != null && activeNameInfo != '') {
    activeName.value = activeNameInfo;
}
// 获取设备信息
form.value.deviceId = proxy.$route.query && proxy.$route.query.deviceId;
if (form.value.deviceId != 0) {
    console.log(form.value.deviceId)
    // this.connectMqtt();
    getDeviceInfo(form.value.deviceId);
}
isSubDev.value = proxy.$route.query.isSubDev === 1;
</script>
