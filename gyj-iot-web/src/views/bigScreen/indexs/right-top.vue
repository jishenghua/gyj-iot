<template>
  <div style="padding-top: 10px">
<!--    <dv-capsule-chart :config="config" style="width: 430px; height: 230px" />-->
  </div>
</template>

<script setup name="right-top">
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

/** 查询mqtt统计*/
function statisticMqtt() {
  if (mqtt.value) {
    statisticNettyMqtt().then((response) => {
      staticInfo.value = response.data;
      config.value = {
        data: [
          {
            name: '今日接收',
            value: staticInfo.value['today_received'],
          },
          {
            name: '今日发送',
            value: staticInfo.value['today_send'],
          },
          {
            name: '订阅总数',
            value: staticInfo.value['subscribe_total'],
          },
          {
            name: '发布消息',
            value: staticInfo.value['send_total'],
          },
          {
            name: '接收消息',
            value: staticInfo.value['receive_total'],
          },
          {
            name: '认证次数',
            value: staticInfo.value['auth_total'],
          },
          {
            name: '认证成功',
            value: staticInfo.value['auth_total'],
          },
        ],
        unit: '次数',
        showValue: true,
      };
      switper();
    });
  } else {
    config.value = {
      data: [
        {
          name: '今日接收',
          value: 6000,
        },
        {
          name: '今日发送',
          value: 5000,
        },
        {
          name: '订阅总数',
          value: 4000,
        },
        {
          name: '发布消息',
          value: 3000,
        },
        {
          name: '接收消息',
          value: 2000,
        },
        {
          name: '认证次数',
          value: 1000,
        },
        {
          name: '认证成功',
          value: 1000,
        },
      ],
      unit: '次数',
      showValue: true,
    };
    switper();
  }
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
