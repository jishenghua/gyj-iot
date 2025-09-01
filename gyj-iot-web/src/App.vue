<template>
  <div id="app" style="background-color:#0e2e87" v-if="$route.meta.bigScreen">
    <router-view />
  </div>
  <div id="app" v-else>
    <router-view />
  </div>
</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'

onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(useSettingsStore().theme)
  })
})
</script>
