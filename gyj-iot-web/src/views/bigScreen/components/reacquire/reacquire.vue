<template>
    <div class='reacquire flex justify-center  blocks  cursor-pointer' :style="{ lineHeight: lineHeight }"
        @click="getData">
        <span>
             重新获取
        </span>
       
    </div>
</template>

<script setup name="reacquire">
const { proxy } = getCurrentInstance();

const props = defineProps({
    lineHeight: {
        type: String,
        default: '200px'
    }
});

function init() {
}

function getData(e){
    proxy.$emit("onclick",e)
}

</script>

<style lang='scss' scoped>
.reacquire {

    user-select:none;
    color: rgb(168, 168, 168);
    span:hover{
        // color:$primary-color ;
        text-decoration: underline;
    }
}

.blocks {
    width: 100%;
    height: 100%;
}

.cursor-pointer {
    cursor: pointer;
}
</style>