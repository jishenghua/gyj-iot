<template>
<div v-if="pageflag" style="overflow:hidden;width: 100%;height:250px;font-size:12px;line-height:24px;">
<!--    <vue3-seamless-scroll :list="deviceLogList" :class-option="defaultOption">-->
        <div style="display:flex;margin-bottom:10px;border-bottom:2px dashed #23cdd8;color:#bbb;" v-for="(item, i) in deviceLogList" :key="i">
            <span style="width:50px;color:#23cdd8;font-size:16px;line-height:48px;font-weight:bolder;">{{ i + 1 }}</span>
            <div style="display:flex;flex-wrap: wrap;">
                <div style="width:200px;">
                    <span>设备编号：</span>
                    <span style="color:#23cdd8">{{ item.serialNumber }}</span>
                </div>
                <div style="width:150px;white-space: nowrap;overflow:hidden;text-overflow:ellipsis;">
                    <span>标识符：</span>
                    <span style="color:#fff;">{{item.identity}}</span>
                </div>
                <div style="width:60px;align-items: flex-end;text-align: right;">
                    <span v-if="item.logType==1" style="color:#ffdb5c">属性上报</span>
                    <span v-else-if="item.logType==2" style="color:#ffdb5c">功能调用</span>
                    <span v-else-if="item.logType==3" style="color:#fb7293">事件上报</span>
                    <span v-else-if="item.logType==4" style="color:#ffdb5c">设备升级</span>
                    <span v-else-if="item.logType==5" style="color:#9fe6b8">设备上线</span>
                    <span v-else-if="item.logType==6" style="color:#ff9f7f">设备离线</span>
                </div>
                <div style="width:200px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
                  <span>上报时间：</span>
                  <span style="color:#fff;">{{ item.createTime }}</span>
                </div>
                <div style="margin-bottom:10px;width:210px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
                  <span>上报值：</span>
                  <span style="color:#fff;">{{item.logValue }}</span>
                </div>
            </div>
        </div>
<!--    </vue3-seamless-scroll>-->
</div>

<Reacquire v-else @onclick="getData" style="line-height:200px;color:#23cdd8;margin-left:210px;">
    重新获取
</Reacquire>
</template>

<script setup name="left-bottom">
import vue3SeamlessScroll from 'vue3-seamless-scroll'
import {
    listDeviceLog
} from "@/api/iot/deviceLog";

const deviceLogList = ref([]);
const pageflag = ref(true);
const timer = ref(null);

const data = reactive({
    defaultOption: {
        step: 2,
        limitMoveNum: 4,
        hoverStop: true,
        singleHeight: 280,
        openWatch: true,
        waitTime: 3000,
    },
    queryParams: {
        pageNum: 1,
        pageSize: 20,
    }
})

const { defaultOption, queryParams } = toRefs(data);

onBeforeUnmount(() => {
    clearData()
})

function getData() {
    listDeviceLog(queryParams.value).then(response => {
        deviceLogList.value = response.rows;
        // 轮询
        switper()
    });
}
function clearData() {
    if (timer.value) {
        clearInterval(timer.value)
        timer.value = null
    }
}
//轮询
function switper() {
    if (timer.value) {
        return
    }
    let looper = (a) => {
        getData()
    };
    timer.value = setInterval(looper, 60000);
}

getData();
</script>
