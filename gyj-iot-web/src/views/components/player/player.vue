<template>
    <div class="root">
        <div class="container-shell">
            <div id="container" ref="containerRef"></div>
        </div>
    </div>
</template>

<script setup name="Player">
import { ptzdirection, ptzscale } from '@/api/iot/sipdevice';

const { proxy } = getCurrentInstance();

let jessibucaPlayer = {};
const isPlaybackPause = ref(false);
const useWebGPU = ref(false);
const isInit = ref(false);
const playinfo = ref({});
const playtype = ref('play');
const uid = ref(new Date().getTime());

const data = reactive({
    operateBtns: {
        fullscreen: true,
        zoom: true,
        play: true,
        audio: true,
    }
})
const { operateBtns } = toRefs(data);

const props = defineProps({
    playerinfo: {
        type: Object,
        default: null,
    }
});

// jessibuca: null,

watch(() => props.playerinfo, (newVal, oldVal) => {
    console.log('playerinfo 发生变化');
    playinfo.value = newVal;
    if (playinfo.value && playinfo.value.playtype !== '') {
        playtype.value = playinfo.value.playtype;
    }
})

// onMounted(() => {
//     console.log(uid.value)
// })

function init() {
    var isSupportWebgpu = 'gpu' in navigator;
    if (isSupportWebgpu) {
        console.log('支持webGPU');
        useWebGPU.value = true;
    } else {
        console.log('暂不支持webGPU，降级到webgl渲染');
        useWebGPU.value = false;
    }

    const useVconsole = isMobile() || isPad();
    if (useVconsole && window.VConsole) {
        new window.VConsole();
    }
    proxy.$nextTick(() => {
        initplayer();
    });
}
function initplayer() {
    isPlaybackPause.value = false;
    initconf();
    console.log(uid.value)
    jessibucaPlayer[uid.value] = new window.JessibucaPro({
        container: proxy.$refs['containerRef'],
        decoder: '/js/jessibuca-pro/decoder-pro.js',
        videoBuffer: Number(0.2), // 缓存时长
        isResize: false,
        useWCS: false,
        useMSE: false,
        useSIMD: true,
        wcsUseVideoRender: false,
        loadingText: '加载中',
        debug: false,
        debugLevel: 'debug',
        showBandwidth: true, // 显示网速
        showPlaybackOperate: true,
        operateBtns: operateBtns.value,
        forceNoOffscreen: true,
        isNotMute: true, // 默认关闭声音
        showPerformance: false,
        // playFailedAndReplay: true,
        // networkDelayTimeoutReplay: true,
        playbackForwardMaxRateDecodeIFrame: 4,
        useWebGPU: useWebGPU.value, // 使用WebGPU
    });
    let jessibuca = jessibucaPlayer[uid.value];
    initcallback(jessibuca);
    isInit.value = true;
}
function initconf() {
    if (playtype.value === 'play') {
        //直播按钮配置
        operateBtns.value.ptz = true;
    } else {
        //录像回放按钮配置
        operateBtns.value.ptz = false;
    }
}
function initcallback(jessibuca) {
    jessibuca.on('error', function (error) {
        console.log('jessibuca error');
        console.log(error);
    });
    jessibuca.on('playFailedAndPaused', function (reason, lastFrameInfo, msg) {
        console.log('playFailedAndPaused', reason, msg);
        // lastFrameInfo 是最后一帧的画面，可以用来重播的时候，显示最后一帧画面。
        // msg 具体的错误信息。
    });
    jessibuca.on('visibilityChange', (value) => {
        if (value === true) {
            // 窗口显示
            console.log('visibilityChange true');
        } else {
            // 窗口隐藏
            console.log('visibilityChange false');
        }
    });
    jessibuca.on('pause', function (pause) {
        console.log('pause success!');
        console.log(pause);
    });
    jessibuca.on('play', function (flag) {
        console.log('play!');
        console.log(flag);
    });
    jessibuca.on('loading', function (load) {
        console.log('loading success!');
        console.log(load);
    });
    jessibuca.on('stats', function (s) {
        console.log('stats is', s);
    });
    jessibuca.on('timeout', function (error) {
        console.log('timeout:', error);
    });
    jessibuca.on('playbackPreRateChange', (rate) => {
        jessibuca.forward(rate);
    });

    let pre = 0;
    let cur = 0;
    jessibuca.on('timeUpdate', function (ts) {
        cur = parseInt(ts / 60000);
        if (pre !== cur) {
            pre++;
        }
    });
    jessibuca.on(JessibucaPro.EVENTS.ptz, (arrow) => {
        console.log('ptz arrow', arrow);
        handlePtz(arrow);
    });
    jessibuca.on('crashLog', (data) => {
        console.log('crashLog is', data);
    });
}
function registercallback(events, func) {
    if (jessibucaPlayer[uid.value]) {
        jessibucaPlayer[uid.value].on(events, func);
    }
}
function isMobile() {
    return /iphone|ipad|android.*mobile|windows.*phone|blackberry.*mobile/i.test(window.navigator.userAgent.toLowerCase());
}
function isPad() {
    return /ipad|android(?!.*mobile)|tablet|kindle|silk/i.test(window.navigator.userAgent.toLowerCase());
}
function play(url) {
    console.log(url)
    if (jessibucaPlayer[uid.value]) {
        console.log(1111111)
        jessibucaPlayer[uid.value].play(url);
    }
}
function pause() {
    if (jessibucaPlayer[uid.value]) {
        jessibucaPlayer[uid.value].pause();
    }
}
function replay(url) {
    if (jessibucaPlayer[uid.value]) {
        jessibucaPlayer[uid.value].destroy().then(() => {
            initplayer();
            play(url);
        });
    } else {
        initplayer();
        play(url);
    }
}
function handlePtz(arrow) {
    let leftRight = 0;
    let upDown = 0;
    if (arrow === 'left') {
        leftRight = 2;
    } else if (arrow === 'right') {
        leftRight = 1;
    } else if (arrow === 'up') {
        upDown = 1;
    } else if (arrow === 'down') {
        upDown = 2;
    }
    var data = {
        leftRight: leftRight,
        upDown: upDown,
        moveSpeed: 125,
    };
    if (playinfo.value && playinfo.value.playtype !== '') {
        ptzdirection(playinfo.value.deviceId, playinfo.value.channelId, data).then(async (response) => {
            //console.log("云台方向控制：" + JSON.stringify(response));
        });
    }
}
function playback(url, playTimes) {
    if (jessibucaPlayer[uid.value]) {
        jessibucaPlayer[uid.value].playback(url, {
            playList: playTimes,
            fps: 25, //FPS（定频(本地设置)生效）
            showControl: true,
            isUseFpsRender: true, // 是否使用固定的fps渲染，如果设置的fps小于流推过来的，会造成内存堆积甚至溢出
            isCacheBeforeDecodeForFpsRender: false, // rfs渲染时，是否在解码前缓存数据
            supportWheel: true, // 是否支持滚动轴切换精度。
        });
        isPlaybackPause.value = false;
    }
}
function playbackPause() {
    if (jessibucaPlayer[uid.value]) {
        jessibucaPlayer[uid.value].playbackPause();
        isPlaybackPause.value = true;
    }
}
function replayback(url, playTimes) {
    if (jessibucaPlayer[uid.value]) {
        jessibucaPlayer[uid.value].destroy().then(() => {
            initplayer();
            playback(url, playTimes);
        });
    } else {
        initplayer();
        playback(url, playTimes);
    }
}
function setPlaybackStartTime(curTime) {
    if (jessibucaPlayer[uid.value]) {
        jessibucaPlayer[uid.value].setPlaybackStartTime(curTime);
    }
}
function destroy() {
    if (jessibucaPlayer[uid.value]) {
        jessibucaPlayer[uid.value].destroy().then(() => {
            initplayer();
        });
    }
}
function close() {
    if (jessibucaPlayer[uid.value]) {
        jessibucaPlayer[uid.value].close();
    }
}

