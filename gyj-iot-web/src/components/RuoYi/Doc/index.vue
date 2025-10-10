<template>
  <div>
    <svg-icon icon-class="question" @click="goto" />
  </div>
</template>

<script setup>
const url = ref('https://www.gyjerp.com/iot/');

function goto() {
  window.open(url.value)
}
</script>
