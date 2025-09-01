<template>
<div id="index" ref="appRef" class="index_home" :class="{ pageisScale: true }">
    <div class="bg">
        <Loading v-if="loading">Loading...</Loading>
        <div v-else class="host-body">
            <!-- 头部 s -->
            <div class="d-flex jc-center title_wrap">
                <div class="zuojuxing"></div>
                <div class="youjuxing"></div>
                <div class="guang"></div>
                <div class="d-flex jc-center">
                    <div class="title">
                        <span class="title-text">管伊佳物联网平台</span>
                    </div>
                </div>
                <div class="timers ">
                    {{ dateYear }} {{ dateWeek }} {{ dateDay }}
                    <i class="blq-icon-shezhi02" style="margin-left:10px" @click="showSetting"></i>
                </div>
            </div>
            <!-- 头部 e-->
            <!-- 内容  s-->
            <index></index>
            <!-- 内容 e -->
        </div>
    </div>
    <Setting ref="setting" />
</div>
</template>

<script setup name="bigScreen-home">
import {
    formatTime
} from "../../utils/bigScreen/index.js";
import Setting from './setting.vue'
import index from './indexs/index.vue'
import {onMounted} from "vue";
const { proxy } = getCurrentInstance();

const timing = ref(null);
const loading = ref(true);
const dateDay = ref(null);
const dateYear = ref(null);
const dateWeek = ref(null);
const weekday = ref(["周日", "周一", "周二", "周三", "周四", "周五", "周六"]);


const drawTiming = ref(null);

// * 默认缩放值
const scale = {
    width: '1',
    height: '1',
}

// * 设计稿尺寸（px）
const baseWidth = 1920
const baseHeight = 1080

// * 需保持的比例（默认1.77778）
const baseProportion = parseFloat((baseWidth / baseHeight).toFixed(5))

const isScale = computed(() => {
    return proxy.$store.state.settings.isScale
})


// const { drawMixin } = drawMixin();

onMounted(() => {
    timeFn();
    cancelLoading();
})

onBeforeUnmount(() => {
    clearInterval(timing.value);
})

function numsFilter(msg) {
    return msg || 0;
}

function showSetting() {
    proxy.$refs.setting.init()
}

function timeFn() {
    timing.value = setInterval(() => {
        dateDay.value = formatTime(new Date(), "HH: mm: ss");
        dateYear.value = formatTime(new Date(), "yyyy-MM-dd");
        dateWeek.value = weekday.value[new Date().getDay()];
    }, 1000);
}

function cancelLoading() {
    setTimeout(() => {
        loading.value = false;
    }, 500);
}

function calcRate () {
    const appRef = proxy.$refs["appRef"]
    if (!appRef) return
    // 当前宽高比
    const currentRate = parseFloat((window.innerWidth / window.innerHeight).toFixed(5))
    if (appRef) {
        if (currentRate > baseProportion) {
            // 表示更宽
            scale.width = ((window.innerHeight * baseProportion) / baseWidth).toFixed(5)
            scale.height = (window.innerHeight / baseHeight).toFixed(5)
            appRef.style.transform = `scale(${scale.width}, ${scale.height}) translate(-50%, -50%)`
        } else {
            // 表示更高
            scale.height = ((window.innerWidth / baseProportion) / baseHeight).toFixed(5)
            scale.width = (window.innerWidth / baseWidth).toFixed(5)
            appRef.style.transform = `scale(${scale.width}, ${scale.height}) translate(-50%, -50%)`
        }
    }
}
function resize () {
    if(!isScale){
        return
    }
    clearTimeout(drawTiming.value)
    drawTiming.value = setTimeout(() => {
        calcRate()
    }, 200)
}

</script>

<style lang="scss">
@import "./home.scss";
@import '../../assets/bigScreen/css/theme/index.css';
@import "../../assets/bigScreen/css/index.scss"
</style>
