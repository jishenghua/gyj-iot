<template>
  <div style="padding-left: 20px">
    <el-form :inline="true" label-width="100px">
      <el-form-item label="监测间隔(ms)">
        <el-tooltip class="item" effect="light" content="取值范围500-10000毫秒" placement="top">
          <el-input v-model="monitorInterval" placeholder="请输入监测间隔" type="number" clearable size="small" style="width: 180px" />
        </el-tooltip>
      </el-form-item>
      <el-form-item label="监测次数">
        <el-tooltip class="item" effect="light" content="取值方位1-300" placement="top">
          <el-input v-model="monitorNumber" placeholder="请输入监测次数" type="number" clearable size="small" style="width: 180px" />
        </el-tooltip>
      </el-form-item>
      <el-form-item>
        <el-button type="success" icon="el-icon-video-play" size="mini" @click="beginMonitor()" style="margin-left: 30px">开始监测</el-button>
        <el-button type="danger" icon="el-icon-video-pause" size="mini" @click="stopMonitor()">停止监测</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="20" v-loading="chartLoading" element-loading-text="正在接收设备数据，请耐心等待......" element-loading-spinner="el-icon-loading" element-loading-background="rgba(0, 0, 0, 0.8)">
      <el-col :span="12" v-for="(item, index) in monitorThings" :key="index" style="margin-bottom: 20px">
        <el-card shadow="hover" :body-style="{ paddingTop: '10px', marginBottom: '-20px' }">
          <div ref="monitor" style="height: 210px; padding: 0"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="DeviceMonitor">
import mqttTool from '@/utils/mqttTool'

const { proxy } = getCurrentInstance();

// 实时监测间隔
const monitorInterval = ref(1000);
// 实时监测次数
const monitorNumber = ref(60);
// 图表集合
const chart = ref([]);
// 图表数据集合
const dataList = ref([]);
// 监测物模型
const monitorThings = ref([]);
// 图表遮罩层
const chartLoading = ref(false);
// 设备信息
const deviceInfo = ref({});

const props = defineProps({
  device: {
    type: Object,
    default: null,
  }
});

watch(() => props.device, (newVal, oldVal) => {
  deviceInfo.value = newVal;
  if (deviceInfo.value && deviceInfo.value.deviceId != 0) {
    // 监测数据
    monitorThings.value = deviceInfo.value.monitorList;
    // 监测数据集合初始化
    dataList.value = [];
    for (let i = 0; i < monitorThings.value.length; i++) {
      dataList.value.push({
        id: monitorThings.value[i].id,
        name: monitorThings.value[i].name,
        data: [],
      });
    }
    // 绘制监测图表
    proxy.$nextTick(function () {
      getMonitorChart();
    });
    mqttCallback();
  }
});

/**
 * Mqtt发布消息
 * @device 设备
 * @model 物模型 ,type 类型(1=属性，2=功能，3=OTA升级(商业版支持)，4=实时监测)
 * */