playinfo.value = props.playerinfo;
if (playinfo.value && playinfo.value.playtype !== '') {
    playtype.value = playinfo.value.playtype;
}
init();

defineExpose({
    registercallback, play, destroy
});
</script>

<style scoped lang="scss">
.root {
    display: flex;
    margin-right: 3rem;
}

.container-shell {
    backdrop-filter: blur(5px);
    // background: hsla(0, 0%, 50%, 0.5);
    //background: #fff;
    //padding: 10px 4px 10px 4px;
    /* border: 2px solid black; */
    // width: auto;
    position: relative;
    border-radius: 10px;
    // box-shadow: 0 5px 5px;
}

.container-shell:before {
    //content: "设备播放器";
    position: absolute;
    color: darkgray;
    //top: 4px;
    left: 10px;
    //text-shadow: 1px 1px black;
}

#container {
    background: rgba(13, 14, 27, 0.7);
    width: 1000px;
    height: 630px;
    border-radius: 5px;
}

.err {
    position: absolute;
    top: 40px;
    left: 10px;
    color: red;
}

.option {
    position: absolute;
    top: 4px;
    right: 10px;
    display: flex;
    place-content: center;
    font-size: 12px;
}

.option span {
    color: white;
}

@media (max-width: 720px) {
    #container {
        width: 90vw;
        height: 52.7vw;
    }
}
</style>
