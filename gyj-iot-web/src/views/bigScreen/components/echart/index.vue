<template>
  <div :id="id" :class="className" :style="{ height: height, width: width }" />
</template>

<script setup name="echart">
import tdTheme from './theme.json' // 引入默认主题

const { proxy } = getCurrentInstance();

const chart = ref(null);
const options = ref({})

const props = defineProps({
  className: {
    type: String,
    default: 'chart'
  },
  id: {
    type: String,
    default: 'chart'
  },
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: '100%'
  },
  options: {
    type: Object,
    default: ()=>({})
  }
});

watch(() => options.value,  (newOptions) => {
    // 设置 true 清空 echart 缓存
    chart.value.setOption(newOptions, true)
  },
  { deep: true } // 深度监听
)

onMounted(() => {
  initChart();
})

onBeforeUnmount(() => {
  chart.value.dispose()
  chart.value = null
})

function initChart () {
  // 初始化echart
  chart.value = echarts.init(proxy.$el, 'tdTheme')
  chart.value.setOption(options.value, true)
}
</script>

<style>
</style>