function mqttPublish(device, model) {
  let topic = '';
  let message = '';
  if (model.type == 4) {
    // 实时监测
    topic = '/' + device.productId + '/' + device.serialNumber + '/monitor/get';
    message = '{"count":' + model.value + ',"interval":' + monitorInterval.value + '}';
  } else {
    return;
  }
  if (topic != '') {
    // 发布
    mqttTool.publish(topic, message, model.name).then((res) => {
      proxy.$modal.notifySuccess(res);
    })
    .catch((res) => {
      proxy.$modal.notifyError(res);
    });
  }
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
    if (topics[3] == 'status') {
      console.log('接收到【设备状态】主题：', topic);
      console.log('接收到【设备状态】内容：', message);
      // 更新列表中设备的状态
      if (deviceInfo.value.serialNumber == deviceNum) {
        deviceInfo.value.status = message.status;
        deviceInfo.value.isShadow = message.isShadow;
        deviceInfo.value.rssi = message.rssi;
      }
    }
    if (topics[3] == 'monitor') {
      console.log('接收到【实时监测】主题：', topic);
      console.log('接收到【实时监测】内容：', message);
      // 实时监测
      chartLoading.value = false;
      for (let k = 0; k < message.length; k++) {
        let value = message[k].value;
        let id = message[k].id;
        let remark = message[k].remark;
        // 数据加载到图表
        for (let i = 0; i < dataList.value.length; i++) {
          if (id == dataList.value[i].id) {
            // 普通类型匹配
            if (dataList.value[i].length > 50) {
              dataList.value[i].shift();
            }
            dataList.value[i].data.push([proxy.getTime(), value]);
            // 更新图表
            chart.value[i].setOption({
              series: [
                {
                  data: dataList.value[i].data,
                },
              ],
            });
            break;
          } else if (dataList.value[i].id.indexOf('array_') == 0) {
            // 数组类型匹配,例如：gateway_temperature,图表id去除前缀后匹配
            let index = dataList.value[i].id.substring(6, 8);
            let identity = dataList.value[i].id.substring(9);
            if (identity == id) {
              let values = value.split(',');
              if (dataList.value[i].length > 50) {
                dataList.value[i].shift();
              }
              dataList.value[i].data.push([proxy.getTime(), values[index]]);
              // 更新图表
              chart.value[i].setOption({
                series: [
                  {
                    data: dataList.value[i].data,
                  },
                ],
              });
              break;
            }
          }
        }
      }
    }
  });
}
/** 更新实时监测参数*/
function beginMonitor() {
  if (deviceInfo.value.status != 3) {
    proxy.$modal.alertError('设备不在线，下发指令失败');
    return;
  }
  // 清空图表数据
  for (let i = 0; i < dataList.value.length; i++) {
    dataList.value[i].data = [];
  }
  if (monitorInterval.value < 500 || monitorInterval.value > 10000) {
    proxy.$modal.alertError('实时监测的间隔范围500-10000毫秒');
  }
  if (monitorNumber.value == 0 || monitorNumber.value > 300) {
    proxy.$modal.alertError('实时监测数量范围1-300');
  }
  // Mqtt发布实时监测消息
  let model = {};
  model.name = '更新实时监测';
  model.value = monitorNumber.value;
  model.type = 4;
  mqttPublish(deviceInfo.value, model);
  chartLoading.value = true;
}
/** 停止实时监测 */
function stopMonitor() {
  if (deviceInfo.value.status != 3) {
    proxy.$modal.alertError('设备不在线，下发指令失败');
    return;
  }
  chartLoading.value = false;
  // Mqtt发布实时监测
  let model = {};
  model.name = '关闭实时监测';
  model.value = 0;
  model.type = 4;
  mqttPublish(deviceInfo.value, model);
}
/**监测数据 */
function getMonitorChart() {
  let color = ['#1890FF', '#91CB74', '#FAC858', '#EE6666', '#73C0DE', '#3CA272', '#FC8452', '#9A60B4', '#ea7ccc'];
  for (let i = 0; i < monitorThings.value.length; i++) {
    // 设置宽度
    proxy.$refs.monitor[i].style.width = document.documentElement.clientWidth / 2 - 255 + 'px';
    chart.value[i] = proxy.$echarts.init(this.$refs.monitor[i]);
    var option;
    option = {
      title: {
        left: 'center',
        text: monitorThings.value[i].name + ' （单位 ' + (monitorThings.value[i].datatype.unit != undefined ? monitorThings.value[i].datatype.unit : '无') + '）',
        textStyle: {
          fontSize: 14,
        },
      },
      grid: {
        top: '50px',
        left: '20px',
        right: '20px',
        bottom: '10px',
        containLabel: true,
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          animation: true,
        },
      },
      xAxis: {
        type: 'time',
        show: false,
        splitLine: {
          show: false,
        },
      },
      yAxis: {
        type: 'value',
        boundaryGap: [0, '100%'],
        splitLine: {
          show: true,
        },
      },
      series: [
        {
          name: monitorThings.value[i].name,
          type: 'line',
          symbol: 'none',
          sampling: 'lttb',
          itemStyle: {
            color: i > 9 ? color[0] : color[i],
          },
          areaStyle: {},
          data: [],
        },
      ],
    };
    option && chart.value[i].setOption(option);
  }
}
/* 获取当前时间*/
function getTime() {
  let date = new Date();
  let y = date.getFullYear();
  let m = date.getMonth() + 1;
  let d = date.getDate();
  let H = date.getHours();
  let mm = date.getMinutes();
  let s = date.getSeconds();
  m = m < 10 ? '0' + m : m;
  d = d < 10 ? '0' + d : d;
  H = H < 10 ? '0' + H : H;
  return y + '-' + m + '-' + d + ' ' + H + ':' + mm + ':' + s;
}
</script>
