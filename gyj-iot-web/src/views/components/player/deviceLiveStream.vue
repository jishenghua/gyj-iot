<template>
    <div>
        <el-row :gutter="20" style="margin-bottom: 16px">
            <el-col :span="1.5">
                <el-select v-model="channelId" placeholder="请选择通道" @change="changeChannel()">
                    <el-option v-for="option in channelList" :key="option.value" :label="option.label" :value="option.value"></el-option>
                </el-select>
            </el-col>
            <el-col :span="1.5" style="line-height: 36px">
                <span style="font-size: 14px">开启拉流：</span>
                <el-switch v-model="pushStream" :disabled="channelId === ''" @change="startPushStream"></el-switch>
            </el-col>
        </el-row>

        <player ref="playerRef" :playerinfo="playinfo"></player>
    </div>
</template>
<script setup name="DeviceLiveStream">
import player from '@/views/components/player/player.vue';
import { startPlay, closeStream, listChannel } from '@/api/iot/channel';

const { proxy } = getCurrentInstance();

// 遮罩层
const loading = ref(true);
const deviceInfo = ref({});
const deviceId = ref('');
const channelId = ref('');
const streamId = ref('');
const ssrc = ref('');
const playurl = ref('');
const playinfo = ref({ playtype: 'play' });
const playing = ref(false);
const pushStream = ref(false);
const retrycount = ref(0);
const channelList = ref([]);

const data = reactive({
    // 查询参数
    queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceSipId: null,
        channelSipId: null,
    }
})

const { queryParams } = toRefs(data);

const props = defineProps({
    device: {
        type: Object,
        default: null,
    }
});

// 获取到父组件传递的device后
watch(() => props.device, (newVal, oldVal) => {
    deviceInfo.value = newVal;
    if (deviceInfo.value.channelId) {
        channelId.value = deviceInfo.value.channelId;
        changeChannel();
    }
    if (deviceInfo.value && deviceInfo.value.deviceId !== 0) {
        queryParams.value.deviceSipId = deviceInfo.value.serialNumber;
        deviceId.value = device.value.serialNumber;
    }
})

onBeforeUnmount(() => {
    console.log('beforeDestroy');
    closeDestroy(false);
})

/** 查询监控设备通道信息列表 */
function getList() {
    loading.value = true;
    listChannel(queryParams.value).then((response) => {
        channelList.value = response.rows.map((item) => {
            return { value: item.channelSipId, label: item.channelName };
        });
        console.log(channelList.value);
    });
}
function changeChannel() {
    playinfo.value.channelId = channelId.value;
    startPlayer();
}
// 直播播放
function TimeoutCallback() {
    closeDestroy(false);
    retrycount.value = 0;
    setTimeout(() => {
        startPlayer();
    }, 1000);
}
function startPushStream() {
    if (!channelId.value) {
        console.log('开始通道: [' + channelId.value + ']');
        return;
    }
    console.log('推流状态: [' + pushStream.value + ']');
    if (pushStream.value) {
        startPlayer();
    } else {
        closeDestroy(true);
    }
}
// 开启直播播放器
function startPlayer() {
    if (!channelId.value) {
        console.log('直播录像通道: [' + channelId.value + ']');
        return;
    }
    deviceId.value = queryParams.value.deviceSipId;
    if (playing.value) {
        closeDestroy(false);
    }
    proxy.$refs['playerRef'].registercallback('loadingTimeout', TimeoutCallback);
    proxy.$refs['playerRef'].registercallback('delayTimeout', TimeoutCallback);
    startPlay(deviceId.value, channelId.value).then((response) => {
        console.log('开始推流: [' + channelId.value + ']');
        const res = response.data;
        streamId.value = res.streamId;
        playurl.value = res.playurl;
        proxy.$refs['playerRef'].play(playurl.value);
        playing.value = true;
        pushStream.value = true;
    });
}
function closeStreamInfo(force) {
    if (force) {
        if (playing.value && streamId.value) {
            console.log('强制关闭推流: [' + streamId.value + ']');
            closeStream(deviceId.value, channelId.value, streamId.value).then((res) => {
                streamId.value = '';
                ssrc.value = '';
                playurl.value = '';
                pushStream.value = false;
            });
            playing.value = false;
        }
    } else {
        if (playing.value && streamId.value) {
            console.log('关闭推流: [' + streamId.value + ']');
            closeStream(deviceId.value, channelId.value, streamId.value).then((res) => {
                streamId.value = '';
                ssrc.value = '';
                playurl.value = '';
                pushStream.value = false;
            });
            playing.value = false;
        }
    }
}
function closeDestroy(force) {
    closeStreamInfo(force);
    proxy.$refs['playerRef'].destroy();
}
function destroy() {
    proxy.$refs['playerRef'].destroy();
}

queryParams.value.deviceSipId = proxy.device.serialNumber;
deviceId.value = proxy.device.serialNumber;
getList();
playinfo.value = {
    playtype: 'play',
    deviceId: proxy.device.serialNumber,
}
</script>
