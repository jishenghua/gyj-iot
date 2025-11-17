<template>
  <div style="padding: 10px; background-color: #f8f8f8">
    <el-row :gutter="20" style="margin: 10px 0px 20px 0px">
      <el-col :xs="24" :sm="24" :md="24" :lg="10" :xl="10">
        <el-card shadow="none" style="">
          <h3 style="font-weight: bold">
            <el-icon><Histogram /></el-icon>
            设备统计
          </h3>
          <el-row :gutter="40" class="panel-group">
            <el-col :span="12" class="card-panel-col">
              <div class="card-panel">
                <div class="card-panel-icon-wrapper icon-blue">
                  <svg-icon icon-class="device" class-name="card-panel-icon" />
                </div>
                <div class="card-panel-description">
                  <div class="card-panel-text">设备数量</div>
                  <count-to :start-val="0" :end-val="deviceStatistic.deviceCount" :duration="3000" class="card-panel-num" />
                </div>
              </div>
            </el-col>
            <el-col :span="12" class="card-panel-col">
              <div class="card-panel">
                <div class="card-panel-icon-wrapper icon-red">
                  <svg-icon icon-class="monitor-a" class-name="card-panel-icon" />
                </div>
                <div class="card-panel-description">
                  <div class="card-panel-text">监测数据</div>
                  <count-to :start-val="0" :end-val="deviceStatistic.monitorCount" :duration="3000" class="card-panel-num" />
                </div>
              </div>
            </el-col>
            <el-col :span="12" class="card-panel-col">
              <div class="card-panel">
                <div class="card-panel-icon-wrapper icon-blue">
                  <svg-icon icon-class="model" class-name="card-panel-icon" />
                </div>
                <div class="card-panel-description">
                  <div class="card-panel-text">产品数量</div>
                  <count-to :start-val="0" :end-val="deviceStatistic.productCount" :duration="1000" class="card-panel-num" />
                </div>
              </div>
            </el-col>
            <el-col :span="12" class="card-panel-col">
              <div class="card-panel">
                <div class="card-panel-icon-wrapper icon-red">
                  <svg-icon icon-class="alert" class-name="card-panel-icon" />
                </div>
                <div class="card-panel-description">
                  <div class="card-panel-text">告警数量</div>
                  <count-to :start-val="0" :end-val="deviceStatistic.alertCount" :duration="1000" class="card-panel-num" />
                </div>
              </div>
            </el-col>
            <el-col :span="12" class="card-panel-col">
              <div class="card-panel">
                <div class="card-panel-icon-wrapper icon-blue">
                  <svg-icon icon-class="log-a" class-name="card-panel-icon" />
                </div>
                <div class="card-panel-description">
                  <div class="card-panel-text">操作记录</div>
                  <count-to :start-val="0" :end-val="deviceStatistic.functionCount" :duration="2000" class="card-panel-num" />
                </div>
              </div>
            </el-col>
            <el-col :span="12" class="card-panel-col">
              <div class="card-panel">
                <div class="card-panel-icon-wrapper icon-red">
                  <svg-icon icon-class="event-a" class-name="card-panel-icon" />
                </div>
                <div class="card-panel-description">
                  <div class="card-panel-text">上报事件</div>
                  <count-to :start-val="0" :end-val="deviceStatistic.eventCount" :duration="2000" class="card-panel-num" />
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
        <el-card shadow="none" style="margin-top: 22px; height: 302px">
          <h3 style="font-weight: bold; margin-bottom: 10px">
            <el-icon><List /></el-icon>
            信息栏
          </h3>
          <div style="cursor: pointer; display: table; width: 100%; line-height: 36px" @click="openDetail(item.noticeId)" v-for="item in noticeList" :key="item.noticeId">
            <div style="display: table-cell; padding-right: 10px">
              <el-tag size="mini" effect="dark" type="warning" v-if="item.noticeType == 2">公告</el-tag>
              <el-tag size="mini" effect="dark" v-else>信息</el-tag>
              {{ item.noticeTitle }}
            </div>
            <div style="display: table-cell; width: 90px; font-size: 14px">
              <el-icon><Clock /></el-icon>
              {{ parseTime(item.createTime, '{y}-{m}-{d}') }}
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="24" :lg="14" :xl="14">
        <div style="overflow: hidden; border: 1px solid #ccc">
          <div id="mapGaode" style="height: 650px"></div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="none" style="margin: 10px 10px 20px 10px">
      <el-row :gutter="120" v-if="isAdmin">
        <el-col :xs="24" :sm="24" :md="24" :lg="8" :xl="8">
          <div style="padding: 20px">
            <div ref="pieCpu" style="height: 161px"></div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="24" :md="24" :lg="8" :xl="8">
          <div style="padding: 20px">
            <div ref="pieMemery" style="height: 161px"></div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="24" :md="24" :lg="8" :xl="8">
          <div style="padding: 20px">
            <div ref="pieDisk" style="height: 161px"></div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!--通知公告详情 -->
    <el-dialog :title="notice.noticeTitle" v-model="open" width="800px" append-to-body>
      <div style="margin-top: -20px; margin-bottom: 10px">
        <el-tag size="mini" effect="dark" type="warning" v-if="notice.noticeType == 2">公告</el-tag>
        <el-tag size="mini" effect="dark" v-else>信息</el-tag>
        <span style="margin-left: 20px">{{ notice.createTime }}</span>
      </div>
      <div v-loading="loading" class="content">
        <div v-html="notice.noticeContent"></div>
      </div>
      <template #footer>
          <div class="dialog-footer">
          <el-button type="primary" @click="closeDetail">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <div style="width: 100%; text-align: center; font-size: 14px; color: #666; line-height: 32px; margin-top: 150px">
      <span>
        Copyright © 2025-2035
        <a href="http://iot.gyjerp.com/" target="_blank">管伊佳物联</a> | Apache License
      </span>
      <br />
      <span>
        项目文档
        <a href="http://www.gyjerp.com/iot/" target="_blank">http://www.gyjerp.com/iot/</a>
      </span>
    </div>
  </div>
