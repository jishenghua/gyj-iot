<template>
  <div style="display: flex">
    <div style="width: 250px; height: 250px"></div>
<!--    <dv-active-ring-chart :config="config" style="width: 250px; height: 250px" />-->
    <div style="font-size: 14px; margin-top: 80px; line-height: 10px; margin-left: -20px">
      <div style="margin-bottom: 20px; color: #23cdd8" v-if="mqtt">
        <div style="margin-bottom: 20px; color: #23cdd8">
          发送消息总数：
          <span style="color: #fff">{{ formatter(staticInfo['send_total']) }}</span>
          <Decoration3 style="width: 200px; height: 20px; margin-top: 5px" />
        </div>
      </div>
      <div style="margin-bottom: 20px; color: #23cdd8" v-else>
        发送字节：
        <span style="color: #fff">{{ formatter(staticInfo['bytes.sent']) }}</span>
        <Decoration3 style="width: 200px; height: 20px; margin-top: 5px" />
      </div>
      <div style="margin-bottom: 20px; color: #23cdd8" v-if="mqtt">
        接收消息总数：
        <span style="color: #fff">{{ formatter(staticInfo['receive_total']) }}</span>
        <Decoration3 style="width: 200px; height: 20px; margin-top: 5px" />
      </div>
      <div style="margin-bottom: 20px; color: #23cdd8" v-else>
        接收字节：
        <span style="color: #fff">{{ formatter(staticInfo['bytes.received']) }}</span>
        <Decoration3 style="width: 200px; height: 20px; margin-top: 5px" />
      </div>
    </div>
  </div>
</template>

<script setup name="right-center">
import { statisticNettyMqtt } from '@/api/iot/netty';
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

const { proxy } = getCurrentInstance();

// emqx统计信息
const staticInfo = ref({});
const config = ref({});
const timer = ref(null);
const mqtt = ref(userStore.mqtt);

onBeforeUnmount(() => {
  clearData();
})

/** 查询emqx统计*/
function statisticMqtt() {
  if (mqtt.value) {
    statisticNettyMqtt().then((response) => {
      staticInfo.value = response.data;
      // 图标配置
      config.value = {
        data: [
          {
            name: '发送',
            value: staticInfo.value['send_total'],
          },
          {
            name: '接收',
            value: staticInfo.value['receive_total'],
          },
        ],
        color: ['#ffdb5c', '#67e0e3'],
      };
      // 轮询
      switper();
    });
  } else {
    // 图标配置
    config.value = {
      data: [
        {
          name: '发送',
          value: 32761563,
        },
        {
          name: '接收',
          value: 31910071,
        },
      ],
      color: ['#ffdb5c', '#67e0e3'],
    };
    // 轮询
    switper();
  }
}

// 数字格式化
function formatter(number) {
  if (number) {
    const numbers = number.toString().split('').reverse();
    const segs = [];
    while (numbers.length) segs.push(numbers.splice(0, 3).join(''));
    return segs.join(',').split('').reverse().join('');
  }
  return 1024;
}

function clearData() {
  if (timer.value) {
    clearInterval(timer.value);
    timer.value = null;
  }
}
//轮询
function switper() {
  if (timer.value) {
    return;
  }
  let looper = (a) => {
    statisticMqtt();
  };
  timer.value = setInterval(looper, 60000);
}

statisticMqtt();
</script>
