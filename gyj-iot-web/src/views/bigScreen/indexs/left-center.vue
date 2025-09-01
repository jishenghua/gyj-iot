<template>
  <div>
    <div ref="statsChart" style="height: 240px; margin: 20px 0 40px 0"></div>
  </div>
</template>

<script setup name="left-center">
import { getNettyMqttStats } from '@/api/iot/netty';
import * as echarts from 'echarts';
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

const { proxy } = getCurrentInstance();

const timer = ref(null);
// emqx状态数据
const stats = ref({});
const mqtt = ref(userStore.mqtt);

onBeforeUnmount(() => {
  clearData();
})

/** 查询mqtt状态数据*/
function getMqttStats() {
  if (mqtt.value) {
    getNettyMqttStats().then((response) => {
      stats.value = response.data;
      drawStats();
      // 轮询
      switper();
    });
  } else {
    proxy.$nextTick(() => {
      // 初始值
      stats.value = {
        'connections.count': 800,
        'connections.max': 8000,
        'sessions.count': 700,
        'sessions.max': 7000,
        'topics.count': 600,
        'topics.max': 6000,
        'subscribers.count': 500,
        'subscribers.max': 5000,
        'routes.count': 400,
        'routes.max': 4000,
        'retained.count': 300,
        'retained.max': 3000,
      };
      drawStats();
      // 轮询
      switper();
    });
  }
}
/** EMQX状态统计 */
function drawStats() {
  // 基于准备好的dom，初始化echarts实例
  let myChart = echarts.init(proxy.$refs.statsChart);
  var option;
  option = {
    animationDuration: 3000,
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
      backgroundColor: 'rgba(58,73,116,0.7)',
      textStyle: {
        color: 'rgba(65,235,246,1)',
      },
    },
    legend: {
      textStyle: {
        color: 'rgba(65,235,246,1)',
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true,
    },
    xAxis: {
      type: 'value',
      boundaryGap: [0, 0.01],
      axisLabel: {
        fontSize: 12,
        color: '#fff',
      },
      splitLine: {
        //网格线
        lineStyle: {
          type: 'dashed', //设置网格线类型 dotted：虚线   solid:实线
          color: 'rgba(65,235,246,1)', //网格线颜色
          width: 0.5,
        },
      },
    },
    yAxis: {
      type: 'category',
      axisLabel: {
        fontSize: 12,
        color: '#fff',
      },
      data: mqtt.value ? ['连接数量', '会话数量', '订阅数量', '路由数量', '保留消息'] : ['连接数量', '会话数量', '主题数量', '订阅数量', '路由数量', '保留消息'],
    },
    series: [
      {
        name: '当前数量',
        type: 'bar',
        data: mqtt.value
                ? [stats.value['connection_count'], stats.value['session_count'], stats.value['subscription_count'], stats.value['retain_count'], stats.value['retain_count']]
                : [stats.value['connections.count'], stats.value['sessions.count'], stats.value['topics.count'], stats.value['subscribers.count'], stats.value['routes.count'], stats.value['retained.count']],
        itemStyle: {
          color: '#67e0e3',
        },
      },
      {
        name: mqtt.value ? '累计总数' : '历史最大数',
        type: 'bar',
        data: mqtt.value
                ? [stats.value['connection_total'], stats.value['session_total'], stats.value['subscription_total'], stats.value['retain_total'], stats.value['retain_total']]
                : [stats.value['connections.max'], stats.value['sessions.max'], stats.value['topics.max'], stats.value['subscribers.max'], stats.value['routes.max'], stats.value['retained.max']],
        itemStyle: {
          color: '#ffdb5c',
        },
      },
    ],
  };
  option && myChart.setOption(option);
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
    getMqttStats();
  };
  timer.value = setInterval(looper, 60000);
}

getMqttStats();
</script>
