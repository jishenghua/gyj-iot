<template>
<div class="center_bottom">
    <div>
<!--        <dv-scroll-board :config="config" style="width:360px;height:175px" />-->
    </div>
    <div style="display:flex;height:115px;margin-top:30px;">
        <div>
<!--            <dv-water-level-pond :config="configCpu" style="width:115px;height:100%;" />-->
            <div style="text-align: center;margin-top:10px;color:#23cdd8;font-weight:600;">CPU</div>
        </div>
        <div style="margin:0 20px;">
<!--            <dv-water-level-pond :config="configMemery" style="width:115px;height:100%;" />-->
            <div style="text-align: center;margin-top:10px;color:#23cdd8;font-weight:600;">内存</div>
        </div>
        <div style="">
<!--            <dv-water-level-pond :config="configDisk" style="width:115px;height:100%;" />-->
            <div style="text-align: center;margin-top:10px;color:#23cdd8;font-weight:600;">系统盘</div>
        </div>
    </div>
</div>
</template>

<script setup name="center-bottom">
import { getServer } from "@/api/monitor/server";

const timer = ref(null);
const config = ref({});

const dataInfo = reactive({
    data: [
        ['服务器名称', ''],
        ['服务器IP', ""],
        ['操作系统', ''],
        ['系统架构', ""],
        ['CPU核心数', ''],
        ['服务器内存', ""],
        ['Java名称', ''],
        ['Java版本', ''],
        ['Java启动时间', ""],
        ['Java运行时长', ""],
        ['Java占用内存', ''],
        ['Java总内存', ""]
    ],
    // 服务器信息
    server: {
        jvm: {
            name: "",
            version: "",
            startTime: "",
            runTime: "",
            used: "",
            total: 100
        },
        sys: {
            computerName: "",
            osName: "",
            computerIp: "",
            osArch: ""
        },
        cpu: {
            cpuNum: 1
        },
        mem: {
            total: 2
        }
    },
    configCpu: {
        data: [50],
        shape: 'roundRect',
        formatter: '{value}%',
        waveHeight: 10
    },
    configMemery: {
        data: [50],
        shape: 'roundRect',
        formatter: '{value}%',
        waveHeight: 10
    },
    configDisk: {
        data: [50],
        shape: 'roundRect',
        formatter: '{value}%',
        waveHeight: 10
    }
})

const { data, server, configCpu, configMemery, configDisk } = toRefs(dataInfo);

onMounted(() => {
    getServerInfo();
})

onBeforeUnmount(() => {
    clearData();
})

/** 查询服务器信息 */
function getServerInfo() {
    getServer().then(response => {
        server.value = response.data;
        config.value = {
            rowNum: 6,
            oddRowBGC: '',
            evenRowBGC: '',
            columnWidth: [105, 230],
            data: [
                ['服务器名：', server.value.sys.computerName],
                ['服务器IP：', server.value.sys.computerIp],
                ['操作系统：', server.value.sys.osName],
                ['系统架构：', server.value.sys.osArch],
                ['CPU核心：', server.value.cpu.cpuNum],
                ['系统内存：', server.value.mem.total],
                ['Java名称：', server.value.jvm.name],
                ['Java版本：', server.value.jvm.version],
                ['启动时间：', server.value.jvm.startTime],
                ['运行时长：', server.value.jvm.runTime],
                ['运行内存：', server.value.jvm.used],
                ['JVM总内存：', server.value.jvm.total],
            ],
        };
        // 计算CPU使用
        let cpu = (server.value.cpu.used + server.value.cpu.sys) / (server.value.cpu.used + server.value.cpu.sys + server.value.cpu.free) * 100;
        configCpu.value = {
            data: [cpu.toFixed(1), cpu.toFixed(1) - 10],
            shape: 'roundRect',
            formatter: '{value}%',
            waveHeight: 10
        };
        // 计算内存
        let memery = server.value.mem.used / (server.value.mem.used + server.value.mem.free) * 100;
        configMemery.value = {
            data: [memery.toFixed(1), memery.toFixed(1) - 10],
            shape: 'roundRect',
            formatter: '{value}%',
            waveHeight: 10
        };
        // 计算硬盘
        let disk = Number(server.value.sysFiles[0].used.replace("GB", "")) / (Number(server.value.sysFiles[0].used.replace("GB", "")) + Number(server.value.sysFiles[0].free.replace("GB", ""))) * 100;
        configDisk.value = {
            data: [disk.toFixed(1), disk.toFixed(1) - 10],
            shape: 'roundRect',
            formatter: '{value}%',
            waveHeight: 10
        };
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
        getServerInfo();
    };
    timer.value = setInterval(looper, 60000);
}
</script>

<style lang="scss" scoped>
.center_bottom {
    width: 100%;
    height: 100%;
    padding: 10px;
    display: flex;
}
</style>
