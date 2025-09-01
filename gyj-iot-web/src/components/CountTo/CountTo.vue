<template>
    <span>{{ animatedValue }}</span>
</template>

<script setup>
    import { ref, watch, onMounted } from 'vue'

    const props = defineProps({
        startVal: { type: Number, default: 0 },
        endVal: { type: Number, required: true },
        duration: { type: Number, default: 2000 }
    })

    const animatedValue = ref(props.startVal)
    let startTime

    const animate = (timestamp) => {
        if (!startTime) startTime = timestamp
        const progress = Math.min((timestamp - startTime) / props.duration, 1)
        animatedValue.value = Math.floor(props.startVal + progress * (props.endVal - props.startVal))
        if (progress < 1) {
            requestAnimationFrame(animate)
        }
    }

    watch(() => props.endVal, (newVal) => {
        startTime = null
        requestAnimationFrame(animate)
    })

    onMounted(() => {
        requestAnimationFrame(animate)
    })
</script>