</template>

<script setup name="Index">
import { getDeviceStatistic } from '@/api/iot/device';
import { listNotice, getNotice } from '@/api/system/notice';
import CountTo from '@/components/CountTo/CountTo';
import { getServer } from '@/api/monitor/server';
import { listAllDeviceShort } from '@/api/iot/device';
import * as echarts from 'echarts';
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()
const { proxy } = getCurrentInstance();

const map = ref(null);
const pieCpu = ref(null);
const pieMemery = ref(null);
const pieDisk = ref(null);

// 遮罩层
const loading = ref(true);
// 是否显示弹出层
const open = ref(false);
// 信息列表
const noticeList = ref([]);
// 信息详情
const notice = ref({});
// 是否为管理员
const isAdmin = ref(false);
// 设备列表
const deviceList = ref([]);
// 设备统计信息
const deviceStatistic = ref({});
// 设备总数
const deviceCount = ref(0);
// 版本号
const version = ref('3.8.0');
const tableData = ref([]);

const data = reactive({
  // 服务器信息
  server: {
    jvm: {
      name: '',
      version: '',
      startTime: '',
      runTime: '',
      used: '',
      total: 100,
    },
    sys: {
      computerName: '',
      osName: '',
      computerIp: '',
      osArch: '',
    },
    cpu: {
      cpuNum: 1,
    },
    mem: {
      total: 2,
    },
  },
});
const { server } = toRefs(data);

