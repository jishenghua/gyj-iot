<template>
<div style="padding-left:20px;">
    <el-row :gutter="10">
        <el-col :span="14">
            <el-row :gutter="10" class="mb8">
                <el-col :span="1.5">
                    <el-button type="warning" plain icon="Refresh" @click="getList">刷新</el-button>
                </el-col>
                <el-tag type="danger" style="margin-left:15px;">该功能暂不可用,后面版本发布</el-tag>
            </el-row>
            <el-table v-loading="loading" :data="modelList" border style="margin-bottom:60px;margin-top:20px;" size="small">
                <el-table-column label="名称" align="center" prop="modelName" />
                <el-table-column label="标识符" align="center" prop="identifier" />
                <el-table-column label="物模型类别" align="center" prop="type">
                    <template #default="scope">
                        <dict-tag :options="iot_things_type" :value="scope.row.type" />
                    </template>
                </el-table-column>
                <el-table-column label="数据类型" align="center" prop="datatype">
                    <template #default="scope">
                        <dict-tag :options="iot_data_type" :value="scope.row.datatype" />
                    </template>
                </el-table-column>
                <el-table-column label="部件" align="center" prop="part">
                    <template #default="scope">
                        {{scope.row.part}} 系统定义的部件
                    </template>
                </el-table-column>
            </el-table>

            <el-divider>高级功能</el-divider>
            <el-form ref="productAppRef" :model="form" label-width="100px">
                <el-form-item label="自定义页面" prop="page">
                    <el-input v-model="form.page" placeholder="请输入自定义页面" />
                </el-form-item>
            </el-form>

        </el-col>
        <el-col :span="8" :offset="2">
            <div class="phone">
                <div class="phone-container"></div>
            </div>
            <div style="text-align:center;margin-top:15px;width:370px;">界面展示</div>
        </el-col>

    </el-row>
</div>
</template>

<script setup name="ProductApp">
import { listModel } from "@/api/iot/model";

const { proxy } = getCurrentInstance();
const { iot_things_type, iot_data_type, iot_yes_no } = proxy.useDict('iot_things_type', 'iot_network_method', 'iot_yes_no');

// 遮罩层
const loading = ref(false);
// 产品物模型表格数据
const modelList = ref([]);
// 弹出层标题
const title = ref("");
// 产品
const productInfo = ref({});

const data = reactive({
    // 查询参数
    queryParams: {
        productId: 0,
        // 1-属性，2-功能，3-事件，4-属性和功能
        type: 4,
    },
    form: {},
})

const { queryParams, form } = toRefs(data);

const props = defineProps({
    product: {
        type: Object,
        default: null
    }
});

// 获取到父组件传递的productId后，刷新列表
watch(() => props.product, (newVal, oldVal) => {
    productInfo.value = newVal;
    if (productInfo.value && productInfo.value.productId != 0) {
        queryParams.value.productId = productInfo.value.productId;
        getList();
    }
});

/** 查询产品物模型列表 */
function getList() {
    loading.value = true;
    listModel(queryParams.value).then((response) => {
        modelList.value = response.rows;
        total.value = response.total;
        loading.value = false;
    });
}
</script>

<style scoped>
.phone {
    height: 700px;
    width: 370px;
    background-image: url("../../../assets/images/phone.jpg");
    background-size: cover;
    top: 0px;
}

.phone-container {
    height: 620px;
    width: 345px;
    border-radius: 20px;
    position: relative;
    top: 45px;
    left: 12px;
    border: 1px solid #888;
    background: linear-gradient(303deg, #b2e9fc 50%, #b5c4f8 50%);

}
</style>
