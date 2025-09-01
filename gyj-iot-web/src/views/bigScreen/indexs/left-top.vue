<template>
<div style="display:flex;margin-top:60px;">
    <div style="margin-left:10px;color:#32c5e9;">
        <Decoration9 style="width:90px;height:90px;font-weight:600;color:#32c5e9;" :dur="10" :color="['#32c5e9', '#238aa4']">{{deviceStatistic.deviceCount}}</Decoration9>
        <div style="text-align: center;margin-top:25px;">设备总数</div>
    </div>
    <div style="margin:0 30px;color:#9fe6b8">
        <Decoration9 style="width:90px;height:90px;font-weight:600;" :dur="10" :color="['#9fe6b8', '#70a181']">{{deviceStatistic.deviceOnlineCount}}</Decoration9>
        <div style="text-align: center;margin-top:25px;font-weight:400;">在线设备</div>
    </div>
    <div style="margin-right:30px;color:#ffdb5c">
        <Decoration9 style="width:90px;height:90px;font-weight:600;" :dur="10" :color="['#ffdb5c', '#b39a41']">{{deviceStatistic.deviceCount-deviceStatistic.deviceOnlineCount}}</Decoration9>
        <div style="text-align: center;margin-top:25px;font-weight:400;">离线设备</div>
    </div>
    <div style="color:#fb7293;">
        <Decoration9 style="width:90px;height:90px;font-weight:600;" :dur="10" :color="['#fb7293', '#ad4f65']">{{deviceStatistic.alertCount}}</Decoration9>
        <div style="text-align: center;margin-top:25px;font-weight:400;">告警数量</div>
    </div>
</div>
</template>

<script setup name="left-top">
import {
    getDeviceStatistic,
} from "@/api/iot/device";

const timer = ref(null);

const data = reactive({
    // 设备统计信息
    deviceStatistic: {
        deviceCount: 100,
        productCount: 100,
        eventCount: 100,
        alertCount: 100,
        deviceOnlineCount:100
    }
})

const { deviceStatistic } = toRefs(data);

onBeforeUnmount(() => {
    clearData();
})

function clearData() {
    if (timer.value) {
        clearInterval(timer.value)
        timer.value = null
    }
}
function getDeviceStatic() {
    getDeviceStatistic().then(response => {
        if (response.code == 200) {
            deviceStatistic.value = response.data;
            // 轮询
            switper()
        }
    });
}
//轮询
function switper() {
    if (timer.value) {
        return
    }
    let looper = (a) => {
        getDeviceStatic();
    };
    timer.value= setInterval(looper, 60000);
}

getDeviceStatic()
</script>
