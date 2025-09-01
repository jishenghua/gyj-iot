<template>
    <el-dialog :title="title" v-model="open" width="400px" append-to-body :close-on-click-modal="false">
        <el-form ref="createFormRef" :model="createForm" label-width="100px" :rules="rules">
            <el-form-item label="行政区划" prop="city">
                <el-cascader :options="cityOptions" v-model="createForm.city" @change="changeProvince" change-on-select :props="{ checkStrictly: false }"></el-cascader>
            </el-form-item>
            <el-form-item label="设备类型" prop="deviceType">
                <el-select v-model="createForm.deviceType" placeholder="请选择设备类型">
                    <el-option v-for="dict in video_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="通道类型" prop="channelType">
                <el-select v-model="createForm.channelType" placeholder="请选择设备类型">
                    <el-option v-for="dict in channel_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="通道数量" prop="createNum">
                <el-input-number controls-position="right" :min="1" :max="10" v-model="createForm.createNum" placeholder="请输入生成通道数量" type="number" style="width: 220px" />
            </el-form-item>
        </el-form>
        <template #footer>
            <div class="dialog-footer">
                <el-button type="primary" @click="submitForm">生 成</el-button>
                <el-button @click="closeDialog">取 消</el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script setup name="SipidDialog">
import { regionData, CodeToText } from 'element-china-area-data';

import { addChannel } from '@/api/iot/channel';

const { proxy } = getCurrentInstance();
const { video_type, channel_type } = proxy.useDict('video_type', 'channel_type');

// 遮罩层
const loading = ref(true);
const title = ref('生成设备编号和通道');
// 总条数
const total = ref(0);
// 打开选择产品对话框
const open = ref(false);
const devsipid = ref('');
// 城市
const cityOptions = ref(regionData);
const city = ref('');
const cityCode = ref('');

const data = reactive({
    createForm: {
        city: '',
        deviceType: '',
        channelType: '',
        createNum: 1,
    },
    // 表单校验
    rules: {
        city: [{ required: true, message: '行政区划不能为空', trigger: 'change' }],
        deviceType: [{ required: true, message: '设备类型不能为空', trigger: 'change' }],
        channelType: [{ required: true, message: '通道类型不能为空', trigger: 'change' }],
        createNum: [{ required: true, message: '请输入生成通道数量', trigger: 'change' }],
    }
})

const { createForm, rules } = toRefs(data);

const props = defineProps({
    product: {
        type: Object,
        default: null,
    }
});

/** 行政区划改变 **/
function changeProvince(data) {
    if (data && data[0] != null && data[1] != null && data[2] != null) {
        const str = CodeToText[data[0]] + '/' + CodeToText[data[1]] + '/' + CodeToText[data[2]];
        createForm.value.citycode = str;
    }
}
/** 提交按钮 */
function submitForm() {
    proxy.$refs["createFormRef"].validate((valid) => {
        if (valid) {
            createForm.value.productId = props.product.productId;
            createForm.value.productName = props.product.productName;
            createForm.value.tenantId = props.product.tenantId;
            createForm.value.tenantName = props.product.tenantName;
            createForm.value.deviceSipId = createForm.value.city[2] + '0000' + createForm.value.deviceType + '0';
            createForm.value.channelSipId = createForm.value.city[2] + '0000' + createForm.value.channelType + '0';
            addChannel(createForm.value.createNum, createForm.value).then((response) => {
                proxy.$modal.msgSuccess('已生成设备编号和通道');
                devsipid.value = response.data;
                confirmSelectProduct();
            });
        }
    });
}

function confirmSelectProduct() {
    open.value = false;
    proxy.$emit('addGenEvent', devsipid.value);
}
/**关闭对话框 */
function closeDialog() {
    open.value = false;
}

function show() {
    open.value = true;
}

defineExpose({
    show
});
</script>

<style scoped></style>
