<template>
<div class="centermap">
    <div class="maptitle">
        <div class="zuo"></div>
        <span class="titletext">设备分布图</span>
        <div class="you"></div>
    </div>
    <div style="height:640px;background-color:#0e2e87;">
        <BorderBox8>
            <div ref="mapRef" style="height:600px;width:760px;padding:10px;"></div>
        </BorderBox8>
    </div>
</div>
</template>

<script setup name="center-map">
import { ref, onMounted } from 'vue';
import { loadBMap } from '@/utils/map.js'
//安装的是echarts完整包，里面包含百度地图扩展，路径为 echarts/extension/bmap/bmap.js，将其引入
//ECharts的百度地图扩展，可以在百度地图上展现点图，线图，热力图等可视化
import 'echarts/extension/bmap/bmap';
import { listAllDeviceShort } from "@/api/iot/device";
import * as echarts from 'echarts';

const { proxy } = getCurrentInstance();

const mapRef = ref(null);

// 设备列表
const deviceList = ref([]);
const deviceCount = ref(0);
const timer = ref(null);

const data = reactive({
    // 查询参数
    queryParams: {
    }
})
const { queryParams } = toRefs(data);

onBeforeUnmount(() => {
    clearData();
})

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
        getAllDevice();
    };
    timer.value = setInterval(looper, 120000);
}
/**查询所有设备 */
function getAllDevice() {
    listAllDeviceShort(queryParams.value).then(response => {
        deviceList.value = response.rows;
        deviceCount.value = response.total;
        loadMap();
        // 轮询
        switper()
    })
}
/**加载地图*/
function loadMap() {
    loadBMap().then(() => {
        getmap();
    });
}

/** 地图 */
function getmap() {
    var myChart = echarts.init(mapRef.value);
    var option;

    // 单击事件
    myChart.on('click', (params) => {
        if (params.data.deviceId) {
            proxy.$router.push({
                path: '/iot/device-edit',
                query: {
                    t: Date.now(),
                    deviceId: params.data.deviceId,
                }
            });
        }
    });

    option = {
        tooltip: {
            trigger: 'item',
            backgroundColor: "rgba(58,73,116,0.7)",
            textStyle: {
                color: "rgba(65,235,246,1)"
            }
        },
        bmap: {
            center: [106, 37.5],
            zoom: 5,
            // 大屏用zomm缩放，导致地图缩放中心点会偏移
            roam: 'move',
            mapOptions: {
              // 禁用百度地图自带的底图可点功能
              enableMapClick: false
            },
            // 百度地图3.0之后的新版自定义样式
            mapStyleV2: {
                styleJson: [{
                  "featureType": "background",
                  "elementType": "geometry",
                  "stylers": {
                    "color": "#0e2e87ff"
                  },
                  "styleType": "background"
                }, {
                  "featureType": "districtlabel",
                  "elementType": "labels",
                  "stylers": {
                    "visibility": "off"
                  },
                  "styleType": "districtlabel"
                }]
            }
        },
        series: [{
                type: 'scatter',
                coordinateSystem: 'bmap',
                data: convertData(deviceList.value, 1),
                symbolSize: 10,
                itemStyle: {
                    color: '#e8fc05'
                }
            },
            {
                type: 'scatter',
                coordinateSystem: 'bmap',
                data: convertData(deviceList.value, 2),
                symbolSize: 10,
                itemStyle: {
                    color: '#fc3464'
                }
            }, {
                type: 'scatter',
                coordinateSystem: 'bmap',
                data: convertData(deviceList.value, 4),
                symbolSize: 10,
                itemStyle: {
                    color: '#eee'
                }
            }, {
                type: 'effectScatter',
                coordinateSystem: 'bmap',
                data: convertData(deviceList.value, 3),
                symbolSize: 12,
                showEffectOn: 'render',
                rippleEffect: {
                    brushType: 'stroke',
                    scale: 5
                },
                label: {
                    formatter: '{b}',
                    position: 'right',
                    show: false
                },
                itemStyle: {
                    color: '#5de88e',
                    shadowBlur: 100,
                    shadowColor: '#333'
                },
                zlevel: 1
            }
        ]
    };
    option && myChart.setOption(option, true);
}

// 格式化数据
function convertData(data, status) {
    var res = [];
    for (var i = 0; i < data.length; i++) {
        var geoCoord = [data[i].longitude, data[i].latitude];
        if (geoCoord && data[i].status == status) {
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
}

function formatter(params) {
    var htmlStr = '<div style="padding:5px;line-height:28px;">';
    htmlStr += "设备名称： <span style='color:#FFF'>" + params.data.name + "</span><br />";
    htmlStr += "设备编号： " + params.data.serialNumber + "<br />";
    htmlStr += "设备状态： ";
    if (params.data.status == 1) {
        htmlStr += "<span style='color:#E6A23C'>未激活</span>" + "<br />";
    } else if (params.data.status == 2) {
        htmlStr += "<span style='color:#F56C6C'>禁用</span>" + "<br />";
    } else if (params.data.status == 3) {
        htmlStr += "<span style='color:#67C23A'>在线</span>" + "<br />";
    } else if (params.data.status == 4) {
        htmlStr += "<span style='color:#909399'>离线</span>" + "<br />";
    }
    if (params.data.isShadow == 1) {
        htmlStr += "设备影子： " + "<span style='color:#67C23A'>启用</span>" + "<br />";
    } else {
        htmlStr += "设备影子： " + "<span style='color:#909399'>未启用</span>" + "<br />";
    }
    htmlStr += "产品名称： " + params.data.productName + "<br />";
    htmlStr += "固件版本： Version " + params.data.firmwareVersion + "<br />";
    htmlStr += "激活时间： " + params.data.activeTime + "<br />";
    htmlStr += "定位方式： ";
    if (params.data.locationWay == 1) {
        htmlStr += "自动定位" + "<br />";
    } else if (params.data.locationWay == 2) {
        htmlStr += "设备定位" + "<br />";
    } else if (params.data.locationWay == 3) {
        htmlStr += "自定义位置" + "<br />";
    } else {
        htmlStr += "未知" + "<br />";
    }
    htmlStr += "所在地址： " + params.data.networkAddress + "<br />";
    htmlStr += '</div>';
    return htmlStr;
}


onMounted(() => {
    getAllDevice();
});
</script>

<style lang="scss">
/* 清除地图LOGO */
.BMap_cpyCtrl {
    display: none !important;
}

.anchorBL {
    display: none !important;
}

.centermap {
    .maptitle {
        height: 60px;
        display: flex;
        justify-content: center;
        padding-top: 10px;
        box-sizing: border-box;

        .titletext {
            font-size: 28px;
            font-weight: 900;
            letter-spacing: 6px;
            background: linear-gradient(92deg,
                    #0072ff 0%,
                    #00eaff 48.8525390625%,
                    #01aaff 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            margin: 0 10px;
        }

        .zuo,
        .you {
            background-size: 100% 100%;
            width: 29px;
            height: 20px;
            margin-top: 8px;
        }

        .zuo {
            background: url("../../../assets/bigScreen/img/xiezuo.png") no-repeat;
        }

        .you {
            background: url("../../../assets/bigScreen/img/xieyou.png") no-repeat;
        }
    }
}
</style>