function init() {
  if (userStore.roles.indexOf("tenant") === -1 && userStore.roles.indexOf('general') === -1) {
    isAdmin.value = true
    searchServer();
  }
}
//刷新iframe
function flushIframe() {
  let iframe = window.parent.document.getElementById('iframe');
  iframe.contentWindow.location.reload(true);
}
/** 查询设备统计信息 */
function searchDeviceStatistic() {
  getDeviceStatistic().then((response) => {
    deviceStatistic.value = response.data;
  });
}
/** 查询公告列表 */
function getNoticeList() {
  let queryParams = {
    pageNum: 1,
    pageSize: 6,
  }
  listNotice(queryParams).then((response) => {
    noticeList.value = response.rows.splice(0, 6);
  });
}
// 打开信息详情
function openDetail(id) {
  open.value = true;
  loading.value = true;
  getNotice(id).then((response) => {
    notice.value = response.data;
    open.value = true;
    loading.value = false;
  });
}
// 取消按钮
function closeDetail() {
  notice.value = {};
  open.value = false;
}
/**查询所有设备 */
function getAllDevice() {
  let queryParams = {
    pageNum: 1,
    pageSize: 6,
  }
  listAllDeviceShort(queryParams).then((response) => {
    deviceList.value = response.rows;
    deviceCount.value = response.total;
    loadMap();
  });
}
/**加载地图*/
function loadMap() {
  proxy.$nextTick(() => {
    getmap1();
  });
}
/** 查询服务器信息 */
function searchServer() {
  getServer().then((response) => {
    server.value = response.data;
    tableData.value = [
      {
        server: '服务器名',
        serverContent: server.value.sys.computerName,
        java: 'Java名称',
        javaContent: server.value.jvm.name,
      },
      {
        server: '服务器IP',
        serverContent: server.value.sys.computerIp,
        java: '启动时间',
        javaContent: server.value.jvm.startTime,
      },
      {
        server: '操作系统',
        serverContent: server.value.sys.osName,
        java: 'Java版本',
        javaContent: server.value.jvm.version,
      },
      {
        server: '系统架构',
        serverContent: server.value.sys.osArch,
        java: '运行时长',
        javaContent: server.value.jvm.runTime,
      },
      {
        server: 'CPU核心',
        serverContent: server.value.cpu.cpuNum,
        java: '占用内存',
        javaContent: server.value.jvm.used,
      },
      {
        server: '内存大小',
        serverContent: server.value.mem.total,
        java: 'JVM总内存',
        javaContent: server.value.jvm.total,
      },
    ];
    proxy.$nextTick(() => {
      drawPieCpu();
      drawPieMemery();
      drawPieDisk();
    });
  });
}
/** 地图 */
function getmap() {
  const myChart = echarts.init(map.value);
  var option;

  // 单击事件
  myChart.on('click', (params) => {
    if (params.data.deviceId) {
      proxy.$router.push({
        path: '/iot/device-edit',
        query: {
          t: Date.now(),
          deviceId: params.data.deviceId,
        },
      });
    }
  });

  // 格式化数据
  let convertData = function (data, status) {
    var res = [];
    for (var i = 0; i < data.length; i++) {
      var geoCoord = [data[i].longitude, data[i].latitude];
      if (geoCoord && data[i].status === status) {
        res.push({
          name: data[i].deviceName,
          value: geoCoord,
          serialNumber: data[i].serialNumber,
          status: data[i].status,
          isShadow: data[i].isShadow,
          firmwareVersion: data[i].firmwareVersion,
          networkAddress: data[i].networkAddress,
          productName: data[i].productName,
          activeTime: data[i].activeTime == null ? '' : data[i].activeTime,
          deviceId: data[i].deviceId,
          locationWay: data[i].locationWay,
        });
      }
    }
    return res;
  };
  option = {
    title: {
      text: '设备分布（在线数 ' + deviceList.value.filter((x) => x.status == 3).length + '）',
      subtext: 'gyjiot open source iot platform',
      sublink: 'http://iot.gyjerp.com',
      target: '_blank',
      textStyle: {
        color: '#333',
        textBorderColor: '#fff',
        textBorderWidth: 10,
      },
      top: 10,
      left: 'center',
    },
    tooltip: {
      trigger: 'item',
      formatter: function (params) {
        var htmlStr = '<div style="padding:5px;line-height:28px;">';
        htmlStr += "设备名称： <span style='color:#409EFF'>" + params.data.name + '</span><br />';
        htmlStr += '设备编号： ' + params.data.serialNumber + '<br />';
        htmlStr += '设备状态： ';
        if (params.data.status == 1) {
          htmlStr += "<span style='color:#E6A23C'>未激活</span>" + '<br />';
        } else if (params.data.status == 2) {
          htmlStr += "<span style='color:#F56C6C'>禁用</span>" + '<br />';
        } else if (params.data.status == 3) {
          htmlStr += "<span style='color:#67C23A'>在线</span>" + '<br />';
        } else if (params.data.status == 4) {
          htmlStr += "<span style='color:#909399'>离线</span>" + '<br />';
        }
        if (params.data.isShadow == 1) {
          htmlStr += '设备影子： ' + "<span style='color:#67C23A'>启用</span>" + '<br />';
        } else {
          htmlStr += '设备影子： ' + "<span style='color:#909399'>未启用</span>" + '<br />';
        }
        htmlStr += '产品名称： ' + params.data.productName + '<br />';
        htmlStr += '固件版本： Version ' + params.data.firmwareVersion + '<br />';
        htmlStr += '激活时间： ' + params.data.activeTime + '<br />';
        htmlStr += '定位方式： ';
        if (params.data.locationWay == 1) {
          htmlStr += '自动定位' + '<br />';
        } else if (params.data.locationWay == 2) {
          htmlStr += '设备定位' + '<br />';
        } else if (params.data.locationWay == 3) {
          htmlStr += '自定义位置' + '<br />';
        } else {
          htmlStr += '未知' + '<br />';
        }
        htmlStr += '所在地址： ' + params.data.networkAddress + '<br />';
        htmlStr += '</div>';
        return htmlStr;
      },
    },
    bmap: {
      center: [133, 38],
      zoom: 5,
      roam: true,
      mapStyle: {
      },
    },
    series: [
      {
        type: 'scatter',
        coordinateSystem: 'bmap',
        data: convertData(deviceList.value, 1),
        symbolSize: 15,
        itemStyle: {
          color: '#E6A23C',
        },
      },
      {
        type: 'scatter',
        coordinateSystem: 'bmap',
        data: convertData(deviceList.value, 2),
        symbolSize: 15,
        itemStyle: {
          color: '#F56C6C',
        },
      },
      {
        type: 'scatter',
        coordinateSystem: 'bmap',
        data: convertData(deviceList.value, 4),
        symbolSize: 15,
        itemStyle: {
          color: '#909399',
        },
      },
      {
        type: 'effectScatter',
        coordinateSystem: 'bmap',
        data: convertData(deviceList.value, 3),
        symbolSize: 15,
        showEffectOn: 'render',
        rippleEffect: {
          brushType: 'stroke',
          scale: 5,
        },
        label: {
          formatter: '{b}',
          position: 'right',
          show: false,
        },
        itemStyle: {
          color: '#67C23A',
          shadowBlur: 100,
          shadowColor: '#333',
        },
        zlevel: 1,
      },
    ],
  };
  option && myChart.setOption(option);
}

/** 高德地图 */
function getmap1() {
  // 创建地图实例
  let map = new AMap.Map('mapGaode', {
    zoom: 4,
    center: [ 108, 34]
  })
  for(var i=0;i<deviceList.value.length;i+=1) {
    let currentInfo = deviceList.value[i]
    if (currentInfo.longitude && currentInfo.latitude) {
      let fillStr = ''
      let htmlStr = '<div style="padding:5px;line-height:28px;">';
      htmlStr += "设备名称： <span style='color:#409EFF'>" + currentInfo.deviceName + '</span><br />';
      htmlStr += '设备编号： ' + currentInfo.serialNumber + '<br />';
      htmlStr += '设备状态： ';
      if (currentInfo.status == 1) {
        fillStr = '%23E6A23C'
        htmlStr += "<span style='color:#E6A23C'>未激活</span>" + '<br />';
      } else if (currentInfo.status == 2) {
        fillStr = '%23F56C6C'
        htmlStr += "<span style='color:#F56C6C'>禁用</span>" + '<br />';
      } else if (currentInfo.status == 3) {
        fillStr = '%2367C23A'
        htmlStr += "<span style='color:#67C23A'>在线</span>" + '<br />';
      } else if (currentInfo.status == 4) {
        fillStr = '%23909399'
        htmlStr += "<span style='color:#909399'>离线</span>" + '<br />';
      }
      if (currentInfo.isShadow == 1) {
        htmlStr += '设备影子： ' + "<span style='color:#67C23A'>启用</span>" + '<br />';
      } else {
        htmlStr += '设备影子： ' + "<span style='color:#909399'>未启用</span>" + '<br />';
      }
      htmlStr += '产品名称： ' + currentInfo.productName + '<br />';
      htmlStr += '固件版本： Version ' + currentInfo.firmwareVersion + '<br />';
      htmlStr += '激活时间： ' + currentInfo.activeTime + '<br />';
      htmlStr += '定位方式： ';
      if (currentInfo.locationWay == 1) {
        htmlStr += '自动定位' + '<br />';
      } else if (currentInfo.locationWay == 2) {
        htmlStr += '设备定位' + '<br />';
      } else if (currentInfo.locationWay == 3) {
        htmlStr += '自定义位置' + '<br />';
      } else {
        htmlStr += '未知' + '<br />';
      }
      let networkAddress = currentInfo.networkAddress || '未知';
      htmlStr += '所在地址： ' + networkAddress + '<br />';
      htmlStr += '</div>';

      // 创建标记点
      let marker = new AMap.Marker({
        position: new AMap.LngLat(currentInfo.longitude, currentInfo.latitude),
        // 自定义图标
        icon: new AMap.Icon({
          image: 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 20 20"><circle cx="10" cy="10" r="8" fill="' + fillStr + '" fill-opacity="0.5" /></svg>',
          size: new AMap.Size(15, 15), // 图标大小
          imageSize: new AMap.Size(15, 15) // 实际显示大小
        }),
        map: map
      })

      // 创建信息窗口（Tooltip）
      let infoWindow = new AMap.InfoWindow({
        content: htmlStr,
        offset: new AMap.Pixel(8, 0)
      })

      // 添加事件监听
      marker.on('mouseover', () => infoWindow.open(map, marker.getPosition()))
      marker.on('mouseout', () => infoWindow.close())

      // 点击事件
      let deviceId = deviceList.value[i].deviceId
      marker.on('click', () => {
        if (deviceId) {
          proxy.$router.push({
            path: '/iot/device-edit',
            query: {
              t: Date.now(),
              deviceId: deviceId,
            },
          });
        }
      })
    }
  }
}
function drawPieCpu() {
  // 基于准备好的dom，初始化echarts实例
  const myChart = echarts.init(pieCpu.value);
  var option;
  option = {
    title: {
      text: 'CPU使用率',
      left: 'left',
      textStyle: {
        fontSize: 16,
      },
    },
    tooltip: {
      trigger: 'item',
    },
    legend: {
      orient: 'vertical',
      left: 'right',
    },
    color: ['#E6A23C', '#F56C6C', '#DDD'],
    series: [
      {
        name: 'CPU使用率 %',
        type: 'pie',
        radius: '55%',
        label: {
          show: false,
        },
        labelLine: {
          normal: {
            position: 'inner',
            show: false,
          },
        },
        data: [
          {
            value: server.value.cpu.used,
            name: '用户',
          },
          {
            value: server.value.cpu.sys,
            name: '系统',
          },
          {
            value: server.value.cpu.free,
            name: '空闲',
          },
        ],
      },
    ],
  };
  option && myChart.setOption(option);
}
function drawPieMemery() {
  // 基于准备好的dom，初始化echarts实例
  const myChart = echarts.init(pieMemery.value);
  var option;
  option = {
    title: {
      text: '内存使用率',
      left: 'left',
      textStyle: {
        fontSize: 16,
      },
    },
    tooltip: {
      trigger: 'item',
    },
    legend: {
      orient: 'vertical',
      left: 'right',
    },
    color: ['#F56C6C', '#DDD'],
    series: [
      {
        name: '内存使用率 G',
        type: 'pie',
        radius: '55%',
        label: {
          show: false,
        },
        labelLine: {
          normal: {
            position: 'inner',
            show: false,
          },
        },
        data: [
          {
            value: server.value.mem.used,
            name: '已用',
          },
          {
            value: server.value.mem.free,
            name: '剩余',
          },
        ],
      },
    ],
  };
  option && myChart.setOption(option);
}
function drawPieDisk() {
  // 基于准备好的dom，初始化echarts实例
  const myChart = echarts.init(pieDisk.value);
  var option;
  let one = server.value.sysFiles[0].used.replace('GB', '');
  let two = server.value.sysFiles[0].free.replace('GB', '');
  option = {
    title: {
      text: '系统盘使用率',
      left: 'left',
      textStyle: {
        fontSize: 16,
      },
    },
    tooltip: {
      trigger: 'item',
    },
    legend: {
      orient: 'vertical',
      left: 'right',
    },
    color: ['#F56C6C', '#DDD'],
    series: [
      {
        name: '磁盘状态 G',
        type: 'pie',
        radius: '55%',
        label: {
          show: false,
        },
        labelLine: {
          normal: {
            position: 'inner',
            show: false,
          },
        },
        data: [
          {
            value: one,
            name: '已用',
          },
          {
            value: two,
            name: '可用',
          },
        ],
      },
    ],
  };
  option && myChart.setOption(option);
}

init();
getAllDevice();
getNoticeList();
searchDeviceStatistic();
</script>

<style lang="scss" scoped>
  .phone {
    height: 729px;
    width: 370px;
    background-image: url('../assets/images/phone.png');
    background-size: cover;
    margin: 0 auto;
  }

  .phone-container {
    height: 618px;
    width: 343px;
    position: relative;
    top: 46px;
    left: 12px;
    background-color: #fff;
  }

  .content {
    line-height: 24px;
    padding: 10px;
    border: 1px solid #eee;
    border-radius: 10px;
  }

  .description {
    font-size: 12px;

    tr {
      line-height: 20px;
    }
  }

  .panel-group {
    .card-panel-col {
      margin-bottom: 10px;
    }

    .card-panel {
      height: 68px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      color: #666;
      border: 1px solid #eee;
      border-radius: 5px;
      //box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.08);
      background-color: #fff;

      &:hover {
        .card-panel-icon-wrapper {
          color: #fff;
        }

        .icon-blue {
          background: #36a3f7;
        }

        .icon-green {
          background: #34bfa3;
        }

        .icon-red {
          background: #f56c6c;
        }

        .icon-orange {
          background: #e6a23c;
        }
      }

      .icon-blue {
        color: #36a3f7;
      }

      .icon-green {
        color: #34bfa3;
      }

      .icon-red {
        color: #f56c6c;
      }

      .icon-orange {
        color: #e6a23c;
      }

      .card-panel-icon-wrapper {
        float: left;
        margin: 10px;
        padding: 10px;
        transition: all 0.38s ease-out;
        border-radius: 6px;
      }

      .card-panel-icon {
        float: left;
        font-size: 30px;
      }

      .card-panel-description {
        float: right;
        font-weight: bold;
        margin: 15px;
        margin-left: 0px;

        .card-panel-text {
          line-height: 14px;
          color: rgba(0, 0, 0, 0.45);
          font-size: 14px;
          margin-bottom: 12px;
          text-align: right;
        }

        .card-panel-num {
          font-size: 18px;
        }
      }
    }
  }
</style>
